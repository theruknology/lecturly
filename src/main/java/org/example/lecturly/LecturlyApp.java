package org.example.lecturly;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * LECTURLY - Lecture Intelligence Platform
 * 
 * A Netflix-style JavaFX application for:
 * - Chat with Gemini 2.5 Flash AI
 * - Audio-to-notes generation using FastAPI backend
 */
public class LecturlyApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LecturlyApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        stage.setTitle("LECTURLY - Lecture Intelligence");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
