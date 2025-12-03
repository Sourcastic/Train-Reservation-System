package com.example.trainreservationsystem.controllers.staff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UpdateTrainStatusController {

    @FXML
    private TableView<Schedule> trainTable;

    @FXML
    private TableColumn<Schedule, Integer> colTrainId;

    @FXML
    private TableColumn<Schedule, String> colTrainName;

    @FXML
    private TableColumn<Schedule, String> colCurrentStatus;

    @FXML
    private ComboBox<String> statusSelect;

    @FXML
    private Button btnUpdateStatus;

    private final ScheduleRepository scheduleRepository = RepositoryFactory.getScheduleRepository();
    private final BookingRepository bookingRepository = RepositoryFactory.getBookingRepository();
    private static final String DEFAULT_STATUS = "On Time";

    // Map to store status for each schedule (in-memory, since schedules table
    // doesn't have status column)
    private final Map<Integer, String> scheduleStatusMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize columns and data
        statusSelect.getItems().addAll("On Time", "Delayed", "Cancelled");
        statusSelect.setValue(DEFAULT_STATUS);

        setupTableColumns();
        loadSchedules();

        btnUpdateStatus.setOnAction(e -> updateStatus());
    }

    private void setupTableColumns() {
        colTrainId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTrainName.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            if (schedule.getRoute() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        schedule.getRoute().getSource() + " → " + schedule.getRoute().getDestination());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        // Status column - shows stored status or default
        colCurrentStatus.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            String status = scheduleStatusMap.getOrDefault(schedule.getId(), DEFAULT_STATUS);
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleRepository.getAllSchedules();
            // Initialize status map with default values for new schedules
            for (Schedule schedule : schedules) {
                scheduleStatusMap.putIfAbsent(schedule.getId(), DEFAULT_STATUS);
            }
            trainTable.setItems(FXCollections.observableArrayList(schedules));
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateStatus() {
        Schedule selectedSchedule = trainTable.getSelectionModel().getSelectedItem();
        if (selectedSchedule == null) {
            AlertUtils.showWarning("No Selection", "Please select a schedule from the table.");
            return;
        }

        String newStatus = statusSelect.getValue();
        if (newStatus == null || newStatus.isEmpty()) {
            AlertUtils.showWarning("No Status", "Please select a status.");
            return;
        }

        try {
            // Store status in memory (in a real app, you'd add a status column to schedules
            // table)
            scheduleStatusMap.put(selectedSchedule.getId(), newStatus);

            List<com.example.trainreservationsystem.models.member.Booking> bookings = bookingRepository
                    .getBookingsByScheduleId(selectedSchedule.getId());

            String routeName = selectedSchedule.getRoute() != null
                    ? selectedSchedule.getRoute().getSource() + " → " + selectedSchedule.getRoute().getDestination()
                    : "your scheduled train";

            String message = String.format(
                    "Train status update for %s on %s: %s",
                    routeName,
                    selectedSchedule.getDepartureDate(),
                    newStatus);

            // Notify all users with bookings on this schedule
            for (com.example.trainreservationsystem.models.member.Booking booking : bookings) {
                if ("CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus())) {
                    NotificationService.getInstance().add(message, booking.getUserId());
                }
            }

            AlertUtils.showSuccess("Success",
                    String.format("Status updated to '%s'. %d users have been notified.", newStatus, bookings.size()));

            // Refresh the table to update UI
            trainTable.refresh();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to update status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
