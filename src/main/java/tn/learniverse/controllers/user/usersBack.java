package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.learniverse.entities.User;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class usersBack implements Initializable {
    public Label navUsernameLabel;
    public Label FirstLetter;
    public Circle circleProfile;
    public Button logoutButton;
    public Button Profilebtn;
    @FXML
    private VBox userCardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Button allButton, studentsButton, instructorsButton, adminsButton;

    private List<User> allUsers = new ArrayList<>();
    private String currentFilter = "All";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayUsers();
        });

        // Set up filter buttons
        allButton.setOnAction(e -> {
            currentFilter = "All";
            filterAndDisplayUsers();
            updateFilterButtonStyles();
        });

        studentsButton.setOnAction(e -> {
            currentFilter = "Student";
            filterAndDisplayUsers();
            updateFilterButtonStyles();
        });

        instructorsButton.setOnAction(e -> {
            currentFilter = "Instructor";
            filterAndDisplayUsers();
            updateFilterButtonStyles();
        });

        adminsButton.setOnAction(e -> {
            currentFilter = "Admin";
            filterAndDisplayUsers();
            updateFilterButtonStyles();
        });

        // Load users on initialization
        loadAllUsers();
    }

    public void OnDashboard(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/back.fxml");
    }
    private void updateFilterButtonStyles() {
        // Reset all buttons to default style
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 5 12;";
        String activeStyle = "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12;";

        allButton.setStyle(defaultStyle);
        studentsButton.setStyle(defaultStyle);
        instructorsButton.setStyle(defaultStyle);
        adminsButton.setStyle(defaultStyle);

        // Set active button style
        switch (currentFilter) {
            case "All":
                allButton.setStyle(activeStyle);
                break;
            case "Student":
                studentsButton.setStyle(activeStyle);
                break;
            case "Instructor":
                instructorsButton.setStyle(activeStyle);
                break;
            case "Admin":
                adminsButton.setStyle(activeStyle);
                break;
        }
    }

    private void loadAllUsers() {
        try {
            UserService userService = new UserService();
            allUsers = userService.getAllUsers();
            filterAndDisplayUsers();
        } catch (SQLException e) {
            showErrorAlert("Error loading users", e.getMessage());
        }
    }

    private void filterAndDisplayUsers() {
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // Safe role check
                    String userRole = user.getRole() != null ? user.getRole() : "";

                    // Filter by role if not "All"
                    boolean roleMatch = currentFilter.equals("All") || userRole.equals(currentFilter);

                    // Filter by search text if any
                    String searchText = searchField.getText().toLowerCase();
                    boolean searchMatch = searchText.isEmpty() ||
                            (user.getNom() != null && user.getNom().toLowerCase().contains(searchText)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));

                    return roleMatch && searchMatch;
                })
                .collect(Collectors.toList());

        displayUserCards(filteredUsers);
    }

    private void displayUserCards(List<User> users) {
        // Clear existing card container
        userCardsContainer.getChildren().clear();

        // Create rows to hold 3 cards each
        HBox currentRow = null;
        int cardsInCurrentRow = 0;

        for (User user : users) {
            // Create a new row for every 3 cards
            if (cardsInCurrentRow % 3 == 0) {
                currentRow = new HBox();
                currentRow.setSpacing(20);
                userCardsContainer.getChildren().add(currentRow);
                cardsInCurrentRow = 0;
            }
            // Create user card
            VBox userCard = createUserCard(user);
            currentRow.getChildren().add(userCard);
            cardsInCurrentRow++;
        }

        // Update count label if you have one
        // userCountLabel.setText("(" + users.size() + " total)");
    }

    private VBox createUserCard(User user) {
        // Create the main card container
        VBox card = new VBox();
        card.setMaxWidth(300.0);
        card.setPrefWidth(300.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Default values for null-safe operations
        String role = user.getRole() != null ? user.getRole() : "Unknown";
        String nom = user.getNom() != null ? user.getNom() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String email = user.getEmail() != null ? user.getEmail() : "";

        // Determine gradient color based on user role
        String gradientColor;
        String textColor;
        switch (role) {
            case "Student":
                gradientColor = "linear-gradient(to right, #7F53AC, #647DEE)";
                textColor = "#647DEE";
                break;
            case "Instructor":
                gradientColor = "linear-gradient(to right, #FF9966, #FF5E62)";
                textColor = "#FF5E62";
                break;
            case "Admin":
                gradientColor = "linear-gradient(to right, #56CCF2, #2F80ED)";
                textColor = "#2F80ED";
                break;
            default:
                gradientColor = "linear-gradient(to right, #6A11CB, #2575FC)";
                textColor = "#2575FC";
        }

        // Create card header with avatar
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setPrefHeight(120.0);
        header.setStyle("-fx-background-color: " + gradientColor + "; -fx-background-radius: 16 16 0 0; -fx-padding: 20;");

        // Create avatar with initials
        StackPane avatarPane = new StackPane();
        Circle avatarCircle = new Circle(40);
        avatarCircle.setFill(Color.WHITE);

        // Get initials from nom and prenom
        String initials = "";
        if (!nom.isEmpty()) {
            initials += nom.charAt(0);
        }
        if (!prenom.isEmpty()) {
            initials += prenom.charAt(0);
        }
        if (initials.isEmpty()) {
            initials = "?"; // Default if no name is available
        }

        Label initialsLabel = new Label(initials.toUpperCase());
        initialsLabel.setStyle("-fx-font-size: 40; -fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        avatarPane.getChildren().addAll(avatarCircle, initialsLabel);
        header.getChildren().add(avatarPane);

        // Create card content
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(8);
        content.setStyle("-fx-padding: 20;");

        // Full name (nom + prenom)
        String fullName = nom + (!nom.isEmpty() && !prenom.isEmpty() ? " " : "") + prenom;
        Label nameLabel = new Label(fullName.trim().isEmpty() ? "Unknown User" : fullName.trim());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Role with verification badge if 2FA is enabled
        HBox roleBox = new HBox();
        roleBox.setAlignment(Pos.CENTER);
        roleBox.setSpacing(5);

        Label roleLabel = new Label(role);
        roleLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
        roleBox.getChildren().add(roleLabel);

        // Add verification badge if user has 2FA enabled
        if (user.isVerified()) {
            Label verifiedLabel = new Label("✓");
            verifiedLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-background-radius: 50%; -fx-padding: 0 4; -fx-font-size: 10px;");
            roleBox.getChildren().add(verifiedLabel);
        }

        // Email
        HBox emailBox = new HBox();
        emailBox.setAlignment(Pos.CENTER);
        emailBox.setSpacing(8);
        emailBox.setStyle("-fx-padding: 8 0;");

        Label emailIcon = new Label("📧");
        emailIcon.setStyle("-fx-text-fill: #64748B;");

        Label emailText = new Label(email);
        emailText.setStyle("-fx-text-fill: #64748B;");

        emailBox.getChildren().addAll(emailIcon, emailText);

        // Create action buttons
        HBox actionButtons = new HBox();
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setSpacing(10);
        actionButtons.setStyle("-fx-padding: 15 0 0 0;");

        Button viewButton = new Button("View");
        viewButton.setPrefWidth(90);
        viewButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 6;");
        viewButton.setOnAction(e -> viewUserDetails(user));

        actionButtons.getChildren().addAll(viewButton);

        // Add third button based on role and ban status
        if (!"Admin".equals(role)) {
            Button actionButton;
            if (user.getBan() == 1) {
                actionButton = new Button("Activate");
                actionButton.setPrefWidth(90);
                actionButton.setStyle("-fx-background-color: #10B981; -fx-cursor: hand; -fx-text-fill: white; -fx-background-radius: 6;");
                actionButton.setOnAction(e -> activateUser(user));
            } else {
                actionButton = new Button("Ban");
                actionButton.setPrefWidth(90);
                actionButton.setStyle("-fx-background-color: #FEE2E2; -fx-cursor: hand; -fx-text-fill: #EF4444; -fx-background-radius: 6;");
                actionButton.setOnAction(e -> banUser(user));
            }

            actionButtons.getChildren().add(actionButton);
        }

        // Add all elements to content - only username, role, and email
        content.getChildren().addAll(nameLabel, roleBox, emailBox, actionButtons);

        // Add header and content to card
        card.getChildren().addAll(header, content);

        return card;
    }

    // Action methods
    private void viewUserDetails(User user) {
        // Implement view user details functionality
        System.out.println("Viewing user: " + user.getNom());
        // e.g., open a detailed view dialog/window
    }

    private void editUser(User user) {
        // Implement edit user functionality
        System.out.println("Editing user: " + user.getNom());
        // e.g., open edit form dialog
    }

    private void banUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ban User");
        alert.setHeaderText("Ban " + user.getNom());
        alert.setContentText("Are you sure you want to ban this user?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Implement user banning in your service
                UserService userService = new UserService();
                userService.banUser(user.getId());

                // Refresh user data
                loadAllUsers();
            } catch (SQLException e) {
                showErrorAlert("Error banning user", e.getMessage());
            }
        }
    }

    private void activateUser(User user) {
        try {
            // Implement user activation in your service
            UserService userService = new UserService();
            userService.activateUser(user.getId());

            // Refresh user data
            loadAllUsers();
        } catch (SQLException e) {
            showErrorAlert("Error activating user", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }
}
