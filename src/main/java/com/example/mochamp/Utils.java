package com.example.mochamp;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

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

    /**
     * Lấy tên file nhưng không chứa extension
     * @param file file cần lấy tên
     * @return tên file
     */
    public static String getNameWithoutExtension(File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    /**
     * Crop ảnh để ảnh thành hình vuông
     * @param img ảnh
     * @return ảnh đã được crop
     */
    public static Image imageCropSquare(Image img) {
        double d = Math.min(img.getWidth(),img.getHeight());
        double x = (d-img.getWidth())/2;
        double y = (d-img.getHeight())/2;
        Canvas canvas = new Canvas(d, d);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.drawImage(img, x, y);
        return canvas.snapshot(null, null);
    }
}
