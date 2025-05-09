package tn.learniverse.controllers;

import tn.learniverse.services.SubscriptionDAO;
import tn.learniverse.entities.Subscription;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.application.Platform;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SubscriptionController {
    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> sortBy;
    @FXML private ComboBox<String> itemsPerPage;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Text totalSubscriptionsText;
    @FXML private Text activeSubscriptionsText;
    @FXML private Text avgDurationText;
    @FXML private Text totalCoursesText;
    @FXML private PieChart statusDistributionChart;
    @FXML private LineChart<String, Number> subscriptionTrendsChart;
    @FXML private Text paginationInfo;
    @FXML private Text totalPagesText;
    @FXML private TextField jumpToPageField;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    private SubscriptionDAO subscriptionDAO;
    private ObservableList<Subscription> subscriptions;
    private static final int DEFAULT_ITEMS_PER_PAGE = 10;
    private int currentPage = 1;

    @FXML
    public void initialize() {
        subscriptionDAO = new SubscriptionDAO();
        subscriptions = FXCollections.observableArrayList();

        // Initialize ComboBoxes
        initializeComboBoxes();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Initial load
        refreshData();
        
        // Initialize charts
        initializeCharts();

        // Add stylesheet for modern sidebar
        Platform.runLater(() -> {
            Scene scene = cardsContainer.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            }
        });
    }

    private void initializeComboBoxes() {
        filterStatus.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        filterStatus.setValue("All");

        sortBy.setItems(FXCollections.observableArrayList("Date (Newest)", "Date (Oldest)", "Status"));
        sortBy.setValue("Date (Newest)");

        itemsPerPage.setItems(FXCollections.observableArrayList("10", "20", "50"));
        itemsPerPage.setValue("10");
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSubscriptions());
        filterStatus.setOnAction(event -> filterSubscriptions());
        sortBy.setOnAction(event -> filterSubscriptions());
        itemsPerPage.setOnAction(event -> {
            currentPage = 1;
            filterSubscriptions();
        });

        addButton.setOnAction(event -> showSubscriptionForm(null));
        refreshButton.setOnAction(event -> refreshData());
        
        prevPageButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                filterSubscriptions();
            }
        });
        
        nextPageButton.setOnAction(event -> {
            int totalPages = calculateTotalPages();
            if (currentPage < totalPages) {
                currentPage++;
                filterSubscriptions();
            }
        });
        
        jumpToPageField.setOnAction(event -> {
            try {
                int page = Integer.parseInt(jumpToPageField.getText());
                if (page > 0 && page <= calculateTotalPages()) {
                    currentPage = page;
                    filterSubscriptions();
                }
            } catch (NumberFormatException e) {
                // Invalid input - ignore
            }
        });
    }

    private void refreshData() {
        subscriptions.clear();
        subscriptions.addAll(subscriptionDAO.readAll());
        updateStatistics();
        filterSubscriptions();
    }

    private void updateStatistics() {
        int total = subscriptions.size();
        long active = subscriptions.stream().filter(s -> "Active".equals(s.getStatus())).count();
        double avgDuration = subscriptions.stream()
                .mapToLong(s -> s.getDurationInDays())
                .average()
                .orElse(0.0);
        long totalCourses = subscriptions.stream()
                .map(Subscription::getCourseId)
                .distinct()
                .count();

        totalSubscriptionsText.setText(String.valueOf(total));
        activeSubscriptionsText.setText(String.valueOf(active));
        avgDurationText.setText(String.format("%.1f", avgDuration));
        totalCoursesText.setText(String.valueOf(totalCourses));
    }

    private void initializeCharts() {
        // Initialize Status Distribution Chart
        updateStatusDistributionChart();
        
        // Initialize Subscription Trends Chart
        updateSubscriptionTrendsChart();
    }

    private void updateStatusDistributionChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        long activeCount = subscriptions.stream().filter(s -> "Active".equals(s.getStatus())).count();
        long inactiveCount = subscriptions.size() - activeCount;
        
        pieChartData.add(new PieChart.Data("Active", activeCount));
        pieChartData.add(new PieChart.Data("Inactive", inactiveCount));
        
        statusDistributionChart.setData(pieChartData);
    }

    private void updateSubscriptionTrendsChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Subscriptions");
        
        // Add your data points here
        // Example:
        series.getData().add(new XYChart.Data<>("Jan", 50));
        series.getData().add(new XYChart.Data<>("Feb", 80));
        series.getData().add(new XYChart.Data<>("Mar", 110));
        
        subscriptionTrendsChart.getData().clear();
        subscriptionTrendsChart.getData().add(series);
    }

    private void filterSubscriptions() {
        List<Subscription> filteredList = subscriptionDAO.readAll();
        // Apply filters based on search, status, and sort criteria
        
        int itemsPerPageValue = Integer.parseInt(itemsPerPage.getValue());
        int totalPages = calculateTotalPages();
        
        updatePagination(totalPages);
        displayCurrentPage(filteredList, itemsPerPageValue);
    }

    private int calculateTotalPages() {
        int itemsPerPageValue = Integer.parseInt(itemsPerPage.getValue());
        return (subscriptions.size() + itemsPerPageValue - 1) / itemsPerPageValue;
    }

    private void updatePagination(int totalPages) {
        totalPagesText.setText(String.valueOf(totalPages));
        paginationInfo.setText(String.format("Page %d of %d", currentPage, totalPages));
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private void displayCurrentPage(List<Subscription> filteredList, int itemsPerPage) {
        cardsContainer.getChildren().clear();
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredList.size());
        
        for (int i = fromIndex; i < toIndex; i++) {
            cardsContainer.getChildren().add(createSubscriptionCard(filteredList.get(i)));
        }
    }

    private VBox createSubscriptionCard(Subscription subscription) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Card Header with ID and Status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8 8 0 0;");

        Label idLabel = new Label("Subscription #" + subscription.getId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a237e;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(subscription.getStatus());
        statusLabel.getStyleClass().add("status-badge");
        if ("active".equalsIgnoreCase(subscription.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4;");
        } else {
            statusLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4;");
        }

        header.getChildren().addAll(idLabel, spacer, statusLabel);

        // Card Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: white;");

        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        // Info rows with icons
        HBox dateRow = createInfoRow(FontAwesomeIcon.CALENDAR, "Date: " + subscription.getDateEarned().format(formatter));
        HBox courseRow = createInfoRow(FontAwesomeIcon.GRADUATION_CAP, "Course ID: " + subscription.getCourseId());
        HBox userRow = createInfoRow(FontAwesomeIcon.USER, "User ID: " + subscription.getUserId());
        HBox offerRow = createInfoRow(FontAwesomeIcon.TAG, "Offer ID: " + subscription.getOffreId());

        content.getChildren().addAll(dateRow, courseRow, userRow, offerRow);

        // Card Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 15, 15, 15));
        actions.setStyle("-fx-border-color: transparent #eee transparent transparent; -fx-border-width: 1;");

        Button editButton = new Button("Edit");
        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
        editIcon.setStyle("-fx-fill: white;");
        editButton.setGraphic(editIcon);
        editButton.getStyleClass().add("edit-button");
        editButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-min-width: 100;");
        editButton.setOnAction(e -> showSubscriptionForm(subscription));

        Button deleteButton = new Button("Delete");
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.setStyle("-fx-fill: white;");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-min-width: 100;");
        deleteButton.setOnAction(e -> deleteSubscription(subscription));

        actions.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(header, content, actions);
        return card;
    }

    private HBox createInfoRow(FontAwesomeIcon iconType, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setStyle("-fx-fill: #1a237e;");
        icon.setGlyphSize(16);
        
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #495057;");
        
        row.getChildren().addAll(icon, label);
        return row;
    }

    @FXML
    private void handleOfferManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OffreView.fxml"));
            Parent root = loader.load();
            Scene scene = addButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert("Error loading Offer Management view: " + e.getMessage());
        }
    }

    private void showSubscriptionForm(Subscription subscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubscriptionForm.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(subscription == null ? "New Subscription" : "Edit Subscription");
            stage.setScene(new Scene(root));
            
            SubscriptionFormController controller = loader.getController();
            controller.setSubscription(subscription);
            
            // Show the dialog and wait for it to close
            stage.showAndWait();
            
            // Refresh the data after the dialog is closed
            refreshData();
        } catch (IOException e) {
            showAlert("Error loading form: " + e.getMessage());
        }
    }

    private void deleteSubscription(Subscription subscription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Subscription");
        alert.setContentText("Are you sure you want to delete this subscription?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            subscriptionDAO.delete(subscription.getId());
            refreshData();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 