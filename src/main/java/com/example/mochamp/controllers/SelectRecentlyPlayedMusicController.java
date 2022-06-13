package com.example.mochamp.controllers;

import com.example.mochamp.DbRepository;
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
        DbRepository db = DbRepository.getInstance();

        container.getChildren().clear();
        for (RecentlyPlayedMusic rpm : db.getRecentlyPlayedMusic()) {
            Button item = new Button(rpm.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                onClickItemHandler.accept(rpm);
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
