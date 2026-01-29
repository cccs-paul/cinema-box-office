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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
  private String source;
  private String comments;
  private String currency;
  private BigDecimal exchangeRate;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private Long categoryId;
  private String categoryName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private List<MoneyAllocationDTO> moneyAllocations;

  // Computed totals from money allocations
  private BigDecimal totalCap;
  private BigDecimal totalOm;
  private BigDecimal totalAmount;

  // Constructors
  public FundingItemDTO() {
    this.moneyAllocations = new ArrayList<>();
  }

  public FundingItemDTO(Long id, String name, String description,
                        String source, String comments, String currency, BigDecimal exchangeRate,
                        Long fiscalYearId, String fiscalYearName,
                        Long responsibilityCentreId, String responsibilityCentreName,
                        Long categoryId, String categoryName,
                        LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active,
                        List<MoneyAllocationDTO> moneyAllocations) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.source = source;
    this.comments = comments;
    this.currency = currency;
    this.exchangeRate = exchangeRate;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.responsibilityCentreId = responsibilityCentreId;
    this.responsibilityCentreName = responsibilityCentreName;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
    this.moneyAllocations = moneyAllocations != null ? moneyAllocations : new ArrayList<>();
    calculateTotals();
  }

  /**
   * Calculate total CAP, OM, and overall total from money allocations.
   */
  private void calculateTotals() {
    this.totalCap = BigDecimal.ZERO;
    this.totalOm = BigDecimal.ZERO;
    if (this.moneyAllocations != null) {
      for (MoneyAllocationDTO allocation : this.moneyAllocations) {
        if (allocation.getCapAmount() != null) {
          this.totalCap = this.totalCap.add(allocation.getCapAmount());
        }
        if (allocation.getOmAmount() != null) {
          this.totalOm = this.totalOm.add(allocation.getOmAmount());
        }
      }
    }
    this.totalAmount = this.totalCap.add(this.totalOm);
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
    List<MoneyAllocationDTO> allocations = new ArrayList<>();
    if (fundingItem.getMoneyAllocations() != null) {
      allocations = fundingItem.getMoneyAllocations().stream()
          .map(MoneyAllocationDTO::fromEntity)
          .collect(Collectors.toList());
    }
    return new FundingItemDTO(
        fundingItem.getId(),
        fundingItem.getName(),
        fundingItem.getDescription(),
        fundingItem.getSource() != null ? fundingItem.getSource().name() : null,
        fundingItem.getComments(),
        fundingItem.getCurrency() != null ? fundingItem.getCurrency().getCode() : "CAD",
        fundingItem.getExchangeRate(),
        fundingItem.getFiscalYear() != null ? fundingItem.getFiscalYear().getId() : null,
        fundingItem.getFiscalYear() != null ? fundingItem.getFiscalYear().getName() : null,
        fundingItem.getFiscalYear() != null && fundingItem.getFiscalYear().getResponsibilityCentre() != null
            ? fundingItem.getFiscalYear().getResponsibilityCentre().getId() : null,
        fundingItem.getFiscalYear() != null && fundingItem.getFiscalYear().getResponsibilityCentre() != null
            ? fundingItem.getFiscalYear().getResponsibilityCentre().getName() : null,
        fundingItem.getCategory() != null ? fundingItem.getCategory().getId() : null,
        fundingItem.getCategory() != null ? fundingItem.getCategory().getName() : null,
        fundingItem.getCreatedAt(),
        fundingItem.getUpdatedAt(),
        fundingItem.getActive(),
        allocations
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

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
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

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
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

  public List<MoneyAllocationDTO> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<MoneyAllocationDTO> moneyAllocations) {
    this.moneyAllocations = moneyAllocations != null ? moneyAllocations : new ArrayList<>();
    calculateTotals();
  }

  public BigDecimal getTotalCap() {
    return totalCap;
  }

  public BigDecimal getTotalOm() {
    return totalOm;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }
}
