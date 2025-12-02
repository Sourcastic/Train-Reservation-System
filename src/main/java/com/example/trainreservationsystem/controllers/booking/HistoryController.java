package com.example.trainreservationsystem.controllers.booking;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.services.NotificationService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.services.booking.BookingService;
import com.example.trainreservationsystem.utils.ui.AlertUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for viewing booking history.
 * Shows all user bookings with actions (cancel, view ticket).
 */
public class HistoryController {
  @FXML
  private TableView<Booking> historyTable;
  @FXML
  private TableColumn<Booking, String> trainCol, dateCol, statusCol, actionsCol;
  @FXML
  private VBox emptyStateBox;

  private final BookingService bookingService = ServiceFactory.getBookingService();

  @FXML
  public void initialize() {
    setupTableColumns();
    if (UserSession.getInstance().isLoggedIn()) {
      refreshBookings();
    }
    setupEmptyState();
  }

  private void setupTableColumns() {
    trainCol.setCellValueFactory(cellData -> new SimpleStringProperty(
        cellData.getValue().getSchedule() != null ? cellData.getValue().getSchedule().toString() : "Unknown"));
    dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    actionsCol.setCellValueFactory(cellData -> new SimpleStringProperty(""));
    actionsCol.setCellFactory(column -> createActionCell());
  }

  private javafx.scene.control.TableCell<Booking, String> createActionCell() {
    return new javafx.scene.control.TableCell<>() {
      private final Button cancelBtn = createCancelButton();
      private final Button ticketBtn = createTicketButton();

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          Booking booking = getTableView().getItems().get(getIndex());
          HBox hbox = new HBox(10);
          addButtonsForBooking(booking, hbox);
          setGraphic(hbox);
        }
      }

      private void addButtonsForBooking(Booking booking, HBox hbox) {
        if (canCancel(booking)) {
          cancelBtn.setOnAction(e -> handleCancel(booking.getId()));
          hbox.getChildren().add(cancelBtn);
        }
        if (canViewTicket(booking)) {
          ticketBtn.setOnAction(e -> handleViewTicket(booking.getId()));
          hbox.getChildren().add(ticketBtn);
        }
      }
    };
  }

  private Button createCancelButton() {
    Button btn = new Button("Cancel");
    btn.getStyleClass().add("button");
    btn.setStyle("-fx-background-color: #d20f39; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12;");
    return btn;
  }

  private Button createTicketButton() {
    Button btn = new Button("View Ticket");
    btn.getStyleClass().add("button");
    btn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");
    return btn;
  }

  private boolean canCancel(Booking booking) {
    return "PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus());
  }

  private boolean canViewTicket(Booking booking) {
    return "CONFIRMED".equals(booking.getStatus());
  }

  private void handleCancel(int bookingId) {
    if (AlertUtils.showConfirmation("Cancel Booking", "Are you sure you want to cancel this booking?")) {
      bookingService.cancelBooking(bookingId);
      NotificationService.getInstance().add("Booking #" + bookingId + " cancelled.");
      AlertUtils.showSuccess("Success", "Booking cancelled successfully.");
      refreshBookings();
    }
  }

  private void handleViewTicket(int bookingId) {
    Booking booking = findBooking(bookingId);
    if (booking == null) {
      AlertUtils.showError("Error", "Booking not found");
      return;
    }
    openTicketWindow(booking);
  }

  private Booking findBooking(int bookingId) {
    // Use simple loop instead of stream - easier to understand, same O(n)
    // complexity
    for (Booking booking : historyTable.getItems()) {
      if (booking.getId() == bookingId) {
        return booking;
      }
    }
    return null;
  }

  private void openTicketWindow(Booking booking) {
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/booking/ticket-view.fxml"));
      Parent root = loader.load();
      TicketViewController controller = loader.getController();
      controller.setBooking(booking);

      Stage stage = new Stage();
      stage.setTitle("Your E-Ticket");
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setScene(new Scene(root));
      stage.setResizable(false);
      stage.showAndWait();
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to display ticket: " + e.getMessage());
    }
  }

  private void refreshBookings() {
    int userId = UserSession.getInstance().getCurrentUser().getId();
    var bookings = bookingService.getUserBookings(userId);
    historyTable.setItems(FXCollections.observableArrayList(bookings));
  }

  private void setupEmptyState() {
    if (historyTable != null && emptyStateBox != null) {
      emptyStateBox.visibleProperty().bind(Bindings.isEmpty(historyTable.getItems()));
      emptyStateBox.managedProperty().bind(emptyStateBox.visibleProperty());
    }
  }
}
