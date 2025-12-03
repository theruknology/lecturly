package org.example.lecturly;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML
    private StackPane contentStackPane;
    @FXML
    private VBox chatView;
    @FXML
    private VBox notesView;
    @FXML
    private Button chatNavButton;
    @FXML
    private Button notesNavButton;

    // Chat View Components
    @FXML
    private TextField apiKeyField;
    @FXML
    private Button connectButton;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea inputArea;
    @FXML
    private Button sendButton;
    @FXML
    private Button clearButton;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane messagesScrollPane;

    // Notes View Components
    @FXML
    private TextField notesApiKeyField;
    @FXML
    private Button notesConnectButton;
    @FXML
    private Label notesStatusLabel;
    @FXML
    private Button browseFileButton;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Button uploadButton;
    @FXML
    private VBox loadingBox;
    @FXML
    private Label loadingLabel;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TextArea outputNotesArea;
    @FXML
    private Button copyNotesButton;

    private ChatController chatController;
    private NotesController notesController;

    @FXML
    public void initialize() {
        // Initialize sub-controllers
        chatController = new ChatController(
                apiKeyField, connectButton, statusLabel, inputArea,
                sendButton, clearButton, messagesContainer, messagesScrollPane
        );

        notesController = new NotesController(
                notesApiKeyField, notesConnectButton, notesStatusLabel,
                browseFileButton, fileNameLabel, uploadButton, loadingBox,
                loadingLabel, progressIndicator, outputNotesArea, copyNotesButton
        );

        // Show chat view by default
        showChatView();
    }

    @FXML
    protected void onChatNavClick() {
        showChatView();
    }

    @FXML
    protected void onNotesNavClick() {
        showNotesView();
    }

    private void showChatView() {
        chatView.setVisible(true);
        notesView.setVisible(false);
        chatNavButton.setStyle("-fx-padding: 12 16 12 16; -fx-font-size: 14; -fx-text-fill: #ffffff; -fx-background-color: #e50914; -fx-font-weight: bold; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand;");
        notesNavButton.setStyle("-fx-padding: 12 16 12 16; -fx-font-size: 14; -fx-text-fill: #cccccc; -fx-background-color: transparent; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
    }

    private void showNotesView() {
        chatView.setVisible(false);
        notesView.setVisible(true);
        chatNavButton.setStyle("-fx-padding: 12 16 12 16; -fx-font-size: 14; -fx-text-fill: #cccccc; -fx-background-color: transparent; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        notesNavButton.setStyle("-fx-padding: 12 16 12 16; -fx-font-size: 14; -fx-text-fill: #ffffff; -fx-background-color: #e50914; -fx-font-weight: bold; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand;");
    }

    @FXML
    protected void onConnectClick() {
        chatController.onConnectClick();
    }

    @FXML
    protected void onSendClick() {
        chatController.onSendClick();
    }

    @FXML
    protected void onClearClick() {
        chatController.onClearClick();
    }

    @FXML
    protected void onNotesConnectClick() {
        notesController.onConnectClick();
    }

    @FXML
    protected void onBrowseFile() {
        notesController.onBrowseFile();
    }

    @FXML
    protected void onGenerateNotes() {
        notesController.onGenerateNotes();
    }

    @FXML
    protected void onCopyNotes() {
        notesController.onCopyNotes();
    }
}
