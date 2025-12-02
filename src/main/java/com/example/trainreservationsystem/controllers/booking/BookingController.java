package com.example.trainreservationsystem.controllers.booking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.trainreservationsystem.controllers.HomeController;
import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.BookingClass;
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
  private Set<Integer> highlightedSeats = new HashSet<>(); // Nearby available seats
  private BookingClass selectedBookingClass;
  private static final int TOTAL_SEATS = 60;

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
      initializeSelectedClass();
      displayScheduleInfo();
      initializeAgeSpinner();
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
    dateLabel
        .setText("Date: " + (schedule.getDepartureDate() != null ? schedule.getDepartureDate().toString() : "N/A"));
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

  private void initializeAgeSpinner() {
    SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 25);
    ageSpinner.setValueFactory(factory);
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

  private void initializeSelectedClass() {
    String classCode = UserSession.getInstance().getSelectedClass();
    if (classCode != null) {
      // Create booking class based on code
      selectedBookingClass = createBookingClassFromCode(classCode);
    }
  }

  private BookingClass createBookingClassFromCode(String classCode) {
    // Match class code to seat ranges
    switch (classCode) {
      case "SL":
        return new BookingClass("SL", "Sleeper Class", 0.6, 1, 20, 0);
      case "3A":
        return new BookingClass("3A", "AC 3 Tier", 1.0, 21, 40, 0);
      case "2A":
        return new BookingClass("2A", "AC 2 Tier", 1.5, 41, 60, 0);
      case "1A":
        return new BookingClass("1A", "AC First Class", 2.0, 61, 80, 0);
      default:
        return null;
    }
  }

  private void createSeatGrid() {
    if (schedule == null) {
      return;
    }

    // If a class is selected, filter seats to that class range
    int totalSeats = TOTAL_SEATS;
    if (selectedBookingClass != null) {
      totalSeats = selectedBookingClass.getSeatEnd();
    }

    SeatGridHelper.createGrid(seatGrid, totalSeats, occupiedSeats, this::toggleSeat);

    // Highlight nearby available seats if a seat is preselected
    if (!selectedSeats.isEmpty()) {
      highlightNearbySeats();
    }
  }

  private void preselectSeatIfAvailable() {
    Integer preselectedSeat = UserSession.getInstance().getPreselectedSeat();
    if (preselectedSeat != null && !occupiedSeats.contains(preselectedSeat)) {
      // Find the button for this seat and select it
      selectSeatProgrammatically(preselectedSeat);
      highlightNearbySeats();
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

  private void highlightNearbySeats() {
    highlightedSeats.clear();

    if (selectedSeats.isEmpty() || selectedBookingClass == null) {
      return;
    }

    // Highlight available seats within 2 seats of any selected seat
    for (Integer selectedSeat : selectedSeats) {
      for (int offset = -2; offset <= 2; offset++) {
        int nearbySeat = selectedSeat + offset;

        // Check if seat is in the same class and available
        if (nearbySeat >= selectedBookingClass.getSeatStart() &&
            nearbySeat <= selectedBookingClass.getSeatEnd() &&
            nearbySeat != selectedSeat &&
            !occupiedSeats.contains(nearbySeat) &&
            !selectedSeats.contains(nearbySeat)) {
          highlightedSeats.add(nearbySeat);
        }
      }
    }

    // Update button styles for highlighted seats - use enhanced for loop instead of
    // forEach
    for (javafx.scene.Node node : seatGrid.getChildren()) {
      if (node instanceof javafx.scene.control.Button) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
        try {
          int seatNum = Integer.parseInt(btn.getText());
          if (highlightedSeats.contains(seatNum)) {
            btn.getStyleClass().remove("seat-available");
            btn.getStyleClass().add("seat-highlighted");
          } else if (!selectedSeats.contains(seatNum) && !occupiedSeats.contains(seatNum)) {
            btn.getStyleClass().remove("seat-highlighted");
            btn.getStyleClass().add("seat-available");
          }
        } catch (NumberFormatException e) {
          // Not a seat button - ignore
        }
      }
    }
  }

  private void toggleSeat(int seatNumber, Button button) {
    // Prevent selecting occupied seats
    if (occupiedSeats.contains(seatNumber)) {
      return;
    }

    // If a class is selected, only allow seats in that class
    if (selectedBookingClass != null && !selectedBookingClass.isSeatInClass(seatNumber)) {
      AlertUtils.showWarning("Invalid Seat",
          "Please select a seat from " + selectedBookingClass.getCode() + " class (seats " +
              selectedBookingClass.getSeatStart() + "-" + selectedBookingClass.getSeatEnd() + ")");
      return;
    }

    if (selectedSeats.contains(seatNumber)) {
      deselectSeat(seatNumber, button);
    } else {
      selectSeat(seatNumber, button);
    }
    updateDisplay();
    highlightNearbySeats();
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
    if (highlightedSeats.contains(seatNumber)) {
      button.getStyleClass().remove("seat-selected");
      button.getStyleClass().add("seat-highlighted");
    } else {
      button.getStyleClass().remove("seat-selected");
      button.getStyleClass().add("seat-available");
    }
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
    nameField.textProperty().addListener((obs, old, val) -> {
      validateForm();
      validateNameField();
    });
  }

  private void validateNameField() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      nameField.setStyle("-fx-border-color: rgba(203,166,164,0.3);");
      return;
    }

    // Check if name contains only letters, spaces, hyphens, and apostrophes
    if (!name.matches("^[a-zA-Z\\s\\-'']+$")) {
      nameField.setStyle("-fx-border-color: #d32f2f;");
    } else if (name.length() < 2) {
      nameField.setStyle("-fx-border-color: #f57c00;");
    } else if (name.length() > 50) {
      nameField.setStyle("-fx-border-color: #f57c00;");
    } else {
      nameField.setStyle("-fx-border-color: #2e7d32;");
    }
  }

  private boolean isValidName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return false;
    }
    String trimmed = name.trim();
    // Name should be 2-50 characters, contain only letters, spaces, hyphens, and
    // apostrophes
    return trimmed.length() >= 2 &&
        trimmed.length() <= 50 &&
        trimmed.matches("^[a-zA-Z\\s\\-'']+$");
  }

  private void validateForm() {
    boolean isValid = isValidName(nameField.getText()) && !selectedSeats.isEmpty();
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

    // Validate passenger name
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      AlertUtils.showWarning("Missing Information",
          "Please enter the passenger's name to continue.");
      nameField.requestFocus();
      return false;
    }

    if (!isValidName(name)) {
      if (name.length() < 2) {
        AlertUtils.showWarning("Invalid Name",
            "Passenger name must be at least 2 characters long.");
      } else if (name.length() > 50) {
        AlertUtils.showWarning("Invalid Name",
            "Passenger name cannot exceed 50 characters.");
      } else {
        AlertUtils.showWarning("Invalid Name",
            "Passenger name can only contain letters, spaces, hyphens, and apostrophes. Numbers and special characters are not allowed.");
      }
      nameField.requestFocus();
      return false;
    }

    // Validate seat selection
    if (selectedSeats.isEmpty()) {
      AlertUtils.showWarning("No Seats Selected",
          "Please select at least one seat before proceeding to payment.");
      return false;
    }

    // Reload occupied seats to ensure we have the latest data
    loadOccupiedSeats();

    // Check if any selected seat is now occupied
    List<Integer> unavailableSeats = new ArrayList<>();
    for (Integer seatNumber : selectedSeats) {
      if (occupiedSeats.contains(seatNumber)) {
        unavailableSeats.add(seatNumber);
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

    String name = nameField.getText().trim();
    if (!isValidName(name)) {
      throw new IllegalArgumentException("Invalid passenger name");
    }

    int age = ageSpinner.getValue();
    if (age < 1 || age > 120) {
      throw new IllegalArgumentException("Passenger age must be between 1 and 120 years");
    }

    if (selectedSeats.isEmpty()) {
      throw new IllegalStateException("No seats selected");
    }

    List<Passenger> passengers = BookingHelper.createPassengers(name, age, new ArrayList<>(selectedSeats));
    return BookingHelper.createBooking(bookingService, schedule, passengers);
  }

  private void saveBookingAndRedirect(Booking booking) {
    BookingHelper.saveBooking(booking, schedule, selectedSeats.size());
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/payment/payment-view.fxml");
  }
}
