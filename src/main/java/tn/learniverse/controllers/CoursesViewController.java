package tn.learniverse.controllers;

import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.entities.Favorite;
import tn.learniverse.entities.User;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;
import tn.learniverse.services.FavoriteService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.SVGPath;
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
import tn.learniverse.tools.Session;


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

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private Slider priceSlider;

    @FXML
    private Label priceLabel;

    @FXML
    private Button favoriteFilterButton;

    private CourseService courseService;
    private LessonService lessonService;
    private FavoriteService favoriteService;
    private Course currentCourse;
    private VBox expandedLessonContent = null;
    private List<Course> allCourses;
    private Set<Integer> userFavorites;
    private int currentUserId ;

    private Image courseDefaultImage;

    @FXML
    private TextField youtubeSearchField;

    @FXML
    private Button youtubeSearchButton;

    @FXML
    private VBox youtubeVideosContainer;

    @FXML
    private VBox youtubeResultsPanel;

    @FXML
    private Button closeYoutubeResultsButton;

    @FXML
    private Label youtubeResultsTitle;

    @FXML
    private VBox youtubeSearchBox; // VBox parent de la recherche YouTube


    @FXML
    private HBox recommendedCoursesContainer;

    @FXML
    private TitledPane recommendedCoursesSection;

    @FXML
    private Button youtubeToggleBtn;

    private List<Course> recommendedCourses = new ArrayList<>();

    private static final String YOUTUBE_API_KEY = "AIzaSyAo0VLftJOdBmw9hlnjXa44WGnTgG2SRwg";
    private HttpClient httpClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseService = new CourseService();
        lessonService = new LessonService();
        favoriteService = new FavoriteService();

        httpClient = HttpClient.newHttpClient();

        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
        }

        youtubeSearchBox.setVisible(true);
        youtubeSearchBox.setManaged(true);

        // Chargez les favoris de l'utilisateur
        try {
            userFavorites = favoriteService.getUserFavorites(currentUserId);
        } catch (SQLException e) {
            System.err.println("Failed to load favorites: " + e.getMessage());
            userFavorites = new HashSet<>();
        }

        // Chargez l'image au démarrage
        try {
            courseDefaultImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image par défaut: " + e.getMessage());
            // Image de secours si la première ne se charge pas
            courseDefaultImage = new Image("https://via.placeholder.com/150?text=Course");
        }

        if (youtubeResultsPanel != null) {
            youtubeResultsPanel.setVisible(false);
            youtubeResultsPanel.setManaged(false);
        }

        // Make sure the details view is hidden initially
        if (courseDetailsView != null) {
            courseDetailsView.setVisible(false);
            courseDetailsView.setManaged(false);
        }

        // Initialize price slider value change listener
        if (priceSlider != null && priceLabel != null) {
            priceSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                priceLabel.setText(String.format("%.0f DT", newValue.doubleValue()));
            });
        }

        // Initialize the category filter
        initializeCategoryFilter();

        // Load all courses
        loadAllCourses();
        // Charger les cours recommandés
        loadRecommendedCourses();

        // Configurer la section recommandée
        recommendedCoursesSection.setExpanded(true);
        recommendedCoursesSection.setAnimated(true);
        recommendedCoursesSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    }

    @FXML
    private void refreshRecommendedCourses() {
        loadRecommendedCourses();
    }

    private void loadRecommendedCourses() {
        try {
            // Récupérer les cours recommandés
            recommendedCourses = courseService.getRecommendedCourses(currentUserId);

            // Si pas assez de recommandations, ajouter des cours aléatoires
            if (recommendedCourses.size() < 3) {
                List<Course> allCourses = courseService.getVisibleCourses();
                Collections.shuffle(allCourses);
                int needed = 3 - recommendedCourses.size();
                for (int i = 0; i < needed && i < allCourses.size(); i++) {
                    if (!recommendedCourses.contains(allCourses.get(i))) {
                        recommendedCourses.add(allCourses.get(i));
                    }
                }
            }

            displayRecommendedCourses();
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des cours recommandés: " + e.getMessage());
            recommendedCourses = new ArrayList<>();
            Label errorLabel = new Label("Impossible de charger les recommandations");
            errorLabel.setStyle("-fx-text-fill: red;");
            recommendedCoursesContainer.getChildren().add(errorLabel);
        }
    }

    private void displayRecommendedCourses() {
        recommendedCoursesContainer.getChildren().clear();

        if (recommendedCourses.isEmpty()) {
            Label noRecoLabel = new Label("No recommendations available at the moment.");
            noRecoLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");
            recommendedCoursesContainer.getChildren().add(noRecoLabel);
            return;
        }

        for (Course course : recommendedCourses) {
            VBox courseCard = createRecommendedCourseCard(course);
            recommendedCoursesContainer.getChildren().add(courseCard);
        }
    }

    private VBox createRecommendedCourseCard(Course course) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                "-fx-padding: 15; -fx-min-width: 250; -fx-max-width: 250;");

        // Image du cours
        ImageView imageView = new ImageView(courseDefaultImage);
        imageView.setFitWidth(220);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Titre
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        titleLabel.setWrapText(true);

        // Catégorie
        Label categoryLabel = new Label(course.getCategory());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        // Prix
        Label priceLabel = new Label(String.format("%.2f DT", course.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4338ca;");

        // Bouton "Voir détails"
        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 5 15;");
        viewButton.setOnAction(e -> viewCourseDetails(course));

        card.getChildren().addAll(imageView, titleLabel, categoryLabel, priceLabel, viewButton);

        // Effet de survol
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3); " +
                        "-fx-padding: 15; -fx-min-width: 250; -fx-max-width: 250;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                        "-fx-padding: 15; -fx-min-width: 250; -fx-max-width: 250;")
        );

        return card;
    }

    @FXML
    private void handleYoutubeSearch() {
        String query = youtubeSearchField.getText().trim();
        if (query.isEmpty()) return;

        openYouTubeResultsWindow(query);
    }


    @FXML
    private void closeYoutubeResults() {
        youtubeResultsPanel.setVisible(false);
        youtubeResultsPanel.setManaged(false);
    }

    // Méthode pour afficher les vidéos dans la vue actuelle
    private void displayVideosInCurrentView(List<YouTubeVideo> videos) {
        youtubeVideosContainer.getChildren().clear();

        // Titre des résultats
        Label headerLabel = new Label("Résultats de recherche pour: \"" + youtubeSearchField.getText().trim() + "\"");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2d3748; -fx-padding: 10 0;");
        youtubeVideosContainer.getChildren().add(headerLabel);

        if (videos.isEmpty()) {
            Label emptyLabel = new Label("Aucune vidéo trouvée pour cette recherche.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-padding: 20 0;");
            youtubeVideosContainer.getChildren().add(emptyLabel);
            return;
        }

        // Ajouter chaque vidéo dans un container avec style amélioré
        for (YouTubeVideo video : videos) {
            HBox videoItem = createVideoItem (video, false);

            // Animation d'entrée
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), videoItem);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            youtubeVideosContainer.getChildren().add(videoItem);
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
        Button openInYouTubeButton = new Button("Voir sur YouTube");
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
            List<Course> courses = courseService.getVisibleCourses();
            Set<String> categories = new HashSet<>();

            // Extract unique categories
            for (Course course : courses) {
                if (course.getCategory() != null && !course.getCategory().isEmpty()) {
                    categories.add(course.getCategory());
                }
            }

            // Add "All Categories" option
            categoryFilter.getItems().add("All Categories");

            // Add all unique categories
            categoryFilter.getItems().addAll(categories);

            // Set default selection
            categoryFilter.setValue("All Categories");

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadAllCourses() {
        try {
            allCourses = courseService.getVisibleCourses();
            displayFilteredCourses(allCourses);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFilteredCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyLabel = new Label("No courses match your search criteria");
            emptyLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 18px;");
            coursesContainer.getChildren().add(emptyLabel);
        } else {
            for (Course course : courses) {
                coursesContainer.getChildren().add(createCourseItem(course));
            }
        }
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        applyFilters();
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        priceSlider.setValue(priceSlider.getMax());
        // Reset le bouton de favoris aussi
        favoriteFilterButton.setStyle("-fx-background-color: white; -fx-text-fill: #4338ca; -fx-border-color: #4338ca; -fx-border-radius: 20; -fx-background-radius: 20;");
        displayFilteredCourses(allCourses);

        // ➤ RÉAFFICHER la barre YouTube
        if (youtubeSearchBox != null) {
            youtubeSearchBox.setVisible(true);
            youtubeSearchBox.setManaged(true);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String category = categoryFilter.getValue();
        double maxPrice = priceSlider.getValue();
        boolean showOnlyFavorites = favoriteFilterButton.getStyle().contains("-fx-background-color: #4338ca");

        List<Course> filteredCourses = allCourses.stream()
                .filter(course -> {
                    // Filter by search text
                    boolean matchesSearch = searchText.isEmpty() ||
                            (course.getTitle() != null && course.getTitle().toLowerCase().contains(searchText)) ||
                            (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchText));

                    // Filter by category
                    boolean matchesCategory = "All Categories".equals(category) ||
                            (course.getCategory() != null && course.getCategory().equals(category));

                    // Filter by price
                    boolean matchesPrice = course.getPrice() <= maxPrice;

                    // Filter by favorites if needed
                    boolean matchesFavorites = !showOnlyFavorites || userFavorites.contains(course.getId());

                    return matchesSearch && matchesCategory && matchesPrice && matchesFavorites;
                })
                .collect(Collectors.toList());

        displayFilteredCourses(filteredCourses);
    }

    /**
     * Filtre les cours pour n'afficher que les favoris
     */
    @FXML
    private void showOnlyFavorites() {
        if (favoriteFilterButton.getStyle().contains("-fx-background-color: #4338ca")) {
            // Reset le filtre
            favoriteFilterButton.setStyle("-fx-background-color: white; -fx-text-fill: #4338ca; -fx-border-color: #4338ca; -fx-border-radius: 20; -fx-background-radius: 20;");
            applyFilters();

            if (youtubeSearchBox != null) {
                youtubeSearchBox.setVisible(true);
                youtubeSearchBox.setManaged(true);
            }
        } else {
            // Activer le filtre favoris
            favoriteFilterButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-border-color: #4338ca; -fx-border-radius: 20; -fx-background-radius: 20;");

            List<Course> favoriteCourses = allCourses.stream()
                    .filter(course -> userFavorites.contains(course.getId()))
                    .collect(Collectors.toList());
            displayFilteredCourses(favoriteCourses);


            if (youtubeSearchBox != null) {
                youtubeSearchBox.setVisible(false);
                youtubeSearchBox.setManaged(false);
            }
        }
    }

    private HBox createCourseItem(Course course) {
        HBox courseItem = new HBox();
        courseItem.setSpacing(15);
        courseItem.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 20;");

        // Image à gauche
        ImageView courseImageView = new ImageView(courseDefaultImage);
        courseImageView.setFitWidth(100);
        courseImageView.setFitHeight(100);
        courseImageView.setPreserveRatio(true);

        // Détails du cours
        VBox courseDetails = new VBox();
        courseDetails.setSpacing(8);
        HBox.setHgrow(courseDetails, Priority.ALWAYS);

        // Label du titre
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2d3748;");

        // Bouton favori avec SVG
        Button favoriteButton = new Button();
        SVGPath heartIcon = new SVGPath();
        boolean isFavorite = userFavorites.contains(course.getId());

        heartIcon.setContent(isFavorite
                ? "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 "
                + "2 5.42 4.42 3 7.5 3c1.74 0 3.41 0.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 "
                + "19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                : "M16.5 3c-1.74 0-3.41 0.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 "
                + "4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32 "
                + "C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3z");

        heartIcon.setFill(isFavorite ? Color.RED : Color.GRAY);
        heartIcon.setScaleX(0.5);
        heartIcon.setScaleY(0.5);

        favoriteButton.setGraphic(heartIcon);
        favoriteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        favoriteButton.setOnAction(e -> {
            boolean isNowFavorite = toggleFavorite(course); // toggleFavorite doit retourner true/false

            // 1. Scale / Rebond du cœur
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), heartIcon);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.5);
            scaleTransition.setToY(1.5);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);

            // 2. Rotation légère
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), heartIcon);
            rotateTransition.setByAngle(20);
            rotateTransition.setAutoReverse(true);
            rotateTransition.setCycleCount(2);

            // 3. Changement de couleur
            FillTransition fillTransition = new FillTransition(Duration.millis(300), heartIcon);
            if (isNowFavorite) {
                fillTransition.setFromValue(Color.GRAY);
                fillTransition.setToValue(Color.RED);
            } else {
                fillTransition.setFromValue(Color.RED);
                fillTransition.setToValue(Color.GRAY);
            }

            // 4. Glow
            DropShadow glow = new DropShadow(20, Color.RED);
            heartIcon.setEffect(glow);

            PauseTransition removeGlow = new PauseTransition(Duration.millis(300));
            removeGlow.setOnFinished(eventGlow -> heartIcon.setEffect(null));

            // 5. Cercle d'ondes
            Circle ripple = new Circle(0, Color.web("#ff6b6b", 0.4));
            ripple.setCenterX(heartIcon.getBoundsInParent().getCenterX());
            ripple.setCenterY(heartIcon.getBoundsInParent().getCenterY());
            ((Pane) favoriteButton.getParent()).getChildren().add(ripple);

            Timeline rippleAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(ripple.radiusProperty(), 0),
                            new KeyValue(ripple.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(ripple.radiusProperty(), 60),
                            new KeyValue(ripple.opacityProperty(), 0.0))
            );
            rippleAnimation.setOnFinished(ev -> ((Pane) favoriteButton.getParent()).getChildren().remove(ripple));

            // 6. Jouer un son
            try {
                AudioClip sound = new AudioClip(getClass().getResource("/sounds/sparkle.mp3").toExternalForm());
                sound.play();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // 7. Lancer toutes les animations ensemble
            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, rotateTransition, fillTransition);
            parallelTransition.play();
            removeGlow.play();
            rippleAnimation.play();
        });

        // HBox contenant le titre à gauche et le cœur à droite
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(10);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleLabel, favoriteButton);

        // Description
        Label descriptionLabel = new Label(course.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
        descriptionLabel.setWrapText(true);

        // Infos supplémentaires
        Label infoLabel = new Label(String.format("Duration: %d hours • Price: %.2f DT • Level: %s • Category: %s",
                course.getDuration(), course.getPrice(), course.getLevel(), course.getCategory()));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        // Boutons d'action
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button viewDetailsButton = new Button("View Course Details");
        viewDetailsButton.setStyle("-fx-background-color: white; -fx-text-fill: #4338ca; -fx-border-color: #4338ca; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> viewCourseDetails(course));

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: white; -fx-text-fill: #3182ce; -fx-border-color: #3182ce; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        updateButton.setOnAction(e -> updateCourse(course));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: #e53e3e; -fx-border-color: #e53e3e; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> deleteCourse(course));

        buttonsBox.getChildren().addAll(viewDetailsButton, updateButton, deleteButton);

        // Ajout des composants
        courseDetails.getChildren().addAll(titleRow, descriptionLabel, infoLabel, buttonsBox);
        courseItem.getChildren().addAll(courseImageView, courseDetails);

        return courseItem;
    }


    @FXML
    private void toggleYoutubeSearch() {
        boolean isShowing = !youtubeSearchBox.isVisible();

        // Animation de la VBox
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), youtubeSearchBox);
        fadeTransition.setFromValue(isShowing ? 0 : 1);
        fadeTransition.setToValue(isShowing ? 1 : 0);

        // Animation du bouton
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), youtubeToggleBtn);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.9);
        scaleTransition.setToY(0.9);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition);

        if(isShowing) {
            youtubeSearchBox.setVisible(true);
            youtubeSearchBox.setManaged(true);
        }

        parallelTransition.setOnFinished(e -> {
            if(!isShowing) {
                youtubeSearchBox.setVisible(false);
                youtubeSearchBox.setManaged(false);
            }

            // Changement de style après l'animation
            if(isShowing) {
                youtubeToggleBtn.setText("Bye bye YouTube!");
                youtubeToggleBtn.getStyleClass().add("hide");
                youtubeToggleBtn.getStyleClass().remove("show");
            } else {
                youtubeToggleBtn.setText("Welcome to the universe of Youtube!");
                youtubeToggleBtn.getStyleClass().add("show");
                youtubeToggleBtn.getStyleClass().remove("hide");
            }
        });

        parallelTransition.play();

    }

    private boolean toggleFavorite(Course course) {
        try {
            boolean isNowFavorite;
            if (userFavorites.contains(course.getId())) {
                // Supprimer des favoris
                favoriteService.removeFavorite(currentUserId, course.getId());
                userFavorites.remove(course.getId());
                isNowFavorite = false;
            } else {
                // Ajouter aux favoris
                favoriteService.addFavorite(currentUserId, course.getId());
                userFavorites.add(course.getId());
                isNowFavorite = true;
            }

            // Rafraîchir les filtres et les recommandations
            applyFilters();
            refreshRecommendedCourses();

            return isNowFavorite;
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to update favorites: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void refreshAll() {
        try {
            // Recharger les favoris
            userFavorites = favoriteService.getUserFavorites(currentUserId);

            // Recharger tous les cours
            allCourses = courseService.getVisibleCourses();

            // Appliquer les filtres actuels
            applyFilters();

            // Recharger les recommandations
            loadRecommendedCourses();

            // Si on est en mode détails, rafraîchir aussi
            if (courseDetailsView.isVisible() && currentCourse != null) {
                viewCourseDetails(currentCourse);
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to refresh data: " + e.getMessage());
        }
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

            // Ajouter bouton favori avec SVG
            Button favoriteButton = new Button();
            SVGPath heartIcon = new SVGPath();
            boolean isFavorite = userFavorites.contains(course.getId());

            heartIcon.setContent(isFavorite
                    ? "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 "
                    + "2 5.42 4.42 3 7.5 3c1.74 0 3.41 0.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 "
                    + "19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                    : "M16.5 3c-1.74 0-3.41 0.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 "
                    + "4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32 "
                    + "C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3z");

            heartIcon.setFill(isFavorite ? Color.RED : Color.GRAY);
            heartIcon.setScaleX(0.6);
            heartIcon.setScaleY(0.6);

            favoriteButton.setGraphic(heartIcon);
            favoriteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

            favoriteButton.setOnAction(e -> {
                // Animation POP
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), heartIcon);
                scaleTransition.setFromX(1.0);
                scaleTransition.setFromY(1.0);
                scaleTransition.setToX(1.4);
                scaleTransition.setToY(1.4);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(2);
                scaleTransition.play();

                toggleFavorite(course);
                viewCourseDetails(course); // Refresh pour mettre à jour
            });

            // Ajouter le bouton coeur à côté du titre
            if (courseTitle.getParent() instanceof HBox container) {
                container.getChildren().add(favoriteButton);
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
            loadAllCourses();

            // If we're in the details view and the updated course is the current one,
            // refresh the details view
            if (courseDetailsView.isVisible() && currentCourse != null && currentCourse.getId() == course.getId()) {
                // Get updated course info
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

                    loadAllCourses();
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
            loadAllCourses();
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
        loadAllCourses();
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
            // Créer une nouvelle fenêtre (Stage)
            Stage youtubeResultsStage = new Stage();
            youtubeResultsStage.setTitle("Résultats YouTube pour: " + query);

            // Créer le conteneur principal
            VBox mainContainer = new VBox(15);
            mainContainer.setPadding(new Insets(20));
            mainContainer.setStyle("-fx-background-color: white;");

            // Titre de la fenêtre
            Label titleLabel = new Label("Résultats pour: \"" + query + "\"");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

            // Conteneur pour les vidéos
            VBox videosContainer = new VBox(15);
            videosContainer.setPadding(new Insets(10, 0, 0, 0));
            ScrollPane scrollPane = new ScrollPane(videosContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(600);

            // Indicateur de chargement
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(50, 50);
            HBox loadingBox = new HBox(progressIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(20));
            videosContainer.getChildren().add(loadingBox);

            // Ajouter les composants au conteneur principal
            mainContainer.getChildren().addAll(titleLabel, scrollPane);

            // Créer la scène et l'afficher
            Scene scene = new Scene(mainContainer, 900, 700);
            youtubeResultsStage.setScene(scene);
            youtubeResultsStage.show();

            // Effectuer la recherche en arrière-plan
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
                    Label emptyLabel = new Label("Aucune vidéo trouvée pour cette recherche.");
                    emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-padding: 20 0;");
                    videosContainer.getChildren().add(emptyLabel);
                } else {
                    // Ajouter les vidéos avec animation
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
                Label errorLabel = new Label("Erreur lors de la recherche: " + exception.getMessage());
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e53e3e;");
                videosContainer.getChildren().add(errorLabel);
            });

            new Thread(searchTask).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre de résultats: " + e.getMessage());
        }
    }



}