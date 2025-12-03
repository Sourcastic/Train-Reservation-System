package com.example.trainreservationsystem.controllers.member;

import com.example.trainreservationsystem.models.shared.User;
import com.example.trainreservationsystem.services.shared.AuthService;
import com.example.trainreservationsystem.services.shared.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button confirmButton;

    private final AuthService authService = AuthService.getInstance();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    public void initialize() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNo());
        }

        // Setup phone number validation
        setupPhoneValidation();
    }

    private void setupPhoneValidation() {
        // Real-time phone number formatting and validation
        phoneField.textProperty().addListener((obs, old, val) -> {
            if (val == null) {
                return;
            }

            // Remove non-digit characters except hyphen
            String cleaned = val.replaceAll("[^\\d-]", "");

            // Allow format: 03XX-XXXXXXX
            if (cleaned.length() <= 4) {
                phoneField.setText(cleaned);
            } else if (cleaned.length() <= 11) {
                // Auto-format: add hyphen after 4 digits
                String digitsOnly = cleaned.replaceAll("-", "");
                if (digitsOnly.length() <= 4) {
                    phoneField.setText(digitsOnly);
                } else {
                    phoneField.setText(digitsOnly.substring(0, 4) + "-" + digitsOnly.substring(4));
                }
            } else {
                // Limit to 11 digits (with hyphen = 12 chars)
                phoneField.setText(old);
            }

            // Visual feedback for validation
            updatePhoneFieldStyle(cleaned.replaceAll("-", ""));
        });
    }

    private void updatePhoneFieldStyle(String phone) {
        if (phone.isEmpty()) {
            phoneField.setStyle("-fx-border-color: rgba(203,166,164,0.3);");
        } else if (isValidPhoneNumber(phone)) {
            phoneField.setStyle("-fx-border-color: #2e7d32;");
        } else if (phone.length() < 11) {
            phoneField.setStyle("-fx-border-color: rgba(203,166,164,0.3);");
        } else {
            phoneField.setStyle("-fx-border-color: #d32f2f;");
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.replaceAll("[\\s-]", "");
        return cleaned.matches("^03\\d{9}$");
    }

    @FXML
    private void handleConfirm() {
        // Clear previous messages
        errorLabel.setText("");
        successLabel.setText("");

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        // Validate phone number format (same place as email validation)
        String phoneError = validatePhoneNumber(phone);
        if (phoneError != null && !phoneError.isEmpty()) {
            errorLabel.setText(phoneError);
            phoneField.requestFocus();
            return;
        }

        // Validate email format
        if (!email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        try {
            User currentUser = userSession.getCurrentUser();
            currentUser.setUsername(name);
            currentUser.setEmail(email);
            currentUser.setPhoneNo(phone);

            authService.updateUserProfile(currentUser);

            successLabel.setText("Profile updated successfully!");

            // Update session user to reflect changes (though we modified the object
            // reference directly above,
            // good to be explicit if session logic changes)
            // In this case, currentUser is a reference to the object in session, so it's
            // already updated.

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("An error occurred while updating profile.");
        }
    }

    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Phone number cannot be empty";
        }

        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        if (cleaned.length() != 11) {
            return "Phone number must be 11 digits (format: 03XX-XXXXXXX)";
        }

        if (!cleaned.matches("^03\\d{9}$")) {
            return "Phone number must start with 03XX followed by 7 digits (format: 03XX-XXXXXXX)";
        }

        return ""; // Valid
    }
}
