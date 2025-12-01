package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.repositories.StaffComplaintRepository;

import java.util.List;

public class StaffComplaintService {

    private final StaffComplaintRepository repository;

    public StaffComplaintService(StaffComplaintRepository repository) {
        this.repository = repository;
    }

    public List<Complaint> getAllComplaints() {
        return repository.getAllComplaints();
    }

    public void respondToComplaint(int complaintId, String responseText, int staffId) {
        repository.saveComplaintResponse(complaintId, responseText, staffId);
    }

    // Optional: add complaint for testing
    public void addComplaint(Complaint c) {
        repository.addComplaint(c);
    }
}
