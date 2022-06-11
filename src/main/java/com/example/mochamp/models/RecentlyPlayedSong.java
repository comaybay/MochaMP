package com.example.mochamp.models;

public class RecentlyPlayedSong {
    private int id;
    private String name;
    private String path;

    public RecentlyPlayedSong(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
