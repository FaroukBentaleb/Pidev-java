package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import tn.learniverse.entities.Course;
import tn.learniverse.services.CourseService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CoursesViewController implements Initializable {

    @FXML
    private VBox coursesContainer;

    private CourseService courseService;

    // Image cache - pour ne charger l'image qu'une seule fois
    private Image courseDefaultImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseService = new CourseService();

        // Chargez l'image au démarrage
        try {

            courseDefaultImage = new Image(getClass().getResourceAsStream("/images/logo.png"));

        } catch (Exception e) {
            System.err.println("Impossible de charger l'image par défaut: " + e.getMessage());
            // Image de secours si la première ne se charge pas
            courseDefaultImage = new Image("https://via.placeholder.com/150?text=Course");
        }

        loadCourses();
    }

    private void loadCourses() {
        coursesContainer.getChildren().clear();

        try {
            List<Course> courses = courseService.getAllCourses();

            if (courses.isEmpty()) {
                Label emptyLabel = new Label("Aucun cours disponible");
                emptyLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 18px;");
                coursesContainer.getChildren().add(emptyLabel);
            } else {
                for (Course course : courses) {
                    coursesContainer.getChildren().add(createCourseItem(course));
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createCourseItem(Course course) {
        // On utilise un HBox au lieu d'un VBox comme conteneur principal
        HBox courseItem = new HBox();
        courseItem.setSpacing(15);
        courseItem.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 20;");

        // Ajout de l'image statique à gauche
        ImageView courseImageView = new ImageView(courseDefaultImage);
        courseImageView.setFitWidth(100);
        courseImageView.setFitHeight(100);
        courseImageView.setPreserveRatio(true);

        // Création d'un VBox pour contenir tous les détails du cours à droite de l'image
        VBox courseDetails = new VBox();
        courseDetails.setSpacing(8);
        HBox.setHgrow(courseDetails, Priority.ALWAYS); // Pour que le VBox prenne tout l'espace disponible à droite

        // Title
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2d3748;");

        // Description
        Label descriptionLabel = new Label(course.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-wrap-text: true;");

        // Additional info (duration, price, level)
        Label infoLabel = new Label(String.format("Duration: %d hours • Price: %.2f DT • Level: %s • Category: %s",
                course.getDuration(), course.getPrice(), course.getLevel(), course.getCategory()));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        // Buttons container
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        // View Details button
        Button viewDetailsButton = new Button("View Course Details");
        viewDetailsButton.setStyle("-fx-background-color: white; -fx-text-fill: #4338ca; -fx-border-color: #4338ca; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> viewCourseDetails(course));

        // Update button
        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: white; -fx-text-fill: #3182ce; -fx-border-color: #3182ce; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        updateButton.setOnAction(e -> updateCourse(course));

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: #e53e3e; -fx-border-color: #e53e3e; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> deleteCourse(course));

        // Add spacing before buttons
        HBox spacer = new HBox();
        spacer.setStyle("-fx-min-width: 10;");
        buttonsBox.getChildren().addAll(spacer, viewDetailsButton, updateButton, deleteButton);

        // Add all elements to the details container
        courseDetails.getChildren().addAll(titleLabel, descriptionLabel, infoLabel, buttonsBox);

        // Add the image and details container to the main HBox
        courseItem.getChildren().addAll(courseImageView, courseDetails);

        return courseItem;
    }

    private void viewCourseDetails(Course course) {
        try {
            // Charger la vue détaillée du cours
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetailsView.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et passer le cours
            CourseDetailsViewController controller = loader.getController();
            controller.setCourse(course);

            // Créer et configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Bloque l'interaction avec la fenêtre parent
            stage.setTitle(course.getTitle() + " - Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'affichage des détails du cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateCourse.fxml"));
            Parent root = loader.load();

            UpdateCourseController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Mise à jour du cours");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload courses after update
            loadCourses();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la page de mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCourse(Course course) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce cours ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    courseService.deleteCourse(course);
                    showAlert(AlertType.INFORMATION, "Succès", "Le cours a été supprimé avec succès !");
                    loadCourses();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression du cours: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddCourse.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un cours");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload courses after adding
            loadCourses();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la page d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeView.fxml"));
            Parent root = loader.load();

            Scene scene = coursesContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToCourses() {
        // Already on courses page, just refresh
        loadCourses();
    }

    @FXML
    private void navigateToAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AboutView.fxml"));
            Parent root = loader.load();

            Scene scene = coursesContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation: " + e.getMessage());
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