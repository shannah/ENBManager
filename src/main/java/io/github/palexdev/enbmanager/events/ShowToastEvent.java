package io.github.palexdev.enbmanager.events;

import org.springframework.context.ApplicationEvent;

public class ShowToastEvent extends ApplicationEvent {

    //================================================================================
    // Constructors
    //================================================================================
    public ShowToastEvent(String message) {
        super(message);
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getMessage() {
        return (String) getSource();
    }
}
