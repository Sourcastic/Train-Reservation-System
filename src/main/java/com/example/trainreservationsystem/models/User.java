package com.example.trainreservationsystem.models;

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
    this.phoneNo = phoneNo;
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
