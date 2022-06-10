package com.example.mochamp;

import com.example.mochamp.models.Playlist;
import com.example.mochamp.models.RecentlyPlayedSong;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MusicPlayerLogic {
    private List<File> musicFiles;
    private List<Media> mediaFiles;
    private Database db;
    private File currentMusicFile;
    private MediaPlayer mediaPlayer;
    private boolean playing = false;
    private Playlist playList = null;
    private Timeline progressBarTimeline = null;

    public boolean isLoopOne() {
        return loopOne;
    }

    public boolean isLoopAll() {
        return loopAll;
    }

    private boolean loopOne = false;
    private boolean loopAll = false;

    private HBox root;
    private Label songCurrentTime;
    public ImageView thumbnail;
    private Slider progressBar;
    private ImageView startStopImage;

    public MusicPlayerLogic(
            HBox root, Label songTitle, Label songDuration, Label songCurrentTime, ImageView thumbnail,
            Slider progressBar, StackPane startStopButton, ImageView startStopImage,
            Button openSongsButton, VBox songContainer
    ) throws Exception {
        this.db = Database.getInstance();

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
        progressBar.setOnMousePressed(e -> seekMusic(e));
        progressBar.setOnDragDetected(e -> progressBarTimeline.pause());
        progressBar.setOnMouseReleased(e -> { seekMusic(e); prevX = -9999; progressBarTimeline.playFromStart(); });
    }

    /**
     * chạy/dừng nhạc và sửa UI.
     * @return boolean đang chơi hay đang dừng nhạc
     */
    public boolean handleStartStopSong() {
        if (mediaPlayer == null)
            return false;

        playing = !playing;
        Image image = playing ? new Image("/pause.png") : new Image("/play.png");
        startStopImage.setImage(image);

        if (playing)
            mediaPlayer.play();
        else
            mediaPlayer.pause();

        return playing;
    }

    public boolean toggleLoopAll() {
        loopAll = !loopAll;
        return loopAll;
    }

    public boolean toggleLoopOne() {
        loopOne = !loopOne;
        return loopOne;
    }

    /**
     * Lấy tên của playlist
     * @return tên playlist, trả về chuỗi trống nếu chưa mở playlist nào
     */
    public String getPlaylistName() {
        return playList == null ? "" : playList.getName();
    }

    /**
     * Lấy playlist
     * @return playlist, trả về null nếu chưa mở playlist nào
     */
    public Playlist getPlaylist() {
        return playList;
    }

    public void setPlaylist(Playlist pl) {
        playList = pl;
    }

    public MediaPlayer getMediaPlayer() { return mediaPlayer; }

    public void clearAllSongs() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        progressBarTimeline.stop();
        setProgressBarToZero();
        progressBar.setDisable(true);

        musicFiles.clear();
        mediaFiles.clear();
    }

    /**
    Thêm bài hát từ FileChooser và chơi bài vừa thêm đầu tiên
     */
    public void addSongsFromFileChooser(Runnable onMediaPlayerReady) {
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

        int prevPlaylistLength = musicFiles.size();
        musicFiles.addAll(selectedFiles);

        for (File file: selectedFiles) {
            mediaFiles.add(new Media(file.toURI().toString()));
        }

        if (selectedFiles.size() != 0) {
            playSongByIndex(prevPlaylistLength, onMediaPlayerReady);
        }
    }

    public void removeSongFromQueue(int songIndex) {
        if (getCurrentSongIndex() == songIndex) {
            mediaPlayer.stop();
            mediaPlayer = null;

            progressBarTimeline.stop();
            setProgressBarToZero();
            progressBar.setDisable(true);
        }

        musicFiles.remove(songIndex);
        mediaFiles.remove(songIndex);
    }

    public List<File> getMusicFiles() {
        return musicFiles;
    }

    public List<Media> getMediaFiles() {
        return mediaFiles;
    }

    public Media getCurrentSongMedia() {
        return mediaPlayer == null ? null : mediaPlayer.getMedia();
    }

    public int getCurrentSongIndex() {
        return mediaFiles.indexOf(getCurrentSongMedia());
    }

    public File getCurrentMusicFile() { return currentMusicFile; }

    public void stopCurrentSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            progressBarTimeline.pause();
        }
    }

    public void playRecentlyPlayedSong(RecentlyPlayedSong rps, Runnable onMediaPlayerReady) {
        playList = null;
        File file = new File(rps.getPath());

        if (!file.exists()) {
            ButtonType okBtn = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.ERROR,"Không tìm thấy file '"+ rps.getName() +
                    "' ở địa chỉ '" + rps.getPath() + "'. File có thể đã bị xóa hoặc bị dời đi nơi khác", okBtn );
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("Không tìm thấy file nhạc");
            alert.showAndWait();
            db.deleteRecentlyPlayedSongById(rps.getId());
            return;
        }

        musicFiles.clear();
        musicFiles.add(file);

        mediaFiles.clear();
        mediaFiles.add(new Media(file.toURI().toString()));

        playSongByIndex(0, onMediaPlayerReady);

        //move rps to top of list
        db.deleteRecentlyPlayedSongById(rps.getId());
        db.insertRecentlyPlayedSong(rps);
    }

    public void playNextSong(Runnable onMediaPlayerReady) {
        int index = Math.min(mediaFiles.indexOf(getCurrentSongMedia()) + 1, mediaFiles.size() - 1) ;
        playSongByIndex(index, onMediaPlayerReady);
    }

    public void playPrevSong(Runnable onMediaPlayerReady) {
        int index = Math.max(mediaFiles.indexOf(getCurrentSongMedia()) - 1, 0);
        playSongByIndex(index, onMediaPlayerReady);
    }

    public void playSongByIndex(int index, Runnable onMediaPlayerReady) {
        if (mediaFiles.size() == 0) {
            return;
        }

        playing = true;
        startStopImage.setImage(new Image("/pause.png"));

        currentMusicFile = musicFiles.get(index);

        stopCurrentSong();

        mediaPlayer = new MediaPlayer(mediaFiles.get(index));
        progressBar.setDisable(false);
        progressBarTimeline.playFromStart();
        mediaPlayer.play();

        mediaPlayer.setOnReady(() -> {
            db.deleteRecentlyPlayedSongByPath(currentMusicFile.getPath());

            ObservableMap<String, Object> metadata = getCurrentSongMedia().getMetadata();
            db.insertRecentlyPlayedSong(new RecentlyPlayedSong(
                -1,
                (String) metadata.getOrDefault("title", getNameWithoutExtension(currentMusicFile)),
                currentMusicFile.getPath()
            ));

            if (onMediaPlayerReady != null) {
                onMediaPlayerReady.run();
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.stop();
            handleStartStopSong();

            if (loopOne) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
                return;
            }

            if (getCurrentSongIndex() < mediaFiles.size() - 1) {
                playNextSong(onMediaPlayerReady);
                return;
            }

            if (loopAll) {
                playSongByIndex(0, onMediaPlayerReady);
                return;
            }
        });
    }

    private void setProgressBarToZero() {
        progressBar.setValue(0);
        songCurrentTime.setText("00:00");
    }

    private Timeline createProgressBarTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            progressBar.setValue((mediaPlayer.getCurrentTime().toSeconds() / getCurrentSongMedia().getDuration().toSeconds()) * 100);

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
        if (getCurrentSongMedia() == null) {
            return;
        }

        double time = (e.getX() / progressBar.getWidth()) * getCurrentSongMedia().getDuration().toSeconds();

        if (Math.round(e.getX()) == prevX) {
            return;
        }

        prevX = Math.round(e.getX());
        mediaPlayer.seek(Duration.seconds(time));
    }

    public void playSongInPlaylist(Playlist playlist, Runnable onMediaPlayerReady) {
        playList = playlist;
        musicFiles = Arrays.stream(playlist.getSongPaths()).map(File::new)
                                                           .filter(File::exists)
                                                           .collect(Collectors.toList());

        mediaFiles = musicFiles.stream().map(f -> new Media(f.toURI().toString()))
                                        .collect(Collectors.toList());

        playSongByIndex(0, onMediaPlayerReady);
    }
}
