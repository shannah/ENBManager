package io.github.palexdev.enbmanager.events;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class AppReadyEvent extends ApplicationEvent {

    //================================================================================
    // Constructors
    //================================================================================
    public AppReadyEvent(Stage stage) {
        super(stage);
    }

    //================================================================================
    // Methods
    //================================================================================
    public Stage stage() {
        return (Stage) getSource();
    }
}
