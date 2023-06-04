package io.github.palexdev.enbmanager.theming;

import io.github.palexdev.enbmanager.settings.AppSettings;
import io.github.palexdev.enbmanager.settings.base.StringSetting;
import io.github.palexdev.enbmanager.utils.AppDirs;
import io.github.palexdev.mfxcomponents.theming.JavaFXThemes;
import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import io.github.palexdev.mfxcomponents.theming.UserAgentBuilder;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import io.github.palexdev.mfxcore.utils.fx.CSSFragment;
import io.methvin.watcher.DirectoryWatcher;
import io.methvin.watcher.hashing.FileHash;
import io.methvin.watcher.hashing.FileHasher;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Component
public class ThemeManager {
    public enum Mode {
        LIGHT, DARK
    }

    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>() {
        @Override
        public void set(Theme newValue) {
            Theme val = getTheme(newValue.name(), getMode());
            if (val == null) val = newValue;
            super.set(val);
        }

        @Override
        protected void invalidated() {
            onThemeChanged();
        }
    };
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>() {
        @Override
        public void set(Mode newValue) {
            Mode oldValue = get();
            super.set(newValue);
            if (oldValue != null && !Objects.equals(oldValue, newValue)) onModeChanged();
        }

        @Override
        protected void invalidated() {
            settings.lastThemeMode.set(get() == Mode.DARK);
        }
    };
    private final Theme appTheme = Stylesheets.APP_THEME;

    private CSSFragment uas;
    private final AppDirs dirs;
    private final AppSettings settings;

    private final ScheduledExecutorService executor;
    private DirectoryWatcher watcher;
    private CompletableFuture<Void> watcherTask;

    private FileHash lastHash;

    //================================================================================
    // Constructors
    //================================================================================
    public ThemeManager(AppDirs dirs, AppSettings settings) {
        this.dirs = dirs;
        this.settings = settings;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        // Trigger path init so that if path can't be used the app shutdowns
        getCachePath();

        // Deploy needed theme assets
        JavaFXThemes.MODENA.deploy();
        MaterialThemes.PURPLE_LIGHT.deploy();

        // Initialize theme
        setTheme(detectLastTheme());

        // Check and invalidate cache if requested
        if (settings.isInvalidateThemesCache()) invalidateCache();
    }

    //================================================================================
    // Methods
    //================================================================================
    public void debugWatch() {
        Path projectPath = dirs.getProjectPath();
        Path source = projectPath.resolve("src/main/resources/io/github/palexdev/enbmanager/css/AppTheme.css");
        Path target = projectPath.resolve("build/resources/main/io/github/palexdev/enbmanager/css/AppTheme.css");
        if (!Files.exists(source) || !Files.exists(target)) return;
        try {
            FileHasher hasher = FileHasher.DEFAULT_FILE_HASHER;
            executor.scheduleAtFixedRate(() -> {
                try {
                    FileHash hash = hasher.hash(source);
                    if (Objects.equals(lastHash, hash)) return;
                    lastHash = hash;
                    Files.writeString(target, Files.readString(source), CREATE, TRUNCATE_EXISTING);
                    buildUserAgent();
                } catch (Exception ignored) {}
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    public void watch(boolean rebuild) {
        if (watcher != null) stopWatch();
        try {
            Path path = getCachePath();
            watcher = DirectoryWatcher.builder()
                .path(path)
                .listener(e -> {
                    String themeName = getThemeName();
                    if (themeName.isBlank()) return;
                    if (e.isDirectory()) return;
                    Path themePath = e.path();
                    if (themePath.getFileName().toString().equals(themeName))
                        reloadThemeFromDisk(themePath, rebuild);
                }).build();
            watcherTask = watcher.watchAsync(executor);
        } catch (IOException ignored) {}
    }

    public void stopWatch() {
        try {
            if (watcher != null) {
                watcher.close();
                watcherTask.cancel(true);
                watcher = null;
                watcherTask = null;
            }
        } catch (IOException ignored) {}
    }

    public void invalidateCache() {
        Path path = getCachePath();
        try {
            File[] files = path.toFile().listFiles((dir, name) -> name.endsWith(".theme"));
            if (files == null) return;
            for (File file : files) file.delete();
        } finally {
            buildUserAgent();
        }
    }

    protected void onThemeChanged() {
        // Check if cached of file-system
        Path cache = getCachePath().resolve(getThemeName());
        if (Files.exists(cache) && cache.toFile().isFile()) {
            reloadThemeFromDisk(cache, false);
        } else {
            buildUserAgent();
        }
        settings.lastTheme.set(getTheme().name());
    }

    protected void onModeChanged() {
        Theme theme = getTheme();
        Mode mode = getMode();
        String name = theme.name();
        if (mode == Mode.LIGHT) name = name.replace("DARK", "LIGHT");
        if (mode == Mode.DARK) name = name.replace("LIGHT", "DARK");
        setTheme(MaterialThemes.valueOf(name));
    }

    protected void reloadThemeFromDisk(Path path, boolean rebuild) {
        if (rebuild) {
            buildUserAgent();
            return;
        }
        try {
            String content = Files.readString(path);
            uas = new CSSFragment(content);
            Application.setUserAgentStylesheet(uas.toDataUri());
        } catch (Exception ignored) {}
    }

    protected void buildUserAgent() {
        // Build it
        Theme theme = getTheme();
        uas = UserAgentBuilder.builder()
            .themes(JavaFXThemes.MODENA, theme, appTheme)
            .setResolveAssets(true)
            .build();
        // Cache it
        try {
            String themeName = getThemeName();
            Path themePath = getCachePath().resolve(themeName);
            Files.writeString(themePath, uas.toString(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException ignored) {}
        // Set it
        Application.setUserAgentStylesheet(uas.toDataUri());
    }

    private Theme detectLastTheme() {
        StringSetting setting = settings.lastTheme;
        Mode mode = (settings.lastThemeMode.get()) ? Mode.DARK : Mode.LIGHT;
        setMode(mode);
        Theme theme = getTheme(setting.get(), mode);
        if (theme == null) {
            setting.reset();
            theme = getTheme(setting.get(), mode);
        }
        return theme;
    }

    private Theme getTheme(String name, Mode mode) {
        if (mode == Mode.LIGHT) name = name.replace("DARK", "LIGHT");
        if (mode == Mode.DARK) name = name.replace("LIGHT", "DARK");
        try {
            return MaterialThemes.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }

    private String getThemeName() {
        Theme theme = getTheme();
        if (theme == null) return "";
        return theme.name() + ".theme";
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Theme getTheme() {
        return theme.get();
    }

    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    public Mode getMode() {
        return mode.get();
    }

    public ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode.set(mode);
    }

    public Path getCachePath() {
        return dirs.getCachePath();
    }
}
