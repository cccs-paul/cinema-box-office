/*
 * myRC - Funding Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Funding Item.
 * Used for transferring funding item data between layers.
 */
package com.boxoffice.dto;

import com.boxoffice.model.FundingItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Funding Item.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
public class FundingItemDTO {

  private Long id;
  private String name;
  private String description;
  private BigDecimal budgetAmount;
  private String status;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;

  // Constructors
  public FundingItemDTO() {}

  public FundingItemDTO(Long id, String name, String description, BigDecimal budgetAmount,
                        String status, Long fiscalYearId, String fiscalYearName,
                        Long responsibilityCentreId, String responsibilityCentreName,
                        LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.budgetAmount = budgetAmount;
    this.status = status;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.responsibilityCentreId = responsibilityCentreId;
    this.responsibilityCentreName = responsibilityCentreName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
  }

  /**
   * Creates a FundingItemDTO from a FundingItem entity.
   *
   * @param fundingItem the funding item entity
   * @return the funding item DTO
   */
  public static FundingItemDTO fromEntity(FundingItem fundingItem) {
    if (fundingItem == null) {
      return null;
    }
    return new FundingItemDTO(
        fundingItem.getId(),
        fundingItem.getName(),
        fundingItem.getDescription(),
        fundingItem.getBudgetAmount(),
        fundingItem.getStatus() != null ? fundingItem.getStatus().name() : null,
        fundingItem.getFiscalYear() != null ? fundingItem.getFiscalYear().getId() : null,
        fundingItem.getFiscalYear() != null ? fundingItem.getFiscalYear().getName() : null,
        fundingItem.getFiscalYear() != null && fundingItem.getFiscalYear().getResponsibilityCentre() != null
            ? fundingItem.getFiscalYear().getResponsibilityCentre().getId() : null,
        fundingItem.getFiscalYear() != null && fundingItem.getFiscalYear().getResponsibilityCentre() != null
            ? fundingItem.getFiscalYear().getResponsibilityCentre().getName() : null,
        fundingItem.getCreatedAt(),
        fundingItem.getUpdatedAt(),
        fundingItem.getActive()
    );
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public BigDecimal getBudgetAmount() {
    return budgetAmount;
  }

  public void setBudgetAmount(BigDecimal budgetAmount) {
    this.budgetAmount = budgetAmount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getFiscalYearId() {
    return fiscalYearId;
  }

  public void setFiscalYearId(Long fiscalYearId) {
    this.fiscalYearId = fiscalYearId;
  }

  public String getFiscalYearName() {
    return fiscalYearName;
  }

  public void setFiscalYearName(String fiscalYearName) {
    this.fiscalYearName = fiscalYearName;
  }

  public Long getResponsibilityCentreId() {
    return responsibilityCentreId;
  }

  public void setResponsibilityCentreId(Long responsibilityCentreId) {
    this.responsibilityCentreId = responsibilityCentreId;
  }

  public String getResponsibilityCentreName() {
    return responsibilityCentreName;
  }

  public void setResponsibilityCentreName(String responsibilityCentreName) {
    this.responsibilityCentreName = responsibilityCentreName;
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

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
