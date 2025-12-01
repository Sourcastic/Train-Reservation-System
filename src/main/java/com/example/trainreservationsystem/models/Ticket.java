package com.example.trainreservationsystem.models;

public class Ticket {
  private int id;
  private int bookingId;
  private String qrCode;
  private String status;

  public Ticket() {
  }

  public Ticket(int id, int bookingId, String qrCode, String status) {
    this.id = id;
    this.bookingId = bookingId;
    this.qrCode = qrCode;
    this.status = status;
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

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
