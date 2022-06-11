package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.models.Playlist;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Consumer;

public class SelectPlaylistController {
    public VBox container;

    public void setOnClickItem(Consumer<Playlist> handler) throws Exception {
        Database db = Database.getInstance();

        container.getChildren().clear();
        for (Playlist playlist : db.getSavedPlaylists()) {
            HBox hBox = new HBox();

            Button item = new Button(playlist.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                handler.accept(playlist);
                closeDialog(e);
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
                }
            });

            hBox.getChildren().addAll(item, deleteBtn);
            container.getChildren().add(hBox);
        };
    }

    private void closeDialog(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
