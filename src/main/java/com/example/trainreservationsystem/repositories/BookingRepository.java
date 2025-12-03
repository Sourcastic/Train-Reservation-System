package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.utils.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

  public Booking createBooking(Booking booking) {
    String insertBooking = "INSERT INTO bookings (user_id, schedule_id, status, booking_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
    String insertPassenger = "INSERT INTO passengers (booking_id, name, age, bring_pet, has_wheelchair, seat_number) VALUES (?, ?, ?, ?, ?, ?)";

    Connection conn = null;
    try {
      conn = Database.getConnection();
      conn.setAutoCommit(false); // Transaction

      int bookingId = -1;
      try (PreparedStatement stmt = conn.prepareStatement(insertBooking)) {
        stmt.setInt(1, booking.getUserId());
        stmt.setInt(2, booking.getScheduleId());
        stmt.setString(3, booking.getStatus());

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          bookingId = rs.getInt(1);
          booking.setId(bookingId);
        }
      }

      if (bookingId != -1 && booking.getPassengers() != null) {
        try (PreparedStatement stmt = conn.prepareStatement(insertPassenger)) {
          for (Passenger p : booking.getPassengers()) {
            stmt.setInt(1, bookingId);
            stmt.setString(2, p.getName());
            stmt.setInt(3, p.getAge());
            stmt.setBoolean(4, p.isBringPet());
            stmt.setBoolean(5, p.isHasWheelchair());
              stmt.setInt(6, p.getSeatNumber());
            stmt.addBatch();
          }
          stmt.executeBatch();
        }
      }

      conn.commit();
      return booking;
    } catch (Exception e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      System.err.println("Error creating booking: " + e.getMessage());
      e.printStackTrace();
        throw new RuntimeException("Failed to create booking", e);
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public List<Booking> getBookingsByUserId(int userId) {
    List<Booking> bookings = new ArrayList<>();
    String query = "SELECT * FROM bookings WHERE user_id = ? ORDER BY booking_date DESC";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        bookings.add(mapResultSetToBooking(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting bookings: " + e.getMessage());
      e.printStackTrace();
    }
    return bookings;
  }

  public boolean updateBookingStatus(int bookingId, String status) {
    String query = "UPDATE bookings SET status = ? WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setString(1, status);
      stmt.setInt(2, bookingId);
      return stmt.executeUpdate() > 0;
    } catch (Exception e) {
      System.err.println("Error updating booking status: " + e.getMessage());
      e.printStackTrace();
        return false;
    }
  }

  private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
    Timestamp ts = rs.getTimestamp("booking_date");
    return new Booking(
        rs.getInt("id"),
        rs.getInt("user_id"),
        rs.getInt("schedule_id"),
        rs.getString("status"),
        ts != null ? ts.toLocalDateTime() : null);
  }

  /**
   * Gets all occupied seat numbers for a specific schedule.
   * Returns seats that are booked and confirmed.
   */
  public List<Integer> getOccupiedSeats(int scheduleId) {
    List<Integer> occupied = new ArrayList<>();
    String query = "SELECT DISTINCT p.seat_number " +
        "FROM passengers p " +
        "JOIN bookings b ON p.booking_id = b.id " +
        "WHERE b.schedule_id = ? AND p.seat_number > 0 " +
        "AND b.status IN ('PENDING', 'CONFIRMED')";

    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, scheduleId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        occupied.add(rs.getInt("seat_number"));
      }
    } catch (Exception e) {
      System.err.println("Error getting occupied seats: " + e.getMessage());
      e.printStackTrace();
    }
    return occupied;
  }

  public boolean isSeatBooked(int scheduleId, int seatNumber) {
    return getOccupiedSeats(scheduleId).contains(seatNumber);
  }
}
