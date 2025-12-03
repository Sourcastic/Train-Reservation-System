package com.example.trainreservationsystem.utils.shared.payment;

import java.util.Map;

/**
 * Adapter interface for different payment methods.
 * Implements Adapter pattern to provide a unified interface for various payment
 * types.
 */
public interface PaymentAdapter {
  /**
   * Processes a payment using the specific payment method.
   *
   * @param amount  The amount to be paid
   * @param details Payment details specific to the payment method
   * @return true if payment was successful, false otherwise
   */
  boolean processPayment(double amount, Map<String, String> details);

  /**
   * Gets the display name of the payment method.
   *
   * @return Payment method name
   */
  String getMethodName();

  /**
   * Validates payment details before processing.
   *
   * @param details Payment details to validate
   * @return Validation result message, empty string if valid
   */
  String validateDetails(Map<String, String> details);
}
