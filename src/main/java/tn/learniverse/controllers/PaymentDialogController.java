package tn.learniverse.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class PaymentDialogController {
    @FXML
    private WebView webView;

    public void initialize(double amount, String currency) {
        try {
            Stripe.apiKey = "sk_test_51QzSoVJv3NyOjLLc6pEKPq6VLUQkCvlf0B5lG2hQKeJBHcuzhoctrm89FvIk4qPIvWcn3uTkT5ZFtTTEgbtFCmnB00WlvabUOb";

            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost/payment-success")
                .setCancelUrl("http://localhost/payment-cancel")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount((long) (amount * 100))
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Learniverse Offer Subscription")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

            Session session = Session.create(params);

            WebEngine webEngine = webView.getEngine();
            webEngine.load(session.getUrl());

            webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
                if (newLocation.startsWith("http://localhost/payment-success")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Payment Successful!");
                        alert.showAndWait();
                        ((Stage) webView.getScene().getWindow()).close();
                    });
                } else if (newLocation.startsWith("http://localhost/payment-cancel")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Payment Cancelled.");
                        alert.showAndWait();
                        ((Stage) webView.getScene().getWindow()).close();
                    });
                }
            });

        } catch (StripeException e) {
            e.printStackTrace();
            // Optionally show an error dialog
        }
    }
} 