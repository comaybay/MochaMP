package com.example.mochamp;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.File;

public class Utils {
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
