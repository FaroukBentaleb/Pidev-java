package tn.learniverse.tests;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.learniverse.services.QRCodeService;

public class QRCodeDialogTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button showQRButton = new Button("Show QR Code");
        showQRButton.setOnAction(e -> showQRDialog());

        VBox root = new VBox(20, showQRButton);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("QR Code Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showQRDialog() {
        try {
            QRCodeService qrCodeService = new QRCodeService();
            String promoMessage = "Test Promo Code: LEARN2024";
            
            // Generate QR code
            ImageView qrImageView = new ImageView(qrCodeService.generateQRCode(promoMessage));
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);
            qrImageView.setPreserveRatio(true);

            // Create dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("QR Code");
            
            // Create dialog content
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 20; -fx-alignment: center;");
            content.getChildren().addAll(qrImageView);

            // Set dialog content
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

            // Show dialog
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating QR code: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 