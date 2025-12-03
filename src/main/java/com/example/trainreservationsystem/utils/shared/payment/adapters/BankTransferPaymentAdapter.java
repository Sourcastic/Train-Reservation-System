package com.example.trainreservationsystem.utils.shared.payment.adapters;

import java.util.Map;

import com.example.trainreservationsystem.utils.shared.payment.PaymentAdapter;

/**
 * Adapter for Bank Transfer payments.
 * Requires IBAN only.
 */
public class BankTransferPaymentAdapter implements PaymentAdapter {

  @Override
  public boolean processPayment(double amount, Map<String, String> details) {
    // Simulate bank transfer processing
    String iban = details.get("iban");

    // In a real application, this would initiate a bank transfer
    // For now, we simulate successful payment
    return iban != null && !iban.isEmpty() && isValidIBAN(iban);
  }

  @Override
  public String getMethodName() {
    return "Bank Transfer";
  }

  @Override
  public String validateDetails(Map<String, String> details) {
    String iban = details.get("iban");

    if (iban == null || iban.trim().isEmpty()) {
      return "IBAN is required";
    }

    if (!isValidIBAN(iban)) {
      return "Invalid IBAN format. IBAN should be 15-34 alphanumeric characters";
    }

    return ""; // Valid
  }

  /**
   * Validates IBAN format (basic validation).
   * IBAN should be 15-34 alphanumeric characters, typically starting with country
   * code.
   */
  private boolean isValidIBAN(String iban) {
    if (iban == null) {
      return false;
    }
    // Remove spaces and convert to uppercase
    String cleaned = iban.replaceAll("\\s", "").toUpperCase();
    // Basic validation: 15-34 alphanumeric characters
    return cleaned.matches("^[A-Z0-9]{15,34}$");
  }
}
