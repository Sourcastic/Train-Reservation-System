package com.example.trainreservationsystem.utils.database;

import com.example.trainreservationsystem.seeders.DatabaseSeeder;

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
      System.out.println("✅ Database tables and procedures created successfully!");

      DatabaseSeeder.seed();
      return true;

    } catch (Exception e) {
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
    // New tables for route handling
    stmt.execute(createStopsTable());
    stmt.execute(createRouteSegmentsTable());
    stmt.execute(createSeatClassesTable());
    stmt.execute(createSeatsTable());
    stmt.execute(createStatisticsTable());
  }

  private static void createStoredProcedures(Statement stmt) throws Exception {
    stmt.execute(createAuthenticateUserFunction());
    stmt.execute(createRegisterUserProcedure());
    stmt.execute(createUpdatePasswordProcedure());
    stmt.execute(createUpdateUserProcedure());
    stmt.execute(createAddRouteWithSegmentsProcedure());
    stmt.execute(createAddScheduleWithSeatsProcedure());
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

  private static String createAddRouteWithSegmentsProcedure() {
    return "CREATE OR REPLACE FUNCTION sp_add_route_with_segments(" +
        "p_name VARCHAR, " +
        "p_source VARCHAR, " +
        "p_destination VARCHAR, " +
        "p_segments JSONB) " +
        "RETURNS INT " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "DECLARE " +
        "  v_route_id INT; " +
        "  v_segment JSONB; " +
        "BEGIN " +
        "  INSERT INTO routes (name, source, destination) " +
        "  VALUES (p_name, p_source, p_destination) " +
        "  RETURNING id INTO v_route_id; " +
        "  " +
        "  FOR v_segment IN SELECT * FROM jsonb_array_elements(p_segments) " +
        "  LOOP " +
        "    INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance, price) " +
        "    VALUES (v_route_id, " +
        "            (v_segment->>'from_stop_id')::INT, " +
        "            (v_segment->>'to_stop_id')::INT, " +
        "            (v_segment->>'distance')::DOUBLE PRECISION, " +
        "            (v_segment->>'price')::DOUBLE PRECISION); " +
        "  END LOOP; " +
        "  " +
        "  RETURN v_route_id; " +
        "END; " +
        "$$;";
  }

  private static String createAddScheduleWithSeatsProcedure() {
    return "CREATE OR REPLACE FUNCTION sp_add_schedule_with_seats(" +
        "p_route_id INT, " +
        "p_departure_date DATE, " +
        "p_departure_time TIME, " +
        "p_arrival_time TIME, " +
        "p_price DECIMAL, " +
        "p_capacity INT, " +
        "p_days_of_week VARCHAR, " +
        "p_seats JSONB) " +
        "RETURNS INT " +
        "LANGUAGE plpgsql " +
        "AS $$ " +
        "DECLARE " +
        "  v_schedule_id INT; " +
        "  v_seat_class JSONB; " +
        "  v_count INT; " +
        "BEGIN " +
        "  INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, price, capacity, days_of_week) "
        +
        "  VALUES (p_route_id, p_departure_date, p_departure_time, p_arrival_time, p_price, p_capacity, p_days_of_week) "
        +
        "  RETURNING id INTO v_schedule_id; " +
        "  " +
        "  FOR v_seat_class IN SELECT * FROM jsonb_array_elements(p_seats) " +
        "  LOOP " +
        "    FOR v_count IN 1..(v_seat_class->>'quantity')::INT " +
        "    LOOP " +
        "      INSERT INTO seats (schedule_id, seat_class_id, is_booked) " +
        "      VALUES (v_schedule_id, (v_seat_class->>'seat_class_id')::INT, false); " +
        "    END LOOP; " +
        "  END LOOP; " +
        "  " +
        "  RETURN v_schedule_id; " +
        "END; " +
        "$$;";
  }
}
