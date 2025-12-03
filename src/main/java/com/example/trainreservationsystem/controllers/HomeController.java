package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.User;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController {

    @FXML
    private javafx.scene.control.Label manageTitleLabel;
    @FXML
    private Button userNameLabel;
    @FXML
    private Button manageNotificationButton;
    @FXML
    private Button logoutButton;

    @FXML
    private ContextMenu menu;

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

        // Add train icon to the title
        FontIcon trainIcon = new FontIcon(FontAwesomeSolid.TRAIN);
        trainIcon.setIconSize(18);
        trainIcon.setIconColor(Color.web("#170d13"));
        manageTitleLabel.setGraphic(trainIcon);
        manageTitleLabel.setContentDisplay(ContentDisplay.LEFT);
        manageTitleLabel.setGraphicTextGap(10);

        // Set user name with profile icon
        if (UserSession.getInstance().isLoggedIn()) {
            String username = UserSession.getInstance().getCurrentUser().getUsername();
            userNameLabel.setText(username);

            FontIcon profileIcon = new FontIcon(FontAwesomeSolid.USER_CIRCLE);
            profileIcon.setIconSize(16);
            profileIcon.setIconColor(Color.web("#170d13"));
            userNameLabel.setGraphic(profileIcon);
            userNameLabel.setContentDisplay(ContentDisplay.LEFT);
            userNameLabel.setGraphicTextGap(8);
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
    private void openDropdown() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        if ("ADMIN".equalsIgnoreCase(currentUser.getUserType())) {
            // Show context menu for Admin
            if (menu == null) {
                menu = new ContextMenu();
                javafx.scene.control.MenuItem updateProfileItem = new javafx.scene.control.MenuItem("Update Profile");
                updateProfileItem.setOnAction(e -> navigateToUpdateProfile());

                javafx.scene.control.MenuItem staffDashboardItem = new javafx.scene.control.MenuItem("Staff Dashboard");
                staffDashboardItem.setOnAction(e -> {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("/com/example/trainreservationsystem/staff-view.fxml"));
                        javafx.scene.Parent root = loader.load();
                        javafx.scene.Scene scene = new javafx.scene.Scene(root);
                        javafx.stage.Stage stage = (javafx.stage.Stage) userNameLabel.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        AlertUtils.showError("Navigation Error", "Could not load Staff Dashboard.");
                    }
                });

                menu.getItems().addAll(updateProfileItem, staffDashboardItem);
            }
            // Show menu below the button
            menu.show(userNameLabel, javafx.geometry.Side.BOTTOM, 0, 0);
        } else {
            // Direct navigation for Customer
            navigateToUpdateProfile();
        }
    }

    private void navigateToUpdateProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/trainreservationsystem/update-profile-view.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, e);
            AlertUtils.showError("Navigation Error", "Failed to load Update Profile view.");
        }
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
