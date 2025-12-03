package com.example.trainreservationsystem.models.shared;

import java.time.LocalDateTime;

public class Complaint {
  private int id;
  private int userId;
  private String subject;
  private String description;
  private String trackingId;
  private LocalDateTime createdAt;

  public Complaint() {
  }

  public Complaint(int id, int userId, String subject, String description, String trackingId, LocalDateTime createdAt) {
    this.id = id;
    this.userId = userId;
    this.subject = subject;
    this.description = description;
    this.trackingId = trackingId;
    this.createdAt = createdAt;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
