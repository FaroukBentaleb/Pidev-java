package com.learniverse.controller;

import com.learniverse.dao.OffreDAO;
import com.learniverse.dao.SubscriptionDAO;
import com.learniverse.model.Offre;
import com.learniverse.model.Subscription;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OffreController {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterStatus;
    @FXML
    private ComboBox<String> sortBy;
    @FXML
    private ComboBox<Integer> itemsPerPage;
    @FXML
    private Button addButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button advancedSearchButton;
    @FXML
    private Button clearFiltersButton;
    @FXML
    private HBox filterChipsContainer;
    @FXML
    private Pagination pagination;
    @FXML
    private Text totalOffersText;
    @FXML
    private Text activeOffersText;
    @FXML
    private Text avgPriceText;
    @FXML
    private Text totalSubscriptionsText;
    @FXML
    private PieChart statusDistributionChart;
    @FXML
    private LineChart<String, Number> priceTrendsChart;
    @FXML
    private Text paginationInfo;
    @FXML
    private TextField jumpToPageField;
    @FXML
    private Text totalPagesText;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;

    private OffreDAO offreDAO;
    private SubscriptionDAO subscriptionDAO;
    private ObservableList<Offre> offres;
    private int currentPage = 1;
    private int itemsPerPageValue = 10;
    private Map<String, String> activeFilters = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize DAOs
        offreDAO = new OffreDAO();
        subscriptionDAO = new SubscriptionDAO();
        
        // Initialize DAO and observable list
        offres = FXCollections.observableArrayList();

        // Initialize filter options
        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("All");

        // Initialize sort options
        sortBy.getItems().addAll(
            "Price (Low to High)", 
            "Price (High to Low)", 
            "Date (Newest)", 
            "Date (Oldest)", 
            "Name (A-Z)"
        );
        sortBy.setValue("Name (A-Z)");

        // Initialize items per page
        itemsPerPage.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
        itemsPerPage.setValue(10);

        // Set up the FlowPane
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setPadding(new Insets(20));

        // Set up event handlers
        setupEventHandlers();

        // Load initial data
        loadData();
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((obs, old, newValue) -> filterOffres());
        filterStatus.valueProperty().addListener((obs, old, newValue) -> filterOffres());
        sortBy.valueProperty().addListener((obs, old, newValue) -> filterOffres());
        itemsPerPage.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                itemsPerPageValue = newValue;
                filterOffres();
            }
        });

        // Button handlers
        addButton.setOnAction(e -> showOffreForm(null));
        refreshButton.setOnAction(e -> loadData());

        // Pagination handlers
        prevPageButton.setOnAction(e -> navigateToPage(currentPage - 1));
        nextPageButton.setOnAction(e -> navigateToPage(currentPage + 1));
        jumpToPageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.matches("\\d*")) {
                try {
                    int page = newVal.isEmpty() ? 1 : Integer.parseInt(newVal);
                    navigateToPage(page);
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers
                }
            }
        });
    }

    private void loadData() {
        try {
            List<Offre> loadedOffres = offreDAO.findAll();
            System.out.println("Loaded " + loadedOffres.size() + " offers from database");
            offres.clear();
            offres.addAll(loadedOffres);
            updateStatistics();
            updateCharts();
            filterOffres();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading data: " + e.getMessage());
        }
    }

    private void filterOffres() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = filterStatus.getValue();
        
        ObservableList<Offre> filtered = offres.stream()
            .filter(offre -> 
                (searchText.isEmpty() || 
                 offre.getName().toLowerCase().contains(searchText) ||
                 offre.getDescription().toLowerCase().contains(searchText)) &&
                (statusFilter.equals("All") || offre.isActive() == statusFilter.equals("Active"))
            )
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        System.out.println("Filtered to " + filtered.size() + " offers");
        
        // Apply sorting
        sortOffres(filtered);
        
        // Update UI
        updatePagination(filtered.size());
        displayCurrentPage(filtered);
    }

    private void sortOffres(ObservableList<Offre> offres) {
        String sortOption = sortBy.getValue();
        offres.sort((o1, o2) -> {
            switch (sortOption) {
                case "Price (Low to High)":
                    return Double.compare(o1.getPricePerMonth(), o2.getPricePerMonth());
                case "Price (High to Low)":
                    return Double.compare(o2.getPricePerMonth(), o1.getPricePerMonth());
                case "Date (Newest)":
                    return o2.getValidFrom().compareTo(o1.getValidFrom());
                case "Date (Oldest)":
                    return o1.getValidFrom().compareTo(o2.getValidFrom());
                case "Name (A-Z)":
                    return o1.getName().compareToIgnoreCase(o2.getName());
                default:
                    return 0;
            }
        });
    }

    private void updatePagination(int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPageValue);
        totalPagesText.setText(String.valueOf(totalPages));
        paginationInfo.setText(String.format(
            "Showing %d-%d of %d offers",
            (currentPage - 1) * itemsPerPageValue + 1,
            Math.min(currentPage * itemsPerPageValue, totalItems),
            totalItems
        ));
        
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private void displayCurrentPage(ObservableList<Offre> filteredOffres) {
        cardsContainer.getChildren().clear();
        
        int startIndex = (currentPage - 1) * itemsPerPageValue;
        int endIndex = Math.min(startIndex + itemsPerPageValue, filteredOffres.size());
        
        System.out.println("Displaying offers from index " + startIndex + " to " + endIndex);
        System.out.println("Items per page: " + itemsPerPageValue);
        System.out.println("Current page: " + currentPage);
        
        if (filteredOffres.isEmpty()) {
            // Show "No offers found" message
            Text noOffersText = new Text("No offers found");
            noOffersText.getStyleClass().add("no-results-text");
            cardsContainer.getChildren().add(noOffersText);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            try {
                Offre offre = filteredOffres.get(i);
                System.out.println("Creating card for offer: " + offre.getName());
                Node card = createOfferCard(offre);
                
                // Set a fixed width for the card to ensure proper layout
                if (card instanceof VBox) {
                    ((VBox) card).setPrefWidth(350);
                    ((VBox) card).setMaxWidth(350);
                }
                
                cardsContainer.getChildren().add(card);
                
                // Add animation
                FadeTransition fade = new FadeTransition(Duration.millis(300), card);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            } catch (Exception e) {
                System.err.println("Error creating card: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Ensure proper layout of cards
        cardsContainer.setPrefWrapLength(cardsContainer.getWidth());
    }

    private VBox createOfferCard(Offre offre) {
        VBox card = new VBox(10); // Increased spacing between elements
        card.getStyleClass().add("offer-card");
        card.setPadding(new Insets(15));
        card.setMinWidth(300);
        card.setMaxWidth(350);

        // Header section with status badge
        HBox header = new HBox(10);
        header.getStyleClass().add("offer-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label(offre.getName());
        titleLabel.getStyleClass().add("offer-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);

        Label statusLabel = new Label(offre.isActive() ? "ACTIVE" : "INACTIVE");
        statusLabel.getStyleClass().addAll("status-badge", 
            offre.isActive() ? "status-active" : "status-inactive");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(titleLabel, spacer, statusLabel);

        // Price section
        VBox priceSection = new VBox(5);
        priceSection.getStyleClass().add("price-section");
        priceSection.setAlignment(Pos.CENTER_LEFT);
        priceSection.setPadding(new Insets(10, 0, 10, 0));

        Label priceLabel = new Label(String.format("%.2f$", offre.getPricePerMonth()));
        priceLabel.getStyleClass().add("price-amount");

        Label perMonthLabel = new Label("per month");
        perMonthLabel.getStyleClass().add("price-period");

        priceSection.getChildren().addAll(priceLabel, perMonthLabel);

        if (offre.getDiscount() != null && offre.getDiscount() > 0) {
            Label discountLabel = new Label(String.format("-%.0f%%", offre.getDiscount()));
            discountLabel.getStyleClass().add("discount-badge");
            priceSection.getChildren().add(discountLabel);
        }

        // Description section with max height
        Text descriptionText = new Text(offre.getDescription() != null ? offre.getDescription() : "");
        descriptionText.getStyleClass().add("description-text");
        descriptionText.setWrappingWidth(280);

        VBox descriptionBox = new VBox(descriptionText);
        descriptionBox.setMaxHeight(100);
        descriptionBox.getStyleClass().add("description-box");

        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.getStyleClass().add("action-buttons");
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> showOffreForm(offre));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteOffre(offre));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Add Subscribe button
        Button subscribeButton = new Button("Subscribe");
        subscribeButton.getStyleClass().add("subscribe-button");
        FontAwesomeIconView checkIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        checkIcon.setFill(Color.WHITE);
        checkIcon.setSize("16");
        subscribeButton.setGraphic(checkIcon);
        subscribeButton.setOnAction(e -> handleSubscription(offre));

        // Add button to card
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(subscribeButton);
        buttonsBox.getChildren().add(buttonBox);

        // Add all sections to the card
        card.getChildren().addAll(
            header,
            priceSection,
            new Separator(),
            descriptionBox,
            buttonsBox
        );

        return card;
    }

    private void showOffreForm(Offre offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OffreForm.fxml"));
            Parent root = loader.load();

            OffreFormController controller = loader.getController();
            if (offre != null) {
                controller.setOffre(offre);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(offre == null ? "Add New Offer" : "Edit Offer");
            stage.setScene(new Scene(root));

            // Refresh the view when the form is closed
            stage.setOnHidden(e -> loadData());

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening form: " + e.getMessage());
        }
    }

    private void deleteOffre(Offre offre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Offer");
        alert.setHeaderText("Delete Offer");
        alert.setContentText("Are you sure you want to delete this offer?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            offreDAO.delete(offre.getId());
            loadData();
        }
    }

    private void updateStatistics() {
        totalOffersText.setText(String.valueOf(offres.size()));
        activeOffersText.setText(String.valueOf(
            offres.stream().filter(Offre::isActive).count()
        ));
        avgPriceText.setText(String.format("$%.2f",
            offres.stream().mapToDouble(Offre::getPricePerMonth).average().orElse(0)
        ));
        totalSubscriptionsText.setText(String.valueOf(
            offres.stream().mapToInt(Offre::getMaxSubscriptions).sum()
        ));
    }

    private void updateCharts() {
        if (statusDistributionChart != null && priceTrendsChart != null) {
            // Update status distribution chart
            Map<String, Long> statusCounts = offres.stream()
                .collect(Collectors.groupingBy(
                    offre -> offre.isActive() ? "Active" : "Inactive",
                    Collectors.counting()
                ));
            
            statusDistributionChart.getData().clear();
            statusCounts.forEach((status, count) -> {
                PieChart.Data slice = new PieChart.Data(status, count);
                statusDistributionChart.getData().add(slice);
            });

            // Update price trends chart
            priceTrendsChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Price");
            
            Map<String, Double> monthlyAverages = offres.stream()
                .filter(offre -> offre.getValidFrom() != null)
                .collect(Collectors.groupingBy(
                    offre -> offre.getValidFrom().getMonth().toString(),
                    Collectors.averagingDouble(Offre::getPricePerMonth)
                ));
            
            monthlyAverages.forEach((month, avg) -> {
                series.getData().add(new XYChart.Data<>(month, avg));
            });
            
            priceTrendsChart.getData().add(series);
        }
    }

    private void showAdvancedSearch() {
        // Implement advanced search dialog
        // This would open a new window with more search options
    }

    private void navigateToPage(int page) {
        int totalPages = Integer.parseInt(totalPagesText.getText());
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            jumpToPageField.setText(String.valueOf(page));
            filterOffres();
        }
    }

    @FXML
    private void handleSubscriptionManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubscriptionView.fxml"));
            Parent root = loader.load();
            
            Scene currentScene = cardsContainer.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            newScene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading Subscription Management view: " + e.getMessage());
        }
    }

    private void handleSubscription(Offre offre) {
        try {
            // Create new subscription
            Subscription subscription = new Subscription();
            subscription.setOffreId(offre.getId());
            subscription.setUserId(1); // TODO: Replace with actual logged-in user ID
            subscription.setDateEarned(LocalDateTime.now());
            subscription.setStatus("Active");
            subscription.setCourseId(offre.getCourseId());
            
            // Save subscription
            subscriptionDAO.create(subscription);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully subscribed to " + offre.getName());

            // Refresh the view
            loadData();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to subscribe: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 