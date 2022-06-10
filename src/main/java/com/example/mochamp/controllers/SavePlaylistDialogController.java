package com.example.mochamp.controllers;


import com.example.mochamp.Database;
import com.example.mochamp.MusicPlayerLogic;
import com.example.mochamp.models.Playlist;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog để người dùng nhập thông tin cần thiết trước khi lưu playlist
 */
public class SavePlaylistDialogController {
    public TextField playlistField;
    public Label errorLabel;

    private Database db;
    private MusicPlayerLogic musicPlayerLogic;
    private Runnable onSuccessHandler;

    public void setup(MusicPlayerLogic musicPlayerLogic, Runnable onSuccess) {
        this.musicPlayerLogic = musicPlayerLogic;
        onSuccessHandler = onSuccess;
    }

    public void initialize() throws Exception {
        db = Database.getInstance();
    }

    public void onSave(ActionEvent event) {
        List<File> musicFiles = musicPlayerLogic.getMusicFiles();
        String[] musicPaths = musicFiles.stream().map(File::getPath).collect(Collectors.toList()).toArray(new String[musicFiles.size()]);

        String playlistName = playlistField.getText().trim();
        if (playlistName.isBlank()) {
            errorLabel.setText("*Tên playlist không được trống");
            return;
        }

        if (db.playlistNameExists(playlistName)) {
            errorLabel.setText("*Tên playlist đã tồn tại, vui lòng đặt tên khác");
            return;
        }

        Playlist playlist = db.insertPlaylist(new Playlist(-1, playlistName, musicPaths));
        musicPlayerLogic.playMusicInPlaylist(playlist, null);
        onSuccessHandler.run();
        closeDialog(event);
    }

    public void onCancel(ActionEvent event) {
        closeDialog(event);
    }

    private void closeDialog(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
