package com.example.trainreservationsystem.controllers.shared.auth;

import com.example.trainreservationsystem.services.shared.AuthService;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for user registration.
 * Handles new user signup and redirects to dashboard.
 */
public class SignupController {
  @FXML
  private TextField nameField;
  @FXML
  private TextField emailField;
  @FXML
  private TextField phoneField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private Label errorLabel;
  @FXML
  private Button signupButton;

  private final AuthService authService = AuthService.getInstance();

  @FXML
  public void initialize() {
    errorLabel.setText("");
    setupInputValidation();
  }

  private void setupInputValidation() {
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
  public void handleSignup() {
    errorLabel.setText("");
    String name = nameField.getText().trim();
    String email = emailField.getText().trim();
    String phone = phoneField.getText().trim();
    String password = passwordField.getText();

    if (!validateInput(name, email, phone, password)) {
      return;
    }

    try {
      authService.signup(name, email, phone, password);
      AlertUtils.showSuccess("Success", "Account created successfully! Please login.");
      handleLogin();
    } catch (IllegalArgumentException e) {
      showError(e.getMessage());
    } catch (Exception e) {
      showError("Signup failed. Please try again.");
      e.printStackTrace();
    }
  }

  private boolean validateInput(String name, String email, String phone, String password) {
    if (name.isEmpty()) {
      showError("Please enter your name");
      return false;
    }
    if (email.isEmpty() || !email.contains("@")) {
      showError("Please enter a valid email");
      return false;
    }

    // Validate phone number (same place as email/password validation)
    String phoneError = validatePhoneNumber(phone);
    if (phoneError != null && !phoneError.isEmpty()) {
      showError(phoneError);
      phoneField.requestFocus();
      return false;
    }

    if (password.length() < 6) {
      showError("Password must be at least 6 characters");
      return false;
    }
    return true;
  }

  @FXML
  public void handleLogin() {
    try {
      Stage stage = (Stage) nameField.getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/login-view.fxml"));
      Parent root = loader.load();
      stage.setScene(new Scene(root, 1280, 800));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load login page");
      e.printStackTrace();
    }
  }

  @FXML
  public void handleBackToLanding() {
    try {
      Stage stage = (Stage) nameField.getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/landing-view.fxml"));
      Parent root = loader.load();
      stage.setScene(new Scene(root, 1280, 800));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load landing page");
      e.printStackTrace();
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

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }
}
