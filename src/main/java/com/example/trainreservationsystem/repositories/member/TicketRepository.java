package com.example.trainreservationsystem.repositories.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class TicketRepository {

    public List<Ticket> getTicketsByUserId(int userId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT t.* FROM tickets t " +
                "JOIN bookings b ON t.booking_id = b.id " +
                "WHERE b.user_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(new Ticket(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getString("qr_code"),
                        rs.getString("status")));
            }
        } catch (Exception e) {
            System.err.println("Error getting tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public Ticket getTicketByBookingId(int bookingId) {
        String query = "SELECT * FROM tickets WHERE booking_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Ticket(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getString("qr_code"),
                        rs.getString("status"));
            }
        } catch (Exception e) {
            System.err.println("Error getting ticket: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void saveTicket(Ticket ticket) {
        String query = "INSERT INTO tickets (booking_id, qr_code, status, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ticket.getBookingId());
            stmt.setString(2, ticket.getQrCode());
            stmt.setString(3, ticket.getStatus());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ticket.setId(rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("Error saving ticket: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save ticket", e);
        }
    }

    public boolean updateTicketStatus(int ticketId, String status) {
        String query = "UPDATE tickets SET status = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, ticketId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating ticket status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
