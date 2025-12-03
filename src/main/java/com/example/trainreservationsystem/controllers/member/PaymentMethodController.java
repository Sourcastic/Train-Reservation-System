package com.example.trainreservationsystem.controllers.member;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.member.PaymentMethod;
import com.example.trainreservationsystem.services.member.payment.PaymentService;
import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.fxml.FXML;
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
      AlertUtils.showError("Error", "Please login first");
      return;
    }

    String methodType = methodTypeCombo.getValue();
    String details = detailsField.getText();

    if (methodType == null || details == null || details.trim().isEmpty()) {
      AlertUtils.showError("Error", "Please fill all fields");
      return;
    }

    PaymentMethod method = new PaymentMethod();
    method.setUserId(UserSession.getInstance().getCurrentUser().getId());
    method.setMethodType(methodType);
    method.setDetails(details);

    paymentService.addPaymentMethod(method);
    NotificationService.getInstance().add("New payment method added: " + methodType);
    AlertUtils.showSuccess("Success", "Payment method added successfully!");

    // Go back to payment screen if there's a pending booking
    if (UserSession.getInstance().getPendingBooking() != null) {
      HomeController.getInstance().loadView("/com/example/trainreservationsystem/member/payment/payment-view.fxml");
    } else {
      HomeController.getInstance().loadView("/com/example/trainreservationsystem/member/search/search-view.fxml");
    }
  }
}
