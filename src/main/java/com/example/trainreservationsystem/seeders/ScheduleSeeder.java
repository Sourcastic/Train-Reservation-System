package com.example.trainreservationsystem.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Seeds schedule data into the database.
 * Includes Schedules and Seats.
 */
public class ScheduleSeeder {

  public static boolean seed(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      // Check if schedules table is empty
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schedules");
      if (rs.next() && rs.getInt(1) > 0) {
        System.out.println("ℹ️  Schedules already seeded, skipping.");
        return false;
      }

      System.out.println("🌱 Seeding Schedules and Seats...");

      // Seed Schedules & Seats
      // Tezgam: Karachi -> Rawalpindi
      seedSchedule(conn, "Tezgam", "17:00:00", "19:30:00", 3000.0,
          "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY");

      // Green Line: Karachi -> Islamabad
      seedSchedule(conn, "Green Line", "22:00:00", "20:00:00", 5000.0,
          "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY");

      // Khyber Mail: Karachi -> Peshawar
      seedSchedule(conn, "Khyber Mail", "22:15:00", "22:00:00", 2500.0,
          "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY");

      // Jaffer Express: Quetta -> Peshawar
      seedSchedule(conn, "Jaffer Express", "09:00:00", "14:00:00", 3500.0,
          "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY");

      System.out.println("✅ Seeded Schedules and Seats");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding schedules: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private static void seedSchedule(Connection conn, String routeName, String depTime, String arrTime, double price,
      String days) throws Exception {
    int routeId = getRouteId(conn, routeName);
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    // Insert Schedule
    int scheduleId;
    String insertScheduleSql = "INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, capacity, price, days_of_week) VALUES (?, ?, CAST(? AS TIME), CAST(? AS TIME), ?, ?, ?) RETURNING id";
    try (PreparedStatement pstmt = conn.prepareStatement(insertScheduleSql)) {
      pstmt.setInt(1, routeId);
      pstmt.setObject(2, tomorrow);
      pstmt.setString(3, depTime);
      pstmt.setString(4, arrTime);
      pstmt.setInt(5, 100); // Total capacity
      pstmt.setDouble(6, price);
      pstmt.setString(7, days);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        scheduleId = rs.getInt(1);
      } else {
        throw new Exception("Failed to retrieve schedule ID for " + routeName);
      }
    }

    // Seed Seats using new Seat Classes
    seedSeats(conn, scheduleId, "Economy", 60);
    seedSeats(conn, scheduleId, "Business", 30);
    seedSeats(conn, scheduleId, "First Class", 10);
  }

  private static void seedSeats(Connection conn, int scheduleId, String className, int count) throws Exception {
    int classId = getSeatClassId(conn, className);
    String insertSeatSql = "INSERT INTO seats (schedule_id, seat_class_id, is_booked) VALUES (?, ?, false)";
    try (PreparedStatement pstmt = conn.prepareStatement(insertSeatSql)) {
      for (int i = 0; i < count; i++) {
        pstmt.setInt(1, scheduleId);
        pstmt.setInt(2, classId);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
    }
  }

  private static int getRouteId(Connection conn, String routeName) throws Exception {
    String sql = "SELECT id FROM routes WHERE name = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, routeName);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("id");
      } else {
        throw new Exception("Route not found: " + routeName);
      }
    }
  }

  private static int getSeatClassId(Connection conn, String className) throws Exception {
    String sql = "SELECT id FROM seat_classes WHERE name = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, className);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("id");
      } else {
        throw new Exception("Seat Class not found: " + className);
      }
    }
  }
}
