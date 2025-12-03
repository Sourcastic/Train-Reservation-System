package com.example.trainreservationsystem.utils.shared.payment.adapters;

import java.util.Map;

import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.UserRepository;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.shared.payment.PaymentAdapter;

/**
 * Adapter for Wallet payments using loyalty points.
 */
public class WalletPaymentAdapter implements PaymentAdapter {

  private final UserRepository userRepository;

  public WalletPaymentAdapter() {
    this.userRepository = RepositoryFactory.getUserRepository();
  }

  @Override
  public boolean processPayment(double amount, Map<String, String> details) {
    if (!UserSession.getInstance().isLoggedIn()) {
      return false;
    }

    int userId = UserSession.getInstance().getCurrentUser().getId();
    int loyaltyPoints = getLoyaltyPoints(userId);

    // Convert amount to points (1 point = $1)
    int requiredPoints = (int) Math.ceil(amount);

    if (loyaltyPoints < requiredPoints) {
      return false;
    }

    // Deduct loyalty points
    try {
      userRepository.deductLoyaltyPoints(userId, requiredPoints);
      return true;
    } catch (Exception e) {
      System.err.println("Error deducting loyalty points: " + e.getMessage());
      return false;
    }
  }

  @Override
  public String getMethodName() {
    return "Wallet (Loyalty Points)";
  }

  @Override
  public String validateDetails(Map<String, String> details) {
    if (!UserSession.getInstance().isLoggedIn()) {
      return "You must be logged in to use wallet payment";
    }

    int userId = UserSession.getInstance().getCurrentUser().getId();
    int loyaltyPoints = getLoyaltyPoints(userId);

    // Get amount from details if available
    String amountStr = details.get("amount");
    if (amountStr != null) {
      try {
        double amount = Double.parseDouble(amountStr);
        int requiredPoints = (int) Math.ceil(amount);

        if (loyaltyPoints < requiredPoints) {
          return String.format("Insufficient loyalty points. Required: %d, Available: %d",
              requiredPoints, loyaltyPoints);
        }
      } catch (NumberFormatException e) {
        return "Invalid amount";
      }
    }

    if (loyaltyPoints <= 0) {
      return "You have no loyalty points available";
    }

    return ""; // Valid
  }

  /**
   * Gets current loyalty points for the logged-in user.
   */
  public int getLoyaltyPoints(int userId) {
    try {
      var user = userRepository.getUserById(userId);
      return user != null ? user.getLoyaltyPoints() : 0;
    } catch (Exception e) {
      System.err.println("Error getting loyalty points: " + e.getMessage());
      return 0;
    }
  }
}
