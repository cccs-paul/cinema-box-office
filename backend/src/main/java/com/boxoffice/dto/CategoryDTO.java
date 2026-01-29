/*
 * myRC - Category DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Category entity.
 */
package com.boxoffice.dto;

import com.boxoffice.model.Category;
import com.boxoffice.model.FundingType;

/**
 * Data Transfer Object for Category entity.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
public class CategoryDTO {

  private Long id;
  private String name;
  private String description;
  private Boolean isDefault;
  private Integer displayOrder;
  private String fundingType;
  private Boolean allowsCap;
  private Boolean allowsOm;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Boolean active;

  // Constructors
  public CategoryDTO() {}

  /**
   * Constructor without fundingType (for backward compatibility).
   * Defaults to BOTH funding type.
   */
  public CategoryDTO(Long id, String name, String description, Boolean isDefault,
                     Integer displayOrder, Long fiscalYearId, String fiscalYearName, Boolean active) {
    this(id, name, description, isDefault, displayOrder, FundingType.BOTH, fiscalYearId, fiscalYearName, active);
  }

  public CategoryDTO(Long id, String name, String description, Boolean isDefault,
                     Integer displayOrder, FundingType fundingType, Long fiscalYearId, 
                     String fiscalYearName, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isDefault = isDefault;
    this.displayOrder = displayOrder;
    this.fundingType = fundingType != null ? fundingType.name() : FundingType.BOTH.name();
    this.allowsCap = fundingType != null ? fundingType.allowsCap() : true;
    this.allowsOm = fundingType != null ? fundingType.allowsOm() : true;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.active = active;
  }

  /**
   * Create a DTO from an entity.
   *
   * @param entity the Category entity
   * @return the DTO
   */
  public static CategoryDTO fromEntity(Category entity) {
    if (entity == null) {
      return null;
    }
    return new CategoryDTO(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getIsDefault(),
        entity.getDisplayOrder(),
        entity.getFundingType(),
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

  public String getFundingType() {
    return fundingType;
  }

  public void setFundingType(String fundingType) {
    this.fundingType = fundingType;
    // Update allowsCap and allowsOm based on fundingType
    if (fundingType != null) {
      try {
        FundingType ft = FundingType.valueOf(fundingType);
        this.allowsCap = ft.allowsCap();
        this.allowsOm = ft.allowsOm();
      } catch (IllegalArgumentException e) {
        // Default to BOTH if invalid
        this.allowsCap = true;
        this.allowsOm = true;
      }
    }
  }

  public Boolean getAllowsCap() {
    return allowsCap;
  }

  public void setAllowsCap(Boolean allowsCap) {
    this.allowsCap = allowsCap;
  }

  public Boolean getAllowsOm() {
    return allowsOm;
  }

  public void setAllowsOm(Boolean allowsOm) {
    this.allowsOm = allowsOm;
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
