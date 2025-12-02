package com.example.trainreservationsystem.services.payment;

import java.util.List;

import com.example.trainreservationsystem.models.Payment;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.PaymentRepository;

/**
 * Service for payment operations.
 * Handles payment processing and payment method management.
 */
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final BookingRepository bookingRepository;

  public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
    this.paymentRepository = paymentRepository;
    this.bookingRepository = bookingRepository;
  }

  public List<PaymentMethod> getPaymentMethods(int userId) {
    return paymentRepository.getPaymentMethods(userId);
  }

  public void addPaymentMethod(PaymentMethod method) {
    paymentRepository.savePaymentMethod(method);
  }

  public void processPayment(int bookingId, double amount, int methodId) {
    Payment payment = new Payment();
    payment.setBookingId(bookingId);
    payment.setAmount(amount);
    payment.setPaymentMethodId(methodId);
    payment.setStatus("SUCCESS");

    paymentRepository.savePayment(payment);
    bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");
  }
}
