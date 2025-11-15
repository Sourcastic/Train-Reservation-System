package com.example.trainreservationsystem;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import javafx.scene.control.ContentDisplay;



public class HomeController {

    @FXML
    private Button manageAccountButton;

    @FXML
    private HBox manageSearchBox;

    @FXML
    private Button manageSwapButton;

    @FXML
    private Button manageSearchButton;

    @FXML
    private Button manageTitleLabel;

    @FXML
    private Button manageNotificationButton;

    @FXML
    void onManageAccountClick(MouseEvent event) {
        System.out.println("Manage Account");
    }



    @FXML
    private void initialize() {
        // Create FontIcons
        FontIcon userIcon = new FontIcon("fas-user");
        userIcon.setIconSize(16);
        userIcon.setIconColor(Color.web("#11111b"));

        FontIcon labelLogoIcon = new FontIcon("fas-train");
        labelLogoIcon.setIconSize(32);
        labelLogoIcon.setIconColor(Color.web("#40a02b"));

        FontIcon notificationIcon = new FontIcon("fas-bell");
        notificationIcon.setIconSize(16);
        notificationIcon.setIconColor(Color.web("#11111b"));

        FontIcon searchIcon = new FontIcon("fas-search");
        searchIcon.setIconSize(16);
        searchIcon.setIconColor(Color.web("#11111b"));

        FontIcon swapIcon = new FontIcon("fas-exchange-alt");
        swapIcon.setIconSize(16);
        swapIcon.setIconColor(Color.web("#11111b"));


        // Attach the icons
        manageAccountButton.setGraphic(userIcon);
        manageAccountButton.setContentDisplay(ContentDisplay.LEFT);
        manageAccountButton.setGraphicTextGap(8);

        manageTitleLabel.setGraphic(labelLogoIcon);
        manageTitleLabel.setContentDisplay(ContentDisplay.LEFT);
        manageTitleLabel.setGraphicTextGap(8);

        manageNotificationButton.setGraphic(notificationIcon);
        manageNotificationButton.setContentDisplay(ContentDisplay.LEFT);
        manageNotificationButton.setGraphicTextGap(8);

        manageSearchButton.setGraphic(searchIcon);
        manageSearchButton.setContentDisplay(ContentDisplay.LEFT);


        manageSwapButton.setGraphic(swapIcon);
        manageSwapButton.setContentDisplay(ContentDisplay.LEFT);
        manageSwapButton.setGraphicTextGap(8);



    }
}
