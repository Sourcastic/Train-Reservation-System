package com.example.trainreservationsystem.repositories.shared;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.shared.Complaint;
import com.example.trainreservationsystem.utils.shared.database.Database;

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
        complaints.add(mapResultSetToComplaint(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting complaints: " + e.getMessage());
      e.printStackTrace();
    }
    return complaints;
  }

  public List<Complaint> getAllComplaints() {
    List<Complaint> complaints = new ArrayList<>();
    String query = "SELECT * FROM complaints ORDER BY created_at DESC";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        complaints.add(mapResultSetToComplaint(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting all complaints: " + e.getMessage());
      e.printStackTrace();
    }
    return complaints;
  }

  public void saveComplaintResponse(int complaintId, String responseText, int staffId) {
    // For now, we'll send a notification to the user
    // In a full implementation, you might want to add a response column to
    // complaints table
    String query = "SELECT user_id FROM complaints WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, complaintId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        int userId = rs.getInt("user_id");
        // Send notification to user about the response
        com.example.trainreservationsystem.services.shared.NotificationService.getInstance()
            .add("Response to your complaint: " + responseText, userId);
      }
    } catch (Exception e) {
      System.err.println("Error saving complaint response: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to save complaint response", e);
    }
  }

  private Complaint mapResultSetToComplaint(ResultSet rs) throws Exception {
    Complaint complaint = new Complaint();
    complaint.setId(rs.getInt("id"));
    complaint.setUserId(rs.getInt("user_id"));
    complaint.setSubject(rs.getString("subject"));
    complaint.setDescription(rs.getString("description"));
    complaint.setTrackingId(rs.getString("tracking_id"));
    if (rs.getTimestamp("created_at") != null) {
      complaint.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    }
    return complaint;
  }
}
