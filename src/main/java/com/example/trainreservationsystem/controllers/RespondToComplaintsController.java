package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.repositories.StaffComplaintRepository;
import com.example.trainreservationsystem.services.StaffComplaintService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Controller for respondtocomplaints-view.fxml
 */
public class RespondToComplaintsController {

    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, Integer> idColumn;
    @FXML private TableColumn<Complaint, String> subjectColumn;
    @FXML private TableColumn<Complaint, String> trackingColumn;
    @FXML private TableColumn<Complaint, String> createdAtColumn;
    @FXML private Button refreshButton;
    @FXML private Button respondButton;

    private StaffComplaintService staffComplaintService;
    private ObservableList<Complaint> tableData;

    public RespondToComplaintsController() {
        // keep simple constructor
        StaffComplaintRepository repo = new StaffComplaintRepository();
        this.staffComplaintService = new StaffComplaintService(repo);
    }

    @FXML
    private void initialize() {
        // defensive: ensure fx:ids are wired
        if (complaintsTable == null) {
            System.err.println("ERROR: complaintsTable is null — check fx:id in FXML and controller package path");
            return;
        }

        // Set up basic columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        trackingColumn.setCellValueFactory(new PropertyValueFactory<>("trackingId"));

        // createdAt: format LocalDateTime -> string safely
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        createdAtColumn.setCellValueFactory(cellData -> {
            Complaint c = cellData.getValue();
            if (c == null || c.getCreatedAt() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(c.getCreatedAt().format(fmt));
        });

        loadComplaints();

        respondButton.setOnAction(e -> onRespondClicked());
        refreshButton.setOnAction(e -> loadComplaints());
    }

    private void loadComplaints() {
        Complaint[] complaints = staffComplaintService.getAllComplaints();
        tableData = FXCollections.observableArrayList(Arrays.asList(complaints));
        complaintsTable.setItems(tableData);
    }

    private void onRespondClicked() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Please select a complaint to respond to.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Respond to Complaint");
        dialog.setHeaderText("Complaint: " + selected.getSubject());
        ButtonType sendBtn = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendBtn, ButtonType.CANCEL);

        TextArea area = new TextArea();
        area.setPromptText("Type response here...");
        area.setWrapText(true);
        area.setPrefRowCount(6);
        dialog.getDialogPane().setContent(area);

        dialog.setResultConverter(bt -> bt == sendBtn ? area.getText() : null);

        dialog.showAndWait().ifPresent(responseText -> {
            try {
                staffComplaintService.respondToComplaint(selected.getId(), responseText, "Staff");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Response saved for complaint ID " + selected.getId());
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid input", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not save response: " + ex.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
