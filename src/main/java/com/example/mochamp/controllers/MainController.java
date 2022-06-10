package com.example.mochamp.controllers;

import com.example.mochamp.Database;
import com.example.mochamp.HelloApplication;
import com.example.mochamp.MusicPlayerLogic;
import com.example.mochamp.Utils;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

public class MainController {
    private Database db;
    private MusicPlayerLogic musicPlayerLogic = null;

    public HBox root;
    //head player
    public Label songTitle;
    public Label songArtist;
    public Label songDuration;
    public Label songCurrentTime;
    public ImageView thumbnail;
    public Slider progressBar;
    public StackPane startStopButton;
    public StackPane prevSong;
    public StackPane nextSong;
    public ImageView startStopImage;
    public ImageView closeButton;
    public ImageView minimizeButton;

    //playlist menu
    public Button selectRecentlyPlayedSongButton;
    public Button selectPlaylistButton;
    public Button openSongsButton;
    public Button savePlaylistButton;
    public Button shuffleSongsButton;
    public Button loopAllButton;
    public Button loopOneButton;
    public Button clearAllSongsButton;
    public Pane menu;
    public ScrollPane playlistScrollPane;
    public VBox songContainer;
    public VBox playlist;
    public Label playlistName;

    private Border songCardFocusBorder;
    private Background bgPause;
    private Background bgPlay;
    private Pane selectedSongCard;

