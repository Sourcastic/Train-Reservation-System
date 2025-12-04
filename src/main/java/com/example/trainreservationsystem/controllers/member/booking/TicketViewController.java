package com.example.trainreservationsystem.controllers.member.booking;

import java.util.List;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.Passenger;
import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Controller for displaying e-ticket details.
 * Shows booking information in a printable ticket format.
 * Supports navigation through multiple tickets for a single booking.
 */
public class TicketViewController {
  @FXML
  private Label bookingIdLabel, fromLabel, toLabel, dateLabel, timeLabel;
  @FXML
  private Label passengerLabel, seatLabel, statusLabel, amountLabel;
  @FXML
  private Label ticketIcon, ticketCounterLabel;
  @FXML
  private Rectangle qrPlaceholder;
  @FXML
  private Button previousButton, nextButton;

  private Booking booking;
  private List<Ticket> tickets;
  private int currentTicketIndex = 0;

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

  /**
   * Sets the booking and tickets to display.
   * 
   * @param booking The booking information
   * @param tickets List of tickets for this booking
   */
  public void setBookingWithTickets(Booking booking, List<Ticket> tickets) {
    this.booking = booking;
    this.tickets = tickets;
    this.currentTicketIndex = 0;
    displayTicketInfo();
    updateNavigationButtons();
  }

  private void displayTicketInfo() {
    if (booking == null || booking.getSchedule() == null || tickets == null || tickets.isEmpty())
      return;

    displayBasicInfo();
    displayCurrentTicket();
    displayStatus();
    updateTicketCounter();
  }

  private void displayBasicInfo() {
    bookingIdLabel.setText("Booking #" + booking.getId());
    fromLabel.setText(booking.getSchedule().getRoute().getSource());
    toLabel.setText(booking.getSchedule().getRoute().getDestination());
    // Use booking date as fallback - in future, add travelDate field to Booking
    if (booking.getBookingDate() != null) {
      dateLabel.setText(booking.getBookingDate().toLocalDate().toString());
    } else {
      dateLabel.setText("N/A");
    }
    timeLabel.setText(booking.getSchedule().getDepartureTime().toString());
    amountLabel.setText("PKR " + String.format("%.2f", booking.getTotalAmount()));
  }

  private void displayCurrentTicket() {
    if (tickets == null || tickets.isEmpty() || currentTicketIndex >= tickets.size())
      return;

    Ticket currentTicket = tickets.get(currentTicketIndex);

    // Display passenger name (find passenger with matching seat)
    if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
      for (Passenger p : booking.getPassengers()) {
        if (p.getSeatNumber() == currentTicket.getSeatId()) {
          String name = p.getName().split("\\(")[0].trim();
          passengerLabel.setText(name);
          break;
        }
      }
    }

    // Display single seat number for current ticket
    seatLabel.setText(String.valueOf(currentTicket.getSeatId()));
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

  private void updateTicketCounter() {
    if (tickets != null && !tickets.isEmpty()) {
      ticketCounterLabel.setText(String.format("Ticket %d of %d", currentTicketIndex + 1, tickets.size()));
    } else {
      ticketCounterLabel.setText("");
    }
  }

  private void updateNavigationButtons() {
    if (tickets == null || tickets.isEmpty()) {
      previousButton.setDisable(true);
      nextButton.setDisable(true);
      return;
    }

    // Disable previous button on first ticket
    previousButton.setDisable(currentTicketIndex == 0);

    // Disable next button on last ticket
    nextButton.setDisable(currentTicketIndex >= tickets.size() - 1);
  }

  @FXML
  private void handlePrevious() {
    if (currentTicketIndex > 0) {
      currentTicketIndex--;
      displayCurrentTicket();
      updateTicketCounter();
      updateNavigationButtons();
    }
  }

  @FXML
  private void handleNext() {
    if (currentTicketIndex < tickets.size() - 1) {
      currentTicketIndex++;
      displayCurrentTicket();
      updateTicketCounter();
      updateNavigationButtons();
    }
  }

  @FXML
  private void handleClose() {
    Stage stage = (Stage) bookingIdLabel.getScene().getWindow();
    stage.close();
  }
}
