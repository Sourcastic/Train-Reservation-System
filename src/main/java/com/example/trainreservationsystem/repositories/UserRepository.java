package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.User;

public class UserRepository {
  public User getUserByUsername(String username) {
    // Mock user return
    if ("demo".equals(username)) {
      return new User(1, "demo", "demo123", "demo@example.com");
    }
    return null;
  }
}
