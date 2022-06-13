package com.example.mochamp;

import com.example.mochamp.models.Playlist;
import com.example.mochamp.models.RecentlyPlayedMusic;
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
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chứa các tính năng liên quan đến việc tương tác với nhạc, playlist và media player
 */
public class MusicPlayerLogic {
    private List<File> musicFiles;
    private List<Media> musicMediaFiles;
    private final List<File> normalOrderMusicFiles;

    private final DbRepository db;
    private File currentMusicFile;
    private MediaPlayer mediaPlayer;
    private Playlist playList;
    private final Timeline progressBarTimeline;

    public boolean isLoopOne() {
        return loopOne;
    }

    public boolean isLoopAll() {
        return loopAll;
    }

    private boolean playing;
    private boolean loopOne;
    private boolean loopAll;
    private boolean isSortMusicByName;
    private boolean autoPlay;

    private final HBox root;
    private final Label musicCurrentTime;
    public ImageView thumbnail;
    private final Slider progressBar;
    private final ImageView startStopImage;

    public MusicPlayerLogic(
            HBox root, Label musicCurrentTime, ImageView thumbnail,
            Slider progressBar, ImageView startStopImage
    ) throws Exception {
        this.db = DbRepository.getInstance();

        this.musicCurrentTime = musicCurrentTime;
        this.progressBar = progressBar;
        this.startStopImage = startStopImage;
        this.root = root;
        this.thumbnail = thumbnail;

        playing = false;
        loopOne = false;
        loopAll = false;
        isSortMusicByName = false;
        autoPlay = true;
        playList = null;

        musicFiles = new ArrayList<>();
        musicMediaFiles = new ArrayList<>();
        normalOrderMusicFiles = new ArrayList<>();
        progressBarTimeline = createProgressBarTimeline();
    }

    /**
     * Setup tính năng của thanh thời lượng nhạc
     */
    public void useProgressBarLogic() {
        progressBar.setOnMousePressed(this::seekMusic);
        progressBar.setOnDragDetected(e -> progressBarTimeline.pause());
        progressBar.setOnMouseReleased(e -> { seekMusic(e); prevX = -9999; progressBarTimeline.playFromStart(); });
    }

    /**
     * chạy/dừng nhạc và sửa UI.
     * @return boolean đang chơi hay đang dừng nhạc
     */
    public boolean handleStartStopMusic() {
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

    /**
     * tắt mở lặp lại bài hát khi ở cuối hàng chờ
     */
    public boolean toggleLoopAll() {
        loopAll = !loopAll;
        return loopAll;
    }

    /**
     * tắt mở lặp lại bài hát hiện tại
     */
    public boolean toggleLoopOne() {
        loopOne = !loopOne;
        return loopOne;
    }

    /**
     * tắt mở sắp xếp bài hát theo tên
     */
    public boolean toggleSortMusicByName() {
        isSortMusicByName = !isSortMusicByName;

        if (isSortMusicByName) {
            sortMusicFilesByName();
            musicMediaFiles = musicFiles.stream().map(f -> new Media(f.toURI().toString()))
                    .collect(Collectors.toList());
        }
        else {
            musicFiles.clear();
            musicFiles.addAll(normalOrderMusicFiles);
            updateMediaFiles();
        }

        return isSortMusicByName;
    }

    /**
     * tắt mở tự động phát bài tiếp theo
     */
    public boolean toggleAutoPlay() {
        autoPlay = !autoPlay;
        return autoPlay;
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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Xóa hết nhạc khỏi hàng chờ nhạc
     */
    public void clearAllMusicFromQueue() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        progressBarTimeline.stop();
        setProgressBarToZero();
        progressBar.setDisable(true);

        musicFiles.clear();
        normalOrderMusicFiles.clear();
        musicMediaFiles.clear();
    }

    /**
     * Thêm nhạc từ FileChooser và chơi bài vừa thêm đầu tiên
     */
    public void addMusicFromFileChooser(Runnable onMediaPlayerReady) {
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
        selectedFiles = (selectedFiles == null) ? new ArrayList<>() : selectedFiles;

        musicFiles.addAll(selectedFiles);
        normalOrderMusicFiles.addAll(selectedFiles);

        if (isSortMusicByName) {
            sortMusicFilesByName();
        }

        musicMediaFiles = musicFiles.stream().map(f -> new Media(f.toURI().toString()))
                .collect(Collectors.toList());

        if (selectedFiles.size() != 0) {
            playMusicByIndex(indexOfMusicFile(selectedFiles.get(0)), onMediaPlayerReady);
        }
    }

    /**
     * Chơi nhạc gần đây. Xóa hàng chờ và thêm nhạc gần đây vào hàng chờ
     * @param rpm nhạc gần đây
     * @param onMediaPlayerReady callback khi media file sẵn sàng
     */
    public void playRecentlyPlayedMusic(RecentlyPlayedMusic rpm, Runnable onMediaPlayerReady) {
        playList = null;
        File file = new File(rpm.getPath());

        if (!file.exists()) {
            ButtonType okBtn = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.ERROR,"Không tìm thấy file '"+ rpm.getName() +
                    "' ở địa chỉ '" + rpm.getPath() + "'. File có thể đã bị xóa hoặc bị dời đi nơi khác", okBtn );
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("Không tìm thấy file nhạc");
            alert.showAndWait();
            db.deleteRecentlyPlayedMusic(rpm.getId());
            return;
        }

        musicFiles.clear();
        musicFiles.add(file);
        normalOrderMusicFiles.clear();
        normalOrderMusicFiles.add(file);

        musicMediaFiles.clear();
        musicMediaFiles.add(new Media(file.toURI().toString()));

        playMusicByIndex(0, onMediaPlayerReady);

        //move rpm to top of list
        db.deleteRecentlyPlayedMusic(rpm.getId());
        db.insertRecentlyPlayedMusic(rpm);
    }

