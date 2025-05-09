package tn.learniverse.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import tn.learniverse.services.OffreDAO;
import tn.learniverse.entities.Offre;
import tn.learniverse.services.QRCodeService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import tn.learniverse.services.SubscriptionDAO;

public class OffreDisplayController {
    @FXML
    private FlowPane offersContainer;
    
    @FXML
    private Button offersButton;
    
    @FXML
    private Button subscribeButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortByComboBox;
    
    @FXML
    private Pagination pagination;
    
    private Dialog<Void> qrDialog;
    private ImageView qrImageView;
    
    private OffreDAO offreDAO;
    private QRCodeService qrCodeService;
    private Offre currentOffer;
    
    private List<Offre> allOffers;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        offreDAO = new OffreDAO();
        offersContainer.setHgap(15);
        offersContainer.setVgap(15);
        offersContainer.setPadding(new Insets(15));
        
        // Initialize lists and load offers
        allOffers = offreDAO.findAll();
        
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // If search field is cleared, show all offers
                allOffers = offreDAO.findAll();
                filterAndDisplayOffers();
            } else {
                // Filter based on search text
                filterAndDisplayOffers();
            }
        });
        
        // Set up sorting combo box
        sortByComboBox.getItems().addAll(
            "Price: Low to High",
            "Price: High to Low",
            "Newest First"
        );
        sortByComboBox.setValue("Newest First");
        sortByComboBox.setOnAction(e -> filterAndDisplayOffers());
        
        // Initialize pagination
        int pageCount = (int) Math.ceil((double) allOffers.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayOffersForCurrentPage();
        });
        
        // Initial display
        filterAndDisplayOffers();
        
        // Add stylesheet after scene is created
        offersContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                String css = getClass().getResource("/styles/offer-display.css").toExternalForm();
                newScene.getStylesheets().add(css);
            }
        });
        
        // Initialize QR code dialog
        initializeQRDialog();
        
        qrCodeService = new QRCodeService();
        
        // Initialize subscribe button if it exists
        if (subscribeButton != null) {
            subscribeButton.setOnAction(event -> showPaymentDialog());
        }
    }

    private void initializeQRDialog() {
        qrDialog = new Dialog<>();
        qrDialog.setTitle("QR Code");
        qrDialog.initModality(Modality.APPLICATION_MODAL);

        // Create dialog content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        qrImageView = new ImageView();
        qrImageView.setFitWidth(250);
        qrImageView.setFitHeight(250);
        qrImageView.setPreserveRatio(true);

        Button downloadButton = new Button("Download QR Code");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setStyle("-fx-background-color: #2962FF; -fx-text-fill: white; -fx-font-size: 14px;");
        downloadButton.setOnAction(e -> downloadQRCode());

        content.getChildren().addAll(qrImageView, downloadButton);

        // Set up dialog
        DialogPane dialogPane = qrDialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        
        // Add stylesheet
        String css = getClass().getResource("/styles/offer-display.css").toExternalForm();
        dialogPane.getStylesheets().add(css);
    }

    private void loadOffersView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OffreDisplay.fxml"));
            Parent root = loader.load();
            
            Scene currentScene = offersButton.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            // Create new scene with the same size as current scene
            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error Loading View", "Could not load the offers view. Please try again.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void filterAndDisplayOffers() {
        List<Offre> filteredOffers = allOffers.stream()
            .filter(offer -> {
                String searchText = searchField.getText().toLowerCase();
                return searchText.isEmpty() ||
                    offer.getName().toLowerCase().contains(searchText) ||
                    offer.getDescription().toLowerCase().contains(searchText);
            })
            .sorted((o1, o2) -> {
                String sortBy = sortByComboBox.getValue();
                switch (sortBy) {
                    case "Price: Low to High":
                        return Double.compare(o1.getPricePerMonth(), o2.getPricePerMonth());
                    case "Price: High to Low":
                        return Double.compare(o2.getPricePerMonth(), o1.getPricePerMonth());
                    case "Newest First":
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    default:
                        return 0;
                }
            })
            .collect(Collectors.toList());
            
        // Update pagination
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredOffers.size() / ITEMS_PER_PAGE));
        pagination.setPageCount(pageCount);
        
        // Store filtered offers and display current page
        allOffers = filteredOffers;
        displayOffersForCurrentPage();
    }

    private void displayOffersForCurrentPage() {
        int currentPage = pagination.getCurrentPageIndex();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allOffers.size());
        
        List<Offre> currentPageOffers = allOffers.subList(startIndex, endIndex);
        
        offersContainer.getChildren().clear();
        currentPageOffers.forEach(offer -> offersContainer.getChildren().add(createOfferCard(offer)));
    }

    private VBox createOfferCard(Offre offer) {
        VBox card = new VBox();
        card.getStyleClass().add("offer-card");
        card.setSpacing(5);

        // Header with title and status
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(offer.getName());
        titleLabel.getStyleClass().add("offer-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label("ACTIVE");
        statusLabel.getStyleClass().addAll("status-badge", "active");

        header.getChildren().addAll(titleLabel, statusLabel);

        // Price section
        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);
        priceBox.getStyleClass().add("price-section");

        Label currencyLabel = new Label("$");
        currencyLabel.getStyleClass().add("price-currency");
        
        Label priceLabel = new Label(String.format("%.2f", offer.getPricePerMonth()));
        priceLabel.getStyleClass().add("price");
        
        Label periodLabel = new Label("per month");
        periodLabel.getStyleClass().add("price-period");

        VBox priceInfo = new VBox(2);
        priceInfo.setAlignment(Pos.CENTER_LEFT);
        
        HBox priceRow = new HBox(2);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        priceRow.getChildren().addAll(currencyLabel, priceLabel);
        
        priceInfo.getChildren().addAll(priceRow, periodLabel);
        
        priceBox.getChildren().add(priceInfo);

        Label countdownLabel = new Label();
        countdownLabel.getStyleClass().add("countdown-label");

        if (offer.getDiscount() != null && offer.getDiscount() > 0 && offer.getValidUntil() != null) {
            updateCountdown(countdownLabel, offer.getValidUntil());
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateCountdown(countdownLabel, offer.getValidUntil()))
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            priceBox.getChildren().add(countdownLabel);
        }

        // Description
        Label descriptionLabel = new Label(offer.getDescription());
        descriptionLabel.getStyleClass().add("description");
        descriptionLabel.setWrapText(true);

        // Separators
        Separator topSeparator = new Separator();
        Separator bottomSeparator = new Separator();

        // Buttons
        VBox buttonBox = new VBox(8); // vertical gap
        buttonBox.getStyleClass().add("button-box");

        // First row: Show Details + Subscribe
        HBox topRow = new HBox(8);
        // Show Details button with icon
        Button detailsButton = new Button("Show Details");
        detailsButton.getStyleClass().addAll("show-details-button");
        FontAwesomeIconView detailsIcon = new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE);
        detailsIcon.setStyle("-fx-fill: #1976D2;");
        detailsIcon.setGlyphSize(16);
        detailsButton.setGraphic(detailsIcon);
        detailsButton.setContentDisplay(ContentDisplay.LEFT);
        detailsButton.setGraphicTextGap(8);
        detailsButton.setOnAction(e -> showOfferDetails(offer));

        // Subscribe button with icon
        Button subscribeButton = new Button("Subscribe");
        subscribeButton.getStyleClass().addAll("subscribe-button");
        FontAwesomeIconView subscribeIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        subscribeIcon.setStyle("-fx-fill: white;");
        subscribeIcon.setGlyphSize(16);
        subscribeButton.setGraphic(subscribeIcon);
        subscribeButton.setContentDisplay(ContentDisplay.LEFT);
        subscribeButton.setGraphicTextGap(8);
        subscribeButton.setOnAction(e -> handleSubscribe(offer));
        topRow.getChildren().addAll(detailsButton, subscribeButton);

        // Second row: Add to Calendar with icon, centered
        HBox calendarRow = new HBox();
        calendarRow.setAlignment(Pos.CENTER);
        Button calendarButton = new Button("Add to Calendar");
        calendarButton.getStyleClass().addAll("show-details-button", "calendar-button");
        FontAwesomeIconView calendarIcon = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR);
        calendarIcon.setStyle("-fx-fill: #1976D2;");
        calendarIcon.setGlyphSize(16);
        calendarButton.setGraphic(calendarIcon);
        calendarButton.setContentDisplay(ContentDisplay.LEFT);
        calendarButton.setGraphicTextGap(8);
        calendarButton.setOnAction(e -> handleAddToCalendar(offer));
        calendarRow.getChildren().add(calendarButton);

        buttonBox.getChildren().addAll(topRow, calendarRow);

        // Add all components to card
        card.getChildren().addAll(
            header,
            topSeparator,
            priceBox,
            bottomSeparator,
            descriptionLabel,
            buttonBox
        );

        return card;
    }

    private void showOfferDetails(Offre offer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Offer Details");
        
        // Load stylesheet
        DialogPane dialogPane = dialog.getDialogPane();
        String css = getClass().getResource("/styles/offer-display.css").toExternalForm();
        dialogPane.getStylesheets().add(css);
        dialogPane.getStyleClass().add("modern-dialog");
        
        // Create custom header
        VBox headerBox = new VBox(10);
        headerBox.getStyleClass().add("dialog-header");
        headerBox.setPadding(new Insets(30));
        
        // Price and title section
        HBox priceHeader = new HBox();
        priceHeader.setAlignment(Pos.CENTER_LEFT);
        priceHeader.setSpacing(20);
        
        // Price display
        VBox priceBox = new VBox(5);
        HBox priceRow = new HBox(4);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        
        Label currencyLabel = new Label("$");
        Label priceLabel = new Label(String.format("%.2f", offer.getPricePerMonth()));
        currencyLabel.getStyleClass().addAll("price-label", "currency-label");
        priceLabel.getStyleClass().add("price-label");
        
        priceRow.getChildren().addAll(currencyLabel, priceLabel);
        
        Label periodLabel = new Label("per month");
        periodLabel.getStyleClass().add("period-label");
        
        priceBox.getChildren().addAll(priceRow, periodLabel);
        
        // Title and badges
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(offer.getName());
        titleLabel.getStyleClass().add("dialog-title");
        
        // Badges container
        HBox badgesBox = new HBox(10);
        badgesBox.setAlignment(Pos.CENTER_LEFT);
        
        // Status badge
        Label statusLabel = new Label("ACTIVE");
        statusLabel.getStyleClass().add("status-badge-large");
        
        // Discount badge if applicable
        if (offer.getDiscount() != null && offer.getDiscount() > 0) {
            Label discountLabel = new Label(String.format("-%.0f%%", offer.getDiscount()));
            discountLabel.getStyleClass().add("discount-badge-large");
            badgesBox.getChildren().add(discountLabel);
        }
        
        badgesBox.getChildren().add(statusLabel);
        titleBox.getChildren().addAll(titleLabel, badgesBox);
        
        priceHeader.getChildren().addAll(priceBox, titleBox);
        headerBox.getChildren().add(priceHeader);
        
        // Main content
        VBox content = new VBox(25);
        content.getStyleClass().add("dialog-content");
        content.setPadding(new Insets(30));

        // Description Section with icon
        VBox descriptionSection = createDetailSection(
            "Description",
            offer.getDescription(),
            "INFO_CIRCLE"
        );

        // Conditions Section with icon
        VBox conditionsSection = createDetailSection(
            "Conditions",
            offer.getConditions(),
            "EXCLAMATION_CIRCLE"
        );

        // Benefits Section with checkmarks
        VBox benefitsSection = new VBox(15);
        benefitsSection.getStyleClass().add("detail-section");
        
        HBox benefitsHeader = new HBox(10);
        benefitsHeader.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView starIcon = new FontAwesomeIconView(FontAwesomeIcon.STAR);
        starIcon.getStyleClass().add("section-icon");
        
        Label benefitsTitle = new Label("Benefits");
        benefitsTitle.getStyleClass().add("section-title");
        
        benefitsHeader.getChildren().addAll(starIcon, benefitsTitle);
        
        FlowPane benefitsList = new FlowPane();
        benefitsList.setHgap(15);
        benefitsList.setVgap(10);
        benefitsList.setPrefWrapLength(500);
        
        String[] benefits = offer.getBenefits().split("\n");
        for (String benefit : benefits) {
            if (benefit != null && !benefit.trim().isEmpty()) {
                HBox benefitItem = new HBox(12);
                benefitItem.getStyleClass().add("benefit-item");
                benefitItem.setAlignment(Pos.CENTER_LEFT);
                
                FontAwesomeIconView checkIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
                checkIcon.setSize("16");
                checkIcon.getStyleClass().add("check-icon");
                
                Label benefitText = new Label(benefit.trim());
                benefitText.getStyleClass().add("benefit-text");
                benefitText.setWrapText(true);
                benefitText.setStyle("-fx-padding: 0 0 0 5;");
                
                benefitItem.getChildren().addAll(checkIcon, benefitText);
                benefitsList.getChildren().add(benefitItem);
            }
        }
        
        benefitsSection.getChildren().addAll(benefitsHeader, benefitsList);

        // Validity Period Section
        VBox validitySection = new VBox(15);
        validitySection.getStyleClass().add("detail-section");
        
        HBox validityHeader = new HBox(10);
        validityHeader.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView clockIcon = new FontAwesomeIconView(FontAwesomeIcon.CLOCK_ALT);
        clockIcon.getStyleClass().add("section-icon");
        
        Label validityTitle = new Label("Validity Period");
        validityTitle.getStyleClass().add("section-title");
        
        validityHeader.getChildren().addAll(clockIcon, validityTitle);
        
        GridPane validityGrid = new GridPane();
        validityGrid.setHgap(20);
        validityGrid.setVgap(10);
        validityGrid.getStyleClass().add("validity-grid");
        
        // From date
        HBox fromBox = new HBox(10);
        fromBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView fromIcon = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR);
        fromIcon.setSize("16");
        fromIcon.getStyleClass().add("date-icon");
        Label fromLabel = new Label("Valid From:");
        fromLabel.getStyleClass().add("date-label");
        Label fromValue = new Label(offer.getValidFrom().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        fromValue.getStyleClass().add("date-value");
        fromBox.getChildren().addAll(fromIcon, fromLabel, fromValue);
        
        // Until date
        HBox untilBox = new HBox(10);
        untilBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView untilIcon = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR);
        untilIcon.setSize("16");
        untilIcon.getStyleClass().add("date-icon");
        Label untilLabel = new Label("Valid Until:");
        untilLabel.getStyleClass().add("date-label");
        Label untilValue = new Label(offer.getValidUntil().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        untilValue.getStyleClass().add("date-value");
        untilBox.getChildren().addAll(untilIcon, untilLabel, untilValue);
        
        validityGrid.setStyle("-fx-padding: 10 0 0 0;");
        validityGrid.addRow(0, fromBox);
        validityGrid.addRow(1, untilBox);
        
        validityGrid.setVgap(15);
        
        validitySection.getChildren().addAll(validityHeader, validityGrid);

        // Add all sections with separators
        content.getChildren().addAll(
            descriptionSection,
            new Separator(),
            conditionsSection,
            new Separator(),
            benefitsSection,
            new Separator(),
            validitySection
        );

        // Configure dialog
        dialogPane.setHeader(headerBox);
        dialogPane.setContent(content);
        
        // Style buttons
        ButtonType subscribeButton = new ButtonType("Subscribe Now", ButtonBar.ButtonData.OK_DONE);
        ButtonType qrCodeButton = new ButtonType("QR Code", ButtonBar.ButtonData.LEFT);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(subscribeButton, qrCodeButton, closeButton);

        Button subscribeBtn = (Button) dialogPane.lookupButton(subscribeButton);
        subscribeBtn.getStyleClass().add("dialog-subscribe-button");
        
        Button qrBtn = (Button) dialogPane.lookupButton(qrCodeButton);
        qrBtn.getStyleClass().add("qr-button");
        
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.getStyleClass().add("dialog-close-button");

        // Store the current offer for QR code generation
        currentOffer = offer;

        // Handle button actions
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == subscribeButton) {
                handleSubscribe(offer);
            } else if (dialogButton == qrCodeButton) {
                showQRCode();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private VBox createDetailSection(String title, String content, String iconName) {
        VBox section = new VBox(15);
        section.getStyleClass().add("detail-section");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(iconName));
        icon.getStyleClass().add("section-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        
        header.getChildren().addAll(icon, titleLabel);
        
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("section-content");
        contentLabel.setWrapText(true);
        
        section.getChildren().addAll(header, contentLabel);
        return section;
    }

    private void handleSubscribe(Offre offer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentDialog.fxml"));
            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));

            PaymentDialogController controller = loader.getController();
            controller.initialize(offer.getPricePerMonth(), "USD"); // Use offer price

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Payment Error", "Could not open payment dialog.");
        }
    }

    @FXML
    private void handleNewOffer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OffreForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Offer");
            stage.setScene(new Scene(root));

            // Refresh offers when the form is closed
            stage.setOnHidden(e -> filterAndDisplayOffers());

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error opening offer form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void refreshOffers() {
        filterAndDisplayOffers();
    }

    @FXML
    public void showQRCode() {
        try {
            if (currentOffer == null) {
                showAlert(Alert.AlertType.WARNING, "No Offer Selected", "Please select an offer first.");
                return;
            }

            // Generate QR code content
            String qrContent = String.format("Offer: %s\nPrice: $%.2f/month\nPromo Code: %s", 
                currentOffer.getName(), 
                currentOffer.getPricePerMonth(),
                currentOffer.getPromoCode() != null ? currentOffer.getPromoCode() : "N/A");

            // Generate QR code and convert to Image
            String qrCodeBase64 = qrCodeService.generateQRCode(qrContent);
            byte[] imageBytes = Base64.getDecoder().decode(qrCodeBase64);
            Image qrImage = new Image(new ByteArrayInputStream(imageBytes));
            
            // Set the image to ImageView
            qrImageView.setImage(qrImage);

            // Show the dialog
            qrDialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "QR Code Error", "Could not generate QR code: " + e.getMessage());
        }
    }

    private void downloadQRCode() {
        if (qrImageView.getImage() == null) {
            showAlert(Alert.AlertType.WARNING, "No QR Code", "Please generate a QR code first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
        );
        fileChooser.setInitialFileName("offer-qr-code.png");

        File file = fileChooser.showSaveDialog(qrDialog.getDialogPane().getScene().getWindow());
        if (file != null) {
            try {
                // Write the image to file
                BufferedImage bImage = SwingFXUtils.fromFXImage(qrImageView.getImage(), null);
                ImageIO.write(bImage, "png", file);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "QR code saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not save QR code: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showPaymentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentDialog.fxml"));
            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));
            
            PaymentDialogController controller = loader.getController();
            controller.initialize(99.99, "USD"); // You can get these values from your offer data
            
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void updateCountdown(Label label, LocalDate validUntil) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = validUntil.atTime(23, 59, 59);
        long seconds = ChronoUnit.SECONDS.between(now, end);

        if (seconds <= 0) {
            label.setText("Promotion expired");
            label.setVisible(false);
        } else {
            long days = seconds / (24 * 3600);
            long hours = (seconds % (24 * 3600)) / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            label.setText(String.format("Ends in: %dd %02dh %02dm %02ds", days, hours, minutes, secs));
            label.setVisible(true);
        }
    }

    // Add this method to handle the Add to Calendar button
    @FXML
    private void handleAddToCalendar() {
        // This method is only called from FXML if wired directly, but we want per-offer context
        // So, we will wire the button in createOfferCard to a lambda that calls this logic with the offer
        // This method is left for FXML compatibility, but does nothing
    }

    // Helper for per-offer calendar integration
    private void handleAddToCalendar(Offre offer) {
        try {
            String title = offer.getName();
            String description = offer.getDescription();
            LocalDate endDate = offer.getValidUntil();
            LocalDate startDate = offer.getValidFrom();
            if (endDate == null) {
                showAlert(Alert.AlertType.WARNING, "No Deadline", "This offer does not have a deadline to add to calendar.");
                return;
            }
            // Google Calendar expects: yyyyMMdd'T'HHmmss'Z' (UTC). We'll use all-day event if no time.
            String start = startDate != null ? startDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) : endDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            String end = endDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            String url = "https://www.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&details=" + URLEncoder.encode(description, StandardCharsets.UTF_8)
                + "&dates=" + start + "/" + end;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Calendar Error", "Could not add to calendar: " + e.getMessage());
        }
    }
}