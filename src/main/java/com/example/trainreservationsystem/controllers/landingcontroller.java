package com.example.trainreservationsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LandingController {

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/login-view.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/signup-view.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
