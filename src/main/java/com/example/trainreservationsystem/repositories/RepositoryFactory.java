package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.repositories.admin.CancellationPolicyRepository;
import com.example.trainreservationsystem.repositories.admin.DiscountRepository;
import com.example.trainreservationsystem.repositories.admin.RouteRepository;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.admin.SeatClassRepository;
import com.example.trainreservationsystem.repositories.admin.StatisticsRepository;
import com.example.trainreservationsystem.repositories.admin.StopRepository;
import com.example.trainreservationsystem.repositories.admin.TrainRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.repositories.member.NotificationRepository;
import com.example.trainreservationsystem.repositories.member.PaymentRepository;
import com.example.trainreservationsystem.repositories.member.TicketRepository;
import com.example.trainreservationsystem.repositories.shared.ComplaintRepository;
import com.example.trainreservationsystem.repositories.shared.SeatRepository;
import com.example.trainreservationsystem.repositories.shared.UserRepository;
import com.example.trainreservationsystem.repositories.staff.StaffComplaintRepository;

/**
 * Factory for creating repository instances.
 * Uses singleton pattern to reuse repository instances.
 */
public class RepositoryFactory {

  private static BookingRepository bookingRepository;
  private static PaymentRepository paymentRepository;
  private static UserRepository userRepository;
  private static TrainRepository trainRepository;
  private static ComplaintRepository complaintRepository;
  private static DiscountRepository discountRepository;
  private static NotificationRepository notificationRepository;
  private static RouteRepository routeRepository;
  private static ScheduleRepository scheduleRepository;
  private static SeatClassRepository seatClassRepository;
  private static SeatRepository seatRepository;
  private static StopRepository stopRepository;
  private static TicketRepository ticketRepository;
  private static StatisticsRepository statisticsRepository;
  private static StaffComplaintRepository staffComplaintRepository;
  private static CancellationPolicyRepository cancellationPolicyRepository;

  public static BookingRepository getBookingRepository() {
    if (bookingRepository == null) {
      bookingRepository = new BookingRepository();
    }
    return bookingRepository;
  }

  public static PaymentRepository getPaymentRepository() {
    if (paymentRepository == null) {
      paymentRepository = new PaymentRepository();
    }
    return paymentRepository;
  }

  public static UserRepository getUserRepository() {
    if (userRepository == null) {
      userRepository = new UserRepository();
    }
    return userRepository;
  }

  public static TrainRepository getTrainRepository() {
    if (trainRepository == null) {
      trainRepository = new TrainRepository();
    }
    return trainRepository;
  }

  public static ComplaintRepository getComplaintRepository() {
    if (complaintRepository == null) {
      complaintRepository = new ComplaintRepository();
    }
    return complaintRepository;
  }

  public static DiscountRepository getDiscountRepository() {
    if (discountRepository == null) {
      discountRepository = new DiscountRepository();
    }
    return discountRepository;
  }

  public static NotificationRepository getNotificationRepository() {
    if (notificationRepository == null) {
      notificationRepository = new NotificationRepository();
    }
    return notificationRepository;
  }

  public static RouteRepository getRouteRepository() {
    if (routeRepository == null) {
      routeRepository = new RouteRepository();
    }
    return routeRepository;
  }

  public static ScheduleRepository getScheduleRepository() {
    if (scheduleRepository == null) {
      scheduleRepository = new ScheduleRepository();
    }
    return scheduleRepository;
  }

  public static SeatClassRepository getSeatClassRepository() {
    if (seatClassRepository == null) {
      seatClassRepository = new SeatClassRepository();
    }
    return seatClassRepository;
  }

  public static SeatRepository getSeatRepository() {
    if (seatRepository == null) {
      seatRepository = new SeatRepository();
    }
    return seatRepository;
  }

  public static StopRepository getStopRepository() {
    if (stopRepository == null) {
      stopRepository = new StopRepository();
    }
    return stopRepository;
  }

  public static TicketRepository getTicketRepository() {
    if (ticketRepository == null) {
      ticketRepository = new TicketRepository();
    }
    return ticketRepository;
  }

  public static StatisticsRepository getStatisticsRepository() {
    if (statisticsRepository == null) {
      statisticsRepository = new StatisticsRepository();
    }
    return statisticsRepository;
  }

  public static StaffComplaintRepository getStaffComplaintRepository() {
    if (staffComplaintRepository == null) {
      staffComplaintRepository = new StaffComplaintRepository();
    }
    return staffComplaintRepository;
  }

  public static CancellationPolicyRepository getCancellationPolicyRepository() {
    if (cancellationPolicyRepository == null) {
      cancellationPolicyRepository = new CancellationPolicyRepository();
    }
    return cancellationPolicyRepository;
  }
}
