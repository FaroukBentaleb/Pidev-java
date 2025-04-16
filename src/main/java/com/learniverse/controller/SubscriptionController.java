package com.learniverse.controller;

import com.learniverse.dao.SubscriptionDAO;
import com.learniverse.model.Subscription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SubscriptionController {
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterStatus;
    @FXML
    private ComboBox<String> sortBy;
    @FXML
    private Button addButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Pagination pagination;

    private SubscriptionDAO subscriptionDAO;
    private ObservableList<Subscription> subscriptions;
    private static final int ITEMS_PER_PAGE = 12;

    @FXML
    public void initialize() {
        subscriptionDAO = new SubscriptionDAO();
        subscriptions = FXCollections.observableArrayList();

        // Initialize filter and sort options
        filterStatus.setItems(FXCollections.observableArrayList(
            "All", "Active", "Inactive", "Pending", "Expired"
        ));
        filterStatus.setValue("All");

        sortBy.setItems(FXCollections.observableArrayList(
            "Date (Newest)", "Date (Oldest)", "Status"
        ));
        sortBy.setValue("Date (Newest)");

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSubscriptions();
        });

        filterStatus.setOnAction(event -> filterSubscriptions());
        sortBy.setOnAction(event -> filterSubscriptions());

        // Set up button actions
        addButton.setOnAction(event -> showSubscriptionForm(null));
        refreshButton.setOnAction(event -> refreshCards());

        // Initial load
        refreshCards();
    }

    private void refreshCards() {
        subscriptions.clear();
        subscriptions.addAll(subscriptionDAO.readAll());
        filterSubscriptions();
    }

    private void filterSubscriptions() {
        List<Subscription> filteredList = subscriptionDAO.readAll();
        // Apply filters and sorting here based on searchField.getText(), filterStatus.getValue(), and sortBy.getValue()
        
        int totalPages = (filteredList.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(totalPages);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        cardsContainer.getChildren().clear();
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, subscriptions.size());
        
        for (int i = fromIndex; i < toIndex; i++) {
            cardsContainer.getChildren().add(createSubscriptionCard(subscriptions.get(i)));
        }
        
        return cardsContainer;
    }

    private VBox createSubscriptionCard(Subscription subscription) {
        VBox card = new VBox(10);
        card.getStyleClass().add("subscription-card");

        // Card Header with Status
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label idLabel = new Label("Subscription #" + subscription.getId());
        idLabel.getStyleClass().add("card-header");
        
        Label statusLabel = new Label(subscription.getStatus());
        statusLabel.getStyleClass().addAll("card-status", "status-" + subscription.getStatus().toLowerCase());
        
        header.getChildren().addAll(idLabel, statusLabel);

        // Card Content
        VBox content = new VBox(5);
        content.getStyleClass().add("card-info");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Text dateText = new Text("Date Earned: " + subscription.getDateEarned().format(formatter));
        Text courseText = new Text("Course ID: " + subscription.getCourseId());
        Text userText = new Text("User ID: " + subscription.getUserId());
        Text offerText = new Text("Offer ID: " + subscription.getOffreId());

        content.getChildren().addAll(dateText, courseText, userText, offerText);

        // Card Actions
        HBox actions = new HBox(10);
        actions.getStyleClass().add("card-actions");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(event -> showSubscriptionForm(subscription));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> deleteSubscription(subscription));

        actions.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(header, content, actions);
        return card;
    }

    private void showSubscriptionForm(Subscription subscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubscriptionForm.fxml"));
            Parent root = loader.load();
            SubscriptionFormController controller = loader.getController();
            controller.setSubscription(subscription);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(subscription == null ? "Add New Subscription" : "Edit Subscription");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshCards();
        } catch (IOException e) {
            e.printStackTrace();
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
            refreshCards();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 