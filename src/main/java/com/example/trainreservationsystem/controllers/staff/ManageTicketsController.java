package com.example.trainreservationsystem.controllers.staff;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.TrainRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * Controller for managing tickets (active bookings).
 * Allows staff/admin to view and cancel any active booking.
 */
public class ManageTicketsController {
  @FXML
  private TableView<Booking> bookingsTable;
  @FXML
  private TableColumn<Booking, Integer> colBookingId;
  @FXML
  private TableColumn<Booking, Integer> colUserId;
  @FXML
  private TableColumn<Booking, String> colRoute;
  @FXML
  private TableColumn<Booking, String> colDate;
  @FXML
  private TableColumn<Booking, String> colTime;
  @FXML
  private TableColumn<Booking, String> colStatus;
  @FXML
  private TableColumn<Booking, String> colAmount;
  @FXML
  private TableColumn<Booking, String> colBookingDate;
  @FXML
  private TableColumn<Booking, Void> colActions;

  private final BookingRepository bookingRepository = RepositoryFactory.getBookingRepository();
  private final TrainRepository trainRepository = RepositoryFactory.getTrainRepository();
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @FXML
  public void initialize() {
    setupTableColumns();
    loadBookings();
  }

  private void setupTableColumns() {
    colBookingId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    colAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        String.format("$%.2f", cellData.getValue().getTotalAmount())));

    // Route column
    colRoute.setCellValueFactory(cellData -> {
      Booking booking = cellData.getValue();
      if (booking.getSchedule() != null && booking.getSchedule().getRoute() != null) {
        String route = booking.getSchedule().getRoute().getSource() + " → " +
            booking.getSchedule().getRoute().getDestination();
        return new javafx.beans.property.SimpleStringProperty(route);
      }
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Date column - use booking date as fallback
    colDate.setCellValueFactory(cellData -> {
      Booking booking = cellData.getValue();
      if (booking.getBookingDate() != null) {
        return new javafx.beans.property.SimpleStringProperty(
            booking.getBookingDate().toLocalDate().format(DATE_FORMATTER));
      }
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Time column
    colTime.setCellValueFactory(cellData -> {
      Booking booking = cellData.getValue();
      if (booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null) {
        return new javafx.beans.property.SimpleStringProperty(
            booking.getSchedule().getDepartureTime().format(TIME_FORMATTER));
      }
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Booking date column
    colBookingDate.setCellValueFactory(cellData -> {
      Booking booking = cellData.getValue();
      if (booking.getBookingDate() != null) {
        return new javafx.beans.property.SimpleStringProperty(
            booking.getBookingDate().format(DATETIME_FORMATTER));
      }
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Actions column with delete button
    colActions.setCellFactory(new Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>>() {
      @Override
      public TableCell<Booking, Void> call(TableColumn<Booking, Void> param) {
        return new TableCell<Booking, Void>() {
          private final Button deleteButton = new Button("Delete");

          {
            deleteButton.setStyle(
                "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 5;");
            deleteButton.setOnAction(event -> {
              Booking booking = getTableView().getItems().get(getIndex());
              if (booking != null) {
                handleDeleteBooking(booking);
              }
            });
          }

          @Override
          protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              setGraphic(deleteButton);
            }
          }
        };
      }
    });
  }

  private void loadBookings() {
    try {
      List<Booking> activeBookings = bookingRepository.getActiveBookings();

      // Load schedule information for each booking
      for (Booking booking : activeBookings) {
        if (booking.getSchedule() == null) {
          booking.setSchedule(trainRepository.getScheduleById(booking.getScheduleId()));
        }
      }

      bookingsTable.setItems(FXCollections.observableArrayList(activeBookings));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load bookings: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handleDeleteBooking(Booking booking) {
    if (booking == null) {
      return;
    }

    String message = String.format(
        "Are you sure you want to cancel booking #%d?\n\n" +
            "User ID: %d\n" +
            "Route: %s\n" +
            "Status: %s\n\n" +
            "The user will be notified of this cancellation.",
        booking.getId(),
        booking.getUserId(),
        booking.getSchedule() != null && booking.getSchedule().getRoute() != null
            ? booking.getSchedule().getRoute().getSource() + " → " + booking.getSchedule().getRoute().getDestination()
            : "N/A",
        booking.getStatus());

    if (AlertUtils.showConfirmation("Cancel Booking", message)) {
      try {
        // Cancel the booking
        bookingRepository.updateBookingStatus(booking.getId(), "CANCELLED");

        // Send notification to user
        NotificationService.getInstance().add(
            "Staff cancelled your booking #" + booking.getId(),
            booking.getUserId());

        AlertUtils.showSuccess("Success",
            "Booking #" + booking.getId() + " has been cancelled. User has been notified.");

        // Refresh the table
        loadBookings();
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to cancel booking: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @FXML
  public void handleRefresh() {
    loadBookings();
  }

  @FXML
  public void handleBack() {
    // Navigate back to staff view
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/staff/staff-view.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.scene.Scene scene = bookingsTable.getScene();
      if (scene != null) {
        javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
      }
    } catch (Exception e) {
      // Fallback to HomeController navigation
      HomeController.getInstance().showStaffDashboard();
    }
  }
}
