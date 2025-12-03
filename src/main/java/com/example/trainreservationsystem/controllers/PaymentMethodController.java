package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.services.PaymentService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class PaymentMethodController {
  @FXML
  private ComboBox<String> methodTypeCombo;
  @FXML
  private TextField detailsField;

  private final PaymentService paymentService = ServiceFactory.getPaymentService();

  @FXML
  public void initialize() {
    methodTypeCombo.getItems().addAll("CARD", "WALLET", "BANK_TRANSFER");
    methodTypeCombo.setValue("CARD");
  }

  @FXML
  public void handleSave() {
    if (!UserSession.getInstance().isLoggedIn()) {
      showAlert("Error", "Please login first");
      return;
    }

    String methodType = methodTypeCombo.getValue();
    String details = detailsField.getText();

    if (methodType == null || details == null || details.trim().isEmpty()) {
      showAlert("Error", "Please fill all fields");
      return;
    }

    PaymentMethod method = new PaymentMethod();
    method.setUserId(UserSession.getInstance().getCurrentUser().getId());
    method.setMethodType(methodType);
    method.setDetails(details);

    paymentService.addPaymentMethod(method);
    showAlert("Success", "Payment method added successfully!");

    // Go back to payment screen if there's a pending booking
    if (UserSession.getInstance().getPendingBooking() != null) {
      HomeController.getInstance().loadView("/com/example/trainreservationsystem/payment-view.fxml");
    } else {
      HomeController.getInstance().loadView("/com/example/trainreservationsystem/search-view.fxml");
    }
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
