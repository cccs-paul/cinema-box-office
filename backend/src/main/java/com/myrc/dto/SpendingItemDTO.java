/*
 * myRC - Spending Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Spending Item.
 * Used for transferring spending item data between layers.
 */
package com.myrc.dto;

import com.myrc.model.SpendingItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Spending Item.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
public class SpendingItemDTO {

  private Long id;
  private String name;
  private String description;
  private String vendor;
  private String referenceNumber;
  private BigDecimal amount;
  private String status;
  private String currency;
  private BigDecimal exchangeRate;
  private Long categoryId;
  private String categoryName;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private Long procurementItemId;
  private String procurementItemName;
  private BigDecimal procurementFinalPrice;
  private BigDecimal procurementQuotedPrice;
  private String procurementPriceCurrency;
  private BigDecimal procurementFinalPriceCad;
  private BigDecimal procurementQuotedPriceCad;
  // Tracking event info (for non-procurement-linked items)
  private Integer eventCount;
  private String mostRecentEventType;
  private String mostRecentEventDate;
  // Procurement tracking info (for procurement-linked items)
  private String procurementTrackingStatus;
  private String procurementMostRecentEventType;
  private String procurementMostRecentEventDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private List<SpendingMoneyAllocationDTO> moneyAllocations;

  // Constructors
  public SpendingItemDTO() {
    this.moneyAllocations = new ArrayList<>();
  }

