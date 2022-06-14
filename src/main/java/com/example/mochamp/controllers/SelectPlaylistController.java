package com.example.mochamp.controllers;

import com.example.mochamp.DbRepository;
import com.example.mochamp.models.Playlist;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * popup chọn playlist đã lưu
 */
public class SelectPlaylistController extends PopupController{
    public VBox container;

    private DbRepository db;

    public void initialize() throws Exception {
        db = DbRepository.getInstance();
    }

    public void setup(Pane popup, Button creator, Consumer<Playlist> onClickItemHandler, Consumer<Playlist> onDeletePlaylistHandler) {
        container.getChildren().clear();
        for (Playlist playlist : db.getSavedPlaylists()) {
            HBox hBox = new HBox();

            Button item = new Button(playlist.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                onClickItemHandler.accept(playlist);
                close(e);
            });

            Button deleteBtn = new Button();
            deleteBtn.getStyleClass().add("btn-img-alt");
            deleteBtn.getStyleClass().add("bg-x");

            deleteBtn.setOnAction((e) -> {
                ButtonType okBtn = new ButtonType("Xóa playlist", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(Alert.AlertType.NONE,"Bạn có chắc muốn xóa playlist '" + playlist.getName() + "'?", okBtn, cancelBtn);

                alert.setTitle("Xóa playlist");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.orElse(cancelBtn) == okBtn) {
                    db.deletePlaylist(playlist.getId());
                    onDeletePlaylistHandler.accept(playlist);
                }
            });

            hBox.getChildren().addAll(item, deleteBtn);
            container.getChildren().add(hBox);
        }

        setupPopupPaneAndMoveToButton(popup, creator);
    }
}
