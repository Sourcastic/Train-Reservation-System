package com.example.trainreservationsystem.controllers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;

public class HomeController {

    @FXML
    private Button manageAccountButton;
    @FXML
    private Button manageTitleLabel;
    @FXML
    private Button manageNotificationButton;
    @FXML
    private StackPane contentArea;

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
        // Create FontIcons
        // FontIcon userIcon = new FontIcon("fas-user");
        // userIcon.setIconSize(16);
        // userIcon.setIconColor(Color.web("#11111b"));

        // FontIcon labelLogoIcon = new FontIcon("fas-train");
        // labelLogoIcon.setIconSize(32);
        // labelLogoIcon.setIconColor(Color.web("#40a02b"));

        // FontIcon notificationIcon = new FontIcon("fas-bell");
        // notificationIcon.setIconSize(16);
        // notificationIcon.setIconColor(Color.web("#11111b"));

        // Attach the icons
        // manageAccountButton.setGraphic(userIcon);
        manageAccountButton.setContentDisplay(ContentDisplay.LEFT);
        manageAccountButton.setGraphicTextGap(8);

        // manageTitleLabel.setGraphic(labelLogoIcon);
        manageTitleLabel.setContentDisplay(ContentDisplay.LEFT);
        manageTitleLabel.setGraphicTextGap(8);

        // manageNotificationButton.setGraphic(notificationIcon);
        manageNotificationButton.setContentDisplay(ContentDisplay.LEFT);
        manageNotificationButton.setGraphicTextGap(8);

        // Load default view
        showSearch();
    }

    @FXML
    private void showSearch() {
        loadView("/com/example/trainreservationsystem/search-view.fxml");
    }

    @FXML
    private void showHistory() {
        loadView("/com/example/trainreservationsystem/booking-history.fxml");
    }

    @FXML
    private void showComplaint() {
        loadView("/com/example/trainreservationsystem/complaint-view.fxml");
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
}
