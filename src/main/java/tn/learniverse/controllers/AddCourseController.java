package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.User;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.MailService;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
            Course course = new Course(
                    titleField.getText(),
                    descriptionField.getText(),
                    Integer.parseInt(durationField.getText()),
                    Double.parseDouble(priceField.getText()),
                    levelComboBox.getValue(),
                    categoryComboBox.getValue(),
                    7
            );

            courseService.addCourse(course);

            if (course.getPrice() == 0) {
                sendEmailToStudents(course);
            }

            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès !");
            clearForm();

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout du cours : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendEmailToStudents(Course course) {
        try {
            UserService userService = new UserService();
            List<User> students = userService.getAllStudents();

            for (User student : students) {
                String to = student.getEmail();
                String subject = "New Free Course Available on Learniverse!";

                String body = "<!DOCTYPE html>"
                        + "<html lang='en'>"
                        + "<head>"
                        + "  <meta charset='UTF-8'>"
                        + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "  <title>Learniverse Email</title>"
                        + "</head>"
                        + "<body style='margin: 0; padding: 0; background-color: #f8ebf4;'>"
                        + "  <div style='padding: 20px;'>"
                        + "    <div style='background-color: #fcfafd; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 15px rgba(0,0,0,0.1); max-width: 600px; width: 100%; margin: auto;'>"
                        + "      <h2 style='color: #333; font-size: 26px; margin-bottom: 10px; text-align: center;'>🚀 New Free Course: " + course.getTitle() + "</h2>"
                        + "      <p style='font-size: 16px; color: #555; line-height: 1.5; margin-bottom: 20px; text-align: center;'>"
                        + "        🌟 Here's an exciting opportunity for you! A new free course, <strong>" + course.getTitle() + "</strong>, is now available on <strong>Learniverse.</strong>!"
                        + "      </p>"
                        + "      <p style='font-size: 16px; color: #555; text-align: center;'>Don't miss this chance to enhance your skills! We invite you to check it out now.</p>"
                        + "      <div style='text-align: center; margin-top: 30px;'>"
                        + "        <p style='font-size: 17px; color: #007bff; font-weight: bold;'>"
                        + "          Open the Learniverse app now and explore this free course — and many more exciting opportunities await you!"
                        + "        </p>"
                        + "      </div>"
                        + "      <p style='margin-top: 30px; font-size: 14px; color: #999; text-align: center;'>🙏 Thank you for being part of the <strong>Learniverse</strong> community.</p>"
                        + "      <p style='font-size: 14px; color: #999; text-align: center;'>📩 Feel free to contact us if you need help.</p>"
                        + "      <div style='text-align: center; margin-top: 30px;'>"
                        + "        <img src='https://karimtrabelsi.s3.eu-west-3.amazonaws.com/logo.png' alt='Learniverse' style='width: 100px; margin-top: 10px;'>"
                        + "        <p style='color: #aaa; font-size: 12px; margin-top: 10px;'>🌍 Learniverse | Your Path to Knowledge 📚</p>"
                        + "        <p style='color: #aaa; font-size: 12px;'>© 2025 Learniverse, All rights reserved.</p>"
                        + "      </div>"
                        + "    </div>"
                        + "  </div>"
                        + "</body>"
                        + "</html>";

                MailService.sendMail(to, subject, body);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi des emails aux étudiants : " + e.getMessage());
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