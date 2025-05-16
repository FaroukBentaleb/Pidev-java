package tn.learniverse.controllers.Competition;

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

import io.github.palexdev.materialfx.controls.MFXButton;
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

import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;
import tn.learniverse.tools.Session;

public class CompetitionStep2Controller implements Initializable {

    public MFXButton btncreate;
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
        btncreate.setText("Save Competition");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/challenge_form.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_step1.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the competition and challenges
            CompetitionStep1Controller controller = loader.getController();
            controller.setCompetition(competition, getChallenges());
            
            // Set up the close handler
            controller.setOnCloseHandler(onCloseHandler);
            
            // Replace the current scene with step 1
            Scene scene = challengesContainer.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_view.fxml"));
            Parent previewForm = loader.load();
            
            // Get the controller and set the competition
            CompetitionViewController controller = loader.getController();
            controller.setCompetition(competition, getChallenges());
            
            // Set up the close handler
            controller.setOnCloseHandler(onCloseHandler);
            
            // Replace the current scene with preview
            Scene scene = challengesContainer.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(previewForm));
            stage.show();
            
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
                    btncreate.setText("Update Competition");
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competitions_list.fxml"));
                    Parent listView = loader.load();
                    
                    // Get the controller and reload competitions
                    CompetitionsListController controller = loader.getController();
                    controller.loadCompetitions();
                    
                    // Replace the current scene with the list view
                    Scene scene = ((Node) event.getSource()).getScene();
                    Stage stage = (Stage) scene.getWindow();
                    stage.setScene(new Scene(listView));
                    stage.show();
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, successMessage, ButtonType.OK);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Could not navigate back to competitions list: " + e.getMessage());
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database error: " + e.getMessage());
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
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/challenge_form.fxml"));
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