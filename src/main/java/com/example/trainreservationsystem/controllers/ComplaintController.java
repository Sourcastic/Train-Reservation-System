package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.services.ComplaintService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ComplaintController {
  @FXML
  private TextField subjectField;
  @FXML
  private TextArea descriptionArea;

  private final ComplaintService complaintService = ServiceFactory.getComplaintService();

  @FXML
  public void handleSubmit() {
    if (!UserSession.getInstance().isLoggedIn()) {
      showAlert("Error", "You must be logged in.");
      return;
    }

    String subject = subjectField.getText();
    String desc = descriptionArea.getText();

    if (subject.isEmpty() || desc.isEmpty()) {
      showAlert("Error", "Fields cannot be empty.");
      return;
    }

    complaintService.submitComplaint(UserSession.getInstance().getCurrentUser().getId(), subject, desc);
    showAlert("Success", "Complaint submitted successfully.");
    subjectField.clear();
    descriptionArea.clear();
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
