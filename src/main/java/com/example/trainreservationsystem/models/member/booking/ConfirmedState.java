package com.example.trainreservationsystem.models.member.booking;

import com.example.trainreservationsystem.models.member.Booking;

/**
 * Confirmed state - booking has been paid and confirmed.
 * Can only transition to CANCELLED (with refund logic).
 */
public class ConfirmedState implements BookingState {

  @Override
  public void confirm(Booking booking) {
    throw new IllegalStateException("Booking is already confirmed");
  }

  @Override
  public void cancel(Booking booking) {
    // Business logic: Cancellation allowed but may have refund policy
    booking.setStatus("CANCELLED");
  }

  @Override
  public String getStatus() {
    return "CONFIRMED";
  }
}
