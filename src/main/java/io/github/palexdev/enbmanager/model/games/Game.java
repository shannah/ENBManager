package io.github.palexdev.enbmanager.model.games;

import io.github.palexdev.enbmanager.settings.base.GameSettings;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.InputStream;

public interface Game {

    InputStream icon();

    String name();

    String exeName();

    GameSettings getSettings();

    boolean isRunning();

    default ExtensionFilter toFilter() {
        return new ExtensionFilter(exeName(), exeName());
    }
}
