package com.esprit.java.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Utility.RichTextUtils;
import com.esprit.java.Utility.CompetitionViewHelper;

public class ChallengeFormController implements Initializable {

    @FXML
    private Label challengeNumberLabel;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private CustomHtmlEditor descriptionEditor;
    
    @FXML
    private CustomHtmlEditor contentEditor;
    
    @FXML
    private CustomHtmlEditor solutionEditor;
    
    @FXML
    private Label titleError;
    
    @FXML
    private Label descriptionError;
    
    @FXML
    private Label contentError;
    
    @FXML
    private Label solutionError;
    
    @FXML
    private Button previewButton;
    
    private int challengeNumber;
    private CompetitionStep2Controller parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure the HTMLEditors without replacing the FXML instances
        if (descriptionEditor != null) {
            // Configure the editor with initial content
            descriptionEditor.setHtmlText(RichTextUtils.createEmptyHtml("Enter challenge description (min 10 characters)"));
            
            // Make sure the CSS class is applied
            if (!descriptionEditor.getStyleClass().contains("small-html-editor")) {
                descriptionEditor.getStyleClass().add("small-html-editor");
            }
        }
        
        if (contentEditor != null) {
            // Configure the editor with initial content 
            contentEditor.setHtmlText(RichTextUtils.createEmptyHtml("Enter challenge content/question (min 10 characters)"));
            
            // Make sure the CSS class is applied
            if (!contentEditor.getStyleClass().contains("small-html-editor")) {
                contentEditor.getStyleClass().add("small-html-editor");
            }
        }
        
        if (solutionEditor != null) {
            // Configure the editor with initial content
            solutionEditor.setHtmlText(RichTextUtils.createEmptyHtml("Enter challenge solution (min 10 characters)"));
            
            // Make sure the CSS class is applied
            if (!solutionEditor.getStyleClass().contains("small-html-editor")) {
                solutionEditor.getStyleClass().add("small-html-editor");
            }
        }
        
        // Add listeners for real-time validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> validateTitle());
        
        // For HTML editors, we can't easily listen to content changes, so we'll validate on focus change
        if (descriptionEditor != null) {
            descriptionEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) { // validate when focus is lost
                    validateDescription();
                }
            });
        }
        
        if (contentEditor != null) {
            contentEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) { // validate when focus is lost
                    validateContent();
                }
            });
        }
        
        if (solutionEditor != null) {
            solutionEditor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) { // validate when focus is lost
                    validateSolution();
                }
            });
        }
    }
    
    public void setup(int number, CompetitionStep2Controller parentController) {
        this.challengeNumber = number;
        this.parentController = parentController;
        challengeNumberLabel.setText("Challenge #" + number);
    }
    
    @FXML
    void handleRemoveChallenge(ActionEvent event) {
        // Get the parent VBox containing this challenge form
        Node challengeForm = ((Node) event.getSource()).getParent().getParent().getParent();
        
        // Ask the parent controller to remove this challenge
        if (parentController != null) {
            parentController.removeChallenge((VBox) challengeForm, challengeNumber);
        }
    }
    
    @FXML
    void handlePreviewChallenge(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        
        // Create a challenge object from the form data
        Challenge challenge = getChallenge();
        
        // Show the preview
        CompetitionViewHelper.previewChallenge(challenge);
    }
    
    public Challenge getChallenge() {
        Challenge challenge = new Challenge();
        
        challenge.setTitle(titleField.getText().trim());
        challenge.setDescription(descriptionEditor.getHtmlText());
        challenge.setContent(contentEditor.getHtmlText());
        
        if (solutionEditor != null) {
            challenge.setSolution(solutionEditor.getHtmlText());
        }
        
        return challenge;
    }
    
    public boolean validateInputs() {
        boolean titleValid = validateTitle();
        boolean descriptionValid = validateDescription();
        boolean contentValid = validateContent();
        boolean solutionValid = validateSolution();
        
        return titleValid && descriptionValid && contentValid && solutionValid;
    }
    
    private boolean validateTitle() {
        String title = titleField.getText().trim();
        boolean isValid = true;
        
        // Reset styling
        titleField.getStyleClass().remove("error-field");
        
        if (title.isEmpty()) {
            titleError.setText("Title is required");
            titleError.setVisible(true);
            titleField.getStyleClass().add("error-field");
            isValid = false;
        } else if (title.length() > 255) {
            titleError.setText("Title cannot exceed 255 characters");
            titleError.setVisible(true);
            titleField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            titleError.setVisible(false);
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
            descriptionError.setText("Description is required");
            descriptionError.setVisible(true);
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (description.length() < 10) {
            descriptionError.setText("Description must be at least 10 characters");
            descriptionError.setVisible(true);
            descriptionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            descriptionError.setVisible(false);
        }
        
        return isValid;
    }
    
    private boolean validateContent() {
        if (contentEditor == null) return true;
        
        String content = RichTextUtils.extractTextFromHtml(contentEditor.getHtmlText());
        boolean isValid = true;
        
        // Reset styling
        contentEditor.setStyle("");
        
        if (content.isEmpty()) {
            contentError.setText("Content is required");
            contentError.setVisible(true);
            contentEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (content.length() < 10) {
            contentError.setText("Content must be at least 10 characters");
            contentError.setVisible(true);
            contentEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            contentError.setVisible(false);
        }
        
        return isValid;
    }
    
    private boolean validateSolution() {
        if (solutionEditor == null) return true;
        
        String solution = RichTextUtils.extractTextFromHtml(solutionEditor.getHtmlText());
        boolean isValid = true;
        
        // Reset styling
        solutionEditor.setStyle("");
        
        if (solution.isEmpty()) {
            solutionError.setText("Solution is required");
            solutionError.setVisible(true);
            solutionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (solution.length() < 10) {
            solutionError.setText("Solution must be at least 10 characters");
            solutionError.setVisible(true);
            solutionEditor.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            solutionError.setVisible(false);
        }
        
        return isValid;
    }
    
    private void resetValidationStyling() {
        titleField.setStyle("");
        if (descriptionEditor != null) descriptionEditor.setStyle("");
        if (contentEditor != null) contentEditor.setStyle("");
        if (solutionEditor != null) solutionEditor.setStyle("");
        
        titleError.setVisible(false);
        descriptionError.setVisible(false);
        contentError.setVisible(false);
        solutionError.setVisible(false);
    }
    
    // Methods to pre-fill fields (useful for editing)
    public void setTitle(String title) {
        titleField.setText(title);
    }
    
    public void setDescription(String description) {
        if (descriptionEditor != null && description != null && !description.isEmpty()) {
            if (description.startsWith("<html>")) {
                descriptionEditor.setHtmlText(description);
            } else {
                // Convert plain text to HTML
                descriptionEditor.setHtmlText(RichTextUtils.textToHtml(description));
            }
        }
    }
    
    public void setContent(String content) {
        if (contentEditor != null && content != null && !content.isEmpty()) {
            if (content.startsWith("<html>")) {
                contentEditor.setHtmlText(content);
            } else {
                // Convert plain text to HTML
                contentEditor.setHtmlText(RichTextUtils.textToHtml(content));
            }
        }
    }
    
    public void setSolution(String solution) {
        if (solutionEditor != null && solution != null && !solution.isEmpty()) {
            if (solution.startsWith("<html>")) {
                solutionEditor.setHtmlText(solution);
            } else {
                // Convert plain text to HTML
                solutionEditor.setHtmlText(RichTextUtils.textToHtml(solution));
            }
        }
    }
} 