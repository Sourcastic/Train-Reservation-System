package com.example.trainreservationsystem.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds seat class data into the database.
 */
public class SeatClassSeeder {

    public static boolean seed(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check if seat_classes table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM seat_classes");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("ℹ️  Seat Classes already seeded, skipping.");
                return false;
            }

            System.out.println("🌱 Seeding Seat Classes...");

            String sql = "INSERT INTO seat_classes (name, base_fare, description) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Business
                pstmt.setString(1, "Business");
                pstmt.setDouble(2, 1500.0);
                pstmt.setString(3, "Premium service with extra legroom and meals.");
                pstmt.addBatch();

                // First Class
                pstmt.setString(1, "First Class");
                pstmt.setDouble(2, 2500.0);
                pstmt.setString(3, "Luxury cabins with full amenities.");
                pstmt.addBatch();

                // Economy
                pstmt.setString(1, "Economy");
                pstmt.setDouble(2, 500.0);
                pstmt.setString(3, "Standard seating for budget travel.");
                pstmt.addBatch();

                pstmt.executeBatch();
            }

            System.out.println("✅ Seeded Seat Classes");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error seeding seat classes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
