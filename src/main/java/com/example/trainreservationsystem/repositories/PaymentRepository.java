package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Payment;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.utils.database.Database;

public class PaymentRepository {
  private static List<PaymentMethod> mockPaymentMethods = new ArrayList<>();
  private static int mockMethodIdCounter = 1;

  public PaymentRepository() {
    // Initialize with a default payment method for mock mode
    if (mockPaymentMethods.isEmpty()) {
      mockPaymentMethods.add(new PaymentMethod(1, 1, "VISA", "**** 1234"));
    }
  }

  public List<PaymentMethod> getPaymentMethods(int userId) {
    if (Database.isMockMode()) {
      List<PaymentMethod> userMethods = new ArrayList<>();
      for (PaymentMethod pm : mockPaymentMethods) {
        if (pm.getUserId() == userId) {
          userMethods.add(pm);
        }
      }
      return userMethods;
    }

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
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return getPaymentMethods(userId); // Retry with mock
      }
      System.err.println("Error getting payment methods: " + e.getMessage());
      e.printStackTrace();
      return getPaymentMethods(userId); // Fallback to mock
    }
    return methods;
  }

  public void savePaymentMethod(PaymentMethod method) {
    if (Database.isMockMode()) {
      method.setId(mockMethodIdCounter++);
      mockPaymentMethods.add(method);
      return;
    }

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
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        savePaymentMethod(method); // Retry with mock
        return;
      }
      System.err.println("Error saving payment method: " + e.getMessage());
      e.printStackTrace();
      savePaymentMethod(method); // Fallback to mock
    }
  }

  public void savePayment(Payment payment) {
    if (Database.isMockMode()) {
      System.out.println("Mock: Processed payment for booking " + payment.getBookingId());
      return;
    }

    String query = "INSERT INTO payments (booking_id, amount, payment_method_id, status, payment_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, payment.getBookingId());
      stmt.setDouble(2, payment.getAmount());
      stmt.setInt(3, payment.getPaymentMethodId());
      stmt.setString(4, payment.getStatus());
      stmt.executeUpdate();
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        savePayment(payment); // Retry with mock
        return;
      }
      System.err.println("Error saving payment: " + e.getMessage());
      e.printStackTrace();
      savePayment(payment); // Fallback to mock
    }
  }
}
