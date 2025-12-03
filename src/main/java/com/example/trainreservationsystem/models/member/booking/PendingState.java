package com.example.trainreservationsystem.models.member.booking;

import com.example.trainreservationsystem.models.member.Booking;

/**
 * Pending state - initial state when booking is created.
 * Can transition to CONFIRMED or CANCELLED.
 */
public class PendingState implements BookingState {

  @Override
  public void confirm(Booking booking) {
    booking.setStatus("CONFIRMED");
  }

  @Override
  public void cancel(Booking booking) {
    booking.setStatus("CANCELLED");
  }

  @Override
  public String getStatus() {
    return "PENDING";
  }
}
