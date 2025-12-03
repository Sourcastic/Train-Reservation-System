package com.example.trainreservationsystem.utils.shared.payment;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.services.shared.UserSession;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Helper for showing payment success and redirecting.
 */
public class PaymentSuccessHelper {

  public static void showSuccessAndRedirect(Booking booking, Label amountLabel,
      String paymentMethod) {
    String content = buildSuccessMessage(booking, amountLabel);
    showSuccessAlert(content, paymentMethod);
    clearAndRedirect();
  }

  private static String buildSuccessMessage(Booking booking, Label amountLabel) {
    return "Your booking has been confirmed.\n\n" +
        "Booking ID: #" + booking.getId() + "\n" +
        "Amount Paid: " + amountLabel.getText() + "\n\n" +
        "Check your booking history for e-ticket details.";
  }

  private static void showSuccessAlert(String content, String paymentMethod) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Payment Successful");
    alert.setHeaderText("Payment Completed via " + paymentMethod + "!");
    alert.setContentText(content);

    if (alert.getDialogPane().getScene() != null &&
        alert.getDialogPane().getScene().getWindow() != null) {
      alert.getDialogPane().getStylesheets().add(
          PaymentSuccessHelper.class.getResource("/com/example/trainreservationsystem/stylesheet.css")
              .toExternalForm());
      alert.getDialogPane().getStyleClass().add("success-alert");
    }

    alert.showAndWait();
  }

  private static void clearAndRedirect() {
    UserSession.getInstance().setPendingBooking(null);
    HomeController.getInstance().showHistory();
  }
}
