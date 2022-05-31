package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.models.RecentlyPlayedSong;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class SelectRecentlyPlayedSongController {
    public VBox container;

    public void setOnClickItem(Consumer<RecentlyPlayedSong> handler) throws Exception {
        Database db = Database.getInstance();

        container.getChildren().clear();
        for (RecentlyPlayedSong rps : db.getRecentlyPlayedSongs()) {
            Button item = new Button(rps.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                handler.accept(rps);
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
