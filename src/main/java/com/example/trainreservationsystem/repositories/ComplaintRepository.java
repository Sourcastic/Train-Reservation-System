package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.shared.Complaint;
import com.example.trainreservationsystem.utils.shared.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ComplaintRepository {

  public void saveComplaint(Complaint complaint) {
    String query = "INSERT INTO complaints (user_id, subject, description, tracking_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, complaint.getUserId());
      stmt.setString(2, complaint.getSubject());
      stmt.setString(3, complaint.getDescription());
      stmt.setString(4, complaint.getTrackingId());
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error saving complaint: " + e.getMessage());
      e.printStackTrace();
        throw new RuntimeException("Failed to save complaint", e);
    }
  }

    public List<Complaint> getComplaintsByUserId(int userId) {
        List<Complaint> complaints = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Complaint complaint = new Complaint();
                complaint.setUserId(rs.getInt("user_id"));
                complaint.setSubject(rs.getString("subject"));
                complaint.setDescription(rs.getString("description"));
                complaint.setTrackingId(rs.getString("tracking_id"));
                complaints.add(complaint);
            }
        } catch (Exception e) {
            System.err.println("Error getting complaints: " + e.getMessage());
            e.printStackTrace();
    }
        return complaints;
  }
}
