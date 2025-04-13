package com.esprit.java.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;
import com.esprit.java.Utility.RichTextUtils;

public class CompetitionStep1Controller implements Initializable {

    @FXML
    private TextField nameField;
    
    @FXML
    private CustomHtmlEditor descriptionEditor;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Spinner<Integer> durationSpinner;
    
    @FXML
    private TextField imageUrlField;
    
    // The competition object to be passed between steps
    private Competition competition;
    
    // Store challenges when returning from step 2
    private List<Challenge> existingChallenges;
    
    // Handler for when the form is closed
    private Runnable onCloseHandler;
    
    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the competition object
        competition = new Competition();
        
        // Setup category ComboBox
        categoryComboBox.getItems().addAll(
            "Algorithms", 
            "Data Structures", 
            "Web Development", 
            "Mobile Development", 
            "Machine Learning", 
            "Database Design"
        );
        
        // Setup status ComboBox
        statusComboBox.getItems().addAll(
            "Draft", 
            "Planned", 
            "Scheduled", 
            "Active", 
            "Completed"
        );
        // Set default value per Symfony entity
        statusComboBox.setValue("Planned");
        
        // Setup duration spinner (min: 15 minutes, max: 360 min, in steps of 15)
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 360, 60, 15);
        durationSpinner.setValueFactory(valueFactory);
        
        // Set default dates
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today.plusDays(1));
        
        // Add listeners to update end date when start date changes
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                if (endDatePicker.getValue() == null || endDatePicker.getValue().isBefore(newDate)) {
                    endDatePicker.setValue(newDate.plusDays(1));
                }
            }
        });
        
        // Configure the HTML editor without replacing it
        if (descriptionEditor != null) {
            // Set initial content
            descriptionEditor.setHtmlText(RichTextUtils.createEmptyHtml("Enter competition description (min 10 characters)"));
            
            // Apply styling
            if (!descriptionEditor.getStyleClass().contains("small-html-editor")) {
                descriptionEditor.getStyleClass().add("small-html-editor");
            }
            
            // Add validation on focus change
            descriptionEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateDescription();
                }
            });
        }
        
        // Add style class for validation
        nameField.getStyleClass().add("form-field");
        categoryComboBox.getStyleClass().add("form-field");
        statusComboBox.getStyleClass().add("form-field");
        startDatePicker.getStyleClass().add("form-field");
        endDatePicker.getStyleClass().add("form-field");
    }
    
    /**
     * Method to set the competition data when returning from step 2
     */
    public void setCompetition(Competition competition, List<Challenge> challenges) {
        this.competition = competition;
        this.existingChallenges = challenges;
        
        // Populate form fields with existing data
        if (competition != null) {
            nameField.setText(competition.getNom());
            
            // Handle HTML content
            if (descriptionEditor != null && competition.getDescription() != null && !competition.getDescription().isEmpty()) {
                if (competition.getDescription().startsWith("<html>")) {
                    descriptionEditor.setHtmlText(competition.getDescription());
                } else {
                    // Convert plain text to HTML
                    descriptionEditor.setHtmlText(RichTextUtils.textToHtml(competition.getDescription()));
                }
            }
            
            categoryComboBox.setValue(competition.getCategorie());
            statusComboBox.setValue(competition.getEtat());
            
            if (competition.getDateComp() != null) {
                startDatePicker.setValue(competition.getDateComp().toLocalDate());
            }
            
            if (competition.getDateFin() != null) {
                endDatePicker.setValue(competition.getDateFin().toLocalDate());
            }
            
            durationSpinner.getValueFactory().setValue(competition.getDuration());
            imageUrlField.setText(competition.getImageUrl());
        }
    }
    
    @FXML
    void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            imageUrlField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleNext(ActionEvent event) {
        if (validateForm()) {
            try {
                // Update competition with form data
                updateCompetitionFromForm();
                
                // Load step 2 form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step2.fxml"));
                Parent step2Form = loader.load();
                
                // Get the controller and set the competition
                CompetitionStep2Controller controller = loader.getController();
                controller.setCompetition(competition);
                
                // Set up the close handler
                controller.setOnCloseHandler(onCloseHandler);
                
                // Replace the current form with step 2
                VBox parent = (VBox) descriptionEditor.getScene().getRoot();
                parent.getChildren().setAll(step2Form);
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not load next step", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        if (onCloseHandler != null) {
            onCloseHandler.run();
        }
    }
    
    private boolean validateForm() {
        // Reset styling
        resetValidationStyling();
        
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;
        
        // Validate name (min: 3, max: 255)
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errors.append("- The competition name is required.\n");
            nameField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (name.length() < 3) {
            errors.append("- The competition name must be at least 3 characters.\n");
            nameField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (name.length() > 255) {
            errors.append("- The competition name cannot exceed 255 characters.\n");
            nameField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate description
        boolean descriptionValid = validateDescription();
        if (!descriptionValid) {
            errors.append("- The description must be at least 10 characters.\n");
            isValid = false;
        }
        
        // Validate category
        if (categoryComboBox.getValue() == null) {
            errors.append("- The category is required.\n");
            categoryComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate status
        if (statusComboBox.getValue() == null) {
            errors.append("- The status is required.\n");
            statusComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate start date
        if (startDatePicker.getValue() == null) {
            errors.append("- The competition start date is required.\n");
            startDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (startDatePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("- The competition start date cannot be in the past.\n");
            startDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate end date
        if (endDatePicker.getValue() == null) {
            errors.append("- The competition end date is required.\n");
            endDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errors.append("- The competition end date must be after the start date.\n");
            endDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate duration
        Integer duration = durationSpinner.getValue();
        if (duration == null) {
            errors.append("- The duration is required.\n");
            durationSpinner.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (duration < 15) {
            errors.append("- Duration must be at least 15 minutes.\n");
            durationSpinner.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Please correct the following errors:", errors.toString());
        }
        
        return isValid;
    }
    
    private boolean validateDescription() {
        if (descriptionEditor == null) return true;
        
        String description = RichTextUtils.extractTextFromHtml(descriptionEditor.getHtmlText());
        boolean isValid = true;
        
        // Reset styling
        descriptionEditor.setStyle("");
        
        if (description.isEmpty()) {
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (description.length() < 10) {
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        return isValid;
    }
    
    private void resetValidationStyling() {
        nameField.setStyle("");
        if (descriptionEditor != null) descriptionEditor.setStyle("");
        categoryComboBox.setStyle("");
        statusComboBox.setStyle("");
        startDatePicker.setStyle("");
        endDatePicker.setStyle("");
        durationSpinner.setStyle("");
    }
    
    private void updateCompetitionFromForm() {
        // Set basic information
        competition.setNom(nameField.getText());
        competition.setDescription(descriptionEditor.getHtmlText());
        competition.setCategorie(categoryComboBox.getValue());
        competition.setEtat(statusComboBox.getValue());
        competition.setDuration(durationSpinner.getValue());
        
        // Set the start date and time
        LocalDate startDate = startDatePicker.getValue();
        if (startDate != null) {
            competition.setDateComp(LocalDateTime.of(startDate, LocalTime.MIDNIGHT));
        }
        
        // Set the end date and time
        LocalDate endDate = endDatePicker.getValue();
        if (endDate != null) {
            competition.setDateFin(LocalDateTime.of(endDate, LocalTime.MIDNIGHT));
        }
        
        // Set image path if selected
        if (imageUrlField.getText() != null && !imageUrlField.getText().isEmpty()) {
            competition.setImageUrl(imageUrlField.getText());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 