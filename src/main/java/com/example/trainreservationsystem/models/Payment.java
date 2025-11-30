package com.example.trainreservationsystem.models;

import java.time.LocalDateTime;

public class Payment {
  private int id;
  private int bookingId;
  private double amount;
  private int paymentMethodId;
  private String status;
  private LocalDateTime paymentDate;

  public Payment() {
  }

  public Payment(int id, int bookingId, double amount, int paymentMethodId, String status, LocalDateTime paymentDate) {
    this.id = id;
    this.bookingId = bookingId;
    this.amount = amount;
    this.paymentMethodId = paymentMethodId;
    this.status = status;
    this.paymentDate = paymentDate;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBookingId() {
    return bookingId;
  }

  public void setBookingId(int bookingId) {
    this.bookingId = bookingId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public int getPaymentMethodId() {
    return paymentMethodId;
  }

  public void setPaymentMethodId(int paymentMethodId) {
    this.paymentMethodId = paymentMethodId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDateTime paymentDate) {
    this.paymentDate = paymentDate;
  }
}
