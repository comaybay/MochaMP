package com.example.mochamp.models;

/**
 * Chứa thông tin về nhạc đã chơi gần đây
 */
public class RecentlyPlayedMusic {
    private final int id;
    private final String name;
    private final String path;

    public RecentlyPlayedMusic(int id, String name, String path) {
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
