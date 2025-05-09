package tn.learniverse.controllers;

import tn.learniverse.services.SubscriptionDAO;
import tn.learniverse.entities.Subscription;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SubscriptionFormController {
    @FXML
    private TextField courseIdField;
    @FXML
    private DatePicker dateEarnedPicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField userIdField;
    @FXML
    private TextField offreIdField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private SubscriptionDAO subscriptionDAO;
    private Subscription subscription;

    @FXML
    public void initialize() {
        subscriptionDAO = new SubscriptionDAO();
        setupEventHandlers();
        initializeStatusComboBox();
    }

    private void setupEventHandlers() {
        saveButton.setOnAction(event -> saveSubscription());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void initializeStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList("active", "inactive", "pending", "expired"));
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        if (subscription != null) {
            // Populate form with existing subscription data
            if (subscription.getCourseId() > 0) {
                courseIdField.setText(String.valueOf(subscription.getCourseId()));
            }
            dateEarnedPicker.setValue(subscription.getDateEarned().toLocalDate());
            statusComboBox.setValue(subscription.getStatus());
            if (subscription.getUserId() > 0) {
                userIdField.setText(String.valueOf(subscription.getUserId()));
            }
            if (subscription.getOffreId() > 0) {
                offreIdField.setText(String.valueOf(subscription.getOffreId()));
            }
        } else {
            // Set default values for new subscription
            dateEarnedPicker.setValue(LocalDate.now());
            statusComboBox.setValue("active");
        }
    }

    private void saveSubscription() {
        try {
            if (subscription == null) {
                subscription = new Subscription();
            }

            // Validate required fields
            if (statusComboBox.getValue() == null || dateEarnedPicker.getValue() == null) {
                showAlert("Please fill in all required fields.");
                return;
            }

            // Set subscription properties
            if (!courseIdField.getText().isEmpty()) {
                subscription.setCourseId(Integer.parseInt(courseIdField.getText()));
            }
            subscription.setDateEarned(LocalDateTime.of(dateEarnedPicker.getValue(), LocalTime.now()));
            subscription.setStatus(statusComboBox.getValue());
            if (!userIdField.getText().isEmpty()) {
                subscription.setUserId(Integer.parseInt(userIdField.getText()));
            }
            if (!offreIdField.getText().isEmpty()) {
                subscription.setOffreId(Integer.parseInt(offreIdField.getText()));
            }

            // Save the subscription
            if (subscription.getId() == 0) {
                subscriptionDAO.create(subscription);
            } else {
                subscriptionDAO.update(subscription);
            }

            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers for course ID, user ID, and offer ID.");
        } catch (Exception e) {
            showAlert("Error saving subscription: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 