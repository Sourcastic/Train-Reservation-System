package com.example.trainreservationsystem.controllers.shared.auth;

import com.example.trainreservationsystem.utils.shared.ui.StylesheetHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for landing page.
 * Provides navigation to login and signup.
 */
public class LandingController {

  @FXML
  private Button loginButton;

  @FXML
  public void handleLogin() {
    navigateTo("/com/example/trainreservationsystem/shared/login-view.fxml");
  }

  @FXML
  public void handleSignup() {
    navigateTo("/com/example/trainreservationsystem/shared/signup-view.fxml");
  }

  private void navigateTo(String fxmlPath) {
    try {
      Stage stage = (Stage) loginButton.getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent root = loader.load();
      Scene scene = new Scene(root, 1280, 800);
      StylesheetHelper.applyStylesheet(scene);
      stage.setScene(scene);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
