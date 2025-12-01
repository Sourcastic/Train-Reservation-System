package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.services.StaffComplaintService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RespondToComplaintsController {

    @FXML
    private TableView<Complaint> complaintsTable;

    @FXML
    private TableColumn<Complaint, String> subjectColumn;

    @FXML
    private TableColumn<Complaint, String> trackingIdColumn;

    @FXML
    private TableColumn<Complaint, String> dateColumn;

    @FXML
    private Label selectedSubjectLabel;

    @FXML
    private Label selectedDescriptionLabel;

    @FXML
    private TextArea responseArea;

    @FXML
    private Button btnSubmitResponse;

    private final StaffComplaintService staffService = com.example.trainreservationsystem.services.ServiceFactory.getStaffComplaintService();

    private ObservableList<Complaint> complaintsList;

    @FXML
    public void initialize() {
        // Setup columns
        subjectColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject()));
        trackingIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTrackingId()));
        dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""
        ));

        // Load complaints
        loadComplaints();

        // Handle table selection
        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSubjectLabel.setText(newSelection.getSubject());
                selectedDescriptionLabel.setText(newSelection.getDescription());
            }
        });

        // Handle response button
        btnSubmitResponse.setOnAction(e -> handleSendResponse());
    }

    private void loadComplaints() {
        complaintsList = FXCollections.observableArrayList(staffService.getAllComplaints());
        complaintsTable.setItems(complaintsList);
    }

    @FXML
    private void handleSendResponse() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a complaint first.");
            return;
        }

        String responseText = responseArea.getText();
        if (responseText.isEmpty()) {
            showAlert("Error", "Response cannot be empty.");
            return;
        }

        int staffId = 1; // Replace with actual logged-in staff ID
        staffService.respondToComplaint(selected.getId(), responseText, staffId);

        showAlert("Success", "Response sent successfully!");
        responseArea.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
