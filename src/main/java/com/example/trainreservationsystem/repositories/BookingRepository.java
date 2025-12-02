package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    // Create a new booking in DB
    public Booking createBooking(Booking booking) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO bookings (user_id, schedule_id, journey_date, status, booking_date) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getScheduleId());
            ps.setDate(3, Date.valueOf(booking.getJourneyDate().toLocalDate()));
            ps.setString(4, booking.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) booking.setId(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booking;
    }

    // Get bookings by user ID
    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapResultSetToBooking(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Get booking by booking ID
    public Booking getBookingById(int bookingId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE id = ?")) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToBooking(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update booking status
    public boolean updateBookingStatus(int bookingId, String status) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if a seat is booked (simplified, can enhance later)
    public boolean isSeatBooked(int scheduleId, int seatNumber) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM bookings WHERE schedule_id = ? AND seat_number = ?")) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, seatNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all bookings for a schedule
    public List<Booking> getBookingsBySchedule(int scheduleId) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE schedule_id = ?")) {
            ps.setInt(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapResultSetToBooking(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Get bookings by journey date for reminders (UC-19)
    public List<Booking> getBookingsByJourneyDate(LocalDate date) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE journey_date = ?")) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapResultSetToBooking(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Map ResultSet row to Booking object
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("schedule_id")
        );
        b.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        b.setStatus(rs.getString("status"));
        b.setJourneyDate(rs.getDate("journey_date").toLocalDate().atStartOfDay());
        return b;
    }
    // Get expired bookings (journey date before today or invalid status)
    public List<Booking> getExpiredBookings() {
        List<Booking> expired = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM bookings WHERE journey_date < ? OR status = 'Invalid'")) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                expired.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expired;
    }

    // Delete booking by ID
    public boolean deleteBooking(int bookingId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
