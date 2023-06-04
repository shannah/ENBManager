package io.github.palexdev.enbmanager.model.repo;

import io.github.palexdev.enbmanager.SpringHelper;
import io.github.palexdev.enbmanager.components.dialogs.DialogBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.events.ConfigsChangedEvent;
import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.utils.FileUtils;
import io.github.palexdev.enbmanager.views.MainView;
import javafx.stage.Modality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigsRepo {
    //================================================================================
    // Properties
    //================================================================================
    private final DialogServiceBase dialogs;
    private final Game game;
    private final Path configsPath;
    private final Map<Path, Config> configs = new LinkedHashMap<>();

    //================================================================================
    // Constructors
    //================================================================================
    public ConfigsRepo(Game game, Path repoPath) {
        this.dialogs = SpringHelper.getBean(DialogServiceBase.class);
        this.game = game;
        this.configsPath = initConfigsPath(repoPath);
        detectConfigs();
    }

    //================================================================================
    // Methods
    //================================================================================
    public boolean load(Path gamePath, Config config) {
        if (!configs.containsKey(config.path())) return false;
        return doLoad(gamePath, config);
    }

    public boolean save(Path gamePath, String name, Collection<? extends Path> files) {
        Path path = configsPath.resolve(name);
        if (configs.containsKey(path)) {
            boolean overwrite = askOverwrite(name);
            if (overwrite) doDelete(configs.get(path));
        }
        Config config = Config.from(path).addFiles(files);
        boolean saved = doSave(gamePath, config);
        configs.put(path, config);
        configsChanged();
        return saved;
    }

    public boolean delete(Config config) {
        if (!configs.containsKey(config.path())) return false;
        boolean deleted = doDelete(config);
        configsChanged();
        return deleted;
    }

    protected boolean doLoad(Path gamePath, Config config) {
        try {
            Path path = config.path();
            for (Path source : config.files()) {
                Path relative = path.relativize(source);
                Path target = gamePath.resolve(relative);
                FileUtils.copy(source, target);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(); // TODO improve?
            return false;
        }
    }

    protected boolean doSave(Path gamePath, Config config) {
        try {
            Path path = config.path();
            for (Path source : config.files()) {
                Path relative = gamePath.relativize(source);
                Path target = path.resolve(relative);
                FileUtils.copy(source, target);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(); // TODO improve?
            return false;
        }
    }

    protected boolean doDelete(Config config) {
        try {
            FileUtils.delete(config.path());
            configs.remove(config.path());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(); // TODO improve?
            detectConfigs();
            return false;
        }
    }

    protected Path initConfigsPath(Path repoPath) {
        try {
            Path path = repoPath.resolve(game.name());
            if (!Files.isDirectory(path)) Files.createDirectories(path);
            return path;
        } catch (IOException ex) {
            dialogs.showDialog(MainView.class, DialogBase.fatal("Ok"),
                () -> new DialogConfig<>()
                    .setShowAlwaysOnTop(false)
                    .setShowMinimize(false)
                    .setCenterInOwnerNode(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Fatal error")
                    .setContentText("Unsupported OS detected. App will shutdown!")
            );
            return null;
        }
    }

    protected void detectConfigs() {
        if (!isInitialized()) return;
        configs.clear();
        try (Stream<Path> stream = Files.list(configsPath)) {
            List<Path> dirs = stream.filter(Files::isDirectory).toList();
            for (Path path : dirs) {
                Config config = readConfigDir(path);
                if (config != null) configs.put(path, config);
            }
        } catch (IOException ex) {
            dialogs.showDialog(MainView.class, DialogBase.error(),
                () -> new DialogConfig<>()
                    .setShowAlwaysOnTop(false)
                    .setShowMinimize(false)
                    .setCenterInOwnerNode(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Failed to detect configs for game %s".formatted(game.name()))
                    .setContentText("Reason: %s".formatted(ex.getMessage()))
            );
        } finally {
            configsChanged();
        }
    }

    protected Config readConfigDir(Path dir) {
        String dirName = dir.getFileName().toString();
        try (Stream<Path> stream = Files.list(dir)) {
            Config config = Config.from(dir);
            stream.forEach(config::addFiles);
            return config;
        } catch (IOException ex) {
            dialogs.showDialog(MainView.class, DialogBase.error(),
                () -> new DialogConfig<>()
                    .setShowAlwaysOnTop(false)
                    .setShowMinimize(false)
                    .setCenterInOwnerNode(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Failed to read config %s".formatted(dirName))
                    .setContentText("Reason: %s".formatted(ex.getMessage()))
            );
            return null;
        }
    }

    protected boolean askOverwrite(String name) {
        return dialogs.showConfirmDialog(MainView.class, DialogBase.DialogType.WARN, "Overwrite",
            () -> new DialogConfig<>()
                .implicitOwner()
                .setModality(Modality.WINDOW_MODAL)
                .setHeaderText("Overwrite config")
                .setContentText("A configuration by the name of %s is already saved, overwrite?".formatted(name))
        );
    }

    protected void configsChanged() {
        SpringHelper.notify(new ConfigsChangedEvent(configs.values()));
    }

    //================================================================================
    // Getters
    //================================================================================
    public boolean isInitialized() {
        return configsPath != null;
    }

    public Game getGame() {
        return game;
    }

    public Path getConfigsPath() {
        return configsPath;
    }
}
