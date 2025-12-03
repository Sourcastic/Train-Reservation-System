package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Ticket;
import com.example.trainreservationsystem.repositories.TicketRepository;

import java.util.UUID;

public class TicketService {
    private final TicketRepository ticketRepository = new TicketRepository();

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
