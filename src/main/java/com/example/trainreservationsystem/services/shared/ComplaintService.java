package com.example.trainreservationsystem.services.shared;

import java.util.UUID;

import com.example.trainreservationsystem.models.shared.Complaint;
import com.example.trainreservationsystem.repositories.shared.ComplaintRepository;

public class ComplaintService {
  private final ComplaintRepository repository;

  public ComplaintService(ComplaintRepository repository) {
    this.repository = repository;
  }

  public void submitComplaint(int userId, String subject, String description) {
    Complaint c = new Complaint();
    c.setUserId(userId);
    c.setSubject(subject);
    c.setDescription(description);
    c.setTrackingId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

    repository.saveComplaint(c);
  }
}
