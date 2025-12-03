package com.example.trainreservationsystem.controllers.member;

import com.example.trainreservationsystem.services.member.LoyaltyPointsService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;
import com.example.trainreservationsystem.services.shared.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MemberHomeController {

  @FXML
  private Label loyaltyPointsLabel;

  @FXML
  private void initialize() {
    // Display loyalty points for the current user
    if (UserSession.getInstance().isLoggedIn()) {
      LoyaltyPointsService loyaltyService = ServiceFactory.getLoyaltyPointsService();
      int points = loyaltyService.getLoyaltyPoints(UserSession.getInstance().getCurrentUser().getId());
      loyaltyPointsLabel.setText(String.format("Loyalty Points: %d", points));
    }
  }

  @FXML
  private void goToSearch() {
    HomeController.getInstance().showSearch();
  }

  @FXML
  private void goToBook() {
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/booking/booking-view.fxml");
  }

  @FXML
  private void goToHistory() {
    HomeController.getInstance().showHistory();
  }
}
