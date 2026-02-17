/*
 * myRC - Travel Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.TravelItem;
import com.myrc.model.TravelMoneyAllocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for TravelItem.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
public class TravelItemDTO {

  private Long id;
  private String name;
  private String description;
  private String travelAuthorizationNumber;
  private String referenceNumber;
  private String destination;
  private String purpose;
  private BigDecimal estimatedCost;
  private BigDecimal actualCost;
  private String status;
  private String travelType;
  private String currency;
  private BigDecimal exchangeRate;
  private LocalDate departureDate;
  private LocalDate returnDate;
  private String travellerName;
  private Integer numberOfTravellers;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private List<TravelMoneyAllocationDTO> moneyAllocations = new ArrayList<>();
  private BigDecimal moneyAllocationTotalOm;
  private BigDecimal estimatedCostCad;
  private BigDecimal actualCostCad;

  public TravelItemDTO() {}

  /**
   * Create a DTO from a TravelItem entity.
   */
  public static TravelItemDTO fromEntity(TravelItem entity) {
    TravelItemDTO dto = new TravelItemDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setTravelAuthorizationNumber(entity.getTravelAuthorizationNumber());
    dto.setReferenceNumber(entity.getReferenceNumber());
    dto.setDestination(entity.getDestination());
    dto.setPurpose(entity.getPurpose());
    dto.setEstimatedCost(entity.getEstimatedCost());
    dto.setActualCost(entity.getActualCost());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
    dto.setTravelType(entity.getTravelType() != null ? entity.getTravelType().name() : null);
    dto.setCurrency(entity.getCurrency() != null ? entity.getCurrency().name() : "CAD");
    dto.setExchangeRate(entity.getExchangeRate());
    dto.setDepartureDate(entity.getDepartureDate());
    dto.setReturnDate(entity.getReturnDate());
    dto.setTravellerName(entity.getTravellerName());
    dto.setNumberOfTravellers(entity.getNumberOfTravellers());
    dto.setFiscalYearId(entity.getFiscalYear().getId());
    dto.setFiscalYearName(entity.getFiscalYear().getName());
    dto.setResponsibilityCentreId(entity.getFiscalYear().getResponsibilityCentre().getId());
    dto.setResponsibilityCentreName(entity.getFiscalYear().getResponsibilityCentre().getName());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    dto.setActive(entity.getActive());
    dto.setEstimatedCostCad(entity.getEstimatedCostInCAD());
    dto.setActualCostCad(entity.getActualCostInCAD());

    // Map money allocations
    List<TravelMoneyAllocationDTO> allocDtos = new ArrayList<>();
    BigDecimal totalOm = BigDecimal.ZERO;
    if (entity.getMoneyAllocations() != null) {
      for (TravelMoneyAllocation alloc : entity.getMoneyAllocations()) {
        TravelMoneyAllocationDTO allocDto = TravelMoneyAllocationDTO.fromEntity(alloc);
        allocDtos.add(allocDto);
        totalOm = totalOm.add(alloc.getOmAmount() != null ? alloc.getOmAmount() : BigDecimal.ZERO);
      }
    }
    dto.setMoneyAllocations(allocDtos);
    dto.setMoneyAllocationTotalOm(totalOm);

    return dto;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getTravelAuthorizationNumber() { return travelAuthorizationNumber; }
  public void setTravelAuthorizationNumber(String travelAuthorizationNumber) { this.travelAuthorizationNumber = travelAuthorizationNumber; }

  public String getReferenceNumber() { return referenceNumber; }
  public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

  public String getDestination() { return destination; }
  public void setDestination(String destination) { this.destination = destination; }

  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }

  public BigDecimal getEstimatedCost() { return estimatedCost; }
  public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

  public BigDecimal getActualCost() { return actualCost; }
  public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getTravelType() { return travelType; }
  public void setTravelType(String travelType) { this.travelType = travelType; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public BigDecimal getExchangeRate() { return exchangeRate; }
  public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

  public LocalDate getDepartureDate() { return departureDate; }
  public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

  public LocalDate getReturnDate() { return returnDate; }
  public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

  public String getTravellerName() { return travellerName; }
  public void setTravellerName(String travellerName) { this.travellerName = travellerName; }

  public Integer getNumberOfTravellers() { return numberOfTravellers; }
  public void setNumberOfTravellers(Integer numberOfTravellers) { this.numberOfTravellers = numberOfTravellers; }

  public Long getFiscalYearId() { return fiscalYearId; }
  public void setFiscalYearId(Long fiscalYearId) { this.fiscalYearId = fiscalYearId; }

  public String getFiscalYearName() { return fiscalYearName; }
  public void setFiscalYearName(String fiscalYearName) { this.fiscalYearName = fiscalYearName; }

  public Long getResponsibilityCentreId() { return responsibilityCentreId; }
  public void setResponsibilityCentreId(Long responsibilityCentreId) { this.responsibilityCentreId = responsibilityCentreId; }

  public String getResponsibilityCentreName() { return responsibilityCentreName; }
  public void setResponsibilityCentreName(String responsibilityCentreName) { this.responsibilityCentreName = responsibilityCentreName; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }

  public List<TravelMoneyAllocationDTO> getMoneyAllocations() { return moneyAllocations; }
  public void setMoneyAllocations(List<TravelMoneyAllocationDTO> moneyAllocations) { this.moneyAllocations = moneyAllocations; }

  public BigDecimal getMoneyAllocationTotalOm() { return moneyAllocationTotalOm; }
  public void setMoneyAllocationTotalOm(BigDecimal moneyAllocationTotalOm) { this.moneyAllocationTotalOm = moneyAllocationTotalOm; }

  public BigDecimal getEstimatedCostCad() { return estimatedCostCad; }
  public void setEstimatedCostCad(BigDecimal estimatedCostCad) { this.estimatedCostCad = estimatedCostCad; }

  public BigDecimal getActualCostCad() { return actualCostCad; }
  public void setActualCostCad(BigDecimal actualCostCad) { this.actualCostCad = actualCostCad; }
}
