package com.example.mochamp.models;

public class Playlist {
    private int id;
    private String name;
    private String[] songPaths;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getSongPaths() {
        return songPaths;
    }

    public Playlist(int id, String name, String[] songPaths) {
        this.id = id;
        this.name = name;
        this.songPaths = songPaths;
    }
}
