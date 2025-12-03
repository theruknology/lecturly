package org.example.lecturly;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChatController {
    private TextField apiKeyField;
    private Button connectButton;
    private Label statusLabel;
    private TextArea inputArea;
    private Button sendButton;
    private Button clearButton;
    private VBox messagesContainer;
    private ScrollPane messagesScrollPane;

    private GeminiChatService chatService;
    private ScheduledExecutorService executorService;
    private boolean isConnected = false;

    public ChatController(TextField apiKeyField, Button connectButton, Label statusLabel,
                         TextArea inputArea, Button sendButton, Button clearButton,
                         VBox messagesContainer, ScrollPane messagesScrollPane) {
        this.apiKeyField = apiKeyField;
        this.connectButton = connectButton;
        this.statusLabel = statusLabel;
        this.inputArea = inputArea;
        this.sendButton = sendButton;
        this.clearButton = clearButton;
        this.messagesContainer = messagesContainer;
        this.messagesScrollPane = messagesScrollPane;
        executorService = Executors.newScheduledThreadPool(1);
        inputArea.setWrapText(true);

        // Auto-scroll to bottom
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        sendButton.setDisable(true);
        clearButton.setDisable(true);
        apiKeyField.setPromptText("Enter your Gemini API key");
    }

    public void onConnectClick() {
        String apiKey = apiKeyField.getText().trim();
        if (apiKey.isEmpty()) {
            showError("Please enter your Gemini API key");
            return;
        }

        try {
            chatService = new GeminiChatService(apiKey);
            isConnected = true;
            sendButton.setDisable(false);
            clearButton.setDisable(false);
            connectButton.setDisable(true);
            apiKeyField.setDisable(true);
            statusLabel.setText("✓ Connected to Gemini 2.5 Flash");
            statusLabel.setStyle("-fx-text-fill: #3fb950;");
            addSystemMessage("Connected! Ready to chat with Gemini 2.5 Flash.");
        } catch (Exception e) {
            showError("Failed to connect: " + e.getMessage());
        }
    }

    protected void onSendClick() {
        if (!isConnected) {
            showError("Not connected. Please connect first.");
            return;
        }

        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        inputArea.clear();
        sendButton.setDisable(true);
        addUserMessage(message);
        statusLabel.setText("Waiting for response...");
        statusLabel.setStyle("-fx-text-fill: #d29922;");

        // Send message in background thread
        executorService.execute(() -> {
            try {
                String response = chatService.chat(message);
                Platform.runLater(() -> {
                    addAssistantMessage(response);
                    statusLabel.setText("✓ Connected to Gemini 2.5 Flash");
                    statusLabel.setStyle("-fx-text-fill: #3fb950;");
                    sendButton.setDisable(false);
                    inputArea.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error: " + e.getMessage());
                    statusLabel.setText("✗ Error occurred");
                    statusLabel.setStyle("-fx-text-fill: #f85149;");
                    sendButton.setDisable(false);
                    inputArea.requestFocus();
                });
            }
        });
    }

    protected void onClearClick() {
        if (chatService != null) {
            chatService.clearHistory();
            messagesContainer.getChildren().clear();
            addSystemMessage("Conversation cleared. Starting fresh!");
        }
    }

    private void addUserMessage(String text) {
        HBox messageBox = createMessageBubble(text, true);
        messagesContainer.getChildren().add(messageBox);
    }

    private void addAssistantMessage(String text) {
        HBox messageBox = createMessageBubble(text, false);
        messagesContainer.getChildren().add(messageBox);
    }

    private void addSystemMessage(String text) {
        HBox messageBox = createMessageBubble(text, null);
        messagesContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBubble(String text, Boolean isUser) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(8, 16, 8, 16));
        hbox.setSpacing(8);

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.LEFT);
        messageLabel.setStyle("-fx-font-size: 13; -fx-font-family: 'Segoe UI';");

        VBox messageBubble = new VBox(messageLabel);
        messageBubble.setPadding(new Insets(12, 16, 12, 16));
        messageBubble.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");

        if (isUser != null) {
            if (isUser) {
                // User message - right aligned, blue background
                messageBubble.setStyle("-fx-background-color: #1f6feb; -fx-border-radius: 12; -fx-background-radius: 12;");
                messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #ffffff;");
                hbox.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(messageBubble, Priority.NEVER);
                messageBubble.setMaxWidth(500);
            } else {
                // Assistant message - left aligned, gray background
                messageBubble.setStyle("-fx-background-color: #21262d; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #30363d; -fx-border-width: 1;");
                messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #e6edf3;");
                hbox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(messageBubble, Priority.NEVER);
                messageBubble.setMaxWidth(500);
            }
        } else {
            // System message - centered, subtle color
            messageBubble.setStyle("-fx-background-color: #21262d; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #30363d; -fx-border-width: 1;");
            messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #8b949e; -fx-font-style: italic;");
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
}
