package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.shared.Complaint;

import java.util.ArrayList;
import java.util.List;

public class StaffComplaintRepository {

    // Mock in-memory storage for complaints
    private final List<Complaint> complaintsDB = new ArrayList<>();

    // Mock in-memory storage for staff responses
    private final List<String> responsesDB = new ArrayList<>();

    // Add a complaint (for testing/demo purposes)
    public void addComplaint(Complaint complaint) {
        complaintsDB.add(complaint);
    }

    // Get all complaints (for staff TableView)
    public List<Complaint> getAllComplaints() {
        return new ArrayList<>(complaintsDB);
    }

    // Save a staff response
    public void saveComplaintResponse(int complaintId, String responseText, int staffId) {
        responsesDB.add("Staff " + staffId + " responded to complaint " + complaintId + ": " + responseText);
        System.out.println("Saved response: " + responseText + " for complaint " + complaintId);
    }

    // Optional: get all responses (for testing)
    public List<String> getAllResponses() {
        return new ArrayList<>(responsesDB);
    }
}
