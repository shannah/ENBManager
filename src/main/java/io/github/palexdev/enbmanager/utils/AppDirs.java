package io.github.palexdev.enbmanager.utils;

import io.github.palexdev.enbmanager.ENBManager;
import io.github.palexdev.enbmanager.components.dialogs.DialogBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.views.MainView;
import javafx.stage.Modality;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AppDirs {
    //================================================================================
    // Properties
    //================================================================================
    private final OSUtils os;
    private final DialogServiceBase dialogs;
    private Path configPath;
    private Path cachePath;

    //================================================================================
    // Constructors
    //================================================================================
    public AppDirs(OSUtils os, DialogServiceBase dialogs) {
        this.os = os;
        this.dialogs = dialogs;
    }

    //================================================================================
    // Methods
    //================================================================================
    public Path getConfigPath() {
        if (configPath == null) {
            OSUtils.OSType type = os.getOSType();
            Path path = switch (type) {
                case Windows -> Path.of(System.getenv("APPDATA"));
                case Linux -> Path.of(System.getProperty("user.home"), ".config");
                case MacOS -> Path.of(System.getProperty("user.home"), "Library", "Application Support");
                default -> null;
            };

            if (path == null) return unsupportedOS();
            if (!Files.isDirectory(path)) return pathNotFound(path);

            path = path.resolve(ENBManager.APP_NAME);
            configPath = tryCreate(path);
        }
        return configPath;
    }

    public Path getCachePath() {
        if (cachePath == null) {
            OSUtils.OSType type = os.getOSType();
            Path path = switch (type) {
                case Windows -> Path.of(System.getenv("APPDATA"));
                case Linux -> Path.of(System.getProperty("user.home"), ".cache");
                case MacOS -> Path.of(System.getProperty("user.home"), "Library", "Caches");
                case Other -> null;
            };

            if (path == null) return unsupportedOS();
            if (!Files.isDirectory(path)) return pathNotFound(path);
            if (type == OSUtils.OSType.Windows) {
                path = path.resolve("Cache");
                try {
                    Files.createDirectories(path);
                } catch (IOException ex) {
                    fatal(
                        ("""
                            Unable to create cache directory at: %s
                            Reason: %s
                            App will shutdown!"""
                        ).formatted(path, ex.getMessage()));
                }
            }

            path = path.resolve(ENBManager.APP_NAME);
            cachePath = tryCreate(path);
        }
        return cachePath;
    }

    public Path getProjectPath() {
        return Path.of(System.getProperty("user.dir"));
    }

    private Path tryCreate(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException ex) {
            fatal("Failed to create directory %s, because: %s\nApp will shutdown!".formatted(path, ex.getMessage()));
            return null;
        }
    }

    private Path unsupportedOS() {
        fatal("Unsupported OS detected. App will shutdown!");
        return null;
    }

    private Path pathNotFound(Path path) {
        fatal("Path %s not found. App will shutdown!".formatted(path));
        return null;
    }

    private void fatal(String reason) {
        dialogs.showDialog(MainView.class, DialogBase.fatal("Ok"), () -> new DialogConfig<>()
            .setShowAlwaysOnTop(false)
            .setShowMinimize(false)
            .setCenterInOwnerNode(false)
            .setModality(Modality.APPLICATION_MODAL)
            .setHeaderText("Fatal error")
            .setContentText(reason)
        );
    }
}
