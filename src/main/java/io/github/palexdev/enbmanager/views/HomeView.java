package io.github.palexdev.enbmanager.views;

import io.github.palexdev.enbmanager.components.ActionsPane;
import io.github.palexdev.enbmanager.components.FilesTable;
import io.github.palexdev.enbmanager.components.dialogs.ConfigSaveDialog;
import io.github.palexdev.enbmanager.components.dialogs.DialogBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase;
import io.github.palexdev.enbmanager.components.dialogs.DialogServiceBase.DialogConfig;
import io.github.palexdev.enbmanager.components.misc.SelectionModel;
import io.github.palexdev.enbmanager.events.AppReadyEvent;
import io.github.palexdev.enbmanager.model.ENBManagerModel;
import io.github.palexdev.enbmanager.utils.UIUtils;
import io.github.palexdev.enbmanager.views.HomeView.HomePane;
import io.github.palexdev.enbmanager.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.window.popups.MFXTooltip;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.virtualizedfx.controls.VirtualScrollPane;
import io.github.palexdev.virtualizedfx.utils.VSPUtils;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class HomeView extends View<HomePane> {
    //================================================================================
    // Properties
    //================================================================================
    private final DialogServiceBase dialogs;
    private final ENBManagerModel model;

    //================================================================================
    // Constructors
    //================================================================================
    public HomeView(DialogServiceBase dialogs, ENBManagerModel model) {
        this.dialogs = dialogs;
        this.model = model;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected HomePane build() {
        return new HomePane();
    }

    @Override
    @Order(1)
    public void onAppReady(AppReadyEvent event) {
        super.onAppReady(event);
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    class HomePane extends StackPane {
        private final FilesTable table;

        HomePane() {
            // Init table
            table = new FilesTable(model.getFiles());
            SelectionModel<Path> sModel = table.getSelectionModel();
            VirtualScrollPane vsp = table.wrap();
            VBox.setVgrow(vsp, Priority.ALWAYS);
            Runnable speedAction = () -> {
                double ch = table.getCellHeight();
                double cw = table.getColumns().stream()
                    .mapToDouble(c -> c.getRegion().getWidth())
                    .min()
                    .orElse(ch);
                VSPUtils.setHSpeed(vsp, cw / 3, cw, cw / 2);
                VSPUtils.setVSpeed(vsp, ch / 3, ch, ch / 2);
            };
            When.onInvalidated(table.estimatedSizeProperty())
                .then(i -> speedAction.run())
                .executeNow()
                .listen();
            table.autosizeColumns();

            // Init actions
            ActionsPane pane = new ActionsPane(vsp);
            MFXIconButton autosize = createAction("autosize", "Autosize columns", e -> table.autosizeColumns());
            MFXIconButton refresh = createAction("refresh", "Refresh files", e -> refresh());
            MFXIconButton save = createAction("save", "Save selection", e -> save());
            save.disableProperty().bind(sModel.emptyProperty());
            MFXIconButton delete = createAction("delete", "Delete selection", e -> delete());
            delete.disableProperty().bind(sModel.emptyProperty());
            pane.addActions(autosize, refresh, save, delete);

            // Finalize init
            getChildren().add(pane);
            getStyleClass().add("home-view");
        }

        MFXIconButton createAction(String type, String tooltip, EventHandler<ActionEvent> action) {
            MFXIconButton btn = new MFXIconButton().filled();
            btn.getStyleClass().addAll("action-button", type);
            btn.setOnAction(action);
            MFXTooltip tp = UIUtils.installTooltip(btn, tooltip);
            tp.setEventDispatcher(getEventDispatcher());
            // This is needed otherwise events are captured and consumed by the tooltip
            return btn;
        }

        void refresh() {
            model.updateFiles();
        }

        void save() {
            ObservableMap<Integer, Path> selection = table.getSelectionModel().getSelection();
            ConfigSaveDialog.Result result = dialogs.showConfigSaveDialog(MainView.class,
                () -> new DialogConfig<ConfigSaveDialog>()
                    .implicitOwner()
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("Save config")
                    .setContentText("Config name")
            );
            boolean saved = model.save(result.name(), selection.values());
            String message = "Config %s saved".formatted(saved ? "was" : "was not");
            UIUtils.showToast(message);
            if (result.deleteOnSave()) model.delete(selection.values());
        }

        void delete() {
            ObservableMap<Integer, Path> selection = table.getSelectionModel().getSelection();
            boolean confirm = dialogs.showConfirmDialog(MainView.class, DialogBase.DialogType.WARN, "Delete",
                () -> new DialogConfig<>()
                    .implicitOwner()
                    .setModality(Modality.APPLICATION_MODAL)
                    .setHeaderText("File deletion")
                    .setContentText("Delete selected files?")
            );
            if (!confirm) return;
            boolean deleted = model.delete(selection.values());
            String message = "Files %s deleted successfully".formatted(deleted ? "were" : "were not");
            UIUtils.showToast(message);
            table.getSelectionModel().clearSelection(); // TODO: upon delete, all elements become selected, FIX ME
        }
    }
}
