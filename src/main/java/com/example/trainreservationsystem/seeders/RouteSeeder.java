package com.example.trainreservationsystem.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds route data into the database.
 * Includes Stops, Routes, and Route Segments.
 */
public class RouteSeeder {

  public static boolean seed(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      // Check if routes table is empty
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM routes");
      if (rs.next() && rs.getInt(1) > 0) {
        System.out.println("ℹ️  Routes/Stops already seeded, skipping.");
        return false;
      }

      System.out.println("🌱 Seeding Stops, Routes, and Segments...");

      // 1. Seed Stops
      seedStops(conn);

      // 2. Seed Routes & Segments
      // Tezgam: Karachi -> Rawalpindi
      seedRoute(conn, "Tezgam", "Karachi", "Rawalpindi", new String[][] {
          { "Karachi", "Hyderabad", "165", "600" },
          { "Hyderabad", "Rohri", "310", "1200" },
          { "Rohri", "Multan", "360", "1500" },
          { "Multan", "Lahore", "335", "1400" },
          { "Lahore", "Rawalpindi", "290", "1100" }
      });

      // Green Line: Karachi -> Islamabad (Express)
      seedRoute(conn, "Green Line", "Karachi", "Islamabad", new String[][] {
          { "Karachi", "Rohri", "475", "2500" },
          { "Rohri", "Lahore", "695", "3500" },
          { "Lahore", "Islamabad", "380", "1500" }
      });

      // Khyber Mail: Karachi -> Peshawar
      seedRoute(conn, "Khyber Mail", "Karachi", "Peshawar", new String[][] {
          { "Karachi", "Hyderabad", "165", "500" },
          { "Hyderabad", "Multan", "670", "1800" },
          { "Multan", "Lahore", "335", "1200" },
          { "Lahore", "Rawalpindi", "290", "1000" },
          { "Rawalpindi", "Peshawar", "170", "600" }
      });

      // Jaffer Express: Quetta -> Peshawar
      seedRoute(conn, "Jaffer Express", "Quetta", "Peshawar", new String[][] {
          { "Quetta", "Sibi", "160", "500" },
          { "Sibi", "Sukkur", "240", "800" },
          { "Sukkur", "Multan", "450", "1400" },
          { "Multan", "Rawalpindi", "625", "2000" },
          { "Rawalpindi", "Peshawar", "170", "600" }
      });

      System.out.println("✅ Seeded Stops, Routes, and Segments");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Error seeding routes: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private static void seedStops(Connection conn) throws Exception {
    String[] stops = {
        "Karachi", "Hyderabad", "Rohri", "Multan", "Lahore",
        "Rawalpindi", "Islamabad", "Peshawar", "Quetta", "Sibi", "Sukkur"
    };

    String sql = "INSERT INTO stops (name) VALUES (?)";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      for (String stop : stops) {
        pstmt.setString(1, stop);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
    }
    System.out.println("   - Seeded " + stops.length + " stops");
  }

  private static void seedRoute(Connection conn, String name, String source, String destination, String[][] segments)
      throws Exception {
    // Insert Route
    int routeId;
    String insertRouteSql = "INSERT INTO routes (name, source, destination) VALUES (?, ?, ?) RETURNING id";
    try (PreparedStatement pstmt = conn.prepareStatement(insertRouteSql)) {
      pstmt.setString(1, name);
      pstmt.setString(2, source);
      pstmt.setString(3, destination);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        routeId = rs.getInt(1);
      } else {
        throw new Exception("Failed to retrieve route ID for " + name);
      }
    }

    // Insert Segments
    String insertSegmentSql = "INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance, price) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = conn.prepareStatement(insertSegmentSql)) {
      for (String[] segment : segments) {
        int fromId = getStopId(conn, segment[0]);
        int toId = getStopId(conn, segment[1]);
        double distance = Double.parseDouble(segment[2]);
        double price = Double.parseDouble(segment[3]);

        pstmt.setInt(1, routeId);
        pstmt.setInt(2, fromId);
        pstmt.setInt(3, toId);
        pstmt.setDouble(4, distance);
        pstmt.setDouble(5, price);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
    }
  }

  private static int getStopId(Connection conn, String stopName) throws Exception {
    String sql = "SELECT id FROM stops WHERE name = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, stopName);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("id");
      } else {
        throw new Exception("Stop not found: " + stopName);
      }
    }
  }
}
