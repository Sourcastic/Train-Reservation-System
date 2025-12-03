package com.example.trainreservationsystem.services.member.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.trainreservationsystem.models.admin.Discount;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.Payment;
import com.example.trainreservationsystem.models.member.PaymentMethod;
import com.example.trainreservationsystem.repositories.admin.DiscountRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.repositories.member.PaymentRepository;
import com.example.trainreservationsystem.services.member.LoyaltyPointsService;
import com.example.trainreservationsystem.utils.shared.payment.PaymentAdapter;

/**
 * Service for payment operations.
 * Handles payment processing, discount codes, and loyalty points.
 */
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final BookingRepository bookingRepository;
  private final DiscountRepository discountRepository;
  private final LoyaltyPointsService loyaltyPointsService;

  public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository,
      DiscountRepository discountRepository, LoyaltyPointsService loyaltyPointsService) {
    this.paymentRepository = paymentRepository;
    this.bookingRepository = bookingRepository;
    this.discountRepository = discountRepository;
    this.loyaltyPointsService = loyaltyPointsService;
  }

  public List<PaymentMethod> getPaymentMethods(int userId) {
    return paymentRepository.getPaymentMethods(userId);
  }

  public void addPaymentMethod(PaymentMethod method) {
    paymentRepository.savePaymentMethod(method);
  }

  /**
   * Validates and applies a discount code.
   * Returns the discount amount if valid, 0 otherwise.
   *
   * @param discountCode   The discount code to apply
   * @param originalAmount The original amount before discount
   * @param scheduleId     Optional schedule ID to validate schedule-specific
   *                       discounts
   */
  public double applyDiscountCode(String discountCode, double originalAmount, Integer scheduleId) {
    if (discountCode == null || discountCode.trim().isEmpty()) {
      return 0;
    }

    Discount discount = null;
    if (scheduleId != null) {
      discount = discountRepository.findByCodeAndSchedule(discountCode.trim().toUpperCase(), scheduleId);
    } else {
      discount = discountRepository.findByCode(discountCode.trim().toUpperCase());
    }

    if (discount != null && discount.isValid()) {
      // Additional validation: check if discount applies to this schedule
      if (scheduleId != null && !discount.isValidForSchedule(scheduleId)) {
        return 0;
      }
      double discountAmount = discount.calculateDiscount(originalAmount);
      return discountAmount;
    }
    return 0;
  }

  /**
   * Validates and applies a discount code (backward compatibility - no schedule
   * validation).
   * Returns the discount amount if valid, 0 otherwise.
   */
  public double applyDiscountCode(String discountCode, double originalAmount) {
    return applyDiscountCode(discountCode, originalAmount, null);
  }

  /**
   * Processes payment using PaymentAdapter pattern.
   * Supports discount codes and loyalty points.
   */
  public void processPayment(int bookingId, double originalAmount, PaymentAdapter adapter,
      Map<String, String> paymentDetails, String discountCode) {
    // Get booking to check schedule
    Booking booking = bookingRepository.getBookingById(bookingId);
    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }

    Integer scheduleId = null;
    if (booking.getSchedule() != null) {
      scheduleId = booking.getSchedule().getId();
    }

    // Apply discount if code provided
    double discountAmount = 0;
    Discount appliedDiscount = null;

    if (discountCode != null && !discountCode.trim().isEmpty()) {
      if (scheduleId != null) {
        appliedDiscount = discountRepository.findByCodeAndSchedule(discountCode.trim().toUpperCase(), scheduleId);
      } else {
        appliedDiscount = discountRepository.findByCode(discountCode.trim().toUpperCase());
      }

      if (appliedDiscount != null && appliedDiscount.isValid()) {
        // Additional validation: check if discount applies to this schedule
        if (scheduleId == null || appliedDiscount.isValidForSchedule(scheduleId)) {
          discountAmount = appliedDiscount.calculateDiscount(originalAmount);
          // Increment discount usage
          discountRepository.incrementUsage(appliedDiscount.getId());
        }
      }
    }

    double finalAmount = originalAmount - discountAmount;

    // Add amount to payment details for wallet validation
    Map<String, String> detailsWithAmount = new HashMap<>(paymentDetails);
    detailsWithAmount.put("amount", String.valueOf(finalAmount));

    // Validate payment details using adapter
    String validationError = adapter.validateDetails(detailsWithAmount);
    if (!validationError.isEmpty()) {
      throw new IllegalArgumentException(validationError);
    }

    // Process payment using adapter
    boolean paymentSuccess = adapter.processPayment(finalAmount, detailsWithAmount);
    if (!paymentSuccess) {
      throw new RuntimeException("Payment processing failed");
    }

    // Create temporary payment method record
    PaymentMethod tempMethod = new PaymentMethod();
    tempMethod.setUserId(booking.getUserId());
    // Truncate method name to fit database column (50 chars)
    String methodName = adapter.getMethodName();
    if (methodName.length() > 50) {
      methodName = methodName.substring(0, 50);
    }
    tempMethod.setMethodType(methodName);
    tempMethod.setDetails("One-time payment via " + methodName);
    paymentRepository.savePaymentMethod(tempMethod);

    // Create payment record
    Payment payment = new Payment();
    payment.setBookingId(bookingId);
    payment.setAmount(finalAmount);
    payment.setPaymentMethodId(tempMethod.getId());
    payment.setStatus("SUCCESS");

    paymentRepository.savePayment(payment);
    bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");

    // Grant loyalty points only if not using wallet payment
    // (Wallet payment already deducts points, so we don't grant new ones)
    if (!adapter.getMethodName().contains("Wallet")) {
      loyaltyPointsService.grantLoyaltyPoints(booking.getUserId(), finalAmount);
    }
  }

  /**
   * Processes payment with discount code support and loyalty points.
   * Creates a temporary payment method if methodId is 0.
   *
   * @deprecated Use processPayment with PaymentAdapter instead
   */
  @Deprecated
  public void processPayment(int bookingId, double originalAmount, int methodId, String discountCode,
      String paymentMethodType) {
    // Apply discount if code provided
    double discountAmount = 0;
    Discount appliedDiscount = null;

    if (discountCode != null && !discountCode.trim().isEmpty()) {
      appliedDiscount = discountRepository.findByCode(discountCode.trim().toUpperCase());
      if (appliedDiscount != null && appliedDiscount.isValid()) {
        discountAmount = appliedDiscount.calculateDiscount(originalAmount);
        // Increment discount usage
        discountRepository.incrementUsage(appliedDiscount.getId());
      }
    }

    double finalAmount = originalAmount - discountAmount;

    // If no payment method ID provided, create a temporary one
    int actualMethodId = methodId;
    if (actualMethodId == 0 && paymentMethodType != null) {
      Booking booking = bookingRepository.getBookingById(bookingId);
      if (booking != null) {
        PaymentMethod tempMethod = new PaymentMethod();
        tempMethod.setUserId(booking.getUserId());
        tempMethod.setMethodType(paymentMethodType);
        tempMethod.setDetails("One-time payment");
        paymentRepository.savePaymentMethod(tempMethod);
        actualMethodId = tempMethod.getId();
      }
    }

    // Create payment record
    Payment payment = new Payment();
    payment.setBookingId(bookingId);
    payment.setAmount(finalAmount);
    payment.setPaymentMethodId(actualMethodId);
    payment.setStatus("SUCCESS");

    paymentRepository.savePayment(payment);
    bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");

    // Grant loyalty points based on final amount (after discount)
    // Get user ID from booking
    Booking booking = bookingRepository.getBookingById(bookingId);
    if (booking != null) {
      // Grant 10% loyalty points on the final amount paid
      loyaltyPointsService.grantLoyaltyPoints(booking.getUserId(), finalAmount);
    }
  }

  /**
   * Processes payment with discount code support and loyalty points.
   */
  public void processPayment(int bookingId, double originalAmount, int methodId, String discountCode) {
    processPayment(bookingId, originalAmount, methodId, discountCode, null);
  }

  /**
   * Processes payment (backward compatibility - no discount).
   */
  public void processPayment(int bookingId, double amount, int methodId) {
    processPayment(bookingId, amount, methodId, null, null);
  }
}
