/*
 * myRC - Fiscal Year DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.FiscalYear;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Fiscal Year.
 * Used for transferring fiscal year data between layers.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 */
public class FiscalYearDTO {

  private Long id;
  private String name;
  private String description;
  private Long responsibilityCentreId;
  private String responsibilityCentreName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private Boolean showCategoryFilter;
  private Boolean groupByCategory;
  private Integer onTargetMin;
  private Integer onTargetMax;

  // Constructors
  public FiscalYearDTO() {}

  public FiscalYearDTO(Long id, String name, String description,
                       Long responsibilityCentreId, String responsibilityCentreName,
                       LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active,
                       Boolean showCategoryFilter, Boolean groupByCategory,
                       Integer onTargetMin, Integer onTargetMax) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.responsibilityCentreId = responsibilityCentreId;
    this.responsibilityCentreName = responsibilityCentreName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
    this.showCategoryFilter = showCategoryFilter;
    this.groupByCategory = groupByCategory;
    this.onTargetMin = onTargetMin;
    this.onTargetMax = onTargetMax;
  }

  /**
   * Creates a FiscalYearDTO from a FiscalYear entity.
   *
   * @param fiscalYear the fiscal year entity
   * @return the fiscal year DTO
   */
  public static FiscalYearDTO fromEntity(FiscalYear fiscalYear) {
    if (fiscalYear == null) {
      return null;
    }
    return new FiscalYearDTO(
        fiscalYear.getId(),
        fiscalYear.getName(),
        fiscalYear.getDescription(),
        fiscalYear.getResponsibilityCentre() != null ? fiscalYear.getResponsibilityCentre().getId() : null,
        fiscalYear.getResponsibilityCentre() != null ? fiscalYear.getResponsibilityCentre().getName() : null,
        fiscalYear.getCreatedAt(),
        fiscalYear.getUpdatedAt(),
        fiscalYear.getActive(),
        fiscalYear.getShowCategoryFilter(),
        fiscalYear.getGroupByCategory(),
        fiscalYear.getOnTargetMin(),
        fiscalYear.getOnTargetMax()
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

  public Boolean getShowCategoryFilter() {
    return showCategoryFilter;
  }

  public void setShowCategoryFilter(Boolean showCategoryFilter) {
    this.showCategoryFilter = showCategoryFilter;
  }

  public Boolean getGroupByCategory() {
    return groupByCategory;
  }

  public void setGroupByCategory(Boolean groupByCategory) {
    this.groupByCategory = groupByCategory;
  }

  public Integer getOnTargetMin() {
    return onTargetMin;
  }

  public void setOnTargetMin(Integer onTargetMin) {
    this.onTargetMin = onTargetMin;
  }

  public Integer getOnTargetMax() {
    return onTargetMax;
  }

  public void setOnTargetMax(Integer onTargetMax) {
    this.onTargetMax = onTargetMax;
  }
}
