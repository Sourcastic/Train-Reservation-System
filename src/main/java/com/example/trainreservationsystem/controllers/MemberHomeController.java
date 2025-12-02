package com.example.trainreservationsystem.controllers;

import javafx.fxml.FXML;

public class MemberHomeController {

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
