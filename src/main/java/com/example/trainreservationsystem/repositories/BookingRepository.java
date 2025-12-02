package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    // Create a new booking
    public Booking createBooking(Booking booking) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO bookings (user_id, schedule_id, journey_date, status, booking_date) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getScheduleId());
            ps.setTimestamp(3, Timestamp.valueOf(booking.getJourneyDate()));
            ps.setString(4, booking.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                booking.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return booking;
    }

    // Get all bookings for a user
    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE user_id = ?")) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Get booking by ID
    public Booking getBookingById(int bookingId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE id = ?")) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToBooking(rs);

        } catch (Exception e) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete booking by ID
    public boolean deleteBooking(int bookingId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE id = ?")) {

            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Map ResultSet to Booking object
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setScheduleId(rs.getInt("schedule_id"));
        booking.setStatus(rs.getString("status"));
        booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        booking.setJourneyDate(rs.getTimestamp("journey_date").toLocalDateTime());
        return booking;
    }

    // Get expired bookings
    public List<Booking> getExpiredBookings() {
        List<Booking> expired = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM bookings WHERE journey_date < ? OR status = 'Invalid'")) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                expired.add(mapResultSetToBooking(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return expired;
    }
    // Get all bookings for a schedule
    public List<Booking> getBookingsBySchedule(int scheduleId) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE schedule_id = ?")) {

            ps.setInt(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Get bookings by journey date
    public List<Booking> getBookingsByJourneyDate(LocalDate date) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE journey_date = ?")) {

            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }
}
