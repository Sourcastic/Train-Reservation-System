package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffComplaintRepository {

    // Fetch all complaints along with any staff response (if exists)
    public List<Complaint> getAllComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        String query = """
        SELECT c.id, c.user_id, c.subject, c.description, c.tracking_id, c.created_at,
               sr.response, sr.staff_id, sr.responded_at
        FROM complaints c
        LEFT JOIN staff_responses sr ON c.id = sr.complaint_id
        ORDER BY c.created_at DESC
        """;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Complaint c = new Complaint();
                c.setId(rs.getInt("id"));
                c.setUserId(rs.getInt("user_id"));
                c.setSubject(rs.getString("subject"));
                c.setDescription(rs.getString("description"));
                c.setTrackingId(rs.getString("tracking_id"));
                c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                // Optional staff response
                String response = rs.getString("response");
                if (response != null) {
                    c.setStaffResponse(response);
                    c.setStaffId(rs.getInt("staff_id"));
                    Timestamp respondedAt = rs.getTimestamp("responded_at");
                    if (respondedAt != null) {
                        c.setRespondedAt(respondedAt.toLocalDateTime());
                    }
                }

                complaints.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return complaints;
    }

    // Save staff response for a complaint
    public void saveStaffResponse(int complaintId, String response, String responderName) {
        String insertQuery = "INSERT INTO staff_responses (complaint_id, staff_id, response) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // For simplicity, you can set a dummy staff_id (or modify to get actual staff ID)
            int staffId = 1; // TODO: Replace with actual logged-in staff ID if needed
            pstmt.setInt(1, complaintId);
            pstmt.setInt(2, staffId);
            pstmt.setString(3, response);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
