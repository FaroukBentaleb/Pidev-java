package tn.learniverse.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ComboBox;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;
import tn.learniverse.services.ReclamationService;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class Dashboard implements Initializable {
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

    private final ReclamationService reclamationService = new ReclamationService();

    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    public void Logout(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }

    public void DisplayReclamationsBack(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }
}
