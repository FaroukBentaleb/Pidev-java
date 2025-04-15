package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import tn.learniverse.entities.Course;
import tn.learniverse.services.CourseService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddCourseController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<String> levelComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button addButton;

    private CourseService courseService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the service
        courseService = new CourseService();

        // Setup ComboBox for level
        ObservableList<String> levelOptions = FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced"
        );
        levelComboBox.setItems(levelOptions);

        // Setup ComboBox for category
        ObservableList<String> categoryOptions = FXCollections.observableArrayList(
                "Health & Wellness",
                "Lifestyle & Productivity",
                "Business & Finance",
                "Digital Marketing",
                "User Research & Analytics",
                "Creative Art",
                "Writing & Communication",
                "Tech & Programming"
        );
        categoryComboBox.setItems(categoryOptions);
    }

    @FXML
    private void AddCourse(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Create new course from form data
                Course course = new Course(
                        titleField.getText(),
                        descriptionField.getText(),
                        Integer.parseInt(durationField.getText()),
                        Double.parseDouble(priceField.getText()),
                        levelComboBox.getValue(),
                        categoryComboBox.getValue(),
                        // In the service, the user ID is hardcoded to 7
                        7
                );

                // Add course to database
                courseService.addCourse(course);

                // Show success message
                showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès !");

                // Clear form
                clearForm();

            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout du cours : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate title
        if (titleField.getText().isEmpty() || titleField.getText().length() < 5 || titleField.getText().length() > 50) {
            errorMessage.append("Le titre est obligatoire et doit être entre 5 et 50 caractères.\n");
        }

        // Validate description
        if (descriptionField.getText().isEmpty()) {
            errorMessage.append("La description est obligatoire.\n");
        }

        // Validate duration
        try {
            int duration = Integer.parseInt(durationField.getText());
            if (duration < 1) {
                errorMessage.append("La durée doit être supérieure à 0.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("La durée doit être un nombre entier valide.\n");
        }

        // Validate price
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                errorMessage.append("Le prix doit être un nombre positif.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Le prix doit être un nombre valide.\n");
        }

        // Validate level
        if (levelComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner un niveau.\n");
        }

        // Validate category
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner une catégorie.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        durationField.clear();
        priceField.clear();
        levelComboBox.setValue(null);
        categoryComboBox.setValue(null);
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}