package com.example.trainreservationsystem.services.shared;

import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.models.member.PaymentMethod;
import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.repositories.member.BookingRepository;
import com.example.trainreservationsystem.repositories.member.PaymentRepository;
import com.example.trainreservationsystem.repositories.member.TicketRepository;

/**
 * Singleton service that caches user data in memory using write-through
 * caching.
 * When data is modified, it updates both the database and the cache.
 */
public class DataCache {
    private static DataCache instance;

    // Repositories for write-through operations
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    // Cached data (synchronized for thread safety)
    private final List<Booking> bookings = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<PaymentMethod> paymentMethods = new ArrayList<>();

    private DataCache() {
        this.bookingRepository = com.example.trainreservationsystem.repositories.RepositoryFactory
                .getBookingRepository();
        this.paymentRepository = com.example.trainreservationsystem.repositories.RepositoryFactory
                .getPaymentRepository();
        this.ticketRepository = com.example.trainreservationsystem.repositories.RepositoryFactory.getTicketRepository();
    }

    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    /**
     * Loads all user data from the database into memory cache.
     * Called after successful login.
     */
    public synchronized void loadUserData(int userId) {
        System.out.println("📥 Loading user data into cache (userId: " + userId + ")...");

        // Clear existing cache
        clearCache();

        // Load from database
        bookings.addAll(bookingRepository.getBookingsByUserId(userId));
        tickets.addAll(ticketRepository.getTicketsByUserId(userId));
        paymentMethods.addAll(paymentRepository.getPaymentMethods(userId));

        System.out.println("✅ User data loaded: " + bookings.size() + " bookings, " +
                tickets.size() + " tickets, " + paymentMethods.size() + " payment methods");
    }

    /**
     * Clears all cached data.
     * Called on logout.
     */
    public synchronized void clearCache() {
        bookings.clear();
        tickets.clear();
        paymentMethods.clear();
    }

    // ========== READ OPERATIONS (from cache) ==========

    public synchronized List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public synchronized List<Ticket> getTickets() {
        return new ArrayList<>(tickets);
    }

    public synchronized List<PaymentMethod> getPaymentMethods() {
        return new ArrayList<>(paymentMethods);
    }

    // ========== WRITE-THROUGH OPERATIONS (update DB and cache) ==========

    /**
     * Adds a new booking - writes to database AND updates cache.
     */
    public synchronized Booking addBooking(Booking booking) {
        // Write to database
        Booking savedBooking = bookingRepository.createBooking(booking);

        // Update cache
        bookings.add(0, savedBooking); // Add to front (most recent first)

        return savedBooking;
    }

    /**
     * Updates booking status - writes to database AND updates cache.
     */
    public synchronized boolean updateBookingStatus(int bookingId, String status) {
        // Write to database
        boolean updated = bookingRepository.updateBookingStatus(bookingId, status);

        // Update cache
        if (updated) {
            for (Booking booking : bookings) {
                if (booking.getId() == bookingId) {
                    booking.setStatus(status);
                    break;
                }
            }
        }

        return updated;
    }

    /**
     * Adds a new payment method - writes to database AND updates cache.
     */
    public synchronized void addPaymentMethod(PaymentMethod method) {
        // Write to database
        paymentRepository.savePaymentMethod(method);

        // Update cache
        paymentMethods.add(method);
    }

    /**
     * Adds a new ticket - writes to database AND updates cache.
     */
    public synchronized void addTicket(Ticket ticket) {
        // Write to database
        ticketRepository.saveTicket(ticket);

        // Update cache
        tickets.add(0, ticket); // Add to front (most recent first)
    }

    /**
     * Updates ticket status - writes to database AND updates cache.
     */
    public synchronized boolean updateTicketStatus(int ticketId, String status) {
        // Write to database
        boolean updated = ticketRepository.updateTicketStatus(ticketId, status);

        // Update cache
        if (updated) {
            for (Ticket ticket : tickets) {
                if (ticket.getId() == ticketId) {
                    ticket.setStatus(status);
                    break;
                }
            }
        }

        return updated;
    }
}
