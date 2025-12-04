package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds station (stop) data into the database.
 * Only seeds if the stops table is empty.
 */
public class StationSeeder {

    /**
     * Seeds stations into the database.
     *
     * @param conn Database connection
     * @return true if data was seeded, false if table already had data
     */
    public static boolean seed(Connection conn) {
        return seed(conn, false);
    }

    /**
     * Seeds stations into the database.
     *
     * @param conn  Database connection
     * @param force If true, seeds even if data already exists
     * @return true if data was seeded, false if table already had data (and
     *         force=false)
     */
    public static boolean seed(Connection conn, boolean force) {
        try (Statement stmt = conn.createStatement()) {
            // Check if stops table is empty (unless forcing)
            if (!force) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM stops");
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Skip silently in normal mode
                }
            }

            // Seed Pakistani cities
            stmt.executeUpdate(
                    "INSERT INTO stops (name) VALUES " +
                            "('Karachi'), " +
                            "('Lahore'), " +
                            "('Islamabad'), " +
                            "('Rawalpindi'), " +
                            "('Peshawar'), " +
                            "('Quetta'), " +
                            "('Multan'), " +
                            "('Faisalabad'), " +
                            "('Hyderabad'), " +
                            "('Sukkur')");

            System.out.println("✅ Seeded 10 stations (Pakistani cities)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error seeding stations: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
