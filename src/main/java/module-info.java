module ENBManager {
    //***** UI related *****//
    requires javafx.controls;

    requires MaterialFX;
    requires mfx.components;
    requires com.sandec.mdfx;
    requires fr.brouillard.oss.cssfx;

    requires org.scenicview.scenicview;

    //***** Spring *****//
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;

    //***** Misc *****//
    requires com.github.oshi;
    requires directory.watcher;
    requires java.prefs;

    //***** Exports *****//
    // Base
    exports io.github.palexdev.enbmanager;

    // Components
    exports io.github.palexdev.enbmanager.components;
    exports io.github.palexdev.enbmanager.components.dialogs;
    exports io.github.palexdev.enbmanager.components.misc;
    exports io.github.palexdev.enbmanager.components.settings;

    // Events
    exports io.github.palexdev.enbmanager.events;

    // Model
    exports io.github.palexdev.enbmanager.model;
    exports io.github.palexdev.enbmanager.model.games;
    exports io.github.palexdev.enbmanager.model.repo;

    // Settings
    exports io.github.palexdev.enbmanager.settings;
    exports io.github.palexdev.enbmanager.settings.base;

    // Theming
    exports io.github.palexdev.enbmanager.theming;

    // Utils
    exports io.github.palexdev.enbmanager.utils;

    // Views
    exports io.github.palexdev.enbmanager.views;
    exports io.github.palexdev.enbmanager.views.base;

    //***** Opens *****//
    opens io.github.palexdev.enbmanager to spring.core;
    opens io.github.palexdev.enbmanager.views to spring.core;
}