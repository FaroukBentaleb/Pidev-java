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

        // Désactiver le bouton d'ajout par défaut
        addButton.setDisable(true);
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
            if (newValue == null || newValue.isEmpty()) {
                titleErrorLabel.setText("Le titre est obligatoire");
                titleErrorLabel.setVisible(true);
                isTitleValid = false;
            } else if (newValue.length() < 5) {
                titleErrorLabel.setText("Le titre doit contenir au moins 5 caractères");
                titleErrorLabel.setVisible(true);
                isTitleValid = false;
            } else if (newValue.length() > 50) {
                titleErrorLabel.setText("Le titre ne doit pas dépasser 50 caractères");
                titleErrorLabel.setVisible(true);
                isTitleValid = false;
            } else {
                titleErrorLabel.setVisible(false);
                isTitleValid = true;
            }
            updateAddButtonState();
        });

        // Validation de la description
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                descriptionErrorLabel.setText("La description est obligatoire");
                descriptionErrorLabel.setVisible(true);
                isDescriptionValid = false;
            } else {
                descriptionErrorLabel.setVisible(false);
                isDescriptionValid = true;
            }
            updateAddButtonState();
        });

        // Validation de la durée
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.isEmpty()) {
                    durationErrorLabel.setText("La durée est obligatoire");
                    durationErrorLabel.setVisible(true);
                    isDurationValid = false;
                } else {
                    int duration = Integer.parseInt(newValue);
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
            updateAddButtonState();
        });

        // Validation du prix
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.isEmpty()) {
                    priceErrorLabel.setText("Le prix est obligatoire");
                    priceErrorLabel.setVisible(true);
                    isPriceValid = false;
                } else {
                    double price = Double.parseDouble(newValue);
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
            updateAddButtonState();
        });

        // Validation du niveau
        levelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                levelErrorLabel.setText("Veuillez sélectionner un niveau");
                levelErrorLabel.setVisible(true);
                isLevelValid = false;
            } else {
                levelErrorLabel.setVisible(false);
                isLevelValid = true;
            }
            updateAddButtonState();
        });

        // Validation de la catégorie
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                categoryErrorLabel.setText("Veuillez sélectionner une catégorie");
                categoryErrorLabel.setVisible(true);
                isCategoryValid = false;
            } else {
                categoryErrorLabel.setVisible(false);
                isCategoryValid = true;
            }
            updateAddButtonState();
        });
    }

    private void updateAddButtonState() {
        // Activer le bouton uniquement si tous les champs sont valides
        addButton.setDisable(!(isTitleValid && isDescriptionValid && isDurationValid &&
                isPriceValid && isLevelValid && isCategoryValid));
    }

    @FXML
    private void AddCourse(ActionEvent event) {
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

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        durationField.clear();
        priceField.clear();
        levelComboBox.setValue(null);
        categoryComboBox.setValue(null);

        // Réinitialiser l'état des validations
        isTitleValid = false;
        isDescriptionValid = false;
        isDurationValid = false;
        isPriceValid = false;
        isLevelValid = false;
        isCategoryValid = false;

        // Désactiver le bouton d'ajout
        addButton.setDisable(true);
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}