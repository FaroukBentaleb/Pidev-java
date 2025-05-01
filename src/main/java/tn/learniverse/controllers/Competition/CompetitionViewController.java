package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import  tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;
import tn.learniverse.tools.Session;

public class CompetitionViewController implements Initializable {
    
    @FXML
    private ImageView competitionImage;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label categoryLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label durationLabel;
    
    @FXML
    private Label startDateLabel;
    
    @FXML
    private Label endDateLabel;
    
    @FXML
    private VBox descriptionContainer;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private VBox challengesContainer;
    
    @FXML
    private Label nameErrorLabel;
    
    @FXML
    private Label descriptionErrorLabel;
    
    @FXML
    private Label dateErrorLabel;
    
    private Competition competition;
    private List<Challenge> challenges;
    private Runnable onCloseHandler;
    private Map<String, Boolean> validationStatus;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize validation map
        validationStatus = new HashMap<>();
        
        // Set all validation statuses to false initially
        validationStatus.put("name", false);
        validationStatus.put("description", false);
        validationStatus.put("dates", false);
        validationStatus.put("challenges", false);
        
        // Hide error labels initially
        if (nameErrorLabel != null) nameErrorLabel.setVisible(false);
        if (descriptionErrorLabel != null) descriptionErrorLabel.setVisible(false);
        if (dateErrorLabel != null) dateErrorLabel.setVisible(false);
    }
    
    public void setCompetition(Competition competition, List<Challenge> challenges) {
        this.competition = competition;
        this.challenges = challenges;
        updateView();
        
        // Validate initial state
        validateName(competition.getNom());
        validateDescription(competition.getDescription());
        validateDates(competition.getDateComp(), competition.getDateFin());
        validateChallenges(challenges);
        
        // Add listeners for real-time validation
        setupValidationListeners();
    }
    
    /**
     * Overloaded version that extracts challenges from the competition
     */
    public void setCompetition(Competition competition) throws SQLException {
        this.competition = competition;
        if (competition.getChallenges() == null || competition.getChallenges().isEmpty()) {
            challenges = CompetitionService.getChallengesForCompetition(competition.getId());
        } else {

            this.challenges = competition.getChallenges();
        }
        updateView();
    }

    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;
    }
    
