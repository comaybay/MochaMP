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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.text.Collator;
import java.util.*;

public class MainController {
    public CheckBox cb_sort;
    public CheckBox cb_darkskin;
    public Pane player;
    public Pane tab;
    public Pane bottom;
    public CheckBox cb_autoplay;
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
    public Pane menu, songComponent;
    public Label text;
    public VBox songContainer;
    public VBox playlist;
    public Label playlistName;

    public Border songCardFocusBorder;
    public Background bgPause;
    public Background bgPlay;

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
                musicPlayerLogic.handleStartStopSong();
                onChangeBtnPlay();
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

        musicPlayerLogic.useProgressBarLogic();

        SortPlaylist();
        DarkSkin();
        AutoPlay();
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
        controller.setOnClickItem(playlist -> musicPlayerLogic.playSongInPlaylist(playlist, this::updateUI));

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
        Media currentMedia = musicPlayerLogic.getCurrentMedia();
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

        int index = musicPlayerLogic.getCurrentSongIndex();

        int itemCount = songContainer.getChildren().size();
        int songCount = musicPlayerLogic.getMusicFiles().size();

        if (itemCount != songCount) {
            songContainer.getChildren().clear();
            for (int i = 0; i < musicPlayerLogic.getMusicFiles().size(); i++) {
                songComponent = createSongComponent(i,musicPlayerLogic.getMusicFiles().get(i), musicPlayerLogic.getMediaFiles().get(i));
                songContainer.getChildren().add(songComponent);
            }
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

        Pane selectedSongCard = (Pane)songContainer.getChildren().get(index);
        selectedSongCard.setBorder(songCardFocusBorder);

        Button btn_play = (Button)selectedSongCard.getChildren().get(0);
        btn_play.setPrefSize(35,35);
        btn_play.setLayoutX(15);
        btn_play.setLayoutY(15);
        btn_play.setBackground(bgPause);
    }

    public Pane createSongComponent(int songIndex, File songFile, Media mediaFile) {
        ObservableMap<String, Object> metadata = mediaFile.getMetadata();
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
        Label artist = new Label((String) metadata.getOrDefault("artist", ""));
        artist.setPrefSize(169,21);
        artist.setLayoutX(65);
        artist.setLayoutY(40);
        artist.setFont(new Font(12));
        artist.setTextFill(Color.WHITE);
        //button dot
        Button btn_dot = new Button();
        btn_dot.setPrefSize(36,31);
        btn_dot.setLayoutX(243);
        btn_dot.setLayoutY(12);
        btn_dot.getStyleClass().add("btn-img");
        btn_dot.getStyleClass().add("bg-dot");
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
    //Cài đặt
    public void onHelloButtonClick(ActionEvent actionEvent) {
        if(!menu.isVisible()){
            menu.setVisible(true);
        }
        else {
            menu.setVisible(false);
        }
    }
    //Sắp xếp bài hát
    public void SortPlaylist() {
        List<File> list_music = musicPlayerLogic.getMusicFiles();
        List<Media> list_media = musicPlayerLogic.getMediaFiles();

        cb_sort.setOnMouseClicked(e ->{
            List<String> list_name_music = new ArrayList<>() ;
            List<File> list_music_update= new ArrayList<>();
            List<Integer> list_value = new ArrayList<>();
            int index = musicPlayerLogic.getCurrentSongIndex();
            if(cb_sort.isSelected()){
                songContainer.getChildren().clear();
                for (File file : list_music) {
                    list_name_music.add(file.getName());
                }
                Collator collate = Collator.getInstance(new Locale("vi"));
                Collections.sort(list_name_music, collate);
                for(int i = 0; i< list_name_music.size();i++){
                    for(int j = 0 ; j < list_name_music.size();j ++){
                        if(list_music.get(j).toString().contains(list_name_music.get(i))){
                            list_music_update.add(list_music.get(j));
                            list_value.add(j);
                        }
                    }
                }
                for (int i = 0; i < list_music_update.size(); i++) {
                    songComponent = createSongComponent(i,list_music_update.get(i), list_media.get(i));
                    songContainer.getChildren().add(songComponent);
                }
                int pos = index;
                for(int j =0 ; j < list_value.size(); j++){
                    if(list_value.get(j) == index){
                        pos = j;
                    }
                }
                Pane selectedSongCard = (Pane)songContainer.getChildren().get(pos);
                selectedSongCard.setBorder(songCardFocusBorder);

                Button btn_play = (Button)selectedSongCard.getChildren().get(0);
                btn_play.setPrefSize(35,35);
                btn_play.setLayoutX(15);
                btn_play.setLayoutY(15);
                btn_play.setBackground(bgPause);
            }
            else {
                songContainer.getChildren().clear();
                for (int i = 0; i < list_music.size(); i++) {
                    Pane songComponent = createSongComponent(i,list_music.get(i), list_media.get(i));
                    songContainer.getChildren().add(songComponent);
                }
                updatePlaylistUI();
            }
        });

    }
    //đồng bộ 2 btn play
    public void onChangeBtnPlay(){
        int index = musicPlayerLogic.getCurrentSongIndex();
        Pane selectedSongCard = (Pane)songContainer.getChildren().get(index);
        Button btn_play = (Button)selectedSongCard.getChildren().get(0);
        String[] split = startStopImage.getImage().getUrl().split("/");
        if (Objects.equals(split[split.length -1], "pause.png")) {
            btn_play.setBackground(bgPause);
        }
        else {
            btn_play.setBackground(bgPlay);
        }
    }
    //Check box đổi màu giao diện
    public void DarkSkin(){
        cb_darkskin.setOnMouseClicked(e -> {
            if(cb_darkskin.isSelected()){
                menu.setStyle("-fx-background-color:rgb(32, 31, 31);");
                player.setStyle("-fx-background-color:rgb(32, 31, 31);");
                playlist.setStyle("-fx-background-color:rgb(32, 31, 31);");
                tab.setStyle("-fx-background-color:rgb(32, 31, 31);");
                bottom.setStyle("-fx-background-color:rgb(32, 31, 31);");
                songContainer.setStyle("-fx-background-color:rgb(60, 60, 60);");
            }
            else {
                menu.setStyle("-fx-background-color: linear-gradient(to bottom, -primary, -primary-dark);");
                player.setStyle("-fx-background-color: linear-gradient(to bottom, -primary, -primary-dark);");
                playlist.setStyle("-fx-background-color: linear-gradient(to bottom, -primary, -primary-dark);");
                tab.setStyle("-fx-background-color: linear-gradient(to bottom, -primary, -primary-dark);");
                bottom.setStyle("-fx-background-color: linear-gradient(to bottom, -primary, -primary-dark);");
                songContainer.setStyle("-fx-background-color: -primary;");
            }
        });
    }
    //Tự động phát
    public void AutoPlay(){
        cb_autoplay.setOnMouseClicked(e -> {
            if(cb_autoplay.isSelected()){
                musicPlayerLogic.playSongByIndex(musicPlayerLogic.getCurrentSongIndex(), this::updateUI);
            }
            else {
                musicPlayerLogic.playOneSong(musicPlayerLogic.getCurrentSongIndex(), this::updateUI);
            }
        });
    }
}
