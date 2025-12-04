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
                        rs.getInt("seat_id"),
                        rs.getString("qr_code"),
                        rs.getString("status")));
            }
        } catch (Exception e) {
            System.err.println("Error getting tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getTicketsByBookingId(int bookingId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE booking_id = ? ORDER BY seat_id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(new Ticket(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("seat_id"),
                        rs.getString("qr_code"),
                        rs.getString("status")));
            }
        } catch (Exception e) {
            System.err.println("Error getting tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public void saveTicket(Ticket ticket) {
        String query = "INSERT INTO tickets (booking_id, seat_id, qr_code, status, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ticket.getBookingId());
            stmt.setInt(2, ticket.getSeatId());
            stmt.setString(3, ticket.getQrCode());
            stmt.setString(4, ticket.getStatus());
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

    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        // Join with bookings to get amount information
        String query = "SELECT t.*, b.total_amount FROM tickets t " +
                "LEFT JOIN bookings b ON t.booking_id = b.id " +
                "ORDER BY t.created_at DESC";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ticket ticket = new Ticket(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("seat_id"),
                        rs.getString("qr_code"),
                        rs.getString("status"));
                // Store amount in a way we can access it - we'll need to extend Ticket model or
                // use a wrapper
                // For now, we'll fetch it separately in the controller
                tickets.add(ticket);
            }
        } catch (Exception e) {
            System.err.println("Error getting all tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    /**
     * Gets ticket with booking amount for display purposes.
     * Returns a map of ticketId -> amount for efficient lookup.
     */
    public java.util.Map<Integer, Double> getTicketAmounts() {
        java.util.Map<Integer, Double> amounts = new java.util.HashMap<>();
        String query = "SELECT t.id, b.total_amount FROM tickets t " +
                "LEFT JOIN bookings b ON t.booking_id = b.id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int ticketId = rs.getInt("id");
                double amount = rs.getDouble("total_amount");
                if (!rs.wasNull()) {
                    amounts.put(ticketId, amount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting ticket amounts: " + e.getMessage());
            e.printStackTrace();
        }
        return amounts;
    }

    public Ticket getTicketById(int ticketId) {
        String query = "SELECT * FROM tickets WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Ticket(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("seat_id"),
                        rs.getString("qr_code"),
                        rs.getString("status"));
            }
        } catch (Exception e) {
            System.err.println("Error getting ticket by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
