package io.github.palexdev.enbmanager.views;

import io.github.palexdev.enbmanager.components.ActionsPane;
import io.github.palexdev.enbmanager.components.ConfigsList;
import io.github.palexdev.enbmanager.components.dialogs.DialogBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.components.misc.SelectionModel;
import io.github.palexdev.enbmanager.model.ENBManagerModel;
import io.github.palexdev.enbmanager.model.repo.Config;
import io.github.palexdev.enbmanager.utils.UIUtils;
import io.github.palexdev.enbmanager.views.RepoView.RepoPane;
import io.github.palexdev.enbmanager.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.virtualizedfx.controls.VirtualScrollPane;
import io.github.palexdev.virtualizedfx.utils.VSPUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.springframework.stereotype.Component;

@Component
public class RepoView extends View<RepoPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final DialogServiceBase dialogs;
    private final ENBManagerModel model;

    //================================================================================
    // Constructors
    //================================================================================
    public RepoView(DialogServiceBase dialogs, ENBManagerModel model) {
        this.dialogs = dialogs;
        this.model = model;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected RepoPane build() {
        return new RepoPane();
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    class RepoPane extends StackPane {
        private final ConfigsList list;

        RepoPane() {
            // Init list
            list = new ConfigsList(model.getConfigs());
            SelectionModel<Config> sModel = list.getSelectionModel();
            VirtualScrollPane vsp = list.wrap();
            vsp.getStylesheets().clear();
            VBox.setVgrow(vsp, Priority.ALWAYS);
            Runnable speedAction = () -> {
                double ch = list.getCellSize();
                VSPUtils.setVSpeed(vsp, ch / 3, ch, ch / 2);
            };
            When.onInvalidated(list.cellSizeProperty())
                .then(i -> speedAction.run())
                .executeNow()
                .listen();

            // Init actions
            ActionsPane pane = new ActionsPane(vsp);
            MFXIconButton refresh = createAction("refresh", "Refresh configs", e -> refresh());
            MFXIconButton load = createAction("load", "Load selected", e -> load());
            load.disableProperty().bind(sModel.emptyProperty());
            MFXIconButton remove = createAction("delete", "Delete selected", e -> delete());
            remove.disableProperty().bind(sModel.emptyProperty());
            pane.addActions(refresh, load, remove);

            // Finalize init
            getChildren().add(pane);
            getStyleClass().add("repo-view");
        }

        MFXIconButton createAction(String type, String tooltip, EventHandler<ActionEvent> action) {
            MFXIconButton btn = new MFXIconButton().filled();
            btn.getStyleClass().add(type);
            btn.setOnAction(action);
            UIUtils.installTooltip(btn, tooltip);
            return btn;
        }

        void refresh() {
            model.refreshConfigs();
        }

        void load() {
            Config config = list.getSelectionModel().getSelectedItem();
            if (config == null) return;
            DialogBase.DialogType type = DialogBase.DialogType.INFO;
            StringBuilder sb = new StringBuilder("Are you sure you want to load configuration: " + config.name());
            if (!model.getFiles().isEmpty()) {
                type = DialogBase.DialogType.WARN;
                sb.append("\nFiles of an existing configuration have been detected!");
            }
            boolean confirm = dialogs.showConfirmDialog(MainView.class, type, "Load",
                () -> new DialogConfig<>()
                    .implicitOwner()
                    .setShowMinimize(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Load configuration!")
                    .setContentText(sb.toString())
            );
            if (!confirm) return;
            boolean loaded = model.load(config);
            String message = "Config %s loaded correctly".formatted(loaded ? "was" : "was not");
            UIUtils.showToast(message);
        }

        void delete() {
            Config config = list.getSelectionModel().getSelectedItem();
            boolean confirm = dialogs.showConfirmDialog(MainView.class, DialogBase.DialogType.WARN, "Delete",
                () -> new DialogConfig<>()
                    .implicitOwner()
                    .setShowMinimize(false)
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Config deletion!")
                    .setContentText("Delete configuration %s and all its files?".formatted(config.name()))
            );
            if (!confirm) return;
            boolean deleted = model.delete(config);
            String message = "Config %s deleted correctly".formatted(deleted ? "was" : "was not");
            UIUtils.showToast(message);
        }
    }
}
