package com.example.trainreservationsystem.services.shared;

import com.example.trainreservationsystem.models.shared.User;
import com.example.trainreservationsystem.repositories.UserRepository;

public class AuthService {
    private final UserRepository userRepository;

    private static AuthService instance;

    private AuthService() {
        this.userRepository = com.example.trainreservationsystem.repositories.RepositoryFactory.getUserRepository();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
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
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Validate phone number format (same place as email/password validation)
        String phoneError = validatePhoneNumber(phoneNo);
        if (phoneError != null && !phoneError.isEmpty()) {
            throw new IllegalArgumentException(phoneError);
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Format phone number before saving
        phoneNo = formatPhoneNumber(phoneNo);

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

    public boolean resetPassword(String email, String newPassword) {
        try {
            userRepository.updatePassword(email, newPassword);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateUserProfile(User user) throws Exception {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (user.getPhoneNo() == null || user.getPhoneNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Validate phone number format (same place as email validation)
        String phoneError = validatePhoneNumber(user.getPhoneNo());
        if (phoneError != null && !phoneError.isEmpty()) {
            throw new IllegalArgumentException(phoneError);
        }

        // Format phone number before saving
        user.setPhoneNo(formatPhoneNumber(user.getPhoneNo()));

        userRepository.updateUser(user);
    }

    /**
     * Validates phone number format.
     * Pakistani format: 03XX-XXXXXXX or 03XXXXXXXXX (11 digits starting with 03)
     */
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Phone number cannot be empty";
        }

        // Remove spaces and hyphens for validation
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        if (cleaned.length() != 11) {
            return "Phone number must be 11 digits (format: 03XX-XXXXXXX)";
        }

        if (!cleaned.matches("^03\\d{9}$")) {
            return "Phone number must start with 03XX followed by 7 digits (format: 03XX-XXXXXXX)";
        }

        return ""; // Valid
    }

    /**
     * Formats phone number to standard format: 03XX-XXXXXXX
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        if (cleaned.matches("^03\\d{9}$")) {
            // Format as 03XX-XXXXXXX
            return cleaned.substring(0, 4) + "-" + cleaned.substring(4);
        }

        return phoneNumber; // Return original if can't format
    }
}
