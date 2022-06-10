package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.models.RecentlyPlayedMusic;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * popup chọn nhạc đã chơi gần đây
 */
public class SelectRecentlyPlayedMusicController {
    public VBox container;

    public void setup(Consumer<RecentlyPlayedMusic> onClickItemHandler) throws Exception {
        Database db = Database.getInstance();

        container.getChildren().clear();
        for (RecentlyPlayedMusic rps : db.getRecentlyPlayedMusic()) {
            Button item = new Button(rps.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                onClickItemHandler.accept(rps);
                closeDialog(e);
            });
            container.getChildren().add(item);
        }
    }

    private void closeDialog(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
