package com.example.trainreservationsystem.services;

import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final TrainRepository trainRepository;

    public BookingService(BookingRepository bookingRepository, TrainRepository trainRepository) {
        this.bookingRepository = bookingRepository;
        this.trainRepository = trainRepository;
    }

    public Booking createBooking(int userId, Schedule schedule, List<Passenger> passengers) {
        // Simplified booking creation
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setScheduleId(schedule.getId());
        booking.setPassengers(passengers);
        booking.setStatus("PENDING");

        return bookingRepository.createBooking(booking);
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> bookings = bookingRepository.getBookingsByUserId(userId);
        // Hydrate schedule info
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

    // Clear all expired bookings (journey date past or invalid)
    public void clearExpiredBookings() {
        List<Booking> expired = bookingRepository.getExpiredBookings();
        clearExpiredBookings(expired);
    }

    // Clear selected bookings (used by controller)
    public void clearExpiredBookings(List<Booking> bookings) {
        for (Booking b : bookings) {
            bookingRepository.deleteBooking(b.getId());
            // Optional: send notification
            // notificationService.sendNotification(b.getUserId(), "Your booking has been cleared.");
        }
    }

    // Fetch expired bookings for UI table
    public List<Booking> getExpiredBookings() {
        return bookingRepository.getExpiredBookings();
    }
}
