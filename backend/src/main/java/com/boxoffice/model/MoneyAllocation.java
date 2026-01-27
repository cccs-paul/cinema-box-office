/*
 * myRC - Money Allocation Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Money Allocation for a Funding Item.
 * Each allocation tracks CAP (Capital) and OM (Operations & Maintenance) amounts
 * for a specific money type associated with a funding item.
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
 * Entity representing a Money Allocation for a Funding Item.
 * Each funding item can have allocations for multiple money types,
 * with each allocation tracking separate CAP and OM amounts.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@Entity
@Table(name = "money_allocations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"funding_item_id", "money_id"}, name = "uk_allocation_fi_money")
})
public class MoneyAllocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "funding_item_id", nullable = false)
  private FundingItem fundingItem;

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
  public MoneyAllocation() {
  }

  /**
   * Creates a new MoneyAllocation with the specified funding item and money.
   * Both CAP and OM amounts default to $0.00.
   *
   * @param fundingItem the funding item this allocation belongs to
   * @param money the money type for this allocation
   */
  public MoneyAllocation(FundingItem fundingItem, Money money) {
    this.fundingItem = fundingItem;
    this.money = money;
    this.capAmount = BigDecimal.ZERO;
    this.omAmount = BigDecimal.ZERO;
  }

  /**
   * Creates a new MoneyAllocation with specified amounts.
   *
   * @param fundingItem the funding item this allocation belongs to
   * @param money the money type for this allocation
   * @param capAmount the Capital (CAP) amount
   * @param omAmount the Operations & Maintenance (OM) amount
   */
  public MoneyAllocation(FundingItem fundingItem, Money money, BigDecimal capAmount, BigDecimal omAmount) {
    this.fundingItem = fundingItem;
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

  public FundingItem getFundingItem() {
    return fundingItem;
  }

  public void setFundingItem(FundingItem fundingItem) {
    this.fundingItem = fundingItem;
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
    if (!(o instanceof MoneyAllocation)) return false;
    MoneyAllocation that = (MoneyAllocation) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "MoneyAllocation{" +
        "id=" + id +
        ", fundingItemId=" + (fundingItem != null ? fundingItem.getId() : null) +
        ", moneyCode=" + (money != null ? money.getCode() : null) +
        ", capAmount=" + capAmount +
        ", omAmount=" + omAmount +
        '}';
  }
}
