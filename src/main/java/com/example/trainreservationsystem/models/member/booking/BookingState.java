package com.example.trainreservationsystem.models.member.booking;

import com.example.trainreservationsystem.models.member.Booking;

/**
 * State interface for Booking status transitions.
 * Implements State pattern to manage booking lifecycle.
 */
public interface BookingState {
  /**
   * Confirms the booking.
   *
   * @param booking The booking to confirm
   * @throws IllegalStateException if transition is not allowed
   */
  void confirm(Booking booking);

  /**
   * Cancels the booking.
   *
   * @param booking The booking to cancel
   * @throws IllegalStateException if transition is not allowed
   */
  void cancel(Booking booking);

  /**
   * Gets the status string for this state.
   *
   * @return Status string
   */
  String getStatus();
}
