package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.repositories.*;

public class ServiceFactory {

  private static BookingService bookingService;
  private static PaymentService paymentService;
  private static TrainService trainService;
  private static ComplaintService complaintService;
  private static TicketService ticketService;
    private static NotificationService notificationService;

  public static BookingService getBookingService() {
    if (bookingService == null) {
      bookingService = new BookingService(new BookingRepository(), new TrainRepository());
    }
    return bookingService;
  }

  public static PaymentService getPaymentService() {
    if (paymentService == null) {
      paymentService = new PaymentService(new PaymentRepository(), new BookingRepository(),getNotificationService());
    }
    return paymentService;
  }

  public static TrainService getTrainService() {
    if (trainService == null) {
      trainService = new TrainService(new TrainRepository(),new BookingRepository(),getNotificationService() );
    }
    return trainService;
  }

  public static ComplaintService getComplaintService() {
    if (complaintService == null) {
      complaintService = new ComplaintService(new ComplaintRepository());
    }
    return complaintService;
  }

  public static TicketService getTicketService() {
    if (ticketService == null) {
      ticketService = new TicketService();
    }
    return ticketService;
  }

    public static NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = new NotificationService(new NotificationRepository());
        }
        return notificationService;
    }

}
