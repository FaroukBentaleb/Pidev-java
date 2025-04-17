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
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;
import com.esprit.java.Services.CompetitionService;
import com.esprit.java.Utility.DatabaseConnection;

public class CompetitionStep2Controller implements Initializable {

    @FXML
    private VBox challengesContainer;
    
    private Competition competition;
    private List<Challenge> existingChallenges;
    private Runnable onCloseHandler;
    private List<ChallengeFormController> challengeControllers;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        challengeControllers = new ArrayList<>();
        existingChallenges = new ArrayList<>();
    }
    
    public void setCompetition(Competition competition) {
        this.competition = competition;
    }
    
    public void setExistingChallenges(List<Challenge> challenges) {
        this.existingChallenges = challenges != null ? challenges : new ArrayList<>();
        updateChallengesDisplay();
    }
    
    private void updateChallengesDisplay() {
        challengesContainer.getChildren().clear();
        challengeControllers.clear();

        if (existingChallenges.isEmpty()) {
            // No need for warning, just show the add button
            return;
        }

        // Add each challenge with proper sequential numbering
        for (int i = 0; i < existingChallenges.size(); i++) {
            addExistingChallenge(existingChallenges.get(i), i + 1);
        }
    }
    
    /**
     * Adds an existing challenge with a specific challenge number
     */
    private void addExistingChallenge(Challenge challenge, int challengeNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/challenge_form.fxml"));
            VBox challengeForm = loader.load();
            ChallengeFormController controller = loader.getController();
            
            // Set up the challenge controller with the specified number
            controller.setup(challengeNumber, this);
            
            // Set the challenge data
            controller.setChallenge(challenge);
            
            // Set up the delete handler
            controller.setOnDelete(() -> {
                challengesContainer.getChildren().remove(challengeForm);
                challengeControllers.remove(controller);
                
                // Update numbers of remaining challenges
                updateChallengeNumbers();
            });
            
            challengeControllers.add(controller);
            challengesContainer.getChildren().add(challengeForm);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddChallenge() {
        addChallengeForm(null);
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Update competition with current form data without validation
            updateCompetitionFromForm();
            
            // Load step 1 form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step1.fxml"));
            Parent step1Form = loader.load();
            
            // Get the controller and set the competition and challenges
            CompetitionStep1Controller controller = loader.getController();
            controller.setCompetition(competition, getChallenges());
            
            // Set up the close handler
            controller.setOnCloseHandler(onCloseHandler);
            
            // Replace the current form with step 1
            if (challengesContainer != null && challengesContainer.getScene() != null) {
                VBox parent = (VBox) challengesContainer.getScene().getRoot();
                parent.getChildren().setAll(step1Form);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handlePreview(ActionEvent event) {
        try {
            // Update competition with current form data without validation
            updateCompetitionFromForm();
            
            // Load preview form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_view.fxml"));
            Parent previewForm = loader.load();
            
            // Get the controller and set the competition
            CompetitionViewController controller = loader.getController();
            controller.setCompetition(competition, getChallenges());
            
            // Set up the close handler
            controller.setOnCloseHandler(onCloseHandler);
            
            // Replace the current form with preview
            if (challengesContainer != null && challengesContainer.getScene() != null) {
                VBox parent = (VBox) challengesContainer.getScene().getRoot();
                parent.getChildren().setAll(previewForm);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCreateCompetition(ActionEvent event) {
        try {
            // First validate the form
            if (!validateForm()) {
                return;
            }
            
            // Update competition with current form data
            updateCompetitionFromForm();
            
            // Get database connection
            Connection connection = null;
            String successMessage = "";
            
            try {
                connection = DatabaseConnection.getConnection();
                
                // Create competition service with connection
                CompetitionService competitionService = new CompetitionService(connection);
                
                // Save or update the competition
                if (competition.getId() > 0) {
                    // Update existing competition
                    competitionService.updateCompetitionWithChallenges(competition, getChallenges());
                    successMessage = "Competition updated successfully.";
                } else {
                    // Create new competition
                    competition = competitionService.createCompetition(competition, getChallenges());
                    successMessage = "Competition created successfully.";
                }
                
                // Navigate back to the competitions list automatically
                try {
                    // Load the competitions list view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competitions_list.fxml"));
                    Parent listView = loader.load();
                    
                    // Get the controller and initialize it
                    CompetitionsListController controller = loader.getController();
                    
                    // Get the current scene's root
                    Node root = challengesContainer.getScene().getRoot();
                    if (root instanceof VBox) {
                        VBox container = (VBox) root;
                        // Clear the container and add the list view
                        container.getChildren().clear();
                        container.getChildren().add(listView);
                        
                        // Load competitions
                        controller.loadCompetitions();
                        
                        // Show success message after navigation
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText(successMessage);
                        successAlert.show(); // Use show() instead of showAndWait() to not block the UI
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // If automatic navigation fails, use the onCloseHandler as fallback
                    if (onCloseHandler != null) {
                        onCloseHandler.run();
                    }
                    // Show success message even if navigation fails
                    showAlert("Success", successMessage);
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
    
    private List<Challenge> getChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        for (ChallengeFormController controller : challengeControllers) {
            Challenge challenge = controller.getChallenge();
            if (challenge != null) {
                challenges.add(challenge);
            }
        }
        return challenges;
    }
    
    private void addChallengeForm(Challenge existingChallenge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/challenge_form.fxml"));
            VBox challengeForm = loader.load();
            ChallengeFormController controller = loader.getController();
            
            // Calculate the challenge number based on current number of challenges
            int challengeNumber = challengeControllers.size() + 1;
            
            // Set up the challenge controller with the correct number
            controller.setup(challengeNumber, this);
            
            if (existingChallenge != null) {
                controller.setChallenge(existingChallenge);
            }
            
            // Set up the delete handler
            controller.setOnDelete(() -> {
                challengesContainer.getChildren().remove(challengeForm);
                challengeControllers.remove(controller);
                
                // Update numbers of remaining challenges
                updateChallengeNumbers();
            });
            
            challengeControllers.add(controller);
            challengesContainer.getChildren().add(challengeForm);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the challenge numbers of all challenge forms to maintain sequential ordering
     */
    private void updateChallengeNumbers() {
        for (int i = 0; i < challengeControllers.size(); i++) {
            challengeControllers.get(i).setup(i + 1, this);
        }
    }
    
    private boolean validateForm() {
        if (challengeControllers.isEmpty()) {
            showAlert("Error", "At least one challenge is required.");
            return false;
        }
        
        for (ChallengeFormController controller : challengeControllers) {
            if (!controller.validateForm()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void updateCompetitionFromForm() {
        if (competition != null) {
            competition.setChallenges(getChallenges());
        }
    }
    
    private void showAlert(String title, String content) {
        Alert.AlertType type = Alert.AlertType.ERROR;
        
        // Determine alert type based on title
        if (title.equalsIgnoreCase("Success")) {
            type = Alert.AlertType.INFORMATION;
        } else if (title.equalsIgnoreCase("Warning")) {
            type = Alert.AlertType.WARNING;
        }
        
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;
    }

    // This method will be called by the CompetitionViewController when creating the competition
    public boolean validateChallenges() {
        if (challengeControllers.isEmpty()) {
            showAlert("Error", "At least one challenge is required.");
            return false;
        }
        
        for (ChallengeFormController controller : challengeControllers) {
            if (!controller.validateForm()) {
                return false;
            }
        }
        
        return true;
    }
} 