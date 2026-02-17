/*
 * myRC - Training Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-16
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Training Item associated with a Fiscal Year.
 * Each fiscal year can have 0..n training items.
 * Training items track employee training costs with OM-only money type breakdowns.
 */
package com.myrc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Entity representing a Training Item associated with a Fiscal Year.
 * Training items represent training activities such as courses, conferences,
 * certifications, or workshops within a fiscal year.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "training_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_ti_name_fy")
})
public class TrainingItem {

  /**
   * Enumeration of training item status values.
   */
  public enum Status {
    PLANNED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
  }

  /**
   * Enumeration of training type values.
   */
  public enum TrainingType {
    COURSE,
    CONFERENCE,
    CERTIFICATION,
    WORKSHOP,
    SEMINAR,
    ONLINE,
    OTHER
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  /**
   * Training provider or institution name.
   */
  @Column(length = 200)
  private String provider;

  /**
   * Reference or registration number.
   */
  @Column(length = 100)
  private String referenceNumber;

  /**
   * Estimated cost of the training.
   */
  @Column(precision = 15, scale = 2)
  private BigDecimal estimatedCost;

  /**
   * Actual/final cost of the training.
   */
  @Column(precision = 15, scale = 2)
  private BigDecimal actualCost;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PLANNED;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TrainingType trainingType = TrainingType.OTHER;

  /**
   * The currency for this training item. Defaults to CAD.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.CAD;

  /**
   * The exchange rate to convert to CAD. Required when currency is not CAD.
   */
  @Column(precision = 15, scale = 6)
  private BigDecimal exchangeRate;

  /**
   * Start date of the training.
   */
  @Column
  private LocalDate startDate;

  /**
   * End date of the training.
   */
  @Column
  private LocalDate endDate;

  /**
   * Location of the training.
   */
  @Column(length = 500)
  private String location;

  /**
   * Name(s) of the employee(s) attending the training.
   */
  @Column(length = 500)
  private String employeeName;

  /**
   * Number of participants.
   */
  @Column
  private Integer numberOfParticipants;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Money allocations for this training item (OM only).
   * Each allocation tracks the OM amount for a specific money type.
   */
  @OneToMany(mappedBy = "trainingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TrainingMoneyAllocation> moneyAllocations = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Version for optimistic locking.
   */
  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private Boolean active = true;

  // Constructors
  public TrainingItem() {}

  public TrainingItem(String name, String description, Status status, FiscalYear fiscalYear) {
    this.name = name;
    this.description = description;
    this.status = status != null ? status : Status.PLANNED;
    this.fiscalYear = fiscalYear;
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

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public BigDecimal getEstimatedCost() {
    return estimatedCost;
  }

  public void setEstimatedCost(BigDecimal estimatedCost) {
    this.estimatedCost = estimatedCost;
  }

  public BigDecimal getActualCost() {
    return actualCost;
  }

  public void setActualCost(BigDecimal actualCost) {
    this.actualCost = actualCost;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public TrainingType getTrainingType() {
    return trainingType;
  }

  public void setTrainingType(TrainingType trainingType) {
    this.trainingType = trainingType;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public Integer getNumberOfParticipants() {
    return numberOfParticipants;
  }

  public void setNumberOfParticipants(Integer numberOfParticipants) {
    this.numberOfParticipants = numberOfParticipants;
  }

  public FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  public void setFiscalYear(FiscalYear fiscalYear) {
    this.fiscalYear = fiscalYear;
  }

  public List<TrainingMoneyAllocation> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<TrainingMoneyAllocation> moneyAllocations) {
    this.moneyAllocations = moneyAllocations;
  }

  /**
   * Add a money allocation to this training item.
   *
   * @param allocation the allocation to add
   */
  public void addMoneyAllocation(TrainingMoneyAllocation allocation) {
    moneyAllocations.add(allocation);
    allocation.setTrainingItem(this);
  }

  /**
   * Remove a money allocation from this training item.
   *
   * @param allocation the allocation to remove
   */
  public void removeMoneyAllocation(TrainingMoneyAllocation allocation) {
    moneyAllocations.remove(allocation);
    allocation.setTrainingItem(null);
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

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  /**
   * Get the estimated cost in CAD, applying exchange rate if necessary.
   *
   * @return the estimated cost in CAD
   */
  public BigDecimal getEstimatedCostInCAD() {
    if (estimatedCost == null) {
      return BigDecimal.ZERO;
    }
    if (currency == Currency.CAD || exchangeRate == null) {
      return estimatedCost;
    }
    return estimatedCost.multiply(exchangeRate);
  }

  /**
   * Get the actual cost in CAD, applying exchange rate if necessary.
   *
   * @return the actual cost in CAD
   */
  public BigDecimal getActualCostInCAD() {
    if (actualCost == null) {
      return BigDecimal.ZERO;
    }
    if (currency == Currency.CAD || exchangeRate == null) {
      return actualCost;
    }
    return actualCost.multiply(exchangeRate);
  }

  @Override
  public String toString() {
    return "TrainingItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", provider='" + provider + '\'' +
        ", referenceNumber='" + referenceNumber + '\'' +
        ", estimatedCost=" + estimatedCost +
        ", actualCost=" + actualCost +
        ", status=" + status +
        ", trainingType=" + trainingType +
        ", currency=" + currency +
        ", exchangeRate=" + exchangeRate +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", location='" + location + '\'' +
        ", employeeName='" + employeeName + '\'' +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
