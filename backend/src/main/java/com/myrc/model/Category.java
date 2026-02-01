/*
 * myRC - Category Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Category associated with a Fiscal Year.
 * Categories are used to group both funding and spending items for better organization.
 * Default categories include: Compute, GPUs, Storage, Software Licenses,
 * Small Procurement, and Contractors.
 */
package com.myrc.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Entity representing a Category associated with a Fiscal Year.
 * Categories are used to group both funding and spending items for better organization.
 * Each fiscal year has default categories, and users can create additional custom ones.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_category_name_fy")
})
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The name of the category (e.g., "Compute", "GPUs", "Storage").
   */
  @Column(nullable = false, length = 100)
  private String name;

  /**
   * Optional description of the category.
   */
  @Column(length = 500)
  private String description;

  /**
   * Whether this is a system-defined default category.
   * Default categories cannot be deleted or renamed by users.
   */
  @Column(nullable = false)
  private Boolean isDefault = false;

  /**
   * Display order for sorting categories in the UI.
   */
  @Column(nullable = false)
  private Integer displayOrder = 0;

  /**
   * The allowed funding type for this category.
   * Determines which money allocation fields (CAP, OM, or both) are available.
   * Defaults to BOTH (allowing both CAP and OM amounts).
   */
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private FundingType fundingType = FundingType.BOTH;

  /**
   * The fiscal year this category belongs to.
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

  /**
   * Version for optimistic locking.
   * Prevents lost updates when multiple users edit the same record.
   */
  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private Boolean active = true;

  // Constructors
  public Category() {}

  public Category(String name, String description, FiscalYear fiscalYear) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = false;
  }

  public Category(String name, String description, FiscalYear fiscalYear, Boolean isDefault) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
  }

  public Category(String name, String description, FiscalYear fiscalYear, Boolean isDefault, Integer displayOrder) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
    this.displayOrder = displayOrder;
  }

  public Category(String name, String description, FiscalYear fiscalYear, Boolean isDefault, Integer displayOrder, FundingType fundingType) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
    this.displayOrder = displayOrder;
    this.fundingType = fundingType != null ? fundingType : FundingType.BOTH;
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

  public FundingType getFundingType() {
    return fundingType;
  }

  public void setFundingType(FundingType fundingType) {
    this.fundingType = fundingType != null ? fundingType : FundingType.BOTH;
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

  /**
   * Get the version for optimistic locking.
   *
   * @return the version number
   */
  public Long getVersion() {
    return version;
  }

  /**
   * Set the version for optimistic locking.
   *
   * @param version the version number
   */
  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "Category{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", isDefault=" + isDefault +
        ", displayOrder=" + displayOrder +
        ", fundingType=" + fundingType +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
