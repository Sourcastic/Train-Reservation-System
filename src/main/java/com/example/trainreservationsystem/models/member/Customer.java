package com.example.trainreservationsystem.models.member;

import java.util.List;

import com.example.trainreservationsystem.models.shared.User;

/**
 * Customer class extends User.
 * Demonstrates inheritance - Customer inherits all User properties and methods.
 */
public class Customer extends User {
  private int loyaltyPoints;
  private List<PaymentMethod> paymentMethods;

  public Customer() {
    super();
  }

  public Customer(int id, String username, String password, String email, int loyaltyPoints) {
    super(id, username, password, email);
    this.loyaltyPoints = loyaltyPoints;
  }

  public int getLoyaltyPoints() {
    return loyaltyPoints;
  }

  public void setLoyaltyPoints(int loyaltyPoints) {
    this.loyaltyPoints = loyaltyPoints;
  }

  public List<PaymentMethod> getPaymentMethods() {
    return paymentMethods;
  }

  public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
    this.paymentMethods = paymentMethods;
  }

  /**
   * Overrides validate() method from User class.
   * Demonstrates polymorphism - same method name, different implementation.
   * Customer validation includes loyalty points check.
   */
  @Override
  public String validate() {
    // Call parent class validation first (inheritance)
    String parentValidation = super.validate();
    if (parentValidation != null && !parentValidation.isEmpty()) {
      return parentValidation;
    }

    // Additional validation specific to Customer (polymorphism)
    if (loyaltyPoints < 0) {
      return "Loyalty points cannot be negative";
    }

    return ""; // Valid
  }
}