    /**
     * Đưa nhạc từ playlist vào hàng chờ và chơi bài đầu tiên trong playlist
     * @param playlist playlist muốn chơi
     * @param onMediaPlayerReady callback khi media file sẵn sàng
     */
    public void playMusicInPlaylist(Playlist playlist, Runnable onMediaPlayerReady) {
        playList = playlist;
        musicFiles = Arrays.stream(playlist.getMusicPaths()).map(File::new)
                .filter(File::exists)
                .collect(Collectors.toList());
        normalOrderMusicFiles.clear();
        normalOrderMusicFiles.addAll(musicFiles);

        if (isSortMusicByName) {
            sortMusicFilesByName();
        }

        musicMediaFiles = musicFiles.stream().map(f -> new Media(f.toURI().toString()))
                .collect(Collectors.toList());

        playMusicByIndex(0, onMediaPlayerReady);
    }

    /**
     * Chơi nhạc tiếp theo trong hàng chờ
     * @param onMediaPlayerReady callback khi media file sẵn sàng
     */
    public void playNextMusic(Runnable onMediaPlayerReady) {
        int index = Math.min(musicMediaFiles.indexOf(getCurrentMusicMedia()) + 1, musicMediaFiles.size() - 1) ;
        playMusicByIndex(index, onMediaPlayerReady);
    }

    /**
     * Chơi nhạc trước đó trong hàng chờ
     * @param onMediaPlayerReady callback khi media file sẵn sàng
     */
    public void playPrevMusic(Runnable onMediaPlayerReady) {
        int index = Math.max(musicMediaFiles.indexOf(getCurrentMusicMedia()) - 1, 0);
        playMusicByIndex(index, onMediaPlayerReady);
    }

    /**
     * Chơi nhạc trong hàng chờ thông qua index của nhạc
     * @param index index của nhạc trong hàng chờ
     * @param onMediaPlayerReady callback khi media file sẵn sàng
     */
    public void playMusicByIndex(int index, Runnable onMediaPlayerReady) {
        if (musicMediaFiles.size() == 0) {
            return;
        }

        playing = true;
        startStopImage.setImage(new Image("/pause.png"));

        currentMusicFile = musicFiles.get(index);

        pauseCurrentMusic();

        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        mediaPlayer = new MediaPlayer(musicMediaFiles.get(index));
        progressBar.setDisable(false);
        progressBarTimeline.playFromStart();
        mediaPlayer.play();

        mediaPlayer.setOnReady(() -> {
            db.deleteRecentlyPlayedMusic(currentMusicFile.getPath());

            ObservableMap<String, Object> metadata = getCurrentMusicMedia().getMetadata();
            db.insertRecentlyPlayedMusic(new RecentlyPlayedMusic(
                    -1,
                    (String) metadata.getOrDefault("title", Utils.getNameWithoutExtension(currentMusicFile)),
                    currentMusicFile.getPath()
            ));

            if (onMediaPlayerReady != null) {
                onMediaPlayerReady.run();
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (loopOne) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
                return;
            }

            if (getCurrentMusicIndex() < musicMediaFiles.size() - 1) {
                if (autoPlay) {
                    playNextMusic(onMediaPlayerReady);
                }
                else {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.stop();
                    handleStartStopMusic();
                }
                return;
            }

            if (loopAll) {
                playMusicByIndex(0, onMediaPlayerReady);
            }
            else {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.stop();
                handleStartStopMusic();
            }
        });
    }

