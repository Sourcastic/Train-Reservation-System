package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
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

      LocalDate today = LocalDate.now();
      StringBuilder sql = new StringBuilder(
          "INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, capacity, price) VALUES ");

      // Generate schedules for next 7 days
      for (int day = 1; day <= 7; day++) {
        LocalDate scheduleDate = today.plusDays(day);

        // Route 1: New York to Boston (2 schedules per day)
        addSchedule(sql, 1, scheduleDate, LocalTime.of(8, 0), LocalTime.of(12, 0), 100, 50.00);
        addSchedule(sql, 1, scheduleDate, LocalTime.of(14, 0), LocalTime.of(18, 0), 100, 55.00);

        // Route 2: Boston to New York (2 schedules per day)
        addSchedule(sql, 2, scheduleDate, LocalTime.of(9, 0), LocalTime.of(13, 0), 100, 50.00);
        addSchedule(sql, 2, scheduleDate, LocalTime.of(15, 30), LocalTime.of(19, 30), 100, 55.00);

        // Route 3: Chicago to St. Louis (2 schedules per day)
        addSchedule(sql, 3, scheduleDate, LocalTime.of(9, 30), LocalTime.of(14, 30), 80, 40.00);
        addSchedule(sql, 3, scheduleDate, LocalTime.of(16, 0), LocalTime.of(21, 0), 80, 45.00);

        // Route 4: St. Louis to Chicago (1 schedule per day)
        addSchedule(sql, 4, scheduleDate, LocalTime.of(10, 0), LocalTime.of(15, 0), 80, 40.00);

        // Route 5: Los Angeles to San Francisco (2 schedules per day)
        addSchedule(sql, 5, scheduleDate, LocalTime.of(10, 0), LocalTime.of(15, 30), 120, 75.00);
        addSchedule(sql, 5, scheduleDate, LocalTime.of(18, 0), LocalTime.of(23, 30), 120, 80.00);

        // Route 6: San Francisco to Los Angeles (1 schedule per day)
        addSchedule(sql, 6, scheduleDate, LocalTime.of(11, 0), LocalTime.of(16, 30), 120, 75.00);

        // Route 7: Washington DC to New York (2 schedules per day)
        addSchedule(sql, 7, scheduleDate, LocalTime.of(7, 30), LocalTime.of(11, 0), 90, 60.00);
        addSchedule(sql, 7, scheduleDate, LocalTime.of(13, 0), LocalTime.of(16, 30), 90, 65.00);

        // Route 8: New York to Washington DC (1 schedule per day)
        addSchedule(sql, 8, scheduleDate, LocalTime.of(8, 30), LocalTime.of(12, 0), 90, 60.00);

        // Route 9: Atlanta to Miami (1 schedule per day)
        addSchedule(sql, 9, scheduleDate, LocalTime.of(9, 0), LocalTime.of(17, 0), 110, 85.00);

        // Route 10: Miami to Atlanta (1 schedule per day)
        addSchedule(sql, 10, scheduleDate, LocalTime.of(10, 0), LocalTime.of(18, 0), 110, 85.00);
      }

      // Remove trailing comma and execute
      String finalSql = sql.toString().replaceAll(",\\s*$", "");
      stmt.executeUpdate(finalSql);

      System.out.println("✅ Seeded schedules for next 7 days");
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
  private static void addSchedule(StringBuilder sql, int routeId, LocalDate date,
      LocalTime departure, LocalTime arrival,
      int capacity, double price) {
    sql.append(String.format(
        "(%d, '%s', '%s', '%s', %d, %.2f), ",
        routeId, date, departure, arrival, capacity, price));
  }
}
