package com.example.trainreservationsystem.repositories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Booking;

public class BookingRepository {

    private List<Booking> mockDb = new ArrayList<>();
    private int currentId = 1;

    public Booking createBooking(Booking booking) {
        booking.setId(currentId++);
        booking.setBookingDate(LocalDateTime.now());
        mockDb.add(booking);
        return booking;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking b : mockDb) {
            if (b.getUserId() == userId) {
                userBookings.add(b);
            }
        }
        return userBookings;
    }

    public Booking getBookingById(int bookingId) {
        for (Booking b : mockDb) {
            if (b.getId() == bookingId) {
                return b;
            }
        }
        return null;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        for (Booking b : mockDb) {
            if (b.getId() == bookingId) {
                b.setStatus(status);
                return true;
            }
        }
        return false;
    }

    public boolean isSeatBooked(int scheduleId, int seatNumber) {
        return false; // Mock always available
    }
}
