package com.example.trainreservationsystem.controllers.shared;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import com.example.trainreservationsystem.models.shared.User;
import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
        NotificationService.getInstance().addListener(this::updateNotificationBadge);
        setupIcons();
        loadDefaultView();
    }

    private void setupIcons() {
        manageTitleLabel.setGraphic(IconHelper.createIcon(FontAwesomeSolid.TRAIN, 18, "#170d13"));
        manageTitleLabel.setContentDisplay(ContentDisplay.LEFT);
        manageTitleLabel.setGraphicTextGap(10);

        if (UserSession.getInstance().isLoggedIn()) {
            userNameLabel.setText(UserSession.getInstance().getCurrentUser().getUsername());
            userNameLabel.setGraphic(IconHelper.createIcon(FontAwesomeSolid.USER_CIRCLE, 16, "#170d13"));
            userNameLabel.setContentDisplay(ContentDisplay.LEFT);
            userNameLabel.setGraphicTextGap(8);
        }

        setButtonIcon(manageNotificationButton, FontAwesomeSolid.BELL, 8);
        setButtonIcon(btnDashboard, FontAwesomeSolid.TH, 12);
        setButtonIcon(btnSearch, FontAwesomeSolid.SEARCH, 12);
        setButtonIcon(btnHistory, FontAwesomeSolid.HISTORY, 12);
        setButtonIcon(btnComplaint, FontAwesomeSolid.COMMENT, 12);
    }

    private void setButtonIcon(Button button, FontAwesomeSolid icon, double gap) {
        button.setGraphic(IconHelper.createIcon(icon, 16, "#170d13"));
        button.setGraphicTextGap(gap);
    }

    private void loadDefaultView() {
        if (!UserSession.getInstance().isLoggedIn()) {
            redirectToLanding();
            return;
        }

        String userType = UserSession.getInstance().getCurrentUser().getUserType();
        String viewPath = ("CUSTOMER".equals(userType) || "MEMBER".equals(userType))
                ? "/com/example/trainreservationsystem/member/member-home.fxml"
                : "/com/example/trainreservationsystem/member/search/search-view.fxml";
        loadView(viewPath);
    }

    private void redirectToLanding() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/shared/landing-view.fxml"));
            stage.setScene(new Scene(root, 1280, 800));
        } catch (IOException e) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, "Failed to redirect to landing", e);
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
        loadView("/com/example/trainreservationsystem/member/member-home.fxml");
    }

    @FXML
    public void showSearch() {
        loadView("/com/example/trainreservationsystem/member/search/search-view.fxml");
    }

    @FXML
    public void showHistory() {
        loadView("/com/example/trainreservationsystem/member/booking/booking-history.fxml");
    }

    public void showStaffDashboard() {
        loadView("/com/example/trainreservationsystem/staff/staff-dashboard-view.fxml");
    }

    @FXML
    private void openDropdown() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        boolean isStaff = "ADMIN".equalsIgnoreCase(currentUser.getUserType())
                || "STAFF".equalsIgnoreCase(currentUser.getUserType());

        if (isStaff) {
            showStaffMenu();
        } else {
            navigateToUpdateProfile();
        }
    }

    private void showStaffMenu() {
        if (menu == null) {
            menu = new ContextMenu();
            MenuItem updateProfileItem = new MenuItem("Update Profile");
            updateProfileItem.setOnAction(e -> navigateToUpdateProfile());

            MenuItem staffDashboardItem = new MenuItem("Staff Dashboard");
            staffDashboardItem.setOnAction(e -> navigateToStaffDashboard());

            menu.getItems().addAll(updateProfileItem, staffDashboardItem);
        }
        menu.show(userNameLabel, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void navigateToStaffDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/staff/staff-view.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, "Failed to load staff dashboard", e);
            AlertUtils.showError("Navigation Error", "Could not load Staff Dashboard.");
        }
    }

    private void navigateToUpdateProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/trainreservationsystem/member/update-profile-view.fxml"));
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
        loadView("/com/example/trainreservationsystem/member/complaint-view.fxml");
    }

    @FXML
    private void openNotifications() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/trainreservationsystem/member/notification-inbox.fxml"));
            Stage notificationStage = new Stage();
            notificationStage.setTitle("Notifications");
            notificationStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            notificationStage.setScene(new Scene(root));
            notificationStage.setResizable(false);
            notificationStage.showAndWait();
        } catch (IOException e) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, "Failed to load notifications", e);
            AlertUtils.showError("Error", "Failed to load notifications.");
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
