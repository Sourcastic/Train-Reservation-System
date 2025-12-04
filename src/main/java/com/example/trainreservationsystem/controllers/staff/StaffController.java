package com.example.trainreservationsystem.controllers.staff;

import java.io.IOException;

import com.example.trainreservationsystem.applications.HomeApplication;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.shared.ui.StylesheetHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
    private Button btnTrainStatus;
    @FXML
    private Button btnManageStops;
    @FXML
    private Button btnManageSeatClasses;
    @FXML
    private Button btnManageRoutes;
    @FXML
    private Button btnManageSchedules;
    @FXML
    private Button btnManageDiscounts;
    @FXML
    private Button btnManageUsers;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        // Load Dashboard by default
        showDashboard();
        // Hide admin-only features from staff
        setupRoleBasedVisibility();
    }

    private void setupRoleBasedVisibility() {
        com.example.trainreservationsystem.services.shared.UserSession session = com.example.trainreservationsystem.services.shared.UserSession
                .getInstance();

        if (session.isLoggedIn()) {
            String userType = session.getCurrentUser().getUserType();
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userType);

            // Hide admin-only features from staff
            if (!isAdmin) {
                // Staff cannot access these features
                btnManageUsers.setVisible(false);
                btnManageUsers.setManaged(false);
                btnManageDiscounts.setVisible(false);
                btnManageDiscounts.setManaged(false);
                btnManageStops.setVisible(false);
                btnManageStops.setManaged(false);
                btnManageSeatClasses.setVisible(false);
                btnManageSeatClasses.setManaged(false);
                btnManageRoutes.setVisible(false);
                btnManageRoutes.setManaged(false);
                btnManageSchedules.setVisible(false);
                btnManageSchedules.setManaged(false);
            }
        }
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
        resetButtonStyle(btnTrainStatus);
        resetButtonStyle(btnManageStops);
        resetButtonStyle(btnManageSeatClasses);
        resetButtonStyle(btnManageRoutes);
        resetButtonStyle(btnManageSchedules);
        resetButtonStyle(btnManageDiscounts);
        resetButtonStyle(btnManageUsers);

        // Highlight active button
        if (activeButton != null) {
            activeButton.setStyle(
                    "-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 10;");
        }
    }

    private void resetButtonStyle(Button button) {
        if (button != null) {
            button.setStyle(
                    "-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 10;");
        }
    }

    @FXML
    private void showDashboard() {
        loadView("staff/staff-dashboard-view.fxml", btnDashboard);
    }

    @FXML
    private void showComplaints() {
        // Assuming respondtocomplaints-view.fxml is the staff complaint view
        loadView("staff/respondtocomplaints-view.fxml", btnComplaints);
    }

    @FXML
    private void showBookings() {
        loadView("staff/manage-tickets-view.fxml", btnConfirmBookings);
    }

    @FXML
    private void showValidation() {
        loadView("staff/ticket-validation-view.fxml", btnTicketValidation);
    }

    @FXML
    private void showTrainStatus() {
        loadView("staff/updatetrainstatus-view.fxml", btnTrainStatus);
    }

    @FXML
    private void showManageStops() {
        loadView("admin/manage-stops-view.fxml", btnManageStops);
    }

    @FXML
    private void showManageSeatClasses() {
        loadView("admin/manage-seat-classes-view.fxml", btnManageSeatClasses);
    }

    @FXML
    private void showManageRoutes() {
        loadView("admin/manage-routes-view.fxml", btnManageRoutes);
    }

    @FXML
    private void showManageSchedules() {
        loadView("admin/manage-schedules-view.fxml", btnManageSchedules);
    }

    @FXML
    private void showManageDiscounts() {
        loadView("admin/manage-discounts-view.fxml", btnManageDiscounts);
    }

    @FXML
    private void showManageUsers() {
        loadView("admin/manage-users-view.fxml", btnManageUsers);
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(
                    HomeApplication.class.getResource("/com/example/trainreservationsystem/shared/landing-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            // Clear any existing stylesheets first to avoid conflicts
            scene.getStylesheets().clear();
            // Apply stylesheet programmatically to ensure it's loaded
            StylesheetHelper.applyStylesheet(scene);
            // Also apply to root in case scene isn't fully initialized
            StylesheetHelper.applyStylesheet(root);
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
