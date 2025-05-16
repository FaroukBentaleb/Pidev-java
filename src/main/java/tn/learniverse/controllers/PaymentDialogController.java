package tn.learniverse.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Subscription;
import tn.learniverse.entities.User;
import tn.learniverse.services.SubscriptionDAO;

import java.time.LocalDate;

public class PaymentDialogController {
    public VBox paymentContainer;
    @FXML
    private WebView webView;

    public void initialize(Course course, User usr) {
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
                                .setCurrency("USD")
                                .setUnitAmount((long) (course.getPrice() * 100))
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
                        try {
                            SubscriptionDAO subscriptionService = new SubscriptionDAO();
                            Subscription sbs = new Subscription();
                            sbs.setCourseId(course.getId());
                            sbs.setStatus("On-Going");
                            sbs.setUserId(usr.getId());
                            sbs.setDateEarned(LocalDate.now().atStartOfDay());
                            sbs.setDurationInDays(90);
                            subscriptionService.create(sbs);

                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Subscription Done! Your subscription was successfully completed.");
                            alert.showAndWait();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Payment succeeded but subscription failed.");
                            alert.showAndWait();
                        }

                        ((Stage) webView.getScene().getWindow()).close();
                    });
                }
                 else if (newLocation.startsWith("http://localhost/payment-cancel")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Payment Cancelled.");
                        alert.showAndWait();
                        ((Stage) webView.getScene().getWindow()).close();
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Optionally show an error dialog
        }
    }
    public void initialize(double amount, String str) {
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
                                                    .setCurrency("USD")
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
                        ((Stage) webView.getScene().getWindow()).close();
                    });
                }
                else if (newLocation.startsWith("http://localhost/payment-cancel")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Payment Cancelled.");
                        alert.showAndWait();
                        ((Stage) webView.getScene().getWindow()).close();
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Optionally show an error dialog
        }
    }
} 