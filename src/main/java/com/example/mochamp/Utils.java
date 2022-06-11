package com.example.mochamp;

import com.example.mochamp.controllers.SelectRecentlyPlayedSongController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Utils {
    /**
     * Setup style và một số tính năng cần có cho một popup
     * @param pane root của popup cần setup
     * @param creator nút mà người dùng bấm vào để tạo popup
     * @return stage của popup
     */
    public static Stage setupPopupPane(Pane pane, Button creator) {
        Stage popup = new Stage();
        Scene scene = new Scene(pane);
        popup.setScene(scene);

        Bounds boundsInScreen = creator.localToScreen(creator.getBoundsInLocal());
        popup.setX(boundsInScreen.getCenterX());
        popup.setY(boundsInScreen.getCenterY());
        popup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                popup.close();
            }
        });

        popup.initStyle(StageStyle.UNDECORATED);
        return popup;
    }
}
