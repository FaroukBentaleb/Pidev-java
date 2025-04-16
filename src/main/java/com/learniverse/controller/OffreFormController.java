package com.learniverse.controller;

import com.learniverse.dao.OffreDAO;
import com.learniverse.model.Offre;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class OffreFormController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField discountField;
    @FXML private TextArea conditionsArea;
    @FXML private TextField promoCodeField;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField maxSubscriptionsField;
    @FXML private TextArea targetAudienceArea;
    @FXML private TextArea benefitsArea;
    @FXML private TextField customPlanField;
    @FXML private DatePicker validFromPicker;
    @FXML private DatePicker validUntilPicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ComboBox<String> planTypeComboBox;
    private static final String CUSTOM_PLAN = "Custom";

    private OffreDAO offreDAO;
    private Offre offre;

    @FXML
    public void initialize() {
        offreDAO = new OffreDAO();

        // Set up numeric validation for price and discount fields
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });

        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                discountField.setText(oldValue);
            }
        });

        maxSubscriptionsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxSubscriptionsField.setText(oldValue);
            }
        });

        // Set default values for dates
        validFromPicker.setValue(LocalDate.now());
        validUntilPicker.setValue(LocalDate.now().plusYears(1));
        
        // Initialize plan types
        planTypeComboBox.getItems().addAll("Basic", "Standard", "Premium", CUSTOM_PLAN);
        planTypeComboBox.setValue("Basic"); // Set default value
        
        // Add listener to enable/disable custom plan field based on selection
        planTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustom = CUSTOM_PLAN.equals(newVal);
            customPlanField.setDisable(!isCustom);
            if (!isCustom) {
                customPlanField.clear();
            }
        });
        
        // Initially disable custom plan field
        customPlanField.setDisable(true);
        
        // Add date validation listeners
        validFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && validUntilPicker.getValue() != null && 
                validUntilPicker.getValue().isBefore(newVal)) {
                validUntilPicker.setValue(newVal.plusYears(1));
            }
        });
        
        validUntilPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && validFromPicker.getValue() != null && 
                newVal.isBefore(validFromPicker.getValue())) {
                validUntilPicker.setValue(validFromPicker.getValue().plusYears(1));
            }
        });

        // Set up button actions
        saveButton.setOnAction(event -> saveOffre());
        cancelButton.setOnAction(event -> closeWindow());

        // Set default values
        activeCheckBox.setSelected(true);
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
        if (offre != null) {
            // Populate fields with existing offer data
            nameField.setText(offre.getName());
            descriptionArea.setText(offre.getDescription());
            priceField.setText(String.valueOf(offre.getPricePerMonth()));
            discountField.setText(offre.getDiscount() != null ? String.valueOf(offre.getDiscount()) : "");
            conditionsArea.setText(offre.getConditions());
            promoCodeField.setText(offre.getPromoCode());
            activeCheckBox.setSelected(offre.isActive());
            maxSubscriptionsField.setText(offre.getMaxSubscriptions() != null ? 
                String.valueOf(offre.getMaxSubscriptions()) : "");
            targetAudienceArea.setText(offre.getTargetAudience());
            benefitsArea.setText(offre.getBenefits());
            customPlanField.setText(offre.getCustomPlan());
            validFromPicker.setValue(offre.getValidFrom());
            if (offre.getValidUntil() != null) {
                validUntilPicker.setValue(offre.getValidUntil());
            }
            planTypeComboBox.setValue(offre.getApplicablePlans());
        }
    }

    private void saveOffre() {
        if (!validateFields()) {
            return;
        }

        if (offre == null) {
            offre = new Offre();
        }

        try {
            // Basic validations first
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Name is required.");
                return;
            }

            if (priceField.getText().trim().isEmpty()) {
                showAlert("Price is required.");
                return;
            }

            // Set offer properties
            offre.setName(nameField.getText().trim());
            offre.setDescription(descriptionArea.getText().trim());
            
            try {
                offre.setPricePerMonth(Double.parseDouble(priceField.getText().trim()));
            } catch (NumberFormatException e) {
                showAlert("Price must be a valid number.");
                return;
            }
            
            // Handle plan type and custom plan
            String selectedPlan = planTypeComboBox.getValue();
            if (CUSTOM_PLAN.equals(selectedPlan)) {
                if (customPlanField.getText().trim().isEmpty()) {
                    showAlert("Custom plan details are required when Custom is selected.");
                    return;
                }
                offre.setApplicablePlans("Custom");
                offre.setCustomPlan(customPlanField.getText().trim());
            } else {
                offre.setApplicablePlans(selectedPlan);
                offre.setCustomPlan(null);
            }
            
            // Handle discount
            String discountText = discountField.getText().trim();
            if (!discountText.isEmpty()) {
                try {
                    double discount = Double.parseDouble(discountText);
                    if (discount < 0 || discount > 100) {
                        showAlert("Discount must be between 0 and 100.");
                        return;
                    }
                    offre.setDiscount(discount);
                } catch (NumberFormatException e) {
                    showAlert("Discount must be a valid number.");
                    return;
                }
            } else {
                offre.setDiscount(0.0);
            }
            
            offre.setConditions(conditionsArea.getText().trim());
            offre.setPromoCode(promoCodeField.getText().trim());
            offre.setActive(activeCheckBox.isSelected());
            
            // Handle max subscriptions
            String maxSubsText = maxSubscriptionsField.getText().trim();
            if (!maxSubsText.isEmpty()) {
                try {
                    offre.setMaxSubscriptions(Integer.parseInt(maxSubsText));
                } catch (NumberFormatException e) {
                    showAlert("Max subscriptions must be a valid number.");
                    return;
                }
            } else {
                offre.setMaxSubscriptions(0);
            }
            
            offre.setTargetAudience(targetAudienceArea.getText().trim());
            offre.setBenefits(benefitsArea.getText().trim());

            // Date handling with explicit validation
            try {
                LocalDate validFrom = validFromPicker.getValue();
                if (validFrom == null) {
                    validFrom = LocalDate.now();
                    validFromPicker.setValue(validFrom);
                }
                
                LocalDate validUntil = validUntilPicker.getValue();
                if (validUntil == null) {
                    validUntil = validFrom.plusYears(1);
                    validUntilPicker.setValue(validUntil);
                }

                if (validUntil.isBefore(validFrom)) {
                    showAlert("Valid until date must be after valid from date.");
                    return;
                }

                System.out.println("Setting validFrom: " + validFrom);
                System.out.println("Setting validUntil: " + validUntil);
                
                offre.setValidFrom(validFrom);
                offre.setValidUntil(validUntil);
            } catch (Exception e) {
                System.err.println("Error setting dates: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error setting dates: " + e.getMessage());
                return;
            }

            // Save to database
            try {
                if (offre.getId() == 0) {
                    System.out.println("Creating new offer");
                    offreDAO.create(offre);
                } else {
                    System.out.println("Updating existing offer");
                    offreDAO.update(offre);
                }
                closeWindow();
            } catch (Exception e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error saving to database: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error saving offer: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Basic field validation
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Name is required.\n");
        }
        if (priceField.getText().trim().isEmpty()) {
            errors.append("Price is required.\n");
        }

        // Date validation
        LocalDate validFrom = validFromPicker.getValue();
        LocalDate validUntil = validUntilPicker.getValue();

        System.out.println("Validating dates - Valid From: " + validFrom);
        System.out.println("Validating dates - Valid Until: " + validUntil);

        if (validFrom == null) {
            System.out.println("Setting default valid from date");
            validFrom = LocalDate.now();
            validFromPicker.setValue(validFrom);
        }

        if (validUntil == null) {
            System.out.println("Setting default valid until date");
            validUntil = validFrom.plusYears(1);
            validUntilPicker.setValue(validUntil);
        }

        if (validUntil.isBefore(validFrom)) {
            errors.append("Valid until date must be after valid from date.\n");
        }

        if (errors.length() > 0) {
            showAlert(errors.toString());
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 