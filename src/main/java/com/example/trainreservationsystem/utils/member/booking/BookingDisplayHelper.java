package com.example.trainreservationsystem.utils.member.booking;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.services.shared.UserSession;

import javafx.scene.control.Label;

/**
 * Helper for displaying booking details in UI.
 */
public class BookingDisplayHelper {

  public static void display(Booking booking, Label journeyLabel, Label dateLabel,
      Label passengersLabel, Label seatsLabel, Label amountLabel) {
    displayJourney(booking, journeyLabel, dateLabel);
    displayPassengers(booking, passengersLabel, seatsLabel);
    displayAmount(booking, amountLabel);
  }

  private static void displayJourney(Booking booking, Label journeyLabel, Label dateLabel) {
    journeyLabel.setText(booking.getSchedule().getRoute().getSource() +
        " → " + booking.getSchedule().getRoute().getDestination());
    // Use travel date from session (user's selected travel date) first, then
    // booking date as fallback
    java.time.LocalDate travelDate = UserSession.getInstance().getSelectedTravelDate();
    if (travelDate != null) {
      dateLabel.setText(travelDate.toString());
    } else if (booking.getBookingDate() != null) {
      dateLabel.setText(booking.getBookingDate().toLocalDate().toString());
    } else {
      dateLabel.setText("N/A");
    }
  }

  private static void displayPassengers(Booking booking, Label passengersLabel, Label seatsLabel) {
    if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
      passengersLabel.setText(String.valueOf(booking.getPassengers().size()));
      seatsLabel.setText(formatSeatNumbers(booking));
    }
  }

  private static String formatSeatNumbers(Booking booking) {
    return booking.getPassengers().stream()
        .map(p -> String.valueOf(p.getSeatNumber()))
        .reduce((a, b) -> a + ", " + b)
        .orElse("N/A");
  }

  private static void displayAmount(Booking booking, Label amountLabel) {
    double total = booking.getTotalAmount();
    amountLabel.setText("$" + String.format("%.2f", total));
  }
}
