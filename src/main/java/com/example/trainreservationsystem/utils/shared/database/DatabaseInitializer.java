package com.example.trainreservationsystem.utils.shared.database;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Initializes database tables.
 * Creates all required tables if they don't exist.
 */
public class DatabaseInitializer {

  public static boolean initialize() {
    try (Connection conn = Database.getConnection();
        Statement stmt = conn.createStatement()) {

      createTables(stmt);
      createStoredProcedures(stmt);
      applyMigrations(stmt);
      createIndexes(stmt);
      System.out.println("[SUCCESS] Database tables and procedures created successfully!");

      // Note: Seeding is now optional. Use SeedCommand to seed manually.
      // DatabaseSeeder.seed(); // Commented out - use SeedCommand instead
      return true;

    } catch (Exception e) {
      System.err.println("[ERROR] Database initialization error: " + e.getMessage());
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
    // New tables for route handling
    stmt.execute(createStopsTable());
    stmt.execute(createRouteSegmentsTable());
    stmt.execute(createSeatClassesTable());
    stmt.execute(createSeatsTable());
    stmt.execute(createStatisticsTable());
    stmt.execute(createDiscountsTable());
    stmt.execute(createCancellationPoliciesTable());
  }

  private static void createStoredProcedures(Statement stmt) throws Exception {
    stmt.execute(createAuthenticateUserFunction());
    stmt.execute(createRegisterUserProcedure());
    stmt.execute(createUpdatePasswordProcedure());
    stmt.execute(createUpdateUserProcedure());
  }

  /**
   * Creates database indexes on foreign keys and frequently queried columns.
   * This significantly improves query performance.
   */
  private static void createIndexes(Statement stmt) throws Exception {
    // Indexes on foreign keys
    createIndexIfNotExists(stmt, "idx_schedules_route_id", "schedules", "route_id");
    createIndexIfNotExists(stmt, "idx_bookings_user_id", "bookings", "user_id");
    createIndexIfNotExists(stmt, "idx_bookings_schedule_id", "bookings", "schedule_id");
    createIndexIfNotExists(stmt, "idx_passengers_booking_id", "passengers", "booking_id");
    createIndexIfNotExists(stmt, "idx_payments_booking_id", "payments", "booking_id");
    createIndexIfNotExists(stmt, "idx_payments_method_id", "payments", "payment_method_id");
    createIndexIfNotExists(stmt, "idx_payment_methods_user_id", "payment_methods", "user_id");
    createIndexIfNotExists(stmt, "idx_tickets_booking_id", "tickets", "booking_id");
    createIndexIfNotExists(stmt, "idx_complaints_user_id", "complaints", "user_id");
    createIndexIfNotExists(stmt, "idx_notifications_user_id", "notifications", "user_id");
    createIndexIfNotExists(stmt, "idx_seats_schedule_id", "seats", "schedule_id");
    createIndexIfNotExists(stmt, "idx_seats_seat_class_id", "seats", "seat_class_id");
    createIndexIfNotExists(stmt, "idx_route_segments_route_id", "route_segments", "route_id");
    createIndexIfNotExists(stmt, "idx_statistics_schedule_id", "statistics", "schedule_id");
    createIndexIfNotExists(stmt, "idx_discounts_schedule_id", "discounts", "schedule_id");
    createIndexIfNotExists(stmt, "idx_discounts_code", "discounts", "code");
    createIndexIfNotExists(stmt, "idx_cancellation_policies_active", "cancellation_policies", "is_active");

    // Indexes on frequently queried columns
    createIndexIfNotExists(stmt, "idx_bookings_status", "bookings", "status");
    createIndexIfNotExists(stmt, "idx_schedules_departure_date", "schedules", "departure_date");
    createIndexIfNotExists(stmt, "idx_routes_source_destination", "routes", "source, destination");
    createIndexIfNotExists(stmt, "idx_discounts_type", "discounts", "type");
    createIndexIfNotExists(stmt, "idx_discounts_is_active", "discounts", "is_active");
  }

  private static void createIndexIfNotExists(Statement stmt, String indexName, String tableName, String columns)
      throws Exception {
    try {
      String sql = "CREATE INDEX IF NOT EXISTS " + indexName + " ON " + tableName + " (" + columns + ")";
      stmt.execute(sql);
    } catch (Exception e) {
      // Index might already exist or table might not exist yet - ignore
      System.out.println("[INFO] Index creation note for " + indexName + ": " + e.getMessage());
    }
  }

  /**
   * Applies database migrations to update existing tables.
   * Handles ALTER TABLE statements for schema changes.
   */
  private static void applyMigrations(Statement stmt) throws Exception {
    try {
      // Migration: Increase payment_methods.method_type column size from VARCHAR(20)
      // to VARCHAR(50)
      stmt.execute(
          "DO $$ " +
              "BEGIN " +
              "  IF EXISTS (SELECT 1 FROM information_schema.columns " +
              "             WHERE table_name = 'payment_methods' AND column_name = 'method_type' " +
              "             AND character_maximum_length = 20) THEN " +
              "    ALTER TABLE payment_methods ALTER COLUMN method_type TYPE VARCHAR(50); " +
              "  END IF; " +
              "END $$;");

      // Migration: Add new columns to discounts table if they don't exist
      stmt.execute(
          "DO $$ " +
              "BEGIN " +
              "  IF NOT EXISTS (SELECT 1 FROM information_schema.columns " +
              "                 WHERE table_name = 'discounts' AND column_name = 'schedule_id') THEN " +
              "    ALTER TABLE discounts ADD COLUMN schedule_id INT REFERENCES schedules(id); " +
              "  END IF; " +
              "  IF NOT EXISTS (SELECT 1 FROM information_schema.columns " +
              "                 WHERE table_name = 'discounts' AND column_name = 'name') THEN " +
              "    ALTER TABLE discounts ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'Discount'; " +
              "  END IF; " +
              "  IF NOT EXISTS (SELECT 1 FROM information_schema.columns " +
              "                 WHERE table_name = 'discounts' AND column_name = 'type') THEN " +
              "    ALTER TABLE discounts ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'DISCOUNT_CODE' " +
              "      CHECK (type IN ('PROMO', 'VOUCHER', 'DISCOUNT_CODE')); " +
              "  END IF; " +
              "END $$;");
    } catch (Exception e) {
      // Migration failed - table might not exist yet or column already updated
      // This is okay, just log and continue
      System.out.println("[INFO] Migration note: " + e.getMessage());
    }
  }

  private static String createAuthenticateUserFunction() {
    return "CREATE OR REPLACE FUNCTION sp_authenticate_user(" +
        "p_email VARCHAR, " +
        "p_password VARCHAR) " +
        "RETURNS TABLE(id INT, username VARCHAR, email VARCHAR, user_type VARCHAR) " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "BEGIN " +
        "    RETURN QUERY " +
        "    SELECT u.id, u.username, u.email, u.user_type " +
        "    FROM users u " +
        "    WHERE u.email = p_email AND u.password = p_password; " +
        "END; " +
        "$$;";
  }

  private static String createRegisterUserProcedure() {
    return "CREATE OR REPLACE PROCEDURE sp_register_user(" +
        "p_username VARCHAR, " +
        "p_password VARCHAR, " +
        "p_email VARCHAR, " +
        "p_phone_no VARCHAR, " +
        "p_user_type VARCHAR) " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "BEGIN " +
        "    INSERT INTO users (username, password, email, phone_no, user_type, loyalty_points) " +
        "    VALUES (p_username, p_password, p_email, p_phone_no, p_user_type, 0); " +
        "END; " +
        "$$;";
  }

  private static String createUpdatePasswordProcedure() {
    return "CREATE OR REPLACE PROCEDURE sp_update_password(" +
        "p_email VARCHAR, " +
        "p_new_password VARCHAR) " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "BEGIN " +
        "    UPDATE users " +
        "    SET password = p_new_password " +
        "    WHERE email = p_email; " +
        "END; " +
        "$$;";
  }

  private static String createUpdateUserProcedure() {
    return "CREATE OR REPLACE PROCEDURE sp_update_user(" +
        "p_id INT, " +
        "p_username VARCHAR, " +
        "p_email VARCHAR, " +
        "p_phone_no VARCHAR) " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "BEGIN " +
        "    UPDATE users " +
        "    SET username = p_username, " +
        "        email = p_email, " +
        "        phone_no = p_phone_no " +
        "    WHERE id = p_id; " +
        "END; " +
        "$$;";
  }

  private static String createUsersTable() {
    return "CREATE TABLE IF NOT EXISTS users (" +
        "id SERIAL PRIMARY KEY, " +
        "username VARCHAR(50) UNIQUE NOT NULL, " +
        "password VARCHAR(255) NOT NULL, " +
        "email VARCHAR(100) NOT NULL, " +
        "phone_no VARCHAR(20), " +
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
        "price DECIMAL(10, 2) NOT NULL, " +
        "days_of_week VARCHAR(255))";
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
        "method_type VARCHAR(50) NOT NULL, " +
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

  // New tables
  private static String createStopsTable() {
    return "CREATE TABLE IF NOT EXISTS stops (" +
        "id SERIAL PRIMARY KEY, " +
        "name VARCHAR(100) NOT NULL)";
  }

  private static String createRouteSegmentsTable() {
    return "CREATE TABLE IF NOT EXISTS route_segments (" +
        "id SERIAL PRIMARY KEY, " +
        "route_id INT REFERENCES routes(id), " +
        "from_stop_id INT REFERENCES stops(id), " +
        "to_stop_id INT REFERENCES stops(id), " +
        "distance DOUBLE PRECISION NOT NULL, " +
        "price DOUBLE PRECISION NOT NULL)";
  }

  private static String createSeatClassesTable() {
    return "CREATE TABLE IF NOT EXISTS seat_classes (" +
        "id SERIAL PRIMARY KEY, " +
        "name VARCHAR(50) NOT NULL, " +
        "base_fare DOUBLE PRECISION NOT NULL, " +
        "description TEXT)";
  }

  private static String createSeatsTable() {
    return "CREATE TABLE IF NOT EXISTS seats (" +
        "id SERIAL PRIMARY KEY, " +
        "schedule_id INT REFERENCES schedules(id), " +
        "seat_class_id INT REFERENCES seat_classes(id), " +
        "is_booked BOOLEAN DEFAULT FALSE)";
  }

  private static String createStatisticsTable() {
    return "CREATE TABLE IF NOT EXISTS statistics (" +
        "id SERIAL PRIMARY KEY, " +
        "schedule_id INT REFERENCES schedules(id), " +
        "day_of_week VARCHAR(10) NOT NULL, " +
        "departure_time TIME NOT NULL, " +
        "seat_class_id INT REFERENCES seat_classes(id), " +
        "seats_sold INT NOT NULL)";
  }

  private static String createDiscountsTable() {
    return "CREATE TABLE IF NOT EXISTS discounts (" +
        "id SERIAL PRIMARY KEY, " +
        "schedule_id INT REFERENCES schedules(id), " +
        "name VARCHAR(100) NOT NULL, " +
        "code VARCHAR(50) UNIQUE NOT NULL, " +
        "type VARCHAR(20) NOT NULL CHECK (type IN ('PROMO', 'VOUCHER', 'DISCOUNT_CODE')), " +
        "description TEXT, " +
        "discount_percentage DECIMAL(5, 2) DEFAULT 0, " +
        "discount_amount DECIMAL(10, 2) DEFAULT 0, " +
        "valid_from DATE, " +
        "valid_to DATE, " +
        "is_active BOOLEAN DEFAULT true, " +
        "max_uses INT DEFAULT 0, " +
        "current_uses INT DEFAULT 0)";
  }

  private static String createCancellationPoliciesTable() {
    return "CREATE TABLE IF NOT EXISTS cancellation_policies (" +
        "id SERIAL PRIMARY KEY, " +
        "name VARCHAR(100) NOT NULL, " +
        "description TEXT, " +
        "hours_before_departure INT NOT NULL, " +
        "refund_percentage DECIMAL(5, 2) NOT NULL, " +
        "allow_cancellation BOOLEAN DEFAULT true, " +
        "min_hours_before_departure INT NOT NULL DEFAULT 0, " +
        "is_active BOOLEAN DEFAULT false)";
  }
}
