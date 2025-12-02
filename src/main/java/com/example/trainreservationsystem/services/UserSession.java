package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.models.User;

public class UserSession {
  private static UserSession instance;
  private User currentUser;
  private Schedule selectedSchedule;
  private Booking pendingBooking;

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
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void logout() {
    this.currentUser = null;
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

  public void setPendingBooking(Booking booking) {
    this.pendingBooking = booking;
  }

  public Booking getPendingBooking() {
    return pendingBooking;
  }
}
