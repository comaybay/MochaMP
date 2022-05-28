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

public class SavePlaylistDialogController {
    public TextField playlistField;
    public Label errorLabel;

    private Database db;
    private MusicPlayerLogic musicPlayerLogic;

    public void setMusicPlayerLogic(MusicPlayerLogic musicPlayerLogic) {
        this.musicPlayerLogic = musicPlayerLogic;
    }

    public void initialize() throws Exception {
        db = Database.getInstance();
    }

    public void onSave(ActionEvent event) {
        List<File> songFiles = musicPlayerLogic.getMusicFiles();
        String[] songPaths = songFiles.stream().map(file -> file.getPath()).collect(Collectors.toList()).toArray(new String[songFiles.size()]);

        String playlistName = playlistField.getText().trim();
        if (playlistName.isBlank()) {
            errorLabel.setText("*Tên playlist không được trống");
            return;
        }

        Playlist playlist = db.insertPlaylist(new Playlist(-1, playlistName, songPaths));
        musicPlayerLogic.playSongInPlaylist(playlist, null);
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
