package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;
import org.kordamp.ikonli.javafx.FontIcon;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.materialfx.controls.MFXButton;

public class CompetitionsListController implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private Button createBtn;
    
    @FXML
    private VBox competitionsContainer;
    
    @FXML
    private Pagination pagination;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private MFXIconWrapper infoIcon;
    
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
            
            // Hide info icon since it's causing issues
            if (infoIcon != null) {
                infoIcon.setVisible(false);
                infoIcon.setManaged(false);
            }
            
            // Hide create button for students
            String userRole = Session.getCurrentUser().getRole();
            if ("Student".equals(userRole)) {
                createBtn.setVisible(false);
                createBtn.setManaged(false);
            }
            
            // Load competitions from database
            loadCompetitions();
            
            // Initialize the pagination
            setupPagination();
            
            // Add search listener for real-time filtering
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCompetitions(newValue);
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to connect to the database", e.getMessage());
            e.printStackTrace();
        }
    }
    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Profile.fxml");
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
        
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredCompetitions.size());
        
        // If no competitions to display
        if (filteredCompetitions.isEmpty()) {
            Label noCompetitionsLabel = new Label("No competitions found");
            noCompetitionsLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px;");
            competitionsContainer.getChildren().add(noCompetitionsLabel);
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
        for (int i = startIndex; i < endIndex; i++) {
            Competition competition = filteredCompetitions.get(i);
            
            try {
                // Create a competition card
                Parent competitionCard = createCompetitionCard(competition);
                
                // Add card to the container
                competitionsContainer.getChildren().add(competitionCard);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_card.fxml"));
        Parent card = loader.load();
        
        // Get components from the loaded FXML
        ImageView competitionImage = (ImageView) card.lookup("#competitionImage");
        Label titleLabel = (Label) card.lookup("#titleLabel");
        Label categoryLabel = (Label) card.lookup("#categoryLabel");
        Label statusLabel = (Label) card.lookup("#statusLabel");
        Label dateLabel = (Label) card.lookup("#dateLabel");
        Label endDateLabel = (Label) card.lookup("#endDateLabel");
        Label durationLabel = (Label) card.lookup("#durationLabel");
        Label descriptionPreviewLabel = (Label) card.lookup("#descriptionPreviewLabel");
        Label participantsLabel = (Label) card.lookup("#participantsLabel");
        MFXButton editBtn = (MFXButton) card.lookup("#editBtn");
        MFXButton deleteBtn = (MFXButton) card.lookup("#deleteBtn");
        
        // Set card data
        titleLabel.setText(competition.getNom());
        categoryLabel.setText(competition.getCategorie());
        statusLabel.setText(competition.getEtat());
        
        // Format date with time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateLabel.setText(competition.getDateComp().format(formatter));
        
        // Calculate and set end date based on duration
        endDateLabel.setText(competition.getDateComp().plusMinutes(competition.getDuration()).format(formatter));
        
        // Set duration
        durationLabel.setText(competition.getDuration() + " minutes");
        
        // Set description preview (truncated if needed)
        String description = competition.getDescription();
        if (description != null && !description.isEmpty()) {
            // Strip HTML tags for preview
            description = description.replaceAll("<[^>]*>", "");
            // Truncate if too long
            if (description.length() > 150) {
                description = description.substring(0, 147) + "...";
            }
            descriptionPreviewLabel.setText(description);
        } else {
            descriptionPreviewLabel.setText("No description available");
        }
        
        // Set participants count (simulated for now)
        // In a real app, you would fetch this from the database
        int participantsCount = (int) (Math.random() * 50); // Random number for demo
        participantsLabel.setText(participantsCount + " registered");
        
        // Add status-specific styling
        switch(competition.getEtat().toLowerCase()) {
            case "active":
                statusLabel.getStyleClass().add("competition-status-active");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #ebf8ff; -fx-text-fill: #2b6cb0;");
                break;
            case "planned":
                statusLabel.getStyleClass().add("competition-status-planned");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e6fffa; -fx-text-fill: #2c7a7b;");
                break;
            case "completed":
                statusLabel.getStyleClass().add("competition-status-completed");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f0fff4; -fx-text-fill: #276749;");
                break;
            case "draft":
                statusLabel.getStyleClass().add("competition-status-draft");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f7fafc; -fx-text-fill: #4a5568;");
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
        
        // Hide edit/delete buttons based on user role and ownership
        String userRole = Session.getCurrentUser().getRole();
        if ("Student".equals(userRole)) {
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        } else if ("Instructor".equals(userRole)) {
            // For instructors, only show buttons if they own the competition
            int instructorId = competition.getInstructorId();
            int userId = Session.getCurrentUser().getId();
            boolean isOwner = instructorId == userId;
            
            editBtn.setVisible(isOwner);
            editBtn.setManaged(isOwner);
            deleteBtn.setVisible(isOwner);
            deleteBtn.setManaged(isOwner);
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
        int width = 220;  // Updated width for horizontal layout
        int height = 180; // Updated height for horizontal layout
        
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_step1.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Create New Competition");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Close the current window
            ((Node) event.getSource()).getScene().getWindow().hide();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to open competition creation form", e.getMessage());
        }
    }
    
    private void handleViewCompetition(Competition competition) {
        try {
            // Load competition view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_view.fxml"));
            Parent view = loader.load();
            
            // Get the controller and set competition data
            CompetitionViewController controller = loader.getController();
            controller.setCompetition(competition);
            
            // Get the stage and set the new scene
            Stage stage = (Stage) competitionsContainer.getScene().getWindow();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not load competition view", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleEditCompetition(Competition competition) {
        try {
            // Load competition edit form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_step1.fxml"));
            Parent form = loader.load();
            
            // Get the controller and set the competition
            CompetitionStep1Controller controller = loader.getController();
            
            // Fetch challenges for this competition
            List<Challenge> challenges = competitionService.getChallengesForCompetition(competition.getId());
            
            // Set both competition and its challenges
            controller.setCompetition(competition, challenges);
            
            // Get the stage and set the new scene
            Stage stage = (Stage) competitionsContainer.getScene().getWindow();
            Scene scene = new Scene(form);
            stage.setScene(scene);
            stage.show();
            
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
                    CompetitionsListController.class.getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }
    }
} 