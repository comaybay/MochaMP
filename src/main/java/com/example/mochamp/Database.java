package com.example.mochamp;

import com.example.mochamp.models.Playlist;
import com.example.mochamp.models.RecentlyPlayedSong;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Database {
    String url;
    String unameDB;
    String passDB;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    private static Database instance = null;
    public static Database getInstance() throws Exception{
        if (instance == null) {
            List<String> settings = Files.readAllLines(Paths.get("database-settings.txt"))
                    .stream().map(s -> s.split("=")[1])
                    .collect(Collectors.toList());

            instance = new Database(settings.get(0), settings.get(1), settings.get(2));
            instance.init();
        }

        return instance;
    }

    public Database(String url, String unameDB, String passDB) {
        this.url = url;
        this.unameDB = unameDB;
        this.passDB = passDB;
    }

    /**
     * load driver và khởi tạo database nếu chưa có, chỉ chạy hàm này một lần
     */
    private void init() throws ClassNotFoundException{
        Class.forName("org.postgresql.Driver"); // load the driver for postgres

        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet rs = stm.executeQuery("SELECT datname FROM pg_catalog.pg_database WHERE datname = 'MochaMP'\n");
        ) {
           boolean hasTable = rs.next();
           if (!hasTable) {
               stm.executeUpdate("CREATE DATABASE \"MochaMP\"");
           }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        url += "MochaMP";
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
        ) {
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS public.recently_played_songs(" +
                    "\nid bigint NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "\nname character varying NOT NULL," +
                    "\npath character varying NOT NULL" +
                    "\n)");

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS public.playlists(" +
                    "\nid integer NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "\nname character varying NOT NULL," +
                    "\nsong_paths character varying[] NOT NULL" +
                    "\n)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, unameDB, passDB);
    }

    public ArrayList<RecentlyPlayedSong> getRecentlyPlayedSongs() {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM recently_played_songs ORDER BY id DESC LIMIT 10")) {

            ArrayList<RecentlyPlayedSong> songs = new ArrayList<>();
            while (resultSet.next()) {
                songs.add(new RecentlyPlayedSong(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("path")
                ));
            }

            return songs;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<Playlist> getSavedPlaylists() {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM playlists ORDER BY id DESC");) {

            ArrayList<Playlist> playlists = new ArrayList<>();
            // display the column header in the ResultSet
            while (resultSet.next()) {
                playlists.add(new Playlist(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        (String[]) resultSet.getArray("song_paths").getArray()
                ));
            }

            return playlists;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String formatField(String fieldValue) {
        return "'" + fieldValue + "'";
    }

    private String formatField(String[] fieldValues) {
        String res = "'{";

        for (int i = 0; i < fieldValues.length; i++) {
            String value = fieldValues[i];
            res += (i == 0 ? "\"" : ", \"") + value + "\"";
        }

        res += "}'";
        return res.replace("\\", "\\\\");
    }

    /**
     * thêm bài hát gần đây vào CSDL
     * @param rps bài hát gần đây
     */
    public void insertRecentlyPlayedSong(RecentlyPlayedSong rps) {
        try (
            Connection con = createConnection();
            Statement stm = con.createStatement();
        ) {
        String name =rps.getName().replace("'", "''").replace("\0", "");
        stm.executeUpdate("INSERT INTO recently_played_songs(name, path) VALUES(" +
                formatField(name) + ","+
                formatField(rps.getPath()) +
                ")");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecentlyPlayedSongById(int id){
        try (
                Connection con = createConnection();
                Statement stm = con.createStatement();
        ) {
            stm.executeUpdate("DELETE FROM recently_played_songs WHERE id = " + formatField(Integer.toString(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecentlyPlayedSongByPath(String path){
        try (
                Connection con = createConnection();
                Statement stm = con.createStatement();
        ) {
            stm.executeUpdate("DELETE FROM recently_played_songs WHERE path = " + formatField(path));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * thêm playlist vào CSDL
     * @param pl playlist
     * @return playlist được thêm vào (chứa cả id được gán trong CSDL)
     */
    public Playlist insertPlaylist(Playlist pl) {
        System.out.println(formatField(pl.getSongPaths()));
        try (Connection con = createConnection();
             Statement stm = con.createStatement();) {
            stm.executeUpdate("INSERT INTO playlists(name, song_paths) VALUES(" +
                    formatField(pl.getName()) + ","+
                    formatField(pl.getSongPaths()) +
                    ")");

            ResultSet resultSet =  stm.executeQuery("SELECT id FROM playlists ORDER BY id DESC LIMIT 1");
            resultSet.next();
            return new Playlist(resultSet.getInt("id"), pl.getName(), pl.getSongPaths());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * cập nhật playlist vào CSDL
     * @param pl playlist
     */
    public void updatePlaylist(Playlist pl) {
        System.out.println(formatField(pl.getSongPaths()));
        try (Connection con = createConnection();
             Statement stm = con.createStatement();) {
            stm.executeUpdate("UPDATE playlists " +
                    "SET name=" + formatField(pl.getName()) + ","+
                    "song_paths=" + formatField(pl.getSongPaths()) +
                    "WHERE id=" + formatField(Integer.toString(pl.getId())) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlaylist(int id) {
        try (Connection con = createConnection();
          Statement stm = con.createStatement();){
            stm.executeUpdate("DELETE FROM playlists WHERE id = " + formatField(Integer.toString(id)));
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean playlistNameExists(String name) {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM playlists WHERE name=" + formatField(name));) {

            if (resultSet.next()) {
                return true;
            }

            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
