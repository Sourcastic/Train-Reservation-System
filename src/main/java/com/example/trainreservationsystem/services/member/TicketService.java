package com.example.trainreservationsystem.services.member;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.member.TicketRepository;

public class TicketService {
    private final TicketRepository ticketRepository = RepositoryFactory.getTicketRepository();

    /**
     * Generates multiple tickets for a booking - one ticket per seat.
     * 
     * @param bookingId   The booking ID
     * @param seatNumbers List of seat numbers for this booking
     * @return List of generated tickets
     */
    public List<Ticket> generateTickets(int bookingId, List<Integer> seatNumbers) {
        List<Ticket> tickets = new ArrayList<>();

        for (int seatNumber : seatNumbers) {
            Ticket ticket = new Ticket();
            ticket.setBookingId(bookingId);
            ticket.setSeatId(seatNumber);
            ticket.setQrCode(UUID.randomUUID().toString().substring(0, 16).toUpperCase());
            ticket.setStatus("VALID");

            // Save to database
            ticketRepository.saveTicket(ticket);
            tickets.add(ticket);
        }

        return tickets;
    }

    /**
     * Gets all tickets for a specific booking.
     * 
     * @param bookingId The booking ID
     * @return List of tickets for this booking
     */
    public List<Ticket> getTicketsByBookingId(int bookingId) {
        return ticketRepository.getTicketsByBookingId(bookingId);
    }
}
