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

public class UpdatePlaylistDialogController {
    public TextField playlistField;
    public Label errorLabel;

    private Database db;
    private MusicPlayerLogic musicPlayerLogic;
    private Runnable onSuccessHandler;

    public void setup(MusicPlayerLogic musicPlayerLogic, Runnable onSuccess) {
        this.musicPlayerLogic = musicPlayerLogic;
        playlistField.setText(musicPlayerLogic.getPlaylistName());
        onSuccessHandler = onSuccess;
    }

    public void initialize() throws Exception {
        db = Database.getInstance();
    }

    public void onUpdate(ActionEvent event) {
        List<File> songFiles = musicPlayerLogic.getMusicFiles();
        String[] songPaths = songFiles.stream().map(file -> file.getPath()).collect(Collectors.toList()).toArray(new String[songFiles.size()]);

        String playlistName = playlistField.getText().trim();

        if (!playlistName.equals(musicPlayerLogic.getPlaylistName()) && !checkValidName(playlistName)) {
            return;
        }

        Playlist updatedPlaylist = new Playlist(musicPlayerLogic.getPlaylist().getId(), playlistName, songPaths);
        db.updatePlaylist(updatedPlaylist);
        musicPlayerLogic.setPlaylist(updatedPlaylist);
        onSuccessHandler.run();
        closeDialog(event);
    }

    public void onNewPlaylist(ActionEvent event) {
        List<File> songFiles = musicPlayerLogic.getMusicFiles();
        String[] songPaths = songFiles.stream().map(file -> file.getPath()).collect(Collectors.toList()).toArray(new String[songFiles.size()]);

        String playlistName = playlistField.getText().trim();
        if (!checkValidName(playlistName)) {
            return;
        }

        Playlist playlist = db.insertPlaylist(new Playlist(-1, playlistName, songPaths));
        musicPlayerLogic.setPlaylist(playlist);
        onSuccessHandler.run();
        closeDialog(event);
    }

    private boolean checkValidName(String playlistName) {
        if (playlistName.isBlank()) {
            errorLabel.setText("*Tên playlist không được trống");
            return false;
        }

        if (db.playlistNameExists(playlistName)) {
            errorLabel.setText("*Tên playlist đã tồn tại, vui lòng đặt tên khác");
            return false;
        }

        return true;
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
