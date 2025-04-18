package com.learniverse.controller;

import com.learniverse.dao.OffreDAO;
import com.learniverse.model.Offre;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;

public class OffreDisplayController {
    @FXML
    private FlowPane offersContainer;
    
    @FXML
    private Button offersButton;
    
    private OffreDAO offreDAO;

    @FXML
    public void initialize() {
        offreDAO = new OffreDAO();
        offersContainer.setHgap(15);
        offersContainer.setVgap(15);
        offersContainer.setPadding(new Insets(15));
        
        // Add a listener to load stylesheet after scene is created
        offersContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                String css = getClass().getResource("/styles/offer-display.css").toExternalForm();
                newScene.getStylesheets().add(css);
            }
        });
        
        // Set up offers button click handler
        offersButton.setOnAction(event -> loadOffersView());
        
        loadOffers();
        
        // Find all NEW badges and set their visibility randomly
        offersContainer.lookupAll(".new-badge").forEach(node -> {
            node.setVisible(Math.random() < 0.3);
        });
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

    private void loadOffers() {
        List<Offre> offers = offreDAO.findAll();
        offersContainer.getChildren().clear();
        
        for (Offre offer : offers) {
            if (offer.isActive()) {
                offersContainer.getChildren().add(createOfferCard(offer));
            }
        }
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

        if (offer.getDiscount() != null && offer.getDiscount() > 0) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label discountLabel = new Label(String.format("-%.0f%%", offer.getDiscount()));
            discountLabel.getStyleClass().add("discount-badge");
            
            priceBox.getChildren().addAll(spacer, discountLabel);
        }

        // Description
        Label descriptionLabel = new Label(offer.getDescription());
        descriptionLabel.getStyleClass().add("description");
        descriptionLabel.setWrapText(true);

        // Separators
        Separator topSeparator = new Separator();
        Separator bottomSeparator = new Separator();

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(5);
        buttonBox.getStyleClass().add("button-box");

        Button detailsButton = new Button("Show Details");
        detailsButton.getStyleClass().add("show-details-button");
        detailsButton.setOnAction(e -> showOfferDetails(offer));

        Button subscribeButton = new Button("Subscribe");
        subscribeButton.getStyleClass().add("subscribe-button");
        subscribeButton.setOnAction(e -> handleSubscribe(offer));

        buttonBox.getChildren().addAll(detailsButton, subscribeButton);

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
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(subscribeButton, closeButton);

        Button subscribeBtn = (Button) dialogPane.lookupButton(subscribeButton);
        subscribeBtn.getStyleClass().add("dialog-subscribe-button");
        
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.getStyleClass().add("dialog-close-button");

        // Handle subscribe action
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == subscribeButton) {
                handleSubscribe(offer);
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Subscribe to Offer");
        alert.setHeaderText("Subscribe to " + offer.getName());
        alert.setContentText("You are about to subscribe to this offer for $" + 
            String.format("%.2f", offer.getPricePerMonth()) + " per month.\n\n" +
            "This feature will be available soon!");
        alert.showAndWait();
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
            stage.setOnHidden(e -> loadOffers());

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
        loadOffers();
    }
}