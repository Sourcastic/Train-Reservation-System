package com.example.trainreservationsystem.utils.shared.payment;

import java.util.HashMap;
import java.util.Map;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.services.member.booking.BookingService;
import com.example.trainreservationsystem.services.member.payment.PaymentService;
import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Processes payments with delay simulation.
 * Handles booking confirmation, discount codes, and loyalty points.
 */
public class PaymentProcessor {

  /**
   * Legacy method for backward compatibility.
   *
   * @deprecated Use process with PaymentAdapter instead
   */
  @Deprecated
  public static void process(Booking booking, BookingService bookingService,
      String paymentMethod, String details,
      Runnable onSuccess, Runnable onError) {
    process(booking, bookingService, paymentMethod, details, null, onSuccess, onError);
  }

  public static void process(Booking booking, BookingService bookingService,
      PaymentAdapter adapter, Map<String, String> paymentDetails, String discountCode,
      Runnable onSuccess, Runnable onError) {
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

    pause.setOnFinished(e -> {
      Platform.runLater(() -> {
        try {
          PaymentService paymentService = ServiceFactory.getPaymentService();
          double originalAmount = booking.getTotalAmount();

          // Process payment using adapter
          paymentService.processPayment(
              booking.getId(),
              originalAmount,
              adapter,
              paymentDetails,
              discountCode);

          String paymentMethod = adapter.getMethodName();
          String details = buildPaymentDetailsString(paymentDetails, adapter);
          sendNotifications(booking, paymentMethod, details, discountCode, originalAmount);
          onSuccess.run();
        } catch (Exception ex) {
          System.err.println("Error processing payment: " + ex.getMessage());
          ex.printStackTrace();
          onError.run();
        }
      });
    });

    pause.play();
  }

  /**
   * Legacy method for backward compatibility.
   *
   * @deprecated Use process with PaymentAdapter instead
   */
  @Deprecated
  public static void process(Booking booking, BookingService bookingService,
      String paymentMethod, String details, String discountCode,
      Runnable onSuccess, Runnable onError) {
    // Convert to adapter-based approach
    PaymentAdapter adapter = createAdapterForMethod(paymentMethod);
    Map<String, String> paymentDetails = parseDetailsString(details);

    process(booking, bookingService, adapter, paymentDetails, discountCode, onSuccess, onError);
  }

  private static PaymentAdapter createAdapterForMethod(String methodName) {
    if (methodName.contains("Card")) {
      return new com.example.trainreservationsystem.utils.shared.payment.adapters.CardPaymentAdapter();
    } else if (methodName.contains("Bank") || methodName.contains("Transfer")) {
      return new com.example.trainreservationsystem.utils.shared.payment.adapters.BankTransferPaymentAdapter();
    } else if (methodName.contains("Wallet")) {
      return new com.example.trainreservationsystem.utils.shared.payment.adapters.WalletPaymentAdapter();
    }
    // Default to card adapter
    return new com.example.trainreservationsystem.utils.shared.payment.adapters.CardPaymentAdapter();
  }

  private static Map<String, String> parseDetailsString(String details) {
    Map<String, String> map = new HashMap<>();
    // Simple parsing - in real app, this would be more sophisticated
    if (details != null && !details.isEmpty()) {
      map.put("details", details);
    }
    return map;
  }

  private static String buildPaymentDetailsString(Map<String, String> details, PaymentAdapter adapter) {
    if (adapter.getMethodName().contains("Card")) {
      return "Card ending in " + (details.get("cardNumber") != null
          ? details.get("cardNumber").substring(Math.max(0, details.get("cardNumber").length() - 4))
          : "****");
    } else if (adapter.getMethodName().contains("Bank")) {
      return "IBAN: " + details.getOrDefault("iban", "****");
    } else if (adapter.getMethodName().contains("Wallet")) {
      return "Loyalty Points: " + details.getOrDefault("points", "N/A");
    }
    return details.toString();
  }

  private static void sendNotifications(Booking booking, String paymentMethod, String details,
      String discountCode, double originalAmount) {
    NotificationService.getInstance().add(
        "Payment successful via " + paymentMethod + "! Booking #" + booking.getId() + " confirmed");
    NotificationService.getInstance().add(
        "E-ticket generated for " + booking.getSchedule().getRoute().getSource() +
            " to " + booking.getSchedule().getRoute().getDestination());

    if (discountCode != null && !discountCode.trim().isEmpty()) {
      NotificationService.getInstance().add(
          "Discount code '" + discountCode + "' applied successfully!");
    }

    NotificationService.getInstance().add("Payment Details: " + details);
  }
}
