module com.example.mochamp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens com.example.mochamp to javafx.fxml;
    exports com.example.mochamp;
    exports com.example.mochamp.controllers;
    opens com.example.mochamp.controllers to javafx.fxml;
}