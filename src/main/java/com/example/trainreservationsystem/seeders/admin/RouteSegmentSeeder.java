package com.example.trainreservationsystem.seeders.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds route segment data into the database.
 * Only seeds if the route_segments table is empty.
 */
public class RouteSegmentSeeder {

    /**
     * Seeds route segments into the database.
     *
     * @param conn Database connection
     * @return true if data was seeded, false if table already had data
     */
    public static boolean seed(Connection conn) {
        return seed(conn, false);
    }

    /**
     * Seeds route segments into the database.
     *
     * @param conn  Database connection
     * @param force If true, seeds even if data already exists
     * @return true if data was seeded, false if table already had data (and
     *         force=false)
     */
    public static boolean seed(Connection conn, boolean force) {
        try (Statement stmt = conn.createStatement()) {
            // Check if route_segments table is empty (unless forcing)
            if (!force) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM route_segments");
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Skip silently in normal mode
                }
            }

            // Stops IDs (assumed based on StationSeeder order):
            // 1: Karachi, 2: Lahore, 3: Islamabad, 4: Rawalpindi, 5: Peshawar
            // 6: Quetta, 7: Multan, 8: Faisalabad, 9: Hyderabad, 10: Sukkur

            // Routes IDs (assumed based on RouteSeeder order):
            // 1: Karachi -> Rawalpindi
            // 2: Karachi -> Islamabad
            // 3: Karachi -> Peshawar
            // 4: Quetta -> Peshawar
            // 5: Karachi -> Lahore

            StringBuilder sql = new StringBuilder(
                    "INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance, price) VALUES ");

            // Route 1: Karachi -> Rawalpindi
            // Karachi(1) -> Hyderabad(9) -> Multan(7) -> Lahore(2) -> Rawalpindi(4)
            addSegment(sql, 1, 1, 9, 160, 500);
            addSegment(sql, 1, 9, 7, 700, 1500);
            addSegment(sql, 1, 7, 2, 350, 800);
            addSegment(sql, 1, 2, 4, 300, 700);

            // Route 2: Karachi -> Islamabad
            // Karachi(1) -> Hyderabad(9) -> Multan(7) -> Lahore(2) -> Islamabad(3)
            addSegment(sql, 2, 1, 9, 160, 500);
            addSegment(sql, 2, 9, 7, 700, 1500);
            addSegment(sql, 2, 7, 2, 350, 800);
            addSegment(sql, 2, 2, 3, 310, 750);

            // Route 3: Karachi -> Peshawar
            // Karachi(1) -> Hyderabad(9) -> Multan(7) -> Lahore(2) -> Rawalpindi(4) ->
            // Peshawar(5)
            addSegment(sql, 3, 1, 9, 160, 500);
            addSegment(sql, 3, 9, 7, 700, 1500);
            addSegment(sql, 3, 7, 2, 350, 800);
            addSegment(sql, 3, 2, 4, 300, 700);
            addSegment(sql, 3, 4, 5, 180, 400);

            // Route 4: Quetta -> Peshawar
            // Quetta(6) -> Sukkur(10) -> Multan(7) -> Lahore(2) -> Rawalpindi(4) ->
            // Peshawar(5)
            addSegment(sql, 4, 6, 10, 390, 1000);
            addSegment(sql, 4, 10, 7, 450, 1100);
            addSegment(sql, 4, 7, 2, 350, 800);
            addSegment(sql, 4, 2, 4, 300, 700);
            addSegment(sql, 4, 4, 5, 180, 400);

            // Route 5: Karachi -> Lahore
            // Karachi(1) -> Hyderabad(9) -> Sukkur(10) -> Multan(7) -> Faisalabad(8) ->
            // Lahore(2)
            addSegment(sql, 5, 1, 9, 160, 500);
            addSegment(sql, 5, 9, 10, 330, 800);
            addSegment(sql, 5, 10, 7, 450, 1100);
            addSegment(sql, 5, 7, 8, 250, 600);
            addSegment(sql, 5, 8, 2, 180, 400);

            // Remove trailing comma and execute
            String finalSql = sql.toString().replaceAll(",\\s*$", "");
            stmt.executeUpdate(finalSql);

            System.out.println("✅ Seeded route segments for 5 routes");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error seeding route segments: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void addSegment(StringBuilder sql, int routeId, int fromStopId, int toStopId, double distance,
            double price) {
        sql.append(String.format(
                "(%d, %d, %d, %.2f, %.2f), ",
                routeId, fromStopId, toStopId, distance, price));
    }
}
