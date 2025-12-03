package com.example.trainreservationsystem.models.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a train schedule (a specific run of a route).
 */
public class Schedule {

  public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  }

  private int id;
  private Route route;
  private LocalDate departureDate;
  private LocalTime departureTime;
  private LocalTime arrivalTime;
  private double price;
  private int capacity;
  private List<DayOfWeek> daysOfWeek = new ArrayList<>();
  private List<Seat> seats = new ArrayList<>();
  private List<Booking> bookings = new ArrayList<>();

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

  // Getters & setters
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

  public List<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }

  public List<Seat> getSeats() {
    return seats;
  }

  public void setSeats(List<Seat> seats) {
    this.seats = seats;
  }

  public List<Booking> getBookings() {
    return bookings;
  }

  public void setBookings(List<Booking> bookings) {
    this.bookings = bookings;
  }

  /**
   * Add a seat to the schedule.
   */
  public void addSeat(Seat seat) {
    this.seats.add(seat);
  }

  /**
   * Add a booking to this schedule.
   */
  public void addBooking(Booking booking) {
    this.bookings.add(booking);
  }

  /**
   * Clear all bookings.
   */
  public void clearBookings() {
    this.bookings.clear();
  }

  /**
   * Count free seats per SeatClass.
   */
  public Map<Integer, Long> freeSeatsByClass() {
    return seats.stream()
        .filter(seat -> !seat.isBooked())
        .collect(Collectors.groupingBy(seat -> seat.getSeatClass().getId(), Collectors.counting()));
  }

  /**
   * Calculate total price for a given seat (route price + class base fare).
   */
  public double calculateSeatPrice(Seat seat) {
    double routePrice = route.totalPrice();
    return routePrice + seat.getSeatClass().getBaseFare();
  }

  /**
   * Modify departure and arrival times.
   */
  public void modifyTimes(LocalTime newDeparture, LocalTime newArrival) {
    this.departureTime = newDeparture;
    this.arrivalTime = newArrival;
  }

  /**
   * Generate a simple statistics snapshot for this schedule.
   */
  public Statistics generateStatistics(String dayOfWeek) {
    Statistics stats = new Statistics();
    stats.setScheduleId(this.id);
    stats.setDayOfWeek(dayOfWeek);
    stats.setDepartureTime(this.departureTime);
    // For each seat class, record seats sold
    Map<Integer, Long> soldPerClass = bookings.stream()
        .collect(Collectors.groupingBy(b -> b.getSeat().getSeatClass().getId(), Collectors.counting()));
    stats.setSeatsSoldPerClass(soldPerClass);
    return stats;
  }

  /**
   * Returns a string representation of the schedule showing the route name.
   */
  @Override
  public String toString() {
    if (route != null && route.getName() != null) {
      return route.getName();
    } else if (route != null) {
      return route.getSource() + " → " + route.getDestination();
    } else {
      return "Schedule #" + id;
    }
  }
}
