package io.github.palexdev.enbmanager.settings;

import io.github.palexdev.enbmanager.settings.base.GameSettings;
import org.springframework.stereotype.Component;

@Component
public class SkyrimSettings extends GameSettings {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected String node() {
        return root() + "/skyrim";
    }
}
