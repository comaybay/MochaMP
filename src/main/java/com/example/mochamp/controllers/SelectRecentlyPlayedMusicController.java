package com.example.mochamp.controllers;

import com.example.mochamp.DbRepository;
import com.example.mochamp.models.RecentlyPlayedMusic;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * popup chọn nhạc đã chơi gần đây
 */
public class SelectRecentlyPlayedMusicController extends PopupController {
    public VBox container;

    private DbRepository db;

    public void initialize() throws Exception {
        db = DbRepository.getInstance();
    }

    public void setup(Pane popup, Button creator, Consumer<RecentlyPlayedMusic> onClickItemHandler) {
        container.getChildren().clear();
        for (RecentlyPlayedMusic rpm : db.getRecentlyPlayedMusic()) {
            Button item = new Button(rpm.getName());
            item.getStyleClass().add("txt-btn");
            item.setPrefWidth(200);
            item.setOnAction(e -> {
                onClickItemHandler.accept(rpm);
                close(e);
            });
            container.getChildren().add(item);
        }

        setupPopupPaneAndMoveToButton(popup, creator);
    }
}
