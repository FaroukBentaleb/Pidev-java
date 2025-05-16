package tn.learniverse.controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.entities.User;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.learniverse.services.LogsService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;


public class PurchasedCourses implements Initializable {

    public ImageView UserPicture;
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
    private VBox expandedLessonContent = null;
    private List<Course> allCourses;
    private int currentUserId ;

    private Image courseDefaultImage;

    private List<Course> recommendedCourses = new ArrayList<>();

    private static final String YOUTUBE_API_KEY = "AIzaSyAo0VLftJOdBmw9hlnjXa44WGnTgG2SRwg";
    private HttpClient httpClient;
    public Button Settingsbtn;
    public Label role;
    public Label usernameLabel;
    public Button logoutButton;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSessionMonitor();
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/settings.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Settingsbtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            if(Session.getCurrentUser()==null){
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event,"/fxml/user/Login.fxml");
            }
            else{
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if(image.isError()){
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        courseService = new CourseService();
        lessonService = new LessonService();

        httpClient = HttpClient.newHttpClient();

        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
            System.out.println("Current user ID: " + currentUserId);
        }

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

        // Initialize the category filter
        initializeCategoryFilter();

        // Load all courses
        loadAllCourses();

        // Style pour le bouton Enroll
        if (enrollButton != null) {
            enrollButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
            enrollButton.setOnMouseEntered(e -> enrollButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;"));
            enrollButton.setOnMouseExited(e -> enrollButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;"));
        }

        // Style pour le bouton Add Lesson
        if (addLessonButton != null) {
            addLessonButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
            addLessonButton.setOnMouseEntered(e -> addLessonButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;"));
            addLessonButton.setOnMouseExited(e -> addLessonButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;"));
        }
    }

    // Méthode améliorée pour créer un élément vidéo
    private HBox createVideoItem(YouTubeVideo video, boolean fullSize) {
        HBox videoItem = new HBox();
        videoItem.setSpacing(15);
        videoItem.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-padding: 15;");

        // Largeur de la vidéo en fonction du contexte
        int videoWidth = 320;
        int videoHeight = 180;

        // WebView pour afficher la vidéo YouTube
        WebView webView = new WebView();
        webView.setPrefSize(videoWidth, videoHeight);
        webView.setMaxSize(videoWidth, videoHeight);
        webView.setMinSize(videoWidth, videoHeight);

        // Utilisation de l'API iframe YouTube
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(
                "<html><body style='margin:0;padding:0;overflow:hidden;'>" +
                        "<iframe width='" + videoWidth + "' height='" + videoHeight + "' " +
                        "src='https://www.youtube.com/embed/" + video.getId() + "?rel=0' " +
                        "frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' " +
                        "allowfullscreen></iframe>" +
                        "</body></html>", "text/html");

        // Détails de la vidéo
        VBox videoDetails = new VBox();
        videoDetails.setSpacing(8);
        HBox.setHgrow(videoDetails, Priority.ALWAYS);

        // Titre de la vidéo
        Label titleLabel = new Label(video.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2d3748;");
        titleLabel.setWrapText(true);

        // Chaîne avec icône
        HBox channelBox = new HBox(8);
        channelBox.setAlignment(Pos.CENTER_LEFT);

        Circle channelIcon = new Circle(8, Color.RED);
        Label channelLabel = new Label(video.getChannelTitle());
        channelLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        channelBox.getChildren().addAll(channelIcon, channelLabel);

        // Description
        Label descriptionLabel = new Label(video.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        descriptionLabel.setWrapText(true);

        // Bouton pour ouvrir dans YouTube
        Button openInYouTubeButton = new Button("View on YouTube");
        openInYouTubeButton.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
        openInYouTubeButton.setOnAction(e -> {
            try {
                // Ouvrir le navigateur avec l'URL de la vidéo
                String url = "https://www.youtube.com/watch?v=" + video.getId();
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Ajouter une marge en haut pour le bouton
        VBox.setMargin(openInYouTubeButton, new Insets(10, 0, 0, 0));

        videoDetails.getChildren().addAll(titleLabel, channelBox, descriptionLabel, openInYouTubeButton);

        // Ajouter un effet de survol
        videoItem.setOnMouseEntered(e ->
                videoItem.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3); -fx-padding: 15;")
        );

        videoItem.setOnMouseExited(e ->
                videoItem.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-padding: 15;")
        );

        // Ajout à l'item
        videoItem.getChildren().addAll(webView, videoDetails);

        return videoItem;
    }


    private void initializeCategoryFilter() {
        try {
            List<Course> courses = courseService.getVisibleCoursesForInstructor(Session.getCurrentUser().getId());
            Set<String> categories = new HashSet<>();

            for (Course course : courses) {
                if (course.getCategory() != null && !course.getCategory().isEmpty()) {
                    categories.add(course.getCategory());
                }
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadAllCourses() {
        try {
            allCourses = courseService.getVisibleCoursesForInstructor(Session.getCurrentUser().getId());
            displayFilteredCourses(allCourses);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFilteredCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyLabel = new Label("No created courses yet ");
            emptyLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 18px;");
            coursesContainer.getChildren().add(emptyLabel);
        } else {
            for (Course course : courses) {
                coursesContainer.getChildren().add(createCourseItem(course));
            }
        }
    }

    private HBox createCourseItem(Course course) {
        HBox courseItem = new HBox();
        courseItem.setSpacing(15);
        courseItem.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 20;");

        // Image à gauche
        ImageView courseImageView = new ImageView(courseDefaultImage);
        courseImageView.setFitWidth(120);
        courseImageView.setFitHeight(120);
        courseImageView.setPreserveRatio(true);
        courseImageView.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10;");

        // Détails du cours
        VBox courseDetails = new VBox();
        courseDetails.setSpacing(10);
        HBox.setHgrow(courseDetails, Priority.ALWAYS);

        // Label du titre
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #1e293b; -fx-padding: 0 0 5 0;");


        // HBox contenant le titre à gauche et le cœur à droite
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(10);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleLabel);

        // Description
        Label descriptionLabel = new Label(course.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-padding: 5 0;");
        descriptionLabel.setWrapText(true);

        // Infos supplémentaires
        Label infoLabel = new Label(String.format("Duration: %d hours • Price: %.2f DT • Level: %s • Category: %s",
                course.getDuration(), course.getPrice(), course.getLevel(), course.getCategory()));
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-padding: 5 0;");

        // Boutons d'action
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button viewDetailsButton = new Button("View Course Details");
        viewDetailsButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> viewCourseDetails(course));

        Button updateButton = createIconButton(
                "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                "Update Course",
                "update-icon-button"
        );
        updateButton.setStyle("-fx-background-color: #10b981; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        updateButton.setOnMouseEntered(e -> updateButton.setStyle("-fx-background-color: #059669; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        updateButton.setOnMouseExited(e -> updateButton.setStyle("-fx-background-color: #10b981; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        updateButton.setOnAction(e -> updateCourse(course));

        Button deleteButton = createIconButton(
                "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
                "Delete Course",
                "delete-icon-button"
        );
        deleteButton.setStyle("-fx-background-color: #ef4444; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #dc2626; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #ef4444; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        deleteButton.setOnAction(e -> deleteCourse(course));

        buttonsBox.getChildren().addAll(viewDetailsButton, updateButton, deleteButton);

        // Ajout des composants
        courseDetails.getChildren().addAll(titleRow, descriptionLabel, infoLabel, buttonsBox);
        courseItem.getChildren().addAll(courseImageView, courseDetails);

        // Effet de survol
        courseItem.setOnMouseEntered(e ->
                courseItem.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 7); -fx-padding: 20;")
        );

        courseItem.setOnMouseExited(e ->
                courseItem.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 20;")
        );

        return courseItem;
    }

    private void viewCourseDetails(Course course) {
        try {
            this.currentCourse = course;

            // Mettre à jour le texte du Label existant (NE PAS le remplacer)
            courseTitle.setText(course.getTitle());
            courseTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #2d3748;");

            // Nettoyer l'ancien coeur (s'il existe)
            if (courseTitle.getParent() instanceof HBox container) {
                container.getChildren().removeIf(node -> node instanceof Button);
            }

            // Mettre à jour les autres champs
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

            if (addCourseButton != null) {
                addCourseButton.setVisible(false);
                addCourseButton.setManaged(false);
            }

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Error displaying course details: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void backToCoursesList() {
        // Restaurer le label du titre original
        if (courseTitle.getParent() instanceof VBox) {
            VBox parent = (VBox) courseTitle.getParent();
            for (int i = 0; i < parent.getChildren().size(); i++) {
                if (parent.getChildren().get(i) instanceof HBox &&
                        ((HBox) parent.getChildren().get(i)).getChildren().get(0) instanceof Label) {
                    parent.getChildren().set(i, courseTitle);
                    break;
                }
            }
        }

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
        courseContentContainer.getChildren().clear();

        try {
            List<Lesson> lessons = lessonService.getAllLessons().stream()
                    .filter(lesson -> lesson.getCourse().getId() == course.getId())
                    .toList();

            if (lessons.isEmpty()) {
                Label noLessonsLabel = new Label("No lessons available for this course yet.");
                noLessonsLabel.getStyleClass().add("info-label");
                courseContentContainer.getChildren().add(noLessonsLabel);
            } else {
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
        VBox lessonItem = new VBox();
        lessonItem.setSpacing(0);
        lessonItem.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-margin: 10;");

        // En-tête de la leçon avec style moderne
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setSpacing(15);
        header.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 0 0;");

        // Icône de la leçon
        SVGPath lessonIcon = new SVGPath();
        lessonIcon.setContent("M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5");
        lessonIcon.setFill(Color.web("#3b82f6"));
        lessonIcon.setScaleX(1.2);
        lessonIcon.setScaleY(1.2);

        // Les infos de la leçon
        VBox lessonInfo = new VBox(8);
        lessonInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lessonInfo, Priority.ALWAYS);

        Label titleLabel = new Label(lesson.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1e293b;");

        Label descLabel = new Label(lesson.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        descLabel.setWrapText(true);

        lessonInfo.getChildren().addAll(titleLabel, descLabel);

        // Les boutons d'action avec style moderne
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton pour éditer la leçon
        Button editButton = createIconButton(
                "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                "Edit Lesson",
                "edit-icon-button"
        );
        editButton.setStyle("-fx-background-color: #10b981; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-background-color: #059669; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        editButton.setOnMouseExited(e -> editButton.setStyle("-fx-background-color: #10b981; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        editButton.setOnAction(e -> openEditLessonWindow(lesson));

        // Bouton pour supprimer la leçon
        Button deleteButton = createIconButton(
                "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
                "Delete Lesson",
                "delete-icon-button"
        );
        deleteButton.setStyle("-fx-background-color: #ef4444; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #dc2626; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #ef4444; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;"));
        deleteButton.setOnAction(e -> deleteLesson(lesson));

        // Bouton pour développer/réduire la leçon avec icône simple
        Button expandButton = new Button("+");
        expandButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 8 15; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");

        actions.getChildren().addAll(editButton, deleteButton, expandButton);

        // Assembler l'en-tête
        header.getChildren().addAll(lessonIcon, lessonInfo, actions);

        // Contenu détaillé de la leçon avec style moderne
        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 20, 20, 20));
        content.setMaxHeight(0);
        content.setVisible(false);
        content.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 15 15;");

        // Le contenu de la leçon avec style amélioré
        TextArea contentText = new TextArea(lesson.getContent());
        contentText.setEditable(false);
        contentText.setWrapText(true);
        contentText.setPrefHeight(200);
        contentText.setStyle("-fx-control-inner-background: white; " +
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-font-size: 14px; " +
                "-fx-text-fill: #334155;");

        // Ajouter l'attachement si présent avec style moderne
        if (lesson.getAttachment() != null && !lesson.getAttachment().isEmpty()) {
            HBox attachmentBox = new HBox(10);
            attachmentBox.setAlignment(Pos.CENTER_LEFT);
            attachmentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-padding: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

            SVGPath attachmentIcon = new SVGPath();
            attachmentIcon.setContent("M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z");
            attachmentIcon.setFill(Color.web("#3b82f6"));
            attachmentIcon.setScaleX(0.8);
            attachmentIcon.setScaleY(0.8);

            Label attachmentLabel = new Label("Attachment: ");
            attachmentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

            Hyperlink attachmentLink = new Hyperlink(lesson.getAttachment());
            attachmentLink.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
            attachmentLink.setOnAction(e -> {
                // Logique pour ouvrir l'attachement
            });

            attachmentBox.getChildren().addAll(attachmentIcon, attachmentLabel, attachmentLink);
            content.getChildren().add(attachmentBox);
        }

        content.getChildren().add(contentText);

        // Ajouter l'en-tête et le contenu au conteneur principal
        lessonItem.getChildren().addAll(header, content);

        // Effets de survol pour l'en-tête
        header.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), lessonItem);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            lessonItem.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 7); " +
                    "-fx-margin: 10;");
        });

        header.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), lessonItem);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            lessonItem.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                    "-fx-margin: 10;");
        });

        // Configurer le comportement d'accordéon
        expandButton.setOnAction(e -> toggleLessonContent(content, expandButton));

        return lessonItem;
    }

    private void toggleLessonContent(VBox content, Button expandButton) {
        if (expandedLessonContent != null && expandedLessonContent != content) {
            expandedLessonContent.setVisible(false);
            expandedLessonContent.setMaxHeight(0);
            Button prevExpandButton = findExpandButtonForContent(expandedLessonContent);
            if (prevExpandButton != null) {
                prevExpandButton.setText("▼");
                prevExpandButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15; " +
                        "-fx-background-radius: 20; -fx-cursor: hand;");
            }
        }

        boolean isExpanded = content.isVisible();
        if (isExpanded) {
            content.setVisible(false);
            content.setMaxHeight(0);
            expandButton.setText("▼");
            expandButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                    "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand;");
            expandedLessonContent = null;
        } else {
            content.setVisible(true);
            content.setMaxHeight(Double.MAX_VALUE);
            expandButton.setText("▲");
            expandButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand;");
            expandedLessonContent = content;

            // Animation de slide avec fade
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), content);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            TranslateTransition slideTransition = new TranslateTransition(Duration.millis(300), content);
            slideTransition.setFromY(-10);
            slideTransition.setToY(0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, slideTransition);
            parallelTransition.play();
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
            stage.setTitle("Update Course");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllCourses();

            if (courseDetailsView.isVisible() && currentCourse != null && currentCourse.getId() == course.getId()) {
                try {
                    Course updatedCourse = courseService.getCourseById(course.getId());
                    if (updatedCourse != null) {
                        viewCourseDetails(updatedCourse);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error while opening the update page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCourse(Course course) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this course?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    courseService.deleteCourse(course);
                    showAlert(AlertType.INFORMATION, "Success", "Course has been successfully deleted!");

                    if (courseDetailsView.isVisible() && currentCourse != null && currentCourse.getId() == course.getId()) {
                        backToCoursesList();
                    }

                    loadAllCourses();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Error", "Error while deleting the course: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
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
            stage.setTitle("Add a Course");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllCourses();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error while opening the add page: " + e.getMessage());
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
            showAlert(AlertType.ERROR, "Error", "Error while navigating: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AboutView.fxml"));
            Parent root = loader.load();

            Scene scene = coursesContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error while navigating: " + e.getMessage());
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


    // Méthode pour effectuer la recherche YouTube
    private List<YouTubeVideo> searchYouTube(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String apiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=" +
                encodedQuery + "&type=video&key=" + YOUTUBE_API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur API YouTube: " + response.statusCode() + " " + response.body());
        }

        List<YouTubeVideo> videos = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response.body());
        JSONArray items = jsonResponse.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject id = item.getJSONObject("id");
            String videoId = id.getString("videoId");

            JSONObject snippet = item.getJSONObject("snippet");
            String title = snippet.getString("title");
            String description = snippet.getString("description");
            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
            String channelTitle = snippet.getString("channelTitle");

            videos.add(new YouTubeVideo(videoId, title, description, thumbnailUrl, channelTitle));
        }

        return videos;
    }

    private void openYouTubeResultsWindow(String query) {
        try {
            Stage youtubeResultsStage = new Stage();
            youtubeResultsStage.setTitle("YouTube Results for: " + query);

            VBox mainContainer = new VBox(15);
            mainContainer.setPadding(new Insets(20));
            mainContainer.setStyle("-fx-background-color: white;");

            Label titleLabel = new Label("Results for: \"" + query + "\"");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

            VBox videosContainer = new VBox(15);
            videosContainer.setPadding(new Insets(10, 0, 0, 0));
            ScrollPane scrollPane = new ScrollPane(videosContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(600);

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(50, 50);
            HBox loadingBox = new HBox(progressIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(20));
            videosContainer.getChildren().add(loadingBox);

            mainContainer.getChildren().addAll(titleLabel, scrollPane);

            Scene scene = new Scene(mainContainer, 900, 700);
            youtubeResultsStage.setScene(scene);
            youtubeResultsStage.show();

            Task<List<YouTubeVideo>> searchTask = new Task<>() {
                @Override
                protected List<YouTubeVideo> call() throws Exception {
                    return searchYouTube(query);
                }
            };

            searchTask.setOnSucceeded(event -> {
                List<YouTubeVideo> videos = searchTask.getValue();
                videosContainer.getChildren().clear();

                if (videos.isEmpty()) {
                    Label emptyLabel = new Label("No videos found for this search.");
                    emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-padding: 20 0;");
                    videosContainer.getChildren().add(emptyLabel);
                } else {
                    for (YouTubeVideo video : videos) {
                        HBox videoItem = createVideoItem(video, true);

                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), videoItem);
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.play();

                        videosContainer.getChildren().add(videoItem);
                    }
                }
            });

            searchTask.setOnFailed(event -> {
                Throwable exception = searchTask.getException();
                videosContainer.getChildren().clear();
                Label errorLabel = new Label("Error during search: " + exception.getMessage());
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e53e3e;");
                videosContainer.getChildren().add(errorLabel);
            });

            new Thread(searchTask).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error while opening the results window: " + e.getMessage());
        }
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
                loadLessonsForCourse(currentCourse);
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Error", "Failed to delete lesson");
            }
        }
    }

    // Add new method for creating icon buttons
    private Button createIconButton(String svgPath, String tooltipText, String styleClass) {
        Button button = new Button();
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.WHITE);
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        button.setGraphic(icon);
        button.setTooltip(new Tooltip(tooltipText));
        button.getStyleClass().add(styleClass);

        // Style de base pour tous les boutons
        button.setStyle("-fx-background-color: #4f46e5; -fx-padding: 10; " +
                "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");

        // Effet de survol - changement de couleur
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: #4338ca; -fx-padding: 10; " +
                    "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                    "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        });

        // Retour à la couleur d'origine quand le curseur quitte le bouton
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: #4f46e5; -fx-padding: 10; " +
                    "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                    "-fx-max-width: 40px; -fx-max-height: 40px; -fx-cursor: hand;");
        });

        return button;
    }
    private Timeline sessionMonitor;
    public void startSessionMonitor() {
        sessionMonitor = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    LogsService logsService = new LogsService();
                    if (Session.getCurrentLog() != null) {
                        int currentLogId = Session.getCurrentLog().getId();
                        boolean exists = logsService.logExists(currentLogId);
                        if (!exists) {
                            // Stop the session monitor
                            sessionMonitor.stop();

                            Platform.runLater(() -> {
                                try {
                                    Navigator.showAlert(Alert.AlertType.WARNING, "Session expired", "You have been logged out.");
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/Login.fxml"));
                                    Parent root = loader.load();
                                    Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
                                    stage.setScene(new Scene(root));
                                    stage.setTitle("Login");
                                    stage.show();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                })
        );
    }
    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");

    }

    public void ToReclamaitons(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamations.fxml");
    }

    public void ToForum(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/AfficherPoste.fxml");
    }

    public void ToDiscover(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");

    }

}