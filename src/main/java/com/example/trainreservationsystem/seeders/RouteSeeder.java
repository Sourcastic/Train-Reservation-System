package com.example.trainreservationsystem.seeders;

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
    try (Statement stmt = conn.createStatement()) {
      // Check if routes table is empty
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM routes");
      if (rs.next() && rs.getInt(1) > 0) {
        System.out.println("ℹ️  Routes table already has data, skipping seed");
        return false;
      }

      // Seed routes
      stmt.executeUpdate(
          "INSERT INTO routes (name, source, destination) VALUES " +
              "('Northeast Express', 'New York', 'Boston'), " +
              "('Northeast Return', 'Boston', 'New York'), " +
              "('Midwest Line', 'Chicago', 'St. Louis'), " +
              "('Midwest Return', 'St. Louis', 'Chicago'), " +
              "('West Coast Express', 'Los Angeles', 'San Francisco'), " +
              "('West Coast Return', 'San Francisco', 'Los Angeles'), " +
              "('East Coast Line', 'Washington DC', 'New York'), " +
              "('East Coast Return', 'New York', 'Washington DC'), " +
              "('Southern Route', 'Atlanta', 'Miami'), " +
              "('Southern Return', 'Miami', 'Atlanta')");

      System.out.println("✅ Seeded 10 routes");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding routes: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
