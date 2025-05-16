package tn.learniverse.controllers;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.animation.ScaleTransition;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Competition;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;
import tn.learniverse.services.ReclamationService;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;
import java.sql.SQLException;

public class back implements Initializable {
    @FXML private Label navUsernameLabel;
    @FXML private TextField searchField;
    @FXML private Label FirstLetter;
    @FXML private Circle circleProfile;
    @FXML private Button logoutButton;
    @FXML private Button Profilebtn;
    @FXML private PieChart reclamationsPieChart;
    @FXML private BarChart<String, Number> reclamationsBarChart;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label nonTraiteLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label traiteLabel;
    @FXML private ComboBox<String> statsTypeComboBox;

    @FXML private VBox statisticsContainer;
    @FXML private Label totalCompetitionsLabel;
    @FXML private Label activeCompetitionsLabel;
    @FXML private Label completedCompetitionsLabel;
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

    private final ReclamationService reclamationService = new ReclamationService();

    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            competitionService = new CompetitionService(DatabaseConnection.getConnection());
            loadStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database", e.getMessage());
        }
        initializeStatsTypeComboBox();
        try {
            loadReclamationsStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            Image image = new Image("file:///C:/wamp64/www/images/icon/profile.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Profilebtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        if(Session.getCurrentUser()!=null){
            this.navUsernameLabel.setText(Session.getCurrentUser().getNom());
            this.FirstLetter.setText(Session.getCurrentUser().getNom().toUpperCase().substring(0, 1));
            Random random = new Random();
            Color randomColor = Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            circleProfile.setFill(randomColor);
        }
    }

    private void initializeStatsTypeComboBox() {
        statsTypeComboBox.getItems().addAll(
                "Toutes les réclamations",
                "Réclamations non traitées",
                "Réclamations en cours",
                "Réclamations traitées"
        );
        statsTypeComboBox.setValue("Toutes les réclamations");

        statsTypeComboBox.setOnAction(event -> {
            try {
                updateBarChart();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateBarChart() throws SQLException {
        String selectedType = statsTypeComboBox.getValue();
        reclamationsBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réclamations par mois");

        String[] allMonths = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        for (String month : allMonths) {
            series.getData().add(new XYChart.Data<>(month, 0));
        }
        List<ReclamationService.MonthlyStats> monthlyStats;
        int year = Calendar.getInstance().get(Calendar.YEAR);

        switch (selectedType) {
            case "Réclamations non traitées":
                monthlyStats = reclamationService.getMonthlyStatsByStatus(year, "Non Traité");
                break;
            case "Réclamations en cours":
                monthlyStats = reclamationService.getMonthlyStatsByStatus(year, "En Cours");
                break;
            case "Réclamations traitées":
                monthlyStats = reclamationService.getMonthlyStatsByStatus(year, "Traité");
                break;
            default:
                monthlyStats = reclamationService.getMonthlyStats(year);
                break;
        }

        for (ReclamationService.MonthlyStats stat : monthlyStats) {
            String monthName = stat.getMonthName();
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getXValue().equals(monthName)) {
                    data.setYValue(stat.getCount());
                    break;
                }
            }
        }

        reclamationsBarChart.getData().add(series);

        CategoryAxis xAxis = (CategoryAxis) reclamationsBarChart.getXAxis();
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelFill(Color.BLACK);
        xAxis.setTickLabelGap(10);

        NumberAxis yAxis = (NumberAxis) reclamationsBarChart.getYAxis();
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.format("%d", object.intValue());
            }

            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string);
            }
        });

        yAxis.setTickUnit(1);
        yAxis.setMinorTickCount(0);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);

        double maxY = series.getData().stream()
                .mapToDouble(data -> data.getYValue().doubleValue())
                .max()
                .orElse(5.0);
        yAxis.setUpperBound(Math.ceil(maxY) + 1);
        String barColor = switch (selectedType) {
            case "Réclamations non traitées" -> "#FF6B6B";
            case "Réclamations en cours" -> "#4ECDC4";
            case "Réclamations traitées" -> "#45B7D1";
            default -> "#2196F3";
        };

        series.getData().forEach(data -> {
            Node node = data.getNode();
            node.setStyle("-fx-bar-fill: " + barColor + ";");
            Tooltip tooltip = new Tooltip(
                    data.getXValue() + "\n" +
                            "Nombre: " + ((Number) data.getYValue()).intValue()
            );
            Tooltip.install(node, tooltip);

            node.setOnMouseEntered(event -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                st.setToX(1.1);
                st.setToY(1.1);
                st.play();
            });

            node.setOnMouseExited(event -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                st.setToX(1);
                st.setToY(1);
                st.play();
            });
        });
    }

    private void loadReclamationsStats() throws SQLException {
        int nonTraiteCount = reclamationService.getCountByStatus("Non Traité");
        int enCoursCount = reclamationService.getCountByStatus("En Cours");
        int traiteCount = reclamationService.getCountByStatus("Traité");
        int total = nonTraiteCount + enCoursCount + traiteCount;
        totalReclamationsLabel.setText(String.valueOf(total));
        nonTraiteLabel.setText(String.valueOf(nonTraiteCount));
        enCoursLabel.setText(String.valueOf(enCoursCount));
        traiteLabel.setText(String.valueOf(traiteCount));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Non Traité", nonTraiteCount),
                new PieChart.Data("En Cours", enCoursCount),
                new PieChart.Data("Traité", traiteCount)
        );

        reclamationsPieChart.setData(pieChartData);
        reclamationsPieChart.setTitle("Distribution des Réclamations");
        pieChartData.forEach(data -> {
            String percentage = String.format("%.1f%%", (data.getPieValue() / total * 100));
            Tooltip tooltip = new Tooltip(
                    data.getName() + "\n" +
                            "Nombre: " + (int) data.getPieValue() + "\n" +
                            "Pourcentage: " + percentage
            );
            Tooltip.install(data.getNode(), tooltip);
            if (data.getName().equals("Non Traité")) {
                data.getNode().setStyle("-fx-pie-color: #FF6B6B;");
            } else if (data.getName().equals("En Cours")) {
                data.getNode().setStyle("-fx-pie-color: #4ECDC4;");
            } else {
                data.getNode().setStyle("-fx-pie-color: #45B7D1;");
            }
        });
        updateBarChart();
    }
    private void loadStatistics() {
        try {
            // Competitions Statistics
            String totalComps = String.valueOf(competitionService.getTotalCompetitions());
            totalCompetitionsLabel.setText(totalComps);

            String activeComps = String.valueOf(competitionService.getActiveCompetitions());
            activeCompetitionsLabel.setText(activeComps);

            String completedComps = String.valueOf(competitionService.getCompletedCompetitions());
            completedCompetitionsLabel.setText(completedComps);

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

    public void Logout(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }

    public void DisplayReclamationsBack(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/backoffice_competitions.fxml");
    }

    public void ToReclamations(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }

    public void ToForums(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackForum.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }


    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/OffreView.fxml");
    }

    public void ToDash(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/Back.fxml");
    }
}
