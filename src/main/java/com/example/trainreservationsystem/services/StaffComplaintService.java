package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.repositories.StaffComplaintRepository;

import java.util.List;

public class StaffComplaintService {

    private final StaffComplaintRepository repository;

    public StaffComplaintService(StaffComplaintRepository repository) {
        this.repository = repository;
    }

    // Return all complaints as an array
    public Complaint[] getAllComplaints() {
        List<Complaint> complaintsList = repository.getAllComplaints();
        return complaintsList.toArray(new Complaint[0]);
    }

    // Respond to a complaint
    public void respondToComplaint(int complaintId, String staffResponse, String responderName) {
        if (staffResponse == null || staffResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Response cannot be empty");
        }

        // Trim response and pass to repository
        repository.saveStaffResponse(complaintId, staffResponse.trim(), responderName);
    }

}
