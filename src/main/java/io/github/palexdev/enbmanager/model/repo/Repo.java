package io.github.palexdev.enbmanager.model.repo;

import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.utils.AppDirs;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collection;

@Component
public class Repo {
    //================================================================================
    // Properties
    //================================================================================
    private final AppDirs dirs;
    private ConfigsRepo configsRepo;

    //================================================================================
    // Constructors
    //================================================================================
    public Repo(AppDirs dirs) {
        this.dirs = dirs;
        dirs.getConfigPath(); // Trigger path init so that if path can't be used the app shutdowns
    }

    //================================================================================
    // Methods
    //================================================================================
    public boolean loadConfig(Path gamePath, Config config) {
        if (configsRepo == null) return false;
        return configsRepo.load(gamePath, config);
    }

    public boolean saveConfig(Path gamePath, String name, Collection<? extends Path> files) {
        if (configsRepo == null) return false;
        return configsRepo.save(gamePath, name, files);
    }

    public boolean deleteConfig(Config config) {
        if (configsRepo == null) return false;
        return configsRepo.delete(config);
    }

    public void refreshConfigs() {
        if (configsRepo == null) return;
        configsRepo.detectConfigs();
    }

    public ConfigsRepo getConfigsRepo(Game game) {
        if (configsRepo == null || configsRepo.getGame() != game) {
            configsRepo = new ConfigsRepo(game, getRepoPath());
        }
        return configsRepo;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Path getRepoPath() {
        return dirs.getConfigPath();
    }
}
