package com.example.trainreservationsystem.controllers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.services.NotificationService;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.utils.ui.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private javafx.scene.control.Label manageTitleLabel;
    @FXML
    private javafx.scene.control.Label userNameLabel;
    @FXML
    private Button manageNotificationButton;
    @FXML
    private Button logoutButton;
    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnComplaint;

    private static HomeController instance;

    public static HomeController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;

        // Add notification listener for badge updates
        NotificationService.getInstance().addListener(count -> {
            updateNotificationBadge(count);
        });

        // Create FontIcons using FontAwesome5 enum
        FontIcon notificationIcon = new FontIcon(FontAwesomeSolid.BELL);
        notificationIcon.setIconSize(16);
        notificationIcon.setIconColor(Color.web("#170d13"));

        FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.setIconSize(16);
        searchIcon.setIconColor(Color.web("#170d13"));

        FontIcon historyIcon = new FontIcon(FontAwesomeSolid.HISTORY);
        historyIcon.setIconSize(16);
        historyIcon.setIconColor(Color.web("#170d13"));

        FontIcon complaintIcon = new FontIcon(FontAwesomeSolid.COMMENT);
        complaintIcon.setIconSize(16);
        complaintIcon.setIconColor(Color.web("#170d13"));

        FontIcon dashboardIcon = new FontIcon(FontAwesomeSolid.TH);
        dashboardIcon.setIconSize(16);
        dashboardIcon.setIconColor(Color.web("#170d13"));

        // Set user name
        if (UserSession.getInstance().isLoggedIn()) {
            String username = UserSession.getInstance().getCurrentUser().getUsername();
            userNameLabel.setText(username);
        }

        // Attach the icons
        manageNotificationButton.setGraphic(notificationIcon);
        manageNotificationButton.setContentDisplay(ContentDisplay.LEFT);
        manageNotificationButton.setGraphicTextGap(8);

        btnDashboard.setGraphic(dashboardIcon);
        btnDashboard.setGraphicTextGap(12);

        btnSearch.setGraphic(searchIcon);
        btnSearch.setGraphicTextGap(12);

        btnHistory.setGraphic(historyIcon);
        btnHistory.setGraphicTextGap(12);

        btnComplaint.setGraphic(complaintIcon);
        btnComplaint.setGraphicTextGap(12);

        // Load default view based on login status
        if (UserSession.getInstance().isLoggedIn()) {
            String userType = UserSession.getInstance().getCurrentUser().getUserType();
            if ("CUSTOMER".equals(userType) || "MEMBER".equals(userType)) {
                loadView("/com/example/trainreservationsystem/member-home.fxml");
            } else {
                // Staff dashboard or other user types
                loadView("/com/example/trainreservationsystem/search/search-view.fxml");
            }
        } else {
            // Not logged in - redirect to landing
            redirectToLanding();
        }
    }

    private void redirectToLanding() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/trainreservationsystem/landing-view.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        if (AlertUtils.showConfirmation("Logout", "Are you sure you want to logout?")) {
            UserSession.getInstance().logout();
            redirectToLanding();
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/com/example/trainreservationsystem/member-home.fxml");
    }

    @FXML
    public void showSearch() {
        loadView("/com/example/trainreservationsystem/search/search-view.fxml");
    }

    @FXML
    public void showHistory() {
        loadView("/com/example/trainreservationsystem/booking/booking-history.fxml");
    }

    @FXML
    private void showComplaint() {
        loadView("/com/example/trainreservationsystem/complaint-view.fxml");
    }

    @FXML
    private void openNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/trainreservationsystem/notification-inbox.fxml"));
            Parent root = loader.load();

            Stage notificationStage = new Stage();
            notificationStage.setTitle("Notifications");
            notificationStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            notificationStage.setScene(new Scene(root));
            notificationStage.setResizable(false);
            notificationStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to simple alert
            var notifications = NotificationService.getInstance().getAll();
            String content = notifications.isEmpty()
                    ? "No notifications yet."
                    : String.join("\n• ", notifications);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("Recent Activity");
            alert.setContentText(notifications.isEmpty() ? content : "• " + content);
            alert.showAndWait();
        }
    }

    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge(int count) {
        if (count > 0) {
            // Add badge to notification button text
            manageNotificationButton.setText("Notifications (" + count + ")");
        } else {
            manageNotificationButton.setText("Notifications");
        }
    }
}
