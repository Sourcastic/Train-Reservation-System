package com.example.trainreservationsystem.models.shared;

public class User {
  private int id;
  private String username;
  private String password;
  private String email;
  private String phoneNo;
  private String userType;
  private int loyaltyPoints;

  public User() {
  }

  public User(int id, String username, String password, String email) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
  }

  public User(int id, String username, String password, String email, String phoneNo, String userType,
      int loyaltyPoints) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.phoneNo = phoneNo;
    this.userType = userType;
    this.loyaltyPoints = loyaltyPoints;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNo() {
    return phoneNo;
  }

  public void setPhoneNo(String phoneNo) {
    // Validate phone number format (encapsulation - data validation)
    if (phoneNo != null && !phoneNo.trim().isEmpty()) {
      String validationError = validatePhoneNumber(phoneNo);
      if (validationError != null && !validationError.isEmpty()) {
        throw new IllegalArgumentException(validationError);
      }
      // Format phone number to standard format
      this.phoneNo = formatPhoneNumber(phoneNo);
    } else {
      this.phoneNo = phoneNo;
    }
  }

  /**
   * Validates user data.
   * Demonstrates encapsulation - validation logic within the class.
   *
   * @return Error message if invalid, empty string if valid
   */
  public String validate() {
    if (username == null || username.trim().isEmpty()) {
      return "Username cannot be empty";
    }
    if (email == null || email.trim().isEmpty()) {
      return "Email cannot be empty";
    }
    if (!email.contains("@")) {
      return "Invalid email format";
    }
    if (phoneNo != null && !phoneNo.trim().isEmpty()) {
      String phoneError = validatePhoneNumber(phoneNo);
      if (phoneError != null && !phoneError.isEmpty()) {
        return phoneError;
      }
    }
    return ""; // Valid
  }

  /**
   * Validates phone number format.
   * Pakistani format: 03XX-XXXXXXX or 03XXXXXXXXX (11 digits starting with 03)
   */
  private String validatePhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      return "Phone number cannot be empty";
    }

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
      return cleaned.substring(0, 4) + "-" + cleaned.substring(4);
    }

    return phoneNumber;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public int getLoyaltyPoints() {
    return loyaltyPoints;
  }

  public void setLoyaltyPoints(int loyaltyPoints) {
    this.loyaltyPoints = loyaltyPoints;
  }
}
