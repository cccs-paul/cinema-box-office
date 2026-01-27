/*
 * myRC - Funding Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Funding Item associated with a Fiscal Year.
 * Each fiscal year can have 0..n funding items.
 */
package com.boxoffice.model;

import java.math.BigDecimal;
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

/**
 * Entity representing a Funding Item associated with a Fiscal Year.
 * Funding items represent budgetary allocations or line items within a fiscal year.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@Entity
@Table(name = "funding_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_fi_name_fy")
})
public class FundingItem {

  /**
   * Enumeration of funding item status values.
   */
  public enum Status {
    DRAFT,
    PENDING,
    APPROVED,
    ACTIVE,
    CLOSED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(precision = 15, scale = 2)
  private BigDecimal budgetAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.DRAFT;

  /**
   * The currency for this funding item. Defaults to CAD.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.CAD;

  /**
   * The exchange rate to convert to CAD. Required when currency is not CAD.
   * A value of 1.0 means 1 unit of the currency equals 1 CAD.
   */
  @Column(precision = 15, scale = 6)
  private java.math.BigDecimal exchangeRate;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Money allocations for this funding item.
   * Each allocation tracks CAP and OM amounts for a specific money type.
   */
  @OneToMany(mappedBy = "fundingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<MoneyAllocation> moneyAllocations = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private Boolean active = true;

  // Constructors
  public FundingItem() {}

  public FundingItem(String name, String description, BigDecimal budgetAmount, 
                     Status status, FiscalYear fiscalYear) {
    this.name = name;
    this.description = description;
    this.budgetAmount = budgetAmount;
    this.status = status != null ? status : Status.DRAFT;
    this.fiscalYear = fiscalYear;
  }

  public FundingItem(String name, String description, FiscalYear fiscalYear) {
    this(name, description, null, Status.DRAFT, fiscalYear);
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

  public BigDecimal getBudgetAmount() {
    return budgetAmount;
  }

  public void setBudgetAmount(BigDecimal budgetAmount) {
    this.budgetAmount = budgetAmount;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency != null ? currency : Currency.CAD;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
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

  public List<MoneyAllocation> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<MoneyAllocation> moneyAllocations) {
    this.moneyAllocations = moneyAllocations != null ? moneyAllocations : new ArrayList<>();
  }

  /**
   * Add a money allocation to this funding item.
   *
   * @param allocation the money allocation to add
   */
  public void addMoneyAllocation(MoneyAllocation allocation) {
    if (moneyAllocations == null) {
      moneyAllocations = new ArrayList<>();
    }
    moneyAllocations.add(allocation);
    allocation.setFundingItem(this);
  }

  /**
   * Remove a money allocation from this funding item.
   *
   * @param allocation the money allocation to remove
   */
  public void removeMoneyAllocation(MoneyAllocation allocation) {
    if (moneyAllocations != null) {
      moneyAllocations.remove(allocation);
      allocation.setFundingItem(null);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FundingItem that = (FundingItem) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "FundingItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", budgetAmount=" + budgetAmount +
        ", currency=" + currency +
        ", exchangeRate=" + exchangeRate +
        ", status=" + status +
        ", active=" + active +
        '}';
  }
}
