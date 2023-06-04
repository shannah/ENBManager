package io.github.palexdev.enbmanager.components.settings;

import io.github.palexdev.enbmanager.SpringHelper;
import io.github.palexdev.enbmanager.components.FloatingField;
import io.github.palexdev.enbmanager.model.ENBManagerModel;
import io.github.palexdev.enbmanager.model.games.Game;
import io.github.palexdev.enbmanager.settings.base.Setting;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.utils.converters.FunctionalStringConverter;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class GamePathSettingComponent extends FieldSettingComponent<String> {

    //================================================================================
    // Constructors
    //================================================================================
    public GamePathSettingComponent(Setting<String> setting, Supplier<FloatingField> fieldFactory) {
        super(setting, fieldFactory);
        setConverter(FunctionalStringConverter.converter(
            s -> s,
            s -> s
        ));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public List<String> defaultStyleClasses() {
        return List.of("setting-component", "path");
    }

    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new Skin(this);
    }

    @Override
    public Supplier<SettingComponentBehavior<String, Setting<String>>> defaultBehaviorProvider() {
        return () -> new SettingComponentBehavior<>(this) {
            @Override
            public void reset(MouseEvent me) {
                // Null events can come from the component's delegate method
                if (me == null || me.getButton() == MouseButton.PRIMARY) {
                    ENBManagerModel model = SpringHelper.getBean(ENBManagerModel.class);
                    SettingComponent<String, Setting<String>> component = getNode();
                    component.getSetting().set(component.getInitialValue());
                    model.setPath(Path.of(component.getSetting().get()));
                }
            }
        };
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    class Skin extends FieldSettingComponent<String>.Skin {
        private final ENBManagerModel model;
        private final HBox container;

        protected Skin(FieldSettingComponent<String> component) {
            super(component);
            model = SpringHelper.getBean(ENBManagerModel.class);
            MFXIconButton chooseBtn = new MFXIconButton().filled();
            chooseBtn.setOnAction(e -> pickGameFolder());
            chooseBtn.getStyleClass().add("choose-icon");
            field.field().textProperty().bind(model.pathProperty().asString());
            field.setEditable(false);
            field.setMaxWidth(Double.MAX_VALUE);
            container = new HBox(field, chooseBtn, resetIcon);
            container.getStyleClass().add("container");
            HBox.setHgrow(field, Priority.ALWAYS);
            getChildren().setAll(container);
        }

        protected void pickGameFolder() {
            Game game = model.getGame();
            Path path = model.getPath();
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(game.toFilter());
            if (path != null) fc.setInitialDirectory(path.toFile());

            File file = fc.showOpenDialog(getScene().getWindow());
            if (file == null) return;
            path = file.toPath().getParent();
            if (path == null) return;

            model.setPath(path);
            getSetting().set(path.toString());
        }

        @Override
        protected void setText(String val) {
            // This is a bound value, no need to set
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            container.resizeRelocate(x, y, w, h);
        }
    }
}
