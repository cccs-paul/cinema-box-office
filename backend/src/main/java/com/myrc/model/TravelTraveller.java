/*
 * myRC - Travel Traveller Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.model;

import java.math.BigDecimal;
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
import jakarta.persistence.Version;

/**
 * Entity representing a traveller in a Travel Item.
 * Each travel item has 1..n travellers, each with their own name, TAAC,
 * costs, currency, exchange rate, and approval status.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "travel_travellers")
public class TravelTraveller {

  /**
   * Approval status for this traveller's travel request.
   */
  public enum ApprovalStatus {
    PLANNED,
    TAAC_ESTIMATE_SUBMITTED,
    TAAC_ESTIMATE_APPROVED,
    TAAC_FINAL_SUBMITTED,
    TAAC_FINAL_APPROVED,
    CANCELLED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "travel_item_id", nullable = false)
  private TravelItem travelItem;

  @Column(nullable = false, length = 500)
  private String name;

  /**
   * Travel Authorization and Advance Claim number.
   */
  @Column(length = 100)
  private String taac;

  @Column(precision = 15, scale = 2)
  private BigDecimal estimatedCost;

  @Column(precision = 15, scale = 2)
  private BigDecimal finalCost;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.CAD;

  @Column(precision = 15, scale = 6)
  private BigDecimal exchangeRate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ApprovalStatus approvalStatus = ApprovalStatus.PLANNED;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Version
  @Column(nullable = false)
  private Long version = 0L;

  // Constructors
  public TravelTraveller() {}

  public TravelTraveller(String name, TravelItem travelItem) {
    this.name = name;
    this.travelItem = travelItem;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public TravelItem getTravelItem() { return travelItem; }
  public void setTravelItem(TravelItem travelItem) { this.travelItem = travelItem; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getTaac() { return taac; }
  public void setTaac(String taac) { this.taac = taac; }

  public BigDecimal getEstimatedCost() { return estimatedCost; }
  public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

  public BigDecimal getFinalCost() { return finalCost; }
  public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

  public Currency getCurrency() { return currency; }
  public void setCurrency(Currency currency) { this.currency = currency; }

  public BigDecimal getExchangeRate() { return exchangeRate; }
  public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

  public ApprovalStatus getApprovalStatus() { return approvalStatus; }
  public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }

  /**
   * Get the estimated cost in CAD, applying exchange rate if necessary.
   */
  public BigDecimal getEstimatedCostInCAD() {
    if (estimatedCost == null) return BigDecimal.ZERO;
    if (currency == Currency.CAD || exchangeRate == null) return estimatedCost;
    return estimatedCost.multiply(exchangeRate);
  }

  /**
   * Get the final cost in CAD, applying exchange rate if necessary.
   */
  public BigDecimal getFinalCostInCAD() {
    if (finalCost == null) return BigDecimal.ZERO;
    if (currency == Currency.CAD || exchangeRate == null) return finalCost;
    return finalCost.multiply(exchangeRate);
  }

  @Override
  public String toString() {
    return "TravelTraveller{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", taac='" + taac + '\'' +
        ", approvalStatus=" + approvalStatus +
        ", estimatedCost=" + estimatedCost +
        ", finalCost=" + finalCost +
        ", currency=" + currency +
        '}';
  }
}
