package com.example.trainreservationsystem.controllers.auth;

import com.example.trainreservationsystem.services.AuthService;
import com.example.trainreservationsystem.services.UserSession;
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
 * Controller for user login.
 * Handles authentication and redirects to appropriate dashboard.
 */
public class LoginController {
  @FXML
  private TextField emailField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private Label errorLabel;
  @FXML
  private Button loginButton;

  private final AuthService authService = new AuthService();

  @FXML
  public void initialize() {
    errorLabel.setText("");
    setupEnterKeyHandlers();
  }

  private void setupEnterKeyHandlers() {
    passwordField.setOnAction(e -> handleLogin());
  }

  @FXML
  public void handleLogin() {
    errorLabel.setText("");
    String email = emailField.getText().trim();
    String password = passwordField.getText();

    if (!validateInput(email, password)) {
      return;
    }

    try {
      var user = authService.login(email, password);
      UserSession.getInstance().login(user);
      redirectToDashboard(user.getUserType());
    } catch (IllegalArgumentException e) {
      showError(e.getMessage());
    } catch (Exception e) {
      showError("Login failed. Please try again.");
      e.printStackTrace();
    }
  }

  private boolean validateInput(String email, String password) {
    if (email.isEmpty()) {
      showError("Please enter your email");
      return false;
    }
    if (password.isEmpty()) {
      showError("Please enter your password");
      return false;
    }
    return true;
  }

  private void redirectToDashboard(String userType) {
    try {
      Stage stage = (Stage) emailField.getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/home-view.fxml"));
      Parent root = loader.load();
      stage.setScene(new Scene(root, 1380, 780));
      stage.setTitle("Train Reservation System");
      // HomeController.initialize() will automatically load the appropriate dashboard
      // based on user login status, so no need to call showDashboard() here
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load dashboard: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  public void handleSignup() {
    try {
      Stage stage = (Stage) emailField.getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/signup-view.fxml"));
      Parent root = loader.load();
      stage.setScene(new Scene(root, 1280, 800));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load signup page");
      e.printStackTrace();
    }
  }

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }
}
