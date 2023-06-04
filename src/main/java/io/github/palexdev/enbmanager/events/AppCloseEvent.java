package io.github.palexdev.enbmanager.events;

import org.springframework.context.ApplicationEvent;

public class AppCloseEvent extends ApplicationEvent {
    //================================================================================
    // Properties
    //================================================================================
    private final int status;

    //================================================================================
    // Constructors
    //================================================================================
    public AppCloseEvent(boolean force) {
        this(force, 0);
    }

    public AppCloseEvent(boolean force, int status) {
        super(force);
        this.status = status;
    }

    //================================================================================
    // Methods
    //================================================================================
    public boolean isForceShutdown() {
        return (boolean) getSource();
    }

    public int getStatus() {
        return status;
    }
}