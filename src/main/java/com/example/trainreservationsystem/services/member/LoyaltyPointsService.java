package com.example.trainreservationsystem.services.member;

import com.example.trainreservationsystem.repositories.UserRepository;

/**
 * Service for managing loyalty points.
 * Grants 10% of booking total price as loyalty points.
 */
public class LoyaltyPointsService {
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public LoyaltyPointsService(UserRepository userRepository, NotificationService notificationService) {
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  /**
   * Grants loyalty points to a user based on booking total.
   * Awards 10% of the total booking price as loyalty points.
   *
   * @param userId       The user ID to grant points to
   * @param bookingTotal The total booking amount
   * @return The number of loyalty points granted
   */
  public int grantLoyaltyPoints(int userId, double bookingTotal) {
    try {
      // Calculate 10% of booking total, rounded to nearest integer
      int pointsToGrant = (int) Math.round(bookingTotal * 0.10);

      if (pointsToGrant > 0) {
        // Add points to user's account
        userRepository.addLoyaltyPoints(userId, pointsToGrant);

        // Notify user via observer pattern
        String message = String.format("You've earned %d loyalty points! (10%% of your booking total: $%.2f)",
            pointsToGrant, bookingTotal);
        notificationService.add(message, userId);

        System.out.println("[SUCCESS] Granted " + pointsToGrant + " loyalty points to user " + userId);
        return pointsToGrant;
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Error granting loyalty points: " + e.getMessage());
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * Gets current loyalty points for a user.
   */
  public int getLoyaltyPoints(int userId) {
    try {
      var user = userRepository.getUserById(userId);
      return user != null ? user.getLoyaltyPoints() : 0;
    } catch (Exception e) {
      System.err.println("Error getting loyalty points: " + e.getMessage());
      e.printStackTrace();
      return 0;
    }
  }
}
