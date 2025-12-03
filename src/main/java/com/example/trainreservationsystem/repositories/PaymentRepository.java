package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Payment;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.utils.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

  public List<PaymentMethod> getPaymentMethods(int userId) {
    List<PaymentMethod> methods = new ArrayList<>();
    String query = "SELECT * FROM payment_methods WHERE user_id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        methods.add(new PaymentMethod(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("method_type"),
            rs.getString("details")));
      }
    } catch (Exception e) {
      System.err.println("Error getting payment methods: " + e.getMessage());
      e.printStackTrace();
    }
    return methods;
  }

  public void savePaymentMethod(PaymentMethod method) {
    String query = "INSERT INTO payment_methods (user_id, method_type, details) VALUES (?, ?, ?) RETURNING id";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, method.getUserId());
      stmt.setString(2, method.getMethodType());
      stmt.setString(3, method.getDetails());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        method.setId(rs.getInt(1));
      }
    } catch (Exception e) {
      System.err.println("Error saving payment method: " + e.getMessage());
      e.printStackTrace();
        throw new RuntimeException("Failed to save payment method", e);
    }
  }

  public void savePayment(Payment payment) {
    String query = "INSERT INTO payments (booking_id, amount, payment_method_id, status, payment_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, payment.getBookingId());
      stmt.setDouble(2, payment.getAmount());
      stmt.setInt(3, payment.getPaymentMethodId());
      stmt.setString(4, payment.getStatus());
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error saving payment: " + e.getMessage());
      e.printStackTrace();
        throw new RuntimeException("Failed to save payment", e);
    }
  }

    public List<Payment> getPaymentsByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT p.* FROM payments p " +
                "JOIN bookings b ON p.booking_id = b.id " +
                "WHERE b.user_id = ? ORDER BY p.payment_date DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMethodId(rs.getInt("payment_method_id"));
                payment.setStatus(rs.getString("status"));
                payments.add(payment);
            }
        } catch (Exception e) {
            System.err.println("Error getting payments: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
  }
}
