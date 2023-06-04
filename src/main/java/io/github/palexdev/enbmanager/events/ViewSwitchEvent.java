package io.github.palexdev.enbmanager.events;

import io.github.palexdev.enbmanager.views.base.View;
import org.springframework.context.ApplicationEvent;

public class ViewSwitchEvent extends ApplicationEvent {

    //================================================================================
    // Constructors
    //================================================================================
    public ViewSwitchEvent(View<?> view) {
        super(view);
    }

    //================================================================================
    // Methods
    //================================================================================
    public View<?> getView() {
        return (View<?>) super.getSource();
    }
}
