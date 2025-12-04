package com.example.trainreservationsystem.controllers.staff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.member.TicketRepository;
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
 * Controller for ticket validation.
 * Allows staff to view tickets and close them.
 */
public class TicketValidationController {
  @FXML
  private TableView<Ticket> ticketsTable;
  @FXML
  private TableColumn<Ticket, Integer> colTicketId;
  @FXML
  private TableColumn<Ticket, Integer> colBookingId;
  @FXML
  private TableColumn<Ticket, String> colQrCode;
  @FXML
  private TableColumn<Ticket, String> colStatus;
  @FXML
  private TableColumn<Ticket, String> colAmount;
  @FXML
  private TableColumn<Ticket, Void> colActions;

  private final TicketRepository ticketRepository = RepositoryFactory.getTicketRepository();
  private Map<Integer, Double> ticketAmounts = new HashMap<>();

  @FXML
  public void initialize() {
    setupTableColumns();
    loadTickets();
  }

  private void setupTableColumns() {
    colTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
    colQrCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

    // Amount column - get from booking via ticketAmounts map
    colAmount.setCellValueFactory(cellData -> {
      Ticket ticket = cellData.getValue();
      if (ticket != null && ticketAmounts.containsKey(ticket.getId())) {
        double amount = ticketAmounts.get(ticket.getId());
        return new javafx.beans.property.SimpleStringProperty("PKR " + String.format("%.2f", amount));
      }
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Actions column with close button
    colActions.setCellFactory(new Callback<TableColumn<Ticket, Void>, TableCell<Ticket, Void>>() {
      @Override
      public TableCell<Ticket, Void> call(TableColumn<Ticket, Void> param) {
        return new TableCell<Ticket, Void>() {
          private final Button closeButton = new Button("Close Ticket");

          {
            closeButton.setStyle(
                "-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 5;");
            closeButton.setDisable(true); // Disable by default
            closeButton.setOnAction(event -> {
              Ticket ticket = getTableView().getItems().get(getIndex());
              if (ticket != null) {
                handleCloseTicket(ticket);
              }
            });
          }

          @Override
          protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              Ticket ticket = getTableView().getItems().get(getIndex());
              // Only enable close button if ticket is VALID
              if (ticket != null && "VALID".equals(ticket.getStatus())) {
                closeButton.setDisable(false);
                setGraphic(closeButton);
              } else {
                closeButton.setDisable(true);
                setGraphic(closeButton);
              }
            }
          }
        };
      }
    });
  }

  private void loadTickets() {
    try {
      List<Ticket> tickets = ticketRepository.getAllTickets();
      // Load ticket amounts for display
      ticketAmounts = ticketRepository.getTicketAmounts();
      ticketsTable.setItems(FXCollections.observableArrayList(tickets));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load tickets: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handleCloseTicket(Ticket ticket) {
    if (ticket == null) {
      return;
    }

    if (!"VALID".equals(ticket.getStatus())) {
      AlertUtils.showWarning("Invalid Action", "Only VALID tickets can be closed.");
      return;
    }

    String message = String.format(
        "Are you sure you want to close ticket #%d?\n\n" +
            "Booking ID: %d\n" +
            "QR Code: %s\n\n" +
            "This action cannot be undone.",
        ticket.getId(),
        ticket.getBookingId(),
        ticket.getQrCode());

    if (AlertUtils.showConfirmation("Close Ticket", message)) {
      try {
        boolean updated = ticketRepository.updateTicketStatus(ticket.getId(), "CLOSED");
        if (updated) {
          AlertUtils.showSuccess("Success", "Ticket #" + ticket.getId() + " has been closed.");
          loadTickets(); // Refresh the table
        } else {
          AlertUtils.showError("Error", "Failed to close ticket.");
        }
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to close ticket: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @FXML
  public void handleRefresh() {
    loadTickets();
  }

  @FXML
  public void handleBack() {
    // Navigate back to staff view
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
          getClass().getResource("/com/example/trainreservationsystem/staff/staff-view.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.scene.Scene scene = ticketsTable.getScene();
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
