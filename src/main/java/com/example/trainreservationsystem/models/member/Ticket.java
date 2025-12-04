package com.example.trainreservationsystem.models.member;

public class Ticket {
  private int id;
  private int bookingId;
  private int seatId;
  private String qrCode;
  private String status;

  public Ticket() {
  }

  public Ticket(int id, int bookingId, int seatId, String qrCode, String status) {
    this.id = id;
    this.bookingId = bookingId;
    this.seatId = seatId;
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

  public int getSeatId() {
    return seatId;
  }

  public void setSeatId(int seatId) {
    this.seatId = seatId;
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
