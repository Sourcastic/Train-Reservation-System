package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.SeatClass;
import com.example.trainreservationsystem.repositories.SeatClassRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ManageSeatClassesController {

    @FXML
    private TableView<SeatClass> seatClassesTable;
    @FXML
    private TableColumn<SeatClass, Integer> idColumn;
    @FXML
    private TableColumn<SeatClass, String> nameColumn;
    @FXML
    private TableColumn<SeatClass, Double> baseFareColumn;
    @FXML
    private TableColumn<SeatClass, String> descriptionColumn;
    @FXML
    private TableColumn<SeatClass, Void> actionColumn;

    @FXML
    private TextField classNameField;
    @FXML
    private TextField baseFareField;
    @FXML
    private TextField descriptionField;
    @FXML
    private Button addSeatClassButton;
    @FXML
    private Label messageLabel;

    private final SeatClassRepository seatClassRepository = new SeatClassRepository();
    private ObservableList<SeatClass> seatClassesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        baseFareColumn.setCellValueFactory(new PropertyValueFactory<>("baseFare"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set up action column with delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteButton.setOnAction(event -> {
                    SeatClass seatClass = getTableView().getItems().get(getIndex());
                    handleDeleteSeatClass(seatClass);
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

        loadSeatClasses();
    }

    private void loadSeatClasses() {
        try {
            seatClassesList.clear();
            seatClassesList.addAll(seatClassRepository.getAllSeatClasses());
            seatClassesTable.setItems(seatClassesList);
        } catch (Exception e) {
            showMessage("Error loading seat classes: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleAddSeatClass() {
        String name = classNameField.getText().trim();
        String baseFareStr = baseFareField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty() || baseFareStr.isEmpty() || description.isEmpty()) {
            showMessage("Please fill in all fields", false);
            return;
        }

        try {
            double baseFare = Double.parseDouble(baseFareStr);

            SeatClass newSeatClass = new SeatClass();
            newSeatClass.setName(name);
            newSeatClass.setBaseFare(baseFare);
            newSeatClass.setDescription(description);

            seatClassRepository.addSeatClass(newSeatClass);

            showMessage("Seat class added successfully!", true);
            classNameField.clear();
            baseFareField.clear();
            descriptionField.clear();
            loadSeatClasses();
        } catch (NumberFormatException e) {
            showMessage("Invalid base fare. Please enter a valid number.", false);
        } catch (Exception e) {
            showMessage("Error adding seat class: " + e.getMessage(), false);
        }
    }

    private void handleDeleteSeatClass(SeatClass seatClass) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Seat Class");
        confirmAlert.setContentText("Are you sure you want to delete seat class: " + seatClass.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    seatClassRepository.deleteSeatClass(seatClass.getId());
                    showMessage("Seat class deleted successfully!", true);
                    loadSeatClasses();
                } catch (Exception e) {
                    showMessage("Error deleting seat class: " + e.getMessage(), false);
                }
            }
        });
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + ";");
    }
}
