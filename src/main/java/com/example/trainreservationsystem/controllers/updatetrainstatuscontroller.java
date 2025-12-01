package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.TrainService;
import com.example.trainreservationsystem.repositories.TrainRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class updatetrainstatuscontroller {

    @FXML
    private TableView<Schedule> scheduleTable;

    @FXML
    private TableColumn<Schedule, Integer> idColumn;

    @FXML
    private TableColumn<Schedule, String> routeColumn;

    @FXML
    private TableColumn<Schedule, String> departureColumn;

    @FXML
    private TableColumn<Schedule, String> arrivalColumn;

    @FXML
    private TableColumn<Schedule, String> statusColumn;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button confirmButton;

    private TrainService trainService;

    private ObservableList<Schedule> scheduleList;

    @FXML
    public void initialize() {
        // Initialize service with repository
        trainService = new TrainService(new TrainRepository());

        // Initialize ComboBox items
        statusComboBox.setItems(FXCollections.observableArrayList("On-time", "Delayed", "Cancelled"));

        // Configure table columns
        idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());

        routeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getRoute().getSource() + " → " + cell.getValue().getRoute().getDestination()));

        departureColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDepartureTime().toString()));

        arrivalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getArrivalTime().toString()));

        statusColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getStatus() == null ? "On-time" : cell.getValue().getStatus()));

        // Load schedules (from DB later)
        List<Schedule> schedules = trainService.searchSchedules("Any", "Any", java.time.LocalDate.now());
        scheduleList = FXCollections.observableArrayList(schedules);
        scheduleTable.setItems(scheduleList);

        // Configure confirm button
        confirmButton.setOnAction(e -> handleUpdateStatus());
    }

    @FXML
    private void handleUpdateStatus() {
        Schedule selected = scheduleTable.getSelectionModel().getSelectedItem();
        String newStatus = statusComboBox.getValue();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No train selected", "Please select a train to update.");
            return;
        }
        if (newStatus == null || newStatus.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No status selected", "Please select a new status.");
            return;
        }

        // TODO: Update in DB
        trainService.updateScheduleStatusAndNotify(selected.getId(), newStatus);

        // Update the TableView
        selected.setStatus(newStatus);
        scheduleTable.refresh();

        showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                "Train " + selected.getRoute().getSource() + " → " + selected.getRoute().getDestination()
                        + " status updated to " + newStatus + ".");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
