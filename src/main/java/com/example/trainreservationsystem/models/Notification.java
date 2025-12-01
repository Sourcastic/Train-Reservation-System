package com.example.trainreservationsystem.models;

public class Notification {
  private int id;
  private int userId;
  private String message;
  private boolean sent;

  public Notification() {
  }

  public Notification(int id, int userId, String message, boolean sent) {
    this.id = id;
    this.userId = userId;
    this.message = message;
    this.sent = sent;
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isSent() {
    return sent;
  }

  public void setSent(boolean sent) {
    this.sent = sent;
  }
}
