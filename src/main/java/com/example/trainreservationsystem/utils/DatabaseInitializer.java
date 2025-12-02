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
          "username VARCHAR(50) NOT NULL, " +
          "password VARCHAR(255) NOT NULL, " +
          "email VARCHAR(100) UNIQUE NOT NULL, " +
          "phone_no VARCHAR(20), " +
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
          "price DECIMAL(10, 2) NOT NULL)");

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

      // Notifications table
      stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
          "id SERIAL PRIMARY KEY, " +
          "user_id INT REFERENCES users(id), " +
          "message TEXT NOT NULL, " +
          "sent BOOLEAN DEFAULT FALSE, " +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      // Tickets table
      stmt.execute("CREATE TABLE IF NOT EXISTS tickets (" +
          "id SERIAL PRIMARY KEY, " +
          "booking_id INT REFERENCES bookings(id), " +
          "passenger_id INT REFERENCES passengers(id), " +
          "ticket_number VARCHAR(50) UNIQUE NOT NULL, " +
          "issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

      // Seats table
      stmt.execute("CREATE TABLE IF NOT EXISTS seats (" +
          "id SERIAL PRIMARY KEY, " +
          "schedule_id INT REFERENCES schedules(id), " +
          "seat_number INT NOT NULL, " +
          "seat_class VARCHAR(20) DEFAULT 'ECONOMY', " +
          "is_booked BOOLEAN DEFAULT FALSE)");

      // Discounts table
      stmt.execute("CREATE TABLE IF NOT EXISTS discounts (" +
          "id SERIAL PRIMARY KEY, " +
          "code VARCHAR(50) UNIQUE NOT NULL, " +
          "percentage DECIMAL(5, 2) NOT NULL, " +
          "expiry_date DATE)");

      // Stored Procedures

      // 1. User Registration
      stmt.execute("CREATE OR REPLACE PROCEDURE sp_register_user(" +
          "p_username VARCHAR, " +
          "p_password VARCHAR, " +
          "p_email VARCHAR, " +
          "p_phone_no VARCHAR, " +
          "p_user_type VARCHAR) " +
          "LANGUAGE plpgsql AS $$ " +
          "BEGIN " +
          "    INSERT INTO users (username, password, email, phone_no, user_type, loyalty_points) " +
          "    VALUES (p_username, p_password, p_email, p_phone_no, p_user_type, 0); " +
          "EXCEPTION " +
          "    WHEN unique_violation THEN " +
          "        RAISE EXCEPTION 'Email already registered'; " +
          "    WHEN OTHERS THEN " +
          "        RAISE; " +
          "END; $$");

      // 2. User Login
      stmt.execute("DROP FUNCTION IF EXISTS sp_authenticate_user(VARCHAR, VARCHAR)");
      stmt.execute("CREATE OR REPLACE FUNCTION sp_authenticate_user(" +
          "p_email VARCHAR, " +
          "p_password VARCHAR) " +
          "RETURNS TABLE(id INT) " +
          "LANGUAGE plpgsql AS $$ " +
          "BEGIN " +
          "    RETURN QUERY " +
          "    SELECT u.id " +
          "    FROM users u " +
          "    WHERE u.email = p_email AND u.password = p_password; " +
          "END; $$");

      // 3. Update Password
      stmt.execute("CREATE OR REPLACE PROCEDURE sp_update_password(" +
          "p_user_id INT, " +
          "p_new_password VARCHAR) " +
          "LANGUAGE plpgsql AS $$ " +
          "BEGIN " +
          "    UPDATE users " +
          "    SET password = p_new_password " +
          "    WHERE id = p_user_id; " +
          "END; $$");

      // Admin Protection Trigger
      stmt.execute("CREATE OR REPLACE FUNCTION fn_prevent_admin_delete() " +
          "RETURNS TRIGGER AS $$ " +
          "BEGIN " +
          "    IF OLD.username = 'admin' THEN " +
          "        RAISE EXCEPTION 'Cannot delete the super admin user'; " +
          "    END IF; " +
          "    RETURN OLD; " +
          "END; $$ LANGUAGE plpgsql");

      stmt.execute("DROP TRIGGER IF EXISTS trg_prevent_admin_delete ON users");
      stmt.execute("CREATE TRIGGER trg_prevent_admin_delete " +
          "BEFORE DELETE ON users " +
          "FOR EACH ROW " +
          "EXECUTE FUNCTION fn_prevent_admin_delete()");

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
            "INSERT INTO users (username, password, email, phone_no, user_type) VALUES " +
                "('demo', 'demo123', 'demo@example.com', '1234567890', 'CUSTOMER'), " +
                "('superadmin', 'superadmin123', 'superadmin@example.com', '0000000000', 'ADMIN')");
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
