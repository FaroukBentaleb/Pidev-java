package com.learniverse.controller;

import com.learniverse.dao.OffreDAO;
import com.learniverse.model.Offre;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class OffreFormController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField discountField;
    @FXML private TextArea conditionsField;
    @FXML private TextField promoCodeField;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField maxSubscriptionsField;
    @FXML private TextField targetAudienceField;
    @FXML private TextArea benefitsField;
    @FXML private TextField customPlanField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ComboBox<String> planTypeCombo;
    private static final String CUSTOM_PLAN = "Custom";

    private OffreDAO offreDAO;
    private Offre offre;
    private Map<Control, Text> validationMessages;

    @FXML
    public void initialize() {
        offreDAO = new OffreDAO();
        validationMessages = new HashMap<>();

        // Initialize validation messages
        setupValidationMessages();

        // Target Audience validation
        targetAudienceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                targetAudienceField.setText(oldValue);
            }
            validateField(targetAudienceField);
        });

        // Discount validation with real-time format checking
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                discountField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    double value = Double.parseDouble(newValue);
                    if (value > 100) {
                        discountField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    discountField.setText(oldValue);
                }
            }
            validateField(discountField);
        });

        // Max Subscriptions validation with real-time format checking
        maxSubscriptionsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxSubscriptionsField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value < 0) {
                        maxSubscriptionsField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    maxSubscriptionsField.setText(oldValue);
                }
            }
            validateField(maxSubscriptionsField);
        });

        // Set up numeric validation for price and discount fields
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
            validateField(priceField);
        });

        // Add validation listeners to required fields
        nameField.textProperty().addListener((obs, old, newValue) -> validateField(nameField));
        descriptionField.textProperty().addListener((obs, old, newValue) -> validateField(descriptionField));
        conditionsField.textProperty().addListener((obs, old, newValue) -> validateField(conditionsField));
        planTypeCombo.valueProperty().addListener((obs, old, newValue) -> {
            validateField(planTypeCombo);
            boolean isCustom = CUSTOM_PLAN.equals(newValue);
            customPlanField.setDisable(!isCustom);
            if (!isCustom) {
                customPlanField.clear();
                removeValidationStyles(customPlanField);
            } else {
                validateField(customPlanField);
            }
        });
        customPlanField.textProperty().addListener((obs, old, newValue) -> validateField(customPlanField));

        // Set default values for dates
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusYears(1));
        
        // Initialize plan types
        planTypeCombo.getItems().addAll("Basic", "Standard", "Premium", CUSTOM_PLAN);
        planTypeCombo.setValue("Basic"); // Set default value
        
        // Add date validation listeners
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateField(startDatePicker);
            if (newVal != null && endDatePicker.getValue() != null && 
                endDatePicker.getValue().isBefore(newVal)) {
                endDatePicker.setValue(newVal.plusYears(1));
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateField(endDatePicker);
            if (newVal != null && startDatePicker.getValue() != null && 
                newVal.isBefore(startDatePicker.getValue())) {
                endDatePicker.setValue(startDatePicker.getValue().plusYears(1));
            }
        });

        // Set default values
        activeCheckBox.setSelected(true);
        
        // Initially disable custom plan field
        customPlanField.setDisable(true);
        
        // Initial validation of all fields
        validateAllFields();
    }

    private void setupValidationMessages() {
        addValidationMessage(nameField, "Name is required");
        addValidationMessage(targetAudienceField, "Target audience should be between 3 and 100 characters");
        addValidationMessage(priceField, "Price is required and must be a valid number");
        addValidationMessage(discountField, "Discount must be between 0 and 100");
        addValidationMessage(maxSubscriptionsField, "Max subscriptions must be a positive number");
        addValidationMessage(descriptionField, "Description is required");
        addValidationMessage(conditionsField, "Conditions are required");
        addValidationMessage(planTypeCombo, "Plan type is required");
        addValidationMessage(customPlanField, "Custom plan details are required when 'Custom' is selected");
        addValidationMessage(startDatePicker, "Start date is required");
        addValidationMessage(endDatePicker, "End date is required");
    }

    private void addValidationMessage(Control field, String message) {
        Text validationText = new Text();
        validationText.getStyleClass().add("validation-error");
        validationText.setVisible(false);
        validationText.setText(message);
        
        // Find the field's VBox container
        if (field.getParent() instanceof VBox) {
            VBox container = (VBox) field.getParent();
            container.getChildren().add(validationText);
        }
        
        validationMessages.put(field, validationText);
    }

    private void validateField(Control field) {
        boolean isValid = true;
        String errorMessage = null;

        if (field instanceof TextField) {
            TextField textField = (TextField) field;
            String value = textField.getText().trim();
            
            if (field == nameField) {
                isValid = !value.isEmpty();
            } else if (field == targetAudienceField) {
                if (!value.isEmpty()) {
                    isValid = value.length() >= 3 && value.length() <= 100;
                    if (!isValid) {
                        errorMessage = "Target audience should be between 3 and 100 characters";
                    }
                }
            } else if (field == priceField) {
                try {
                    double price = Double.parseDouble(value);
                    isValid = price >= 0;
                    if (!isValid) errorMessage = "Price must be a positive number";
                } catch (NumberFormatException e) {
                    isValid = false;
                    errorMessage = "Price must be a valid number";
                }
            } else if (field == discountField && !value.isEmpty()) {
                try {
                    double discount = Double.parseDouble(value);
                    isValid = discount >= 0 && discount <= 100;
                    if (!isValid) {
                        errorMessage = "Discount must be between 0 and 100";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    errorMessage = "Discount must be a valid number";
                }
            } else if (field == maxSubscriptionsField && !value.isEmpty()) {
                try {
                    int maxSubs = Integer.parseInt(value);
                    isValid = maxSubs >= 0;
                    if (!isValid) {
                        errorMessage = "Max subscriptions must be a positive number";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    errorMessage = "Max subscriptions must be a valid number";
                }
            } else if (field == customPlanField && !field.isDisabled()) {
                isValid = !value.isEmpty();
            }
        } else if (field instanceof TextArea) {
            String value = ((TextArea) field).getText().trim();
            isValid = !value.isEmpty();
        } else if (field instanceof ComboBox) {
            isValid = ((ComboBox<?>) field).getValue() != null;
        } else if (field instanceof DatePicker) {
            DatePicker datePicker = (DatePicker) field;
            isValid = datePicker.getValue() != null;
            
            if (isValid && datePicker == endDatePicker && startDatePicker.getValue() != null) {
                isValid = !datePicker.getValue().isBefore(startDatePicker.getValue());
                if (!isValid) errorMessage = "End date must be after start date";
            }
        }

        updateValidationStatus(field, isValid, errorMessage);
        updateSaveButtonState();
    }

    private void updateValidationStatus(Control field, boolean isValid, String customMessage) {
        Text validationText = validationMessages.get(field);
        if (validationText != null) {
            if (!isValid && customMessage != null) {
                validationText.setText(customMessage);
            }
            validationText.setVisible(!isValid);
        }

        if (isValid) {
            field.getStyleClass().remove("error");
            field.getStyleClass().add("success");
        } else {
            field.getStyleClass().remove("success");
            field.getStyleClass().add("error");
        }
    }

    private void removeValidationStyles(Control field) {
        field.getStyleClass().removeAll("error", "success");
        Text validationText = validationMessages.get(field);
        if (validationText != null) {
            validationText.setVisible(false);
        }
    }

    private void validateAllFields() {
        validateField(nameField);
        validateField(priceField);
        validateField(descriptionField);
        validateField(conditionsField);
        validateField(planTypeCombo);
        if (CUSTOM_PLAN.equals(planTypeCombo.getValue())) {
            validateField(customPlanField);
        }
        validateField(startDatePicker);
        validateField(endDatePicker);
        
        if (!discountField.getText().trim().isEmpty()) {
            validateField(discountField);
        }
        if (!maxSubscriptionsField.getText().trim().isEmpty()) {
            validateField(maxSubscriptionsField);
        }
    }

    private void updateSaveButtonState() {
        boolean isValid = true;
        
        // Required fields validation
        isValid &= !nameField.getText().trim().isEmpty();
        isValid &= !priceField.getText().trim().isEmpty();
        isValid &= !descriptionField.getText().trim().isEmpty();
        isValid &= !conditionsField.getText().trim().isEmpty();
        isValid &= planTypeCombo.getValue() != null;
        isValid &= !(CUSTOM_PLAN.equals(planTypeCombo.getValue()) && 
                    (customPlanField.getText() == null || customPlanField.getText().trim().isEmpty()));
        isValid &= startDatePicker.getValue() != null;
        isValid &= endDatePicker.getValue() != null;
        
        // Target Audience validation (if not empty)
        String targetAudience = targetAudienceField.getText().trim();
        if (!targetAudience.isEmpty()) {
            isValid &= targetAudience.length() >= 3 && targetAudience.length() <= 100;
        }
        
        // Discount validation (if not empty)
        String discountText = discountField.getText().trim();
        if (!discountText.isEmpty()) {
            try {
                double discount = Double.parseDouble(discountText);
                isValid &= discount >= 0 && discount <= 100;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }
        
        // Max Subscriptions validation (if not empty)
        String maxSubsText = maxSubscriptionsField.getText().trim();
        if (!maxSubsText.isEmpty()) {
            try {
                int maxSubs = Integer.parseInt(maxSubsText);
                isValid &= maxSubs >= 0;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }
        
        // Check dates
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            isValid &= !endDatePicker.getValue().isBefore(startDatePicker.getValue());
        }
        
        saveButton.setDisable(!isValid);
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
        if (offre != null) {
            // Populate fields with existing offer data
            nameField.setText(offre.getName());
            descriptionField.setText(offre.getDescription());
            priceField.setText(String.valueOf(offre.getPricePerMonth()));
            discountField.setText(offre.getDiscount() != null ? String.valueOf(offre.getDiscount()) : "");
            conditionsField.setText(offre.getConditions());
            promoCodeField.setText(offre.getPromoCode());
            activeCheckBox.setSelected(offre.isActive());
            maxSubscriptionsField.setText(offre.getMaxSubscriptions() != null ? 
                String.valueOf(offre.getMaxSubscriptions()) : "");
            targetAudienceField.setText(offre.getTargetAudience());
            benefitsField.setText(offre.getBenefits());
            customPlanField.setText(offre.getCustomPlan());
            
            // Handle dates
            if (offre.getValidFrom() != null) {
                startDatePicker.setValue(offre.getValidFrom());
            } else {
                startDatePicker.setValue(LocalDate.now());
            }
            
            if (offre.getValidUntil() != null) {
                endDatePicker.setValue(offre.getValidUntil());
            } else {
                endDatePicker.setValue(startDatePicker.getValue().plusYears(1));
            }
            
            // Set plan type
            planTypeCombo.setValue(offre.getApplicablePlans() != null ? offre.getApplicablePlans() : "Basic");
        } else {
            // Set default values for new offer
            nameField.clear();
            descriptionField.clear();
            priceField.clear();
            discountField.clear();
            conditionsField.clear();
            maxSubscriptionsField.clear();
            activeCheckBox.setSelected(true);
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusYears(1));
            planTypeCombo.setValue("Basic");
        }
    }

    @FXML
    public void saveOffre() {
        if (!validateFields()) {
            return;
        }

        if (offre == null) {
            offre = new Offre();
        }

        try {
            // Set offer properties
            offre.setName(nameField.getText().trim());
            offre.setDescription(descriptionField.getText().trim());
            offre.setConditions(conditionsField.getText().trim());
            
            try {
                offre.setPricePerMonth(Double.parseDouble(priceField.getText().trim()));
            } catch (NumberFormatException e) {
                showAlert("Price must be a valid number.");
                return;
            }
            
            // Validate and set target audience
            String targetAudience = targetAudienceField.getText().trim();
            if (!targetAudience.isEmpty()) {
                if (targetAudience.length() < 3 || targetAudience.length() > 100) {
                    showAlert("Target audience should be between 3 and 100 characters.");
                    return;
                }
                offre.setTargetAudience(targetAudience);
            }
            
            // Validate and set discount
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
            
            // Validate and set max subscriptions
            String maxSubsText = maxSubscriptionsField.getText().trim();
            if (!maxSubsText.isEmpty()) {
                try {
                    int maxSubs = Integer.parseInt(maxSubsText);
                    if (maxSubs < 0) {
                        showAlert("Max subscriptions must be a positive number.");
                        return;
                    }
                    offre.setMaxSubscriptions(maxSubs);
                } catch (NumberFormatException e) {
                    showAlert("Max subscriptions must be a valid number.");
                    return;
                }
            } else {
                offre.setMaxSubscriptions(0);
            }
            
            // Set other fields
            offre.setActive(activeCheckBox.isSelected());
            offre.setPromoCode(promoCodeField.getText().trim());
            offre.setBenefits(benefitsField.getText().trim());
            
            // Set plan type and custom plan
            String selectedPlan = planTypeCombo.getValue();
            offre.setApplicablePlans(selectedPlan);
            if (CUSTOM_PLAN.equals(selectedPlan)) {
                offre.setCustomPlan(customPlanField.getText().trim());
            } else {
                offre.setCustomPlan(null);
            }
            
            // Handle dates
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate == null) {
                showAlert("Start date is required.");
                return;
            }
            
            if (endDate == null) {
                showAlert("End date is required.");
                return;
            }
            
            if (endDate.isBefore(startDate)) {
                showAlert("End date must be after start date.");
                return;
            }
            
            offre.setValidFrom(startDate);
            offre.setValidUntil(endDate);

            // Save to database
            if (offre.getId() == 0) {
                offreDAO.create(offre);
            } else {
                offreDAO.update(offre);
            }
            
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error saving offer: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Required fields validation
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Name is required.\n");
        }
        if (priceField.getText().trim().isEmpty()) {
            errors.append("Price is required.\n");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("Description is required.\n");
        }
        if (conditionsField.getText().trim().isEmpty()) {
            errors.append("Conditions are required.\n");
        }
        if (planTypeCombo.getValue() == null) {
            errors.append("Plan type is required.\n");
        }
        if (CUSTOM_PLAN.equals(planTypeCombo.getValue()) && 
            (customPlanField.getText() == null || customPlanField.getText().trim().isEmpty())) {
            errors.append("Custom plan details are required when 'Custom' is selected.\n");
        }

        // Date validation
        LocalDate validFrom = startDatePicker.getValue();
        LocalDate validUntil = endDatePicker.getValue();

        if (validFrom == null) {
            errors.append("Valid from date is required.\n");
        }
        if (validUntil == null) {
            errors.append("Valid until date is required.\n");
        }
        if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
            errors.append("Valid until date must be after valid from date.\n");
        }

        if (errors.length() > 0) {
            showAlert(errors.toString());
            return false;
        }
        return true;
    }

    @FXML
    public void closeWindow() {
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