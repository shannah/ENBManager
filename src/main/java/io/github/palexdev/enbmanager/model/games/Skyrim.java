package io.github.palexdev.enbmanager.model.games;

import io.github.palexdev.enbmanager.Res;
import io.github.palexdev.enbmanager.settings.SkyrimSettings;
import io.github.palexdev.enbmanager.utils.OSUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class Skyrim extends GameBase<SkyrimSettings> {

    //================================================================================
    // Constructors
    //================================================================================
    public Skyrim(OSUtils os, SkyrimSettings settings) {
        super(os, settings, "Skyrim", "Skyrim.exe");
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public InputStream icon() {
        return Res.loadAsset("Skyrim.png");
    }

    @Override
    public SkyrimSettings getSettings() {
        return settings;
    }
}