    /**
     * Xóa nhạc khỏi hàng chờ nhạc
     * @param musicIndex index của nhạc muốn xóa
     */
    public void removeMusicFromQueue(int musicIndex) {
        if (getCurrentMusicIndex() == musicIndex) {
            mediaPlayer.stop();
            mediaPlayer = null;

            progressBarTimeline.stop();
            setProgressBarToZero();
            progressBar.setDisable(true);
        }

        File musicFile = musicFiles.get(musicIndex);
        musicFiles.remove(musicIndex);
        normalOrderMusicFiles.remove(musicFile);
        musicMediaFiles.remove(musicIndex);
    }

    public List<File> getMusicFiles() {
        return musicFiles;
    }

    public List<Media> getMusicMediaFiles() {
        return musicMediaFiles;
    }

    public Media getCurrentMusicMedia() {
        return mediaPlayer == null ? null : mediaPlayer.getMedia();
    }

    public int getCurrentMusicIndex() {
        return indexOfMusicFile(currentMusicFile);
    }

    /**
     * Tìm index của file nhạc (so sánh địa chỉ, không dùng hàm indexof() để tránh so sánh nội dung file)
     */
    private int indexOfMusicFile(File musicFile) {
        for (int i = 0; i < musicFiles.size(); i++) {
            if (musicFiles.get(i) == musicFile) {
                return i;
            }
        }
        return -1;
    }

    public File getCurrentMusicFile() {
        return currentMusicFile;
    }

    /**
     * Dừng nhạc đang chơi
     */
    public void pauseCurrentMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            progressBarTimeline.pause();
        }
    }

    /**
     * chuyển thanh thời lượng về trạng thái vô định
     */
    private void setProgressBarToZero() {
        progressBar.setValue(0);
        musicCurrentTime.setText("00:00");
    }

    /**
     * Tạo timeline cho thanh thời lượng (cập nhật UI thanh theo thời gian nhạc đang chơi)
     * @return timeline
     */
    private Timeline createProgressBarTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            progressBar.setValue((mediaPlayer.getCurrentTime().toSeconds() / getCurrentMusicMedia().getDuration().toSeconds()) * 100);

            int secs = (int) Math.round(mediaPlayer.getCurrentTime().toSeconds());
            int minPart = secs/60;
            int secPart = secs -  minPart * 60;
            musicCurrentTime.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }


    private long prevX;

    /**
     * Seek nhạc theo con trỏ chuột
     * @param e event của con trỏ chuột
     */
    private void seekMusic(MouseEvent e) {
        if (getCurrentMusicMedia() == null) {
            return;
        }

        double time = (e.getX() / progressBar.getWidth()) * getCurrentMusicMedia().getDuration().toSeconds();

        if (Math.round(e.getX()) == prevX) {
            return;
        }

        prevX = Math.round(e.getX());
        mediaPlayer.seek(Duration.seconds(time));
    }

    /**
     * Cập nhật media file cho giống với file nhạc
     */
    private void updateMediaFiles() {
        musicMediaFiles = musicFiles.stream().map(f -> new Media(f.toURI().toString()))
                .collect(Collectors.toList());
    }

    /**
     * sắp xếp bài hát theo ngày
     */
    public void sortMusicFilesByName() {
        Collator collate = Collator.getInstance(new Locale("vi"));
        musicFiles.sort(Comparator.comparing(File::getName, collate));
    }
}
