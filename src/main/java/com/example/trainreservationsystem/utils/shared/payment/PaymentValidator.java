package com.example.trainreservationsystem.utils.shared.payment;

/**
 * Validates payment form inputs.
 * Returns validation results with error messages or success details.
 */
public class PaymentValidator {

  public static ValidationResult validateCard(String cardNumber, String cardName,
      String expiry, String cvv) {
    String cleanedCard = cardNumber.replaceAll("\\s", "");

    if (cleanedCard.length() < 16) {
      return ValidationResult.error("Please enter a valid 16-digit card number");
    }
    if (cardName.trim().isEmpty()) {
      return ValidationResult.error("Please enter cardholder name");
    }
    if (!expiry.matches("\\d{2}/\\d{2}")) {
      return ValidationResult.error("Please enter expiry in MM/YY format");
    }
    if (cvv.length() < 3) {
      return ValidationResult.error("Please enter a valid 3-digit CVV");
    }

    return ValidationResult.success("****" + cleanedCard.substring(12));
  }

  public static ValidationResult validateCash(String contact) {
    if (contact.trim().isEmpty()) {
      return ValidationResult.error("Please enter contact number for cash payment");
    }
    return ValidationResult.success("Contact: " + contact);
  }

  public static ValidationResult validateBankTransfer(String iban) {
    if (iban == null || iban.trim().isEmpty()) {
      return ValidationResult.error("Please enter IBAN");
    }
    String cleaned = iban.replaceAll("\\s", "").toUpperCase();
    if (!cleaned.matches("^[A-Z0-9]{15,34}$")) {
      return ValidationResult.error("Invalid IBAN format. IBAN should be 15-34 alphanumeric characters");
    }
    return ValidationResult.success("IBAN: " + cleaned);
  }

  public static ValidationResult validateWallet(int availablePoints, double amount) {
    int requiredPoints = (int) Math.ceil(amount);
    if (availablePoints < requiredPoints) {
      return ValidationResult.error(
          String.format("Insufficient loyalty points. Required: %d, Available: %d", requiredPoints, availablePoints));
    }
    return ValidationResult.success(String.format("Using %d loyalty points", requiredPoints));
  }

  public static class ValidationResult {
    private final boolean valid;
    private final String message;
    private final String details;

    private ValidationResult(boolean valid, String message, String details) {
      this.valid = valid;
      this.message = message;
      this.details = details;
    }

    public static ValidationResult success(String details) {
      return new ValidationResult(true, null, details);
    }

    public static ValidationResult error(String message) {
      return new ValidationResult(false, message, null);
    }

    public boolean isValid() {
      return valid;
    }

    public String getMessage() {
      return message;
    }

    public String getDetails() {
      return details;
    }
  }
}
