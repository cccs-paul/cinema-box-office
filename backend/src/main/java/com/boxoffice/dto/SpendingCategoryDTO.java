/*
 * myRC - Spending Category DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for SpendingCategory entity.
 */
package com.boxoffice.dto;

import com.boxoffice.model.SpendingCategory;

/**
 * Data Transfer Object for SpendingCategory entity.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
public class SpendingCategoryDTO {

  private Long id;
  private String name;
  private String description;
  private Boolean isDefault;
  private Integer displayOrder;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Boolean active;

  // Constructors
  public SpendingCategoryDTO() {}

  public SpendingCategoryDTO(Long id, String name, String description, Boolean isDefault,
                              Integer displayOrder, Long fiscalYearId, String fiscalYearName, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isDefault = isDefault;
    this.displayOrder = displayOrder;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.active = active;
  }

  /**
   * Create a DTO from an entity.
   *
   * @param entity the SpendingCategory entity
   * @return the DTO
   */
  public static SpendingCategoryDTO fromEntity(SpendingCategory entity) {
    if (entity == null) {
      return null;
    }
    return new SpendingCategoryDTO(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getIsDefault(),
        entity.getDisplayOrder(),
        entity.getFiscalYear() != null ? entity.getFiscalYear().getId() : null,
        entity.getFiscalYear() != null ? entity.getFiscalYear().getName() : null,
        entity.getActive()
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

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
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

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
