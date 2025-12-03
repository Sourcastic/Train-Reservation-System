package com.example.trainreservationsystem.services.member;

import java.util.UUID;

import com.example.trainreservationsystem.models.member.Ticket;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.member.TicketRepository;

public class TicketService {
    private final TicketRepository ticketRepository = RepositoryFactory.getTicketRepository();

    public Ticket generateTicket(int bookingId) {
        Ticket ticket = new Ticket();
        ticket.setBookingId(bookingId);
        ticket.setQrCode(UUID.randomUUID().toString().substring(0, 16).toUpperCase());
        ticket.setStatus("VALID");

        // Save to database
        ticketRepository.saveTicket(ticket);

        return ticket;
    }

    public Ticket getTicketByBookingId(int bookingId) {
        // Fetch from database
        Ticket ticket = ticketRepository.getTicketByBookingId(bookingId);

        // If no ticket exists, generate one
        if (ticket == null) {
            ticket = generateTicket(bookingId);
        }

        return ticket;
    }
}
