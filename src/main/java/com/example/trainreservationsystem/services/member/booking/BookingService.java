package com.example.trainreservationsystem.services.member.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.trainreservationsystem.models.admin.CancellationPolicy;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.Passenger;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.CancellationPolicyRepository;
import com.example.trainreservationsystem.repositories.admin.TrainRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;

/**
 * Service for booking operations.
 * Handles booking creation, retrieval, and status updates.
 */
public class BookingService {
  private final BookingRepository bookingRepository;
  private final TrainRepository trainRepository;
  private final CancellationPolicyRepository cancellationPolicyRepository;

  public BookingService(BookingRepository bookingRepository, TrainRepository trainRepository) {
    this.bookingRepository = bookingRepository;
    this.trainRepository = trainRepository;
    this.cancellationPolicyRepository = RepositoryFactory.getCancellationPolicyRepository();
  }

  public Booking createBooking(int userId, Schedule schedule, List<Passenger> passengers) {
    Booking booking = new Booking();
    booking.setUserId(userId);
    booking.setScheduleId(schedule.getId());
    booking.setSchedule(schedule);
    booking.setPassengers(passengers);
    booking.setStatus("PENDING");

    Booking created = bookingRepository.createBooking(booking);
    if (created != null) {
      created.setSchedule(schedule);
    }
    return created;
  }

  public List<Booking> getUserBookings(int userId) {
    List<Booking> bookings = bookingRepository.getBookingsByUserId(userId);
    for (Booking b : bookings) {
      b.setSchedule(trainRepository.getScheduleById(b.getScheduleId()));
    }
    return bookings;
  }

  /**
   * Cancels a booking with validation against cancellation policy.
   *
   * @throws IllegalArgumentException if cancellation is not allowed
   */
  public void cancelBooking(int bookingId) throws IllegalArgumentException {
    Booking booking = bookingRepository.getBookingById(bookingId);
    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }

    // Load schedule if not already loaded
    if (booking.getSchedule() == null) {
      booking.setSchedule(trainRepository.getScheduleById(booking.getScheduleId()));
    }

    // Validate cancellation
    validateCancellation(booking);

    // Cancel the booking
    bookingRepository.updateBookingStatus(bookingId, "CANCELLED");
  }

  /**
   * Validates if a booking can be cancelled based on cancellation policy.
   *
   * @throws IllegalArgumentException if cancellation is not allowed
   */
  public void validateCancellation(Booking booking) throws IllegalArgumentException {
    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }

    if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
      throw new IllegalArgumentException("Only PENDING or CONFIRMED bookings can be cancelled");
    }

    // Load schedule if needed
    if (booking.getSchedule() == null) {
      booking.setSchedule(trainRepository.getScheduleById(booking.getScheduleId()));
    }

    if (booking.getSchedule() == null) {
      throw new IllegalArgumentException("Schedule information not available");
    }

    // Calculate hours until departure
    // Use booking date as travel date fallback (in future, add travelDate to
    // Booking)
    LocalDate travelDate = booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate() : LocalDate.now();
    LocalDateTime departureDateTime = travelDate.atTime(booking.getSchedule().getDepartureTime());
    LocalDateTime now = LocalDateTime.now();

    if (departureDateTime.isBefore(now)) {
      throw new IllegalArgumentException("Cannot cancel a booking for a train that has already departed");
    }

    long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureDateTime);

    // Get cancellation policy
    CancellationPolicy policy = cancellationPolicyRepository.getActivePolicy();

    if (!policy.canCancel(hoursUntilDeparture)) {
      throw new IllegalArgumentException(
          String.format(
              "Cancellation is not allowed. You can cancel up to %d hours before departure (minimum %d hours).",
              policy.getHoursBeforeDeparture(), policy.getMinHoursBeforeDeparture()));
    }
  }

  /**
   * Calculates refund amount for a cancelled booking.
   * Refund is only given if cancelled at least X hours before departure (default
   * 24).
   * Otherwise, no refund is given.
   */
  public double calculateRefund(int bookingId) {
    Booking booking = bookingRepository.getBookingById(bookingId);
    if (booking == null) {
      return 0;
    }

    // Load schedule if needed
    if (booking.getSchedule() == null) {
      booking.setSchedule(trainRepository.getScheduleById(booking.getScheduleId()));
    }

    if (booking.getSchedule() == null) {
      return 0;
    }

    // Calculate hours until departure
    // Use booking date as travel date fallback (in future, add travelDate to
    // Booking)
    LocalDate travelDate = booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate() : LocalDate.now();
    LocalDateTime departureDateTime = travelDate.atTime(booking.getSchedule().getDepartureTime());
    LocalDateTime now = LocalDateTime.now();
    long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureDateTime);

    // Get cancellation policy to get the refund threshold
    CancellationPolicy policy = cancellationPolicyRepository.getActivePolicy();
    int refundThresholdHours = policy.getHoursBeforeDeparture();

    // Only refund if cancelled at least X hours before departure
    if (hoursUntilDeparture >= refundThresholdHours) {
      // Full refund (or use policy's refund percentage if configured)
      return booking.getTotalAmount() * (policy.getRefundPercentage() / 100.0);
    } else {
      // No refund if less than threshold hours before departure
      return 0;
    }
  }

  public void confirmBooking(int bookingId) {
    bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");
  }

  public List<Integer> getOccupiedSeats(int scheduleId) {
    return bookingRepository.getOccupiedSeats(scheduleId);
  }
}
