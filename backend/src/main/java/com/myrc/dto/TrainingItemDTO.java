/*
 * myRC - Training Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.TrainingItem;
import com.myrc.model.TrainingMoneyAllocation;
import com.myrc.model.TrainingParticipant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for TrainingItem.
 * Participants carry individual costs; item-level costs are computed from participants.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
public class TrainingItemDTO {

  private Long id;
  private String name;
  private String description;
  private String provider;
  private String status;
  private String trainingType;
  private String format;
  private LocalDate startDate;
  private LocalDate endDate;
  private String location;
  private Integer numberOfParticipants;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private List<TrainingParticipantDTO> participants = new ArrayList<>();
  private List<TrainingMoneyAllocationDTO> moneyAllocations = new ArrayList<>();
  private BigDecimal moneyAllocationTotalOm;
  private BigDecimal estimatedCostCad;
  private BigDecimal actualCostCad;

  public TrainingItemDTO() {}

  /**
   * Create a DTO from a TrainingItem entity.
   */
  public static TrainingItemDTO fromEntity(TrainingItem entity) {
    TrainingItemDTO dto = new TrainingItemDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setProvider(entity.getProvider());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
    dto.setTrainingType(entity.getTrainingType() != null ? entity.getTrainingType().name() : null);
    dto.setFormat(entity.getFormat() != null ? entity.getFormat().name() : null);
    dto.setStartDate(entity.getStartDate());
    dto.setEndDate(entity.getEndDate());
    dto.setLocation(entity.getLocation());
    dto.setNumberOfParticipants(entity.getNumberOfParticipants());
    dto.setFiscalYearId(entity.getFiscalYear().getId());
    dto.setFiscalYearName(entity.getFiscalYear().getName());
    dto.setResponsibilityCentreId(entity.getFiscalYear().getResponsibilityCentre().getId());
    dto.setResponsibilityCentreName(entity.getFiscalYear().getResponsibilityCentre().getName());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    dto.setActive(entity.getActive());

    // Map participants
    List<TrainingParticipantDTO> participantDtos = new ArrayList<>();
    if (entity.getParticipants() != null) {
      for (TrainingParticipant p : entity.getParticipants()) {
        participantDtos.add(TrainingParticipantDTO.fromEntity(p));
      }
    }
    dto.setParticipants(participantDtos);

    // Compute costs from participants
    dto.setEstimatedCostCad(entity.getEstimatedCostInCAD());
    dto.setActualCostCad(entity.getActualCostInCAD());

    // Map money allocations
    List<TrainingMoneyAllocationDTO> allocDtos = new ArrayList<>();
    BigDecimal totalOm = BigDecimal.ZERO;
    if (entity.getMoneyAllocations() != null) {
      for (TrainingMoneyAllocation alloc : entity.getMoneyAllocations()) {
        TrainingMoneyAllocationDTO allocDto = TrainingMoneyAllocationDTO.fromEntity(alloc);
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

  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getTrainingType() { return trainingType; }
  public void setTrainingType(String trainingType) { this.trainingType = trainingType; }

  public String getFormat() { return format; }
  public void setFormat(String format) { this.format = format; }

  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }

  public Integer getNumberOfParticipants() { return numberOfParticipants; }
  public void setNumberOfParticipants(Integer numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }

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

  public List<TrainingParticipantDTO> getParticipants() { return participants; }
  public void setParticipants(List<TrainingParticipantDTO> participants) { this.participants = participants; }

  public List<TrainingMoneyAllocationDTO> getMoneyAllocations() { return moneyAllocations; }
  public void setMoneyAllocations(List<TrainingMoneyAllocationDTO> moneyAllocations) { this.moneyAllocations = moneyAllocations; }

  public BigDecimal getMoneyAllocationTotalOm() { return moneyAllocationTotalOm; }
  public void setMoneyAllocationTotalOm(BigDecimal moneyAllocationTotalOm) { this.moneyAllocationTotalOm = moneyAllocationTotalOm; }

  public BigDecimal getEstimatedCostCad() { return estimatedCostCad; }
  public void setEstimatedCostCad(BigDecimal estimatedCostCad) { this.estimatedCostCad = estimatedCostCad; }

  public BigDecimal getActualCostCad() { return actualCostCad; }
  public void setActualCostCad(BigDecimal actualCostCad) { this.actualCostCad = actualCostCad; }
}
