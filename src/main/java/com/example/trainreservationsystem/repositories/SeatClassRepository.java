package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.admin.SeatClass;
import com.example.trainreservationsystem.utils.shared.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SeatClassRepository {

    public SeatClass addSeatClass(SeatClass seatClass) throws Exception {
        String sql = "INSERT INTO seat_classes (name, base_fare, description) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, seatClass.getName());
            stmt.setDouble(2, seatClass.getBaseFare());
            stmt.setString(3, seatClass.getDescription());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating seat class failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seatClass.setId(generatedKeys.getInt(1));
                } else {
                    throw new Exception("Creating seat class failed, no ID obtained.");
                }
            }
        }
        return seatClass;
    }

    public List<SeatClass> getAllSeatClasses() throws Exception {
        List<SeatClass> seatClasses = new ArrayList<>();
        String sql = "SELECT * FROM seat_classes";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                seatClasses.add(new SeatClass(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("base_fare"),
                        rs.getString("description")));
            }
        }
        return seatClasses;
    }

    public SeatClass getSeatClassById(int id) throws Exception {
        String sql = "SELECT * FROM seat_classes WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new SeatClass(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("base_fare"),
                            rs.getString("description"));
                }
            }
        }
        return null;
    }

    public void updateSeatClass(SeatClass seatClass) throws Exception {
        String sql = "UPDATE seat_classes SET name = ?, base_fare = ?, description = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, seatClass.getName());
            stmt.setDouble(2, seatClass.getBaseFare());
            stmt.setString(3, seatClass.getDescription());
            stmt.setInt(4, seatClass.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteSeatClass(int id) throws Exception {
        String sql = "DELETE FROM seat_classes WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
