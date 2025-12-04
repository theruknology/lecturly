package org.example.lecturly;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Controller for the notebook view with side-by-side notes and chat
 */
public class NotebookController {
    @FXML private Label notebookNameLabel;
    @FXML private Button backButton;
    @FXML private Button saveButton;
    
    // Upload & Notes Panel
    @FXML private TextField apiKeyField;
    @FXML private Button connectButton;
    @FXML private Label statusLabel;
    @FXML private Button browseFileButton;
    @FXML private Label fileNameLabel;
    @FXML private Button uploadButton;
    @FXML private VBox loadingBox;
    @FXML private Label loadingLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextArea notesArea;
    
    // Chat Panel
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextArea inputArea;
    @FXML private Button sendButton;
    @FXML private Button clearButton;

    private Notebook notebook;
    private NotebookStorageService storageService;
    private AudioProcessingService audioService;
    private GeminiChatService chatService;
    private ScheduledExecutorService executorService;
    private File selectedFile;
    private boolean isConnected = false;

    @FXML
    public void initialize() {
        storageService = new NotebookStorageService();
        audioService = new AudioProcessingService();
        executorService = Executors.newScheduledThreadPool(2);
        
        inputArea.setWrapText(true);
        notesArea.setWrapText(true);
        uploadButton.setDisable(true);
        sendButton.setDisable(true);
        clearButton.setDisable(true);
        
        // Enable chat when API key is entered
        apiKeyField.textProperty().addListener((obs, oldVal, newVal) -> {
            String apiKey = newVal != null ? newVal.trim() : "";
            if (!apiKey.isEmpty()) {
                // Enable chat buttons if API key is present
                sendButton.setDisable(false);
                clearButton.setDisable(false);
                
                // Update notes context if chat service exists
                if (chatService != null) {
                    String notes = notesArea.getText().trim();
                    chatService.setNotesContext(notes);
                }
            } else {
                // Disable if API key is cleared
                sendButton.setDisable(true);
                clearButton.setDisable(true);
            }
        });
        
        // Update notes context when notes change
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (chatService != null) {
                String notes = newVal != null ? newVal.trim() : "";
                chatService.setNotesContext(notes);
            }
        });
        
        // Auto-scroll chat to bottom
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
        notebookNameLabel.setText(notebook.getName());
        notesArea.setText(notebook.getNotes());
        
        // Load chat history
        messagesContainer.getChildren().clear();
        for (Notebook.ChatMessage msg : notebook.getChatHistory()) {
            addMessageToChat(msg.getRole().equals("user"), msg.getContent());
        }
        
        if (notebook.getChatHistory().isEmpty()) {
            addSystemMessage("Chat with your notes! Ask questions about the content.");
        }
        
        // If API key is available, enable chat buttons
        String apiKey = apiKeyField.getText().trim();
        if (!apiKey.isEmpty()) {
            sendButton.setDisable(false);
            clearButton.setDisable(false);
            
            // If chat history exists, restore it
            if (!notebook.getChatHistory().isEmpty()) {
                try {
                    chatService = new GeminiChatService(apiKey);
                    // Set notes context
                    String notes = notesArea.getText().trim();
                    if (!notes.isEmpty()) {
                        chatService.setNotesContext(notes);
                    }
                    // Restore chat history (without notes prefix, as it's in system instruction)
                    chatService.restoreHistory(notebook.getChatHistory(), null);
                } catch (Exception e) {
                    System.err.println("Failed to restore chat history: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    protected void onBackToDashboard() {
        try {
            saveNotebook();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            scene.setFill(javafx.scene.paint.Color.BLACK);
            stage.setScene(scene);
            stage.setTitle("LECTURLY - Notebooks");
        } catch (IOException e) {
            showError("Failed to return to dashboard: " + e.getMessage());
        }
    }

    @FXML
    protected void onSave() {
        saveNotebook();
        showInfo("Notebook saved successfully!");
    }

    private void saveNotebook() {
        if (notebook != null) {
            notebook.setNotes(notesArea.getText());
            try {
                storageService.saveNotebook(notebook);
            } catch (IOException e) {
                showError("Failed to save notebook: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onConnect() {
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
        statusLabel.setStyle("-fx-text-fill: #3fb950; -fx-font-family: 'Barlow Condensed';");
    }

    @FXML
    protected void onBrowseFile() {
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

    @FXML
    protected void onGenerateNotes() {
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
        loadingLabel.setText("Processing audio file...");

        executorService.execute(() -> {
            try {
                String notes = audioService.generateNotesFromAudio(selectedFile);
                Platform.runLater(() -> {
                    notesArea.setText(notes);
                    notebook.setNotes(notes);
                    saveNotebook();
                    loadingBox.setVisible(false);
                    uploadButton.setDisable(false);
                    browseFileButton.setDisable(false);
                    statusLabel.setText("✓ Notes generated successfully");
                    statusLabel.setStyle("-fx-text-fill: #3fb950; -fx-font-family: 'Barlow Condensed';");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error generating notes: " + e.getMessage());
                    loadingBox.setVisible(false);
                    uploadButton.setDisable(false);
                    browseFileButton.setDisable(false);
                    statusLabel.setText("✗ Error occurred");
                    statusLabel.setStyle("-fx-text-fill: #f85149; -fx-font-family: 'Barlow Condensed';");
                });
            }
        });
    }

    @FXML
    protected void onSendMessage() {
        String apiKey = apiKeyField.getText().trim();
        if (apiKey.isEmpty()) {
            showError("Please enter your Gemini API key first.");
            return;
        }

        if (chatService == null) {
            try {
                chatService = new GeminiChatService(apiKey);
                // Set notes context if available
                String notesContext = notesArea.getText().trim();
                if (!notesContext.isEmpty()) {
                    chatService.setNotesContext(notesContext);
                }
                sendButton.setDisable(false);
                clearButton.setDisable(false);
            } catch (Exception e) {
                showError("Failed to initialize chat: " + e.getMessage());
                return;
            }
        } else {
            // Update notes context if it changed
            String notesContext = notesArea.getText().trim();
            chatService.setNotesContext(notesContext);
        }

        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        inputArea.clear();
        sendButton.setDisable(true);
        addMessageToChat(true, message);
        
        // Use the message as-is, notes context is handled by the service
        String fullMessage = message;

        executorService.execute(() -> {
            try {
                String response = chatService.chat(fullMessage);
                Platform.runLater(() -> {
                    addMessageToChat(false, response);
                    
                    // Save chat message to notebook (save original user message, not the one with notes context)
                    notebook.addChatMessage(new Notebook.ChatMessage("user", message));
                    notebook.addChatMessage(new Notebook.ChatMessage("assistant", response));
                    saveNotebook();
                    
                    sendButton.setDisable(false);
                    inputArea.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error: " + e.getMessage());
                    sendButton.setDisable(false);
                    inputArea.requestFocus();
                });
            }
        });
    }

    @FXML
    protected void onClearChat() {
        if (chatService != null) {
            chatService.clearHistory();
            messagesContainer.getChildren().clear();
            notebook.getChatHistory().clear();
            saveNotebook();
            addSystemMessage("Conversation cleared. Starting fresh!");
        }
    }

    private void addMessageToChat(boolean isUser, String text) {
        HBox messageBox = createMessageBubble(isUser, text);
        messagesContainer.getChildren().add(messageBox);
    }

    private void addSystemMessage(String text) {
        HBox messageBox = createMessageBubble(null, text);
        messagesContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBubble(Boolean isUser, String text) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(8, 16, 8, 16));
        hbox.setSpacing(8);

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.LEFT);
        messageLabel.setStyle("-fx-font-size: 13; -fx-font-family: 'Barlow Condensed';");

        VBox messageBubble = new VBox(messageLabel);
        messageBubble.setPadding(new Insets(12, 16, 12, 16));
        messageBubble.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");

        if (isUser != null) {
            if (isUser) {
                // User message - right aligned, red background
                messageBubble.setStyle("-fx-background-color: #e50914; -fx-border-radius: 12; -fx-background-radius: 12;");
                messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #ffffff;");
                hbox.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(messageBubble, Priority.NEVER);
                messageBubble.setMaxWidth(500);
            } else {
                // Assistant message - left aligned, dark background
                messageBubble.setStyle("-fx-background-color: #141414; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #333333; -fx-border-width: 1;");
                messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #e6e6e6;");
                hbox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(messageBubble, Priority.NEVER);
                messageBubble.setMaxWidth(500);
            }
        } else {
            // System message - centered, subtle color
            messageBubble.setStyle("-fx-background-color: #141414; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #333333; -fx-border-width: 1;");
            messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #808080; -fx-font-style: italic;");
            hbox.setAlignment(Pos.CENTER);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
        }

        hbox.getChildren().add(messageBubble);
        return hbox;
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

