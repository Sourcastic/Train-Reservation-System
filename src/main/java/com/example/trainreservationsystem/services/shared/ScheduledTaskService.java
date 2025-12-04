package com.example.trainreservationsystem.services.shared;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.TrainRepository;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.repositories.member.PaymentRepository;

/**
 * Service for running scheduled background tasks.
 * Handles automatic booking cancellation, departure reminders, etc.
 */
public class ScheduledTaskService {
  private static ScheduledTaskService instance;
  private final ScheduledExecutorService scheduler;
  private final BookingRepository bookingRepository;
  private final PaymentRepository paymentRepository;
  private final NotificationService notificationService;
  private final TrainRepository trainRepository;

  // Timeout for unpaid PENDING bookings (in minutes)
  private static final int BOOKING_TIMEOUT_MINUTES = 15;

  private ScheduledTaskService() {
    this.scheduler = Executors.newScheduledThreadPool(2);
    this.bookingRepository = RepositoryFactory.getBookingRepository();
    this.paymentRepository = RepositoryFactory.getPaymentRepository();
    this.notificationService = NotificationService.getInstance();
    this.trainRepository = RepositoryFactory.getTrainRepository();
  }

  public static synchronized ScheduledTaskService getInstance() {
    if (instance == null) {
      instance = new ScheduledTaskService();
    }
    return instance;
  }

  /**
   * Starts all scheduled tasks.
   * Should be called when the application starts.
   */
  public void start() {
    // Run booking cancellation check every 5 minutes
    scheduler.scheduleAtFixedRate(this::cancelUnpaidBookings, 0, 5, TimeUnit.MINUTES);

    // Run departure reminders check every hour
    scheduler.scheduleAtFixedRate(this::sendDepartureReminders, 0, 1, TimeUnit.HOURS);

    System.out.println("✅ Scheduled tasks started");
  }

  /**
   * Cancels unpaid PENDING bookings that have exceeded the timeout period.
   */
  private void cancelUnpaidBookings() {
    try {
      List<Booking> pendingBookings = bookingRepository.getPendingBookings();
      LocalDateTime now = LocalDateTime.now();
      int cancelledCount = 0;

      for (Booking booking : pendingBookings) {
        // Check if booking has exceeded timeout
        LocalDateTime bookingDate = booking.getBookingDate();
        if (bookingDate != null) {
          long minutesSinceBooking = ChronoUnit.MINUTES.between(bookingDate, now);

          if (minutesSinceBooking >= BOOKING_TIMEOUT_MINUTES) {
            // Check if payment exists for this booking
            boolean hasPayment = paymentRepository.hasPaymentForBooking(booking.getId());

            if (!hasPayment) {
              // Cancel the booking
              bookingRepository.updateBookingStatus(booking.getId(), "CANCELLED");
              cancelledCount++;

              // Notify user
              notificationService.add(
                  "Your booking #" + booking.getId() + " was automatically cancelled due to non-payment.",
                  booking.getUserId());

              System.out.println("⏰ Auto-cancelled unpaid booking #" + booking.getId());
            }
          }
        }
      }

      if (cancelledCount > 0) {
        System.out.println("✅ Cancelled " + cancelledCount + " unpaid booking(s)");
      }
    } catch (Exception e) {
      System.err.println("❌ Error in cancelUnpaidBookings: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Sends departure reminders to users with upcoming trips.
   * Sends reminders 24 hours and 2 hours before departure.
   */
  private void sendDepartureReminders() {
    try {
      List<Booking> confirmedBookings = bookingRepository.getConfirmedBookings();
      LocalDateTime now = LocalDateTime.now();
      int reminderCount = 0;

      for (Booking booking : confirmedBookings) {
        // Load schedule if not already loaded
        if (booking.getSchedule() == null) {
          try {
            booking.setSchedule(trainRepository.getScheduleById(booking.getScheduleId()));
          } catch (Exception e) {
            System.err.println("Error loading schedule for booking #" + booking.getId() + ": " + e.getMessage());
            continue;
          }
        }

        if (booking.getSchedule() == null) {
          continue;
        }

        // Calculate departure datetime - use booking date as travel date fallback
        LocalDate travelDate = booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate()
            : LocalDate.now();
        LocalDateTime departureDateTime = travelDate.atTime(booking.getSchedule().getDepartureTime());

        // Check if departure is within 24 hours (and not already passed)
        long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureDateTime);

        if (hoursUntilDeparture > 0 && hoursUntilDeparture <= 24) {
          // Check if we already sent a reminder (simple check - in production, use a
          // flag)
          // For now, send reminder if departure is between 23-24 hours or 1-2 hours away
          if ((hoursUntilDeparture >= 23 && hoursUntilDeparture <= 24) ||
              (hoursUntilDeparture >= 1 && hoursUntilDeparture <= 2)) {

            String routeInfo = booking.getSchedule().getRoute() != null
                ? booking.getSchedule().getRoute().getName()
                : "your scheduled train";

            String message = String.format(
                "Reminder: Your train (%s) departs in %d hour(s) on %s at %s",
                routeInfo,
                (int) hoursUntilDeparture,
                travelDate,
                booking.getSchedule().getDepartureTime());

            notificationService.add(message, booking.getUserId());
            reminderCount++;
          }
        }
      }

      if (reminderCount > 0) {
        System.out.println("📧 Sent " + reminderCount + " departure reminder(s)");
      }
    } catch (Exception e) {
      System.err.println("❌ Error in sendDepartureReminders: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Shuts down the scheduler gracefully.
   * Should be called when the application shuts down.
   */
  public void shutdown() {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
