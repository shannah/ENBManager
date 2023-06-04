package io.github.palexdev.enbmanager.events;

import io.github.palexdev.enbmanager.model.repo.Config;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

@SuppressWarnings("unchecked")
public class ConfigsChangedEvent extends ApplicationEvent {

    //================================================================================
    // Constructors
    //================================================================================
    public ConfigsChangedEvent(Collection<Config> configs) {
        super(configs);
    }

    //================================================================================
    // Methods
    //================================================================================
    public Collection<Config> getConfigs() {
        return (Collection<Config>) getSource();
    }
}
