module org.example.lecturly {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.google.gson;
    requires java.net.http;

    opens org.example.lecturly to javafx.fxml;
    exports org.example.lecturly;
}