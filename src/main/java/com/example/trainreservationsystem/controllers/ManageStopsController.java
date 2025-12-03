package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Stop;
import com.example.trainreservationsystem.repositories.StopRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ManageStopsController {

    @FXML
    private TableView<Stop> stopsTable;
    @FXML
    private TableColumn<Stop, Integer> idColumn;
    @FXML
    private TableColumn<Stop, String> nameColumn;
    @FXML
    private TableColumn<Stop, Void> actionColumn;

    @FXML
    private TextField stopNameField;
    @FXML
    private Button addStopButton;
    @FXML
    private Label messageLabel;

    private final StopRepository stopRepository = new StopRepository();
    private ObservableList<Stop> stopsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Set up action column with delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteButton.setOnAction(event -> {
                    Stop stop = getTableView().getItems().get(getIndex());
                    handleDeleteStop(stop);
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
        });

        loadStops();
    }

    private void loadStops() {
        try {
            stopsList.clear();
            stopsList.addAll(stopRepository.getAllStops());
            stopsTable.setItems(stopsList);
        } catch (Exception e) {
            showMessage("Error loading stops: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleAddStop() {
        String name = stopNameField.getText().trim();

        if (name.isEmpty()) {
            showMessage("Please enter a stop name", false);
            return;
        }

        try {
            Stop newStop = new Stop();
            newStop.setName(name);
            stopRepository.addStop(newStop);

            showMessage("Stop added successfully!", true);
            stopNameField.clear();
            loadStops();
        } catch (Exception e) {
            showMessage("Error adding stop: " + e.getMessage(), false);
        }
    }

    private void handleDeleteStop(Stop stop) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Stop");
        confirmAlert.setContentText("Are you sure you want to delete stop: " + stop.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    stopRepository.deleteStop(stop.getId());
                    showMessage("Stop deleted successfully!", true);
                    loadStops();
                } catch (Exception e) {
                    showMessage("Error deleting stop: " + e.getMessage(), false);
                }
            }
        });
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + ";");
    }
}
