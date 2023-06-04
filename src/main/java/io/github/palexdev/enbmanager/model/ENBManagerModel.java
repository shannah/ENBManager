package io.github.palexdev.enbmanager.model;

import io.github.palexdev.enbmanager.components.dialogs.DialogBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.events.AppCloseEvent;
import io.github.palexdev.enbmanager.events.ConfigsChangedEvent;
import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.model.repo.Config;
import io.github.palexdev.enbmanager.model.repo.Repo;
import io.github.palexdev.enbmanager.utils.FileUtils;
import io.github.palexdev.enbmanager.utils.PathsComparator;
import io.github.palexdev.enbmanager.views.MainView;
import io.github.palexdev.mfxcore.collections.TransformableListWrapper;
import io.methvin.watcher.DirectoryWatcher;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class ENBManagerModel {
    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<Game> game = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            onGameChanged();
        }
    };
    private final ObjectProperty<Path> path = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            onPathChanged();
        }
    };
    private final ObservableList<Config> configs = FXCollections.observableArrayList();
    private final TransformableListWrapper<Path> files = new TransformableListWrapper<>(FXCollections.observableArrayList());
    private final Set<String> fileNames;

    private final Executor executor;
    private DirectoryWatcher watcher;
    private CompletableFuture<Void> watcherTask;

    private final DialogServiceBase dialogs;
    private final Repo repo;

    //================================================================================
    // Constructors
    //================================================================================
    public ENBManagerModel(DialogServiceBase dialogs, Repo repo) {
        this.dialogs = dialogs;
        this.repo = repo;
        executor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        files.setComparator(PathsComparator.instance());
        fileNames = getFileNames();
    }

    /* Actions */
    public boolean load(Config config) {
        return repo.loadConfig(getPath(), config);
    }

    public boolean save(String name, Collection<? extends Path> files) {
        if (name.isBlank()) return false;
        return repo.saveConfig(getPath(), name, files);
    }

    public boolean delete(Config config) {
        return repo.deleteConfig(config);
    }

    public boolean delete(Collection<Path> files) {
        try {
            for (Path file : files) {
                FileUtils.delete(file);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(); // TODO improve?
            return false;
        }
    }

    /* Model and FileSystem */
    protected void onGameChanged() {
        CompletableFuture.runAsync(() -> {
            Game game = getGame();
            repo.getConfigsRepo(game);
        });
    }

    protected void onPathChanged() {
        CompletableFuture.runAsync(() -> {
            updateFiles();
            updateWatcher();
        }, executor);
    }

    public void updateFiles() {
        Path path = getPath();
        if (path == null || !Files.isDirectory(path)) {
            files.clear();
            return;
        }
        Set<Path> files = fileNames.stream()
            .map(path::resolve)
            .filter(Files::exists)
            .collect(Collectors.toSet());
        Platform.runLater(() -> this.files.setAll(files));
    }

    public void refreshConfigs() {
        repo.refreshConfigs();
    }

    protected void updateWatcher() {
        try {
            Path path = getPath();
            if (watcher != null) {
                watcher.close();
                watcherTask.cancel(true);
                watcher = null;
                watcherTask = null;
            }
            watcher = DirectoryWatcher.builder()
                .path(path)
                .listener(e -> updateFiles())
                .build();
            watcherTask = watcher.watchAsync(executor);
        } catch (IOException ex) {
            dialogs.showDialog(MainView.class, DialogBase.error(),
                () -> new DialogConfig<>()
                    .implicitOwner()
                    .setShowMinimize(false)
                    .setShowAlwaysOnTop(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Failed to build DirectoryWatcher service!")
                    .setContentText(
                        "Changes made to the game directory won't be detected automatically." +
                            "Reason: %s".formatted(ex.getMessage())
                    )
            );
        }
    }

    //================================================================================
    // Events
    //================================================================================
    @EventListener
    public void onConfigsChanged(ConfigsChangedEvent event) {
        configs.clear();
        configs.addAll(event.getConfigs());
        /* TODO setAll fails, VirtualizedFX bug. FIX ME
         *  possible solution, treat setAll as clear and then set, but preserve cells
         */
    }

    @EventListener
    public void onExit(AppCloseEvent event) {
        try {
            if (watcherTask != null) watcherTask.cancel(true);
            if (watcher != null) watcher.close();
        } catch (Exception ignored) {}
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Game getGame() {
        return game.get();
    }

    /**
     * Specifies the current game manged by the app.
     */
    public ObjectProperty<Game> gameProperty() {
        return game;
    }

    public void setGame(Game game) {
        this.game.set(game);
    }

    public Path getPath() {
        return path.get();
    }

    /**
     * Specifies the current managed game's directory.
     */
    public ObjectProperty<Path> pathProperty() {
        return path;
    }

    /**
     * @return an unmodifiable list of the current game saved configurations
     */
    public ObservableList<Config> getConfigs() {
        return FXCollections.unmodifiableObservableList(configs);
    }

    /**
     * @return an unmodifiable list containing all the detected config files in the game's directory
     */
    public ObservableList<Path> getFiles() {
        return FXCollections.unmodifiableObservableList(files);
    }

    public void setPath(Path path) {
        this.path.set(path);
    }

    public Set<String> getFileNames() {
        if (fileNames == null) {
            // Try loading from file
            try {
                Path repoPath = repo.getRepoPath();
                Path file = repoPath.resolve("files.txt");
                if (!Files.exists(file) || !file.toFile().isFile())
                    throw new FileNotFoundException();
                return Set.copyOf(Files.readAllLines(file));
            } catch (Exception ex) {
                // TODO this need to be wrote to file!
                return Set.of(
                    // Folders
                    "enbcache", "enbseries", "exes", "injFX_Shaders",
                    "_sample_enbraindrops", "ReShade", "reshade-shaders", "SweetFX",
                    "Data" + FileSystems.getDefault().getSeparator() + "Shaders",
                    // Files
                    "common.fhx", "d3d9.dll", "d3d9.fx", "d3d9injFX.dll",
                    "d3d9SFX.dll", "d3d9_aa.dll", "d3dcompiler_46e.dll", "d3d9_fx.dll",
                    "d3d9_fxaa.dll", "d3d9_SFX.dll", "d3d9_SFX_FXAA.dll", "d3d9_SFX_SMAA.dll",
                    "d3d9_Sharpen.dll", "d3d9_smaa.dll", "d3d9_SweetFX.dll", "d3d11.dll",
                    "d3dx9.dll", "dxgi.dll", "dxgi.fx", "dxgi.ini",
                    "eax.dll", "EED_verasansmono.bmp", "effect.txt", "enb.dll",
                    "enbadaptation.fx", "enbbloom.fx", "enbdepthoffield.fx", "enbdepthoffield.fx.ini",
                    "enbdepthoffield.ini", "enbeffect.fx", "enbeffectpostpass.fx", "enbeffectprepass.fx",
                    "enbhelper.dll", "enbhost.exe", "ENBInjector.exe", "enbinjector.ini",
                    "enblens.fx", "enblensmask.png", "enblensmask.bmp", "enblocal.ini",
                    "enbpalette.bmp", "enbpatch.ini", "enbraindrops.dds", "enbraindrops_small.dds",
                    "enbraindrops.png", "enbraindrops_small.png", "enbraindrops.tga", "enbraindrops_small.tga",
                    "enbseries.ini", "enbseries.dll", "enbspectrum.bmp", "enbsunsprite.bmp",
                    "enbsunsprite.fx", "enbsunsprite.tga", "enbunderwater.fx", "enbweather.bmp",
                    "enbunderwaternoise.bmp", "EnhancedENBDiagnostics.fxh", "FixForBrightObjects.txt", "FXAA.dll",
                    "FXAA_d3d9.dll", "FXAA_Tool.exe", "injector.ini", "injFX_Settings.h",
                    "injFXAA.dll", "INSTALL.txt", "lens.fx", "license.txt",
                    "license_en.txt", "license_ru.txt", "log.txt", "log.log",
                    "ParallaxMod.txt", "readme_en.txt", "ReShade.fx", "shader.fx",
                    "SkyrimCustomShader_Config.h", "SMAA.fx", "SMAA.h", "Sweet.fx",
                    "Sweetfx_d3d9.dll", "SweetFX_preset.txt", "SweetFX_settings.txt", "technique.fxh",
                    "uninstall.exe", "_weatherlist.ini", "aaa.ini", "bbb.ini",
                    "other_d3d9.dll"
                );
            }
        }
        return fileNames;
    }
}
