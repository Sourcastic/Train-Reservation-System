package com.example.trainreservationsystem.controllers.auth;

import com.example.trainreservationsystem.services.AuthService;
import com.example.trainreservationsystem.utils.ui.AlertUtils;
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
    phoneField.textProperty().addListener((obs, old, val) -> {
      if (val != null && !val.matches("\\d{0,11}")) {
        phoneField.setText(old);
      }
    });
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
    if (phone.isEmpty() || phone.length() < 10) {
      showError("Please enter a valid phone number");
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

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }
}
