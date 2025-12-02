package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.services.ComplaintService;
import com.example.trainreservationsystem.services.NotificationService;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.utils.ui.AlertUtils;

import javafx.fxml.FXML;
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
      AlertUtils.showError("Error", "You must be logged in.");
      return;
    }

    String subject = subjectField.getText().trim();
    String desc = descriptionArea.getText().trim();

    if (subject.isEmpty() || desc.isEmpty()) {
      AlertUtils.showError("Error", "Fields cannot be empty.");
      return;
    }

    complaintService.submitComplaint(UserSession.getInstance().getCurrentUser().getId(), subject, desc);
    NotificationService.getInstance().add("Complaint submitted: " + subject);
    AlertUtils.showSuccess("Success", "Complaint submitted successfully.");
    subjectField.clear();
    descriptionArea.clear();
  }
}
