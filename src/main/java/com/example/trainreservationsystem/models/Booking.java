package com.example.trainreservationsystem.models;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {
  private int id;
  private int userId;
  private int scheduleId; // Replaces trainId
  private String status;
  private LocalDateTime bookingDate;

  private List<Passenger> passengers;
  private Schedule schedule;
  private double totalAmount;

  public Booking() {
  }

  public Booking(int id, int userId, int scheduleId, String status, LocalDateTime bookingDate) {
    this.id = id;
    this.userId = userId;
    this.scheduleId = scheduleId;
    this.status = status;
    this.bookingDate = bookingDate;
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

  public int getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(int scheduleId) {
    this.scheduleId = scheduleId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDateTime bookingDate) {
    this.bookingDate = bookingDate;
  }

  public List<Passenger> getPassengers() {
    return passengers;
  }

  public void setPassengers(List<Passenger> passengers) {
    this.passengers = passengers;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

    private Seat seat;

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(double totalAmount) {
    this.totalAmount = totalAmount;
  }
}
