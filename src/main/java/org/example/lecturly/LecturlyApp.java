package org.example.lecturly;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * LECTURLY - Lecture Intelligence Platform
 * 
 * A Netflix-style JavaFX application for:
 * - Chat with Gemini 2.5 Flash AI
 * - Audio-to-notes generation using FastAPI backend
 * - Notebook management with local storage
 */
public class LecturlyApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load Barlow Condensed fonts
        loadBarlowCondensedFonts();
        
        // Start with dashboard view
        FXMLLoader fxmlLoader = new FXMLLoader(LecturlyApp.class.getResource("dashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        // Set scene background to black
        scene.setFill(javafx.scene.paint.Color.BLACK);
        
        // Apply Netflix theme styles (if CSS file exists)
        try {
            var cssUrl = getClass().getResource("netflix-theme.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        
        stage.setTitle("LECTURLY - Lecture Intelligence");
        stage.setScene(scene);
        stage.show();
    }

    private void loadBarlowCondensedFonts() {
        try {
            // Try multiple possible paths for the font directory
            String[] possiblePaths = {
                "Barlow_Condensed",
                "../Barlow_Condensed",
                System.getProperty("user.dir") + "/Barlow_Condensed"
            };
            
            File fontDir = null;
            for (String path : possiblePaths) {
                File testDir = new File(path);
                if (testDir.exists() && testDir.isDirectory()) {
                    fontDir = testDir;
                    break;
                }
            }
            
            if (fontDir != null) {
                File[] fontFiles = fontDir.listFiles((dir, name) -> name.endsWith(".ttf"));
                if (fontFiles != null) {
                    for (File fontFile : fontFiles) {
                        try {
                            Font.loadFont(fontFile.toURI().toURL().toExternalForm(), 12);
                            System.out.println("Loaded font: " + fontFile.getName());
                        } catch (Exception e) {
                            System.err.println("Failed to load font " + fontFile.getName() + ": " + e.getMessage());
                        }
                    }
                }
            } else {
                System.err.println("Barlow_Condensed directory not found. Using system fonts.");
            }
        } catch (Exception e) {
            System.err.println("Error loading fonts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
