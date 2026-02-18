/*
 * myRC - Travel Traveller DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.myrc.model.TravelTraveller;

/**
 * Data Transfer Object for Travel Traveller.
 */
public class TravelTravellerDTO {

  private Long id;
  private String name;
  private String taac;
  private BigDecimal estimatedCost;
  private BigDecimal finalCost;
  private String currency;
  private BigDecimal exchangeRate;
  private String approvalStatus;
  private BigDecimal estimatedCostCad;
  private BigDecimal finalCostCad;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public TravelTravellerDTO() {}

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getTaac() { return taac; }
  public void setTaac(String taac) { this.taac = taac; }

  public BigDecimal getEstimatedCost() { return estimatedCost; }
  public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

  public BigDecimal getFinalCost() { return finalCost; }
  public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public BigDecimal getExchangeRate() { return exchangeRate; }
  public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

  public String getApprovalStatus() { return approvalStatus; }
  public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

  public BigDecimal getEstimatedCostCad() { return estimatedCostCad; }
  public void setEstimatedCostCad(BigDecimal estimatedCostCad) { this.estimatedCostCad = estimatedCostCad; }

  public BigDecimal getFinalCostCad() { return finalCostCad; }
  public void setFinalCostCad(BigDecimal finalCostCad) { this.finalCostCad = finalCostCad; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  /**
   * Create a DTO from a TravelTraveller entity.
   */
  public static TravelTravellerDTO fromEntity(TravelTraveller entity) {
    TravelTravellerDTO dto = new TravelTravellerDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setTaac(entity.getTaac());
    dto.setEstimatedCost(entity.getEstimatedCost());
    dto.setFinalCost(entity.getFinalCost());
    dto.setCurrency(entity.getCurrency() != null ? entity.getCurrency().name() : "CAD");
    dto.setExchangeRate(entity.getExchangeRate());
    dto.setApprovalStatus(entity.getApprovalStatus() != null ? entity.getApprovalStatus().name() : "PLANNED");
    dto.setEstimatedCostCad(entity.getEstimatedCostInCAD());
    dto.setFinalCostCad(entity.getFinalCostInCAD());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    return dto;
  }
}
