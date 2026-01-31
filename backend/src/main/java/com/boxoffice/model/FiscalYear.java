/*
 * myRC - Fiscal Year Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
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
 * Entity representing a Fiscal Year associated with a Responsibility Centre.
 * A fiscal year is identified by name (unique per responsibility centre).
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 */
@Entity
@Table(name = "fiscal_years", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "responsibility_centre_id"}, name = "uk_fy_name_rc")
})
public class FiscalYear {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(length = 500)
  private String description;

  @ManyToOne(optional = false)
  @JoinColumn(name = "responsibility_centre_id", nullable = false)
  private ResponsibilityCentre responsibilityCentre;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private Boolean active = true;

  @Column(name = "show_category_filter", nullable = false)
  private Boolean showCategoryFilter = true;

  @Column(name = "group_by_category", nullable = false)
  private Boolean groupByCategory = false;

  @Column(name = "on_target_min", nullable = false)
  private Integer onTargetMin = -2;

  @Column(name = "on_target_max", nullable = false)
  private Integer onTargetMax = 10;

  // Constructors
  public FiscalYear() {}

  public FiscalYear(String name, String description, ResponsibilityCentre responsibilityCentre) {
    this.name = name;
    this.description = description;
    this.responsibilityCentre = responsibilityCentre;
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

  public ResponsibilityCentre getResponsibilityCentre() {
    return responsibilityCentre;
  }

  public void setResponsibilityCentre(ResponsibilityCentre responsibilityCentre) {
    this.responsibilityCentre = responsibilityCentre;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FiscalYear that = (FiscalYear) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "FiscalYear{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", active=" + active +
        '}';
  }
}
