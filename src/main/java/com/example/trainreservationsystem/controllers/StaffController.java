package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.applications.HomeApplication;
import com.example.trainreservationsystem.services.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class StaffController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnComplaints;
    @FXML
    private Button btnConfirmBookings;
    @FXML
    private Button btnTicketValidation;
    @FXML
    private Button btnNotifications;
    @FXML
    private Button btnTrainStatus;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        // Load Dashboard by default
        showDashboard();
    }

    private void loadView(String fxmlFile, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HomeApplication.class.getResource("/com/example/trainreservationsystem/" + fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            updateActiveButton(activeButton);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error (e.g., show alert)
        }
    }

    private void updateActiveButton(Button activeButton) {
        // Reset styles for all buttons
        resetButtonStyle(btnDashboard);
        resetButtonStyle(btnComplaints);
        resetButtonStyle(btnConfirmBookings);
        resetButtonStyle(btnTicketValidation);
        resetButtonStyle(btnNotifications);
        resetButtonStyle(btnTrainStatus);

        // Highlight active button
        if (activeButton != null) {
            activeButton.setStyle(
                    "-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 10;");
        }
    }

    private void resetButtonStyle(Button button) {
        button.setStyle(
                "-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 10;");
    }

    @FXML
    private void showDashboard() {
        loadView("staff-dashboard-view.fxml", btnDashboard);
    }

    @FXML
    private void showComplaints() {
        // Assuming respondtocomplaints-view.fxml is the staff complaint view
        loadView("respondtocomplaints-view.fxml", btnComplaints);
    }

    @FXML
    private void showBookings() {
        // Placeholder or actual view
        // loadView("confirm-bookings-view.fxml", btnConfirmBookings);
        System.out.println("Show Bookings clicked");
        updateActiveButton(btnConfirmBookings);
    }

    @FXML
    private void showValidation() {
        // Placeholder or actual view
        // loadView("ticket-validation-view.fxml", btnTicketValidation);
        System.out.println("Show Validation clicked");
        updateActiveButton(btnTicketValidation);
    }

    @FXML
    private void showNotifications() {
        // Placeholder or actual view
        // loadView("notifications-view.fxml", btnNotifications);
        System.out.println("Show Notifications clicked");
        updateActiveButton(btnNotifications);
    }

    @FXML
    private void showTrainStatus() {
        loadView("updatetrainstatus-view.fxml", btnTrainStatus);
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(
                    HomeApplication.class.getResource("/com/example/trainreservationsystem/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
