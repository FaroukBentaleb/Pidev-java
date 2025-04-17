package tn.learniverse.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CoursesViewController implements Initializable {

    @FXML
    private VBox coursesContainer;

    @FXML
    private VBox courseDetailsView;

    @FXML
    private VBox coursesListView;

    @FXML
    private Label courseTitle;

    @FXML
    private ImageView courseImage;

    @FXML
    private Label courseLevel;

    @FXML
    private Label courseCategory;

    @FXML
    private Label courseDuration;

    @FXML
    private Label coursePrice;

    @FXML
    private Label courseDescription;

    @FXML
    private VBox courseContentContainer;

    @FXML
    private Button addLessonButton;

    @FXML
    private Button enrollButton;

    @FXML
    private Button addCourseButton;

    private CourseService courseService;
    private LessonService lessonService;
    private Course currentCourse;
    private VBox expandedLessonContent = null; // Pour suivre quelle leçon est actuellement développée

    // Image cache - pour ne charger l'image qu'une seule fois
    private Image courseDefaultImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseService = new CourseService();
        lessonService = new LessonService();

        // Chargez l'image au démarrage
        try {
            courseDefaultImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image par défaut: " + e.getMessage());
            // Image de secours si la première ne se charge pas
            courseDefaultImage = new Image("https://via.placeholder.com/150?text=Course");
        }

        // Make sure the details view is hidden initially
        if (courseDetailsView != null) {
            courseDetailsView.setVisible(false);
            courseDetailsView.setManaged(false);
        }

        loadCourses();
    }

    private void loadCourses() {
        coursesContainer.getChildren().clear();

        try {
            // Replace getAllCourses() with getVisibleCourses()
            List<Course> courses = courseService.getVisibleCourses();

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
            courseTitle.setText(course.getTitle());
            courseLevel.setText("Level: " + course.getLevel());
            courseCategory.setText("Category: " + course.getCategory());
            courseDuration.setText("Duration: " + course.getDuration() + " hours");
            coursePrice.setText("Price: " + String.format("%.2f DT", course.getPrice()));
            courseDescription.setText(course.getDescription());

            courseImage.setImage(courseDefaultImage);

            loadLessonsForCourse(course);

            coursesListView.setVisible(false);
            coursesListView.setManaged(false);
            courseDetailsView.setVisible(true);
            courseDetailsView.setManaged(true);

            // Hide the Add Course button
            if (addCourseButton != null) {
                addCourseButton.setVisible(false);
                addCourseButton.setManaged(false);
            }

            this.currentCourse = course;

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Error displaying course details: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void backToCoursesList() {
        coursesListView.setVisible(true);
        coursesListView.setManaged(true);
        courseDetailsView.setVisible(false);
        courseDetailsView.setManaged(false);

        // Show the Add Course button again
        if (addCourseButton != null) {
            addCourseButton.setVisible(true);
            addCourseButton.setManaged(true);
        }
    }

    private void loadLessonsForCourse(Course course) {
        // Clear previous lessons
        courseContentContainer.getChildren().clear();

        try {
            // Get lessons for the course
            List<Lesson> lessons = lessonService.getAllLessons().stream()
                    .filter(lesson -> lesson.getCourse().getId() == course.getId())
                    .toList();

            if (lessons.isEmpty()) {
                Label noLessonsLabel = new Label("No lessons available for this course yet.");
                noLessonsLabel.getStyleClass().add("info-label");
                courseContentContainer.getChildren().add(noLessonsLabel);
            } else {
                // Create lesson UI elements for each lesson
                for (Lesson lesson : lessons) {
                    courseContentContainer.getChildren().add(createLessonItem(lesson));
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load lessons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createLessonItem(Lesson lesson) {
        // Conteneur principal pour toute la leçon
        VBox lessonItem = new VBox();
        lessonItem.setSpacing(0);
        lessonItem.getStyleClass().add("lesson-item");

        // En-tête de la leçon (toujours visible)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.getStyleClass().add("lesson-header");
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");

        // Les infos de la leçon
        VBox lessonInfo = new VBox(5);
        lessonInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lessonInfo, Priority.ALWAYS);

        Label titleLabel = new Label(lesson.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label descLabel = new Label(lesson.getDescription());
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        descLabel.setWrapText(true);

        lessonInfo.getChildren().addAll(titleLabel, descLabel);

        // Les boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton pour éditer la leçon
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3182ce; -fx-border-color: #3182ce; -fx-border-radius: 15; -fx-background-radius: 15;");
        editButton.setOnAction(e -> openEditLessonWindow(lesson));

        // Bouton pour supprimer la leçon
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e53e3e; -fx-border-color: #e53e3e; -fx-border-radius: 15; -fx-background-radius: 15;");
        deleteButton.setOnAction(e -> deleteLesson(lesson));

        // Bouton pour développer/réduire la leçon
        Button expandButton = new Button("⯆");
        expandButton.setStyle("-fx-background-color: #f7fafc; -fx-text-fill: #4a5568; -fx-border-color: #cbd5e0; -fx-border-radius: 15; -fx-background-radius: 15;");

        actions.getChildren().addAll(editButton, deleteButton, expandButton);

        // Assembler l'en-tête
        header.getChildren().addAll(lessonInfo, actions);

        // Contenu détaillé de la leçon (initialement caché)
        VBox content = new VBox(10);
        content.setPadding(new Insets(0, 15, 15, 15));
        content.setMaxHeight(0);
        content.setVisible(false);
        content.setStyle("-fx-background-color: #f9fafc; -fx-background-radius: 0 0 8 8; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 8 8;");

        // Le contenu de la leçon
        TextArea contentText = new TextArea(lesson.getContent());
        contentText.setEditable(false);
        contentText.setWrapText(true);
        contentText.setPrefHeight(200);
        contentText.setStyle("-fx-control-inner-background: #f9fafc; -fx-background-radius: 4;");

        content.getChildren().add(contentText);

        // Ajouter l'attachement si présent
        if (lesson.getAttachment() != null && !lesson.getAttachment().isEmpty()) {
            HBox attachmentBox = new HBox(10);
            attachmentBox.setAlignment(Pos.CENTER_LEFT);

            Label attachmentLabel = new Label("Attachment: ");
            attachmentLabel.setStyle("-fx-font-weight: bold;");

            Hyperlink attachmentLink = new Hyperlink(lesson.getAttachment());
            attachmentLink.setStyle("-fx-text-fill: #4338ca;");
            attachmentLink.setOnAction(e -> {
                // Logique pour ouvrir l'attachement
            });

            attachmentBox.getChildren().addAll(attachmentLabel, attachmentLink);
            content.getChildren().add(attachmentBox);
        }

        // Ajouter l'en-tête et le contenu au conteneur principal
        lessonItem.getChildren().addAll(header, content);

        // Configurer le comportement d'accordéon
        expandButton.setOnAction(e -> toggleLessonContent(content, expandButton));

        return lessonItem;
    }

    private void toggleLessonContent(VBox content, Button expandButton) {
        if (expandedLessonContent != null && expandedLessonContent != content) {
            expandedLessonContent.setVisible(false);
            expandedLessonContent.setMaxHeight(0);
            Button prevExpandButton = findExpandButtonForContent(expandedLessonContent);
            if (prevExpandButton != null) prevExpandButton.setText("⯆");
        }

        boolean isExpanded = content.isVisible();
        if (isExpanded) {
            content.setVisible(false);
            content.setMaxHeight(0);
            expandButton.setText("⯆");
            expandedLessonContent = null;
        } else {
            content.setVisible(true);
            content.setMaxHeight(Double.MAX_VALUE);
            expandButton.setText("⯅");
            expandedLessonContent = content;

            // Animation de slide
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), content);
            tt.setFromY(-10);
            tt.setToY(0);
            tt.play();
        }
    }

    private Button findExpandButtonForContent(VBox content) {
        // Cette fonction trouve le bouton d'expansion associé à un contenu donné
        for (int i = 0; i < courseContentContainer.getChildren().size(); i++) {
            if (courseContentContainer.getChildren().get(i) instanceof VBox lessonItem) {
                if (lessonItem.getChildren().size() >= 2 && lessonItem.getChildren().get(1) == content) {
                    // Trouver le bouton d'expansion dans l'en-tête
                    if (lessonItem.getChildren().get(0) instanceof HBox header) {
                        if (header.getChildren().size() >= 2 && header.getChildren().get(1) instanceof HBox actions) {
                            // Le bouton d'expansion est généralement le dernier
                            if (!actions.getChildren().isEmpty()) {
                                return (Button) actions.getChildren().get(actions.getChildren().size() - 1);
                            }
                        }
                    }
                }
            }
        }
        return null;
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

            // If we're in the details view and the updated course is the current one,
            // refresh the details view
            if (courseDetailsView.isVisible() && currentCourse != null && currentCourse.getId() == course.getId()) {
                viewCourseDetails(course);
            }
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
            if (response == ButtonType.OK) {
                try {
                    courseService.deleteCourse(course);
                    showAlert(AlertType.INFORMATION, "Succès", "Le cours a été supprimé avec succès !");

                    // If we're in the details view and the deleted course is the current one,
                    // go back to the courses list
                    if (courseDetailsView.isVisible() && currentCourse != null && currentCourse.getId() == course.getId()) {
                        backToCoursesList();
                    }

                    loadCourses();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression du cours: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void openEditLessonWindow(Lesson lesson) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditLesson.fxml"));
            Parent root = loader.load();

            EditLessonController controller = loader.getController();
            controller.setLesson(lesson);
            controller.setRefreshCallback(() -> loadLessonsForCourse(currentCourse));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Lesson");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Could not open Edit Lesson window");
        }
    }

    private void deleteLesson(Lesson lesson) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Lesson");
        confirmAlert.setHeaderText("Delete " + lesson.getTitle());
        confirmAlert.setContentText("Are you sure you want to delete this lesson? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                lessonService.deleteLesson(lesson);
                loadLessonsForCourse(currentCourse); // Recharger la liste des leçons
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Error", "Failed to delete lesson");
            }
        }
    }

    @FXML
    private void openAddLessonWindow() {
        if (currentCourse == null) {
            showAlert(AlertType.WARNING, "Warning", "No course selected");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddLesson.fxml"));
            Parent root = loader.load();

            AddLessonController controller = loader.getController();
            controller.setCourse(currentCourse);
            controller.setRefreshCallback(() -> loadLessonsForCourse(currentCourse));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Lesson");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Could not open Add Lesson window");
        }
    }

    @FXML
    private void handleEnroll() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Enrollment");
        alert.setHeaderText("Thank you for your interest!");
        alert.setContentText("Enrollment feature will be available soon.");
        alert.showAndWait();
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