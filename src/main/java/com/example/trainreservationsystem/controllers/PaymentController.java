package com.example.trainreservationsystem.controllers;

import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.services.PaymentService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class PaymentController {
  @FXML
  private Label bookingDetailsLabel;
  @FXML
  private Label amountLabel;
  @FXML
  private ComboBox<PaymentMethod> paymentMethodCombo;
  @FXML
  private javafx.scene.control.Button payButton;
  @FXML
  private javafx.scene.control.Button addPaymentMethodButton;

  private final PaymentService paymentService = ServiceFactory.getPaymentService();
  private Booking booking;

  @FXML
  public void initialize() {
    booking = UserSession.getInstance().getPendingBooking();
    if (booking != null && booking.getSchedule() != null) {
      bookingDetailsLabel.setText("Booking ID: " + booking.getId() + " - " + booking.getSchedule().toString());
      amountLabel.setText("Amount: $" + booking.getSchedule().getPrice());
    }

    loadPaymentMethods();
  }

  private void loadPaymentMethods() {
    if (UserSession.getInstance().isLoggedIn()) {
      List<PaymentMethod> methods = paymentService.getPaymentMethods(
          UserSession.getInstance().getCurrentUser().getId());
      paymentMethodCombo.setItems(FXCollections.observableArrayList(methods));
      if (!methods.isEmpty()) {
        paymentMethodCombo.setValue(methods.get(0));
      }
    }
  }

  @FXML
  public void handlePay() {
    PaymentMethod selected = paymentMethodCombo.getValue();
    if (selected == null) {
      showAlert("Error", "Please select a payment method or add one.");
      return;
    }

    if (booking == null) {
      showAlert("Error", "No booking found.");
      return;
    }

    try {
      paymentService.processPayment(
          booking.getId(),
          booking.getSchedule().getPrice(),
          selected.getId());

      showAlert("Success", "Payment processed successfully! Booking confirmed.");
      HomeController.getInstance().loadView("/com/example/trainreservationsystem/booking-history.fxml");
    } catch (Exception e) {
      showAlert("Error", "Payment failed: " + e.getMessage());
    }
  }

  @FXML
  public void handleAddPaymentMethod() {
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/payment-method-view.fxml");
  }

  public void refreshPaymentMethods() {
    loadPaymentMethods();
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
