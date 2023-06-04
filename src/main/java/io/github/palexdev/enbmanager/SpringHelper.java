package io.github.palexdev.enbmanager;

import io.github.palexdev.enbmanager.events.AppCloseEvent;
import io.github.palexdev.enbmanager.events.ViewSwitchEvent;
import io.github.palexdev.enbmanager.views.base.View;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringHelper {
    //================================================================================
    // Properties
    //================================================================================
    private static ConfigurableApplicationContext context;

    //================================================================================
    // Constructors
    //================================================================================
    private SpringHelper() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static void setView(Class<? extends View<?>> view) {
        context.publishEvent(new ViewSwitchEvent(context.getBean(view)));
    }

    public static void exit() {
        context.publishEvent(new AppCloseEvent(false));
    }

    public static void forceShutdown(int status) {
        notify(new AppCloseEvent(true, status));
/*        context.close();
        Platform.exit();
        System.exit(status);*/
    }

    public static <T> T getBean(Class<? extends T> k) {
        return context.getBean(k);
    }

    public static void notify(ApplicationEvent event) {
        context.publishEvent(event);
    }

    static void setContext(ConfigurableApplicationContext context) {
        SpringHelper.context = context;
    }
}
