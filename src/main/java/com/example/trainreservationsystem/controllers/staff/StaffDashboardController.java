package com.example.trainreservationsystem.controllers.staff;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.repositories.member.TicketRepository;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for staff dashboard with booking statistics and reports.
 */
public class StaffDashboardController {
  @FXML
  private Label totalBookingsLabel;
  @FXML
  private Label pendingBookingsLabel;
  @FXML
  private Label confirmedBookingsLabel;
  @FXML
  private Label cancelledBookingsLabel;
  @FXML
  private Label totalRevenueLabel;
  @FXML
  private TableView<Schedule> schedulesTable;
  @FXML
  private TableColumn<Schedule, String> routeCol;
  @FXML
  private TableColumn<Schedule, String> dateCol;
  @FXML
  private TableColumn<Schedule, String> timeCol;
  @FXML
  private TableColumn<Schedule, Integer> bookingsCol;
  @FXML
  private VBox statsContainer;
  @FXML
  private Button btnManageDiscounts;
  @FXML
  private Button btnManageCancellationPolicies;
  @FXML
  private Button btnManageUsers;
  @FXML
  private Button btnClearBookings;
  @FXML
  private Button btnGenerateReport;

  private final BookingRepository bookingRepository = RepositoryFactory.getBookingRepository();
  private final ScheduleRepository scheduleRepository = RepositoryFactory.getScheduleRepository();
  private final TicketRepository ticketRepository = RepositoryFactory.getTicketRepository();

  @FXML
  public void initialize() {
    setupTableColumns();
    loadStatistics();
    loadScheduleStatistics();
    setupRoleBasedVisibility();
  }

  private void setupRoleBasedVisibility() {
    com.example.trainreservationsystem.services.shared.UserSession session = com.example.trainreservationsystem.services.shared.UserSession
        .getInstance();

    if (session.isLoggedIn()) {
      String userType = session.getCurrentUser().getUserType();
      boolean isAdmin = "ADMIN".equalsIgnoreCase(userType);

      // Hide admin-only buttons from staff
      if (!isAdmin) {
        if (btnManageDiscounts != null) {
          btnManageDiscounts.setVisible(false);
          btnManageDiscounts.setManaged(false);
        }
        if (btnManageUsers != null) {
          btnManageUsers.setVisible(false);
          btnManageUsers.setManaged(false);
        }
        // Staff can manage cancellation policies and generate reports
        // But cannot manage discounts or users
      }
    }
  }

