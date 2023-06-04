package io.github.palexdev.enbmanager.views.base;

import io.github.palexdev.enbmanager.events.AppReadyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.springframework.context.event.EventListener;

public abstract class View<P extends Pane> {
    //================================================================================
    // Properties
    //================================================================================
    protected P root;

    //================================================================================
    // Constructors
    //================================================================================
    protected View() {}

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract P build();

    //================================================================================
    // Methods
    //================================================================================
    public Region toRegion() {
        return root;
    }

    //================================================================================
    // Events
    //================================================================================
    @EventListener
    public void onAppReady(AppReadyEvent event) {
        root = build();
    }
}
