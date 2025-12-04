package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.trainreservationsystem.models.admin.SeatClass;
import com.example.trainreservationsystem.repositories.admin.SeatClassRepository;

/**
 * Seeds default seat classes (Economy, Business, First Class).
 */
public class SeatClassSeeder {

    public static boolean seed(Connection conn, boolean force) {
        try {
            // Check if seat classes already exist
            String checkQuery = "SELECT COUNT(*) FROM seat_classes";
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0 && !force) {
                    System.out.println("[INFO] Seat classes already seeded, skipping...");
                    return false;
                }
            }

            System.out.println("[INFO] Seeding seat classes...");
            SeatClassRepository repo = new SeatClassRepository();

            // Economy Class - baseline pricing
            SeatClass economy = new SeatClass();
            economy.setName("Economy");
            economy.setBaseFare(0.0); // No additional fare
            economy.setDescription("Standard comfortable seating with basic amenities");
            repo.addSeatClass(economy);
            System.out.println("  ✓ Added Economy class");

            // Business Class - moderate premium
            SeatClass business = new SeatClass();
            business.setName("Business");
            business.setBaseFare(500.0); // Additional PKR 500
            business.setDescription("Premium seating with extra legroom and complimentary refreshments");
            repo.addSeatClass(business);
            System.out.println("  ✓ Added Business class");

            // First Class - highest premium
            SeatClass firstClass = new SeatClass();
            firstClass.setName("First Class");
            firstClass.setBaseFare(1000.0); // Additional PKR 1000
            firstClass.setDescription("Luxury seating with private cabins, fine dining, and concierge service");
            repo.addSeatClass(firstClass);
            System.out.println("  ✓ Added First Class");

            System.out.println("[SUCCESS] Seat classes seeded successfully!");
            return true;

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to seed seat classes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
