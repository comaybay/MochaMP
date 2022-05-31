package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.models.Playlist;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class SelectPlaylistController {
    public VBox container;

    public void setOnClickItem(Consumer<Playlist> handler) throws Exception {
        Database db = Database.getInstance();

        container.getChildren().clear();
        for (Playlist playlist : db.getSavedPlaylists()) {
            Button item = new Button(playlist.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                handler.accept(playlist);
                closeDialog(e);
            });
            container.getChildren().add(item);
        };
    }

    private void closeDialog(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
