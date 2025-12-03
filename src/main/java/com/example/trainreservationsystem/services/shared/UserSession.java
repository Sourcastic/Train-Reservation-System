package com.example.trainreservationsystem.services.shared;

import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.shared.User;

public class UserSession {
  private static UserSession instance;
  private User currentUser;
  private Schedule selectedSchedule;
  private com.example.trainreservationsystem.models.Booking pendingBooking;
  private String selectedClass; // SL, 3A, 2A
  private double selectedClassPriceMultiplier; // Price multiplier for selected class
  private Integer preselectedSeat; // Seat number preselected from class card

  private UserSession() {
  }

  public static synchronized UserSession getInstance() {
    if (instance == null) {
      instance = new UserSession();
    }
    return instance;
  }

  public void login(User user) {
    this.currentUser = user;

    // Preload user data into cache
    DataCache.getInstance().loadUserData(user.getId());

    // Load notifications
    NotificationService.getInstance().loadNotificationsForUser(user.getId());
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void logout() {
    this.currentUser = null;

    // Clear cached data
    DataCache.getInstance().clearCache();
    NotificationService.getInstance().clear();
  }

  public void clearSession() {
    logout();
  }

  public boolean isLoggedIn() {
    return currentUser != null;
  }

  public void setSelectedSchedule(Schedule schedule) {
    this.selectedSchedule = schedule;
  }

  public Schedule getSelectedSchedule() {
    return selectedSchedule;
  }

  public void setPendingBooking(com.example.trainreservationsystem.models.Booking booking) {
    this.pendingBooking = booking;
  }

  public com.example.trainreservationsystem.models.Booking getPendingBooking() {
    return pendingBooking;
  }

  public void setSelectedClass(String classCode, double priceMultiplier) {
    this.selectedClass = classCode;
    this.selectedClassPriceMultiplier = priceMultiplier;
  }

  public String getSelectedClass() {
    return selectedClass;
  }

  public double getSelectedClassPriceMultiplier() {
    return selectedClassPriceMultiplier;
  }

  public void setPreselectedSeat(Integer seatNumber) {
    this.preselectedSeat = seatNumber;
  }

  public Integer getPreselectedSeat() {
    return preselectedSeat;
  }
}
