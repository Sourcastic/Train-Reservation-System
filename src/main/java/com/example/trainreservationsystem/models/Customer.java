package com.example.trainreservationsystem.models;

import java.util.List;

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
}
