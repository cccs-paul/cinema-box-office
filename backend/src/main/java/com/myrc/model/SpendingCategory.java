/*
 * myRC - Spending Category Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Spending Category associated with a Fiscal Year.
 * Categories are used to group spending items for better organization.
 * Default categories include: Compute, GPUs, Storage, Software Licenses,
 * Small Procurement, and Contractors.
 */
package com.myrc.model;

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
import jakarta.persistence.Version;

/**
 * Entity representing a Spending Category associated with a Fiscal Year.
 * Categories are used to group spending items for better organization.
 * Each fiscal year has default categories, and users can create additional ones.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Entity
@Table(name = "spending_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_spending_category_name_fy")
})
public class SpendingCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The name of the spending category (e.g., "Compute", "GPUs", "Storage").
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
   * Default categories cannot be deleted by users.
   */
  @Column(nullable = false)
  private Boolean isDefault = false;

  /**
   * Display order for sorting categories in the UI.
   */
  @Column(nullable = false)
  private Integer displayOrder = 0;

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
  public SpendingCategory() {}

  public SpendingCategory(String name, String description, FiscalYear fiscalYear) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = false;
  }

  public SpendingCategory(String name, String description, FiscalYear fiscalYear, Boolean isDefault) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
  }

  public SpendingCategory(String name, String description, FiscalYear fiscalYear, Boolean isDefault, Integer displayOrder) {
    this.name = name;
    this.description = description;
    this.fiscalYear = fiscalYear;
    this.isDefault = isDefault;
    this.displayOrder = displayOrder;
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
    return "SpendingCategory{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", isDefault=" + isDefault +
        ", displayOrder=" + displayOrder +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
