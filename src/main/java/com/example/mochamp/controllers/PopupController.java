package com.example.mochamp.controllers;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class PopupController {
    /**
     * Setup style và một số tính năng cần có cho một popup
     * @param pane root của popup cần setup
     * @param creator nút mà người dùng bấm vào để tạo popup
     */
    protected void setupPopupPaneAndMoveToButton(Pane pane, Button creator) {
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
        popup.show();
    }

    /**
     * Đóng popup
     */
    protected void close(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
