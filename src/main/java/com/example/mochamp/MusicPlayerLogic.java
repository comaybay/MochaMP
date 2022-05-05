package com.example.mochamp;

import com.example.mochamp.controllers.MainController;
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
import javafx.scene.control.ProgressBar;
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
    private final List<File> musicFiles;
    private final List<Media> mediaFiles;
    private Media currentMedia;
    private File currentMusicFile;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private boolean playing = false;
    private Timeline progressBarTimeline = null;

    private final HBox root;
    private final Label songCurrentTime;
    public ImageView thumbnail;
    private final ProgressBar progressBar;
    private final ImageView startStopImage;

    public MusicPlayerLogic(
            HBox root, Label songTitle, Label songDuration, Label songCurrentTime, ImageView thumbnail,
             ProgressBar progressBar, StackPane startStopButton, ImageView startStopImage,
            Button openSongsButton, VBox songContainer
    ) {
        this.songCurrentTime = songCurrentTime;
        this.progressBar = progressBar;
        this.startStopImage = startStopImage;
        this.root = root;
        this.thumbnail = thumbnail;

        musicFiles = new ArrayList<>();
        mediaFiles = new ArrayList<>();
        progressBarTimeline = createProgressBarTimeline();
    }

    public void useProgressBarLogic() {
        progressBar.setOnMousePressed(this::seekMusic);
        progressBar.setOnDragDetected(e -> progressBarTimeline.pause());
        progressBar.setOnMouseReleased(e -> { seekMusic(e); prevX = 999; progressBarTimeline.playFromStart(); });
    }

    public void handleStartStopSong(int value) {
        if (mediaPlayer == null)
            return;

        playing = !playing;
        Image image = playing ? new Image("/pause.png") : new Image("/play.png");
        Image img_pause = new Image("image/2x/outline_pause_circle_filled_white_18dp.png");
        BackgroundImage bgImagedot = new BackgroundImage(img_pause, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Image img_play = new Image("image/2x/outline_play_circle_filled_white_18dp.png");
        BackgroundImage bgImagePlay = new BackgroundImage(img_play, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);

        Background bg = playing ? new Background(bgImagedot) : new Background(bgImagePlay);
        MainController.setArrayButton().get(value).setBackground(bg);

        startStopImage.setImage(image);

        if (playing)
            mediaPlayer.play();
        else
            mediaPlayer.pause();
    }

    /**
    Mở FileChooser để chọn bài hát
    @return danh sách bài hát người dùng đã chọn
     */
    public List<File> openSongChooserDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file nhạc");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Audio files (*.mp3, *.flac, *.wav, *.m4a, *.mp4, *.wma, *.aac)",
                        "*.mp3", "*.flac", "*.wav", "*.m4a", "*.mp4", "*.wma", "*.aac"
                )
        );

        Stage stage = (Stage) root.getScene().getWindow();

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        selectedFiles = selectedFiles == null ? new ArrayList<>() : selectedFiles;

        musicFiles.addAll(selectedFiles);

        for (File file: selectedFiles) {
            mediaFiles.add(new Media(file.toURI().toString()));
        }

        return selectedFiles;
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

    public int getCurrentSongIndex() {return mediaFiles.indexOf(currentMedia);}

    public File getCurrentMusicFile() { return currentMusicFile; }

    public void stopCurrentSong() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void playNextSong(Runnable onMediaPlayerReady) {
        int index = Math.min(mediaFiles.indexOf(currentMedia) + 1, mediaFiles.size() - 1) ;
        playSongByIndex(index, onMediaPlayerReady);
    }

    public void playPrevSong(Runnable onMediaPlayerReady) {
        int index = Math.max(mediaFiles.indexOf(currentMedia) - 1, 0);
        playSongByIndex(index, onMediaPlayerReady);
    }

    public void playSongByIndex(int index, Runnable onMediaPlayerReady) {
        if (mediaFiles.size() == 0) {
            return;
        }

        playing = true;
        startStopImage.setImage(new Image("/pause.png"));

        currentMusicFile = musicFiles.get(index);
        currentMedia = mediaFiles.get(index);

        stopCurrentSong();

        mediaPlayer = new MediaPlayer(currentMedia);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();
        progressBarTimeline.playFromStart();

        mediaPlayer.setOnReady(onMediaPlayerReady);
    }

    private Timeline createProgressBarTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            progressBar.setProgress((mediaPlayer.getCurrentTime().toSeconds() / currentMedia.getDuration().toSeconds()) );


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
