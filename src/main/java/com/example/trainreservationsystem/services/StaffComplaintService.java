package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.repositories.StaffComplaintRepository;

import java.util.Arrays;

public class StaffComplaintService {

    private final StaffComplaintRepository repository;

    public StaffComplaintService(StaffComplaintRepository repository) {
        this.repository = repository;
    }

    public Complaint[] getAllComplaints() {
        return repository.findAllComplaints();
    }

    public void respondToComplaint(int complaintId, String staffResponse, String responderName) {
        // Basic validation (trim)
        if (staffResponse == null || staffResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Response cannot be empty");
        }
        repository.saveStaffResponse(complaintId, staffResponse.trim(), responderName == null ? "Staff" : responderName);
    }
}
