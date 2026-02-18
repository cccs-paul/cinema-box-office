/*
 * myRC - Travel Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-16
 * Version: 2.0.0
 *
 * Description:
 * Entity representing a Travel Item (trip) associated with a Fiscal Year.
 * Each fiscal year can have 0..n travel items.
 * Travel items have 1..n travellers with individual costs.
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
 * Travel items represent business trips. Each has 1..n travellers with
 * individual costs, TAAC numbers, and approval statuses.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "travel_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_trv_name_fy")
})
public class TravelItem {

  public enum Status {
    PLANNED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
  }

  public enum TravelType {
    DOMESTIC,
    NORTH_AMERICA,
    INTERNATIONAL,
    LOCAL
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  /**
   * EMAP number (replaces reference number).
   */
  @Column(length = 100)
  private String emap;

  @Column(length = 500)
  private String destination;

  @Column(length = 2000)
  private String purpose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PLANNED;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TravelType travelType = TravelType.DOMESTIC;

  @Column
  private LocalDate departureDate;

  @Column
  private LocalDate returnDate;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Travellers for this travel item.
   * Each traveller has their own name, TAAC, costs, currency, exchange rate, and approval status.
   */
  @OneToMany(mappedBy = "travelItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TravelTraveller> travellers = new ArrayList<>();

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
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getEmap() { return emap; }
  public void setEmap(String emap) { this.emap = emap; }

  public String getDestination() { return destination; }
  public void setDestination(String destination) { this.destination = destination; }

  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }

  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }

  public TravelType getTravelType() { return travelType; }
  public void setTravelType(TravelType travelType) { this.travelType = travelType; }

  public LocalDate getDepartureDate() { return departureDate; }
  public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

  public LocalDate getReturnDate() { return returnDate; }
  public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

  public FiscalYear getFiscalYear() { return fiscalYear; }
  public void setFiscalYear(FiscalYear fiscalYear) { this.fiscalYear = fiscalYear; }

  public List<TravelTraveller> getTravellers() { return travellers; }
  public void setTravellers(List<TravelTraveller> travellers) { this.travellers = travellers; }

  public List<TravelMoneyAllocation> getMoneyAllocations() { return moneyAllocations; }
  public void setMoneyAllocations(List<TravelMoneyAllocation> moneyAllocations) { this.moneyAllocations = moneyAllocations; }

  public void addTraveller(TravelTraveller traveller) {
    travellers.add(traveller);
    traveller.setTravelItem(this);
  }

  public void removeTraveller(TravelTraveller traveller) {
    travellers.remove(traveller);
    traveller.setTravelItem(null);
  }

  public void addMoneyAllocation(TravelMoneyAllocation allocation) {
    moneyAllocations.add(allocation);
    allocation.setTravelItem(this);
  }

  public void removeMoneyAllocation(TravelMoneyAllocation allocation) {
    moneyAllocations.remove(allocation);
    allocation.setTravelItem(null);
  }

  /**
   * Get total estimated cost in CAD, computed from all travellers.
   */
  public BigDecimal getEstimatedCostInCAD() {
    return travellers.stream()
        .map(TravelTraveller::getEstimatedCostInCAD)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get total actual/final cost in CAD, computed from all travellers.
   */
  public BigDecimal getActualCostInCAD() {
    return travellers.stream()
        .map(TravelTraveller::getFinalCostInCAD)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get the number of travellers.
   */
  public int getNumberOfTravellers() {
    return travellers.size();
  }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }

  @Override
  public String toString() {
    return "TravelItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", travelType=" + travelType +
        ", emap='" + emap + '\'' +
        ", status=" + status +
        ", travellers=" + (travellers != null ? travellers.size() : 0) +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        '}';
  }
}
