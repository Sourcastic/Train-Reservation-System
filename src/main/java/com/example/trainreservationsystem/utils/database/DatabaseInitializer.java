package com.example.trainreservationsystem.utils.database;

import java.sql.Connection;
import java.sql.Statement;

import com.example.trainreservationsystem.seeders.DatabaseSeeder;

/**
 * Initializes database tables.
 * Creates all required tables if they don't exist.
 */
public class DatabaseInitializer {

  public static boolean initialize() {
    try (Connection conn = Database.getConnection();
        Statement stmt = conn.createStatement()) {

      createTables(stmt);
      System.out.println("✅ Database tables created successfully!");

      DatabaseSeeder.seed();
      return true;

    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        System.out.println("ℹ️  Running in mock data mode (no database connection)");
        return false;
      }
      System.err.println("❌ Database initialization error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private static void createTables(Statement stmt) throws Exception {
    stmt.execute(createUsersTable());
    stmt.execute(createRoutesTable());
    stmt.execute(createSchedulesTable());
    stmt.execute(createBookingsTable());
    stmt.execute(createPassengersTable());
    stmt.execute(createPaymentMethodsTable());
    stmt.execute(createPaymentsTable());
    stmt.execute(createComplaintsTable());
    stmt.execute(createNotificationsTable());
    stmt.execute(createTicketsTable());
  }

  private static String createUsersTable() {
    return "CREATE TABLE IF NOT EXISTS users (" +
        "id SERIAL PRIMARY KEY, " +
        "username VARCHAR(50) UNIQUE NOT NULL, " +
        "password VARCHAR(255) NOT NULL, " +
        "email VARCHAR(100) NOT NULL, " +
        "loyalty_points INT DEFAULT 0, " +
        "user_type VARCHAR(20) DEFAULT 'CUSTOMER')";
  }

  private static String createRoutesTable() {
    return "CREATE TABLE IF NOT EXISTS routes (" +
        "id SERIAL PRIMARY KEY, " +
        "name VARCHAR(100), " +
        "source VARCHAR(100) NOT NULL, " +
        "destination VARCHAR(100) NOT NULL)";
  }

  private static String createSchedulesTable() {
    return "CREATE TABLE IF NOT EXISTS schedules (" +
        "id SERIAL PRIMARY KEY, " +
        "route_id INT REFERENCES routes(id), " +
        "departure_date DATE NOT NULL, " +
        "departure_time TIME NOT NULL, " +
        "arrival_time TIME NOT NULL, " +
        "capacity INT NOT NULL, " +
        "price DECIMAL(10, 2) NOT NULL)";
  }

  private static String createBookingsTable() {
    return "CREATE TABLE IF NOT EXISTS bookings (" +
        "id SERIAL PRIMARY KEY, " +
        "user_id INT REFERENCES users(id), " +
        "schedule_id INT REFERENCES schedules(id), " +
        "status VARCHAR(20) NOT NULL, " +
        "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
  }

  private static String createPassengersTable() {
    return "CREATE TABLE IF NOT EXISTS passengers (" +
        "id SERIAL PRIMARY KEY, " +
        "booking_id INT REFERENCES bookings(id), " +
        "name VARCHAR(100) NOT NULL, " +
        "age INT NOT NULL, " +
        "bring_pet BOOLEAN DEFAULT FALSE, " +
        "has_wheelchair BOOLEAN DEFAULT FALSE, " +
        "seat_number INT)";
  }

  private static String createPaymentMethodsTable() {
    return "CREATE TABLE IF NOT EXISTS payment_methods (" +
        "id SERIAL PRIMARY KEY, " +
        "user_id INT REFERENCES users(id), " +
        "method_type VARCHAR(20) NOT NULL, " +
        "details VARCHAR(255) NOT NULL)";
  }

  private static String createPaymentsTable() {
    return "CREATE TABLE IF NOT EXISTS payments (" +
        "id SERIAL PRIMARY KEY, " +
        "booking_id INT REFERENCES bookings(id), " +
        "amount DECIMAL(10, 2) NOT NULL, " +
        "payment_method_id INT REFERENCES payment_methods(id), " +
        "status VARCHAR(20) NOT NULL, " +
        "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
  }

  private static String createComplaintsTable() {
    return "CREATE TABLE IF NOT EXISTS complaints (" +
        "id SERIAL PRIMARY KEY, " +
        "user_id INT REFERENCES users(id), " +
        "subject VARCHAR(200) NOT NULL, " +
        "description TEXT NOT NULL, " +
        "tracking_id VARCHAR(50) UNIQUE NOT NULL, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
  }

  private static String createNotificationsTable() {
    return "CREATE TABLE IF NOT EXISTS notifications (" +
        "id SERIAL PRIMARY KEY, " +
        "user_id INT REFERENCES users(id), " +
        "message TEXT NOT NULL, " +
        "sent BOOLEAN DEFAULT FALSE, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
  }

  private static String createTicketsTable() {
    return "CREATE TABLE IF NOT EXISTS tickets (" +
        "id SERIAL PRIMARY KEY, " +
        "booking_id INT REFERENCES bookings(id), " +
        "qr_code VARCHAR(50) UNIQUE NOT NULL, " +
        "status VARCHAR(20) NOT NULL, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
  }
}
