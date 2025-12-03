package com.example.trainreservationsystem.models.admin;

/**
 * Model for cancellation and refund policies.
 * Defines rules for when bookings can be cancelled and refund percentages.
 */
public class CancellationPolicy {
  private int id;
  private String name;
  private String description;

  // Cancellation allowed up to X hours before departure
  private int hoursBeforeDeparture;

  // Refund percentage (0-100)
  private double refundPercentage;

  // Whether cancellation is allowed after this time
  private boolean allowCancellation;

  // Minimum hours before departure to allow cancellation
  private int minHoursBeforeDeparture;

  public CancellationPolicy() {
  }

  public CancellationPolicy(int id, String name, String description, int hoursBeforeDeparture,
      double refundPercentage, boolean allowCancellation, int minHoursBeforeDeparture) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.hoursBeforeDeparture = hoursBeforeDeparture;
    this.refundPercentage = refundPercentage;
    this.allowCancellation = allowCancellation;
    this.minHoursBeforeDeparture = minHoursBeforeDeparture;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getHoursBeforeDeparture() {
    return hoursBeforeDeparture;
  }

  public void setHoursBeforeDeparture(int hoursBeforeDeparture) {
    this.hoursBeforeDeparture = hoursBeforeDeparture;
  }

  public double getRefundPercentage() {
    return refundPercentage;
  }

  public void setRefundPercentage(double refundPercentage) {
    this.refundPercentage = refundPercentage;
  }

  public boolean isAllowCancellation() {
    return allowCancellation;
  }

  public void setAllowCancellation(boolean allowCancellation) {
    this.allowCancellation = allowCancellation;
  }

  public int getMinHoursBeforeDeparture() {
    return minHoursBeforeDeparture;
  }

  public void setMinHoursBeforeDeparture(int minHoursBeforeDeparture) {
    this.minHoursBeforeDeparture = minHoursBeforeDeparture;
  }

  /**
   * Checks if cancellation is allowed based on hours until departure.
   */
  public boolean canCancel(long hoursUntilDeparture) {
    if (!allowCancellation) {
      return false;
    }
    return hoursUntilDeparture >= minHoursBeforeDeparture && hoursUntilDeparture <= hoursBeforeDeparture;
  }

  /**
   * Calculates refund amount based on original amount and hours until departure.
   */
  public double calculateRefund(double originalAmount, long hoursUntilDeparture) {
    if (!canCancel(hoursUntilDeparture)) {
      return 0;
    }
    return originalAmount * (refundPercentage / 100.0);
  }
}
