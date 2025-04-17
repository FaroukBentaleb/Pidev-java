package com.esprit.java.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import com.esprit.java.controllers.CompetitionViewController;
import com.esprit.java.Utility.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;
import com.esprit.java.Services.CompetitionService;

import org.kordamp.ikonli.javafx.FontIcon;

public class CompetitionsListController implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchBtn;
    
    @FXML
    private Button createBtn;
    
    @FXML
    private GridPane competitionsContainer;
    
    @FXML
    private Pagination pagination;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox mainContainer;
    
    private static final int ITEMS_PER_PAGE = 6; // Number of competitions per page (3 columns × 2 rows)
    
    private List<Competition> allCompetitions;
    private ObservableList<Competition> filteredCompetitions;
    private CompetitionService competitionService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the competition service with a database connection
            Connection connection = DatabaseConnection.getConnection();
            competitionService = new CompetitionService(connection);
            
            // Load competitions from database
            loadCompetitions();
            
            // Initialize the pagination
            setupPagination();
            
            // Add search listener for real-time filtering
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCompetitions(newValue);
            });
            
            // Setup listeners to handle window resizing
            competitionsContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                adjustGridLayout(newVal.doubleValue());
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to connect to the database", e.getMessage());
            e.printStackTrace();
        }
    }
    
    void loadCompetitions() {
        try {
            // Fetch all competitions from the database
            allCompetitions = competitionService.getAllCompetitions();
            
            // Initialize filtered list
            filteredCompetitions = FXCollections.observableArrayList(allCompetitions);
            
            // Update the pagination
            updatePagination();
            
            // Display first page
            displayCompetitionsPage(0);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to load competitions", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupPagination() {
        // Set page factory for pagination
        pagination.setPageFactory(this::displayCompetitionsPage);
        
        // Add change listener to handle page changes properly
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            // Force display update when the page changes
            displayCompetitionsPage(newIndex.intValue());
        });
    }
    
    private void updatePagination() {
        int totalItems = filteredCompetitions.size();
        int pageCount = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        
        // Ensure at least 1 page
        pagination.setPageCount(Math.max(1, pageCount));
        
        // Update the current page index if it's now out of bounds
        int currentPageIndex = pagination.getCurrentPageIndex();
        if (currentPageIndex >= pageCount && pageCount > 0) {
            pagination.setCurrentPageIndex(0);
        }
        
        // Update status label
        int displayedItems = Math.min(ITEMS_PER_PAGE, totalItems);
        String status;
        if (totalItems > 0) {
            status = String.format("Showing %d of %d competitions", displayedItems, totalItems);
        } else {
            status = "No competitions to display";
        }
        statusLabel.setText(status);
    }
    
    private Parent displayCompetitionsPage(int pageIndex) {
        // Clear existing content
        competitionsContainer.getChildren().clear();
        
        // Ensure the grid maintains its size
        competitionsContainer.setMinSize(1120, 600);
        competitionsContainer.setPrefSize(1120, 600);
        competitionsContainer.setMaxSize(1120, 600);
        
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredCompetitions.size());
        
        // If no competitions to display
        if (filteredCompetitions.isEmpty()) {
            Label noCompetitionsLabel = new Label("No competitions found");
            noCompetitionsLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px;");
            GridPane.setColumnSpan(noCompetitionsLabel, 3);
            GridPane.setRowSpan(noCompetitionsLabel, 2);
            GridPane.setHalignment(noCompetitionsLabel, HPos.CENTER);
            GridPane.setValignment(noCompetitionsLabel, VPos.CENTER);
            competitionsContainer.add(noCompetitionsLabel, 0, 0);
            return competitionsContainer;
        }
        
        // Check if the current page is valid
        int totalPages = (int) Math.ceil((double) filteredCompetitions.size() / ITEMS_PER_PAGE);
        if (pageIndex >= totalPages) {
            pageIndex = 0;
            pagination.setCurrentPageIndex(0);
            startIndex = 0;
            endIndex = Math.min(ITEMS_PER_PAGE, filteredCompetitions.size());
        }
        
        // Display competitions for the current page
        int row = 0;
        int col = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Competition competition = filteredCompetitions.get(i);
            
            try {
                // Create a competition card
                Parent competitionCard = createCompetitionCard(competition);
                
                // Set fixed size for the card
                competitionCard.setStyle("-fx-min-width: 350; -fx-pref-width: 350; -fx-max-width: 350; -fx-min-height: 280; -fx-pref-height: 280; -fx-max-height: 280;");
                
                // Add to grid pane at the correct position
                competitionsContainer.add(competitionCard, col, row);
                
                // Update grid position
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Fill remaining spaces with empty nodes to maintain grid size
        while (row < 2) {
            while (col < 3) {
                Region emptyNode = new Region();
                emptyNode.setStyle("-fx-min-width: 350; -fx-pref-width: 350; -fx-max-width: 350; -fx-min-height: 280; -fx-pref-height: 280; -fx-max-height: 280;");
                competitionsContainer.add(emptyNode, col, row);
                col++;
            }
            col = 0;
            row++;
        }
        
        // Update status label for current page
        int totalItems = filteredCompetitions.size();
        int firstItem = startIndex + 1;
        int lastItem = Math.min(endIndex, filteredCompetitions.size());
        String status;
        if (totalItems > 0) {
            status = String.format("Showing %d-%d of %d competitions", firstItem, lastItem, totalItems);
        } else {
            status = "No competitions to display";
        }
        
        statusLabel.setText(status);
        
        return competitionsContainer;
    }
    
    private Parent createCompetitionCard(Competition competition) throws IOException {
        // Load the competition card FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_card.fxml"));
        Parent card = loader.load();
        
        // Get components from the loaded FXML
        ImageView competitionImage = (ImageView) card.lookup("#competitionImage");
        Label titleLabel = (Label) card.lookup("#titleLabel");
        Label categoryLabel = (Label) card.lookup("#categoryLabel");
        Label statusLabel = (Label) card.lookup("#statusLabel");
        Label dateLabel = (Label) card.lookup("#dateLabel");
        Label durationLabel = (Label) card.lookup("#durationLabel");
        Button editBtn = (Button) card.lookup("#editBtn");
        Button deleteBtn = (Button) card.lookup("#deleteBtn");
        
        // Set card data
        titleLabel.setText(competition.getNom());
        categoryLabel.setText(competition.getCategorie());
        statusLabel.setText(competition.getEtat());
        
        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateLabel.setText(competition.getDateComp().format(formatter));
        
        // Set duration
        durationLabel.setText(competition.getDuration() + " minutes");
        
        // Add status-specific styling
        switch(competition.getEtat().toLowerCase()) {
            case "active":
                statusLabel.getStyleClass().add("competition-status-active");
                break;
            case "planned":
                statusLabel.getStyleClass().add("competition-status-planned");
                break;
            case "completed":
                statusLabel.getStyleClass().add("competition-status-completed");
                break;
            case "draft":
                statusLabel.getStyleClass().add("competition-status-draft");
                break;
        }
        
        // Set image if available
        if (competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
            try {
                Image image = new Image("file:" + competition.getImageUrl(), true);
                competitionImage.setImage(image);
            } catch (Exception e) {
                // Use default image or create a placeholder
                setDefaultOrPlaceholderImage(competitionImage);
            }
        } else {
            // Use default image or create a placeholder
            setDefaultOrPlaceholderImage(competitionImage);
        }
        
        // Add click handler to the card for viewing
        card.setOnMouseClicked(event -> handleViewCompetition(competition));
        
        // Add button event handlers
        editBtn.setOnAction(event -> handleEditCompetition(competition));
        deleteBtn.setOnAction(event -> handleDeleteCompetition(competition));
        
        return card;
    }
    
    /**
     * Sets a default image or creates a placeholder for competition cards
     * @param imageView The ImageView to set the image for
     */
    private void setDefaultOrPlaceholderImage(ImageView imageView) {
        try {
            // Try to load the default image first
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-competition.jpg"));
            if (defaultImage.isError()) {
                // If default image can't be loaded, create a placeholder
                createPlaceholderImage(imageView);
            } else {
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            // Create a placeholder if any error occurs
            createPlaceholderImage(imageView);
        }
    }
    
    /**
     * Creates a simple colored placeholder with text when no image is available
     * @param imageView The ImageView to set the placeholder for
     */
    private void createPlaceholderImage(ImageView imageView) {
        // Create a JavaFX WritableImage as a placeholder
        int width = 130;
        int height = 280;
        
        // Create the WritableImage
        javafx.scene.image.WritableImage placeholder = new javafx.scene.image.WritableImage(width, height);
        
        // Get the PixelWriter
        javafx.scene.image.PixelWriter writer = placeholder.getPixelWriter();
        
        // Create a blue-to-purple gradient background
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ratio = (double) y / height;
                int r = (int) (50 + ratio * 30);
                int g = (int) (80 + ratio * 20);
                int b = (int) (150 + ratio * 50);
                writer.setArgb(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
            }
        }
        
        // Set the placeholder image
        imageView.setImage(placeholder);
    }
    
    private void filterCompetitions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // If search is empty, show all
            filteredCompetitions.setAll(allCompetitions);
        } else {
            // Convert to lowercase for case-insensitive search
            String query = searchTerm.toLowerCase();
            
            // Filter competitions based on search term
            List<Competition> filtered = allCompetitions.stream()
                .filter(comp -> 
                    comp.getNom().toLowerCase().contains(query) ||
                    comp.getDescription().toLowerCase().contains(query) ||
                    comp.getCategorie().toLowerCase().contains(query) ||
                    comp.getEtat().toLowerCase().contains(query)
                )
                .collect(Collectors.toList());
            
            filteredCompetitions.setAll(filtered);
        }
        
        // Update pagination and display first page
        updatePagination();
        pagination.setCurrentPageIndex(0);
        displayCompetitionsPage(0);
    }
    
    @FXML
    void handleSearch(ActionEvent event) {
        filterCompetitions(searchField.getText());
    }
    
    @FXML
    private void handleCreateCompetition(ActionEvent event) {
        try {
            // Load competition creation form (Step 1)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step1.fxml"));
            Parent form = loader.load();
            
            // Get the current scene's root
            Node root = ((Node) event.getSource()).getScene().getRoot();
            if (root instanceof VBox) {
                VBox container = (VBox) root;
                // Clear the container and add the form
                container.getChildren().clear();
                container.getChildren().add(form);
                
                // Get the controller and set up the close handler
                CompetitionStep1Controller controller = loader.getController();
                controller.setOnCloseHandler(() -> {
                    // When form is closed, reload the competitions list
                    loadCompetitions();
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Layout Error", "Could not find the main container.");
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not load competition creation form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleViewCompetition(Competition competition) {
        try {
            // Load competition view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_view.fxml"));
            Parent view = loader.load();
            
            // Get the current scene's root
            Node root = competitionsContainer.getScene().getRoot();
            if (root instanceof VBox) {
                VBox container = (VBox) root;
                // Clear the container and add the view
                container.getChildren().clear();
                container.getChildren().add(view);
                

                CompetitionViewController controller = loader.getController();
                controller.setCompetition(competition);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Layout Error", "Could not find the main container.");
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not load competition view", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleEditCompetition(Competition competition) {
        try {
            // Load competition edit form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step1.fxml"));
            Parent form = loader.load();
            
            // Get the current scene's root
            Node root = competitionsContainer.getScene().getRoot();
            if (root instanceof VBox) {
                VBox container = (VBox) root;
                // Clear the container and add the form
                container.getChildren().clear();
                container.getChildren().add(form);
                
                // Get the controller and set the competition
                CompetitionStep1Controller controller = loader.getController();
                
                // Fetch challenges for this competition
                List<Challenge> challenges = competitionService.getChallengesForCompetition(competition.getId());
                
                // Set both competition and its challenges
                controller.setCompetition(competition, challenges);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Layout Error", "Could not find the main container.");
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not load competition form", e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not fetch challenges", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDeleteCompetition(Competition competition) {
        // Ask for confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Competition");
        confirmDialog.setHeaderText("Delete Competition: " + competition.getNom());
        confirmDialog.setContentText(
                "Are you sure you want to delete this competition and all its associated challenges? " +
                "This action cannot be undone.");
        
        // Add custom styling to the dialog
        DialogHelper.styleDialog(confirmDialog);
        
        // Show dialog and wait for response
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the competition
                competitionService.deleteCompetition(competition.getId());
                
                // Remove from the list and refresh the view
                allCompetitions.remove(competition);
                filterCompetitions(searchField.getText());
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Competition Deleted", "The competition has been successfully deleted.");
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to delete competition", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Add custom styling to the dialog
        DialogHelper.styleDialog(alert);
        
        alert.showAndWait();
    }
    
    // Helper class for dialog styling
    private static class DialogHelper {
        public static void styleDialog(Alert dialog) {
            dialog.getDialogPane().getStylesheets().add(
                    CompetitionsListController.class.getResource("/com/esprit/java/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }
    }
    
    public void setMainContainer(VBox container) {
        this.mainContainer = container;
    }
    
    private void adjustGridLayout(double width) {
        // Dynamically adjust the number of columns based on container width
        int numColumns = 3; // Default
        
        if (width < 800) {
            numColumns = 1;
        } else if (width < 1200) {
            numColumns = 2;
        }
        
        // Clear existing constraints
        competitionsContainer.getColumnConstraints().clear();
        
        // Add new constraints
        for (int i = 0; i < numColumns; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.SOMETIMES);
            column.setMinWidth(330);
            column.setPrefWidth(330);
            competitionsContainer.getColumnConstraints().add(column);
        }
        
        // Refresh the grid
        displayCompetitionsPage(pagination.getCurrentPageIndex());
    }
} 