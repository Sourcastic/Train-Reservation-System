package com.example.trainreservationsystem.seeders.shared;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds user data into the database.
 * Only seeds if the users table is empty.
 */
public class UserSeeder {

  /**
   * Seeds demo users into the database.
   *
   * @param conn Database connection
   * @return true if data was seeded, false if table already had data
   */
  public static boolean seed(Connection conn) {
    return seed(conn, false);
  }

  /**
   * Seeds demo users into the database.
   *
   * @param conn  Database connection
   * @param force If true, seeds even if data already exists
   * @return true if data was seeded, false if table already had data (and
   *         force=false)
   */
  public static boolean seed(Connection conn, boolean force) {
    try (Statement stmt = conn.createStatement()) {
      // Check if users table is empty (unless forcing)
      if (!force) {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next() && rs.getInt(1) > 0) {
          return false; // Skip silently in normal mode
        }
      }

      // Seed demo users
      stmt.executeUpdate(
          "INSERT INTO users (username, password, email, user_type, loyalty_points) VALUES " +
              "('demo', 'demo123', 'demo@example.com', 'CUSTOMER', 100), " +
              "('admin', 'admin123', 'admin@trainreservation.com', 'ADMIN', 0), " +
              "('john_doe', 'password123', 'john.doe@email.com', 'CUSTOMER', 250), " +
              "('jane_smith', 'password123', 'jane.smith@email.com', 'CUSTOMER', 150)");

      System.out.println("✅ Seeded 4 users");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding users: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
