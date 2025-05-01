package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Submission;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.entities.Competition;
import tn.learniverse.tools.NavigationManager;
import tn.learniverse.tools.Session;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;


public class MyCompetition implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox competitionsContainer;
    @FXML private HBox paginationContainer;
    @FXML private MFXButton prevButton;
    @FXML private MFXButton nextButton;
    @FXML private Label statusLabel;
    @FXML private MFXIconWrapper infoIcon;
    @FXML private MFXButton filterAllBtn;
    @FXML private MFXButton filterPlannedBtn;
    @FXML private MFXButton filterInProgressBtn;
    @FXML private MFXButton filterCompletedBtn;
    @FXML private MFXButton sortEarliestBtn;
    @FXML private MFXButton sortLatestBtn;

    private static final int ITEMS_PER_PAGE = 6;
    private static final int MAX_VISIBLE_PAGES = 7;

    private List<Competition> allCompetitions;
    private ObservableList<Competition> filteredCompetitions;
    private CompetitionService competitionService;
    private int currentPage = 0;
    private String currentStatusFilter = "All";
    private String currentSortOrder = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            competitionService = new CompetitionService(connection);


            updateFilterButtonStyles();

            loadCompetitions();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCompetitions(newValue);
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to connect to the database", e.getMessage());
            e.printStackTrace();
        }
    }

    void loadCompetitions() {
        allCompetitions = competitionService.getCompetitionsByUserId(Session.getCurrentUser().getId());
        if (allCompetitions.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Competitions",
                    "No Competitions", "You have no competitions to display.");
        }

        filteredCompetitions = FXCollections.observableArrayList(allCompetitions);

        updatePagination();

        displayCompetitionsPage(0);

    }

    private void updatePagination() {
        int totalItems = filteredCompetitions.size();
        int pageCount = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        paginationContainer.getChildren().retainAll(prevButton, nextButton);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= pageCount - 1);

        int halfVisible = MAX_VISIBLE_PAGES / 2;
        int startPage = Math.max(0, currentPage - halfVisible);
        int endPage = Math.min(pageCount, startPage + MAX_VISIBLE_PAGES);

        if (endPage - startPage < MAX_VISIBLE_PAGES) {
            startPage = Math.max(0, endPage - MAX_VISIBLE_PAGES);
        }

        if (startPage > 0) {
            addPageButton(0);
            if (startPage > 1) {
                addEllipsis();
            }
        }

        for (int i = startPage; i < endPage; i++) {
            addPageButton(i);
        }

        if (endPage < pageCount) {
            if (endPage < pageCount - 1) {
                addEllipsis();
            }
            addPageButton(pageCount - 1);
        }

        int displayedItems = Math.min(ITEMS_PER_PAGE, totalItems);
        String status;
        if (totalItems > 0) {
            int firstItem = currentPage * ITEMS_PER_PAGE + 1;
            int lastItem = Math.min(firstItem + ITEMS_PER_PAGE - 1, totalItems);
            status = String.format("Showing %d-%d of %d competitions", firstItem, lastItem, totalItems);
        } else {
            status = "No competitions to display";
        }
        statusLabel.setText(status);
    }

    private void addPageButton(int pageIndex) {
        MFXButton pageButton = new MFXButton(String.valueOf(pageIndex + 1));
        pageButton.getStyleClass().add("pagination-button");
        pageButton.setStyle("-fx-background-color: " + (pageIndex == currentPage ? "#2196F3" : "#ffffff") +
                "; -fx-text-fill: " + (pageIndex == currentPage ? "#ffffff" : "#334155") +
                "; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        pageButton.setOnAction(event -> {
            currentPage = pageIndex;
            displayCompetitionsPage(pageIndex);
        });

        int index = paginationContainer.getChildren().indexOf(nextButton);
        paginationContainer.getChildren().add(index, pageButton);
    }

    private void addEllipsis() {
        Label ellipsis = new Label("...");
        ellipsis.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-padding: 8 12;");
        int index = paginationContainer.getChildren().indexOf(nextButton);
        paginationContainer.getChildren().add(index, ellipsis);
    }

    private void displayCompetitionsPage(int pageIndex) {
        competitionsContainer.getChildren().clear();

        currentPage = pageIndex;
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredCompetitions.size());

        if (filteredCompetitions.isEmpty()) {
            Label noCompetitionsLabel = new Label("No competitions found");
            noCompetitionsLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px;");
            competitionsContainer.getChildren().add(noCompetitionsLabel);
            updatePagination();
            return;
        }

        int totalPages = (int) Math.ceil((double) filteredCompetitions.size() / ITEMS_PER_PAGE);
        if (pageIndex >= totalPages) {
            currentPage = 0;
            startIndex = 0;
            endIndex = Math.min(ITEMS_PER_PAGE, filteredCompetitions.size());
        }

        for (int i = startIndex; i < endIndex; i++) {
            Competition competition = filteredCompetitions.get(i);
            try {
                Parent competitionCard = createCompetitionCard(competition);
                competitionsContainer.getChildren().add(competitionCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        updatePagination();
    }

    private Parent createCompetitionCard(Competition competition) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_card2.fxml"));
        Parent card = loader.load();

        ImageView competitionImage = (ImageView) card.lookup("#competitionImage");
        Label titleLabel = (Label) card.lookup("#titleLabel");
        Label categoryLabel = (Label) card.lookup("#categoryLabel");
        Label statusLabel = (Label) card.lookup("#statusLabel");
        Label dateLabel = (Label) card.lookup("#dateLabel");
        Label endDateLabel = (Label) card.lookup("#endDateLabel");
        Label durationLabel = (Label) card.lookup("#durationLabel");
        Label descriptionPreviewLabel = (Label) card.lookup("#descriptionPreviewLabel");
        Label participantsLabel = (Label) card.lookup("#participantsLabel");
        MFXButton checkBtn = (MFXButton) card.lookup("#checkBtn");
        MFXButton editBtn = (MFXButton) card.lookup("#editBtn");
        MFXButton viewBtn = (MFXButton) card.lookup("#viewBtn");
        Label submittedTag = (Label) card.lookup("#submittedTag");

        titleLabel.setText(competition.getNom());
        categoryLabel.setText(competition.getCategorie());
        statusLabel.setText(competition.getEtat());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateLabel.setText(competition.getDateComp().format(formatter));
        endDateLabel.setText(competition.getDateComp().plusMinutes(competition.getDuration()).format(formatter));
        durationLabel.setText(competition.getDuration() + " minutes");

        String description = competition.getDescription();
        if (description != null && !description.isEmpty()) {
            description = description.replaceAll("<[^>]*>", "");
            if (description.length() > 150) {
                description = description.substring(0, 147) + "...";
            }
            descriptionPreviewLabel.setText(description);
        } else {
            descriptionPreviewLabel.setText("No description available");
        }

        int participantsCount = competition.getCurrentParticipant();
        participantsLabel.setText(participantsCount + " registered");

        switch (competition.getEtat().toLowerCase()) {
            case "active":
                statusLabel.getStyleClass().add("competition-status-active");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #ebf8ff; -fx-text-fill: #2b6cb0;");
                break;
            case "planned":
                statusLabel.getStyleClass().add("competition-status-planned");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e6fffa; -fx-text-fill: #2c7a7b;");
                break;
            case "completed":
                statusLabel.getStyleClass().add("competition-status-completed");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f0fff4; -fx-text-fill: #276749;");
                break;
            case "draft":
                statusLabel.getStyleClass().add("competition-status-draft");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f7fafc; -fx-text-fill: #4a5568;");
                break;
        }

        if (competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
            try {
                Image image = new Image("file:" + competition.getImageUrl(), true);
                competitionImage.setImage(image);
            } catch (Exception e) {
                setDefaultOrPlaceholderImage(competitionImage);
            }
        } else {
            setDefaultOrPlaceholderImage(competitionImage);
        }

        boolean hasSubmitted = competitionService.isUserSubmissited(competition.getId(), Session.getCurrentUser().getId());
        submittedTag.setVisible(hasSubmitted);
        submittedTag.setManaged(hasSubmitted);

        checkBtn.setOnAction(event -> {
            System.out.println(CompetitionService.mySubmissions(Session.getCurrentUser().getId(),competition.getId() ));

            navigateToResults(CompetitionService.mySubmissions(Session.getCurrentUser().getId(),competition.getId()),competition.getNom());
        });
        checkBtn.setVisible(hasSubmitted);
        editBtn.setOnAction(event -> {handleLeaderboard(competition,competitionImage);        });
        editBtn.setVisible(competition.getEtat().toLowerCase().equals("completed"));
        viewBtn.setOnAction(event -> { handleViewCompetition(competition);        });
        viewBtn.setVisible(!competition.getEtat().toLowerCase().equals("completed"));

        return card;
    }
    private void handleLeaderboard(Competition c,ImageView competitionImage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompetitionLeaderboard.fxml"));
            Parent root = loader.load();
            CompetitionLeaderboardController controller = loader.getController();
            controller.initData(c.getId());


            // Set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) competitionImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the leaderboard: " + e.getMessage());
        }
    }
    private void handleViewCompetition(Competition competition) {
        try {
            // Load competition view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/competition_view.fxml"));
            Parent view = loader.load();

            // Get the controller and set competition data
            competition.setChallenges(competitionService.getChallengesForCompetition(competition.getId()));
            CompetitionViewController controller = loader.getController();
            controller.setCompetition(competition);

            // Get the stage and set the new scene
            Stage stage = (Stage) competitionsContainer.getScene().getWindow();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load competition view", e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String error, String s) {
    }


    private void navigateToResults(Map<Challenge, Submission>a,String z) {
        try {
            // Replace with your actual navigation code to results page
            NavigationManager.navigateToCompetitionResults(a,z);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setDefaultOrPlaceholderImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-competition.jpg"));
            if (defaultImage.isError()) {
                createPlaceholderImage(imageView);
            } else {
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            createPlaceholderImage(imageView);
        }
    }

    private void createPlaceholderImage(ImageView imageView) {
        int width = 220;
        int height = 180;
        javafx.scene.image.WritableImage placeholder = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter writer = placeholder.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ratio = (double) y / height;
                int r = (int) (50 + ratio * 30);
                int g = (int) (80 + ratio * 20);
                int b = (int) (150 + ratio * 50);
                writer.setArgb(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
            }
        }
        imageView.setImage(placeholder);
    }

    private void filterCompetitions(String searchTerm) {
        List<Competition> filtered;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filtered = allCompetitions;
        } else {
            String query = searchTerm.toLowerCase();
            filtered = allCompetitions.stream()
                    .filter(comp ->
                            comp.getNom().toLowerCase().contains(query) ||
                                    comp.getDescription().toLowerCase().contains(query) ||
                                    comp.getCategorie().toLowerCase().contains(query) ||
                                    comp.getEtat().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        if (!currentStatusFilter.equals("All")) {
            filtered = filtered.stream()
                    .filter(comp -> comp.getEtat().equalsIgnoreCase(currentStatusFilter))
                    .collect(Collectors.toList());
        }

        if (currentSortOrder != null) {
            Comparator<Competition> comparator = Comparator.comparing(Competition::getDateComp);
            if (currentSortOrder.equals("latest")) {
                comparator = comparator.reversed();
            }
            filtered = filtered.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        filteredCompetitions.setAll(filtered);
        currentPage = 0;
        updatePagination();
        displayCompetitionsPage(0);
    }

    private void updateFilterButtonStyles() {
        filterAllBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterPlannedBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterInProgressBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        filterCompletedBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        MFXButton activeFilterBtn = switch (currentStatusFilter) {
            case "Planned" -> filterPlannedBtn;
            case "InProgress" -> filterInProgressBtn;
            case "Completed" -> filterCompletedBtn;
            default -> filterAllBtn;
        };
        activeFilterBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: #ffffff; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        sortEarliestBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        sortLatestBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");

        if (currentSortOrder != null) {
            MFXButton activeSortBtn = currentSortOrder.equals("earliest") ? sortEarliestBtn : sortLatestBtn;
            activeSortBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: #ffffff; -fx-background-radius: 25; -fx-padding: 8 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        }
    }

    @FXML
    void handleFilterAll(ActionEvent event) {
        currentStatusFilter = "All";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
    void handleFilterPlanned(ActionEvent event) {
        currentStatusFilter = "Planned";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
    void handleFilterInProgress(ActionEvent event) {
        currentStatusFilter = "InProgress";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
    void handleFilterCompleted(ActionEvent event) {
        currentStatusFilter = "Completed";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
    void handleSortEarliest(ActionEvent event) {
        currentSortOrder = "earliest";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    @FXML
    void handleSortLatest(ActionEvent event) {
        currentSortOrder = "latest";
        updateFilterButtonStyles();
        filterCompetitions(searchField.getText());
    }

    private void handleCheckSubmission(Competition competition) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/submission_view.fxml"));
            Parent view = loader.load();

            SubmissionViewController controller = loader.getController();
            controller.setCompetition(competition);

            Stage stage = (Stage) competitionsContainer.getScene().getWindow();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.show();

        } catch (IOException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load submission view", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayCompetitionsPage(currentPage);
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) filteredCompetitions.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            displayCompetitionsPage(currentPage);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogHelper.styleDialog(alert);
        alert.showAndWait();
    }

    private static class DialogHelper {
        public static void styleDialog(Alert dialog) {
            dialog.getDialogPane().getStylesheets().add(
                    MyCompetition.class.getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }
    }
}