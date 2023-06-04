package io.github.palexdev.enbmanager.settings.base;

import org.springframework.stereotype.Component;

@Component
public abstract class GameSettings extends Settings {
    //================================================================================
    // Settings
    //================================================================================
    public final StringSetting path = registerString("path", "Game's path from last session", "");
}
