package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import tn.learniverse.entities.Course;
import tn.learniverse.services.CourseService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UpdateCourseController implements Initializable {

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
    private Button updateButton;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    @FXML
    private Label durationErrorLabel;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private Label levelErrorLabel;

    @FXML
    private Label categoryErrorLabel;

    private CourseService courseService;
    private Course currentCourse;

    // Flags pour vérifier si tous les champs sont valides
    private boolean isTitleValid = false;
    private boolean isDescriptionValid = false;
    private boolean isDurationValid = false;
    private boolean isPriceValid = false;
    private boolean isLevelValid = false;
    private boolean isCategoryValid = false;

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

        // Initialiser les labels d'erreur (masqués au départ)
        initializeErrorLabels();

        // Configurer les listeners pour la validation en temps réel
        setupValidationListeners();

        // Désactiver le bouton de mise à jour par défaut
        updateButton.setDisable(true);
    }

    public void setCourse(Course course) {
        this.currentCourse = course;
        populateFields();
    }

    private void populateFields() {
        if (currentCourse != null) {
            titleField.setText(currentCourse.getTitle());
            descriptionField.setText(currentCourse.getDescription());
            durationField.setText(String.valueOf(currentCourse.getDuration()));
            priceField.setText(String.valueOf(currentCourse.getPrice()));
            levelComboBox.setValue(currentCourse.getLevel());
            categoryComboBox.setValue(currentCourse.getCategory());

            // Déclenchement manuel des validations pour mettre à jour l'état du bouton
            validateTitle(currentCourse.getTitle());
            validateDescription(currentCourse.getDescription());
            validateDuration(String.valueOf(currentCourse.getDuration()));
            validatePrice(String.valueOf(currentCourse.getPrice()));
            validateLevel(currentCourse.getLevel());
            validateCategory(currentCourse.getCategory());

            updateUpdateButtonState();
        }
    }

    private void initializeErrorLabels() {
        // Configurer le style des labels d'erreur
        titleErrorLabel.setTextFill(Color.RED);
        descriptionErrorLabel.setTextFill(Color.RED);
        durationErrorLabel.setTextFill(Color.RED);
        priceErrorLabel.setTextFill(Color.RED);
        levelErrorLabel.setTextFill(Color.RED);
        categoryErrorLabel.setTextFill(Color.RED);

        // Masquer les labels d'erreur initialement
        titleErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);
        durationErrorLabel.setVisible(false);
        priceErrorLabel.setVisible(false);
        levelErrorLabel.setVisible(false);
        categoryErrorLabel.setVisible(false);
    }

    private void setupValidationListeners() {
        // Validation du titre
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitle(newValue);
        });

        // Validation de la description
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescription(newValue);
        });

        // Validation de la durée
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDuration(newValue);
        });

        // Validation du prix
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePrice(newValue);
        });

        // Validation du niveau
        levelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateLevel(newValue);
        });

        // Validation de la catégorie
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateCategory(newValue);
        });
    }

    private void validateTitle(String value) {
        if (value == null || value.isEmpty()) {
            titleErrorLabel.setText("Le titre est obligatoire");
            titleErrorLabel.setVisible(true);
            isTitleValid = false;
        } else if (value.length() < 5) {
            titleErrorLabel.setText("Le titre doit contenir au moins 5 caractères");
            titleErrorLabel.setVisible(true);
            isTitleValid = false;
        } else if (value.length() > 50) {
            titleErrorLabel.setText("Le titre ne doit pas dépasser 50 caractères");
            titleErrorLabel.setVisible(true);
            isTitleValid = false;
        } else {
            titleErrorLabel.setVisible(false);
            isTitleValid = true;
        }
        updateUpdateButtonState();
    }

    private void validateDescription(String value) {
        if (value == null || value.isEmpty()) {
            descriptionErrorLabel.setText("La description est obligatoire");
            descriptionErrorLabel.setVisible(true);
            isDescriptionValid = false;
        } else {
            descriptionErrorLabel.setVisible(false);
            isDescriptionValid = true;
        }
        updateUpdateButtonState();
    }

    private void validateDuration(String value) {
        try {
            if (value == null || value.isEmpty()) {
                durationErrorLabel.setText("La durée est obligatoire");
                durationErrorLabel.setVisible(true);
                isDurationValid = false;
            } else {
                int duration = Integer.parseInt(value);
                if (duration < 1) {
                    durationErrorLabel.setText("La durée doit être supérieure à 0");
                    durationErrorLabel.setVisible(true);
                    isDurationValid = false;
                } else {
                    durationErrorLabel.setVisible(false);
                    isDurationValid = true;
                }
            }
        } catch (NumberFormatException e) {
            durationErrorLabel.setText("La durée doit être un nombre entier valide");
            durationErrorLabel.setVisible(true);
            isDurationValid = false;
        }
        updateUpdateButtonState();
    }

    private void validatePrice(String value) {
        try {
            if (value == null || value.isEmpty()) {
                priceErrorLabel.setText("Le prix est obligatoire");
                priceErrorLabel.setVisible(true);
                isPriceValid = false;
            } else {
                double price = Double.parseDouble(value);
                if (price < 0) {
                    priceErrorLabel.setText("Le prix doit être un nombre positif");
                    priceErrorLabel.setVisible(true);
                    isPriceValid = false;
                } else {
                    priceErrorLabel.setVisible(false);
                    isPriceValid = true;
                }
            }
        } catch (NumberFormatException e) {
            priceErrorLabel.setText("Le prix doit être un nombre valide");
            priceErrorLabel.setVisible(true);
            isPriceValid = false;
        }
        updateUpdateButtonState();
    }

    private void validateLevel(String value) {
        if (value == null) {
            levelErrorLabel.setText("Veuillez sélectionner un niveau");
            levelErrorLabel.setVisible(true);
            isLevelValid = false;
        } else {
            levelErrorLabel.setVisible(false);
            isLevelValid = true;
        }
        updateUpdateButtonState();
    }

    private void validateCategory(String value) {
        if (value == null) {
            categoryErrorLabel.setText("Veuillez sélectionner une catégorie");
            categoryErrorLabel.setVisible(true);
            isCategoryValid = false;
        } else {
            categoryErrorLabel.setVisible(false);
            isCategoryValid = true;
        }
        updateUpdateButtonState();
    }

    private void updateUpdateButtonState() {
        // Activer le bouton uniquement si tous les champs sont valides
        updateButton.setDisable(!(isTitleValid && isDescriptionValid && isDurationValid &&
                isPriceValid && isLevelValid && isCategoryValid));
    }

    @FXML
    private void UpdateCourse(ActionEvent event) {
        try {
            if (currentCourse == null) {
                showAlert(AlertType.ERROR, "Erreur", "Aucun cours à mettre à jour");
                return;
            }

            // Update course with form data
            currentCourse.setTitle(titleField.getText());
            currentCourse.setDescription(descriptionField.getText());
            currentCourse.setDuration(Integer.parseInt(durationField.getText()));
            currentCourse.setPrice(Double.parseDouble(priceField.getText()));
            currentCourse.setLevel(levelComboBox.getValue());
            currentCourse.setCategory(categoryComboBox.getValue());

            // Update course in database
            courseService.updateCourse(currentCourse);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été mis à jour avec succès !");

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour du cours : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}