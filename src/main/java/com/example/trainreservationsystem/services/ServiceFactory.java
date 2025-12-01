package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.ComplaintRepository;
import com.example.trainreservationsystem.repositories.PaymentRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;

public class ServiceFactory {

  private static BookingService bookingService;
  private static PaymentService paymentService;
  private static TrainService trainService;
  private static ComplaintService complaintService;
  private static TicketService ticketService;

  public static BookingService getBookingService() {
    if (bookingService == null) {
      bookingService = new BookingService(new BookingRepository(), new TrainRepository());
    }
    return bookingService;
  }

  public static PaymentService getPaymentService() {
    if (paymentService == null) {
      paymentService = new PaymentService(new PaymentRepository(), new BookingRepository());
    }
    return paymentService;
  }

  public static TrainService getTrainService() {
    if (trainService == null) {
      trainService = new TrainService(new TrainRepository());
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
}
