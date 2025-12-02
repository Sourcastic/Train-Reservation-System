package com.example.trainreservationsystem.controllers.booking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.trainreservationsystem.controllers.HomeController;
import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.services.booking.BookingService;
import com.example.trainreservationsystem.utils.booking.BookingHelper;
import com.example.trainreservationsystem.utils.booking.SeatGridHelper;
import com.example.trainreservationsystem.utils.ui.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Controller for seat selection and booking creation.
 * Handles: schedule display, seat selection, passenger info, booking creation.
 */
public class BookingController {
  // UI Components
  @FXML
  private Label routeLabel, dateLabel, timeLabel, priceLabel;
  @FXML
  private TextField nameField;
  @FXML
  private Spinner<Integer> ageSpinner;
  @FXML
  private GridPane seatGrid;
  @FXML
  private Label selectedSeatsLabel;
  @FXML
  private Button proceedButton;

  // Services & Data
  private final BookingService bookingService = ServiceFactory.getBookingService();
  private Schedule schedule;
  private Set<Integer> selectedSeats = new HashSet<>();
  private Set<Integer> occupiedSeats = new HashSet<>();
  private static final int TOTAL_SEATS = 60;

  @FXML
  public void initialize() {
    schedule = UserSession.getInstance().getSelectedSchedule();
    if (schedule == null)
      return;

    displayScheduleInfo();
    initializeAgeSpinner();
    loadOccupiedSeats();
    createSeatGrid();
    setupValidation();
  }

  private void displayScheduleInfo() {
    routeLabel.setText(schedule.getRoute().getSource() + " → " + schedule.getRoute().getDestination());
    dateLabel.setText("Date: " + schedule.getDepartureDate());
    timeLabel.setText("Time: " + schedule.getDepartureTime());
    priceLabel.setText("$" + String.format("%.2f", schedule.getPrice()) + " per seat");
  }

  private void initializeAgeSpinner() {
    SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 25);
    ageSpinner.setValueFactory(factory);
  }

  private void loadOccupiedSeats() {
    List<Integer> occupied = bookingService.getOccupiedSeats(schedule.getId());
    occupiedSeats.addAll(occupied);
  }

  private void createSeatGrid() {
    SeatGridHelper.createGrid(seatGrid, TOTAL_SEATS, occupiedSeats, this::toggleSeat);
  }

  private void toggleSeat(int seatNumber, Button button) {
    if (selectedSeats.contains(seatNumber)) {
      deselectSeat(seatNumber, button);
    } else {
      selectSeat(seatNumber, button);
    }
    updateDisplay();
  }

  private void selectSeat(int seatNumber, Button button) {
    selectedSeats.add(seatNumber);
    button.getStyleClass().remove("seat-available");
    button.getStyleClass().add("seat-selected");
  }

  private void deselectSeat(int seatNumber, Button button) {
    selectedSeats.remove(seatNumber);
    button.getStyleClass().remove("seat-selected");
    button.getStyleClass().add("seat-available");
  }

  private void updateDisplay() {
    updateSelectedSeatsDisplay();
    validateForm();
  }

  private void updateSelectedSeatsDisplay() {
    if (selectedSeats.isEmpty()) {
      selectedSeatsLabel.setText("No seats selected");
      return;
    }
    String seatsText = formatSeatNumbers();
    selectedSeatsLabel.setText("Selected seats: " + seatsText);
    updateTotalPrice();
  }

  private String formatSeatNumbers() {
    return selectedSeats.stream()
        .sorted()
        .map(String::valueOf)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  private void updateTotalPrice() {
    double total = schedule.getPrice() * selectedSeats.size();
    priceLabel.setText("Total: $" + String.format("%.2f", total));
  }

  private void setupValidation() {
    nameField.textProperty().addListener((obs, old, val) -> validateForm());
  }

  private void validateForm() {
    boolean isValid = !nameField.getText().trim().isEmpty() && !selectedSeats.isEmpty();
    proceedButton.setDisable(!isValid);
  }

  @FXML
  public void handleBack() {
    HomeController.getInstance().showSearch();
  }

  @FXML
  public void handleProceedToPayment() {
    if (!validateBeforeProceeding())
      return;

    try {
      Booking booking = createBookingWithPassengers();
      if (booking != null) {
        saveBookingAndRedirect(booking);
      } else {
        AlertUtils.showError("Error", "Failed to create booking");
      }
    } catch (Exception e) {
      AlertUtils.showError("Error", e.getMessage());
    }
  }

  private boolean validateBeforeProceeding() {
    if (!UserSession.getInstance().isLoggedIn()) {
      AlertUtils.showError("Error", "Please login first");
      return false;
    }
    if (nameField.getText().trim().isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Please enter passenger name");
      return false;
    }
    if (selectedSeats.isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Please select at least one seat");
      return false;
    }
    return true;
  }

  private Booking createBookingWithPassengers() {
    String name = nameField.getText().trim();
    int age = ageSpinner.getValue();
    List<Passenger> passengers = BookingHelper.createPassengers(name, age, new ArrayList<>(selectedSeats));
    return BookingHelper.createBooking(bookingService, schedule, passengers);
  }

  private void saveBookingAndRedirect(Booking booking) {
    BookingHelper.saveBooking(booking, schedule, selectedSeats.size());
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/payment/payment-view.fxml");
  }
}
