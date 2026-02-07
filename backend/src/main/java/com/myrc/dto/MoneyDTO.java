/*
 * myRC - Money DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Money entity.
 * Used for transferring money data between layers.
 */
package com.myrc.dto;

import com.myrc.model.Money;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Money.
 * Used for transferring money data between layers.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
public class MoneyDTO {

  private Long id;
  private String code;
  private String name;
  private String description;
  private Boolean isDefault;
  private Long fiscalYearId;
  private String fiscalYearName;
  private Long responsibilityCentreId;
  private Integer displayOrder;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;

  // Derived labels for the two parts of money
  private String capLabel;
  private String omLabel;

  /**
   * Whether this money type can be deleted.
   * False for default money (AB) and for money types with non-zero allocations.
   */
  private Boolean canDelete;

  // Constructors
  public MoneyDTO() {}

  public MoneyDTO(Long id, String code, String name, String description,
                  Boolean isDefault, Long fiscalYearId, String fiscalYearName,
                  Long responsibilityCentreId, Integer displayOrder,
                  LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.description = description;
    this.isDefault = isDefault;
    this.fiscalYearId = fiscalYearId;
    this.fiscalYearName = fiscalYearName;
    this.responsibilityCentreId = responsibilityCentreId;
    this.displayOrder = displayOrder;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
    this.capLabel = code + " (CAP)";
    this.omLabel = code + " (OM)";
  }

  /**
   * Creates a MoneyDTO from a Money entity.
   *
   * @param money the money entity
   * @return the money DTO
   */
  public static MoneyDTO fromEntity(Money money) {
    if (money == null) {
      return null;
    }
    return new MoneyDTO(
        money.getId(),
        money.getCode(),
        money.getName(),
        money.getDescription(),
        money.getIsDefault(),
        money.getFiscalYear() != null ? money.getFiscalYear().getId() : null,
        money.getFiscalYear() != null ? money.getFiscalYear().getName() : null,
        money.getFiscalYear() != null && money.getFiscalYear().getResponsibilityCentre() != null
            ? money.getFiscalYear().getResponsibilityCentre().getId() : null,
        money.getDisplayOrder(),
        money.getCreatedAt(),
        money.getUpdatedAt(),
        money.getActive()
    );
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
    this.capLabel = code + " (CAP)";
    this.omLabel = code + " (OM)";
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

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
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

  public String getCapLabel() {
    return capLabel;
  }

  public void setCapLabel(String capLabel) {
    this.capLabel = capLabel;
  }

  public String getOmLabel() {
    return omLabel;
  }

  public void setOmLabel(String omLabel) {
    this.omLabel = omLabel;
  }

  public Boolean getCanDelete() {
    return canDelete;
  }

  public void setCanDelete(Boolean canDelete) {
    this.canDelete = canDelete;
  }

  @Override
  public String toString() {
    return "MoneyDTO{" +
        "id=" + id +
        ", code='" + code + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", isDefault=" + isDefault +
        ", fiscalYearId=" + fiscalYearId +
        ", fiscalYearName='" + fiscalYearName + '\'' +
        ", responsibilityCentreId=" + responsibilityCentreId +
        ", displayOrder=" + displayOrder +
        ", active=" + active +
        ", capLabel='" + capLabel + '\'' +
        ", omLabel='" + omLabel + '\'' +
        ", canDelete=" + canDelete +
        '}';
  }
}
