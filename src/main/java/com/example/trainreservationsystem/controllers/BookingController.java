package com.example.trainreservationsystem.controllers;

import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.BookingService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BookingController {
  @FXML
  private Label trainDetailsLabel;
  @FXML
  private TextField seatField;
  @FXML
  private TextField nameField;
  @FXML
  private TextField ageField;

  private final BookingService bookingService = ServiceFactory.getBookingService();
  private Schedule schedule;

  @FXML
  public void initialize() {
    schedule = UserSession.getInstance().getSelectedSchedule();
    if (schedule != null) {
      trainDetailsLabel.setText(schedule.toString() + " - Price: $" + schedule.getPrice());
    }
  }

  @FXML
  public void handleConfirm() {
    if (!UserSession.getInstance().isLoggedIn()) {
      showAlert("Error", "Please login first");
      return;
    }

    try {
      // Simplified for single passenger for now based on UI
      int seat = Integer.parseInt(seatField.getText());
      int age = Integer.parseInt(ageField.getText());
      String name = nameField.getText();

      // Seat number validation (stored in passenger's seat assignment in real DB)
      if (seat < 1) {
        showAlert("Error", "Invalid seat number.");
        return;
      }

      Passenger p = new Passenger(name, age, false, false);
      List<Passenger> passengers = new ArrayList<>();
      passengers.add(p);

      Booking booking = bookingService.createBooking(
          UserSession.getInstance().getCurrentUser().getId(),
          schedule,
          passengers);

      if (booking != null) {
        // Store booking for payment flow
        UserSession.getInstance().setPendingBooking(booking);
        // Redirect to payment screen
        HomeController.getInstance().loadView("/com/example/trainreservationsystem/payment-view.fxml");
      } else {
        showAlert("Error", "Booking failed.");
      }
    } catch (NumberFormatException e) {
      showAlert("Error", "Invalid input for seat or age.");
    } catch (Exception e) {
      e.printStackTrace();
      showAlert("Error", e.getMessage());
    }
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
