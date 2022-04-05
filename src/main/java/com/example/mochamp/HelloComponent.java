package com.example.mochamp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.io.IOException;

public class HelloComponent extends Region {
    public HelloComponent() throws IOException {
        super();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Node node = loader.load();

        this.getChildren().add(node);
    }

    public static class HeadPlayer extends Region {
    }
}
