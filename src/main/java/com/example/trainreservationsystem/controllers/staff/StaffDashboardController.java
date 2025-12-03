package com.example.trainreservationsystem.controllers.staff;

import java.time.LocalDate;
import java.util.List;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

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
  private TableColumn<Schedule, LocalDate> dateCol;
  @FXML
  private TableColumn<Schedule, String> timeCol;
  @FXML
  private TableColumn<Schedule, Integer> bookingsCol;
  @FXML
  private VBox statsContainer;

  private final BookingRepository bookingRepository = RepositoryFactory.getBookingRepository();
  private final ScheduleRepository scheduleRepository = RepositoryFactory.getScheduleRepository();

  @FXML
  public void initialize() {
    setupTableColumns();
    loadStatistics();
    loadScheduleStatistics();
  }

  private void setupTableColumns() {
    routeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getRoute() != null ? cellData.getValue().getRoute().getName() : "N/A"));
    dateCol.setCellValueFactory(new PropertyValueFactory<>("departureDate"));
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
  public void handleBack() {
    HomeController.getInstance().showSearch();
  }
}
