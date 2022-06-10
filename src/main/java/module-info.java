module com.example.mochamp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.example.mochamp to javafx.fxml;
    exports com.example.mochamp;
    exports com.example.mochamp.controllers;
    exports com.example.mochamp.models;
    opens com.example.mochamp.controllers to javafx.fxml;
}