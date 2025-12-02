package com.example.trainreservationsystem.services.booking;

import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;

/**
 * Service for booking operations.
 * Handles booking creation, retrieval, and status updates.
 */
public class BookingService {
  private final BookingRepository bookingRepository;
  private final TrainRepository trainRepository;

  public BookingService(BookingRepository bookingRepository, TrainRepository trainRepository) {
    this.bookingRepository = bookingRepository;
    this.trainRepository = trainRepository;
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

  public void cancelBooking(int bookingId) {
    bookingRepository.updateBookingStatus(bookingId, "CANCELLED");
  }

  public void confirmBooking(int bookingId) {
    bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");
  }

  public List<Integer> getOccupiedSeats(int scheduleId) {
    return bookingRepository.getOccupiedSeats(scheduleId);
  }
}
