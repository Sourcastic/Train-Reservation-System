package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Seat;
import com.example.trainreservationsystem.models.SeatClass;
import com.example.trainreservationsystem.utils.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SeatRepository {

    private final SeatClassRepository seatClassRepository = new SeatClassRepository();

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
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.*, sc.id as class_id, sc.name as class_name, " +
                "sc.base_fare, sc.description " +
                "FROM seats s " +
                "JOIN seat_classes sc ON s.seat_class_id = sc.id " +
                "WHERE s.schedule_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SeatClass seatClass = new SeatClass(
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            rs.getDouble("base_fare"),
                            rs.getString("description"));

                    Seat seat = new Seat(
                            rs.getInt("id"),
                            seatClass);
                    seat.setBooked(rs.getBoolean("is_booked"));
                    seats.add(seat);
                }
            }
        }
        return seats;
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
