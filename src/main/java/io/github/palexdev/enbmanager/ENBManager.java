package io.github.palexdev.enbmanager;

import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.components.dialogs.GamesDialog;
import io.github.palexdev.enbmanager.events.AppCloseEvent;
import io.github.palexdev.enbmanager.events.AppReadyEvent;
import io.github.palexdev.enbmanager.events.ResetSettingsEvent;
import io.github.palexdev.enbmanager.model.ENBManagerModel;
import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.model.games.GamesManager;
import io.github.palexdev.enbmanager.settings.AppSettings;
import io.github.palexdev.enbmanager.settings.base.Settings;
import io.github.palexdev.enbmanager.theming.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.nio.file.Path;
import java.util.Optional;

public class ENBManager extends Application {
    //================================================================================
    // Properties
    //================================================================================
    public static final String APP_NAME = "ENBManager";
    private Stage stage;
    private AppSettings settings;
    private ENBManagerModel model;
    private GamesManager manager;
    private ThemeManager themeManager;
    private DialogServiceBase dialogs;
    private ConfigurableApplicationContext context;
    private static final StringProperty title = new SimpleStringProperty(APP_NAME);

    public static void main(String[] args) {
        launch(args);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // Init Context
        context = new SpringApplicationBuilder(SpringEntry.class)
            .initializers(c -> c.getBeanFactory().registerSingleton("stage", stage))
            .initializers(c -> c.getBeanFactory().registerSingleton("parameters", getParameters()))
            .initializers(c -> c.getBeanFactory().registerSingleton("hostServices", getHostServices()))
            .initializers(SpringHelper::setContext)
            .run();

        ApplicationListener<AppCloseEvent> closeHandler = e -> stop();
        context.addApplicationListener(closeHandler);

        // Get dependencies needed for the initialization
        dialogs = SpringHelper.getBean(DialogServiceBase.class);
        model = SpringHelper.getBean(ENBManagerModel.class);
        manager = SpringHelper.getBean(GamesManager.class);
        themeManager = SpringHelper.getBean(ThemeManager.class);
        settings = context.getBean(AppSettings.class);

        // Reset settings if requested by the environment
        if (settings.isResetSettings()) context.publishEvent(new ResetSettingsEvent(Settings.class));
        if (settings.isDebug())
            themeManager.debugWatch();
        else
            themeManager.watch(false);

        // Check if last session was saved. If it was, proceed with init, otherwise show Games choice dialog
        Optional<Game> opt = manager.detectLastGame();
        if (opt.isEmpty()) {
            GamesDialog.Choice choice = dialogs.showGamesDialog(null, () -> new DialogConfig<GamesDialog>()
                .setShowMinimize(false)
                .setPreserveHeader(true)
                .setCenterInOwnerNode(false)
                .setOnConfigure(d -> d.setGames(manager.getGames()))
            );
            if (!choice.isValid()) {
                stop();
                return;
            }
            model.setGame(choice.game());
            model.setPath(choice.path());
            if (choice.remember()) {
                settings.lastGame.set(choice.game().name());
            } else {
                settings.lastGame.reset();
            }
        } else {
            model.setGame(opt.get());
            model.setPath(Path.of(opt.get().getSettings().path.get()));
        }

        // Init App
        context.publishEvent(new AppReadyEvent(stage));
    }

    @Override
    public void stop() {
        double w = (!Double.isNaN(stage.getWidth()) ? stage.getWidth() : settings.windowWidth.defValue());
        double h = (!Double.isNaN(stage.getHeight()) ? stage.getHeight() : settings.windowHeight.defValue());
        settings.windowWidth.set(w);
        settings.windowHeight.set(h);
        if (model.getGame() != null) settings.lastGame.set(model.getGame().name());
        context.stop();
        Platform.exit();
    }

    @EventListener
    public void onForceShutdown(AppCloseEvent event) {
        if (event.isForceShutdown()) {
            stop();
            System.exit(event.getStatus());
        }
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public static String getTitle() {
        return ENBManager.title.get();
    }

    /**
     * Specifies the app's title.
     */
    public static StringProperty titleProperty() {
        return ENBManager.title;
    }

    public static void setTitle(String title) {
        ENBManager.title.set(title);
    }
}
