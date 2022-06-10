package com.example.mochamp.models;

/**
 * Chứa thông tin về playlist
 */
public class Playlist {
    private final int id;
    private final String name;
    private final String[] musicPaths;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getMusicPaths() {
        return musicPaths;
    }

    public Playlist(int id, String name, String[] musicPaths) {
        this.id = id;
        this.name = name;
        this.musicPaths = musicPaths;
    }
}
