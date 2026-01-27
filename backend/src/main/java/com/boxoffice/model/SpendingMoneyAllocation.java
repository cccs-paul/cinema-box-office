/*
 * myRC - Spending Money Allocation Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Money Allocation for a Spending Item.
 * Each allocation tracks CAP (Capital) and OM (Operations & Maintenance) amounts
 * for a specific money type associated with a spending item.
 */
package com.boxoffice.model;

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

/**
 * Entity representing a Money Allocation for a Spending Item.
 * Each spending item can have allocations for multiple money types,
 * with each allocation tracking separate CAP and OM amounts.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Entity
@Table(name = "spending_money_allocations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"spending_item_id", "money_id"}, name = "uk_spending_allocation_si_money")
})
public class SpendingMoneyAllocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "spending_item_id", nullable = false)
  private SpendingItem spendingItem;

  @ManyToOne(optional = false)
  @JoinColumn(name = "money_id", nullable = false)
  private Money money;

  /**
   * Capital (CAP) amount for this allocation.
   * Defaults to 0.00 CAD.
   */
  @Column(name = "cap_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal capAmount = BigDecimal.ZERO;

  /**
   * Operations & Maintenance (OM) amount for this allocation.
   * Defaults to 0.00 CAD.
   */
  @Column(name = "om_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal omAmount = BigDecimal.ZERO;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  // Constructors
  public SpendingMoneyAllocation() {
  }

  /**
   * Creates a new SpendingMoneyAllocation with the specified spending item and money.
   * Both CAP and OM amounts default to $0.00.
   *
   * @param spendingItem the spending item this allocation belongs to
   * @param money the money type for this allocation
   */
  public SpendingMoneyAllocation(SpendingItem spendingItem, Money money) {
    this.spendingItem = spendingItem;
    this.money = money;
    this.capAmount = BigDecimal.ZERO;
    this.omAmount = BigDecimal.ZERO;
  }

  /**
   * Creates a new SpendingMoneyAllocation with specified amounts.
   *
   * @param spendingItem the spending item this allocation belongs to
   * @param money the money type for this allocation
   * @param capAmount the Capital (CAP) amount
   * @param omAmount the Operations & Maintenance (OM) amount
   */
  public SpendingMoneyAllocation(SpendingItem spendingItem, Money money, BigDecimal capAmount, BigDecimal omAmount) {
    this.spendingItem = spendingItem;
    this.money = money;
    this.capAmount = capAmount != null ? capAmount : BigDecimal.ZERO;
    this.omAmount = omAmount != null ? omAmount : BigDecimal.ZERO;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SpendingItem getSpendingItem() {
    return spendingItem;
  }

  public void setSpendingItem(SpendingItem spendingItem) {
    this.spendingItem = spendingItem;
  }

  public Money getMoney() {
    return money;
  }

  public void setMoney(Money money) {
    this.money = money;
  }

  public BigDecimal getCapAmount() {
    return capAmount;
  }

  public void setCapAmount(BigDecimal capAmount) {
    this.capAmount = capAmount != null ? capAmount : BigDecimal.ZERO;
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

  /**
   * Gets the total amount (CAP + OM) for this allocation.
   *
   * @return the total allocation amount
   */
  public BigDecimal getTotalAmount() {
    BigDecimal cap = capAmount != null ? capAmount : BigDecimal.ZERO;
    BigDecimal om = omAmount != null ? omAmount : BigDecimal.ZERO;
    return cap.add(om);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SpendingMoneyAllocation that = (SpendingMoneyAllocation) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "SpendingMoneyAllocation{" +
        "id=" + id +
        ", spendingItemId=" + (spendingItem != null ? spendingItem.getId() : null) +
        ", moneyCode=" + (money != null ? money.getCode() : null) +
        ", capAmount=" + capAmount +
        ", omAmount=" + omAmount +
        '}';
  }
}
