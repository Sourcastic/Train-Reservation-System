package com.example.trainreservationsystem.controllers;

import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.services.*;

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
    private final BookingService bookingService = ServiceFactory.getBookingService();
    private final NotificationService notificationsService = ServiceFactory.getNotificationService();

    private Booking booking;

    @FXML
    public void initialize() {
        booking = UserSession.getInstance().getPendingBooking();

        if (booking != null && booking.getSchedule() != null) {
            bookingDetailsLabel.setText(
                    "Booking ID: " + booking.getId() +
                            " | Route: " + booking.getSchedule().getRoute().getSource() +
                            " → " + booking.getSchedule().getRoute().getDestination()
            );
            amountLabel.setText("Amount: $" + booking.getSchedule().getPrice());
        }

        loadPaymentMethods();
    }

    private void loadPaymentMethods() {
        if (UserSession.getInstance().isLoggedIn()) {
            List<PaymentMethod> methods = paymentService.getPaymentMethods(
                    UserSession.getInstance().getCurrentUser().getId()
            );

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
            showAlert("Error", "Please select a payment method.");
            return;
        }

        if (booking == null || booking.getSchedule() == null) {
            showAlert("Error", "No booking found.");
            return;
        }

        try {
            // 1️⃣ Process payment
            paymentService.processPayment(
                    booking.getId(),
                    booking.getSchedule().getPrice(),
                    selected.getId()
            );

            // 2️⃣ Confirm booking
            bookingService.confirmBooking(booking.getId());

            // 3️⃣ Send notification
            notificationsService.sendNotification(
                    booking.getUserId(),
                    "Booking Confirmed! Your booking ID " + booking.getId() +
                            " is confirmed. Paid: $" + booking.getSchedule().getPrice()
            );

            // 4️⃣ Show success
            showAlert("Success", "Payment successful! Booking confirmed.");

            // 5️⃣ Redirect to Booking History
            HomeController.getInstance().loadView(
                    "/com/example/trainreservationsystem/booking-history.fxml"
            );

        } catch (Exception e) {
            showAlert("Payment Failed", e.getMessage());
        }
    }

    @FXML
    public void handleAddPaymentMethod() {
        HomeController.getInstance().loadView(
                "/com/example/trainreservationsystem/payment-method-view.fxml"
        );
    }

    public void refreshPaymentMethods() {
        loadPaymentMethods();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
