package com.example.mochamp;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
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
    private Media currentMedia;
    private File currentMusicFile;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private boolean playing = false;
    private Timeline progressBarTimeline = null;

    private HBox root;
    private Label songCurrentTime;
    public ImageView thumbnail;
    private Slider progressBar;
    private ImageView startStopImage;

    public MusicPlayerLogic(
            HBox root, Label songTitle, Label songDuration, Label songCurrentTime, ImageView thumbnail,
            Slider progressBar, StackPane startStopButton, ImageView startStopImage,
            Button openSongsButton, VBox songContainer
    ) {
        this.songCurrentTime = songCurrentTime;
        this.progressBar = progressBar;
        this.startStopImage = startStopImage;
        this.root = root;
        this.thumbnail = thumbnail;

        mediaFiles = new ArrayList<>();
        progressBarTimeline = createProgressBarTimeline();
    }

    public void useProgressBarLogic() {
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


    public void openSongChooserDialog() {
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
    }

    public List<File> getMusicFiles() {
        return musicFiles;
    }

    public List<Media> getMediaFiles() {
        return mediaFiles;
    }

    public Media getCurrentMedia() {
        return currentMedia;
    }

    public File getCurrentMusicFile() {
        return currentMusicFile;
    }

    public MediaPlayer playFirstSongInPlaylist(Runnable onMediaPlayerReady) {
        playing = true;
        startStopImage.setImage(new Image("/pause.png"));

        currentMusicFile = musicFiles.get(0);
        currentMedia = new Media(currentMusicFile.toURI().toString());
        mediaPlayer = new MediaPlayer(currentMedia);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();
        progressBarTimeline.playFromStart();

        mediaPlayer.setOnReady(onMediaPlayerReady);

        //play music
        return mediaPlayer;

    }

    private Timeline createProgressBarTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            progressBar.setValue((mediaPlayer.getCurrentTime().toSeconds() / currentMedia.getDuration().toSeconds()) * 100);

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

    private long prevX;
    private void seekMusic(MouseEvent e) {
        double time = (e.getX() / progressBar.getWidth()) * currentMedia.getDuration().toSeconds();

        if (Math.round(e.getX()) == prevX) {
            return;
        }

        prevX = Math.round(e.getX());
        mediaPlayer.seek(Duration.seconds(time));
    }
}
