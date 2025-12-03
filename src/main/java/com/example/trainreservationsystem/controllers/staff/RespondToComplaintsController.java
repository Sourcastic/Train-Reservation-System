package com.example.trainreservationsystem.controllers.staff;

import com.example.trainreservationsystem.models.shared.Complaint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RespondToComplaintsController {

    @FXML
    private TableView<Complaint> complaintTable;

    @FXML
    private TableColumn<Complaint, String> colId;

    @FXML
    private TableColumn<Complaint, String> colCustomer;

    @FXML
    private TableColumn<Complaint, String> colStatus;

    @FXML
    private TableColumn<Complaint, String> colMessage;

    @FXML
    private Label lblComplaintDetails;

    @FXML
    private TextArea txtResponse;

    @FXML
    private Button btnSubmitResponse;

    private ObservableList<Complaint> complaintsList;

    @FXML
    public void initialize() {
        // Setup columns
        colId.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colCustomer.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getUserId())));
        // colStatus.setCellValueFactory(data -> new
        // javafx.beans.property.SimpleStringProperty(data.getValue().getStatus())); //
        // Complaint model missing status
        colMessage.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));

        // Load complaints
        loadComplaints();

        // Handle table selection
        complaintTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblComplaintDetails
                        .setText("Subject: " + newSelection.getSubject() + "\n\n" + newSelection.getDescription());
            } else {
                lblComplaintDetails.setText("Select a complaint to see details.");
            }
        });

        // Handle response button
        btnSubmitResponse.setOnAction(e -> handleSendResponse());
    }

    private void loadComplaints() {
        // Placeholder data
        complaintsList = FXCollections.observableArrayList();
        // complaintsList.add(new Complaint(1, 101, "Delay", "Train was late", "TRK123",
        // java.time.LocalDateTime.now()));
        complaintTable.setItems(complaintsList);
    }

    @FXML
    private void handleSendResponse() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a complaint first.");
            return;
        }

        String responseText = txtResponse.getText();
        if (responseText.isEmpty()) {
            showAlert("Error", "Response cannot be empty.");
            return;
        }

        // Logic to send response
        System.out.println("Sending response to complaint " + selected.getId() + ": " + responseText);

        showAlert("Success", "Response sent successfully!");
        txtResponse.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
