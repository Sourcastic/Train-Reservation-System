package com.example.trainreservationsystem.seeders.shared;

import com.example.trainreservationsystem.utils.shared.database.Database;

/**
 * Command-line utility to seed the database on demand.
 * Can be run independently without starting the full application.
 *
 * Usage (using Maven - recommended):
 * mvn exec:java
 * -Dexec.mainClass="com.example.trainreservationsystem.seeders.SeedCommand"
 * mvn exec:java
 * -Dexec.mainClass="com.example.trainreservationsystem.seeders.SeedCommand"
 * -Dexec.args="--force"
 * mvn exec:java
 * -Dexec.mainClass="com.example.trainreservationsystem.seeders.SeedCommand"
 * -Dexec.args="--help"
 *
 * Usage (using Maven Wrapper):
 * ./mvnw exec:java
 * -Dexec.mainClass="com.example.trainreservationsystem.seeders.SeedCommand"
 * ./mvnw exec:java
 * -Dexec.mainClass="com.example.trainreservationsystem.seeders.SeedCommand"
 * -Dexec.args="--force"
 *
 * Usage (direct Java - requires Java in PATH):
 * java SeedCommand - Seeds only if tables are empty
 * java SeedCommand --force - Forces seeding even if data exists
 * java SeedCommand --help - Shows help message
 */
public class SeedCommand {

  public static void main(String[] args) {
    boolean force = false;

    // Parse command line arguments
    if (args.length > 0) {
      if (args[0].equals("--help") || args[0].equals("-h")) {
        printHelp();
        return;
      }
      if (args[0].equals("--force") || args[0].equals("-f")) {
        force = true;
      }
    }

    try {
      System.out.println("Database Seeder");
      System.out.println("==================");

      if (force) {
        System.out.println("[WARNING] Force mode: Will seed even if data exists");
      } else {
        System.out.println("[INFO] Normal mode: Will skip if data already exists");
      }
      System.out.println();

      // Run seeders (DatabaseSeeder manages its own connection)
      boolean success = DatabaseSeeder.seed(force);

      // Close database connection
      Database.closeConnection();

      if (success) {
        System.out.println();
        System.out.println("[SUCCESS] Seeding completed successfully!");
        System.exit(0);
      } else {
        System.out.println();
        System.err.println("[ERROR] Seeding failed or was skipped");
        System.exit(1);
      }

    } catch (Exception e) {
      System.err.println("[ERROR] Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void printHelp() {
    System.out.println("Database Seeder Command");
    System.out.println("=======================");
    System.out.println();
    System.out.println("Usage (Maven Wrapper - Recommended):");
    System.out.println("  ./mvnw exec:java@seed              # Normal mode");
    System.out.println("  ./mvnw exec:java@seed-force        # Force mode");
    System.out.println();
    System.out.println("Usage (Windows CMD):");
    System.out.println("  .\\mvnw.cmd exec:java@seed");
    System.out.println("  .\\mvnw.cmd exec:java@seed-force");
    System.out.println();
    System.out.println("Usage (Direct Maven):");
    System.out.println("  mvn exec:java -Dexec.mainClass=com.example.trainreservationsystem.seeders.SeedCommand");
    System.out.println(
        "  mvn exec:java -Dexec.mainClass=com.example.trainreservationsystem.seeders.SeedCommand -Dexec.args=--force");
    System.out.println();
    System.out.println("Usage (Direct Java):");
    System.out.println("  java SeedCommand [options]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  --force, -f    Force seeding even if data already exists");
    System.out.println("  --help, -h     Show this help message");
    System.out.println();
    System.out.println("Examples:");
    System.out
        .println("  mvn exec:java -Dexec.mainClass=\"...SeedCommand\"              # Seed only if tables are empty");
    System.out.println("  mvn exec:java -Dexec.mainClass=\"...SeedCommand\" -Dexec.args=\"--force\"  # Force seed");
    System.out.println();
  }
}
