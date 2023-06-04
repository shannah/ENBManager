package io.github.palexdev.enbmanager.settings;

import io.github.palexdev.enbmanager.settings.base.BooleanSetting;
import io.github.palexdev.enbmanager.settings.base.NumberSetting;
import io.github.palexdev.enbmanager.settings.base.Settings;
import io.github.palexdev.enbmanager.settings.base.StringSetting;
import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import javafx.application.Application;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppSettings extends Settings {
    //================================================================================
    // Settings
    //================================================================================
    public final NumberSetting<Double> windowWidth = registerDouble("window.width", "", 1024.0);
    public final NumberSetting<Double> windowHeight = registerDouble("window.height", "", 720.0);
    public final StringSetting lastGame = registerString("last.game", "Last session's game", "");
    public final StringSetting lastTheme = registerString("last.theme", "Last session's theme", MaterialThemes.INDIGO_LIGHT.name());
    public final BooleanSetting lastThemeMode = registerBoolean("last.theme.mode", "Last session's theme mode", false);

    private final Application.Parameters parameters;
    private Boolean debug = null;
    private Boolean resetSettings = null;
    private Boolean invalidateThemesCache = null;

    //================================================================================
    // Constructors
    //================================================================================
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public AppSettings(Application.Parameters parameters) {
        this.parameters = parameters;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected String node() {
        return root();
    }

    //================================================================================
    // Getters
    //================================================================================
    public boolean isDebug() {
        if (debug == null) {
            Map<String, String> named = parameters.getNamed();
            debug = Boolean.parseBoolean(named.getOrDefault("debug", "false"));
        }
        return debug;
    }

    public boolean isResetSettings() {
        if (resetSettings == null) {
            Map<String, String> named = parameters.getNamed();
            resetSettings = Boolean.parseBoolean(named.getOrDefault("reset-settings", "false"));
        }
        return resetSettings;
    }

    public boolean isInvalidateThemesCache() {
        if (invalidateThemesCache == null) {
            Map<String, String> named = parameters.getNamed();
            invalidateThemesCache = Boolean.parseBoolean(named.getOrDefault("invalidate-themes-cache", "false"));
        }
        return invalidateThemesCache;
    }
}
