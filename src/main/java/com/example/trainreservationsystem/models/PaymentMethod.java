package com.example.trainreservationsystem.models;

public class PaymentMethod {
  private int id;
  private int userId;
  private String methodType;
  private String details;

  public PaymentMethod() {
  }

  public PaymentMethod(int id, int userId, String methodType, String details) {
    this.id = id;
    this.userId = userId;
    this.methodType = methodType;
    this.details = details;
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

  public String getMethodType() {
    return methodType;
  }

  public void setMethodType(String methodType) {
    this.methodType = methodType;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  @Override
  public String toString() {
    return methodType + " - " + details;
  }
}
