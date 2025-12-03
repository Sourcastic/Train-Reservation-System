package com.example.trainreservationsystem.controllers.member.booking;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.Passenger;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Controller for displaying e-ticket details.
 * Shows booking information in a printable ticket format.
 */
public class TicketViewController {
  @FXML
  private Label bookingIdLabel, fromLabel, toLabel, dateLabel, timeLabel;
  @FXML
  private Label passengerLabel, seatLabel, statusLabel, amountLabel;
  @FXML
  private Label ticketIcon;
  @FXML
  private Rectangle qrPlaceholder;

  private Booking booking;

  @FXML
  public void initialize() {
    setupIcons();
  }

  private void setupIcons() {
    if (ticketIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.TICKET_ALT, 48, "#170d13");
      ticketIcon.setGraphic(icon);
    }
  }

  public void setBooking(Booking booking) {
    this.booking = booking;
    displayTicketInfo();
  }

  private void displayTicketInfo() {
    if (booking == null || booking.getSchedule() == null)
      return;

    displayBasicInfo();
    displayPassengerInfo();
    displayStatus();
  }

  private void displayBasicInfo() {
    bookingIdLabel.setText("Booking #" + booking.getId());
    fromLabel.setText(booking.getSchedule().getRoute().getSource());
    toLabel.setText(booking.getSchedule().getRoute().getDestination());
    dateLabel.setText(booking.getSchedule().getDepartureDate().toString());
    timeLabel.setText(booking.getSchedule().getDepartureTime().toString());
    amountLabel.setText("PKR " + String.format("%.2f", booking.getTotalAmount()));
  }

  private void displayPassengerInfo() {
    if (booking.getPassengers() == null || booking.getPassengers().isEmpty())
      return;

    Passenger first = booking.getPassengers().get(0);
    String name = first.getName().split("\\(")[0].trim();
    passengerLabel.setText(name);

    // Use simple loop instead of stream - easier to understand
    StringBuilder seatsBuilder = new StringBuilder();
    for (int i = 0; i < booking.getPassengers().size(); i++) {
      if (i > 0) {
        seatsBuilder.append(", ");
      }
      seatsBuilder.append(booking.getPassengers().get(i).getSeatNumber());
    }
    String seats = seatsBuilder.length() > 0 ? seatsBuilder.toString() : "N/A";
    seatLabel.setText(seats);
  }

  private void displayStatus() {
    statusLabel.setText(booking.getStatus());
    applyStatusStyle(booking.getStatus());
  }

  private void applyStatusStyle(String status) {
    statusLabel.getStyleClass().removeAll("status-confirmed", "status-pending", "status-cancelled");
    switch (status) {
      case "CONFIRMED":
        statusLabel.getStyleClass().add("status-confirmed");
        break;
      case "PENDING":
        statusLabel.getStyleClass().add("status-pending");
        break;
      case "CANCELLED":
        statusLabel.getStyleClass().add("status-cancelled");
        break;
    }
  }

  @FXML
  private void handleClose() {
    Stage stage = (Stage) bookingIdLabel.getScene().getWindow();
    stage.close();
  }
}
