package com.example.trainreservationsystem.seeders.shared;

import java.sql.Connection;

import com.example.trainreservationsystem.seeders.admin.RouteSeeder;
import com.example.trainreservationsystem.seeders.admin.ScheduleSeeder;
import com.example.trainreservationsystem.utils.shared.database.Database;

/**
 * Main database seeder that coordinates all individual seeders.
 * Seeds data in the correct order (respecting foreign key constraints).
 */
public class DatabaseSeeder {

  /**
   * Runs all seeders in the correct order.
   * Only seeds if database is not in mock mode.
   *
   * @return true if seeding was successful or skipped, false on error
   */
  public static boolean seed() {
    return seed(false);
  }

  /**
   * Runs all seeders in the correct order.
   *
   * @param force If true, forces seeding even if data already exists
   * @return true if seeding was successful or skipped, false on error
   */
  public static boolean seed(boolean force) {
    try (Connection conn = Database.getConnection()) {
      if (!force) {
        System.out.println("[INFO] Starting database seeding...");
      } else {
        System.out.println("[INFO] Starting database seeding (force mode)...");
      }

      // Seed in order (respecting foreign key dependencies)
      boolean usersSeeded = UserSeeder.seed(conn, force);
      boolean routesSeeded = RouteSeeder.seed(conn, force);
      boolean schedulesSeeded = ScheduleSeeder.seed(conn, force);

      // Summary
      int seededCount = 0;
      if (usersSeeded)
        seededCount++;
      if (routesSeeded)
        seededCount++;
      if (schedulesSeeded)
        seededCount++;

      if (seededCount > 0) {
        System.out.println("[SUCCESS] Database seeding completed! (" + seededCount + " tables seeded)");
      } else if (!force) {
        // Only show "no seeding needed" in normal mode
        System.out.println("[INFO] Database already has data, no seeding needed");
      } else {
        System.out.println("[SUCCESS] Database seeding completed (force mode)");
      }

      return true;
    } catch (Exception e) {
      System.err.println("[ERROR] Database seeding error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
