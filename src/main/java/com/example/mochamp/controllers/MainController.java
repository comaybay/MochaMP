package com.example.mochamp.controllers;

import com.example.mochamp.MusicPlayerLogic;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
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

        //default image thumbnail
        thumbnail.setImage(imageCropSquare(new Image("/author.png")));
        thumbnail.setClip(new Circle(62.5, 62.5, 62.5));

        startStopButton.setOnMouseClicked(e -> musicPlayerLogic.handleStartStopSong());

        openSongsButton.setOnAction(e -> onOpenSongs());

        musicPlayerLogic.useProgressBarLogic();
    }

    public void onOpenSongs() {
        musicPlayerLogic.openSongChooserDialog();

        List<File> musicFiles =  musicPlayerLogic.getMusicFiles();
        List<Media> mediaFiles = musicPlayerLogic.getMediaFiles();
        for (int i = 0; i < musicFiles.size(); i++) {
            Pane songComponent = createSongComponent(musicFiles.get(i), mediaFiles.get(i));
            songContainer.getChildren().add(songComponent);
        }

        musicPlayerLogic.playFirstSongInPlaylist(() -> updatePlayerUIToCurrentSong());
    }

    public void updatePlayerUIToCurrentSong() {
        Media currentMedia = musicPlayerLogic.getCurrentMedia();
        ObservableMap<String, Object> metadata = currentMedia.getMetadata();
        Image image = imageCropSquare((Image)metadata.getOrDefault("image", new Image("/author.png")));
        thumbnail.setImage(image);

        String title = (String) metadata.getOrDefault("title", getNameWithoutExtension(musicPlayerLogic.getCurrentMusicFile()));
        songTitle.setText(title);

        int secs = (int) Math.round(currentMedia.getDuration().toSeconds());
        int minPart = secs/60;
        int secPart = secs -  minPart * 60;
        songDuration.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
    }

    public Pane createSongComponent(File songFile, Media mediaFile) {
        //button play
        Image img = new Image("file:/"+System.getProperty("user.dir").replace("\\", "/")+"/src/main/image/2x/outline_play_circle_filled_white_18dp.png");
        BackgroundImage bgImage = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background bg = new Background(bgImage);
        Button btn_play = new Button();
        btn_play.setPrefSize(35,35);
        btn_play.setLayoutX(15);
        btn_play.setLayoutY(15);
        btn_play.setBackground(bg);
        //text song
        Label songName = new Label(getNameWithoutExtension(songFile));
        songName.setPrefSize(169,27);
        songName.setLayoutX(65);
        songName.setLayoutY(10);
        songName.setFont(new Font(18));
        songName.setTextFill(Color.WHITE);
        //text sing
        Label artist = new Label();
        artist.setPrefSize(169,21);
        artist.setLayoutX(65);
        artist.setLayoutY(40);
        artist.setFont(new Font(12));
        artist.setTextFill(Color.WHITE);
        //button dot
        Image img_dot = new Image("file:/"+System.getProperty("user.dir").replace("\\", "/")+"/src/main/image/2x/outline_more_vert_white_18dp.png");
        BackgroundImage bgImagedot = new BackgroundImage(img_dot, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background bgdot = new Background(bgImagedot);
        Button btn_dot = new Button();
        btn_dot.setPrefSize(36,31);
        btn_dot.setLayoutX(243);
        btn_dot.setLayoutY(12);
        btn_dot.setBackground(bgdot);
        //creat pane
        Pane pane = new Pane();
        pane.setPrefSize(200,74);
        pane.getChildren().addAll(btn_play,artist,songName,btn_dot);
        Border  appmntBorder = new Border(new BorderStroke(Color.DARKORCHID,
                Color.DARKORCHID,
                Color.DARKORCHID,
                Color.DARKORCHID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10), BorderWidths.DEFAULT,
                new Insets(1)));
        pane.setOnMouseClicked(
                new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        pane.setBorder(appmntBorder);
                        Image img = new Image("file:/"+System.getProperty("user.dir").replace("\\", "/")+"/src/main/image/2x/outline_pause_circle_filled_white_18dp.png");
                        BackgroundImage bgImage = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                                BackgroundSize.DEFAULT);
                        Background bg = new Background(bgImage);
                        Button btn_play = new Button();
                        btn_play.setPrefSize(35,35);
                        btn_play.setLayoutX(15);
                        btn_play.setLayoutY(15);
                        btn_play.setBackground(bg);
                        pane.getChildren().setAll(btn_play,artist,songName,btn_dot);
                    }
                });


        // wait till metadata is found to set info
        mediaFile.getMetadata().addListener((MapChangeListener<String, Object>) change  -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("artist")) {
                    artist.setText(change.getValueAdded().toString());
                }
                else if (change.getKey().equals("title")) {
                    System.out.println(songName);
                    songName.setText(change.getValueAdded().toString());
                }
            }
        });

        return pane;
    }

    public String getNameWithoutExtension(File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    public static Image imageCropSquare(Image img) {
        double d = Math.min(img.getWidth(),img.getHeight());
        double x = (d-img.getWidth())/2;
        double y = (d-img.getHeight())/2;
        Canvas canvas = new Canvas(d, d);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.drawImage(img, x, y);
        return canvas.snapshot(null, null);
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
