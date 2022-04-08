package com.example.mochamp.controllers;

import com.example.mochamp.MusicPlayerLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController {
    public HBox root;
    private MusicPlayerLogic musicPlayerLogic = null;

    //head player
    public Label songTitle;
    public Label songDuration;
    public Label songCurrentTime;
    public ImageView thumbnail;
    public Slider progressBar;
    public StackPane startStopButton;
    public ImageView startStopImage;
    public ImageView closeButton;
    public ImageView minimizeButton;

    //playlist menu
    public Button openSongsButton;
    public Pane menu,playlist;
    public Label text;
    public VBox songContainer;

    public void initialize() {
        closeButton.setOnMouseClicked(e -> Platform.exit());

        minimizeButton.setOnMouseClicked(e -> {
            Stage obj = (Stage) minimizeButton.getScene().getWindow();
            obj.setIconified(true);
        });

        musicPlayerLogic = new MusicPlayerLogic(
                root, songTitle, songDuration, songCurrentTime, thumbnail, progressBar, startStopButton,
                startStopImage, openSongsButton, songContainer
        );

        //playlist menu
        menu.setVisible(false);
    }


    public void onHelloButtonClick(ActionEvent actionEvent) {
        if(!menu.isVisible()){
            menu.setVisible(true);
        }
        else {
            menu.setVisible(false);
        }
    }
}
