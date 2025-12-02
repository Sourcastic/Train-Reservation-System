package com.example.trainreservationsystem.utils.payment;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.services.NotificationService;
import com.example.trainreservationsystem.services.booking.BookingService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Processes payments with delay simulation.
 * Handles booking confirmation and notifications.
 */
public class PaymentProcessor {

  public static void process(Booking booking, BookingService bookingService,
      String paymentMethod, String details,
      Runnable onSuccess, Runnable onError) {
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

    pause.setOnFinished(e -> {
      Platform.runLater(() -> {
        try {
          bookingService.confirmBooking(booking.getId());
          sendNotifications(booking, paymentMethod, details);
          onSuccess.run();
        } catch (Exception ex) {
          onError.run();
        }
      });
    });

    pause.play();
  }

  private static void sendNotifications(Booking booking, String paymentMethod, String details) {
    NotificationService.getInstance().add(
        "Payment successful via " + paymentMethod + "! Booking #" + booking.getId() + " confirmed");
    NotificationService.getInstance().add(
        "E-ticket generated for " + booking.getSchedule().getRoute().getSource() +
            " to " + booking.getSchedule().getRoute().getDestination());
    NotificationService.getInstance().add("Payment Details: " + details);
  }
}
