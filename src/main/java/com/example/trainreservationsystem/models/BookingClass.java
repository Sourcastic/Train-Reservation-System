package com.example.trainreservationsystem.models;

/**
 * Represents a booking class with seat range and pricing information.
 */
public class BookingClass {
  private String code; // SL, 3A, 2A, 1A, etc.
  private String name; // Full name
  private double priceMultiplier; // Multiplier for base price
  private int seatStart; // Starting seat number for this class
  private int seatEnd; // Ending seat number for this class
  private int availableSeats; // Number of available seats

  public BookingClass(String code, String name, double priceMultiplier, int seatStart, int seatEnd,
      int availableSeats) {
    this.code = code;
    this.name = name;
    this.priceMultiplier = priceMultiplier;
    this.seatStart = seatStart;
    this.seatEnd = seatEnd;
    this.availableSeats = availableSeats;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public double getPriceMultiplier() {
    return priceMultiplier;
  }

  public int getSeatStart() {
    return seatStart;
  }

  public int getSeatEnd() {
    return seatEnd;
  }

  public int getAvailableSeats() {
    return availableSeats;
  }

  public boolean isSeatInClass(int seatNumber) {
    return seatNumber >= seatStart && seatNumber <= seatEnd;
  }
}
