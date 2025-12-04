package com.example.trainreservationsystem.services.shared;

import java.time.LocalDate;

import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.shared.User;

public class UserSession {
  private static UserSession instance;
  private User currentUser;
  private Schedule selectedSchedule;
  private com.example.trainreservationsystem.models.member.Booking pendingBooking;
  private String selectedClass; // SL, 3A, 2A
  private double selectedClassPriceMultiplier; // Price multiplier for selected class
  private Integer selectedClassSeatStart; // Starting seat number for selected class
  private Integer selectedClassSeatEnd; // Ending seat number for selected class
  private Integer preselectedSeat; // Seat number preselected from class card
  private LocalDate selectedTravelDate; // Date selected by user when searching

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

  public void setPendingBooking(com.example.trainreservationsystem.models.member.Booking booking) {
    this.pendingBooking = booking;
  }

  public com.example.trainreservationsystem.models.member.Booking getPendingBooking() {
    return pendingBooking;
  }

  public void setSelectedClass(String classCode, double priceMultiplier) {
    this.selectedClass = classCode;
    this.selectedClassPriceMultiplier = priceMultiplier;
  }

  public void setSelectedClass(String classCode, double priceMultiplier, int seatStart, int seatEnd) {
    this.selectedClass = classCode;
    this.selectedClassPriceMultiplier = priceMultiplier;
    this.selectedClassSeatStart = seatStart;
    this.selectedClassSeatEnd = seatEnd;
  }

  public String getSelectedClass() {
    return selectedClass;
  }

  public double getSelectedClassPriceMultiplier() {
    return selectedClassPriceMultiplier;
  }

  public Integer getSelectedClassSeatStart() {
    return selectedClassSeatStart;
  }

  public Integer getSelectedClassSeatEnd() {
    return selectedClassSeatEnd;
  }

  public void setPreselectedSeat(Integer seatNumber) {
    this.preselectedSeat = seatNumber;
  }

  public Integer getPreselectedSeat() {
    return preselectedSeat;
  }

  public void setSelectedTravelDate(LocalDate travelDate) {
    this.selectedTravelDate = travelDate;
  }

  public LocalDate getSelectedTravelDate() {
    return selectedTravelDate;
  }
}
