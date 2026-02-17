/*
 * myRC - Travel Money Allocation Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-16
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Money Allocation for a Travel Item.
 * Travel items use OM-only allocations (no CAP).
 */
package com.myrc.model;

import java.math.BigDecimal;
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
 * Entity representing a Money Allocation for a Travel Item.
 * Travel items use OM-only allocations since travel costs
 * are exclusively Operations &amp; Maintenance expenses.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "travel_money_allocations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"travel_item_id", "money_id"}, name = "uk_travel_allocation_trv_money")
})
public class TravelMoneyAllocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "travel_item_id", nullable = false)
  private TravelItem travelItem;

  @ManyToOne(optional = false)
  @JoinColumn(name = "money_id", nullable = false)
  private Money money;

  /**
   * Operations &amp; Maintenance (OM) amount for this allocation.
   * Travel costs are OM-only.
   */
  @Column(name = "om_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal omAmount = BigDecimal.ZERO;

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
  public TravelMoneyAllocation() {}

  public TravelMoneyAllocation(TravelItem travelItem, Money money) {
    this.travelItem = travelItem;
    this.money = money;
    this.omAmount = BigDecimal.ZERO;
  }

  public TravelMoneyAllocation(TravelItem travelItem, Money money, BigDecimal omAmount) {
    this.travelItem = travelItem;
    this.money = money;
    this.omAmount = omAmount != null ? omAmount : BigDecimal.ZERO;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TravelItem getTravelItem() {
    return travelItem;
  }

  public void setTravelItem(TravelItem travelItem) {
    this.travelItem = travelItem;
  }

  public Money getMoney() {
    return money;
  }

  public void setMoney(Money money) {
    this.money = money;
  }

  public BigDecimal getOmAmount() {
    return omAmount;
  }

  public void setOmAmount(BigDecimal omAmount) {
    this.omAmount = omAmount != null ? omAmount : BigDecimal.ZERO;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TravelMoneyAllocation that = (TravelMoneyAllocation) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "TravelMoneyAllocation{" +
        "id=" + id +
        ", travelItemId=" + (travelItem != null ? travelItem.getId() : null) +
        ", moneyCode=" + (money != null ? money.getCode() : null) +
        ", omAmount=" + omAmount +
        '}';
  }
}