    public void initialize() throws Exception {
        db = Database.getInstance();

        closeButton.setOnMouseClicked(e -> Platform.exit());

        minimizeButton.setOnMouseClicked(e -> {
            Stage obj = (Stage) minimizeButton.getScene().getWindow();
            obj.setIconified(true);
        });

        musicPlayerLogic = new MusicPlayerLogic(
                root, songTitle, songDuration, songCurrentTime, thumbnail, progressBar, startStopButton,
                startStopImage, openSongsButton, songContainer
        );

        menu.setVisible(false);

        //default image thumbnail
        thumbnail.setImage(imageCropSquare(new Image("/author.png")));
        thumbnail.setClip(new Circle(62.5, 62.5, 62.5));

        //backgrounds
        Image img_pause = new Image("image/2x/outline_pause_circle_filled_white_18dp.png");
        BackgroundImage bgImagedot = new BackgroundImage(img_pause, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        bgPause = new Background(bgImagedot);

        Image img_play = new Image("image/2x/outline_play_circle_filled_white_18dp.png");
        BackgroundImage bgImagePlay = new BackgroundImage(img_play, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        bgPlay = new Background(bgImagePlay);

        songCardFocusBorder = new Border(new BorderStroke(Color.YELLOW,
                Color.YELLOW,
                Color.YELLOW,
                Color.YELLOW,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10), BorderWidths.DEFAULT,
                new Insets(1)));

        startStopButton.setOnMouseClicked(e -> {
            if (e.getTarget() != startStopButton) {
                boolean state = musicPlayerLogic.handleStartStopSong();

                if (selectedSongCard != null) {
                    Button btn = (Button)selectedSongCard.getChildren().get(0);
                    btn.setBackground(state ? bgPause : bgPlay);
                }
            }
        });

        prevSong.setOnMouseClicked(e ->  {
            if (e.getTarget() != prevSong) {
                musicPlayerLogic.playPrevSong(this::updateUI);
            }
        });

        nextSong.setOnMouseClicked(e -> {
            if (e.getTarget() != nextSong) {
                musicPlayerLogic.playNextSong(this::updateUI);
            }
        });

        //Playlist menu
        selectRecentlyPlayedSongButton.setOnAction(e -> {
            try {
                openSelectRPSPopup();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        selectPlaylistButton.setOnAction(e -> {
            try {
                openSelectPlaylistPopup();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        openSongsButton.setOnAction(e -> onOpenSongs());

        savePlaylistButton.setOnAction(e -> onSavePlaylist());

        shuffleSongsButton.setOnAction(e -> {
            long seed = System.nanoTime();
            Collections.shuffle(musicPlayerLogic.getMediaFiles(), new Random(seed));
            Collections.shuffle(musicPlayerLogic.getMusicFiles(), new Random(seed));
            musicPlayerLogic.playSongByIndex(0, this::updateUI);
        });

        loopAllButton.setOnAction(e ->  {
            boolean isLoopAll = musicPlayerLogic.toggleLoopAll();
            loopAllButton.setStyle("-fx-opacity: " + (isLoopAll ? "1" : "0.5"));
        });
        loopAllButton.setOnMouseEntered(e -> loopAllButton.setStyle("-fx-opacity: 0.8"));
        loopAllButton.setOnMouseExited(e -> loopAllButton.setStyle("-fx-opacity: " + (musicPlayerLogic.isLoopAll() ? "1" : "0.5")));

        loopOneButton.setOnAction(e -> {
            boolean isLoopOne = musicPlayerLogic.toggleLoopOne();
            loopOneButton.setStyle("-fx-opacity: " + (isLoopOne ? "1" : "0.5"));
        });
        loopOneButton.setOnMouseEntered(e -> loopOneButton.setStyle("-fx-opacity: 0.8"));
        loopOneButton.setOnMouseExited(e -> loopOneButton.setStyle("-fx-opacity: " + (musicPlayerLogic.isLoopOne() ? "1" : "0.5")));

        clearAllSongsButton.setOnAction(e -> {
            if (musicPlayerLogic.getMusicFiles().size() == 0) {
                return;
            }

            ButtonType okBtn = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.NONE,"Xóa hết bài hát khỏi hàng chờ nhạc?", okBtn, cancelBtn);

            alert.setTitle("Xóa hết bài hát");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(cancelBtn) == okBtn) {
                musicPlayerLogic.clearAllSongs();
                updateUI();
            }
        });

        musicPlayerLogic.useProgressBarLogic();
    }

    private void openSelectRPSPopup() throws Exception {
        if (db.getRecentlyPlayedSongs().size() == 0) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("select-rps.fxml"));
        AnchorPane pane = loader.load();

        SelectRecentlyPlayedSongController controller = loader.getController();
        controller.setOnClickItem(rps -> musicPlayerLogic.playRecentlyPlayedSong(rps, this::updateUI));

        Utils.setupPopupPane(pane, selectRecentlyPlayedSongButton)
             .show();
    }

    private void openSelectPlaylistPopup() throws Exception {
        if (db.getSavedPlaylists().size() == 0) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("select-playlist.fxml"));
        AnchorPane pane = loader.load();

        SelectPlaylistController controller = loader.getController();
        controller.setup(
                playlist -> musicPlayerLogic.playSongInPlaylist(playlist, this::updateUI),
                playlist -> {
                    if (musicPlayerLogic.getPlaylist() != null &&
                            playlist.getId() == musicPlayerLogic.getPlaylist().getId()) {
                        musicPlayerLogic.setPlaylist(null);
                        musicPlayerLogic.clearAllSongs();
                        updateUI();
                    }
                });
        Utils.setupPopupPane(pane, selectPlaylistButton)
             .show();
    }

    public void onOpenSongs() {
        musicPlayerLogic.addSongsFromFileChooser(this::updateUI);
    }

    public void onSavePlaylist() {
        try {
            if (musicPlayerLogic.getMusicFiles().size() > 0) {
                DialogPane pane = null;

                if (musicPlayerLogic.getPlaylistName().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("save-playlist-dialog.fxml"));
                    pane = loader.load();

                    SavePlaylistDialogController controller = loader.getController();
                    controller.setup(musicPlayerLogic, this::updateUI);
                }
                else {
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("update-playlist-dialog.fxml"));
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
     * Cập nhật lại UI của chương trình
     */
    public void updateUI() {
        updatePlayerUI();
        updatePlaylistUI();
    }

    private void updatePlayerUI() {
        Media currentMedia = musicPlayerLogic.getCurrentSongMedia();

        if (currentMedia == null) {
            Image image = imageCropSquare(new Image("/author.png"));
            thumbnail.setImage(image);
            songTitle.setText("");
            songArtist.setText("");
            songDuration.setText("00:00");
            return;
        }

        ObservableMap<String, Object> metadata = currentMedia.getMetadata();
        Image image = imageCropSquare((Image)metadata.getOrDefault("image", new Image("/author.png")));
        thumbnail.setImage(image);

        String title = (String) metadata.getOrDefault("title", getNameWithoutExtension(musicPlayerLogic.getCurrentMusicFile()));
        String artist = (String) metadata.getOrDefault("artist", "");
        songTitle.setText(title);
        songArtist.setText(artist);

        int secs = (int) Math.round(currentMedia.getDuration().toSeconds());
        int minPart = secs/60;
        int secPart = secs -  minPart * 60;
        songDuration.setText(String.format("%02d", minPart) + ":" + String.format("%02d", secPart));
    }

    public void updatePlaylistUI() {
        String name = musicPlayerLogic.getPlaylistName();
        playlistName.setText(name);

        int itemCount = songContainer.getChildren().size();
        int songCount = musicPlayerLogic.getMusicFiles().size();

        if (itemCount != songCount) {
            songContainer.getChildren().clear();
            for (int i = 0; i < musicPlayerLogic.getMusicFiles().size(); i++) {
                Pane songComponent = createSongComponent(i);
                songContainer.getChildren().add(songComponent);
            }
        }

        if (songCount == 0) {
            return;
        }

        int i = 0;
        for (Node node : songContainer.getChildren()) {
            Pane songCard = (Pane) node;
            songCard.setBorder(null);
            Button btn_play = (Button)songCard.getChildren().get(0);
            btn_play.setBackground(bgPlay);

            ObservableMap<String, Object> metadata = musicPlayerLogic.getMediaFiles().get(i).getMetadata();
            String title = (String) metadata.getOrDefault("title", getNameWithoutExtension(musicPlayerLogic.getMusicFiles().get(i)));
            String artist = (String) metadata.getOrDefault("artist", "");

            Label titleLabel = (Label)songCard.getChildren().get(1);
            Label artistLabel = (Label)songCard.getChildren().get(2);
            titleLabel.setText(title);
            artistLabel.setText(artist);

            i++;
        };

        int currentSongIndex = musicPlayerLogic.getCurrentSongIndex();
        if (currentSongIndex >= 0) {
            selectedSongCard = (Pane)songContainer.getChildren().get(currentSongIndex);
            selectedSongCard.setBorder(songCardFocusBorder);

            Button btn_play = (Button)selectedSongCard.getChildren().get(0);
            btn_play.setPrefSize(35,35);
            btn_play.setLayoutX(15);
            btn_play.setLayoutY(15);
            btn_play.setBackground(bgPause);

            MediaPlayer mediaPlayer = musicPlayerLogic.getMediaPlayer();
            Runnable handler = mediaPlayer.getOnEndOfMedia();

            mediaPlayer.setOnEndOfMedia(() -> {
                handler.run();
                btn_play.setBackground(bgPlay);

                mediaPlayer.setOnPlaying(() -> {
                    btn_play.setBackground(bgPause);
                    mediaPlayer.setOnPlaying(null);
                });
            });

            //run later để chạy sau khi các thẻ bài hát đã được render ra hết
            //bỏ qua 2 bài đầu để bài hát đang chơi nằm ở giữa
            Platform.runLater(() -> playlistScrollPane.setVvalue(currentSongIndex / (songCount - 1.0f - 2)));
        }
    }

    public Pane createSongComponent(int songIndex) {
        File songFile = musicPlayerLogic.getMusicFiles().get(songIndex);
        Media mediaFile = musicPlayerLogic.getMediaFiles().get(songIndex);

        //button play
        Button btn_play = new Button();
        btn_play.setPrefSize(35,35);
        btn_play.setLayoutX(15);
        btn_play.setLayoutY(15);
        btn_play.setBackground(bgPlay);
        btn_play.setPickOnBounds(false);

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
        Button btn_dot = new Button();
        btn_dot.setPrefSize(26,21);
        btn_dot.setLayoutX(253);
        btn_dot.setLayoutY(24);
        btn_dot.getStyleClass().add("btn-img");
        btn_dot.getStyleClass().add("bg-x");
        btn_dot.setOnAction(e -> {
            ButtonType okBtn = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.NONE,"Xóa bài này khỏi hàng chờ nhạc? (" + songName.getText() + ")", okBtn, cancelBtn);

            alert.setTitle("Xóa playlist");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(cancelBtn) == okBtn) {
                musicPlayerLogic.removeSongFromQueue(songIndex);
                updateUI();
            }
        });

        //creat pane
        Pane pane = new Pane();
        pane.setPrefSize(200,74);
        pane.getChildren().addAll(btn_play,songName,artist,btn_dot);
        pane.setOnMouseClicked(e -> {
            if (musicPlayerLogic.getCurrentSongIndex() != songIndex) {
                handleSongSelected(songIndex);
                btn_play.setBackground(bgPause);
            }
            else {
                musicPlayerLogic.handleStartStopSong();
                btn_play.setBackground(btn_play.getBackground() == bgPlay ? bgPause : bgPlay);
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

    public void handleSongSelected(int songIndex) {
        musicPlayerLogic.playSongByIndex(songIndex, this::updateUI);
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
