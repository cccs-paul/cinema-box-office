/*
 * myRC - Spending Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Spending Item associated with a Fiscal Year.
 * Each fiscal year can have 0..n spending items, each belonging to a category.
 */
package com.myrc.model;

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
import jakarta.persistence.Version;

/**
 * Entity representing a Spending Item associated with a Fiscal Year.
 * Spending items represent actual expenditures or planned spending within a fiscal year.
 * Each spending item is associated with a category for organization.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Entity
@Table(name = "spending_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_si_name_fy")
})
public class SpendingItem {

  /**
   * Enumeration of spending item status values.
   */
  public enum Status {
    DRAFT,
    PENDING,
    APPROVED,
    COMMITTED,
    PAID,
    CANCELLED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 1000)
  private String description;

  /**
   * Vendor or supplier name for this spending item.
   */
  @Column(length = 200)
  private String vendor;

  /**
   * Reference number (e.g., PO number, invoice number).
   */
  @Column(length = 100)
  private String referenceNumber;

  @Column(precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.DRAFT;

  /**
   * The currency for this spending item. Defaults to CAD.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.CAD;

  /**
   * The exchange rate to convert to CAD. Required when currency is not CAD.
   * A value of 1.0 means 1 unit of the currency equals 1 CAD.
   */
  @Column(precision = 15, scale = 6)
  private BigDecimal exchangeRate;

  /**
   * The category this spending item belongs to.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Optional link to a procurement item.
   * If set, this spending item is associated with the procurement process.
   * If null, this is a discrete (standalone) spending item.
   */
  @ManyToOne(optional = true)
  @JoinColumn(name = "procurement_item_id", nullable = true)
  private ProcurementItem procurementItem;

  /**
   * Money allocations for this spending item.
   * Each allocation tracks CAP and OM amounts for a specific money type.
   */
  @OneToMany(mappedBy = "spendingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<SpendingMoneyAllocation> moneyAllocations = new ArrayList<>();

  /**
   * Tracking events for this spending item.
   * Only used when the item is NOT linked to a procurement item.
   */
  @OneToMany(mappedBy = "spendingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<SpendingEvent> events = new ArrayList<>();

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
  public SpendingItem() {}

  public SpendingItem(String name, String description, BigDecimal amount, 
                      Status status, Category category, FiscalYear fiscalYear) {
    this.name = name;
    this.description = description;
    this.amount = amount;
    this.status = status != null ? status : Status.DRAFT;
    this.category = category;
    this.fiscalYear = fiscalYear;
  }

  public SpendingItem(String name, String description, Category category, FiscalYear fiscalYear) {
    this(name, description, null, Status.DRAFT, category, fiscalYear);
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

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
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
    this.currency = currency;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  public void setFiscalYear(FiscalYear fiscalYear) {
    this.fiscalYear = fiscalYear;
  }

  public ProcurementItem getProcurementItem() {
    return procurementItem;
  }

  public void setProcurementItem(ProcurementItem procurementItem) {
    this.procurementItem = procurementItem;
  }

  /**
   * Check if this spending item is linked to a procurement item.
   *
   * @return true if linked to procurement, false if discrete
   */
  public boolean isLinkedToProcurement() {
    return procurementItem != null;
  }

  public List<SpendingMoneyAllocation> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<SpendingMoneyAllocation> moneyAllocations) {
    this.moneyAllocations = moneyAllocations;
  }

  /**
   * Add a money allocation to this spending item.
   *
   * @param allocation the allocation to add
   */
  public void addMoneyAllocation(SpendingMoneyAllocation allocation) {
    moneyAllocations.add(allocation);
    allocation.setSpendingItem(this);
  }

  /**
   * Remove a money allocation from this spending item.
   *
   * @param allocation the allocation to remove
   */
  public void removeMoneyAllocation(SpendingMoneyAllocation allocation) {
    moneyAllocations.remove(allocation);
    allocation.setSpendingItem(null);
  }

  public List<SpendingEvent> getEvents() {
    return events;
  }

  public void setEvents(List<SpendingEvent> events) {
    this.events = events;
  }

  /**
   * Add a tracking event to this spending item.
   *
   * @param event the event to add
   */
  public void addEvent(SpendingEvent event) {
    events.add(event);
    event.setSpendingItem(this);
  }

  /**
   * Remove a tracking event from this spending item.
   *
   * @param event the event to remove
   */
  public void removeEvent(SpendingEvent event) {
    events.remove(event);
    event.setSpendingItem(null);
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
   * Get the total amount in CAD, applying exchange rate if necessary.
   *
   * @return the amount in CAD
   */
  public BigDecimal getAmountInCAD() {
    if (amount == null) {
      return BigDecimal.ZERO;
    }
    if (currency == Currency.CAD || exchangeRate == null) {
      return amount;
    }
    return amount.multiply(exchangeRate);
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
    return "SpendingItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", vendor='" + vendor + '\'' +
        ", referenceNumber='" + referenceNumber + '\'' +
        ", amount=" + amount +
        ", status=" + status +
        ", currency=" + currency +
        ", exchangeRate=" + exchangeRate +
        ", category=" + (category != null ? category.getName() : null) +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", procurementItem=" + (procurementItem != null ? procurementItem.getName() : null) +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
