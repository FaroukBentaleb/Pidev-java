package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.services.LogsService;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.tools.Navigator;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;
import tn.learniverse.tools.Session;
import org.kordamp.ikonli.javafx.FontIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;

public class CompetitionsListController implements Initializable {

    public Label usernameLabel;
    public Label role;
    public Button Settingsbtn;
    public Button logoutButton;
    @FXML private TextField searchField;
    @FXML private MFXButton createBtn;
    @FXML private VBox competitionsContainer;
    @FXML private HBox paginationContainer;
    @FXML private MFXButton prevButton;
    @FXML private MFXButton nextButton;
    @FXML private Label statusLabel;
    @FXML private MFXIconWrapper infoIcon;
    @FXML private MFXButton filterAllBtn;
    @FXML private MFXButton filterPlannedBtn;
    @FXML private MFXButton filterInProgressBtn;
    @FXML private MFXButton filterCompletedBtn;
    @FXML private MFXButton sortEarliestBtn;
    @FXML private MFXButton sortLatestBtn;
    public ImageView UserPicture;


    private static final int ITEMS_PER_PAGE = 6;
    private static final int MAX_VISIBLE_PAGES = 7;

    private List<Competition> allCompetitions;
    private ObservableList<Competition> filteredCompetitions;
    private CompetitionService competitionService;
    private int currentPage = 0;
    private String currentStatusFilter = "All"; // Track current status filter
    private String currentSortOrder = null; // Track current sort order ("earliest" or "latest")

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSessionMonitor();
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/settings.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Settingsbtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            if (Session.getCurrentUser() == null) {
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event, "/fxml/user/Login.fxml");
            } else {
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if (image.isError()) {
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // Initialize the competition service with a database connection
            Connection connection = DatabaseConnection.getConnection();
            competitionService = new CompetitionService(connection);

            // Initialize info icon

            // Hide create button for students
            String userRole = Session.getCurrentUser().getRole();
            if ("Student".equals(userRole)) {
                createBtn.setVisible(false);
                createBtn.setManaged(false);
            }

            // Initialize filter button styles
            updateFilterButtonStyles();

            // Load competitions from database
            loadCompetitions();

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
    

    void loadCompetitions() {
        try {
            // Fetch all competitions from the database
            allCompetitions = competitionService.getAllCompetitions();

            // Initialize filtered list
            filteredCompetitions = FXCollections.observableArrayList(allCompetitions);

            // Update pagination
            updatePagination();

            // Display first page
            displayCompetitionsPage(0);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load competitions", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int totalItems = filteredCompetitions.size();
        int pageCount = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        // Clear existing pagination buttons (except prev/next)
        paginationContainer.getChildren().retainAll(prevButton, nextButton);

        // Update button states
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= pageCount - 1);

        // Calculate visible page range
        int halfVisible = MAX_VISIBLE_PAGES / 2;
        int startPage = Math.max(0, currentPage - halfVisible);
        int endPage = Math.min(pageCount, startPage + MAX_VISIBLE_PAGES);

        // Adjust startPage if we're showing fewer than MAX_VISIBLE_PAGES
        if (endPage - startPage < MAX_VISIBLE_PAGES) {
            startPage = Math.max(0, endPage - MAX_VISIBLE_PAGES);
        }

        // Add first page and ellipsis if needed
        if (startPage > 0) {
            addPageButton(0);
            if (startPage > 1) {
                addEllipsis();
            }
        }

        // Add page number buttons
        for (int i = startPage; i < endPage; i++) {
            addPageButton(i);
        }

        // Add last page and ellipsis if needed
        if (endPage < pageCount) {
            if (endPage < pageCount - 1) {
                addEllipsis();
            }
            addPageButton(pageCount - 1);
        }

        // Update status label
        int displayedItems = Math.min(ITEMS_PER_PAGE, totalItems);
        String status;
        if (totalItems > 0) {
            int firstItem = currentPage * ITEMS_PER_PAGE + 1;
            int lastItem = Math.min(firstItem + ITEMS_PER_PAGE - 1, totalItems);
            status = String.format("Showing %d-%d of %d competitions", firstItem, lastItem, totalItems);
        } else {
            status = "No competitions to display";
        }
        statusLabel.setText(status);
    }

    private void addPageButton(int pageIndex) {
        MFXButton pageButton = new MFXButton(String.valueOf(pageIndex + 1));
        pageButton.getStyleClass().add("pagination-button");
        pageButton.setStyle("-fx-background-color: " + (pageIndex == currentPage ? "#2196F3" : "#ffffff") +
                "; -fx-text-fill: " + (pageIndex == currentPage ? "#ffffff" : "#334155") +
                "; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        pageButton.setOnAction(event -> {
            currentPage = pageIndex;
            displayCompetitionsPage(pageIndex);
        });

        // Insert before nextButton
        int index = paginationContainer.getChildren().indexOf(nextButton);
        paginationContainer.getChildren().add(index, pageButton);
    }

    private void addEllipsis() {
        Label ellipsis = new Label("...");
        ellipsis.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-padding: 8 12;");
        int index = paginationContainer.getChildren().indexOf(nextButton);
        paginationContainer.getChildren().add(index, ellipsis);
    }

    private void displayCompetitionsPage(int pageIndex) {
        // Clear existing content
        competitionsContainer.getChildren().clear();

        currentPage = pageIndex;
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredCompetitions.size());

        // If no competitions to display
        if (filteredCompetitions.isEmpty()) {
            Label noCompetitionsLabel = new Label("No competitions found");
            noCompetitionsLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px;");
            competitionsContainer.getChildren().add(noCompetitionsLabel);
            updatePagination();
            return;
        }

        // Check if the current page is valid
        int totalPages = (int) Math.ceil((double) filteredCompetitions.size() / ITEMS_PER_PAGE);
        if (pageIndex >= totalPages) {
            currentPage = 0;
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

        // Update pagination controls
        updatePagination();
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
        endDateLabel.setText(competition.getDateFin().format(formatter));

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
        int participantsCount = competition.getCurrentParticipant(); // Random number for demo
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
                Image image = new Image(competition.getImageUrl());
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
        System.out.println("User ID: " + userId + ", Instructor ID: " + instructorId);
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

    private void createPlaceholderImage(ImageView imageView) {
        // Create a JavaFX WritableImage as a placeholder
        int width = 220;
        int height = 180;

        // Create the WritableImage
        WritableImage placeholder = new WritableImage(width, height);

        // Get the PixelWriter
        PixelWriter writer = placeholder.getPixelWriter();

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
        List<Competition> filtered;

        // Apply search filter
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filtered = allCompetitions;
        } else {
            String query = searchTerm.toLowerCase();
            filtered = allCompetitions.stream()
                    .filter(comp ->
                            comp.getNom().toLowerCase().contains(query) ||
                                    comp.getDescription().toLowerCase().contains(query) ||
                                    comp.getCategorie().toLowerCase().contains(query) ||
                                    comp.getEtat().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (!currentStatusFilter.equals("All")) {
            filtered = filtered.stream()
                    .filter(comp -> comp.getEtat().equalsIgnoreCase(currentStatusFilter))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if (currentSortOrder != null) {
            Comparator<Competition> comparator = Comparator.comparing(Competition::getDateComp);
            if (currentSortOrder.equals("latest")) {
                comparator = comparator.reversed();
            }
            filtered = filtered.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        filteredCompetitions.setAll(filtered);

        // Reset to first page
        currentPage = 0;
        updatePagination();
        displayCompetitionsPage(0);
    }

    private void updateFilterButtonStyles() {
        // Reset all filter buttons to default style
        filterAllBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterPlannedBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterInProgressBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterCompletedBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        // Highlight the active filter button
        MFXButton activeFilterBtn = switch (currentStatusFilter) {
            case "Planned" -> filterPlannedBtn;
            case "InProgress" -> filterInProgressBtn;
            case "Completed" -> filterCompletedBtn;
            default -> filterAllBtn;
        };
        activeFilterBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: #ffffff; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        // Reset sort buttons to default style
        sortEarliestBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        sortLatestBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        // Highlight the active sort button
        if (currentSortOrder != null) {
            MFXButton activeSortBtn = currentSortOrder.equals("earliest") ? sortEarliestBtn : sortLatestBtn;
            activeSortBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: #ffffff; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        }
    }

    @FXML
  public  void handleFilterAll(ActionEvent event) {
        currentStatusFilter = "All";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
   public void handleFilterPlanned(ActionEvent event) {
        currentStatusFilter = "Planned";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
   public void handleFilterInProgress(ActionEvent event) {
        currentStatusFilter = "InProgress";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
   public void handleFilterCompleted(ActionEvent event) {
        currentStatusFilter = "Completed";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
   public  void handleSortEarliest(ActionEvent event) {
        currentSortOrder = "earliest";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
  public   void handleSortLatest(ActionEvent event) {
        currentSortOrder = "latest";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
   public void handleSearch(ActionEvent event) {
        filterCompetitions(searchField.getText());
    }

    @FXML
    public void handleCreateCompetition(ActionEvent event) {
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
                   try {
                       controller.setCompetition(competition);
                   } catch (SQLException e) {
                       throw new RuntimeException(e);
                   }

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

    @FXML
    public void handlePreviousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayCompetitionsPage(currentPage);
        }
    }

    @FXML
    public void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) filteredCompetitions.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            displayCompetitionsPage(currentPage);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {

    }

    public void handleProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MyCompetition.fxml"));
            Parent root = loader.load();



            // Set the new scene
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the leaderboard: " + e.getMessage());
        }
    }

    private void showAlert(String error, String s) {
    }

    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void ToLogs(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/LogsList.fxml");
    }

    private static class DialogHelper {
        public static void styleDialog(Alert dialog) {
            dialog.getDialogPane().getStylesheets().add(
                    CompetitionsListController.class.getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }
    }
    private Timeline sessionMonitor;
    public void startSessionMonitor() {
        sessionMonitor = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    LogsService logsService = new LogsService();
                    if (Session.getCurrentLog() != null) {
                        int currentLogId = Session.getCurrentLog().getId();
                        boolean exists = logsService.logExists(currentLogId);
                        if (!exists) {
                            // Stop the session monitor
                            sessionMonitor.stop();

                            Platform.runLater(() -> {
                                try {
                                    Navigator.showAlert(Alert.AlertType.WARNING, "Session expired", "You have been logged out.");
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/Login.fxml"));
                                    Parent root = loader.load();
                                    Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
                                    stage.setScene(new Scene(root));
                                    stage.setTitle("Login");
                                    stage.show();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                })
        );
        sessionMonitor.setCycleCount(Timeline.INDEFINITE);
        sessionMonitor.play();
    }
    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Profile.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");

    }

    public void ToReclamaitons(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamations.fxml");
    }

    public void ToForum(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/AfficherPoste.fxml");
    }

    public void ToDiscover(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");

    }
}