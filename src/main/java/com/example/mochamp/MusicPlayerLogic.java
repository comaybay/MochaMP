package com.example.mochamp;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MusicPlayerLogic {
    private List<File> musicFiles;
    private List<Media> mediaFiles;
    private Media media;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private boolean playing = false;
    private Timeline progressBarTimeline = null;

    private HBox root;
    private Label songTitle;
    private Label songDuration;
    private Label songCurrentTime;
    public ImageView thumbnail;
    private Slider progressBar;
    private StackPane startStopButton;
    private ImageView startStopImage;
    private Button openSongsButton;
    private VBox songContainer;

    public MusicPlayerLogic(
            HBox root, Label songTitle, Label songDuration, Label songCurrentTime, ImageView thumbnail,
            Slider progressBar, StackPane startStopButton, ImageView startStopImage,
            Button openSongsButton, VBox songContainer
    ) {
        this.songTitle = songTitle;
        this.songDuration = songDuration;
        this.songCurrentTime = songCurrentTime;
        this.progressBar = progressBar;
        this.startStopButton = startStopButton;
        this.startStopImage = startStopImage;
        this.root = root;
        this.openSongsButton = openSongsButton;
        this.songContainer = songContainer;
        this.thumbnail = thumbnail;

        mediaFiles = new ArrayList<>();
        progressBarTimeline = createProgressBarTimeline();

        //default image thumbnail
        thumbnail.setImage(imageCropSquare(new Image("/author.png")));
        thumbnail.setClip(new Circle(62.5, 62.5, 62.5));

//        startStopButton.setOnMouseClicked(e -> handleStartStopSong());
        openSongsButton.setOnAction(e -> onOpenSongs(e));
        progressBar.setOnMousePressed(e -> seekMusic(e));
        progressBar.setOnDragDetected(e -> progressBarTimeline.pause());
        progressBar.setOnMouseReleased(e -> { seekMusic(e); prevX = -9999; progressBarTimeline.playFromStart(); });
    }

    public void handleStartStopSong() {
        if (mediaPlayer == null)
            return;

        playing = !playing;
        Image image = playing ? new Image("/pause.png") : new Image("/play.png");
        startStopImage.setImage(image);

        if (playing)
            mediaPlayer.play();
        else
            mediaPlayer.pause();
    }

    public void onOpenSongs(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file nhạc");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Audio files (*.mp3, *.flac, *.wav, *.m4a, *.mp4, *.wma, *.aac)",
                        "*.mp3", "*.flac", "*.wav", "*.m4a", "*.mp4", "*.wma", "*.aac"
                )
        );

        Stage stage = (Stage) root.getScene().getWindow();

        musicFiles = fileChooser.showOpenMultipleDialog(stage);
        for (File file: musicFiles) {
            mediaFiles.add(new Media(file.toURI().toString()));
        }

        startStopImage.setImage(new Image("/pause.png"));

        media = new Media(musicFiles.get(0).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();

        for (int i = 0; i < musicFiles.size(); i++) {
            Pane songComponent = createSongComponent(musicFiles.get(i), mediaFiles.get(i));
            songContainer.getChildren().add(songComponent);
        }

        //play music
        mediaPlayer.setOnReady(() -> {

            ObservableMap<String, Object> metadata = media.getMetadata();
            Image image = imageCropSquare((Image)metadata.getOrDefault("image", new Image("/author.png")));
            thumbnail.setImage(image);

            String title = (String) metadata.getOrDefault("title", getNameWithoutExtension(musicFiles.get(0)));
            songTitle.setText(title);

            int secs = (int) Math.round(media.getDuration().toSeconds());
            int minPart = secs/60;
            int secPart = secs -  minPart * 60;
            songDuration.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
        });

        progressBarTimeline.playFromStart();
    }

    private Timeline createProgressBarTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            progressBar.setValue((mediaPlayer.getCurrentTime().toSeconds() / media.getDuration().toSeconds()) * 100);

            int secs = (int) Math.round(mediaPlayer.getCurrentTime().toSeconds());
            int minPart = secs/60;
            int secPart = secs -  minPart * 60;
            songCurrentTime.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        return timeline;
    }

    private String getNameWithoutExtension(File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    private Pane createSongComponent(File songFile, Media mediaFile) {
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

    private void handleSelectSong(int songIndex) {
        //TODO: do this shit.
    }

    private static Image imageCropSquare(Image img) {
        double d = Math.min(img.getWidth(),img.getHeight());
        double x = (d-img.getWidth())/2;
        double y = (d-img.getHeight())/2;
        Canvas canvas = new Canvas(d, d);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.drawImage(img, x, y);
        return canvas.snapshot(null, null);
    }

    private long prevX;
    private void seekMusic(MouseEvent e) {
        double time = (e.getX() / progressBar.getWidth()) * media.getDuration().toSeconds();

        if (Math.round(e.getX()) == prevX) {
            return;
        }

        prevX = Math.round(e.getX());
        mediaPlayer.seek(Duration.seconds(time));
    }
}
