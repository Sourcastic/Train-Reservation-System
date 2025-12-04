package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.time.LocalTime;

/**
 * Seeds schedule data into the database.
 * Only seeds if the schedules table is empty.
 * Creates schedules for the next 7 days.
 */
public class ScheduleSeeder {

  /**
   * Seeds train schedules into the database.
   * Creates multiple schedules per route for the next week.
   *
   * @param conn Database connection
   * @return true if data was seeded, false if table already had data
   */
  public static boolean seed(Connection conn) {
    return seed(conn, false);
  }

  /**
   * Seeds train schedules into the database.
   * Creates multiple schedules per route for the next week.
   *
   * @param conn  Database connection
   * @param force If true, seeds even if data already exists
   * @return true if data was seeded, false if table already had data (and
   *         force=false)
   */
  public static boolean seed(Connection conn, boolean force) {
    try (Statement stmt = conn.createStatement()) {
      // Check if schedules table is empty (unless forcing)
      if (!force) {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schedules");
        if (rs.next() && rs.getInt(1) > 0) {
          return false; // Skip silently in normal mode
        }
      }

      StringBuilder sql = new StringBuilder(
          "INSERT INTO schedules (route_id, departure_time, arrival_time, capacity, price, days_of_week) VALUES ");

      String allDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY";

      // Route 1: Karachi -> Rawalpindi (Tezgam)
      addSchedule(sql, 1, LocalTime.of(17, 0), LocalTime.of(19, 0), 200, 3500.00, allDays);

      // Route 2: Karachi -> Islamabad (Green Line)
      addSchedule(sql, 2, LocalTime.of(22, 0), LocalTime.of(20, 0), 150, 5500.00, allDays);

      // Route 3: Karachi -> Peshawar (Khyber Mail)
      addSchedule(sql, 3, LocalTime.of(22, 15), LocalTime.of(22, 0), 180, 3000.00, allDays);

      // Route 4: Quetta -> Peshawar (Jaffar Express)
      addSchedule(sql, 4, LocalTime.of(9, 0), LocalTime.of(15, 0), 160, 2500.00, allDays);

      // Route 5: Karachi -> Lahore (Karakoram Express)
      addSchedule(sql, 5, LocalTime.of(15, 30), LocalTime.of(10, 0), 220, 4000.00, allDays);

      // Remove trailing comma and execute
      String finalSql = sql.toString().replaceAll(",\\s*$", "");
      stmt.executeUpdate(finalSql);

      System.out.println("✅ Seeded schedules (recurring)");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding schedules: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Helper method to add a schedule to the SQL statement.
   */
  private static void addSchedule(StringBuilder sql, int routeId,
      LocalTime departure, LocalTime arrival,
      int capacity, double price, String daysOfWeek) {
    sql.append(String.format(
        "(%d, '%s', '%s', %d, %.2f, '%s'), ",
        routeId, departure, arrival, capacity, price, daysOfWeek));
  }
}
