package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.services.AuthService;
import com.example.trainreservationsystem.services.UserSession;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        ChangeListener<String> fieldListener = (obs, oldVal, newVal) -> updateButtonState();
        emailField.textProperty().addListener(fieldListener);
        passwordField.textProperty().addListener(fieldListener);

        updateButtonState();
    }

    private void updateButtonState() {
        boolean hasEmail = emailField.getText() != null && !emailField.getText().trim().isEmpty();
        boolean hasPassword = passwordField.getText() != null && !passwordField.getText().isEmpty();
        loginButton.setDisable(!hasEmail || !hasPassword);
    }

    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            User user = authService.login(email, password);
            UserSession.getInstance().login(user);

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/home-view.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1380, 780));
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred during login. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
