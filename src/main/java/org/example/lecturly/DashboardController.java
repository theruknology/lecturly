package org.example.lecturly;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the dashboard view showing all notebooks
 */
public class DashboardController {
    @FXML
    private Button createNotebookButton;
    @FXML
    private VBox notebooksContainer;
    @FXML
    private ScrollPane notebooksScrollPane;

    private NotebookStorageService storageService;

    @FXML
    public void initialize() {
        storageService = new NotebookStorageService();
        // Initialize dummy notebooks if none exist
        storageService.initializeDummyNotebooks();
        loadNotebooks();
    }

    @FXML
    protected void onCreateNotebook() {
        TextInputDialog dialog = new TextInputDialog("Untitled Notebook");
        dialog.setTitle("Create Notebook");
        dialog.setHeaderText("Enter notebook name:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name != null && !name.trim().isEmpty()) {
                Notebook notebook = new Notebook(name.trim());
                try {
                    storageService.saveNotebook(notebook);
                    loadNotebooks();
                } catch (IOException e) {
                    showError("Failed to create notebook: " + e.getMessage());
                }
            }
        });
    }

    protected void loadNotebooks() {
        notebooksContainer.getChildren().clear();
        
        try {
            List<Notebook> notebooks = storageService.loadAllNotebooks();
            System.out.println("Loaded " + notebooks.size() + " notebooks");
            
            if (notebooks.isEmpty()) {
                Label emptyLabel = new Label("No notebooks yet. Create one to get started!");
                emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #808080; -fx-font-family: 'Barlow Condensed';");
                emptyLabel.setPadding(new Insets(40));
                notebooksContainer.getChildren().add(emptyLabel);
            } else {
                for (Notebook notebook : notebooks) {
                    System.out.println("Adding notebook card: " + notebook.getName());
                    notebooksContainer.getChildren().add(createNotebookCard(notebook));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load notebooks: " + e.getMessage());
        }
    }

    private HBox createNotebookCard(Notebook notebook) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: #141414; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        card.setPadding(new Insets(20));
        card.setSpacing(16);
        HBox.setHgrow(card, Priority.ALWAYS);
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #404040; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #141414; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));

        VBox content = new VBox();
        content.setSpacing(8);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label nameLabel = new Label(notebook.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Barlow Condensed';");

        Label dateLabel = new Label("Updated: " + notebook.getFormattedUpdatedAt());
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #808080; -fx-font-family: 'Barlow Condensed';");

        content.getChildren().addAll(nameLabel, dateLabel);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-padding: 8 16 8 16; -fx-font-size: 12; -fx-text-fill: #ffffff; -fx-background-color: #e50914; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand; -fx-font-family: 'Barlow Condensed';");
        deleteButton.setOnAction(e -> {
            e.consume();
            deleteNotebook(notebook);
        });

        card.getChildren().addAll(content, deleteButton);

        // Make the entire card clickable (except delete button)
        card.setOnMouseClicked(e -> {
            // Only open if clicking on the card itself, not on the delete button
            if (e.getTarget() != deleteButton && e.getTarget() != deleteButton.getGraphic()) {
                openNotebook(notebook);
            }
        });

        return card;
    }

    private void openNotebook(Notebook notebook) {
        try {
            System.out.println("Opening notebook: " + notebook.getName());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notebook-view.fxml"));
            Parent root = loader.load();
            NotebookController controller = loader.getController();
            if (controller != null) {
                controller.setNotebook(notebook);
            } else {
                System.err.println("Controller is null!");
            }

            Stage stage = (Stage) createNotebookButton.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.setFill(javafx.scene.paint.Color.BLACK);
            stage.setScene(scene);
            stage.setTitle("LECTURLY - " + notebook.getName());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open notebook: " + e.getMessage());
        }
    }

    private void deleteNotebook(Notebook notebook) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Notebook");
        confirmDialog.setHeaderText("Delete \"" + notebook.getName() + "\"?");
        confirmDialog.setContentText("This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    storageService.deleteNotebook(notebook.getId());
                    loadNotebooks();
                } catch (IOException e) {
                    showError("Failed to delete notebook: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

