package com.example.trainreservationsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UpdateTrainStatusController {

    @FXML
    private TableView<?> trainTable;

    @FXML
    private TableColumn<?, ?> colTrainId;

    @FXML
    private TableColumn<?, ?> colTrainName;

    @FXML
    private TableColumn<?, ?> colCurrentStatus;

    @FXML
    private ComboBox<String> statusSelect;

    @FXML
    private Button btnUpdateStatus;

    @FXML
    public void initialize() {
        // Initialize columns and data
        statusSelect.getItems().addAll("On Time", "Delayed", "Cancelled");
    }

    @FXML
    private void updateStatus() {
        System.out.println("Update Status clicked");
        // Implement status update logic
    }
}
