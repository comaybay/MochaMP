module com.example.mochamp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mochamp to javafx.fxml;
    exports com.example.mochamp;
}