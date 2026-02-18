/*
 * myRC - Training Participant DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.myrc.model.TrainingParticipant;

/**
 * Data Transfer Object for Training Participant.
 */
public class TrainingParticipantDTO {

  private Long id;
  private String name;
  private String eco;
  private String status;
  private BigDecimal estimatedCost;
  private BigDecimal finalCost;
  private String estimatedCurrency;
  private BigDecimal estimatedExchangeRate;
  private String finalCurrency;
  private BigDecimal finalExchangeRate;
  private BigDecimal estimatedCostCad;
  private BigDecimal finalCostCad;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public TrainingParticipantDTO() {}

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getEco() { return eco; }
  public void setEco(String eco) { this.eco = eco; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public BigDecimal getEstimatedCost() { return estimatedCost; }
  public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

  public BigDecimal getFinalCost() { return finalCost; }
  public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

  public String getEstimatedCurrency() { return estimatedCurrency; }
  public void setEstimatedCurrency(String estimatedCurrency) { this.estimatedCurrency = estimatedCurrency; }

  public BigDecimal getEstimatedExchangeRate() { return estimatedExchangeRate; }
  public void setEstimatedExchangeRate(BigDecimal estimatedExchangeRate) { this.estimatedExchangeRate = estimatedExchangeRate; }

  public String getFinalCurrency() { return finalCurrency; }
  public void setFinalCurrency(String finalCurrency) { this.finalCurrency = finalCurrency; }

  public BigDecimal getFinalExchangeRate() { return finalExchangeRate; }
  public void setFinalExchangeRate(BigDecimal finalExchangeRate) { this.finalExchangeRate = finalExchangeRate; }

  public BigDecimal getEstimatedCostCad() { return estimatedCostCad; }
  public void setEstimatedCostCad(BigDecimal estimatedCostCad) { this.estimatedCostCad = estimatedCostCad; }

  public BigDecimal getFinalCostCad() { return finalCostCad; }
  public void setFinalCostCad(BigDecimal finalCostCad) { this.finalCostCad = finalCostCad; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  /**
   * Create a DTO from a TrainingParticipant entity.
   */
  public static TrainingParticipantDTO fromEntity(TrainingParticipant entity) {
    TrainingParticipantDTO dto = new TrainingParticipantDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setEco(entity.getEco());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "PLANNED");
    dto.setEstimatedCost(entity.getEstimatedCost());
    dto.setFinalCost(entity.getFinalCost());
    dto.setEstimatedCurrency(entity.getEstimatedCurrency() != null ? entity.getEstimatedCurrency().name() : "CAD");
    dto.setEstimatedExchangeRate(entity.getEstimatedExchangeRate());
    dto.setFinalCurrency(entity.getFinalCurrency() != null ? entity.getFinalCurrency().name() : "CAD");
    dto.setFinalExchangeRate(entity.getFinalExchangeRate());
    dto.setEstimatedCostCad(entity.getEstimatedCostInCAD());
    dto.setFinalCostCad(entity.getFinalCostInCAD());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    return dto;
  }
}
