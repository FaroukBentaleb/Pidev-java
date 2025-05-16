package tn.learniverse.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.entities.Subscription;
import tn.learniverse.entities.User;
import tn.learniverse.services.LessonService;
import tn.learniverse.services.SubscriptionDAO;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CourseDetailsViewController implements Initializable {

    @FXML
    private ProgressBar courseProgressBar;

    @FXML
    private Label progressNoteLabel;

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
    private Button enrollButton;

    @FXML
    private Button addLessonButton;

    private Course course;
    private Image defaultCourseImage;
    private LessonService lessonService;
    private List<Lesson> courseLessons;
    private VBox expandedLessonContent = null; // Pour suivre quelle leçon est actuellement développée

    @FXML
    private Label lessonCountLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        User currentUser = Session.getCurrentUser();


        System.out.println("Initializing CourseDetailsViewController");

        // Charge l'image par défaut
        try {
            defaultCourseImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image par défaut: " + e.getMessage());
            defaultCourseImage = new Image("https://via.placeholder.com/200x150?text=Course");
        }

        // Initialiser le service des leçons
        lessonService = new LessonService();

        // Configurer le bouton d'ajout de leçon
        if (addLessonButton != null) {
            System.out.println("addLessonButton found, setting action");
            // Le code suivant est commenté car nous utilisons maintenant l'attribut onAction dans le FXML
            // addLessonButton.setOnAction(e -> openAddLessonWindow());
        } else {
            System.out.println("ERROR: addLessonButton is null!");
        }
        if(Session.getCurrentUser().getRole().equals("Student")) {
            enrollButton.setVisible(true);
        }
        else{
            enrollButton.setVisible(false);
        }
        SubscriptionDAO sbs = new SubscriptionDAO();
        if(sbs.existsByUserCourse(Session.getCurrentUser().getId(), course.getId())) {
            enrollButton.setText("✅ Enrolled");
            enrollButton.setDisable(true);
        }
    }

    public void setCourse(Course course) {
        this.course = course;
        updateUI();
        loadLessons();
    }

    private void updateUI() {
        if (course == null) return;

        // Mettre à jour tous les éléments UI avec les données du cours
        courseTitle.setText(course.getTitle());
        courseImage.setImage(defaultCourseImage);
        courseLevel.setText(course.getLevel());
        courseCategory.setText(course.getCategory());
        courseDuration.setText(course.getDuration() + " hours");
        coursePrice.setText(String.format("%.2f DT", course.getPrice()));
        courseDescription.setText(course.getDescription());
    }

    @FXML
    public void openAddLessonWindow() {
        System.out.println("Attempting to open add lesson window");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddLesson.fxml"));
            Parent root = loader.load();

            AddLessonController controller = loader.getController();
            controller.setCourse(course);
            controller.setRefreshCallback(this::loadLessons);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Lesson");
            stage.setScene(new Scene(root));
            System.out.println("Opening add lesson window");
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Add Lesson window", e.getMessage());
        }
    }

    private void loadLessons() {
        try {
            // Vider le conteneur des leçons
            courseContentContainer.getChildren().clear();

            // Récupérer toutes les leçons du cours depuis la base de données
            courseLessons = lessonService.getAllLessons().stream()
                    .filter(lesson -> lesson.getCourse().getId() == course.getId())
                    .toList();

            // Afficher les leçons du cours
            for (Lesson lesson : courseLessons) {
                courseContentContainer.getChildren().add(createLessonAccordionItem(lesson));
            }

            // Afficher un message si aucune leçon n'est disponible
            if (courseLessons.isEmpty()) {
                Label noLessonsLabel = new Label("No lessons available for this course yet.");
                noLessonsLabel.getStyleClass().add("info-label");
                courseContentContainer.getChildren().add(noLessonsLabel);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load lessons", e.getMessage());
        }
    }

    // Création d'un élément accordéon pour chaque leçon
    private VBox createLessonAccordionItem(Lesson lesson) {
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

        // Les infos de la leçon
        VBox lessonInfo = new VBox(5);
        lessonInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lessonInfo, Priority.ALWAYS);

        Label titleLabel = new Label(lesson.getTitle());
        titleLabel.getStyleClass().add("lesson-title");

        Label descLabel = new Label(lesson.getDescription());
        descLabel.getStyleClass().add("lesson-description");
        descLabel.setWrapText(true);

        lessonInfo.getChildren().addAll(titleLabel, descLabel);

        // Les boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton pour éditer la leçon
        Button editButton = createIconButton("Edit");
        editButton.getStyleClass().add("icon-button");
        editButton.setTooltip(new Tooltip("Edit Lesson"));
        editButton.setOnAction(e -> openEditLessonWindow(lesson));

        // Bouton pour supprimer la leçon
        Button deleteButton = createIconButton("Delete");
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setTooltip(new Tooltip("Delete Lesson"));
        deleteButton.setOnAction(e -> deleteLesson(lesson));

        // Bouton pour développer/réduire la leçon
        Button expandButton = createIconButton("⯆");
        expandButton.getStyleClass().add("icon-button");
        expandButton.setTooltip(new Tooltip("Show Content"));

        actions.getChildren().addAll(editButton, deleteButton, expandButton);

        // Assembler l'en-tête
        header.getChildren().addAll(lessonInfo, actions);

        // Contenu détaillé de la leçon (initialement caché)
        VBox content = new VBox(10);
        content.setPadding(new Insets(0, 15, 15, 15));
        content.setMaxHeight(0);
        content.setVisible(false);
        content.getStyleClass().add("lesson-content");

        // Le contenu de la leçon
        TextArea contentText = new TextArea(lesson.getContent());
        contentText.setEditable(false);
        contentText.setWrapText(true);
        contentText.setPrefHeight(200);
        contentText.getStyleClass().add("lesson-content-text");

        content.getChildren().add(contentText);

        // Ajouter l'attachement si présent
        if (lesson.getAttachment() != null && !lesson.getAttachment().isEmpty()) {
            HBox attachmentBox = new HBox(10);
            attachmentBox.setAlignment(Pos.CENTER_LEFT);

            Label attachmentLabel = new Label("Attachment: ");
            attachmentLabel.getStyleClass().add("attachment-label");

            Hyperlink attachmentLink = new Hyperlink(lesson.getAttachment());
            attachmentLink.getStyleClass().add("attachment-link");
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

    private Button createIconButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("icon-button");
        return button;
    }

    // Fonction pour développer/réduire le contenu de la leçon
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

    // cette méthode pour ouvrir le formulaire d'édition
    private void openEditLessonWindow(Lesson lesson) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditLesson.fxml"));
            Parent root = loader.load();

            EditLessonController controller = loader.getController();
            controller.setLesson(lesson);
            controller.setRefreshCallback(this::loadLessons);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Lesson");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Edit Lesson window", e.getMessage());
        }
    }

    private void deleteLesson(Lesson lesson) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Lesson");
        confirmAlert.setHeaderText("Delete " + lesson.getTitle());
        confirmAlert.setContentText("Are you sure you want to delete this lesson? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                lessonService.deleteLesson(lesson);
                loadLessons(); // Recharger la liste des leçons
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete lesson", e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleEnroll() {
        SubscriptionDAO SubscriptionService = new SubscriptionDAO();
        Subscription sbs = new Subscription();
        sbs.setCourseId(course.getId());
        sbs.setUserId(Session.getCurrentUser().getId());
        sbs.setDateEarned(LocalDate.now().atStartOfDay());
        sbs.setDurationInDays(90);
        System.out.println("in");
        SubscriptionService.create(sbs);
        Navigator.showAlert(Alert.AlertType.INFORMATION,"Subscription Done","Your subscription was Done successfully!");
    }

    @FXML
    private void closeWindow() {
        // Fermer la fenêtre
        Stage stage = (Stage) courseTitle.getScene().getWindow();
        stage.close();
    }
}