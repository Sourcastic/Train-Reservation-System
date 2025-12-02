package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.repositories.UserRepository;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User user = userRepository.authenticateUser(email, password);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return user;
    }

    public User signup(String username, String email, String phoneNo, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            userRepository.registerUser(username, password, email, phoneNo);
            return userRepository.authenticateUser(email, password);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Email already registered")) {
                throw new IllegalArgumentException("Email already registered");
            }
            throw e;
        }
    }
}