private void updateView() {
        if (competition == null) return;

        // Update basic info
        nameLabel.setText(competition.getNom());
        categoryLabel.setText("Category: " + competition.getCategorie());
        statusLabel.setText(competition.getEtat());

        // Format and set dates with time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy • h:mm a").withLocale(Locale.ENGLISH);
        startDateLabel.setText(competition.getDateComp().format(formatter));
        endDateLabel.setText(competition.getDateFin().format(formatter));

        // Set duration
        durationLabel.setText("Duration: " + formatDuration(competition.getDuration()));

        // Set status style with enhanced design
        String statusStyle = switch (competition.getEtat().toLowerCase()) {
            case "Completed" -> "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);";
            case "InProgress" -> "-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);";
            case "Planned" -> "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);";
            default -> "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);";
        };
        statusLabel.setStyle(statusStyle);

        // Set competition image
        if (competition.getWebImageUrl() != null && !competition.getWebImageUrl().isEmpty()) {
            try {
                // Try to load from web URL first
                Image image = new Image(competition.getWebImageUrl());
                if (image.isError()) {
                    // Fall back to local file if web URL fails
                    loadLocalImage();
                } else {
                    competitionImage.setImage(image);
                }
            } catch (Exception e) {
                loadLocalImage();
            }
        } else {
            loadLocalImage();
        }

        // Set description as static text
        String descriptionText = extractTextFromHtml(competition.getDescription());
        descriptionLabel.setText(descriptionText);
        descriptionLabel.setMinHeight(60);
        // Add challenges
        challengesContainer.getChildren().clear();
        if (competition.getChallenges() != null) {
            for (Challenge challenge : competition.getChallenges()) {
                addChallengeCard(challenge);
            }
        }

        // Add the Participate button after the challenges
        addParticipateButton();
    }    private void handleStart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/start.fxml"));
            Parent root = loader.load();
            SubmissionViewController controller = loader.getController();
            controller.setCompetition(competition);


            // Set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) competitionImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the submission view: " + e.getMessage());
        }
    }

    /**
     * Adds the Participate button after the challenges
     */
    private void addParticipateButton() {

        // Create the Participate button container
        VBox participateContainer = new VBox();
        participateContainer.setStyle("-fx-padding: 15 10 5 10;" +
                                   "-fx-alignment: center-right;" +
                                   "-fx-spacing: 15;");

        // Create the Participate button
        Button participateButton = new Button();
        participateButton.setStyle("-fx-background-color: #2196F3;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 12 30;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        // Add hover effect
        participateButton.setOnMouseEntered(e ->
            participateButton.setStyle(participateButton.getStyle().replace("-fx-background-color: #2196F3;", "-fx-background-color: #1976D2;")));
        participateButton.setOnMouseExited(e ->
            participateButton.setStyle(participateButton.getStyle().replace("-fx-background-color: #1976D2;", "-fx-background-color: #2196F3;")));

        // Set button text and action based on competition status
        switch (competition.getEtat().toLowerCase()) {
            case "inprogress":
                if (CompetitionService.isUserSubmissited(competition.getId(),Session.getCurrentUser().getId())){
                    participateButton.setText("Submitted");
                    participateButton.setOnAction(e -> handleViewSubmission());
                }
                else{

                participateButton.setText("Start");
                participateButton.setOnAction(e -> handleStart());
                }
                break;
            case "planned":
                if (CompetitionService.isUserParticipant(competition.getId(),Session.getCurrentUser().getId())){
                    participateButton.setText("Participated");
                    participateButton.setOnAction(e -> handleRemoveParti());

                }

                else{
                participateButton.setText("Participate");
                participateButton.setOnAction(e -> handleParticipate());
                }
                break;
            case "completed":
                participateButton.setText("Leaderboard");
                participateButton.setOnAction(e -> handleLeaderboard());
                break;
            default:
                participateButton.setText("Unknown Status");
                participateButton.setDisable(true);
                break;
        }

        // Add the button to the container
        participateContainer.getChildren().add(participateButton);

        // Add the participate container to the challenges container
        challengesContainer.getChildren().add(participateContainer);
    }

    private void handleViewSubmission() {

    }

    private void handleRemoveParti() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Participate in Competition");
        alert.setHeaderText("Competition: " + competition.getNom());
        alert.setContentText("Are you sure you want to leave  this competition?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed participation
            CompetitionService.removeParticipant(competition);
            // Dynamically update the button text to "Participated"
            Button participateButton = (Button) challengesContainer.lookup("#participateButton");
            if (participateButton != null) {
                participateButton.setText("Participate");
            }

            showAlert("Success", "You have successfully left  the competition: " + competition.getNom());
        }
    }

    private void handleLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompetitionLeaderboard.fxml"));
            Parent root = loader.load();
            CompetitionLeaderboardController controller = loader.getController();
            controller.initData(competition.getId());


            // Set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) competitionImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the leaderboard: " + e.getMessage());
        }
    }

    /**
     * Extracts plain text from HTML content
     */
    private String extractTextFromHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "No description provided for this competition.";
        }
        
        // Simple HTML tag removal
        String text = htmlContent.replaceAll("<[^>]*>", " ");
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
    /**
     * Formats minutes into a more readable duration string
     */
    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            
            if (remainingMinutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " " + remainingMinutes + " minute" + (remainingMinutes > 1 ? "s" : "");
            }
        }
    }

    private void addChallengeCard(Challenge challenge) {
        try {
            // Create a VBox for the challenge card with modern styling
            VBox card = new VBox();
            card.setStyle("-fx-padding: 25;");
            card.getStyleClass().add("challenge-card");
            card.setSpacing(20);

            // Challenge number and title
            Label titleLabel = new Label("Challenge " + (challengesContainer.getChildren().size() + 1) + ": " + challenge.getTitle());
            titleLabel.setStyle("-fx-font-size: 22px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #1a365d;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE); // Add this line

            // Description section
            VBox descriptionBox = new VBox(10);
            Label descriptionHeader = new Label("Description");
            descriptionHeader.setStyle("-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2196F3;");

            Label descriptionContent = new Label(extractTextFromHtml(challenge.getDescription()));
            descriptionContent.setStyle("-fx-font-size: 14px;" +
                    "-fx-text-fill: #334155;" +
                    "-fx-wrap-text: true;" +
                    "-fx-line-spacing: 1.4;");
            descriptionContent.setMaxWidth(Double.MAX_VALUE); // Add this line
            descriptionContent.setWrapText(true); // Enable text wrapping
            descriptionContent.setMinHeight(60); // Set minimum height to preferred size
            descriptionBox.getChildren().addAll(descriptionHeader, descriptionContent);

            // Add all sections to the card
            card.getChildren().addAll(titleLabel, descriptionBox);

            // Add spacing between cards
            VBox.setMargin(card, new javafx.geometry.Insets(0, 5, 20, 5));

            // Add the card to the container
            challengesContainer.getChildren().add(card);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load challenge: " + e.getMessage());
        }
    }
    /**
     * Handles the participate button click
     */
    private void handleParticipate() {
        if (competition == null) {
            showAlert("Error", "No competition selected");
            return;
        }
        
        // Show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Participate in Competition");
        alert.setHeaderText("Competition: " + competition.getNom());
        alert.setContentText("Are you sure you want to participate in this competition?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed participation
            CompetitionService.AddParticipant(competition);

            // Dynamically update the button text to "Participated"
            Button participateButton = (Button) challengesContainer.lookup("#participateButton");
            if (participateButton != null) {
                participateButton.setText("Participated");
            }

            showAlert("Success", "You have successfully registered for the competition: " + competition.getNom());
        }
    }
    
    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_competition.png"));
            competitionImage.setImage(defaultImage);
        } catch (Exception e) {
            // If default image fails to load, just leave it empty

        }
    }
    
    private void setupValidationListeners() {
        // Name listener
        if (nameLabel != null) {
            nameLabel.textProperty().addListener((observable, oldValue, newValue) -> {
                validateName(newValue);
            });
        }
        
        // Description listener - if this is editable in the view
        if (descriptionLabel != null) {
            descriptionLabel.textProperty().addListener((observable, oldValue, newValue) -> {
                validateDescription(newValue);
            });
        }
        
        // Challenge listener - update validation when challenges change
        if (challengesContainer != null) {
            challengesContainer.getChildren().addListener((javafx.collections.ListChangeListener.Change<? extends Node> c) -> {
                validateChallenges(challenges);
            });
        }
    }
    
    private boolean validateName(String name) {
        boolean isValid = name != null && !name.trim().isEmpty();
        
        // Update UI based on validation result
        if (nameErrorLabel != null) {
            nameErrorLabel.setVisible(!isValid);
            nameErrorLabel.setText(isValid ? "" : "Competition name is required");
        }
        
        // Update validation status
        validationStatus.put("name", isValid);
        return isValid;
    }
    
    private boolean validateDescription(String description) {
        boolean isValid = description != null && !description.trim().isEmpty();
        
        // Update UI based on validation result
        if (descriptionErrorLabel != null) {
            descriptionErrorLabel.setVisible(!isValid);
            descriptionErrorLabel.setText(isValid ? "" : "Competition description is required");
        }
        
        // Update validation status
        validationStatus.put("description", isValid);
        return isValid;
    }
    
    private boolean validateDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        boolean datesPresent = startDate != null && endDate != null;
        boolean datesOrdered = datesPresent && !startDate.isAfter(endDate);
        boolean isValid = datesPresent && datesOrdered;
        
        // Update UI based on validation result
        if (dateErrorLabel != null) {
            dateErrorLabel.setVisible(!isValid);
            
            if (!datesPresent) {
                dateErrorLabel.setText("Competition dates are required");
            } else if (!datesOrdered) {
                dateErrorLabel.setText("Start date must be before end date");
            } else {
                dateErrorLabel.setText("");
            }
        }
        
        // Update validation status
        validationStatus.put("dates", isValid);
        return isValid;
    }
    
    private boolean validateChallenges(List<Challenge> challenges) {
        boolean isValid = challenges != null && !challenges.isEmpty();
        
        // Update validation status
        validationStatus.put("challenges", isValid);
        return isValid;
    }
    
    private boolean isFormValid() {
        // Check if all validation statuses are true
        return validationStatus.values().stream().allMatch(status -> status);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competitions_list.fxml"));
            Parent root = loader.load();
            
            // Set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) competitionImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
            // Reload competitions
            CompetitionsListController controller = loader.getController();
            controller.loadCompetitions();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_step1.fxml"));
            Parent form = loader.load();
            
            // Get the controller and set the competition
            CompetitionStep1Controller controller = loader.getController();
            controller.setCompetition(competition, competition.getChallenges());
            
            // Set the new scene
            Scene scene = new Scene(form);
            Stage stage = (Stage) competitionImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // Check if the form is valid using the validation status map
            if (!isFormValid()) {
                // Show appropriate error messages
                if (!validationStatus.get("name")) {
                    showAlert("Error", "Competition name is required.");
                } else if (!validationStatus.get("description")) {
                    showAlert("Error", "Competition description is required.");
                } else if (!validationStatus.get("dates")) {
                    showAlert("Error", "Please check the competition dates.");
                } else if (!validationStatus.get("challenges")) {
                    showAlert("Error", "At least one challenge is required.");
                }
                return;
            }

            // Get database connection
            Connection connection = null;
            try {
                connection = DatabaseConnection.getConnection();
                
                // Create competition service with connection
                CompetitionService competitionService = new CompetitionService(connection);
                
                // Create competition
                competitionService.createCompetition(competition, competition.getChallenges());
                
                // Show success message
                showAlert("Success", "Competition created successfully!");
                
                // Close the form
                if (onCloseHandler != null) {
                    onCloseHandler.run();
                }
            } finally {
                // Close connection
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to create competition: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadLocalImage() {
        // Check for local file path
        if (competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
            try {
                Image image = new Image("file:" + competition.getImageUrl());
                if (image.isError()) {
                    setDefaultImage();
                } else {
                    competitionImage.setImage(image);
                }
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    @FXML
    private void handleViewChallenge(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/challenge_view.fxml"));
            Parent root = loader.load();
            // ... rest of the code ...
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToList(ActionEvent event) {
        try {
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Load the competitions list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competitions_list.fxml"));
            Parent root = loader.load();
            
            // Create a new scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not return to competitions list. Please try again.");
            alert.showAndWait();
        }
    }
} 