  private void setupTableColumns() {
    routeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getRoute() != null ? cellData.getValue().getRoute().getName() : "N/A"));
    // Date column removed - schedules no longer have departure date
    dateCol.setCellValueFactory(cellData -> {
      // Return a string property since date column expects string
      return new javafx.beans.property.SimpleStringProperty("N/A");
    });
    timeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getDepartureTime() != null ? cellData.getValue().getDepartureTime().toString() : "N/A"));
    bookingsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(
        getBookingCountForSchedule(cellData.getValue().getId())).asObject());
  }

  private void loadStatistics() {
    try {
      List<Booking> allBookings = bookingRepository.getAllBookings();

      long totalBookings = allBookings.size();
      long pendingBookings = allBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
      long confirmedBookings = allBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
      long cancelledBookings = allBookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count();

      // Calculate total revenue (sum of confirmed bookings)
      double totalRevenue = allBookings.stream()
          .filter(b -> "CONFIRMED".equals(b.getStatus()))
          .mapToDouble(Booking::getTotalAmount)
          .sum();

      totalBookingsLabel.setText(String.valueOf(totalBookings));
      pendingBookingsLabel.setText(String.valueOf(pendingBookings));
      confirmedBookingsLabel.setText(String.valueOf(confirmedBookings));
      cancelledBookingsLabel.setText(String.valueOf(cancelledBookings));
      totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load statistics: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void loadScheduleStatistics() {
    try {
      List<Schedule> schedules = scheduleRepository.getAllSchedules();
      schedulesTable.setItems(FXCollections.observableArrayList(schedules));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load schedule statistics: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private int getBookingCountForSchedule(int scheduleId) {
    try {
      List<Booking> bookings = bookingRepository.getBookingsByScheduleId(scheduleId);
      return bookings.size();
    } catch (Exception e) {
      return 0;
    }
  }

  @FXML
  public void handleRefresh() {
    loadStatistics();
    loadScheduleStatistics();
  }

  @FXML
  public void handleManageDiscounts() {
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/admin/manage-discounts-view.fxml");
  }

  @FXML
  public void handleManageCancellationPolicies() {
    HomeController.getInstance()
        .loadView("/com/example/trainreservationsystem/admin/manage-cancellation-policies-view.fxml");
  }

  @FXML
  public void handleManageUsers() {
    HomeController.getInstance()
        .loadView("/com/example/trainreservationsystem/admin/manage-users-view.fxml");
  }

  @FXML
  public void handleClearBookings() {
    HomeController.getInstance()
        .loadView("/com/example/trainreservationsystem/staff/manage-tickets-view.fxml");
  }

  @FXML
  public void handleGenerateReport() {
    try {
      // Get statistics
      List<Booking> allBookings = bookingRepository.getAllBookings();
      List<Schedule> allSchedules = scheduleRepository.getAllSchedules();
      List<com.example.trainreservationsystem.models.member.Ticket> allTickets = ticketRepository.getAllTickets();

      // Calculate statistics
      long totalBookings = allBookings.size();
      long pendingBookings = allBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
      long confirmedBookings = allBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
      long cancelledBookings = allBookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count();
      double totalRevenue = allBookings.stream()
          .filter(b -> "CONFIRMED".equals(b.getStatus()))
          .mapToDouble(Booking::getTotalAmount)
          .sum();
      long validTickets = allTickets.stream().filter(t -> "VALID".equals(t.getStatus())).count();
      long closedTickets = allTickets.stream().filter(t -> "CLOSED".equals(t.getStatus())).count();

      // Create report content
      StringBuilder report = new StringBuilder();
      report.append("=".repeat(60)).append("\n");
      report.append("TRAIN RESERVATION SYSTEM - ADMIN REPORT\n");
      report.append("Generated on: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
      report.append("=".repeat(60)).append("\n\n");

      report.append("BOOKING STATISTICS\n");
      report.append("-".repeat(60)).append("\n");
      report.append(String.format("Total Bookings: %d\n", totalBookings));
      report.append(String.format("Pending Bookings: %d\n", pendingBookings));
      report.append(String.format("Confirmed Bookings: %d\n", confirmedBookings));
      report.append(String.format("Cancelled Bookings: %d\n", cancelledBookings));
      report.append(String.format("Total Revenue: $%.2f\n", totalRevenue));
      report.append("\n");

      report.append("SCHEDULE STATISTICS\n");
      report.append("-".repeat(60)).append("\n");
      report.append(String.format("Total Schedules: %d\n", allSchedules.size()));
      report.append("\n");

      report.append("TICKET STATISTICS\n");
      report.append("-".repeat(60)).append("\n");
      report.append(String.format("Total Tickets: %d\n", allTickets.size()));
      report.append(String.format("Valid Tickets: %d\n", validTickets));
      report.append(String.format("Closed Tickets: %d\n", closedTickets));
      report.append("\n");

      report.append("BOOKING DETAILS\n");
      report.append("-".repeat(60)).append("\n");
      report.append(
          String.format("%-10s %-10s %-15s %-15s %-10s\n", "Booking ID", "User ID", "Status", "Amount", "Date"));
      report.append("-".repeat(60)).append("\n");
      for (Booking booking : allBookings) {
        String dateStr = booking.getBookingDate() != null
            ? booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            : "N/A";
        report.append(String.format("%-10d %-10d %-15s $%-14.2f %-10s\n",
            booking.getId(), booking.getUserId(), booking.getStatus(),
            booking.getTotalAmount(), dateStr));
      }
      report.append("\n");

      report.append("=".repeat(60)).append("\n");
      report.append("End of Report\n");
      report.append("=".repeat(60)).append("\n");

      // Save to file
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save Report");
      fileChooser.setInitialFileName("train_reservation_report_" +
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
      fileChooser.getExtensionFilters().add(
          new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));

      Stage stage = (Stage) totalBookingsLabel.getScene().getWindow();
      java.io.File file = fileChooser.showSaveDialog(stage);

      if (file != null) {
        try (FileWriter writer = new FileWriter(file)) {
          writer.write(report.toString());
          AlertUtils.showSuccess("Success", "Report generated successfully!\nSaved to: " + file.getAbsolutePath());
        } catch (IOException e) {
          AlertUtils.showError("Error", "Failed to save report: " + e.getMessage());
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to generate report: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  public void handleBack() {
    HomeController.getInstance().showSearch();
  }
}
