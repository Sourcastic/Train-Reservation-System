package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.utils.database.Database;

public class BookingRepository {
  private static List<Booking> mockDb = new ArrayList<>();
  private static int mockIdCounter = 1;

  // Initialize mock data with sample bookings
  static {
    initializeMockData();
  }

  private static void initializeMockData() {
    // Add some sample bookings for demo user (userId = 1)
    LocalDateTime now = LocalDateTime.now();

    // Past booking
    Booking pastBooking = new Booking(1, 1, 1, "CONFIRMED", now.minusDays(5));
    mockDb.add(pastBooking);

    // Recent booking
    Booking recentBooking = new Booking(2, 1, 2, "CONFIRMED", now.minusDays(2));
    mockDb.add(recentBooking);

    // Upcoming booking
    Booking upcomingBooking = new Booking(3, 1, 3, "CONFIRMED", now.minusDays(1));
    mockDb.add(upcomingBooking);

    // Set counter to avoid ID conflicts
    mockIdCounter = 4;
  }

  public Booking createBooking(Booking booking) {
    if (Database.isMockMode()) {
      booking.setId(mockIdCounter++);
      booking.setBookingDate(LocalDateTime.now());
      mockDb.add(booking);
      return booking;
    }

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
            stmt.setInt(6, p.getSeatNumber()); // Use actual seat number from passenger
            stmt.addBatch();
          }
          stmt.executeBatch();
        }
      }

      conn.commit();
      return booking;
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return createBooking(booking); // Retry with mock mode
      }
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      System.err.println("Error creating booking: " + e.getMessage());
      e.printStackTrace();
      // Fallback to mock
      return createBooking(booking);
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public List<Booking> getBookingsByUserId(int userId) {
    if (Database.isMockMode()) {
      List<Booking> userBookings = new ArrayList<>();
      for (Booking b : mockDb) {
        if (b.getUserId() == userId) {
          userBookings.add(b);
        }
      }
      return userBookings;
    }

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
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return getBookingsByUserId(userId); // Retry with mock
      }
      System.err.println("Error getting bookings: " + e.getMessage());
      e.printStackTrace();
      return getBookingsByUserId(userId); // Fallback to mock
    }
    return bookings;
  }

  public boolean updateBookingStatus(int bookingId, String status) {
    if (Database.isMockMode()) {
      for (Booking b : mockDb) {
        if (b.getId() == bookingId) {
          b.setStatus(status);
          return true;
        }
      }
      return false;
    }

    String query = "UPDATE bookings SET status = ? WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setString(1, status);
      stmt.setInt(2, bookingId);
      return stmt.executeUpdate() > 0;
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return updateBookingStatus(bookingId, status); // Retry with mock
      }
      System.err.println("Error updating booking status: " + e.getMessage());
      e.printStackTrace();
      return updateBookingStatus(bookingId, status); // Fallback to mock
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
    if (Database.isMockMode()) {
      // Return some mock occupied seats
      return List.of(5, 8, 15, 22, 30);
    }

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
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return getOccupiedSeats(scheduleId); // Retry with mock
      }
      System.err.println("Error getting occupied seats: " + e.getMessage());
      e.printStackTrace();
    }
    return occupied;
  }

  public boolean isSeatBooked(int scheduleId, int seatNumber) {
    return getOccupiedSeats(scheduleId).contains(seatNumber);
  }
}
