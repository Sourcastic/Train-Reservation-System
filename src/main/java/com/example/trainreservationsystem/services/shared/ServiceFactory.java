package com.example.trainreservationsystem.services.shared;

import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.services.member.booking.BookingService;
import com.example.trainreservationsystem.services.member.payment.PaymentService;

/**
 * Factory for creating service instances.
 * Uses singleton pattern to reuse service instances.
 */
public class ServiceFactory {

  private static BookingService bookingService;
  private static PaymentService paymentService;
  private static TrainService trainService;
  private static ComplaintService complaintService;
  private static LoyaltyPointsService loyaltyPointsService;

  public static BookingService getBookingService() {
    if (bookingService == null) {
      bookingService = new BookingService(
          RepositoryFactory.getBookingRepository(),
          RepositoryFactory.getTrainRepository());
    }
    return bookingService;
  }

  public static PaymentService getPaymentService() {
    if (paymentService == null) {
      paymentService = new PaymentService(
          RepositoryFactory.getPaymentRepository(),
          RepositoryFactory.getBookingRepository(),
          RepositoryFactory.getDiscountRepository(),
          getLoyaltyPointsService());
    }
    return paymentService;
  }

  public static LoyaltyPointsService getLoyaltyPointsService() {
    if (loyaltyPointsService == null) {
      loyaltyPointsService = new LoyaltyPointsService(
          RepositoryFactory.getUserRepository(),
          NotificationService.getInstance());
    }
    return loyaltyPointsService;
  }

  public static com.example.trainreservationsystem.repositories.DiscountRepository getDiscountRepository() {
    return RepositoryFactory.getDiscountRepository();
  }

  public static TrainService getTrainService() {
    if (trainService == null) {
      trainService = new TrainService(RepositoryFactory.getTrainRepository());
    }
    return trainService;
  }

  public static ComplaintService getComplaintService() {
    if (complaintService == null) {
      complaintService = new ComplaintService(RepositoryFactory.getComplaintRepository());
    }
    return complaintService;
  }
}
