package com.example.trainreservationsystem.models.admin;

import java.time.LocalDate;

/**
 * Model for discount codes/promo vouchers.
 * Supports three types: PROMO, VOUCHER, DISCOUNT_CODE
 */
public class Discount {
  public enum DiscountType {
    PROMO, VOUCHER, DISCOUNT_CODE
  }

  private int id;
  private Integer scheduleId; // Nullable - null means applies to all schedules
  private String name;
  private String code;
  private DiscountType type;
  private String description;
  private double discountPercentage;
  private double discountAmount;
  private LocalDate validFrom;
  private LocalDate validTo;
  private boolean isActive;
  private int maxUses;
  private int currentUses;

  public Discount() {
  }

  public Discount(int id, Integer scheduleId, String name, String code, DiscountType type,
      String description, double discountPercentage, double discountAmount,
      LocalDate validFrom, LocalDate validTo, boolean isActive, int maxUses, int currentUses) {
    this.id = id;
    this.scheduleId = scheduleId;
    this.name = name;
    this.code = code;
    this.type = type;
    this.description = description;
    this.discountPercentage = discountPercentage;
    this.discountAmount = discountAmount;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.isActive = isActive;
    this.maxUses = maxUses;
    this.currentUses = currentUses;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getDiscountPercentage() {
    return discountPercentage;
  }

  public void setDiscountPercentage(double discountPercentage) {
    this.discountPercentage = discountPercentage;
  }

  public double getDiscountAmount() {
    return discountAmount;
  }

  public void setDiscountAmount(double discountAmount) {
    this.discountAmount = discountAmount;
  }

  public LocalDate getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(LocalDate validFrom) {
    this.validFrom = validFrom;
  }

  public LocalDate getValidTo() {
    return validTo;
  }

  public void setValidTo(LocalDate validTo) {
    this.validTo = validTo;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public int getMaxUses() {
    return maxUses;
  }

  public void setMaxUses(int maxUses) {
    this.maxUses = maxUses;
  }

  public int getCurrentUses() {
    return currentUses;
  }

  public void setCurrentUses(int currentUses) {
    this.currentUses = currentUses;
  }

  public Integer getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(Integer scheduleId) {
    this.scheduleId = scheduleId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DiscountType getType() {
    return type;
  }

  public void setType(DiscountType type) {
    this.type = type;
  }

  /**
   * Calculates the discount amount for a given price.
   * Returns the discount amount (either percentage-based or fixed amount).
   */
  public double calculateDiscount(double originalPrice) {
    if (discountPercentage > 0) {
      return originalPrice * (discountPercentage / 100.0);
    } else if (discountAmount > 0) {
      return Math.min(discountAmount, originalPrice); // Don't exceed original price
    }
    return 0;
  }

  /**
   * Checks if the discount code is valid for use.
   */
  public boolean isValid() {
    LocalDate today = LocalDate.now();
    return isActive
        && (validFrom == null || !today.isBefore(validFrom))
        && (validTo == null || !today.isAfter(validTo))
        && (maxUses == 0 || currentUses < maxUses);
  }

  /**
   * Checks if the discount is valid for a specific schedule.
   * Returns true if scheduleId is null (applies to all) or matches the given
   * scheduleId.
   */
  public boolean isValidForSchedule(Integer scheduleId) {
    return this.scheduleId == null || this.scheduleId.equals(scheduleId);
  }
}
