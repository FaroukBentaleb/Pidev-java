package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class BackCoursesController implements Initializable {

    @FXML
    private TableView<Course> courseTable;

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane courseCardsContainer;

    @FXML
    private VBox mainContainer;

    private final CourseService courseService = new CourseService();
    private final LessonService lessonService = new LessonService();

    private Label lessonSectionTitle;
    private VBox lessonContainer;
    private Button backToCourseBtn;
    private FlowPane lessonCardsContainer;

    // Ajout des éléments pour la vue détaillée
    private VBox lessonDetailContainer;
    private Button backToLessonsBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the course cards view
        loadCourses();

        // Setup search functionality
        setupSearch();

        // Create the container for lessons to be added to mainContainer later
        createLessonContainerComponents();

        // Créer le conteneur pour les détails des leçons
        createLessonDetailComponents();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.isEmpty()) {
                    loadAllCourses();
                } else {
                    List<Course> filteredCourses = courseService.searchCourses(newValue);
                    updateCourseCards(filteredCourses);
                }
            } catch (SQLException e) {
                showAlert("Error", "Unable to filter courses: " + e.getMessage());
            }
        });
    }

    private void loadCourses() {
        try {
            loadAllCourses();
        } catch (SQLException e) {
            showAlert("Error", "Unable to load courses: " + e.getMessage());
        }
    }

    private void loadAllCourses() throws SQLException {
        List<Course> courses = courseService.getAllCourses();
        updateCourseCards(courses);
    }

    private void updateCourseCards(List<Course> courses) {
        courseCardsContainer.getChildren().clear();

        for (Course course : courses) {
            VBox card = createCourseCard(course);
            courseCardsContainer.getChildren().add(card);
        }
    }

    private VBox createCourseCard(Course course) {
        // Creating the card
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                "-fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 15;");

        // Colored header
        Rectangle header = new Rectangle(220, 8);
        header.setFill(Color.web("#3B82F6"));
        header.setArcWidth(10);
        header.setArcHeight(10);

        // Course title
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // Category and level
        HBox infoBox = new HBox(8);
        Label categoryBadge = createBadge(course.getCategory(), "#EFF6FF", "#3B82F6");
        Label levelBadge = createBadge(course.getLevel(), "#F0FDF4", "#10B981");
        infoBox.getChildren().addAll(categoryBadge, levelBadge);

        // Description with character limit
        TextFlow descriptionFlow = new TextFlow();
        Text descText = new Text(truncateText(course.getDescription(), 100));
        descText.setStyle("-fx-fill: #64748B;");
        descriptionFlow.getChildren().add(descText);

        // Visibility status
        Label statusBadge = createBadge(
                course.isIs_frozen() ? "Hidden" : "Visible",
                course.isIs_frozen() ? "#FEF2F2" : "#F0FDF4",
                course.isIs_frozen() ? "#EF4444" : "#10B981"
        );

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewLessonsBtn = new Button("View Lessons");
        styleButton(viewLessonsBtn, "#3B82F6", "#2563EB");
        viewLessonsBtn.setOnAction(event -> displayLessonsForCourse(course));

        Button toggleVisibilityBtn = new Button(course.isIs_frozen() ? "Unhide" : "Hide");
        styleButton(toggleVisibilityBtn, course.isIs_frozen() ? "#10B981" : "#F59E0B",
                course.isIs_frozen() ? "#059669" : "#D97706");
        toggleVisibilityBtn.setOnAction(event -> {
            try {
                courseService.toggleCourseVisibility(course);
                // Update the card
                card.getChildren().clear();
                VBox updatedCard = createCourseCard(course);
                int index = courseCardsContainer.getChildren().indexOf(card);
                courseCardsContainer.getChildren().set(index, updatedCard);
            } catch (SQLException e) {
                showAlert("Error", "Unable to change course visibility: " + e.getMessage());
            }
        });

        actionBox.getChildren().addAll(viewLessonsBtn, toggleVisibilityBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add elements to the card
        card.getChildren().addAll(header, titleLabel, infoBox, descriptionFlow, spacer, statusBadge, actionBox);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #F8FAFC; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 15;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 15;")
        );

        return card;
    }

    private Label createBadge(String text, String bgColor, String textColor) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;"
        );
        return badge;
    }

    private void styleButton(Button button, String baseColor, String hoverColor) {
        button.setStyle(
                "-fx-background-color: " + baseColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 5px 10px;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + hoverColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 4px; " +
                                "-fx-padding: 5px 10px;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + baseColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 4px; " +
                                "-fx-padding: 5px 10px;"
                )
        );
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private void createLessonContainerComponents() {
        // 1. Create VBox that will contain title, back button, and cards container
        lessonContainer = new VBox(15);
        lessonContainer.setStyle("-fx-padding: 0;");

        // 2. Create header with title and back button
        HBox header = new HBox(10);
        header.setStyle("-fx-padding: 0 0 15 0;");

        lessonSectionTitle = new Label("Lessons");
        lessonSectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        backToCourseBtn = new Button("Back to Courses");
        backToCourseBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        backToCourseBtn.setOnAction(event -> switchToCourseView());

        // Hover effect on button
        backToCourseBtn.setOnMouseEntered(e ->
                backToCourseBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );
        backToCourseBtn.setOnMouseExited(e ->
                backToCourseBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );

        header.getChildren().addAll(lessonSectionTitle, spacer, backToCourseBtn);

        // 3. Create lesson cards container
        lessonCardsContainer = new FlowPane();
        lessonCardsContainer.setHgap(20);
        lessonCardsContainer.setVgap(20);
        lessonCardsContainer.setPrefWrapLength(800); // Adjust as needed

        // 4. Add components to container
        lessonContainer.getChildren().addAll(header, lessonCardsContainer);
    }

    private void createLessonDetailComponents() {
        // 1. Create VBox that will contain title, back button, and details container
        lessonDetailContainer = new VBox(15);
        lessonDetailContainer.setStyle("-fx-padding: 0;");

        // 2. Create header with back button
        HBox header = new HBox(10);
        header.setStyle("-fx-padding: 0 0 15 0;");

        Label detailSectionTitle = new Label("Lesson Details");
        detailSectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        backToLessonsBtn = new Button("Back to Lessons");
        backToLessonsBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; " +
                "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        backToLessonsBtn.setOnAction(event -> switchToLessonsView());

        // Hover effect on button
        backToLessonsBtn.setOnMouseEntered(e ->
                backToLessonsBtn.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );
        backToLessonsBtn.setOnMouseExited(e ->
                backToLessonsBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );

        header.getChildren().addAll(detailSectionTitle, spacer, backToLessonsBtn);

        // 3. Add header to container (we'll add the specific lesson details when needed)
        lessonDetailContainer.getChildren().add(header);
    }

    private VBox createLessonCard(Lesson lesson) {
        // Creating the card
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                "-fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 15;");

        // Colored header - different from courses to distinguish
        Rectangle header = new Rectangle(220, 8);
        header.setFill(Color.web("#8B5CF6")); // Purple to differentiate from courses
        header.setArcWidth(10);
        header.setArcHeight(10);

        // Lesson title
        Label titleLabel = new Label(lesson.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // Description with character limit
        TextFlow descriptionFlow = new TextFlow();
        Text descText = new Text(truncateText(lesson.getDescription(), 100));
        descText.setStyle("-fx-fill: #64748B;");
        descriptionFlow.getChildren().add(descText);

        // Badge for attachment
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        if (lesson.getAttachment() != null && !lesson.getAttachment().isEmpty()) {
            Label attachmentBadge = createBadge("Attachment", "#F0F9FF", "#0369A1");
            infoBox.getChildren().add(attachmentBadge);
        }

        // Button to view details
        Button viewDetailsBtn = new Button("View Details");
        styleButton(viewDetailsBtn, "#8B5CF6", "#7C3AED");
        viewDetailsBtn.setOnAction(event -> showLessonDetails(lesson));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add elements to the card
        card.getChildren().addAll(header, titleLabel, descriptionFlow, infoBox, spacer, viewDetailsBtn);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #F8FAFC; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 15;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 15;")
        );

        return card;
    }

    private void showLessonDetails(Lesson lesson) {

        // Suppression des anciens détails s'ils existent (tout sauf le header)
        if (lessonDetailContainer.getChildren().size() > 1) {
            lessonDetailContainer.getChildren().remove(1, lessonDetailContainer.getChildren().size());
        }

        // Création du contenu principal
        VBox content = new VBox(20);
        content.setPadding(new Insets(5, 0, 20, 0));

        // Créer un header personnalisé avec un accent coloré
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        headerBox.setStyle("-fx-border-color: transparent transparent #E2E8F0 transparent; " +
                "-fx-border-width: 0 0 1px 0; -fx-padding: 0 0 15px 0;");

        Rectangle colorAccent = new Rectangle(8, 40);
        colorAccent.setFill(Color.web("#8B5CF6"));
        colorAccent.setArcWidth(4);
        colorAccent.setArcHeight(4);

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(lesson.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        titleLabel.setWrapText(true);

        Label subtitleLabel = new Label("Lesson Details");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(colorAccent, new Region() {{ setMinWidth(15); }}, titleBox);

        // Sections pour les différentes informations de la leçon
        VBox sectionsContainer = new VBox(30);

        // Section description
        VBox descriptionSection = createSection("Description", lesson.getDescription(), false);

        // Section contenu avec zone de texte dans un conteneur stylisé
        VBox contentSection = new VBox(10);
        Label contentHeader = new Label("Content");
        contentHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        TextArea contentValue = new TextArea(lesson.getContent());
        contentValue.setWrapText(true);
        contentValue.setEditable(false);
        contentValue.setPrefHeight(200);
        contentValue.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px; " +
                "-fx-font-size: 14px; -fx-text-fill: #334155;");

        contentSection.getChildren().addAll(contentHeader, contentValue);

        // Section pièce jointe avec badge
        VBox attachmentSection = new VBox(10);
        Label attachmentHeader = new Label("Attachment");
        attachmentHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        HBox attachmentContainer = new HBox(10);
        attachmentContainer.setAlignment(Pos.CENTER_LEFT);
        attachmentContainer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px;");

        if (lesson.getAttachment() != null && !lesson.getAttachment().isEmpty()) {
            Label fileIcon = new Label("📎");
            fileIcon.setStyle("-fx-font-size: 16px;");

            Label attachmentValue = new Label(lesson.getAttachment());
            attachmentValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

            Label typeBadge = createBadge("File", "#F0F9FF", "#0369A1");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            attachmentContainer.getChildren().addAll(fileIcon, attachmentValue, spacer, typeBadge);
        } else {
            Label noAttachment = new Label("No attachment available");
            noAttachment.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
            attachmentContainer.getChildren().add(noAttachment);
        }

        attachmentSection.getChildren().addAll(attachmentHeader, attachmentContainer);

        // Ajouter toutes les sections au conteneur
        sectionsContainer.getChildren().addAll(
                descriptionSection,
                contentSection,
                attachmentSection
        );

        // Ajouter tout au contenu principal
        content.getChildren().addAll(headerBox, sectionsContainer);

        // Ajouter le contenu au conteneur de détails
        lessonDetailContainer.getChildren().add(content);

        // Passer à la vue des détails
        switchToLessonDetailView();
    }

    // Méthode pour passer à la vue détaillée de leçon
    private void switchToLessonDetailView() {
        // Animation de transition (fade)
        fadeTransition(false, () -> {
            // Vider le conteneur principal
            mainContainer.getChildren().clear();

            // Ajouter uniquement le conteneur de détails
            mainContainer.getChildren().add(lessonDetailContainer);

            // Animation d'apparition
            fadeTransition(true, null);
        });
    }

    // Helper method to create styled sections
    private VBox createSection(String title, String content, boolean isCard) {
        VBox section = new VBox(10);
        Label header = new Label(title);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        VBox contentContainer = new VBox();
        contentContainer.setPadding(new Insets(12));
        contentContainer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        contentContainer.getChildren().add(contentLabel);
        section.getChildren().addAll(header, contentContainer);

        return section;
    }

    private void displayLessonsForCourse(Course course) {
        try {
            // 1. Update the title
            lessonSectionTitle.setText("Lessons for: " + course.getTitle());

            // 2. Load lessons
            List<Lesson> lessons = lessonService.getLessonsByCourse(course);

            // 3. Clear the lesson cards container
            lessonCardsContainer.getChildren().clear();

            // 4. Add a card for each lesson
            for (Lesson lesson : lessons) {
                VBox lessonCard = createLessonCard(lesson);
                lessonCardsContainer.getChildren().add(lessonCard);
            }

            // 5. Hide the cards view and display lessons
            switchToLessonsView();

        } catch (SQLException e) {
            showAlert("Error", "Unable to load lessons: " + e.getMessage());
        }
    }

    private void switchToLessonsView() {
        // Transition animation (fade)
        fadeTransition(false, () -> {
            // Empty the main container
            mainContainer.getChildren().clear();

            // Add only the lessons container
            mainContainer.getChildren().add(lessonContainer);

            // Animation to appear
            fadeTransition(true, null);
        });
    }

    private void switchToCourseView() {
        // Transition animation (fade)
        fadeTransition(false, () -> {
            // Empty the main container
            mainContainer.getChildren().clear();

            // Add the search field and cards container
            HBox searchBox = new HBox(10);
            searchBox.setAlignment(Pos.CENTER_LEFT);

            Label coursesTitle = new Label("Courses List");
            coursesTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            searchBox.getChildren().addAll(coursesTitle, spacer, searchField);

            mainContainer.getChildren().addAll(searchBox, courseCardsContainer);

            // Reload courses
            try {
                loadAllCourses();
            } catch (SQLException e) {
                showAlert("Error", "Unable to load courses: " + e.getMessage());
            }

            // Animation to appear
            fadeTransition(true, null);
        });
    }

    private void fadeTransition(boolean in, Runnable onFinished) {
        double targetOpacity = in ? 1.0 : 0.0;
        double duration = 150; // milliseconds

        javafx.animation.FadeTransition fadeTransition =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(duration), mainContainer);
        fadeTransition.setToValue(targetOpacity);

        if (onFinished != null) {
            fadeTransition.setOnFinished(event -> onFinished.run());
        }

        fadeTransition.play();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}