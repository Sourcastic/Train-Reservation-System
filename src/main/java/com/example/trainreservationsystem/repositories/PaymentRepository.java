//mock

package com.example.trainreservationsystem.repositories;
import com.example.trainreservationsystem.utils.Database;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Payment;
import com.example.trainreservationsystem.models.PaymentMethod;

public class PaymentRepository {
  private List<PaymentMethod> mockPaymentMethods = new ArrayList<>();
  private int currentMethodId = 1;

  public PaymentRepository() {
    // Initialize with a default payment method
    mockPaymentMethods.add(new PaymentMethod(1, 1, "VISA", "**** 1234"));
  }

  public List<PaymentMethod> getPaymentMethods(int userId) {
    List<PaymentMethod> userMethods = new ArrayList<>();
    for (PaymentMethod pm : mockPaymentMethods) {
      if (pm.getUserId() == userId) {
        userMethods.add(pm);
      }
    }
    return userMethods;
  }

  public void savePaymentMethod(PaymentMethod method) {
    method.setId(currentMethodId++);
    mockPaymentMethods.add(method);
    System.out.println("Mock: Saved payment method");
  }

  public void savePayment(Payment payment) {
    System.out.println("Mock: Processed payment");
  }
}
