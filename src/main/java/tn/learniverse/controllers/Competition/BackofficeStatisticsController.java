package tn.learniverse.controllers.Competition;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;
import io.github.palexdev.materialfx.controls.MFXButton;

public class BackofficeStatisticsController implements Initializable {

    @FXML private VBox statisticsContainer;
    @FXML private Label totalCompetitionsLabel;
    @FXML private Label activeCompetitionsLabel;
    @FXML private Label completedCompetitionsLabel;
    @FXML private Label sidebarTotalCompetitionsLabel;
    @FXML private Label sidebarActiveCompetitionsLabel;
    @FXML private Label sidebarCompletedCompetitionsLabel;
    @FXML private Label avgParticipantsLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> popularCompetitionsChart;
    @FXML private LineChart<String, Number> submissionRatingsChart;
    @FXML private Label totalChallengesLabel;
    @FXML private Label avgCompletionRateLabel;
    @FXML private BarChart<String, Number> difficultChallengesChart;
    @FXML private BarChart<String, Number> challengeDifficultyChart;
    @FXML private Label totalSubmissionsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label avgTimeTakenLabel;
    @FXML private Label successRateLabel;
    @FXML private LineChart<String, Number> submissionsOverTimeChart;
    @FXML private MFXButton backButton;

    private CompetitionService competitionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            competitionService = new CompetitionService(DatabaseConnection.getConnection());
            loadStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database", e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Competitions Statistics
            String totalComps = String.valueOf(competitionService.getTotalCompetitions());
            totalCompetitionsLabel.setText(totalComps);
            sidebarTotalCompetitionsLabel.setText(totalComps);

            String activeComps = String.valueOf(competitionService.getActiveCompetitions());
            activeCompetitionsLabel.setText(activeComps);
            sidebarActiveCompetitionsLabel.setText(activeComps);

            String completedComps = String.valueOf(competitionService.getCompletedCompetitions());
            completedCompetitionsLabel.setText(completedComps);
            sidebarCompletedCompetitionsLabel.setText(completedComps);

            avgParticipantsLabel.setText(String.format("%.1f", competitionService.getAverageParticipants()));

            // Competitions by Status (PieChart)
            Map<String, Integer> statusCounts = competitionService.getCompetitionsByStatus();
            ObservableList<PieChart.Data> statusData = FXCollections.observableArrayList();
            statusCounts.forEach((status, count) -> statusData.add(new PieChart.Data(status, count)));
            statusPieChart.setData(statusData);
            statusPieChart.setLabelsVisible(true);
            statusPieChart.setLabelLineLength(10);
            statusPieChart.setLegendVisible(true);

            // Most Popular Competitions (BarChart)
            XYChart.Series<String, Number> popularSeries = new XYChart.Series<>();
            popularSeries.setName("Participants");
            List<Competition> popularCompetitions = competitionService.getMostPopularCompetitions(6);
            for (Competition comp : popularCompetitions) {
                int participantCount = competitionService.getParticipantCount(comp.getId());
                popularSeries.getData().add(new XYChart.Data<>(comp.getNom(), participantCount));
            }
            popularCompetitionsChart.getData().clear();
            popularCompetitionsChart.getData().add(popularSeries);
            popularCompetitionsChart.setLegendVisible(false);

            // Submission Ratings Over Time (LineChart)
            Map<String, Double> ratingsOverTime = competitionService.getAverageSubmissionRatingOverTime();
            XYChart.Series<String, Number> ratingsSeries = new XYChart.Series<>();
            ratingsSeries.setName("Average Rating (0-20)");
            ratingsOverTime.forEach((month, rating) -> ratingsSeries.getData().add(new XYChart.Data<>(month, rating)));
            submissionRatingsChart.getData().clear();
            submissionRatingsChart.getData().add(ratingsSeries);
            submissionRatingsChart.setLegendVisible(false);

            // Challenges Statistics
            totalChallengesLabel.setText(String.valueOf(competitionService.getTotalChallenges()));
            avgCompletionRateLabel.setText(String.format("%.1f%%", competitionService.getAverageCompletionRate() * 100));

            // Most Difficult Challenges (BarChart)
            XYChart.Series<String, Number> difficultSeries = new XYChart.Series<>();
            difficultSeries.setName("Difficulty Score (0-20)");
            List<Challenge> difficultChallenges = competitionService.getMostDifficultChallenges(5);
            for (Challenge challenge : difficultChallenges) {
                double difficultyScore = competitionService.getChallengeDifficultyScore(challenge.getId());
                // Only add challenges with valid ratings
                if (difficultyScore > 0) {
                    String title = challenge.getTitle().length() > 20 ? challenge.getTitle().substring(0, 17) + "..." : challenge.getTitle();
                    difficultSeries.getData().add(new XYChart.Data<>(title, difficultyScore));
                }
            }
//            difficultChallengesChart.getData().clear();
//            difficultChallengesChart.getData().add(difficultSeries);
//            difficultChallengesChart.setLegendVisible(false);
//            // Ensure Y-axis is scaled for 0-20 range
//            ((javafx.scene.chart.NumberAxis) difficultChallengesChart.getYAxis()).setAutoRanging(false);
//            ((javafx.scene.chart.NumberAxis) difficultChallengesChart.getYAxis()).setLowerBound(0);
//            ((javafx.scene.chart.NumberAxis) difficultChallengesChart.getYAxis()).setUpperBound(20);
//            ((javafx.scene.chart.NumberAxis) difficultChallengesChart.getYAxis()).setTickUnit(5);

            // Challenge Difficulty Distribution (BarChart)
            Map<String, Integer> difficultyDistribution = competitionService.getChallengeDifficultyDistribution();
            XYChart.Series<String, Number> distributionSeries = new XYChart.Series<>();
            distributionSeries.setName("Challenge Count");
            difficultyDistribution.forEach((range, count) -> distributionSeries.getData().add(new XYChart.Data<>(range, count)));
            challengeDifficultyChart.getData().clear();
            challengeDifficultyChart.getData().add(distributionSeries);
            challengeDifficultyChart.setLegendVisible(false);

            // Submissions Statistics
            totalSubmissionsLabel.setText(String.valueOf(competitionService.getTotalSubmissions()));
            avgRatingLabel.setText(String.format("%.1f", competitionService.getAverageSubmissionRating()));
            avgTimeTakenLabel.setText(String.format("%.1f min", competitionService.getAverageTimeTaken()));
            successRateLabel.setText(String.format("%.1f%%", competitionService.getSuccessRate() * 100));

            // Submissions Over Time (LineChart)
            Map<String, Integer> submissionsOverTime = competitionService.getSubmissionsOverTime();
            XYChart.Series<String, Number> submissionsSeries = new XYChart.Series<>();
            submissionsSeries.setName("Submissions");
            submissionsOverTime.forEach((month, count) -> submissionsSeries.getData().add(new XYChart.Data<>(month, count)));
            submissionsOverTimeChart.getData().clear();
            submissionsOverTimeChart.getData().add(submissionsSeries);
            submissionsOverTimeChart.setLegendVisible(false);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load statistics", e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/backoffice_competitions.fxml"));
            Parent view = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load dashboard", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}
