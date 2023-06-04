package io.github.palexdev.enbmanager.model.games;

import io.github.palexdev.enbmanager.settings.AppSettings;
import io.github.palexdev.enbmanager.settings.base.GameSettings;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages some aspects of the games supported by the app.
 * Also offers a list of all the games.
 */
@Component
public class GamesManager {
    //================================================================================
    // Properties
    //================================================================================
    private final AppSettings settings;
    private final List<Game> games;

    //================================================================================
    // Constructors
    //================================================================================
    public GamesManager(AppSettings settings, List<Game> games) {
        this.settings = settings;
        this.games = games;
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Queries the app settings to check whether a user chose a game in a previous session, and he decided to save the
     * preference for future sessions.
     * <p>
     * If the previous choice is detected, there's an additional check performed by {@link #detectExecutable(Game)}
     * before returning.
     */
    public Optional<Game> detectLastGame() {
        String s = settings.lastGame.get();
        if (s == null || s.isBlank()) return Optional.empty();
        Optional<Game> opt = games.stream()
            .filter(g -> g.name().equals(s))
            .findFirst();
        if (opt.isPresent()) {
            Game game = opt.get();
            if (detectExecutable(game).isEmpty()) return Optional.empty();
        }
        return opt;
    }

    /**
     * For the given game, retrieves its settings by using {@link Game#getSettings()}, and checks whether the game
     * executable is still present on the previously saved path.
     * <p></p>
     * For convenience the return value is an {@link Optional} that contains the path if invalid otherwise is empty.
     */
    public Optional<Path> detectExecutable(Game game) {
        GameSettings gSettings = game.getSettings();
        Path path = Path.of(gSettings.path.get());
        return Files.isDirectory(path) && Files.exists(path.resolve(game.exeName())) ?
            Optional.of(path) :
            Optional.empty();
    }

    //================================================================================
    // Getters
    //================================================================================

    /**
     * @return an unmodifiable list containing all the supported games
     */
    public List<Game> getGames() {
        return Collections.unmodifiableList(games);
    }
}
