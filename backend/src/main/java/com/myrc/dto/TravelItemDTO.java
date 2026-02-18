/*
 * myRC - Travel Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.TravelItem;
import com.myrc.model.TravelMoneyAllocation;
import com.myrc.model.TravelTraveller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for TravelItem.
 * Travellers carry individual costs; item-level costs are computed from travellers.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
public class TravelItemDTO {

  private Long id;
  private String name;
  private String description;
  private String emap;
  private String destination;
  private String purpose;
  private String status;
  private String travelType;
  private LocalDate departureDate;
  private LocalDate returnDate;
  private Integer numberOfTravellers;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private List<TravelTravellerDTO> travellers = new ArrayList<>();
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
    dto.setEmap(entity.getEmap());
    dto.setDestination(entity.getDestination());
    dto.setPurpose(entity.getPurpose());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
    dto.setTravelType(entity.getTravelType() != null ? entity.getTravelType().name() : null);
    dto.setDepartureDate(entity.getDepartureDate());
    dto.setReturnDate(entity.getReturnDate());
    dto.setNumberOfTravellers(entity.getNumberOfTravellers());
    dto.setFiscalYearId(entity.getFiscalYear().getId());
    dto.setFiscalYearName(entity.getFiscalYear().getName());
    dto.setResponsibilityCentreId(entity.getFiscalYear().getResponsibilityCentre().getId());
    dto.setResponsibilityCentreName(entity.getFiscalYear().getResponsibilityCentre().getName());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    dto.setActive(entity.getActive());

    // Map travellers
    List<TravelTravellerDTO> travellerDtos = new ArrayList<>();
    if (entity.getTravellers() != null) {
      for (TravelTraveller t : entity.getTravellers()) {
        travellerDtos.add(TravelTravellerDTO.fromEntity(t));
      }
    }
    dto.setTravellers(travellerDtos);

    // Compute costs from travellers
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

  public String getEmap() { return emap; }
  public void setEmap(String emap) { this.emap = emap; }

  public String getDestination() { return destination; }
  public void setDestination(String destination) { this.destination = destination; }

  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getTravelType() { return travelType; }
  public void setTravelType(String travelType) { this.travelType = travelType; }

  public LocalDate getDepartureDate() { return departureDate; }
  public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

  public LocalDate getReturnDate() { return returnDate; }
  public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

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

  public List<TravelTravellerDTO> getTravellers() { return travellers; }
  public void setTravellers(List<TravelTravellerDTO> travellers) { this.travellers = travellers; }

  public List<TravelMoneyAllocationDTO> getMoneyAllocations() { return moneyAllocations; }
  public void setMoneyAllocations(List<TravelMoneyAllocationDTO> moneyAllocations) { this.moneyAllocations = moneyAllocations; }

  public BigDecimal getMoneyAllocationTotalOm() { return moneyAllocationTotalOm; }
  public void setMoneyAllocationTotalOm(BigDecimal moneyAllocationTotalOm) { this.moneyAllocationTotalOm = moneyAllocationTotalOm; }

  public BigDecimal getEstimatedCostCad() { return estimatedCostCad; }
  public void setEstimatedCostCad(BigDecimal estimatedCostCad) { this.estimatedCostCad = estimatedCostCad; }

  public BigDecimal getActualCostCad() { return actualCostCad; }
  public void setActualCostCad(BigDecimal actualCostCad) { this.actualCostCad = actualCostCad; }
}
