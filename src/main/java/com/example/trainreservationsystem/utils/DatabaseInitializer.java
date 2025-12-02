package com.example.trainreservationsystem.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
  public static void initialize() {
    try (Connection conn = Database.getConnection();
        Statement stmt = conn.createStatement()) {

      // Users table
      stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
          "id SERIAL PRIMARY KEY, " +
          "username VARCHAR(50) UNIQUE NOT NULL, " +
          "password VARCHAR(255) NOT NULL, " +
          "email VARCHAR(100) NOT NULL, " +
          "loyalty_points INT DEFAULT 0, " +
          "user_type VARCHAR(20) DEFAULT 'CUSTOMER')");


      // Routes table
      stmt.execute("CREATE TABLE IF NOT EXISTS routes (" +
          "id SERIAL PRIMARY KEY, " +
          "name VARCHAR(100), " +
          "source VARCHAR(100) NOT NULL, " +
          "destination VARCHAR(100) NOT NULL)");

      // Schedules (formerly trains)
      stmt.execute("CREATE TABLE IF NOT EXISTS schedules (" +
            "id SERIAL PRIMARY KEY, " +
            "route_id INT REFERENCES routes(id), " +
            "departure_date DATE NOT NULL, " +
            "departure_time TIME NOT NULL, " +
            "arrival_time TIME NOT NULL, " +
                "capacity INT NOT NULL, " +
                "price DECIMAL(10, 2) NOT NULL, " +
                ")");


      // Bookings
      stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
          "id SERIAL PRIMARY KEY, " +
          "user_id INT REFERENCES users(id), " +
          "schedule_id INT REFERENCES schedules(id), " +
          "status VARCHAR(20) NOT NULL, " +
          "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      // Passengers table
      stmt.execute("CREATE TABLE IF NOT EXISTS passengers (" +
          "id SERIAL PRIMARY KEY, " +
          "booking_id INT REFERENCES bookings(id), " +
          "name VARCHAR(100) NOT NULL, " +
          "age INT NOT NULL, " +
          "bring_pet BOOLEAN DEFAULT FALSE, " +
          "has_wheelchair BOOLEAN DEFAULT FALSE, " +
          "seat_number INT)");

      // Payment Methods table
      stmt.execute("CREATE TABLE IF NOT EXISTS payment_methods (" +
          "id SERIAL PRIMARY KEY, " +
          "user_id INT REFERENCES users(id), " +
          "method_type VARCHAR(20) NOT NULL, " +
          "details VARCHAR(255) NOT NULL)");

      // Payments table
      stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
          "id SERIAL PRIMARY KEY, " +
          "booking_id INT REFERENCES bookings(id), " +
          "amount DECIMAL(10, 2) NOT NULL, " +
          "payment_method_id INT REFERENCES payment_methods(id), " +
          "status VARCHAR(20) NOT NULL, " +
          "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      // Complaints table
      stmt.execute("CREATE TABLE IF NOT EXISTS complaints (" +
          "id SERIAL PRIMARY KEY, " +
          "user_id INT REFERENCES users(id), " +
          "subject VARCHAR(200) NOT NULL, " +
          "description TEXT NOT NULL, " +
          "tracking_id VARCHAR(50) UNIQUE NOT NULL, " +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      // Staff Responses table
      stmt.execute("CREATE TABLE IF NOT EXISTS staff_responses (" +
                "id SERIAL PRIMARY KEY, " +
                "complaint_id INT REFERENCES complaints(id), " +
                "staff_id INT REFERENCES users(id), " +
                "response TEXT NOT NULL, " +
                "responded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
      // Notifications table
      stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
          "id SERIAL PRIMARY KEY, " +
          "user_id INT REFERENCES users(id), " +
          "message TEXT NOT NULL, " +
          "sent BOOLEAN DEFAULT FALSE, " +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      stmt.execute("ALTER TABLE schedules " +
              "ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'On-time'");

      // Seed some data if empty
      seedData(conn);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void seedData(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
      if (rs.next() && rs.getInt(1) == 0) {
        stmt.executeUpdate(
            "INSERT INTO users (username, password, email, user_type) VALUES ('demo', 'demo123', 'demo@example.com', 'CUSTOMER')");
      }

      var rsRoutes = stmt.executeQuery("SELECT COUNT(*) FROM routes");
      if (rsRoutes.next() && rsRoutes.getInt(1) == 0) {
        stmt.executeUpdate("INSERT INTO routes (name, source, destination) VALUES " +
            "('Route 1', 'New York', 'Boston'), " +
            "('Route 2', 'Boston', 'New York'), " +
            "('Route 3', 'Chicago', 'St. Louis')");
      }

      var rsSchedules = stmt.executeQuery("SELECT COUNT(*) FROM schedules");
      if (rsSchedules.next() && rsSchedules.getInt(1) == 0) {
        // Assume Route IDs 1, 2, 3 exist from above
        stmt.executeUpdate(
            "INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, capacity, price) VALUES " +
                "(1, '2023-11-01', '08:00:00', '12:00:00', 100, 50.00), " +
                "(2, '2023-11-01', '14:00:00', '18:00:00', 100, 55.00), " +
                "(3, '2023-11-02', '09:30:00', '14:30:00', 80, 40.00)");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
