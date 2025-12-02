package com.example.trainreservationsystem.utils.booking;

import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Passenger;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.NotificationService;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.services.booking.BookingService;

/**
 * Helper for booking operations.
 * Creates passengers and bookings.
 */
public class BookingHelper {

  public static List<Passenger> createPassengers(String name, int age, List<Integer> selectedSeats) {
    List<Passenger> passengers = new ArrayList<>();
    for (Integer seatNumber : selectedSeats) {
      Passenger p = new Passenger(name + " (Seat " + seatNumber + ")", age, false, false);
      p.setSeatNumber(seatNumber);
      passengers.add(p);
    }
    return passengers;
  }

  public static Booking createBooking(BookingService bookingService, Schedule schedule,
      List<Passenger> passengers) {
    int userId = UserSession.getInstance().getCurrentUser().getId();
    return bookingService.createBooking(userId, schedule, passengers);
  }

  public static void saveBooking(Booking booking, Schedule schedule, int selectedSeatsCount) {
    // Use selected class price if available
    double pricePerSeat = schedule.getPrice();
    String selectedClass = UserSession.getInstance().getSelectedClass();
    if (selectedClass != null) {
      double multiplier = UserSession.getInstance().getSelectedClassPriceMultiplier();
      pricePerSeat = schedule.getPrice() * multiplier;
    }

    booking.setTotalAmount(pricePerSeat * selectedSeatsCount);
    if (booking.getSchedule() == null) {
      booking.setSchedule(schedule);
    }
    UserSession.getInstance().setPendingBooking(booking);
    NotificationService.getInstance().add("Booking created for " + selectedSeatsCount + " seat(s)");
  }
}
