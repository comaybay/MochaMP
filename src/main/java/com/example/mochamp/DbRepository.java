package com.example.mochamp;

import com.example.mochamp.models.Playlist;
import com.example.mochamp.models.RecentlyPlayedMusic;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DbRepository {
    String url;
    String usernameDB;
    String passDB;

    private static DbRepository instance = null;

    /**
     * lấy đối tượng singleton
     */
    public static DbRepository getInstance() throws Exception{
        if (instance == null) {
            List<String> settings = Files.readAllLines(Paths.get("database-settings.txt"))
                    .stream().map(s -> s.split("=")[1])
                    .collect(Collectors.toList());

            instance = new DbRepository(settings.get(0), settings.get(1), settings.get(2));
            instance.init();
        }

        return instance;
    }

    private DbRepository(String url, String usernameDB, String passDB) {
        this.url = url;
        this.usernameDB = usernameDB;
        this.passDB = passDB;
    }

    /**
     * load driver và khởi tạo database nếu chưa có, hàm này chỉ chạy một lần
     */
    private void init() throws ClassNotFoundException{
        Class.forName("org.postgresql.Driver"); // load the driver for postgres

        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet rs = stm.executeQuery("SELECT datname FROM pg_catalog.pg_database WHERE datname = 'MochaMP'\n")
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
             Statement stm = con.createStatement()
        ) {
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS public.recently_played_music(" +
                    "\nid bigint NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "\nname character varying NOT NULL," +
                    "\npath character varying NOT NULL" +
                    "\n)");

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS public.playlists(" +
                    "\nid integer NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "\nname character varying NOT NULL," +
                    "\nmusic_paths character varying[] NOT NULL" +
                    "\n)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, usernameDB, passDB);
        }
        catch (SQLException e) {
            ButtonType okBtn = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Lỗi khi tương tác với CSDL, hãy kiểm tra lại phần cài đặt CSDL trong file database-settings.txt",
                    okBtn );
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("Lỗi cơ sở dữ liệu");
            alert.showAndWait();
            Platform.exit();
            throw e;
        }
    }

    /**
     * Lấy nhạc đã chơi gần đây
     * @return 10 nhạc gần đây nhất
     */
    public List<RecentlyPlayedMusic> getRecentlyPlayedMusic() {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM recently_played_music ORDER BY id DESC LIMIT 10")) {

            ArrayList<RecentlyPlayedMusic> musicArr = new ArrayList<>();
            while (resultSet.next()) {
                musicArr.add(new RecentlyPlayedMusic(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("path")
                ));
            }

            return musicArr;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy các playlist đã lưu
     * @return các playlist đã lưu
     */
    public List<Playlist> getSavedPlaylists() {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM playlists ORDER BY id DESC")) {

            ArrayList<Playlist> playlists = new ArrayList<>();
            // display the column header in the ResultSet
            while (resultSet.next()) {
                playlists.add(new Playlist(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        (String[]) resultSet.getArray("music_paths").getArray()
                ));
            }

            return playlists;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * format trường để cho phù hợp khi đưa vào CSDL
     * @param fieldValue trường
     * @return trường đã được format
     */
    private String formatField(String fieldValue) {
        return "'" + fieldValue + "'";
    }

    /**
     * format trường thuộc kiểu mảng để cho phù hợp khi đưa vào CSDL
     * @param fieldValues trường thuộc kiểu mảng
     * @return trường đã được format
     */
    private String formatField(String[] fieldValues) {
        StringBuilder res = new StringBuilder("'{");

        for (int i = 0; i < fieldValues.length; i++) {
            String value = fieldValues[i];
            res.append(i == 0 ? "\"" : ", \"").append(value).append("\"");
        }

        res.append("}'");
        return res.toString().replace("\\", "\\\\");
    }

    /**
     * thêm nhạc gần đây vào CSDL
     * @param rpm nhạc gần đây
     */
    public void insertRecentlyPlayedMusic(RecentlyPlayedMusic rpm) {
        try (
            Connection con = createConnection();
            Statement stm = con.createStatement()
        ) {
        String name =rpm.getName().replace("'", "''").replace("\0", "");
        stm.executeUpdate("INSERT INTO recently_played_music(name, path) VALUES(" +
                formatField(name) + ","+
                formatField(rpm.getPath()) +
                ")");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa nhạc gần đây
     * @param id mã của nhạc gần đây muốn xóa
     */
    public void deleteRecentlyPlayedMusic(int id){
        try (
                Connection con = createConnection();
                Statement stm = con.createStatement()
        ) {
            stm.executeUpdate("DELETE FROM recently_played_music WHERE id = " + formatField(Integer.toString(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa nhạc gần đây
     * @param path đường dẫn của nhạc gần đây muốn xóa
     */
    public void deleteRecentlyPlayedMusic(String path){
        try (
                Connection con = createConnection();
                Statement stm = con.createStatement()
        ) {
            stm.executeUpdate("DELETE FROM recently_played_music WHERE path = " + formatField(path));
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
        try (Connection con = createConnection();
             Statement stm = con.createStatement()) {
            stm.executeUpdate("INSERT INTO playlists(name, music_paths) VALUES(" +
                    formatField(pl.getName()) + ","+
                    formatField(pl.getMusicPaths()) +
                    ")");

            ResultSet resultSet =  stm.executeQuery("SELECT id FROM playlists ORDER BY id DESC LIMIT 1");
            resultSet.next();
            return new Playlist(resultSet.getInt("id"), pl.getName(), pl.getMusicPaths());
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
        try (Connection con = createConnection();
             Statement stm = con.createStatement()) {
            stm.executeUpdate("UPDATE playlists " +
                    "SET name=" + formatField(pl.getName()) + ","+
                    "music_paths=" + formatField(pl.getMusicPaths()) +
                    "WHERE id=" + formatField(Integer.toString(pl.getId())) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa playlist
     * @param id mã của playlist muốn xóa
     */
    public void deletePlaylist(int id) {
        try (Connection con = createConnection();
          Statement stm = con.createStatement()){
            stm.executeUpdate("DELETE FROM playlists WHERE id = " + formatField(Integer.toString(id)));
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem tên playlist có tồn tại hay không
     * @param name tên của playlist muốn kiểm tra
     * @return tên đã tồn tại hay không
     */
    public boolean playlistNameExists(String name) {
        try (Connection con = createConnection();
             Statement stm = con.createStatement();
             ResultSet resultSet = stm.executeQuery("SELECT * FROM playlists WHERE name=" + formatField(name))) {

            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