  public SpendingItemDTO(Long id, String name, String description, String vendor, String referenceNumber,
                         BigDecimal amount, String status, String currency, BigDecimal exchangeRate,
                         Long categoryId, String categoryName, Long fiscalYearId, String fiscalYearName,
                         Long responsibilityCentreId, String responsibilityCentreName,
                         LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active,
                         List<SpendingMoneyAllocationDTO> moneyAllocations) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.vendor = vendor;
    this.referenceNumber = referenceNumber;
    this.amount = amount;
    this.status = status;
    this.currency = currency;
    this.exchangeRate = exchangeRate;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.responsibilityCentreId = responsibilityCentreId;
    this.responsibilityCentreName = responsibilityCentreName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
    this.moneyAllocations = moneyAllocations != null ? moneyAllocations : new ArrayList<>();
  }

  /**
   * Creates a SpendingItemDTO from a SpendingItem entity.
   *
   * @param spendingItem the spending item entity
   * @return the spending item DTO
   */
  public static SpendingItemDTO fromEntity(SpendingItem spendingItem) {
    if (spendingItem == null) {
      return null;
    }
    List<SpendingMoneyAllocationDTO> allocations = new ArrayList<>();
    if (spendingItem.getMoneyAllocations() != null) {
      allocations = spendingItem.getMoneyAllocations().stream()
          .map(SpendingMoneyAllocationDTO::fromEntity)
          .collect(Collectors.toList());
    }
    SpendingItemDTO dto = new SpendingItemDTO(
        spendingItem.getId(),
        spendingItem.getName(),
        spendingItem.getDescription(),
        spendingItem.getVendor(),
        spendingItem.getReferenceNumber(),
        spendingItem.getAmount(),
        spendingItem.getStatus() != null ? spendingItem.getStatus().name() : null,
        spendingItem.getCurrency() != null ? spendingItem.getCurrency().getCode() : "CAD",
        spendingItem.getExchangeRate(),
        spendingItem.getCategory() != null ? spendingItem.getCategory().getId() : null,
        spendingItem.getCategory() != null ? spendingItem.getCategory().getName() : null,
        spendingItem.getFiscalYear() != null ? spendingItem.getFiscalYear().getId() : null,
        spendingItem.getFiscalYear() != null ? spendingItem.getFiscalYear().getName() : null,
        spendingItem.getFiscalYear() != null && spendingItem.getFiscalYear().getResponsibilityCentre() != null
            ? spendingItem.getFiscalYear().getResponsibilityCentre().getId() : null,
        spendingItem.getFiscalYear() != null && spendingItem.getFiscalYear().getResponsibilityCentre() != null
            ? spendingItem.getFiscalYear().getResponsibilityCentre().getName() : null,
        spendingItem.getCreatedAt(),
        spendingItem.getUpdatedAt(),
        spendingItem.getActive(),
        allocations
    );
    // Set procurement item details if linked
    if (spendingItem.getProcurementItem() != null) {
      var procItem = spendingItem.getProcurementItem();
      dto.setProcurementItemId(procItem.getId());
      dto.setProcurementItemName(procItem.getName());
      dto.setProcurementFinalPrice(procItem.getFinalPrice());
      dto.setProcurementQuotedPrice(procItem.getQuotedPrice());
      // Use final price currency if available, otherwise quoted price currency
      String priceCurrency = procItem.getFinalPriceCurrency() != null 
          ? procItem.getFinalPriceCurrency().getCode() 
          : (procItem.getQuotedPriceCurrency() != null 
              ? procItem.getQuotedPriceCurrency().getCode() 
              : null);
      dto.setProcurementPriceCurrency(priceCurrency);
      dto.setProcurementFinalPriceCad(procItem.getFinalPriceCad());
      dto.setProcurementQuotedPriceCad(procItem.getQuotedPriceCad());
    }
    return dto;
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

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public Long getProcurementItemId() {
    return procurementItemId;
  }

  public void setProcurementItemId(Long procurementItemId) {
    this.procurementItemId = procurementItemId;
  }

  public String getProcurementItemName() {
    return procurementItemName;
  }

  public void setProcurementItemName(String procurementItemName) {
    this.procurementItemName = procurementItemName;
  }

  public BigDecimal getProcurementFinalPrice() {
    return procurementFinalPrice;
  }

  public void setProcurementFinalPrice(BigDecimal procurementFinalPrice) {
    this.procurementFinalPrice = procurementFinalPrice;
  }

  public BigDecimal getProcurementQuotedPrice() {
    return procurementQuotedPrice;
  }

  public void setProcurementQuotedPrice(BigDecimal procurementQuotedPrice) {
    this.procurementQuotedPrice = procurementQuotedPrice;
  }

  public String getProcurementPriceCurrency() {
    return procurementPriceCurrency;
  }

  public void setProcurementPriceCurrency(String procurementPriceCurrency) {
    this.procurementPriceCurrency = procurementPriceCurrency;
  }

  public BigDecimal getProcurementFinalPriceCad() {
    return procurementFinalPriceCad;
  }

  public void setProcurementFinalPriceCad(BigDecimal procurementFinalPriceCad) {
    this.procurementFinalPriceCad = procurementFinalPriceCad;
  }

  public BigDecimal getProcurementQuotedPriceCad() {
    return procurementQuotedPriceCad;
  }

  public void setProcurementQuotedPriceCad(BigDecimal procurementQuotedPriceCad) {
    this.procurementQuotedPriceCad = procurementQuotedPriceCad;
  }

  // Event tracking getters/setters (for non-procurement-linked items)
  public Integer getEventCount() {
    return eventCount;
  }

  public void setEventCount(Integer eventCount) {
    this.eventCount = eventCount;
  }

  public String getMostRecentEventType() {
    return mostRecentEventType;
  }

  public void setMostRecentEventType(String mostRecentEventType) {
    this.mostRecentEventType = mostRecentEventType;
  }

  public String getMostRecentEventDate() {
    return mostRecentEventDate;
  }

  public void setMostRecentEventDate(String mostRecentEventDate) {
    this.mostRecentEventDate = mostRecentEventDate;
  }

  // Procurement tracking getters/setters (for procurement-linked items)
  public String getProcurementTrackingStatus() {
    return procurementTrackingStatus;
  }

  public void setProcurementTrackingStatus(String procurementTrackingStatus) {
    this.procurementTrackingStatus = procurementTrackingStatus;
  }

  public String getProcurementMostRecentEventType() {
    return procurementMostRecentEventType;
  }

  public void setProcurementMostRecentEventType(String procurementMostRecentEventType) {
    this.procurementMostRecentEventType = procurementMostRecentEventType;
  }

  public String getProcurementMostRecentEventDate() {
    return procurementMostRecentEventDate;
  }

  public void setProcurementMostRecentEventDate(String procurementMostRecentEventDate) {
    this.procurementMostRecentEventDate = procurementMostRecentEventDate;
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

  public List<SpendingMoneyAllocationDTO> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<SpendingMoneyAllocationDTO> moneyAllocations) {
    this.moneyAllocations = moneyAllocations;
  }
}
