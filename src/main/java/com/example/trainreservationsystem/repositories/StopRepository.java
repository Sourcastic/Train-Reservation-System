package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.admin.Stop;
import com.example.trainreservationsystem.utils.shared.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StopRepository {

    public Stop addStop(Stop stop) throws Exception {
        String sql = "INSERT INTO stops (name) VALUES (?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, stop.getName());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating stop failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    stop.setId(generatedKeys.getInt(1));
                } else {
                    throw new Exception("Creating stop failed, no ID obtained.");
                }
            }
        }
        return stop;
    }

    public List<Stop> getAllStops() throws Exception {
        List<Stop> stops = new ArrayList<>();
        String sql = "SELECT * FROM stops";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stops.add(new Stop(rs.getInt("id"), rs.getString("name")));
            }
        }
        return stops;
    }

    public Stop getStopById(int id) throws Exception {
        String sql = "SELECT * FROM stops WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Stop(rs.getInt("id"), rs.getString("name"));
                }
            }
        }
        return null;
    }

    public void updateStop(Stop stop) throws Exception {
        String sql = "UPDATE stops SET name = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stop.getName());
            stmt.setInt(2, stop.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteStop(int id) throws Exception {
        String sql = "DELETE FROM stops WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
