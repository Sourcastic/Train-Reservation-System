//mock db
package com.example.trainreservationsystem.repositories;


import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.utils.Database;

public class ComplaintRepository {
  public void saveComplaint(Complaint complaint) {
    // Mock save - do nothing
    System.out.println("Mock: Saved complaint: " + complaint.getSubject());
  }
}
