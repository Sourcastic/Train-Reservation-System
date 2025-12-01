package com.example.trainreservationsystem.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule {
  private int id;
  private Route route;
  private LocalDate departureDate;
  private LocalTime departureTime;
  private LocalTime arrivalTime;
  private double price;
  private int capacity;
  private String status; // on-time, delayed, cancelled

  public Schedule() {
  }

  public Schedule(int id, Route route, LocalDate departureDate, LocalTime departureTime, LocalTime arrivalTime,
      double price, int capacity) {
    this.id = id;
    this.route = route;
    this.departureDate = departureDate;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.price = price;
    this.capacity = capacity;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public LocalDate getDepartureDate() {
    return departureDate;
  }

  public void setDepartureDate(LocalDate departureDate) {
    this.departureDate = departureDate;
  }

  public LocalTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalTime departureTime) {
    this.departureTime = departureTime;
  }

  public LocalTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalTime arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  @Override
  public String toString() {
    return route.getSource() + " to " + route.getDestination() + " (" + departureTime + ")";
  }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }
}
