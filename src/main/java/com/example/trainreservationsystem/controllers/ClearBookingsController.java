package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;
import com.example.trainreservationsystem.services.BookingService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class ClearBookingsController {

    @FXML
    private TableView<Booking> expiredTable;

    @FXML
    private TableColumn<Booking, Integer> idColumn;

    @FXML
    private TableColumn<Booking, Integer> userColumn;

    @FXML
    private TableColumn<Booking, Integer> scheduleColumn;

    @FXML
    private TableColumn<Booking, LocalDate> journeyDateColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private Button clearButton;

    private BookingService bookingService;
    private ObservableList<Booking> expiredList;

    @FXML
    public void initialize() {
        // Initialize BookingService with required repositories
        bookingService = new BookingService(new BookingRepository(), new TrainRepository());

        // Enable multiple selection
        expiredTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Bind table columns
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        userColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getUserId()).asObject());
        scheduleColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getScheduleId()).asObject());
        journeyDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getJourneyDate().toLocalDate()));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        // Load expired bookings
        List<Booking> expiredBookings = bookingService.getExpiredBookings();
        expiredList = FXCollections.observableArrayList(expiredBookings);
        expiredTable.setItems(expiredList);

        // Button action
        clearButton.setOnAction(e -> handleClearBookings());
    }

    private void handleClearBookings() {
        ObservableList<Booking> selected = expiredTable.getSelectionModel().getSelectedItems();

        if (selected == null || selected.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No selection", "Please select one or more bookings to clear.");
            return;
        }

        // Confirm with admin
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Clear Bookings");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to clear the selected bookings?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Clear bookings in DB via service
        bookingService.clearExpiredBookings(selected);

        // Remove from table view
        expiredList.removeAll(selected);

        showAlert(Alert.AlertType.INFORMATION, "Bookings Cleared", "Selected bookings have been cleared.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
