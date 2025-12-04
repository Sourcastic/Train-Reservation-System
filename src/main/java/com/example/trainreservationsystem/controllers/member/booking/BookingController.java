package com.example.trainreservationsystem.controllers.member.booking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.Passenger;
import com.example.trainreservationsystem.services.member.booking.BookingService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.member.booking.BookingHelper;
import com.example.trainreservationsystem.utils.member.booking.SeatGridHelper;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

  @FXML
  public void initialize() {
    schedule = UserSession.getInstance().getSelectedSchedule();
    if (schedule == null) {
      AlertUtils.showError("No Schedule Selected",
          "No train schedule was selected. Please search for a train and try again.");
      handleBack();
      return;
    }

    try {
      displayScheduleInfo();
      loadOccupiedSeats();
      createSeatGrid();
      preselectSeatIfAvailable();
      setupValidation();
    } catch (Exception e) {
      AlertUtils.showError("Initialization Error",
          "An error occurred while loading the booking page: " + e.getMessage());
      handleBack();
    }
  }

  private void displayScheduleInfo() {
    if (schedule == null || schedule.getRoute() == null) {
      routeLabel.setText("N/A");
      dateLabel.setText("Date: N/A");
      timeLabel.setText("Time: N/A");
      priceLabel.setText("PKR 0.00 per seat");
      return;
    }

    routeLabel.setText(schedule.getRoute().getSource() + " → " + schedule.getRoute().getDestination());
    // Date is now determined by user's search date, not schedule
    LocalDate travelDate = UserSession.getInstance().getSelectedTravelDate();
    if (travelDate != null) {
      dateLabel.setText("Date: " + travelDate.toString());
    } else {
      dateLabel.setText("Date: Not Selected");
    }
    timeLabel
        .setText("Time: " + (schedule.getDepartureTime() != null ? schedule.getDepartureTime().toString() : "N/A"));

    // Use selected class price if available
    double pricePerSeat = schedule.getPrice();
    String selectedClass = UserSession.getInstance().getSelectedClass();
    if (selectedClass != null) {
      double multiplier = UserSession.getInstance().getSelectedClassPriceMultiplier();
      pricePerSeat = schedule.getPrice() * multiplier;
    }

    priceLabel.setText("PKR " + String.format("%.2f", pricePerSeat) + " per seat");
  }

  private void loadOccupiedSeats() {
    if (schedule == null) {
      return;
    }
    occupiedSeats.clear(); // Clear before adding to avoid duplicates
    List<Integer> occupied = bookingService.getOccupiedSeats(schedule.getId());
    if (occupied != null) {
      occupiedSeats.addAll(occupied);
    }
  }

  private void createSeatGrid() {
    if (schedule == null) {
      return;
    }

    // Get seat range from selected class, or use all seats as fallback
    Integer seatStart = UserSession.getInstance().getSelectedClassSeatStart();
    Integer seatEnd = UserSession.getInstance().getSelectedClassSeatEnd();

    if (seatStart != null && seatEnd != null) {
      // Show only seats for the selected class
      SeatGridHelper.createGrid(seatGrid, seatStart, seatEnd, occupiedSeats, this::toggleSeat);
    } else {
      // Fallback: use schedule capacity from database if no class range is set
      int totalSeats = schedule.getCapacity() > 0 ? schedule.getCapacity() : 60;
      SeatGridHelper.createGrid(seatGrid, totalSeats, occupiedSeats, this::toggleSeat);
    }
  }

  private void preselectSeatIfAvailable() {
    Integer preselectedSeat = UserSession.getInstance().getPreselectedSeat();
    if (preselectedSeat != null && !occupiedSeats.contains(preselectedSeat)) {
      // Validate preselected seat is within class range
      Integer seatStart = UserSession.getInstance().getSelectedClassSeatStart();
      Integer seatEnd = UserSession.getInstance().getSelectedClassSeatEnd();
      if (seatStart != null && seatEnd != null) {
        if (preselectedSeat >= seatStart && preselectedSeat <= seatEnd) {
          // Find the button for this seat and select it
          selectSeatProgrammatically(preselectedSeat);
        }
      } else {
        // No class range restriction, allow any seat
        selectSeatProgrammatically(preselectedSeat);
      }
    }
  }

  private void selectSeatProgrammatically(int seatNumber) {
    if (occupiedSeats.contains(seatNumber)) {
      return;
    }

    selectedSeats.add(seatNumber);
    updateDisplay();

    // Find and update the button - use enhanced for loop instead of forEach
    String seatNumberStr = String.valueOf(seatNumber);
    for (javafx.scene.Node node : seatGrid.getChildren()) {
      if (node instanceof javafx.scene.control.Button) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
        if (btn.getText().equals(seatNumberStr)) {
          btn.getStyleClass().remove("seat-available");
          btn.getStyleClass().add("seat-selected");
        }
      }
    }
  }

  private void toggleSeat(int seatNumber, Button button) {
    // Prevent selecting occupied seats and show warning
    if (occupiedSeats.contains(seatNumber)) {
      AlertUtils.showWarning("Seat Unavailable",
          "Seat " + seatNumber + " is already booked. Please select a different seat.");
      return;
    }

    // Validate seat is within selected class range
    Integer seatStart = UserSession.getInstance().getSelectedClassSeatStart();
    Integer seatEnd = UserSession.getInstance().getSelectedClassSeatEnd();
    if (seatStart != null && seatEnd != null) {
      if (seatNumber < seatStart || seatNumber > seatEnd) {
        AlertUtils.showWarning("Invalid Seat",
            "Seat " + seatNumber
                + " is not available for the selected class. Please select a seat within the class range.");
        return;
      }
    }

    if (selectedSeats.contains(seatNumber)) {
      deselectSeat(seatNumber, button);
    } else {
      selectSeat(seatNumber, button);
    }
    updateDisplay();
  }

  private void selectSeat(int seatNumber, Button button) {
    // Double-check that seat is not occupied
    if (occupiedSeats.contains(seatNumber)) {
      return;
    }

    selectedSeats.add(seatNumber);
    button.getStyleClass().remove("seat-available");
    button.getStyleClass().add("seat-selected");
  }

  private void deselectSeat(int seatNumber, Button button) {
    // Don't allow deselecting if seat is occupied (shouldn't happen, but safety
    // check)
    if (occupiedSeats.contains(seatNumber)) {
      return;
    }

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
    // Handle empty case
    if (selectedSeats.isEmpty()) {
      return "";
    }

    // Convert Set to List and sort - clearer than stream operations
    List<Integer> sortedSeats = new ArrayList<>(selectedSeats);
    Collections.sort(sortedSeats);

    // Build comma-separated string using StringBuilder for efficiency
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < sortedSeats.size(); i++) {
      if (i > 0) {
        result.append(", ");
      }
      result.append(sortedSeats.get(i));
    }

    return result.toString();
  }

  private void updateTotalPrice() {
    if (schedule == null) {
      priceLabel.setText("Total: PKR 0.00");
      return;
    }

    // Use selected class price if available
    double pricePerSeat = schedule.getPrice();
    String selectedClass = UserSession.getInstance().getSelectedClass();
    if (selectedClass != null) {
      double multiplier = UserSession.getInstance().getSelectedClassPriceMultiplier();
      pricePerSeat = schedule.getPrice() * multiplier;
    }

    double total = pricePerSeat * selectedSeats.size();
    priceLabel.setText("Total: PKR " + String.format("%.2f", total));
  }

  private void setupValidation() {
    // Validation only checks if seats are selected - user info is automatically
    // used
    validateForm();
  }

  private void validateForm() {
    boolean isValid = !selectedSeats.isEmpty() && UserSession.getInstance().isLoggedIn();
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

    // Double-check schedule is still valid
    if (schedule == null) {
      AlertUtils.showError("Invalid Schedule",
          "The selected train schedule is no longer available. Please search for a new train.");
      handleBack();
      return;
    }

    try {
      Booking booking = createBookingWithPassengers();
      if (booking != null) {
        saveBookingAndRedirect(booking);
      } else {
        AlertUtils.showError("Booking Failed",
            "Unable to create your booking. Please try again or contact support if the problem persists.");
      }
    } catch (IllegalArgumentException e) {
      AlertUtils.showError("Invalid Input", e.getMessage());
    } catch (Exception e) {
      AlertUtils.showError("Booking Error",
          "An unexpected error occurred while processing your booking: " + e.getMessage() +
              "\n\nPlease try again or contact support if the problem persists.");
      e.printStackTrace();
    }
  }

  private boolean validateBeforeProceeding() {
    // Check if schedule is still valid
    if (schedule == null) {
      AlertUtils.showError("Invalid Schedule",
          "The selected train schedule is no longer available. Please search for a new train.");
      handleBack();
      return false;
    }

    // Check login status
    if (!UserSession.getInstance().isLoggedIn()) {
      AlertUtils.showError("Login Required",
          "You must be logged in to proceed with booking. Please login and try again.");
      return false;
    }

    // User info is automatically used - no validation needed

    // Validate seat selection
    if (selectedSeats.isEmpty()) {
      AlertUtils.showWarning("No Seats Selected",
          "Please select at least one seat before proceeding to payment.");
      return false;
    }

    // Reload occupied seats to ensure we have the latest data
    loadOccupiedSeats();

    // Validate selected seats are within class range and not occupied
    List<Integer> unavailableSeats = new ArrayList<>();
    Integer seatStart = UserSession.getInstance().getSelectedClassSeatStart();
    Integer seatEnd = UserSession.getInstance().getSelectedClassSeatEnd();

    for (Integer seatNumber : selectedSeats) {
      // Check if seat is occupied
      if (occupiedSeats.contains(seatNumber)) {
        unavailableSeats.add(seatNumber);
      }
      // Check if seat is within class range (if range is set)
      else if (seatStart != null && seatEnd != null) {
        if (seatNumber < seatStart || seatNumber > seatEnd) {
          unavailableSeats.add(seatNumber);
        }
      }
    }

    if (!unavailableSeats.isEmpty()) {
      // Build comma-separated string using StringBuilder - clearer than stream
      // operations
      StringBuilder seatsListBuilder = new StringBuilder();
      for (int i = 0; i < unavailableSeats.size(); i++) {
        if (i > 0) {
          seatsListBuilder.append(", ");
        }
        seatsListBuilder.append(unavailableSeats.get(i));
      }
      String seatsList = seatsListBuilder.toString();

      // Use if-else instead of ternary for clarity
      String message;
      if (unavailableSeats.size() == 1) {
        message = "Seat " + seatsList + " is no longer available. Please select a different seat.";
      } else {
        message = "Seats " + seatsList + " are no longer available. Please select different seats.";
      }

      AlertUtils.showError("Seat Unavailable", message);

      // Refresh the seat grid
      seatGrid.getChildren().clear();
      createSeatGrid();
      selectedSeats.clear();
      updateDisplay();
      return false;
    }

    return true;
  }

  private Booking createBookingWithPassengers() {
    if (schedule == null) {
      throw new IllegalStateException("Cannot create booking: schedule is null");
    }

    if (selectedSeats.isEmpty()) {
      throw new IllegalStateException("No seats selected");
    }

    // Use logged-in user's information
    com.example.trainreservationsystem.models.shared.User currentUser = UserSession.getInstance().getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("User not logged in");
    }

    String passengerName = currentUser.getUsername();
    int passengerAge = 25; // Default age - can be updated if user profile has age field

    List<Passenger> passengers = BookingHelper.createPassengers(passengerName, passengerAge,
        new ArrayList<>(selectedSeats));
    return BookingHelper.createBooking(bookingService, schedule, passengers);
  }

  private void saveBookingAndRedirect(Booking booking) {
    BookingHelper.saveBooking(booking, schedule, selectedSeats.size());
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/member/payment/payment-view.fxml");
  }
}
