package io.github.palexdev.enbmanager.views;

import io.github.palexdev.enbmanager.components.FloatingField;
import io.github.palexdev.enbmanager.components.settings.GamePathSettingComponent;
import io.github.palexdev.enbmanager.model.ENBManagerModel;
import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.theming.ThemeManager;
import io.github.palexdev.enbmanager.utils.UIUtils;
import io.github.palexdev.enbmanager.views.SettingsView.SettingsPane;
import io.github.palexdev.enbmanager.views.base.View;
import io.github.palexdev.mfxcomponents.controls.MaterialSurface;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import io.github.palexdev.mfxcore.observables.When;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.palexdev.mfxcomponents.theming.MaterialThemes.INDIGO_LIGHT;
import static io.github.palexdev.mfxcomponents.theming.MaterialThemes.PURPLE_LIGHT;

@Component
public class SettingsView extends View<SettingsPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final ENBManagerModel model;
    private final ThemeManager themes;

    //================================================================================
    // Constructors
    //================================================================================
    public SettingsView(ENBManagerModel model, ThemeManager themes) {
        this.model = model;
        this.themes = themes;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected SettingsPane build() {
        return new SettingsPane();
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    class SettingsPane extends StackPane {
        private final VBox container; // TODO wrap in a scroll pane
        private List<Node> gameSettingsNodes;

        SettingsPane() {
            container = new VBox();
            addThemeSettings();
            addSeparator(30);
            When.onInvalidated(model.gameProperty())
                .then(this::updateGameSettingsView)
                .executeNow(() -> model.getGame() != null)
                .listen();
            getStyleClass().add("settings-view");
            getChildren().add(container);
        }

        protected void addThemeSettings() {
            Label title = new Label("Appearance");
            title.getStyleClass().add("title");
            Label label = new Label("Available themes: ");
            FlowPane fp = new FlowPane();
            fp.getChildren().addAll(
                buildThemeRect("Indigo", INDIGO_LIGHT, "#4355b9"),
                buildThemeRect("Purple", PURPLE_LIGHT, "#6750A4")
            );
            HBox box = new HBox(label, fp);
            box.getStyleClass().add("box");
            container.getChildren().addAll(title, box);
        }

        protected void updateGameSettingsView(Game game) {
            if (gameSettingsNodes != null) container.getChildren().removeAll(gameSettingsNodes);
            Label title = new Label("%s settings".formatted(game.name()));
            title.getStyleClass().add("title");
            GamePathSettingComponent component = new GamePathSettingComponent(game.getSettings().path, FloatingField::new);
            gameSettingsNodes = List.of(title, component);
            container.getChildren().addAll(gameSettingsNodes);
        }

        protected Node buildThemeRect(String tooltip, Theme theme, String color) {
            StackPane p = new StackPane();
            MaterialSurface surface = new MaterialSurface(p);
            surface.setManaged(true);
            surface.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            p.getChildren().add(surface);
            p.setBackground(Background.fill(Color.web(color)));
            p.getStyleClass().add("theme-rect");
            p.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> themes.setTheme(theme));
            UIUtils.installTooltip(p, tooltip);
            return p;
        }

        protected void addSeparator(double size) {
            Region r = new Region();
            r.setMinHeight(USE_PREF_SIZE);
            r.setPrefHeight(size);
            r.setMaxHeight(USE_PREF_SIZE);
            container.getChildren().add(r);
        }
    }
}
