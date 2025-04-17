package com.esprit.java.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

import io.github.palexdev.materialfx.controls.models.spinner.SpinnerModel;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;
import com.esprit.java.Utility.RichTextUtils;
import com.esprit.java.Utility.DatabaseConnection;
import com.esprit.java.Services.CompetitionService;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import io.github.palexdev.materialfx.controls.models.spinner.IntegerSpinnerModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class CompetitionStep1Controller implements Initializable {

    @FXML
    private MFXTextField nameField;
    
    @FXML
    private CustomHtmlEditor descriptionEditor;
    
    @FXML
    private MFXComboBox<String> categoryComboBox;
    
    @FXML
    private MFXComboBox<String> statusComboBox;
    
    @FXML
    private MFXDatePicker dateCompPicker;
    
    @FXML
    private MFXDatePicker endDatePicker;
    
    @FXML
    private MFXSpinner<Integer> durationSpinner;
    
    @FXML
    private MFXTextField imageUrlField;
    
    @FXML
    private Label nameError;
    
    @FXML
    private Label descriptionError;
    
    @FXML
    private Label categoryError;
    
    @FXML
    private Label statusError;
    
    @FXML
    private Label startDateError;
    
    @FXML
    private Label endDateError;
    
    @FXML
    private Label durationError;
    
    @FXML
    private ImageView imagePreview;
    
    @FXML
    private Label imageError;
    
    @FXML
    private MFXTextField startHourField;
    
    @FXML
    private MFXTextField startMinuteField;
    
    @FXML
    private MFXTextField endHourField;
    
    @FXML
    private MFXTextField endMinuteField;
    
    // The competition object to be passed between steps
    private Competition competition;
    
    // Store challenges when returning from step 2
    private List<Challenge> existingChallenges;
    
    // Handler for when the form is closed
    private Runnable onCloseHandler;
    
    // Map to track validation errors
    private Map<String, String> errors;
    
    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize errors map
        errors = new HashMap<>();
        
        // Initialize the competition object
        competition = new Competition();
        
        // Setup category ComboBox
        categoryComboBox.getItems().addAll(
            "Programming", 
            "Algorithm", 
            "Database", 
            "Web Development"
        );
        categoryComboBox.setValue("Programming");
        
    
        
        // Create the spinner model with initial value 60
        IntegerSpinnerModel spinnerModel = new IntegerSpinnerModel(60);
        // Set custom min/max/increment values
        spinnerModel.setMin(30);
        spinnerModel.setMax(480);
        spinnerModel.setIncrement(5);
        // Set the model to the spinner
        durationSpinner.setSpinnerModel(spinnerModel);
        durationSpinner.setEditable(true);
        durationSpinner.setPromptText("Duration in minutes");
        
        // Set default dates
        LocalDate today = LocalDate.now();
        dateCompPicker.setValue(today);
        endDatePicker.setValue(today.plusDays(1));
        
        // Add listeners for real-time updates
        dateCompPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateEndDate());
        durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateEndDate());
        
        // Add validation listeners
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateName());
        
        // For HTML editors, we'll validate on focus change instead of using a property
        // Add focus change listeners for validation
        if (descriptionEditor != null) {
            descriptionEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateDescription();
                }
            });
        }
        
        // Initialize image URL field
        imageUrlField.setText("");
        
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
        
        // Add focus change listeners for validation
        if (nameField != null) {
            nameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateName();
                }
            });
        }
        
        // Add style class for validation
        if (nameField != null) nameField.getStyleClass().add("form-field");
        if (categoryComboBox != null) categoryComboBox.getStyleClass().add("form-field");
        if (statusComboBox != null) statusComboBox.getStyleClass().add("form-field");
        if (dateCompPicker != null) dateCompPicker.getStyleClass().add("form-field");
        if (endDatePicker != null) endDatePicker.getStyleClass().add("form-field");
        
        // Initialize time fields
        initializeTimeFields();
    }
    
    /**
     * Method to set the competition data when returning from step 2
     */
    public void setCompetition(Competition competition, List<Challenge> challenges) {
        this.competition = competition;
        this.existingChallenges = challenges != null ? challenges : new ArrayList<>();
        
        // Populate form fields with existing data
        if (competition != null) {
            if (nameField != null) nameField.setText(competition.getNom());
            
            if (descriptionEditor != null && competition.getDescription() != null) {
                    descriptionEditor.setHtmlText(competition.getDescription());
                }
            
            if (categoryComboBox != null) {
                categoryComboBox.setValue(competition.getCategorie());
            }
            
            if (statusComboBox != null) {
            statusComboBox.setValue(competition.getEtat());
            }
            
            if (dateCompPicker != null && competition.getDateComp() != null) {
                dateCompPicker.setValue(competition.getDateComp().toLocalDate());
            }
            
            if (endDatePicker != null && competition.getDateFin() != null) {
                endDatePicker.setValue(competition.getDateFin().toLocalDate());
            }
            
            if (durationSpinner != null) {
                durationSpinner.setValue(competition.getDuration());
            }
            
            if (imageUrlField != null) {
            imageUrlField.setText(competition.getImageUrl());
            }
        }
        
        // Initialize form validation
        initializeFormValidation();
    }
    
    @FXML
    void handleUploadImage(ActionEvent event) {
        if (event == null || event.getSource() == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Node sourceNode = (Node) event.getSource();
        if (sourceNode == null || sourceNode.getScene() == null || sourceNode.getScene().getWindow() == null) {
            System.err.println("Could not get window reference for FileChooser.");
            return; // Cannot proceed without a window
        }
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Define XAMPP htdocs directory path
                String xamppPath = "C:/xampp/htdocs/competition_images";
                
                // Create directory if it doesn't exist
                File targetDir = new File(xamppPath);
                if (!targetDir.exists()) {
                    if (!targetDir.mkdirs()) {
                        throw new IOException("Failed to create directory: " + xamppPath);
                    }
                }
                
                // Generate a unique filename to avoid overwriting
                String originalFilename = selectedFile.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                String uniqueFilename = System.currentTimeMillis() + fileExtension;
                File targetFile = new File(targetDir, uniqueFilename);
                
                // Copy the file to XAMPP htdocs
                java.nio.file.Files.copy(
                    selectedFile.toPath(), 
                    targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                
                // Update image URL field with both the original path and web path
                String webPath = "http://localhost/competition_images/" + uniqueFilename;
                if (imageUrlField != null) {
                    // Store both the file system path (for JavaFX display) and web path (for web access)
                    competition.setWebImageUrl(webPath);
                    imageUrlField.setText(targetFile.getAbsolutePath());
                }
                
                // Update image preview
                if (imagePreview != null) {
                    try {
                        Image image = new Image(targetFile.toURI().toString(), 60, 40, true, true);
                        imagePreview.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Error loading image preview: " + e.getMessage());
                        if (imageError != null) {
                            imageError.setText("Could not load image preview.");
                            imageError.setVisible(true);
                        }
                        imagePreview.setImage(null);
                    }
                }
                
                // Clear any previous image error
                if (imageError != null) {
                    imageError.setVisible(false);
                }
                
            } catch (IOException e) {
                System.err.println("Error copying image to XAMPP htdocs: " + e.getMessage());
                e.printStackTrace();
                
                // Show error and fallback to original behavior
                showAlert(Alert.AlertType.ERROR, "Error", null, 
                    "Failed to copy image to web server: " + e.getMessage());
                
                // Fallback to original path
                String imagePath = selectedFile.getAbsolutePath();
                if (imageUrlField != null) {
                    imageUrlField.setText(imagePath);
                }
                
                // Update image preview with original file
                if (imagePreview != null) {
                    try {
                        Image image = new Image(selectedFile.toURI().toString(), 60, 40, true, true);
                        imagePreview.setImage(image);
                    } catch (Exception ex) {
                        System.err.println("Error loading image preview: " + ex.getMessage());
                        if (imageError != null) {
                            imageError.setText("Could not load image preview.");
                            imageError.setVisible(true);
                        }
                        imagePreview.setImage(null);
                    }
                }
            }
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
                
                // Get the controller and set the competition and challenges
                CompetitionStep2Controller controller = loader.getController();
                controller.setCompetition(competition);
                controller.setExistingChallenges(existingChallenges);
                
                // Set up the close handler
                controller.setOnCloseHandler(onCloseHandler);
                
                // Replace the current form with step 2
                if (descriptionEditor != null && descriptionEditor.getScene() != null) {
                    VBox parent = (VBox) descriptionEditor.getScene().getRoot();
                    parent.getChildren().setAll(step2Form);
                }
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not load next step", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            // Load the competitions list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competitions_list.fxml"));
            Parent listView = loader.load();
            
            // Get the current scene's root
            Node root = ((Node) event.getSource()).getScene().getRoot();
            if (root instanceof VBox) {
                VBox container = (VBox) root;
                // Clear the container and add the list view
                container.getChildren().clear();
                container.getChildren().add(listView);
                
                // Get the controller and reload competitions
                CompetitionsListController controller = loader.getController();
                controller.loadCompetitions();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not load competitions list", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearImage() {
        imageUrlField.setText("");
        imagePreview.setImage(null);
        competition.setImageUrl(null);
        competition.setWebImageUrl(null);
    }
    
    private boolean validateName() {
        if (nameField == null) return true;
        
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        boolean isValid = true;
        
        // Hide error initially
        if (nameError != null) nameError.setVisible(false);
        
        // Reset styling
        nameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        nameField.setStyle(""); // Reset inline style
        
        if (name.isEmpty()) {
            if (nameError != null) {
                nameError.setText("Competition name is required");
                nameError.setVisible(true);
            }
            nameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
            nameField.setStyle("-fx-border-color: #ef4444;"); // Add direct style for red border
            isValid = false;
        } else if (name.length() < 3) {
            if (nameError != null) {
                nameError.setText("Name must be at least 3 characters");
                nameError.setVisible(true);
            }
            nameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
            nameField.setStyle("-fx-border-color: #ef4444;"); // Add direct style for red border
            isValid = false;
        } else if (name.length() > 255) {
            if (nameError != null) {
                nameError.setText("Name cannot exceed 255 characters");
                nameError.setVisible(true);
            }
            nameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
            nameField.setStyle("-fx-border-color: #ef4444;"); // Add direct style for red border
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean validateDescription() {
        if (descriptionEditor == null) return true;
        
        String description = RichTextUtils.extractTextFromHtml(descriptionEditor.getHtmlText());
        boolean isValid = true;
        
        // Hide error initially
        if (descriptionError != null) descriptionError.setVisible(false);
        
        // Reset styling
        descriptionEditor.setStyle("");
        
        if (description.isEmpty()) {
            if (descriptionError != null) {
                descriptionError.setText("Description is required");
                descriptionError.setVisible(true);
            }
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (description.length() < 10) {
            if (descriptionError != null) {
                descriptionError.setText("Description must be at least 10 characters");
                descriptionError.setVisible(true);
            }
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate name
        isValid = validateName() && isValid;
        
        // Validate description
        isValid = validateDescription() && isValid;
        
        // Validate category
        if (categoryComboBox != null && categoryComboBox.getValue() == null) {
            if (categoryError != null) {
                categoryError.setText("Please select a category");
                categoryError.setVisible(true);
            }
            categoryComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (categoryComboBox != null) {
            categoryComboBox.setStyle("");
            if (categoryError != null) categoryError.setVisible(false);
        }
        
        // Validate status
        if (statusComboBox != null && statusComboBox.getValue() == null) {
            if (statusError != null) {
                statusError.setText("Please select a status");
                statusError.setVisible(true);
            }
            statusComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (statusComboBox != null) {
            statusComboBox.setStyle("");
            if (statusError != null) statusError.setVisible(false);
        }
        
        // Validate start date
        if (dateCompPicker != null && dateCompPicker.getValue() == null) {
            if (startDateError != null) {
                startDateError.setText("Please select a start date");
                startDateError.setVisible(true);
            }
            dateCompPicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (dateCompPicker != null) {
            dateCompPicker.setStyle("");
            if (startDateError != null) startDateError.setVisible(false);
        }
        
        // Validate end date
        if (endDatePicker != null && endDatePicker.getValue() == null) {
            if (endDateError != null) {
                endDateError.setText("Please select an end date");
                endDateError.setVisible(true);
            }
            endDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (dateCompPicker != null && endDatePicker != null && 
                  dateCompPicker.getValue() != null && endDatePicker.getValue() != null &&
                  endDatePicker.getValue().isBefore(dateCompPicker.getValue())) {
            if (endDateError != null) {
                endDateError.setText("End date must be after start date");
                endDateError.setVisible(true);
            }
            endDatePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (endDatePicker != null) {
            endDatePicker.setStyle("");
            if (endDateError != null) endDateError.setVisible(false);
        }
        
        // Validate duration
        if (durationSpinner != null && durationSpinner.getValue() == null) {
            if (durationError != null) {
                durationError.setText("Please specify a duration");
                durationError.setVisible(true);
            }
            durationSpinner.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (durationSpinner != null && (durationSpinner.getValue() < 30 || durationSpinner.getValue() > 480)) {
            if (durationError != null) {
                durationError.setText("Duration must be between 30 and 480 minutes");
                durationError.setVisible(true);
            }
            durationSpinner.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (durationSpinner != null) {
            durationSpinner.setStyle("");
            if (durationError != null) durationError.setVisible(false);
        }
        
        return isValid;
    }
    
    private void resetValidationStyling() {
        if (nameField != null) nameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        if (descriptionEditor != null) descriptionEditor.setStyle("");
        if (categoryComboBox != null) categoryComboBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        if (statusComboBox != null) statusComboBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        if (dateCompPicker != null) dateCompPicker.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        if (endDatePicker != null) endDatePicker.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        if (durationSpinner != null) durationSpinner.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
    }
    
    private void initializeTimeFields() {
        // Set default time values
        LocalTime defaultTime = LocalTime.of(9, 0); // Default to 9:00 AM
        
        // Initialize start time fields
        startHourField.setText(String.format("%02d", defaultTime.getHour()));
        startMinuteField.setText(String.format("%02d", defaultTime.getMinute()));
        
        // Initialize end time fields with default time plus duration
        LocalTime defaultEndTime = defaultTime.plusMinutes(60); // Default to start time + 60 minutes
        endHourField.setText(String.format("%02d", defaultEndTime.getHour()));
        endMinuteField.setText(String.format("%02d", defaultEndTime.getMinute()));
        
        // Add validation for hour fields (0-23)
        addNumericValidation(startHourField, 0, 23);
        addNumericValidation(endHourField, 0, 23);
        
        // Add validation for minute fields (0-59)
        addNumericValidation(startMinuteField, 0, 59);
        addNumericValidation(endMinuteField, 0, 59);
        
        // Add listeners to update end time when duration changes
        if (durationSpinner != null) {
            durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateEndDateTime();
            });
        }
        
        // Add listeners to update end time when start date/time changes
        if (dateCompPicker != null) {
            dateCompPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateEndDateTime();
            });
        }
        
        // Add listeners for time fields to update end time
        startHourField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                updateEndDateTime();
            }
        });
        
        startMinuteField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                updateEndDateTime();
            }
        });
    }
    
    private void addNumericValidation(MFXTextField field, int min, int max) {
        // Restrict input to numeric values only
        field.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("[0-9]")) {
                event.consume();
            }
        });
        
        // Format and validate on focus lost
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // on focus lost
                try {
                    int value = field.getText().isEmpty() ? 0 : Integer.parseInt(field.getText());
                    if (value < min) value = min;
                    if (value > max) value = max;
                    field.setText(String.format("%02d", value));
                } catch (NumberFormatException e) {
                    field.setText(String.format("%02d", min));
                }
            }
        });
    }
    
    private void updateCompetitionFromForm() {
        if (competition == null) {
            competition = new Competition();
        }
        
        // Set basic information
        if (nameField != null) competition.setNom(nameField.getText());
        if (descriptionEditor != null) competition.setDescription(descriptionEditor.getHtmlText());
        if (categoryComboBox != null) competition.setCategorie(categoryComboBox.getValue());
        if (statusComboBox != null) competition.setEtat(statusComboBox.getValue());
        if (durationSpinner != null) competition.setDuration(durationSpinner.getValue());
        
        // Set the start date and time
        if (dateCompPicker != null && dateCompPicker.getValue() != null) {
            LocalTime startTime = getStartTime();
            competition.setDateComp(LocalDateTime.of(dateCompPicker.getValue(), startTime));
        }
        
        // Set the end date and time
        if (endDatePicker != null && endDatePicker.getValue() != null) {
            LocalTime endTime = getEndTime();
            competition.setDateFin(LocalDateTime.of(endDatePicker.getValue(), endTime));
        }
        
        // Set image path if selected - Note: webImageUrl should be already set in handleUploadImage
        if (imageUrlField != null && imageUrlField.getText() != null && !imageUrlField.getText().isEmpty()) {
            competition.setImageUrl(imageUrlField.getText());
            
            // If we don't have a webImageUrl but we do have an imageUrl, we may need to generate a webImageUrl
            if ((competition.getWebImageUrl() == null || competition.getWebImageUrl().isEmpty()) && 
                competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
                // Don't override if it was already set in handleUploadImage
                // This handles the case of loading existing data
                String imageFileName = new File(competition.getImageUrl()).getName();
                String webPath = "http://localhost/competition_images/" + imageFileName;
                competition.setWebImageUrl(webPath);
            }
        } else {
            // Clear both image URLs if image field is empty
            competition.setImageUrl(null);
            competition.setWebImageUrl(null);
        }
        
        // Set default values for other fields
        competition.setCurrentParticipant(0);
        competition.setFreesed(false);
    }
    
    private LocalTime getStartTime() {
        int hour = 0;
        int minute = 0;
        
        try {
            if (startHourField != null && !startHourField.getText().isEmpty()) {
                hour = Integer.parseInt(startHourField.getText());
            }
            if (startMinuteField != null && !startMinuteField.getText().isEmpty()) {
                minute = Integer.parseInt(startMinuteField.getText());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        return LocalTime.of(hour, minute);
    }
    
    private LocalTime getEndTime() {
        int hour = 0;
        int minute = 0;
        
        try {
            if (endHourField != null && !endHourField.getText().isEmpty()) {
                hour = Integer.parseInt(endHourField.getText());
            }
            if (endMinuteField != null && !endMinuteField.getText().isEmpty()) {
                minute = Integer.parseInt(endMinuteField.getText());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        return LocalTime.of(hour, minute);
    }
    
    private void updateEndDateTime() {
        if (dateCompPicker == null || endDatePicker == null || 
            durationSpinner == null || startHourField == null || 
            startMinuteField == null || endHourField == null || 
            endMinuteField == null) return;
        
        LocalDate startDate = dateCompPicker.getValue();
        Integer durationMinutes = durationSpinner.getValue();
        
        if (startDate != null && durationMinutes != null) {
            try {
                int startHour = Integer.parseInt(startHourField.getText());
                int startMinute = Integer.parseInt(startMinuteField.getText());
                
                // Create LocalDateTime with start date and time
                LocalDateTime startDateTime = LocalDateTime.of(
                    startDate, 
                    LocalTime.of(startHour, startMinute)
                );
                
                // Add duration to get end time
                LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
                
                // Update end date picker and time fields
                endDatePicker.setValue(endDateTime.toLocalDate());
                endHourField.setText(String.format("%02d", endDateTime.getHour()));
                endMinuteField.setText(String.format("%02d", endDateTime.getMinute()));
            } catch (NumberFormatException e) {
                // Handle invalid input
                e.printStackTrace();
            }
        }
    }
    
    private void updateEndDate() {
        updateEndDateTime();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void saveCompetition() {
        if (validateForm()) {
            try {
                // Update competition with form data
                updateCompetitionFromForm();
                
                // Save the competition to the database
                try {
                    // Get a database connection
                    Connection connection = DatabaseConnection.getConnection();
                    
                    // Create a CompetitionService with the connection
                    CompetitionService competitionService = new CompetitionService(connection);
                    
                    // Save or update the competition
                    if (competition.getId() > 0) {
                        // Update existing competition
                        competitionService.updateCompetitionWithChallenges(competition, existingChallenges);
                    } else {
                        // Create new competition
                        competition = competitionService.createCompetition(competition, existingChallenges);
                    }
                    
                    // Close the connection
                    connection.close();
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", 
                            "Could not save competition to database", e.getMessage());
                    e.printStackTrace();
                    return;
                }
                
                // Load step 2 form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/competition_step2.fxml"));
                Parent step2Form = loader.load();
                
                // Get the controller and set the competition
                CompetitionStep2Controller controller = loader.getController();
                controller.setCompetition(competition);
                
                // Set up the close handler
                controller.setOnCloseHandler(onCloseHandler);
                
                // Replace the current form with step 2
                if (descriptionEditor != null && descriptionEditor.getScene() != null) {
                    VBox parent = (VBox) descriptionEditor.getScene().getRoot();
                    parent.getChildren().setAll(step2Form);
                }
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not save competition", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void initializeFormValidation() {
        // Hide all error messages initially
        if (nameError != null) nameError.setVisible(false);
        if (descriptionError != null) descriptionError.setVisible(false);
        if (categoryError != null) categoryError.setVisible(false);
        if (statusError != null) statusError.setVisible(false);
        if (startDateError != null) startDateError.setVisible(false);
        if (endDateError != null) endDateError.setVisible(false);
        if (durationError != null) durationError.setVisible(false);
        
        // Add validation listeners for real-time validation
        if (nameField != null) {
            nameField.textProperty().addListener((obs, oldVal, newVal) -> validateName());
        }
        
        if (descriptionEditor != null) {
            descriptionEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateDescription();
                }
            });
        }
        
        if (dateCompPicker != null) {
            dateCompPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateForm();
                updateEndDate();
            });
        }
        
        if (endDatePicker != null) {
            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        }
        
        if (durationSpinner != null) {
            durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateForm();
                updateEndDate();
            });
        }
        
        if (categoryComboBox != null) {
            categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (categoryError != null) categoryError.setVisible(false);
                categoryComboBox.setStyle("");
            });
        }
        
        if (statusComboBox != null) {
            statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (statusError != null) statusError.setVisible(false);
                statusComboBox.setStyle("");
            });
        }
    }
} 