package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.services.AuthService;
import com.example.trainreservationsystem.services.UserSession;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private Button signupButton;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        signupButton.disableProperty().bind(
                Bindings.isEmpty(nameField.textProperty())
                        .or(Bindings.isEmpty(emailField.textProperty()))
                        .or(Bindings.isEmpty(phoneField.textProperty()))
                        .or(Bindings.isEmpty(passwordField.textProperty())));
    }

    @FXML
    private void handleSignup() {
        try {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passwordField.getText();

            User user = authService.signup(name, email, phone, password);
            UserSession.getInstance().login(user);

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/home-view.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1380, 780));
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred during signup. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
