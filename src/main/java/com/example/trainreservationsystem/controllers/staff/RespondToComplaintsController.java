package com.example.trainreservationsystem.controllers.staff;

import com.example.trainreservationsystem.models.shared.Complaint;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.shared.ComplaintRepository;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

public class RespondToComplaintsController {

    @FXML
    private TableView<Complaint> complaintTable;

    @FXML
    private TableColumn<Complaint, String> colId;

    @FXML
    private TableColumn<Complaint, String> colCustomer;

    @FXML
    private TableColumn<Complaint, String> colMessage;

    @FXML
    private Label lblComplaintDetails;

    @FXML
    private TextArea txtResponse;

    @FXML
    private Button btnSubmitResponse;

    private final ComplaintRepository complaintRepository = RepositoryFactory.getComplaintRepository();

    @FXML
    public void initialize() {
        // Setup columns
        colId.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colCustomer.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getUserId())));
        colMessage.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));

        // Load complaints
        loadComplaints();

        // Handle table selection
        complaintTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblComplaintDetails
                        .setText("Subject: " + newSelection.getSubject() + "\n\nDescription: "
                                + newSelection.getDescription() +
                                "\n\nTracking ID: " + newSelection.getTrackingId());
            } else {
                lblComplaintDetails.setText("Select a complaint to see details.");
            }
        });

        // Handle response button
        btnSubmitResponse.setOnAction(e -> handleSendResponse());
    }

    private void loadComplaints() {
        try {
            var complaints = complaintRepository.getAllComplaints();
            complaintTable.setItems(FXCollections.observableArrayList(complaints));
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load complaints: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendResponse() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Error", "Please select a complaint first.");
            return;
        }

        String responseText = txtResponse.getText().trim();
        if (responseText.isEmpty()) {
            AlertUtils.showWarning("Error", "Response cannot be empty.");
            return;
        }

        try {
            int staffId = UserSession.getInstance().getCurrentUser().getId();
            complaintRepository.saveComplaintResponse(selected.getId(), responseText, staffId);
            AlertUtils.showSuccess("Success", "Response sent successfully! The user has been notified.");
            txtResponse.clear();
            // Optionally reload complaints
            loadComplaints();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to send response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
