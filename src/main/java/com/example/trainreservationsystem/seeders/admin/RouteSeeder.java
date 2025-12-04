package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds route data into the database.
 * Only seeds if the routes table is empty.
 */
public class RouteSeeder {

  /**
   * Seeds train routes into the database.
   *
   * @param conn Database connection
   * @return true if data was seeded, false if table already had data
   */
  public static boolean seed(Connection conn) {
    return seed(conn, false);
  }

  /**
   * Seeds train routes into the database.
   *
   * @param conn  Database connection
   * @param force If true, seeds even if data already exists
   * @return true if data was seeded, false if table already had data (and
   *         force=false)
   */
  public static boolean seed(Connection conn, boolean force) {
    try (Statement stmt = conn.createStatement()) {
      // Check if routes table is empty (unless forcing)
      if (!force) {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM routes");
        if (rs.next() && rs.getInt(1) > 0) {
          return false; // Skip silently in normal mode
        }
      }

      // Seed routes

      stmt.executeUpdate(
          "INSERT INTO routes (source, destination) VALUES " +
              "('Karachi', 'Rawalpindi'), " + // Route 1
              "('Karachi', 'Islamabad'), " + // Route 2
              "('Karachi', 'Peshawar'), " + // Route 3
              "('Quetta', 'Peshawar'), " + // Route 4
              "('Karachi', 'Lahore')"); // Route 5

      System.out.println("✅ Seeded 10 routes");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding routes: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
