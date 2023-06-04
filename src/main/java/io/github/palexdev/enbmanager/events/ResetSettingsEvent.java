package io.github.palexdev.enbmanager.events;

import io.github.palexdev.enbmanager.settings.base.Settings;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("unchecked")
public class ResetSettingsEvent extends ApplicationEvent {

    //================================================================================
    // Constructors
    //================================================================================
    public ResetSettingsEvent(Class<? extends Settings> sClass) {
        super(sClass);
    }

    //================================================================================
    // Methods
    //================================================================================
    public Class<? extends Settings> getSettingsClass() {
        return (Class<? extends Settings>) super.getSource();
    }
}
