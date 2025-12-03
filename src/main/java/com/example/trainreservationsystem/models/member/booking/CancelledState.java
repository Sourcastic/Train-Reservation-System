package com.example.trainreservationsystem.models.member.booking;

import com.example.trainreservationsystem.models.member.Booking;

/**
 * Cancelled state - booking has been cancelled.
 * Terminal state - no further transitions allowed.
 */
public class CancelledState implements BookingState {

  @Override
  public void confirm(Booking booking) {
    throw new IllegalStateException("Cannot confirm a cancelled booking");
  }

  @Override
  public void cancel(Booking booking) {
    throw new IllegalStateException("Booking is already cancelled");
  }

  @Override
  public String getStatus() {
    return "CANCELLED";
  }
}
