package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Ticket;
import java.util.UUID;

public class TicketService {
  public Ticket generateTicket(int bookingId) {
    Ticket ticket = new Ticket();
    ticket.setBookingId(bookingId);
    ticket.setQrCode(UUID.randomUUID().toString().substring(0, 16).toUpperCase());
    ticket.setStatus("VALID");
    return ticket;
  }

  public Ticket getTicketByBookingId(int bookingId) {
    // In real app, fetch from DB. For mock, generate on demand
    return generateTicket(bookingId);
  }
}
