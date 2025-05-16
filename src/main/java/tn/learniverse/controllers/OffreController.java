package tn.learniverse.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import tn.learniverse.services.OffreDAO;
import tn.learniverse.services.SubscriptionDAO;
import tn.learniverse.entities.Offre;
import tn.learniverse.entities.Subscription;
import javafx.animation.FadeTransition;
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
import javafx.util.Duration;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class OffreController {
    public Circle circleProfile;
    public Label FirstLetter;
    public Label navUsernameLabel;
    public Button Profilebtn;
    public Button logoutButton;
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
    private Label totalOffersText;
    @FXML
    private Label activeOffersText;
    @FXML
    private Label avgPriceText;
    @FXML
    private Label totalSubscriptionsText;
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
        Profilebtn.setDisable(true);
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

            String picturePath = Session.getCurrentUser().getPicture();
            Image image;
            if (picturePath != null) {
                image = new Image("file:///" + picturePath.replace("\\", "/"), 100, 100, false, true);
                if(image.isError()){
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
                }
            }
            else {
                image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
            }
        }
        // Initialize DAOs
        offreDAO = new OffreDAO();
        subscriptionDAO = new SubscriptionDAO();
        
        // Initialize DAO and observable list
        offres = FXCollections.observableArrayList();

        // Initialize filter options first
        filterStatus.getItems().setAll("All", "Active", "Inactive");
        filterStatus.setValue("All");  // Set default value

        // Initialize sort options
        sortBy.getItems().setAll(
            "Price (Low to High)", 
            "Price (High to Low)", 
            "Date (Newest)", 
            "Date (Oldest)", 
            "Name (A-Z)"
        );
        sortBy.setValue("Name (A-Z)");  // Set default value

        // Initialize items per page
        itemsPerPage = new ComboBox<>();
        itemsPerPage.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
        itemsPerPage.setValue(10);  // Set default value

        Platform.runLater(() -> {
            // Ensure CSS is loaded
            Scene scene = cardsContainer.getScene();
            if (scene != null) {
                scene.getStylesheets().clear(); // Remove any existing stylesheets
                String css = getClass().getResource("/styles/offer-display.css").toExternalForm();
                scene.getStylesheets().add(css);
            }
        });

        // Set up the FlowPane
        cardsContainer.setHgap(24);
        cardsContainer.setVgap(24);
        cardsContainer.setPadding(new Insets(20));

        // Set up event handlers
        setupEventHandlers();

        // Load initial data
        loadData();

        // Initialize charts with data
        setupCharts();
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
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String statusFilter = filterStatus.getValue() != null ? filterStatus.getValue() : "All";
        
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
        if (sortBy.getValue() != null) {
            sortOffres(filtered);
        }
        
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

    private Node createOfferCard(Offre offre) {
        // Main card container
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white;"
            + "-fx-background-radius: 18;"
            + "-fx-effect: dropshadow(gaussian, rgba(30,41,59,0.08), 12, 0, 0, 2);"
            + "-fx-padding: 24 24 18 24;"
            + "-fx-spacing: 12;"
            + "-fx-min-width: 340;"
            + "-fx-max-width: 380;"
            + "-fx-border-color: #e0e7ef;"
            + "-fx-border-radius: 18;");
        card.getStyleClass().add("offer-card");
        
        // Content section (top part)
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("card-content");
        
        // Header with title and status
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label(offre.getName());
        titleLabel.getStyleClass().add("offer-title");

        Label statusLabel = new Label("ACTIVE");
        statusLabel.getStyleClass().add("offer-status");

        titleBox.getChildren().addAll(titleLabel, statusLabel);

        // Discount badge if applicable
        if (offre.getDiscount() != null && offre.getDiscount() > 0) {
            Label discountLabel = new Label(String.format("-%.0f%%", offre.getDiscount()));
            discountLabel.getStyleClass().add("discount-badge");
            header.getChildren().addAll(titleBox, discountLabel);
        } else {
            header.getChildren().add(titleBox);
        }

        // Price Section
        VBox priceBox = new VBox(4);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("%.2f$", offre.getPricePerMonth()));
        priceLabel.getStyleClass().add("offer-price");

        Label perMonthLabel = new Label("per month");
        perMonthLabel.getStyleClass().add("per-month");

        priceBox.getChildren().addAll(priceLabel, perMonthLabel);

        // Description
        Label descriptionLabel = new Label(offre.getDescription());
        descriptionLabel.getStyleClass().add("offer-description");
        descriptionLabel.setWrapText(true);

        // Add all content elements
        contentBox.getChildren().addAll(header, priceBox, descriptionLabel);

        // Button section (bottom part)
        VBox buttonBox = new VBox(8);
        buttonBox.getStyleClass().add("button-box");
        
        // Edit and Delete buttons in one row
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> showOffreForm(offre));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteOffre(offre));

        actionButtons.getChildren().addAll(editButton, deleteButton);

        // Email button
        Button emailButton = new Button("Email");
        emailButton.getStyleClass().add("email-button");
        emailButton.setOnAction(e -> sendOfferEmail(offre));

        buttonBox.getChildren().addAll(actionButtons, emailButton);

        // Add all sections to card
        contentBox.getChildren().addAll(buttonBox);

        card.getChildren().add(contentBox);
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
        try {
            // Create dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Advanced Search");

            // Create form elements
            TextField nameField = new TextField();
            nameField.setPromptText("Offer Name");
            
            TextField minPriceField = new TextField(); 
            minPriceField.setPromptText("Min Price");
            
            TextField maxPriceField = new TextField();
            maxPriceField.setPromptText("Max Price");
            
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("All", "Active", "Inactive");
            statusCombo.setValue("All");
            
            DatePicker startDatePicker = new DatePicker();
            startDatePicker.setPromptText("Start Date");
            
            DatePicker endDatePicker = new DatePicker();
            endDatePicker.setPromptText("End Date");

            // Create buttons
            Button searchButton = new Button("Search");
            Button cancelButton = new Button("Cancel");

            // Layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Price Range:"), 0, 1); 
            grid.add(minPriceField, 1, 1);
            grid.add(maxPriceField, 2, 1);
            grid.add(new Label("Status:"), 0, 2);
            grid.add(statusCombo, 1, 2);
            grid.add(new Label("Date Range:"), 0, 3);
            grid.add(startDatePicker, 1, 3);
            grid.add(endDatePicker, 2, 3);

            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(searchButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            grid.add(buttonBox, 0, 4, 3, 1);

            // Handle search
            searchButton.setOnAction(e -> {
                // TODO: Implement search logic using the form values
                dialog.close();
            });

            cancelButton.setOnAction(e -> dialog.close());

            Scene scene = new Scene(grid);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error showing advanced search: " + e.getMessage());
        }
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
    private void handleSubscriptions(ActionEvent event) {
        System.out.println("Subscriptions button clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubscriptionView.fxml"));
            Parent subscriptionView = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(subscriptionView));
        } catch (IOException e) {
            e.printStackTrace();
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

    private void setupCharts() {
        // Initialize data for subscription status distribution chart
       
    }

    private void exportChartData(List<String[]> data, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Data");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(statusDistributionChart.getScene().getWindow());
        if (file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
            
            try {
                switch (extension) {
                    case "csv":
                        try (PrintWriter writer = new PrintWriter(file)) {
                            for (String[] row : data) {
                                writer.println(String.join(",", row));
                            }
                        }
                        break;
                        
                    case "xlsx":
                        try (Workbook workbook = new XSSFWorkbook()) {
                            Sheet sheet = workbook.createSheet(title);
                            int rowNum = 0;
                            for (String[] row : data) {
                                Row excelRow = sheet.createRow(rowNum++);
                                for (int i = 0; i < row.length; i++) {
                                    excelRow.createCell(i).setCellValue(row[i]);
                                }
                            }
                            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                                workbook.write(fileOut);
                            }
                        }
                        break;
                        
                    case "pdf":
                        PdfWriter writer = new PdfWriter(file);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf);
                        
                        document.add(new Paragraph(title));
                        document.add(new Paragraph("\n"));
                        
                        for (String[] row : data) {
                            document.add(new Paragraph(String.join(": ", row)));
                        }
                        
                        document.close();
                        break;
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Chart data exported successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export chart data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPieChart() {
        exportChartData(getChartData(statusDistributionChart), "Offer Status Distribution");
    }

    @FXML
    private void handleExportLineChart() {
        exportChartData(getLineChartData(), "Price Trends");
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(statusDistributionChart.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write headers
                writer.println("Category,Value");
                
                // Write pie chart data
                for (PieChart.Data data : statusDistributionChart.getData()) {
                    writer.println(data.getName() + "," + data.getPieValue());
                }
                
                // Write line chart data
                writer.println("\nMonth,Price");
                for (var series : priceTrendsChart.getData()) {
                    for (var data : series.getData()) {
                        writer.println(data.getXValue() + "," + data.getYValue());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(statusDistributionChart.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                // Create Pie Chart Sheet
                Sheet pieSheet = workbook.createSheet("Offer Distribution");
                Row headerRow = pieSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Category");
                headerRow.createCell(1).setCellValue("Value");
                
                int rowNum = 1;
                for (PieChart.Data data : statusDistributionChart.getData()) {
                    Row row = pieSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(data.getName());
                    row.createCell(1).setCellValue(data.getPieValue());
                }
                
                // Create Line Chart Sheet
                Sheet lineSheet = workbook.createSheet("Price Trends");
                headerRow = lineSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Month");
                headerRow.createCell(1).setCellValue("Price");
                
                rowNum = 1;
                for (var series : priceTrendsChart.getData()) {
                    for (var data : series.getData()) {
                        Row row = lineSheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(data.getXValue().toString());
                        row.createCell(1).setCellValue(data.getYValue().doubleValue());
                    }
                }
                
                // Write to file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(statusDistributionChart.getScene().getWindow());
        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add title
                document.add(new Paragraph("Offer Management Report"));
                
                // Add Pie Chart data
                document.add(new Paragraph("\nOffer Distribution:"));
                for (PieChart.Data data : statusDistributionChart.getData()) {
                    document.add(new Paragraph(
                        String.format("%s: %.2f%%", data.getName(), data.getPieValue())
                    ));
                }
                
                // Add Line Chart data
                document.add(new Paragraph("\nPrice Trends:"));
                for (var series : priceTrendsChart.getData()) {
                    for (var data : series.getData()) {
                        document.add(new Paragraph(
                            String.format("%s: $%.2f", data.getXValue(), data.getYValue())
                        ));
                    }
                }
                
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    private List<String[]> getChartData(PieChart chart) {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Category", "Value"});
        for (PieChart.Data pieData : chart.getData()) {
            data.add(new String[]{pieData.getName(), String.valueOf(pieData.getPieValue())});
        }
        return data;
    }

    private List<String[]> getLineChartData() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Month", "Price"});
        for (var series : priceTrendsChart.getData()) {
            for (var point : series.getData()) {
                data.add(new String[]{
                    point.getXValue().toString(),
                    String.valueOf(point.getYValue())
                });
            }
        }
        return data;
    }

    private void sendOfferEmail(Offre offre) {
        System.out.println("sendOfferEmail called for: " + offre.getName());
        // Test email config (use your real credentials for production)
        String to = "adam.abidi@esprit.tn";
        String from = "adamabidistream12@gmail.com"; // Use a real sender email
        String host = "smtp.gmail.com";
        String username = "adamabidistream12@gmail.com"; // Your Gmail address
        String password = "djzb adru jpxw ydwn"; // Use an App Password if 2FA is enabled

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");
    }

    private void showOfferDetails(Offre offer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Offer Details");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        // Header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20));
        Label titleLabel = new Label(offer.getName());
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        headerBox.getChildren().add(titleLabel);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Description Section
        VBox descriptionSection = new VBox(5, new Label("Description:"), new Label(offer.getDescription()));
        // Conditions Section
        VBox conditionsSection = new VBox(5, new Label("Conditions:"), new Label(offer.getConditions()));
        // Benefits Section
        VBox benefitsSection = new VBox(5, new Label("Benefits:"), new Label(offer.getBenefits()));
        // Validity Section
        VBox validitySection = new VBox(5, new Label("Valid From:"), new Label(offer.getValidFrom() + " to " + offer.getValidUntil()));

        content.getChildren().addAll(
            descriptionSection,
            new Separator(),
            conditionsSection,
            new Separator(),
            benefitsSection,
            new Separator(),
            validitySection
        );

        // --- QR Code Promo Section ---
        try {
            tn.learniverse.services.QRCodeService qrCodeService = new tn.learniverse.services.QRCodeService();
            String promoCode = "SPECIAL-" + offer.getId();
            String qrCodeBase64 = qrCodeService.generateQRCode(promoCode);
            byte[] imageBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
            javafx.scene.image.Image qrImage = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(imageBytes));
            javafx.scene.image.ImageView qrImageView = new javafx.scene.image.ImageView(qrImage);
            qrImageView.setFitWidth(180);
            qrImageView.setFitHeight(180);
            qrImageView.setPreserveRatio(true);
            Label promoLabel = new Label("Scan here to get a code promo spéciale offre!");
            promoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2962FF;");
            VBox promoBox = new VBox(10, promoLabel, qrImageView);
            promoBox.setAlignment(Pos.CENTER);
            content.getChildren().add(new Separator());
            content.getChildren().add(promoBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // --- End QR Code Promo Section ---

        dialogPane.setHeader(headerBox);
        dialogPane.setContent(content);

        dialog.showAndWait();
    }

    @FXML
    private void showOffers(ActionEvent event) {
        // Reload or focus the offers dashboard
        loadData();
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        // Show or refresh the statistics of offers
        updateStatistics();
    }
    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
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