package io.github.palexdev.enbmanager.model.games;

import io.github.palexdev.enbmanager.Res;
import io.github.palexdev.enbmanager.settings.SkyrimSESettings;
import io.github.palexdev.enbmanager.utils.OSUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class SkyrimSE extends GameBase<SkyrimSESettings> {

    //================================================================================
    // Constructors
    //================================================================================
    public SkyrimSE(OSUtils os, SkyrimSESettings settings) {
        super(os, settings, "Skyrim Special Edition", "SkyrimSE.exe");
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public InputStream icon() {
        return Res.loadAsset("SkyrimSE.png");
    }

    public SkyrimSESettings getSettings() {
        return settings;
    }
}
