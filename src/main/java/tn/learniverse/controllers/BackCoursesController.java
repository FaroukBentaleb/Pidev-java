package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BackCoursesController implements Initializable {

    @FXML
    private TableView<Course> courseTable;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    private TableColumn<Course, String> descriptionColumn;

    @FXML
    private TableColumn<Course, String> levelColumn;

    @FXML
    private TableColumn<Course, String> categoryColumn;

    @FXML
    private TableColumn<Course, Void> actionColumn;

    @FXML
    private VBox mainContainer;

    private final CourseService courseService = new CourseService();
    private final LessonService lessonService = new LessonService();

    private TableView<Lesson> lessonTable;
    private Label lessonSectionTitle;
    private VBox lessonContainer;
    private Button backToCourseBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Appliquer le style de base pour les tables
        applyTableStyle(courseTable);

        // Configuration des colonnes
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Appliquer un style aux cellules des colonnes
        applyCellStyle(titleColumn);
        applyCellStyle(descriptionColumn);
        applyCellStyle(levelColumn);
        applyCellStyle(categoryColumn);

        setupActionColumn();

        try {
            courseTable.setItems(FXCollections.observableArrayList(courseService.getAllCourses()));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage());
        }

        // Animation de survol pour la table des cours
        courseTable.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<>();
            addRowHoverEffect(row);
            return row;
        });

        // Créer le container pour les leçons qu'on ajoutera au mainContainer plus tard
        createLessonContainerComponents();
    }

    private void createLessonContainerComponents() {
        // 1. Créer le VBox qui contiendra le titre, le bouton retour et la table
        lessonContainer = new VBox(15);
        lessonContainer.setStyle("-fx-padding: 0;");

        // 2. Créer un en-tête avec titre et bouton de retour
        HBox header = new HBox(10);
        header.setStyle("-fx-padding: 0 0 15 0;");

        lessonSectionTitle = new Label("Leçons");
        lessonSectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        backToCourseBtn = new Button("Retour aux cours");
        backToCourseBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        backToCourseBtn.setOnAction(event -> switchToCourseView());

        // Effet de survol sur le bouton
        backToCourseBtn.setOnMouseEntered(e ->
                backToCourseBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );
        backToCourseBtn.setOnMouseExited(e ->
                backToCourseBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                        "-fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;")
        );

        header.getChildren().addAll(lessonSectionTitle, spacer, backToCourseBtn);

        // 3. Créer la table des leçons avec style
        lessonTable = new TableView<>();
        applyTableStyle(lessonTable);

        // 4. Configurer les colonnes
        TableColumn<Lesson, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(180);
        applyCellStyle(titleCol);

        TableColumn<Lesson, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);
        applyCellStyle(descCol);

        TableColumn<Lesson, String> contentCol = new TableColumn<>("Contenu");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setPrefWidth(300);
        applyCellStyle(contentCol);

        TableColumn<Lesson, String> attachmentCol = new TableColumn<>("Pièce jointe");
        attachmentCol.setCellValueFactory(new PropertyValueFactory<>("attachment"));
        attachmentCol.setPrefWidth(150);
        applyCellStyle(attachmentCol);

        lessonTable.getColumns().addAll(titleCol, descCol, contentCol, attachmentCol);

        // 5. Animation de survol pour la table des leçons
        lessonTable.setRowFactory(tv -> {
            TableRow<Lesson> row = new TableRow<>();
            addRowHoverEffect(row);
            return row;
        });

        // 6. Ajouter les composants au container
        lessonContainer.getChildren().addAll(header, lessonTable);
    }

    private <T> void addRowHoverEffect(TableRow<T> row) {
        row.setOnMouseEntered(event -> {
            if (!row.isEmpty()) {
                row.setStyle("-fx-background-color: #EFF6FF;");
            }
        });
        row.setOnMouseExited(event -> {
            if (!row.isEmpty()) {
                row.setStyle("");
            }
        });
    }

    private <T> void applyTableStyle(TableView<T> table) {
        table.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");

        // Style de l'en-tête de la table
        table.getStylesheets().add(getClass().getResource("/tableStyle.css").toExternalForm());
    }

    private <T, S> void applyCellStyle(TableColumn<T, S> column) {
        column.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5 8;");
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<Course, Void>, TableCell<Course, Void>>() {
            @Override
            public TableCell<Course, Void> call(TableColumn<Course, Void> param) {
                return new TableCell<Course, Void>() {
                    private final Button viewLessonsBtn = new Button("Voir les leçons");
                    private final Button toggleVisibilityBtn = new Button();
                    private final HBox container = new HBox(5);  // 5px d'espacement entre les boutons

                    {
                        // Style pour le bouton "Voir les leçons"
                        viewLessonsBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                                "-fx-background-radius: 4px; -fx-padding: 5px 10px;");
                        viewLessonsBtn.setOnAction(event -> {
                            Course course = getTableView().getItems().get(getIndex());
                            displayLessonsForCourse(course);
                        });

                        // Effet de survol
                        viewLessonsBtn.setOnMouseEntered(e ->
                                viewLessonsBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                                        "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
                        );
                        viewLessonsBtn.setOnMouseExited(e ->
                                viewLessonsBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                                        "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
                        );

                        // Style et comportement pour le bouton de masquage
                        toggleVisibilityBtn.setOnAction(event -> {
                            Course course = getTableView().getItems().get(getIndex());
                            toggleCourseVisibility(course);
                        });

                        // Ajouter les deux boutons au conteneur
                        container.getChildren().addAll(viewLessonsBtn, toggleVisibilityBtn);
                        container.setAlignment(javafx.geometry.Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Course course = getTableView().getItems().get(getIndex());
                            updateToggleButton(toggleVisibilityBtn, course.isIs_frozen());
                            setGraphic(container);
                        }
                    }
                };
            }
        });
    }

    private void updateToggleButton(Button button, boolean isFrozen) {
        if (isFrozen) {
            button.setText("Unhide");
            button.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-background-radius: 4px; -fx-padding: 5px 10px;");

            // Effet de survol pour le bouton "Unhide"
            button.setOnMouseEntered(e ->
                    button.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                            "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
            );
            button.setOnMouseExited(e ->
                    button.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                            "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
            );
        } else {
            button.setText("Hide");
            button.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                    "-fx-background-radius: 4px; -fx-padding: 5px 10px;");

            // Effet de survol pour le bouton "Hide"
            button.setOnMouseEntered(e ->
                    button.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; " +
                            "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
            );
            button.setOnMouseExited(e ->
                    button.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                            "-fx-background-radius: 4px; -fx-padding: 5px 10px;")
            );
        }
    }

    private void toggleCourseVisibility(Course course) {
        try {
            courseService.toggleCourseVisibility(course);
            // Rafraîchir la table pour mettre à jour l'apparence du bouton
            courseTable.refresh();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de modifier la visibilité du cours: " + e.getMessage());
        }
    }

    private void displayLessonsForCourse(Course course) {
        try {
            // 1. Mettre à jour le titre
            lessonSectionTitle.setText("Leçons pour: " + course.getTitle());

            // 2. Charger les leçons
            List<Lesson> lessons = lessonService.getLessonsByCourse(course);
            lessonTable.setItems(FXCollections.observableArrayList(lessons));

            // 3. Cacher la table des cours et afficher les leçons
            switchToLessonsView();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les leçons: " + e.getMessage());
        }
    }

    private void switchToLessonsView() {
        // Animation de transition (fondu)
        fadeTransition(false, () -> {
            // Vider le conteneur principal
            mainContainer.getChildren().clear();

            // Ajouter uniquement le conteneur des leçons
            mainContainer.getChildren().add(lessonContainer);

            // Animation d'apparition
            fadeTransition(true, null);
        });
    }

    private void switchToCourseView() {
        // Animation de transition (fondu)
        fadeTransition(false, () -> {
            // Vider le conteneur principal
            mainContainer.getChildren().clear();

            // Ajouter le titre et la table des cours
            Label coursesTitle = new Label("Liste des cours");
            coursesTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

            mainContainer.getChildren().addAll(coursesTitle, courseTable);

            // Animation d'apparition
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