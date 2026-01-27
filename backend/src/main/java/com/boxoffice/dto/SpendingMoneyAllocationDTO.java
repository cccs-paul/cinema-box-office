/*
 * myRC - Spending Money Allocation DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Spending Money Allocation.
 * Used for transferring spending money allocation data between layers.
 */
package com.boxoffice.dto;

import com.boxoffice.model.SpendingMoneyAllocation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Spending Money Allocation.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
public class SpendingMoneyAllocationDTO {

  private Long id;
  private Long moneyId;
  private String moneyName;
  private Boolean isDefault;
  private BigDecimal capAmount;
  private BigDecimal omAmount;
  private BigDecimal totalAmount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Constructors
  public SpendingMoneyAllocationDTO() {
  }

  public SpendingMoneyAllocationDTO(Long id, Long moneyId, String moneyName, Boolean isDefault,
                                     BigDecimal capAmount, BigDecimal omAmount, BigDecimal totalAmount,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.moneyId = moneyId;
    this.moneyName = moneyName;
    this.isDefault = isDefault;
    this.capAmount = capAmount;
    this.omAmount = omAmount;
    this.totalAmount = totalAmount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Creates a SpendingMoneyAllocationDTO from a SpendingMoneyAllocation entity.
   *
   * @param allocation the spending money allocation entity
   * @return the spending money allocation DTO
   */
  public static SpendingMoneyAllocationDTO fromEntity(SpendingMoneyAllocation allocation) {
    if (allocation == null) {
      return null;
    }
    return new SpendingMoneyAllocationDTO(
        allocation.getId(),
        allocation.getMoney() != null ? allocation.getMoney().getId() : null,
        allocation.getMoney() != null ? allocation.getMoney().getName() : null,
        allocation.getMoney() != null ? allocation.getMoney().getIsDefault() : false,
        allocation.getCapAmount(),
        allocation.getOmAmount(),
        allocation.getTotalAmount(),
        allocation.getCreatedAt(),
        allocation.getUpdatedAt()
    );
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMoneyId() {
    return moneyId;
  }

  public void setMoneyId(Long moneyId) {
    this.moneyId = moneyId;
  }

  public String getMoneyName() {
    return moneyName;
  }

  public void setMoneyName(String moneyName) {
    this.moneyName = moneyName;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public BigDecimal getCapAmount() {
    return capAmount;
  }

  public void setCapAmount(BigDecimal capAmount) {
    this.capAmount = capAmount;
  }

  public BigDecimal getOmAmount() {
    return omAmount;
  }

  public void setOmAmount(BigDecimal omAmount) {
    this.omAmount = omAmount;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
