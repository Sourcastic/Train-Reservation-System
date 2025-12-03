package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.shared.Seat;
import com.example.trainreservationsystem.models.admin.SeatClass;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class SeatRepository {

    public Seat addSeat(Seat seat, int scheduleId) throws Exception {
        String sql = "INSERT INTO seats (schedule_id, seat_class_id, is_booked) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, scheduleId);
            stmt.setInt(2, seat.getSeatClass().getId());
            stmt.setBoolean(3, seat.isBooked());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating seat failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seat.setId(generatedKeys.getInt(1));
                } else {
                    throw new Exception("Creating seat failed, no ID obtained.");
                }
            }
        }
        return seat;
    }

    public List<Seat> getSeatsByScheduleId(int scheduleId) throws Exception {
        List<Integer> scheduleIds = new ArrayList<>();
        scheduleIds.add(scheduleId);
        java.util.Map<Integer, List<Seat>> result = getSeatsByScheduleIds(scheduleIds);
        return result.getOrDefault(scheduleId, new ArrayList<>());
    }

    /**
     * Batch loads seats for multiple schedules with JOIN to avoid N+1 queries.
     * Returns a map of scheduleId -> list of seats.
     */
    public java.util.Map<Integer, List<Seat>> getSeatsByScheduleIds(List<Integer> scheduleIds) throws Exception {
        java.util.Map<Integer, List<Seat>> seatsBySchedule = new java.util.HashMap<>();
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return seatsBySchedule;
        }

        // Use JOIN to fetch seat classes in a single query (fixes N+1)
        String placeholders = scheduleIds.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "SELECT s.*, sc.id as seat_class_id, sc.name as seat_class_name, " +
                "sc.base_fare, sc.description as seat_class_description " +
                "FROM seats s " +
                "LEFT JOIN seat_classes sc ON s.seat_class_id = sc.id " +
                "WHERE s.schedule_id IN (" + placeholders + ") " +
                "ORDER BY s.schedule_id, s.id";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < scheduleIds.size(); i++) {
                stmt.setInt(i + 1, scheduleIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int scheduleId = rs.getInt("schedule_id");

                    // Build seat class from JOIN result
                    SeatClass seatClass = null;
                    if (rs.getInt("seat_class_id") > 0) {
                        seatClass = new SeatClass(
                                rs.getInt("seat_class_id"),
                                rs.getString("seat_class_name"),
                                rs.getDouble("base_fare"),
                                rs.getString("seat_class_description"));
                    }

                    Seat seat = new Seat(rs.getInt("id"), seatClass);
                    seat.setBooked(rs.getBoolean("is_booked"));

                    seatsBySchedule.computeIfAbsent(scheduleId, k -> new ArrayList<>()).add(seat);
                }
            }
        }
        return seatsBySchedule;
    }

    public void updateSeatStatus(int seatId, boolean isBooked) throws Exception {
        String sql = "UPDATE seats SET is_booked = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isBooked);
            stmt.setInt(2, seatId);
            stmt.executeUpdate();
        }
    }

    public void deleteSeat(int id) throws Exception {
        String sql = "DELETE FROM seats WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
