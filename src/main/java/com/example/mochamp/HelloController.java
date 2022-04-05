package com.example.mochamp;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class HelloController implements Initializable {
    public Pane menu,playlist;
    public Label text;
    public VBox vbox;
    String[] songs = {"Aur Ho","Kun Faya Kun","Tango For Taj","Sheher Mein","Haawa Haawa","Nadaan Parinde","Sadda Haq",
            "Ánh nắng của anh","Hơn cả yêu","Người ấy","Sự thật sau một lời hứa","Miền cát trắng","Thương nhau tới bến","Y chang xuân sang","Rồi nâng cái ly"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menu.setVisible(false);
        for(int i =0; i < songs.length; i++) {
            List_pane(i);
        }
    }
    public void List_pane( int i){
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
        Label song = new Label(songs[i]);
        song.setPrefSize(169,27);
        song.setLayoutX(65);
        song.setLayoutY(10);
        song.setFont(new Font(18));
        song.setTextFill(Color.WHITE);
        //text sing
        Label sing = new Label("A.R Rahaman");
        sing.setPrefSize(169,21);
        sing.setLayoutX(65);
        sing.setLayoutY(40);
        sing.setFont(new Font(12));
        sing.setTextFill(Color.WHITE);
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
        pane.setLayoutX(0);
        pane.setLayoutY(54*i);
        pane.getChildren().addAll(btn_play,sing,song,btn_dot);
        vbox.getChildren().add(pane);
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
                        pane.getChildren().setAll(btn_play,sing,song,btn_dot);
                    }
                });
    }
    public void onHelloButtonClick(ActionEvent actionEvent) {
        if(!menu.isVisible()){
            menu.setVisible(true);
        }
        else {
            menu.setVisible(false);
        }
    }
    public void onFindClick(ActionEvent actionEvent) {

    }
    public void onScroll(ScrollEvent scrollEvent) {
        if (vbox.getTranslateY()<=0){
            vbox.setTranslateY(vbox.getTranslateY() + scrollEvent.getDeltaY());
        }else {
            vbox.setTranslateY(0);
        }
    }
}