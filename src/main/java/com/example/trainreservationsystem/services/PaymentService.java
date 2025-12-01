package com.example.trainreservationsystem.services;

import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Payment;
import com.example.trainreservationsystem.models.PaymentMethod;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.PaymentRepository;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationsService notificationService;  // ADD THIS

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          NotificationsService notificationService) {  // ADD THIS
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    public List<PaymentMethod> getPaymentMethods(int userId) {
        return paymentRepository.getPaymentMethods(userId);
    }

    public void addPaymentMethod(PaymentMethod method) {
        paymentRepository.savePaymentMethod(method);
    }

    public void processPayment(int bookingId, double amount, int methodId) {

        // 1. Get booking so we know userId
        Booking booking = bookingRepository.getBookingById(bookingId);

        // 2. Create and save payment
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(amount);
        payment.setPaymentMethodId(methodId);
        payment.setStatus("SUCCESS"); // mock success
        paymentRepository.savePayment(payment);

        // 3. Update booking status
        bookingRepository.updateBookingStatus(bookingId, "CONFIRMED");

        // 4. Send notification using notification service (REAL DB)
        notificationService.sendNotification(
                booking.getUserId(),
                "Your booking #" + bookingId + " is confirmed. Payment received."
        );
    }
}
