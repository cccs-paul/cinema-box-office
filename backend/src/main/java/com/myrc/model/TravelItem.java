/*
 * myRC - Travel Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-16
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Travel Item (trip) associated with a Fiscal Year.
 * Each fiscal year can have 0..n travel items.
 * Travel items track trip costs with OM-only money type breakdowns.
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
 * Entity representing a Travel Item (trip) associated with a Fiscal Year.
 * Travel items represent business trips including transportation, accommodation,
 * meals, and incidentals within a fiscal year.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "travel_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_trv_name_fy")
})
public class TravelItem {

  /**
   * Enumeration of travel item status values.
   */
  public enum Status {
    PLANNED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
  }

  /**
   * Enumeration of travel type values.
   */
  public enum TravelType {
    DOMESTIC,
    INTERNATIONAL,
    LOCAL,
    CONFERENCE,
    TRAINING,
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
   * Travel authorization number.
   */
  @Column(length = 100)
  private String travelAuthorizationNumber;

  /**
   * Reference number for expense claim.
   */
  @Column(length = 100)
  private String referenceNumber;

  /**
   * Destination city/location.
   */
  @Column(length = 500)
  private String destination;

  /**
   * Purpose of the trip.
   */
  @Column(length = 2000)
  private String purpose;

  /**
   * Estimated total cost of the trip.
   */
  @Column(precision = 15, scale = 2)
  private BigDecimal estimatedCost;

  /**
   * Actual/final total cost of the trip.
   */
  @Column(precision = 15, scale = 2)
  private BigDecimal actualCost;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PLANNED;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TravelType travelType = TravelType.OTHER;

  /**
   * The currency for this travel item. Defaults to CAD.
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
   * Departure date of the trip.
   */
  @Column
  private LocalDate departureDate;

  /**
   * Return date of the trip.
   */
  @Column
  private LocalDate returnDate;

  /**
   * Traveller name(s).
   */
  @Column(length = 500)
  private String travellerName;

  /**
   * Number of travellers.
   */
  @Column
  private Integer numberOfTravellers;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Money allocations for this travel item (OM only).
   */
  @OneToMany(mappedBy = "travelItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TravelMoneyAllocation> moneyAllocations = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private Boolean active = true;

  // Constructors
  public TravelItem() {}

  public TravelItem(String name, String description, Status status, FiscalYear fiscalYear) {
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

  public String getTravelAuthorizationNumber() {
    return travelAuthorizationNumber;
  }

  public void setTravelAuthorizationNumber(String travelAuthorizationNumber) {
    this.travelAuthorizationNumber = travelAuthorizationNumber;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
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

  public TravelType getTravelType() {
    return travelType;
  }

  public void setTravelType(TravelType travelType) {
    this.travelType = travelType;
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

  public LocalDate getDepartureDate() {
    return departureDate;
  }

  public void setDepartureDate(LocalDate departureDate) {
    this.departureDate = departureDate;
  }

  public LocalDate getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDate returnDate) {
    this.returnDate = returnDate;
  }

  public String getTravellerName() {
    return travellerName;
  }

  public void setTravellerName(String travellerName) {
    this.travellerName = travellerName;
  }

  public Integer getNumberOfTravellers() {
    return numberOfTravellers;
  }

  public void setNumberOfTravellers(Integer numberOfTravellers) {
    this.numberOfTravellers = numberOfTravellers;
  }

  public FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  public void setFiscalYear(FiscalYear fiscalYear) {
    this.fiscalYear = fiscalYear;
  }

  public List<TravelMoneyAllocation> getMoneyAllocations() {
    return moneyAllocations;
  }

  public void setMoneyAllocations(List<TravelMoneyAllocation> moneyAllocations) {
    this.moneyAllocations = moneyAllocations;
  }

  public void addMoneyAllocation(TravelMoneyAllocation allocation) {
    moneyAllocations.add(allocation);
    allocation.setTravelItem(this);
  }

  public void removeMoneyAllocation(TravelMoneyAllocation allocation) {
    moneyAllocations.remove(allocation);
    allocation.setTravelItem(null);
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
    return "TravelItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", travelAuthorizationNumber='" + travelAuthorizationNumber + '\'' +
        ", referenceNumber='" + referenceNumber + '\'' +
        ", destination='" + destination + '\'' +
        ", estimatedCost=" + estimatedCost +
        ", actualCost=" + actualCost +
        ", status=" + status +
        ", travelType=" + travelType +
        ", currency=" + currency +
        ", exchangeRate=" + exchangeRate +
        ", departureDate=" + departureDate +
        ", returnDate=" + returnDate +
        ", travellerName='" + travellerName + '\'' +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
