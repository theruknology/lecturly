package org.example.lecturly;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NotesController {
    private TextField apiKeyField;
    private Button connectButton;
    private Label statusLabel;
    private Button browseFileButton;
    private Label fileNameLabel;
    private Button uploadButton;
    private VBox loadingBox;
    private Label loadingLabel;
    private ProgressIndicator progressIndicator;
    private TextArea outputNotesArea;
    private Button copyNotesButton;

    private AudioProcessingService audioService;
    private ScheduledExecutorService executorService;
    private File selectedFile;
    private boolean isConnected = false;

    public NotesController(TextField apiKeyField, Button connectButton, Label statusLabel,
                          Button browseFileButton, Label fileNameLabel, Button uploadButton,
                          VBox loadingBox, Label loadingLabel, ProgressIndicator progressIndicator,
                          TextArea outputNotesArea, Button copyNotesButton) {
        this.apiKeyField = apiKeyField;
        this.connectButton = connectButton;
        this.statusLabel = statusLabel;
        this.browseFileButton = browseFileButton;
        this.fileNameLabel = fileNameLabel;
        this.uploadButton = uploadButton;
        this.loadingBox = loadingBox;
        this.loadingLabel = loadingLabel;
        this.progressIndicator = progressIndicator;
        this.outputNotesArea = outputNotesArea;
        this.copyNotesButton = copyNotesButton;

        this.executorService = Executors.newScheduledThreadPool(1);
        this.audioService = new AudioProcessingService();
        this.uploadButton.setDisable(true);
    }

    public void onConnectClick() {
        // Check if FastAPI backend is available
        if (!audioService.isBackendAvailable()) {
            showError("FastAPI backend not available at http://localhost:8000\n\n" +
                    "Make sure to start the audio backend:\n" +
                    "1. cd audio_backend\n" +
                    "2. set GEMINI_API_KEY=your-api-key\n" +
                    "3. python app.py");
            return;
        }

        isConnected = true;
        connectButton.setDisable(true);
        apiKeyField.setDisable(true);
        browseFileButton.setDisable(false);
        statusLabel.setText("✓ Connected to FastAPI Backend");
        statusLabel.setStyle("-fx-text-fill: #3fb950;");
    }

    public void onBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Audio File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.ogg", "*.flac", "*.m4a"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
            uploadButton.setDisable(false);
        }
    }

    public void onGenerateNotes() {
        if (!isConnected) {
            showError("Not connected. Please connect first.");
            return;
        }

        if (selectedFile == null) {
            showError("Please select an audio file first.");
            return;
        }

        loadingBox.setVisible(true);
        uploadButton.setDisable(true);
        browseFileButton.setDisable(true);
        outputNotesArea.setText("");
        loadingLabel.setText("Processing audio file with FastAPI backend...");

        executorService.execute(() -> {
            try {
                String notes = audioService.generateNotesFromAudio(selectedFile);
                Platform.runLater(() -> {
                    outputNotesArea.setText(notes);
                    loadingBox.setVisible(false);
                    uploadButton.setDisable(false);
                    browseFileButton.setDisable(false);
                    statusLabel.setText("✓ Notes generated successfully");
                    statusLabel.setStyle("-fx-text-fill: #3fb950;");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error generating notes: " + e.getMessage());
                    loadingBox.setVisible(false);
                    uploadButton.setDisable(false);
                    browseFileButton.setDisable(false);
                    statusLabel.setText("✗ Error occurred");
                    statusLabel.setStyle("-fx-text-fill: #f85149;");
                });
            }
        });
    }

    public void onCopyNotes() {
        String notes = outputNotesArea.getText();
        if (!notes.isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(notes);
            clipboard.setContent(content);
            showInfo("Notes copied to clipboard!");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
