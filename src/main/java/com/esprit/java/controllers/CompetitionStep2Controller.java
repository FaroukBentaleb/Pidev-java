package com.esprit.java.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;
import com.esprit.java.Services.CompetitionService;
import com.esprit.java.Utility.DatabaseConnection;

public class CompetitionStep2Controller implements Initializable {

    @FXML
    private VBox challengesContainer;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private VBox noChallengesWarning;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Label warningMessage;
    
    @FXML
    private Button previewButton;
    
    private Competition competition;
    private Map<VBox, ChallengeFormController> challengeControllers;
    private int challengeCounter;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        challengeControllers = new HashMap<>();
        challengeCounter = 0;
    }
    
    public void setCompetition(Competition competition) {
        this.competition = competition;
    }
    
    /**
     * Method to restore previously added challenges when returning from step 1
     */
    public void setExistingChallenges(List<Challenge> challenges) {
        if (challenges == null || challenges.isEmpty()) {
            return;
        }
        
        // Hide the no challenges warning
        noChallengesWarning.setVisible(false);
        
        // Add each challenge to the UI
        for (Challenge challenge : challenges) {
            try {
                // Load the challenge form FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/challenge_form.fxml"));
                VBox challengeForm = loader.load();
                
                // Get the controller and set up the challenge
                ChallengeFormController controller = loader.getController();
                challengeCounter++;
                controller.setup(challengeCounter, this);
                
                // Populate the form with existing data
                controller.setTitle(challenge.getTitle());
                controller.setDescription(challenge.getDescription());
                controller.setContent(challenge.getContent());
                controller.setSolution(challenge.getSolution());
                
                // Add to container and store the controller
                challengesContainer.getChildren().add(challengeForm);
                challengeControllers.put(challengeForm, controller);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    void handleAddChallenge(ActionEvent event) {
        try {
            // Load the challenge form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/challenge_form.fxml"));
            VBox challengeForm = loader.load();
            
            // Get the controller and set up the challenge number
            ChallengeFormController controller = loader.getController();
            challengeCounter++;
            controller.setup(challengeCounter, this);
            
            // Add to container and store the controller
            challengesContainer.getChildren().add(challengeForm);
            challengeControllers.put(challengeForm, controller);
            
            // Hide the warning when challenges are added
            noChallengesWarning.setVisible(false);
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load challenge form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleBack(ActionEvent event) {
        try {
            // Collect the current challenges to pass back to step 1
            List<Challenge> currentChallenges = collectChallenges();
            
            // Load the first step again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step1.fxml"));
            Parent root = loader.load();
            
            // Pass the competition and challenges back to step 1
            CompetitionStep1Controller step1Controller = loader.getController();
            step1Controller.setCompetition(competition, currentChallenges);
            
            // Show the first step
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Create Competition");
            stage.show();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not go back to previous step", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleSave(ActionEvent event) {
        // Check if there's at least one challenge
        if (challengeControllers.isEmpty()) {
            noChallengesWarning.setVisible(true);
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "No Challenges Added", 
                    "At least one challenge is required for the competition.");
            return;
        }
        
        if (validateChallenges()) {
            List<Challenge> challenges = collectChallenges();
            
            // Save the competition and challenges
            try (Connection connection = DatabaseConnection.getConnection()) {
                CompetitionService competitionService = new CompetitionService(connection);
                competitionService.createCompetition(competition, challenges);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Competition Created", 
                        "Competition '" + competition.getNom() + "' was created successfully with " + 
                        challenges.size() + " challenges.");
                
                // Close the window
                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                stage.close();
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                        "Could not save competition", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    void handlePreview(ActionEvent event) {
        // Collect challenges
        List<Challenge> challenges = collectChallenges();
        
        // If we have no challenges, show a message
        if (challenges.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Preview", "No Challenges", 
                    "There are no challenges to preview yet. Please add at least one challenge.");
            return;
        }
        
        // Update competition with current challenges
        competition.setChallenges(challenges);
        
        // Show the competition preview
        com.esprit.java.Utility.CompetitionViewHelper.showCompetition(competition);
    }
    
    public void removeChallenge(VBox challengeForm, int challengeNumber) {
        // Remove the challenge form from the container
        challengesContainer.getChildren().remove(challengeForm);
        
        // Remove the controller from the map
        challengeControllers.remove(challengeForm);
        
        // Show warning if no challenges remain
        if (challengeControllers.isEmpty()) {
            noChallengesWarning.setVisible(true);
        }
        
        // Note: We don't decrement challengeCounter to maintain unique numbers
    }
    
    private boolean validateChallenges() {
        boolean isValid = true;
        
        // Validate each challenge individually - they will show their own error messages
        for (ChallengeFormController controller : challengeControllers.values()) {
            if (!controller.validateInputs()) {
                isValid = false;
            }
        }
        
        if (!isValid) {
            // Try to find and scroll to the first error
            ScrollPane parentScrollPane = findScrollPane();
            if (parentScrollPane != null && !challengeControllers.isEmpty()) {
                // Scroll to the position of the first error
                double scrollPosition = 0.0;
                int errorCount = 0;
                int totalForms = challengeControllers.size();
                
                for (VBox challengeForm : challengeControllers.keySet()) {
                    if (challengeForm.getChildren().stream()
                            .anyMatch(node -> node instanceof javafx.scene.control.Label && 
                                    ((javafx.scene.control.Label) node).isVisible() && 
                                    ((javafx.scene.control.Label) node).getStyleClass().contains("validation-message"))) {
                        scrollPosition = (double) errorCount / totalForms;
                        break;
                    }
                    errorCount++;
                }
                
                // Set scroll position
                parentScrollPane.setVvalue(scrollPosition);
            }
        }
        
        return isValid;
    }
    
    private ScrollPane findScrollPane() {
        // If we already have a direct reference to the ScrollPane
        if (scrollPane != null) {
            return scrollPane;
        }
        
        // Otherwise, try to find the ScrollPane by walking up the parent hierarchy
        Node parent = challengesContainer.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                return (ScrollPane) parent;
            }
            parent = parent.getParent();
        }
        
        return null;
    }
    
    public List<Challenge> collectChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        
        for (ChallengeFormController controller : challengeControllers.values()) {
            Challenge challenge = controller.getChallenge();
            challenges.add(challenge);
        }
        
        return challenges;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setOnCloseHandler(Runnable onCloseHandler) {
    }
} 