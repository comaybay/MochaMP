package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.MochaMPApplication;
import com.example.mochamp.MusicPlayerLogic;
import com.example.mochamp.Utils;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class MainController {
    public Button settingsButton;
    private Database db;
    private MusicPlayerLogic musicPlayerLogic = null;
    private Background bgPause;
    private Background bgPlay;
    private Border musicCardFocusBorder;
    private Pane selectedMusicCard;

    public HBox root;

    // biến của phần chơi nhạc
    public Label musicTitle;
    public Label musicArtist;
    public Label musicDuration;
    public Label musicCurrentTime;
    public ImageView thumbnail;
    public Slider progressBar;
    public StackPane startStopButton;
    public StackPane prevMusic;
    public StackPane nextMusic;
    public ImageView startStopImage;
    public ImageView closeButton;
    public ImageView minimizeButton;

    // biến của phần playlist
    public Button selectRecentlyPlayedMusicButton;
    public Button selectPlaylistButton;
    public Button openMusicButton;
    public Button savePlaylistButton;
    public Button shuffleMusicButton;
    public Button loopAllButton;
    public Button loopOneButton;
    public Button clearAllMusicButton;
    public Pane menu;
    public ScrollPane playlistScrollPane;
    public VBox musicContainer;
    public VBox playlist;
    public Label playlistName;

    /**
     * Setup màn hình chính của chương trình
     */
    public void initialize() throws Exception {
        db = Database.getInstance();

        closeButton.setOnMouseClicked(e -> Platform.exit());

        minimizeButton.setOnMouseClicked(e -> {
            Stage obj = (Stage) minimizeButton.getScene().getWindow();
            obj.setIconified(true);
        });

        menu.setVisible(false);

        // setup thumbnail
        thumbnail.setClip(new Circle(62.5, 62.5, 62.5));
        thumbnail.setImage(Utils.imageCropSquare(new Image("/author.png")));

        // setup biến background
        Image img_pause = new Image("image/2x/outline_pause_circle_filled_white_18dp.png");
        BackgroundImage bgImagedot = new BackgroundImage(img_pause, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        bgPause = new Background(bgImagedot);

        Image img_play = new Image("image/2x/outline_play_circle_filled_white_18dp.png");
        BackgroundImage bgImagePlay = new BackgroundImage(img_play, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        bgPlay = new Background(bgImagePlay);

        musicCardFocusBorder = new Border(new BorderStroke(Color.YELLOW,
                Color.YELLOW,
                Color.YELLOW,
                Color.YELLOW,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10), BorderWidths.DEFAULT,
                new Insets(1)));

        // setup logic nhạc
        musicPlayerLogic = new MusicPlayerLogic(
                root, musicCurrentTime, thumbnail,
                progressBar, startStopImage
        );

        // setup phần chơi nhạc
        musicPlayerLogic.useProgressBarLogic();

        startStopButton.setOnMouseClicked(e -> {
            if (e.getTarget() != startStopButton) {
                boolean state = musicPlayerLogic.handleStartStopMusic();

                if (selectedMusicCard != null) {
                    Button btn = (Button) selectedMusicCard.getChildren().get(0);
                    btn.setBackground(state ? bgPause : bgPlay);
                }
            }
        });

        prevMusic.setOnMouseClicked(e ->  {
            if (e.getTarget() != prevMusic) {
                musicPlayerLogic.playPrevMusic(this::updateUI);
            }
        });

        nextMusic.setOnMouseClicked(e -> {
            if (e.getTarget() != nextMusic) {
                musicPlayerLogic.playNextMusic(this::updateUI);
            }
        });

        // setup phần playlist
        selectRecentlyPlayedMusicButton.setOnAction(e -> {
            try {
                onCLickSelectRPSButton();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        selectPlaylistButton.setOnAction(e -> {
            try {
                openClickSelectPlaylistButton();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        openMusicButton.setOnAction(e -> musicPlayerLogic.addMusicFromFileChooser(this::updateUI));

        savePlaylistButton.setOnAction(e -> onClickSavePlaylistButton());

        shuffleMusicButton.setOnAction(e -> {
            long seed = System.nanoTime();
            Collections.shuffle(musicPlayerLogic.getMusicMediaFiles(), new Random(seed));
            Collections.shuffle(musicPlayerLogic.getMusicFiles(), new Random(seed));
            musicPlayerLogic.playMusicByIndex(0, this::updateUI);
        });

        Function<Boolean, String> getOpacityStyle = hovered -> "-fx-opacity: " + (hovered ? "1" : "0.5");

        loopAllButton.setOnAction(e ->  {
            boolean isLoopAll = musicPlayerLogic.toggleLoopAll();
            loopAllButton.setStyle(getOpacityStyle.apply(isLoopAll));
        });
        loopAllButton.setOnMouseEntered(e -> loopAllButton.setStyle("-fx-opacity: 0.8"));
        loopAllButton.setOnMouseExited(e -> loopAllButton.setStyle(getOpacityStyle.apply(musicPlayerLogic.isLoopAll())));

        loopOneButton.setOnAction(e -> {
            boolean isLoopOne = musicPlayerLogic.toggleLoopOne();
            loopOneButton.setStyle(getOpacityStyle.apply(isLoopOne));
        });
        loopOneButton.setOnMouseEntered(e -> loopOneButton.setStyle("-fx-opacity: 0.8"));
        loopOneButton.setOnMouseExited(e -> loopOneButton.setStyle(getOpacityStyle.apply(musicPlayerLogic.isLoopOne())));

        clearAllMusicButton.setOnAction(e -> {
            if (musicPlayerLogic.getMusicFiles().size() == 0) {
                return;
            }

            ButtonType okBtn = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.NONE,"Xóa hết nhạc khỏi hàng chờ nhạc?", okBtn, cancelBtn);

            alert.setTitle("Xóa hết nhạc");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(cancelBtn) == okBtn) {
                musicPlayerLogic.clearAllMusicFromQueue();
                updateUI();
            }
        });
    }

    /**
     * Mở popup chọn nhạc chơi gần đây nếu có chơi nhạc nào đó gần đây
     */
    private void onCLickSelectRPSButton() throws Exception {
        if (db.getRecentlyPlayedMusic().size() == 0) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(MochaMPApplication.class.getResource("select-rps.fxml"));
        AnchorPane pane = loader.load();

        SelectRecentlyPlayedMusicController controller = loader.getController();
        controller.setup(rps -> musicPlayerLogic.playRecentlyPlayedMusic(rps, this::updateUI));

        Utils.setupPopupPane(pane, selectRecentlyPlayedMusicButton)
             .show();
    }

    /**
     * Mở popup playlist đã lưu nếu có lưu playlist
     */
    private void openClickSelectPlaylistButton() throws Exception {
        if (db.getSavedPlaylists().size() == 0) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(MochaMPApplication.class.getResource("select-playlist.fxml"));
        AnchorPane pane = loader.load();

        SelectPlaylistController controller = loader.getController();
        controller.setup(
                playlist -> musicPlayerLogic.playMusicInPlaylist(playlist, this::updateUI),
                playlist -> {
                    if (musicPlayerLogic.getPlaylist() != null &&
                            playlist.getId() == musicPlayerLogic.getPlaylist().getId()) {
                        musicPlayerLogic.setPlaylist(null);
                        musicPlayerLogic.clearAllMusicFromQueue();
                        updateUI();
                    }
                });
        Utils.setupPopupPane(pane, selectPlaylistButton)
             .show();
    }

    /**
     * Lưu hoặc Cập nhật playlist nếu có nhạc trong hàng chờ
     */
    private void onClickSavePlaylistButton() {
        try {
            if (musicPlayerLogic.getMusicFiles().size() > 0) {
                DialogPane pane;

                if (musicPlayerLogic.getPlaylistName().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(MochaMPApplication.class.getResource("save-playlist-dialog.fxml"));
                    pane = loader.load();

                    SavePlaylistDialogController controller = loader.getController();
                    controller.setup(musicPlayerLogic, this::updateUI);
                }
                else {
                    FXMLLoader loader = new FXMLLoader(MochaMPApplication.class.getResource("update-playlist-dialog.fxml"));
                    pane = loader.load();

                    UpdatePlaylistDialogController controller = loader.getController();
                    controller.setup(musicPlayerLogic, this::updateUI);
                }

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.setDialogPane(pane);
                dialog.show();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Cập nhật lại UI của chương trình, gồm cập nhật phần chơi nhạc và phần playlist
     */
    private void updateUI() {
        updatePlayerUI();
        updatePlaylistUI();
    }

    private void updatePlayerUI() {
        Media currentMedia = musicPlayerLogic.getCurrentMusicMedia();

        if (currentMedia == null) {
            Image image = Utils.imageCropSquare(new Image("/author.png"));
            thumbnail.setImage(image);
            musicTitle.setText("");
            musicArtist.setText("");
            musicDuration.setText("00:00");
            return;
        }

        ObservableMap<String, Object> metadata = currentMedia.getMetadata();
        Image image = Utils.imageCropSquare((Image)metadata.getOrDefault("image", new Image("/author.png")));
        thumbnail.setImage(image);

        String title = (String) metadata.getOrDefault("title", Utils.getNameWithoutExtension(musicPlayerLogic.getCurrentMusicFile()));
        String artist = (String) metadata.getOrDefault("artist", "");
        musicTitle.setText(title);
        musicArtist.setText(artist);

        int secs = (int) Math.round(currentMedia.getDuration().toSeconds());
        int minPart = secs/60;
        int secPart = secs -  minPart * 60;
        musicDuration.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
    }

    public void updatePlaylistUI() {
        String name = musicPlayerLogic.getPlaylistName();
        playlistName.setText(name);

        int itemCount = musicContainer.getChildren().size();
        int musicCount = musicPlayerLogic.getMusicFiles().size();

        if (itemCount != musicCount) {
            musicContainer.getChildren().clear();
            for (int i = 0; i < musicPlayerLogic.getMusicFiles().size(); i++) {
                Pane musicCardComponent = createMusicCardComponent(i);
                musicContainer.getChildren().add(musicCardComponent);
            }
        }

        if (musicCount == 0) {
            return;
        }

        int i = 0;
        for (Node node : musicContainer.getChildren()) {
            Pane musicCard = (Pane) node;
            musicCard.setBorder(null);
            Button btn_play = (Button)musicCard.getChildren().get(0);
            btn_play.setBackground(bgPlay);

            ObservableMap<String, Object> metadata = musicPlayerLogic.getMusicMediaFiles().get(i).getMetadata();
            String title = (String) metadata.getOrDefault("title", Utils.getNameWithoutExtension(musicPlayerLogic.getMusicFiles().get(i)));
            String artist = (String) metadata.getOrDefault("artist", "");

            Label titleLabel = (Label)musicCard.getChildren().get(1);
            Label artistLabel = (Label)musicCard.getChildren().get(2);
            titleLabel.setText(title);
            artistLabel.setText(artist);

            i++;
        }

        int currentMusicIndex = musicPlayerLogic.getCurrentMusicIndex();
        if (currentMusicIndex >= 0) {
            selectedMusicCard = (Pane) musicContainer.getChildren().get(currentMusicIndex);
            selectedMusicCard.setBorder(musicCardFocusBorder);

            Button btn_play = (Button) selectedMusicCard.getChildren().get(0);
            btn_play.setPrefSize(35,35);
            btn_play.setLayoutX(15);
            btn_play.setLayoutY(15);
            btn_play.setBackground(bgPause);

            MediaPlayer mediaPlayer = musicPlayerLogic.getMediaPlayer();
            Runnable handler = mediaPlayer.getOnEndOfMedia();

            mediaPlayer.setOnEndOfMedia(() -> {
                handler.run();

                if (musicPlayerLogic.isLoopAll() || musicPlayerLogic.isLoopOne()) {
                    return;
                }

                btn_play.setBackground(bgPlay);
                mediaPlayer.setOnPlaying(() -> {
                    btn_play.setBackground(bgPause);
                    mediaPlayer.setOnPlaying(null);
                });
            });

            //run later để chạy sau khi các thẻ nhạc đã được render ra hết
            //bỏ qua 2 bài đầu để nhạc đang chơi nằm ở giữa
            Platform.runLater(() -> playlistScrollPane.setVvalue(currentMusicIndex / (musicCount - 1.0f - 2)));
        }
    }

    /**
     * Tạo component thẻ nhạc
     * @param musicIndex index của nhạc trong danh sách chờ
     * @return thẻ nhạc được tạo
     */
    public Pane createMusicCardComponent(int musicIndex) {
        File musicFile = musicPlayerLogic.getMusicFiles().get(musicIndex);
        Media mediaFile = musicPlayerLogic.getMusicMediaFiles().get(musicIndex);

        // nút chơi nhạc
        Button startStopButton = new Button();
        startStopButton.setPrefSize(35,35);
        startStopButton.setLayoutX(15);
        startStopButton.setLayoutY(15);
        startStopButton.setBackground(bgPlay);
        startStopButton.setPickOnBounds(false);

        // tên nhạc
        Label musicName = new Label(Utils.getNameWithoutExtension(musicFile));
        musicName.setPrefSize(169,27);
        musicName.setLayoutX(65);
        musicName.setLayoutY(10);
        musicName.setFont(new Font(18));
        musicName.setTextFill(Color.WHITE);

        // tác giả
        Label artist = new Label();
        artist.setPrefSize(169,21);
        artist.setLayoutX(65);
        artist.setLayoutY(40);
        artist.setFont(new Font(12));
        artist.setTextFill(Color.WHITE);

        // nút xóa
        Button deleteButton = new Button();
        deleteButton.setPrefSize(26,21);
        deleteButton.setLayoutX(246);
        deleteButton.setLayoutY(24);
        deleteButton.getStyleClass().add("btn-img");
        deleteButton.getStyleClass().add("bg-x");
        deleteButton.setOnAction(e -> {
            ButtonType okBtn = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.NONE,"Xóa bài này khỏi hàng chờ nhạc? (" + musicName.getText() + ")", okBtn, cancelBtn);

            alert.setTitle("Xóa playlist");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(cancelBtn) == okBtn) {
                musicPlayerLogic.removeMusicFromQueue(musicIndex);
                updateUI();
            }
        });

        // thẻ
        Pane pane = new Pane();
        pane.setPrefSize(200,74);
        pane.getChildren().addAll(startStopButton,musicName,artist,deleteButton);
        pane.setOnMouseClicked(e -> {
            if (musicPlayerLogic.getCurrentMusicIndex() != musicIndex) {
                musicPlayerLogic.playMusicByIndex(musicIndex, this::updateUI);
                startStopButton.setBackground(bgPause);
            }
            else {
                musicPlayerLogic.handleStartStopMusic();
                startStopButton.setBackground(startStopButton.getBackground() == bgPlay ? bgPause : bgPlay);
            }
        });

        // đợi đến khi metadata của nhạc được tìm thấy để gán giá trị cho thẻ nhạc
        mediaFile.getMetadata().addListener((MapChangeListener<String, Object>) change  -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("artist")) {
                    artist.setText(change.getValueAdded().toString());
                }
                else if (change.getKey().equals("title")) {
                    System.out.println(musicName);
                    musicName.setText(change.getValueAdded().toString());
                }
            }
        });

        return pane;
    }

    /**
     * Mở mục cài đặt
     */
    public void onSettingsButtonClick() {
        menu.setVisible(!menu.isVisible());
    }
}
