package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Ticket;
import com.example.trainreservationsystem.services.BookingService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.TicketService;
import com.example.trainreservationsystem.services.UserSession;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class HistoryController {
  @FXML
  private TableView<Booking> historyTable;
  @FXML
  private TableColumn<Booking, String> trainCol;
  @FXML
  private TableColumn<Booking, String> dateCol;
  @FXML
  private TableColumn<Booking, String> statusCol;
  @FXML
  private TableColumn<Booking, String> actionsCol;

  private final BookingService bookingService = ServiceFactory.getBookingService();
  private final TicketService ticketService = ServiceFactory.getTicketService();

  @FXML
  public void initialize() {
    if (UserSession.getInstance().isLoggedIn()) {
      refreshBookings();
    }

    trainCol.setCellValueFactory(cellData -> new SimpleStringProperty(
        cellData.getValue().getSchedule() != null ? cellData.getValue().getSchedule().toString() : "Unknown"));
    dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

    // Actions column with buttons
    actionsCol.setCellValueFactory(cellData -> new SimpleStringProperty(""));
    actionsCol.setCellFactory(column -> new javafx.scene.control.TableCell<Booking, String>() {
      private final Button cancelBtn = new Button("Cancel");
      private final Button ticketBtn = new Button("View Ticket");

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          Booking booking = getTableView().getItems().get(getIndex());
          javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);

          // Cancel button - only for PENDING or CONFIRMED bookings
          if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
            cancelBtn.setOnAction(e -> handleCancel(booking.getId()));
            hbox.getChildren().add(cancelBtn);
          }

          // Ticket button - only for CONFIRMED bookings
          if ("CONFIRMED".equals(booking.getStatus())) {
            ticketBtn.setOnAction(e -> handleViewTicket(booking.getId()));
            hbox.getChildren().add(ticketBtn);
          }

          setGraphic(hbox);
        }
      }
    });
  }

  private void handleCancel(int bookingId) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Cancel Booking");
    confirm.setContentText("Are you sure you want to cancel this booking?");
    confirm.showAndWait().ifPresent(response -> {
      if (response == javafx.scene.control.ButtonType.OK) {
        bookingService.cancelBooking(bookingId);
        showAlert("Success", "Booking cancelled successfully.");
        refreshBookings();
      }
    });
  }

  private void handleViewTicket(int bookingId) {
    Ticket ticket = ticketService.getTicketByBookingId(bookingId);
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("E-Ticket");
    alert.setHeaderText("Your Ticket");
    alert.setContentText(
        "Booking ID: " + bookingId + "\nQR Code: " + ticket.getQrCode() + "\nStatus: " + ticket.getStatus());
    alert.showAndWait();
  }

  private void refreshBookings() {
    var bookings = bookingService.getUserBookings(UserSession.getInstance().getCurrentUser().getId());
    historyTable.setItems(FXCollections.observableArrayList(bookings));
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
