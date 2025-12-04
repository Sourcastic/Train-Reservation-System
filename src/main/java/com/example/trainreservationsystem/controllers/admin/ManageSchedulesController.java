package com.example.trainreservationsystem.controllers.admin;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.admin.SeatClass;
import com.example.trainreservationsystem.repositories.admin.RouteRepository;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.admin.SeatClassRepository;
import com.example.trainreservationsystem.repositories.shared.SeatRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ManageSchedulesController {

    @FXML
    private TableView<Schedule> schedulesTable;
    @FXML
    private TableColumn<Schedule, Integer> idColumn;
    @FXML
    private TableColumn<Schedule, String> routeColumn;
    @FXML
    private TableColumn<Schedule, String> daysColumn;
    @FXML
    private TableColumn<Schedule, String> departureColumn;
    @FXML
    private TableColumn<Schedule, String> arrivalColumn;
    @FXML
    private TableColumn<Schedule, Void> actionColumn;

    @FXML
    private VBox formContainer;
    @FXML
    private VBox daysContainer;
    @FXML
    private VBox timeContainer;
    @FXML
    private VBox seatClassesContainer;
    @FXML
    private Button addScheduleButton;
    @FXML
    private Label messageLabel;

    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final RouteRepository routeRepository = new RouteRepository();
    private final SeatClassRepository seatClassRepository = new SeatClassRepository();
    private final SeatRepository seatRepository = new SeatRepository();

    private ObservableList<Schedule> schedulesList = FXCollections.observableArrayList();
    private List<Route> allRoutes = new ArrayList<>();
    private List<SeatClass> allSeatClasses = new ArrayList<>();

    private ComboBox<Route> routeComboBox;
    private List<CheckBox> dayCheckBoxes = new ArrayList<>();
    private TextField departureTimeField;
    private TextField arrivalTimeField;
    private List<SeatClassRow> seatClassRows = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        buildForm();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        routeColumn.setCellValueFactory(cellData -> {
            Route route = cellData.getValue().getRoute();
            return new javafx.beans.property.SimpleStringProperty(
                    route.getSource() + " → " + route.getDestination());
        });

        daysColumn.setCellValueFactory(cellData -> {
            String days = cellData.getValue().getDaysOfWeek().stream()
                    .map(d -> d.name().substring(0, 3))
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(days);
        });

        departureColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))));

        arrivalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm"))));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteButton.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    handleDeleteSchedule(schedule);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void loadData() {
        try {
            allRoutes = routeRepository.getAllRoutes();
            allSeatClasses = seatClassRepository.getAllSeatClasses();
            schedulesList.clear();
            schedulesList.addAll(scheduleRepository.getAllSchedules());
            schedulesTable.setItems(schedulesList);
        } catch (Exception e) {
            showMessage("Error loading data: " + e.getMessage(), false);
        }
    }

    private void buildForm() {
        // Route Section
        VBox routeSection = (VBox) formContainer.getChildren().get(0);
        Label routeLabel = new Label("Route");
        routeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

        routeComboBox = new ComboBox<>();
        routeComboBox.setItems(FXCollections.observableArrayList(allRoutes));
        routeComboBox.setPromptText("Select Route");
        routeComboBox.setPrefWidth(400);
        routeComboBox.setConverter(new javafx.util.StringConverter<Route>() {
            @Override
            public String toString(Route route) {
                return route != null ? route.getSource() + " → " + route.getDestination() : "";
            }

            @Override
            public Route fromString(String string) {
                return null;
            }
        });

        routeSection.getChildren().addAll(routeLabel, routeComboBox);

        // Days of Week Section
        Label daysLabel = new Label("Days of Week");
        daysLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
        daysContainer.getChildren().add(daysLabel);

        HBox daysRow = new HBox(10);
        for (Schedule.DayOfWeek day : Schedule.DayOfWeek.values()) {
            CheckBox cb = new CheckBox(day.name().substring(0, 3));
            cb.setUserData(day);
            dayCheckBoxes.add(cb);
            daysRow.getChildren().add(cb);
        }
        daysContainer.getChildren().add(daysRow);

        // Time Section
        Label timeLabel = new Label("Time");
        timeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
        timeContainer.getChildren().add(timeLabel);

        HBox timeRow = new HBox(10);
        Label depLabel = new Label("Departure:");
        departureTimeField = new TextField();
        departureTimeField.setPromptText("HH:MM");
        departureTimeField.setPrefWidth(100);

        Label arrLabel = new Label("Arrival:");
        arrivalTimeField = new TextField();
        arrivalTimeField.setPromptText("HH:MM");
        arrivalTimeField.setPrefWidth(100);

        timeRow.getChildren().addAll(depLabel, departureTimeField, arrLabel, arrivalTimeField);
        timeContainer.getChildren().add(timeRow);

        // Initial seat class add button
        addSeatClassRow(true);
    }

    private void addSeatClassRow(boolean isFirst) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: #ECF0F1; -fx-background-radius: 5; -fx-padding: 10;");

        if (isFirst) {
            Button addButton = new Button("+");
            addButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
            addButton.setOnAction(e -> addSeatClassRow(false));
            row.getChildren().add(addButton);
            seatClassesContainer.getChildren().add(row);
            return;
        }

        ComboBox<SeatClass> seatClassCombo = new ComboBox<>();
        seatClassCombo.setItems(FXCollections.observableArrayList(allSeatClasses));
        seatClassCombo.setPromptText("Seat Class");
        seatClassCombo.setPrefWidth(150);
        seatClassCombo.setConverter(new javafx.util.StringConverter<SeatClass>() {
            @Override
            public String toString(SeatClass sc) {
                return sc != null ? sc.getName() : "";
            }

            @Override
            public SeatClass fromString(String string) {
                return null;
            }
        });

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefWidth(100);

        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> addSeatClassRow(false));

        Button removeButton = new Button("-");
        removeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
        removeButton.setOnAction(e -> {
            seatClassesContainer.getChildren().remove(row);
            seatClassRows.removeIf(scr -> scr.row == row);
        });

        row.getChildren().addAll(seatClassCombo, quantityField, addButton, removeButton);
        seatClassRows.add(new SeatClassRow(row, seatClassCombo, quantityField));
        seatClassesContainer.getChildren().add(row);
    }

    @FXML
    private void handleAddSchedule() {
        try {
            // Validate route
            Route selectedRoute = routeComboBox.getValue();
            if (selectedRoute == null) {
                showMessage("Please select a route", false);
                return;
            }

            // Validate days
            List<Schedule.DayOfWeek> selectedDays = dayCheckBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (Schedule.DayOfWeek) cb.getUserData())
                    .collect(Collectors.toList());

            if (selectedDays.isEmpty()) {
                showMessage("Please select at least one day", false);
                return;
            }

            // Validate times
            String depTimeStr = departureTimeField.getText().trim();
            String arrTimeStr = arrivalTimeField.getText().trim();

            if (depTimeStr.isEmpty() || arrTimeStr.isEmpty()) {
                showMessage("Please enter departure and arrival times", false);
                return;
            }

            LocalTime departureTime = LocalTime.parse(depTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime arrivalTime = LocalTime.parse(arrTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

            // Create schedule
            Schedule newSchedule = new Schedule();
            newSchedule.setRoute(selectedRoute);
            newSchedule.setDepartureTime(departureTime);
            newSchedule.setArrivalTime(arrivalTime);
            newSchedule.setPrice(selectedRoute.totalPrice());
            newSchedule.setDaysOfWeek(selectedDays);

            // Calculate total capacity from seat classes
            int totalCapacity = 0;
            for (SeatClassRow scr : seatClassRows) {
                if (scr.quantityField.getText().trim().isEmpty())
                    continue;
                totalCapacity += Integer.parseInt(scr.quantityField.getText().trim());
            }
            newSchedule.setCapacity(totalCapacity);

            // Save schedule
            Schedule savedSchedule = scheduleRepository.addSchedule(newSchedule);

            // Add seats for each seat class
            for (SeatClassRow scr : seatClassRows) {
                SeatClass sc = scr.seatClassCombo.getValue();
                String qtyStr = scr.quantityField.getText().trim();

                if (sc == null || qtyStr.isEmpty())
                    continue;

                int quantity = Integer.parseInt(qtyStr);
                for (int i = 0; i < quantity; i++) {
                    com.example.trainreservationsystem.models.shared.Seat seat = new com.example.trainreservationsystem.models.shared.Seat();
                    seat.setSeatClass(sc);
                    seat.setBooked(false);
                    seatRepository.addSeat(seat, savedSchedule.getId());
                }
            }

            showMessage("Schedule added successfully!", true);
            clearForm();
            loadData();
        } catch (Exception e) {
            showMessage("Error adding schedule: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void clearForm() {
        routeComboBox.setValue(null);
        dayCheckBoxes.forEach(cb -> cb.setSelected(false));
        departureTimeField.clear();
        arrivalTimeField.clear();
        seatClassRows.clear();
        seatClassesContainer.getChildren().clear();
        addSeatClassRow(true);
    }

    private void handleDeleteSchedule(Schedule schedule) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Schedule");
        confirmAlert.setContentText("Are you sure you want to delete this schedule?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    scheduleRepository.deleteSchedule(schedule.getId());
                    showMessage("Schedule deleted successfully!", true);
                    loadData();
                } catch (Exception e) {
                    showMessage("Error deleting schedule: " + e.getMessage(), false);
                }
            }
        });
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + ";");
    }

    private static class SeatClassRow {
        HBox row;
        ComboBox<SeatClass> seatClassCombo;
        TextField quantityField;

        SeatClassRow(HBox row, ComboBox<SeatClass> seatClassCombo, TextField quantityField) {
            this.row = row;
            this.seatClassCombo = seatClassCombo;
            this.quantityField = quantityField;
        }
    }
}
