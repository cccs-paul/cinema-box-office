/*
 * myRC - Training Money Allocation DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.TrainingMoneyAllocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for TrainingMoneyAllocation.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
public class TrainingMoneyAllocationDTO {

  private Long id;
  private Long moneyId;
  private String moneyName;
  private String moneyCode;
  private Boolean isDefault;
  private BigDecimal omAmount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public TrainingMoneyAllocationDTO() {}

  public static TrainingMoneyAllocationDTO fromEntity(TrainingMoneyAllocation entity) {
    TrainingMoneyAllocationDTO dto = new TrainingMoneyAllocationDTO();
    dto.setId(entity.getId());
    dto.setMoneyId(entity.getMoney().getId());
    dto.setMoneyName(entity.getMoney().getName());
    dto.setMoneyCode(entity.getMoney().getCode());
    dto.setIsDefault(entity.getMoney().getIsDefault());
    dto.setOmAmount(entity.getOmAmount());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    return dto;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getMoneyId() { return moneyId; }
  public void setMoneyId(Long moneyId) { this.moneyId = moneyId; }

  public String getMoneyName() { return moneyName; }
  public void setMoneyName(String moneyName) { this.moneyName = moneyName; }

  public String getMoneyCode() { return moneyCode; }
  public void setMoneyCode(String moneyCode) { this.moneyCode = moneyCode; }

  public Boolean getIsDefault() { return isDefault; }
  public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

  public BigDecimal getOmAmount() { return omAmount; }
  public void setOmAmount(BigDecimal omAmount) { this.omAmount = omAmount; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
