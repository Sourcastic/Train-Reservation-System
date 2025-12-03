package com.example.trainreservationsystem.seeders;

import com.example.trainreservationsystem.utils.database.Database;

import java.sql.Connection;

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

    try (Connection conn = Database.getConnection()) {
      System.out.println("🌱 Starting database seeding...");

      // Seed in order (respecting foreign key dependencies)
      boolean usersSeeded = UserSeeder.seed(conn);
      // 2. Seed Seat Classes
      boolean seatClassesSeeded = SeatClassSeeder.seed(conn);

      // 3. Seed Routes
      boolean routesSeeded = RouteSeeder.seed(conn);

      // 4. Seed Schedules
      boolean schedulesSeeded = ScheduleSeeder.seed(conn);

      // Summary
      int seededCount = 0;
      if (usersSeeded)
        seededCount++;
      if (seatClassesSeeded)
        seededCount++;
      if (routesSeeded)
        seededCount++;
      if (schedulesSeeded)
        seededCount++;

      if (seededCount > 0) {
        System.out.println("✅ Database seeding completed! (" + seededCount + " tables seeded)");
      } else {
        System.out.println("ℹ️  Database already has data, no seeding needed");
      }

      return true;
    } catch (Exception e) {
      System.err.println("❌ Database seeding error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
