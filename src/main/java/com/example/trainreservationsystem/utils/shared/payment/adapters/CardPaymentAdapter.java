package com.example.trainreservationsystem.utils.shared.payment.adapters;

import java.util.Map;

import com.example.trainreservationsystem.utils.shared.payment.PaymentAdapter;

/**
 * Adapter for Credit/Debit Card payments.
 */
public class CardPaymentAdapter implements PaymentAdapter {

  @Override
  public boolean processPayment(double amount, Map<String, String> details) {
    // Simulate card payment processing
    String cardNumber = details.get("cardNumber");
    String cardName = details.get("cardName");
    String expiry = details.get("expiry");
    String cvv = details.get("cvv");

    // In a real application, this would call a payment gateway
    // For now, we simulate successful payment
    return cardNumber != null && !cardNumber.isEmpty()
        && cardName != null && !cardName.isEmpty()
        && expiry != null && !expiry.isEmpty()
        && cvv != null && !cvv.isEmpty();
  }

  @Override
  public String getMethodName() {
    return "Credit/Debit Card";
  }

  @Override
  public String validateDetails(Map<String, String> details) {
    String cardNumber = details.get("cardNumber");
    String cardName = details.get("cardName");
    String expiry = details.get("expiry");
    String cvv = details.get("cvv");

    if (cardNumber == null || cardNumber.trim().isEmpty()) {
      return "Card number is required";
    }
    if (cardNumber.replaceAll("\\s", "").length() < 13) {
      return "Card number must be at least 13 digits";
    }
    if (cardName == null || cardName.trim().isEmpty()) {
      return "Cardholder name is required";
    }
    if (expiry == null || expiry.trim().isEmpty()) {
      return "Expiry date is required";
    }
    if (!expiry.matches("\\d{2}/\\d{2}")) {
      return "Expiry date must be in MM/YY format";
    }
    if (cvv == null || cvv.trim().isEmpty()) {
      return "CVV is required";
    }
    if (cvv.length() != 3) {
      return "CVV must be 3 digits";
    }

    return ""; // Valid
  }
}
