/*
 * myRC - Money Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Money type associated with a Fiscal Year.
 * Each Money has two parts: Capital (CAP) and O&M (OM).
 * Every fiscal year has a default "AB" money, plus additional custom monies.
 */
package com.boxoffice.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entity representing a Money type associated with a Fiscal Year.
 * Each Money consists of two parts: Capital (CAP) and O&M (OM).
 * Funding and Spending items have n monies associated with them.
 * Every FY has a default "AB" money, and RC owners can configure additional monies.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@Entity
@Table(name = "monies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "fiscal_year_id"}, name = "uk_money_code_fy")
})
public class Money {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The short code for this money type (e.g., "AB", "OA", "WCF").
   */
  @Column(nullable = false, length = 10)
  private String code;

  /**
   * The descriptive name for this money type (e.g., "A-Base", "Operating Allotment").
   */
  @Column(nullable = false, length = 100)
  private String name;

  /**
   * Optional description of this money type.
   */
  @Column(length = 500)
  private String description;

  /**
   * Whether this is a system-defined default money (like AB).
   * System defaults cannot be deleted by users.
   */
  @Column(nullable = false)
  private Boolean isDefault = false;

  /**
   * The fiscal year this money belongs to.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private Boolean active = true;

  /**
   * Display order for sorting monies in the UI.
   */
  @Column(nullable = false)
  private Integer displayOrder = 0;

  // Constructors
  public Money() {}

  public Money(String code, String name, String description, FiscalYear fiscalYear) {
    this.code = code;
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
  }

  public Money(String code, String name, String description, FiscalYear fiscalYear, Boolean isDefault) {
    this.code = code;
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
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

  public FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  public void setFiscalYear(FiscalYear fiscalYear) {
    this.fiscalYear = fiscalYear;
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

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  /**
   * Get the CAP (Capital) label for this money type.
   *
   * @return the CAP label (e.g., "AB (CAP)")
   */
  public String getCapLabel() {
    return code + " (CAP)";
  }

  /**
   * Get the OM (O&M) label for this money type.
   *
   * @return the OM label (e.g., "AB (OM)")
   */
  public String getOmLabel() {
    return code + " (OM)";
  }

  @Override
  public String toString() {
    return "Money{" +
        "id=" + id +
        ", code='" + code + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", isDefault=" + isDefault +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", displayOrder=" + displayOrder +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
