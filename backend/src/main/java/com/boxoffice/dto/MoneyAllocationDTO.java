/*
 * myRC - Money Allocation DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Money Allocation.
 * Used for transferring money allocation data between layers.
 */
package com.boxoffice.dto;

import com.boxoffice.model.MoneyAllocation;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Money Allocation.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
public class MoneyAllocationDTO {

  private Long id;
  private Long moneyId;
  private String moneyCode;
  private String moneyName;
  private BigDecimal capAmount;
  private BigDecimal omAmount;

  // Constructors
  public MoneyAllocationDTO() {
  }

  public MoneyAllocationDTO(Long id, Long moneyId, String moneyCode, String moneyName,
                            BigDecimal capAmount, BigDecimal omAmount) {
    this.id = id;
    this.moneyId = moneyId;
    this.moneyCode = moneyCode;
    this.moneyName = moneyName;
    this.capAmount = capAmount != null ? capAmount : BigDecimal.ZERO;
    this.omAmount = omAmount != null ? omAmount : BigDecimal.ZERO;
  }

  /**
   * Creates a MoneyAllocationDTO from a MoneyAllocation entity.
   *
   * @param allocation the money allocation entity
   * @return the money allocation DTO
   */
  public static MoneyAllocationDTO fromEntity(MoneyAllocation allocation) {
    if (allocation == null) {
      return null;
    }
    return new MoneyAllocationDTO(
        allocation.getId(),
        allocation.getMoney() != null ? allocation.getMoney().getId() : null,
        allocation.getMoney() != null ? allocation.getMoney().getCode() : null,
        allocation.getMoney() != null ? allocation.getMoney().getName() : null,
        allocation.getCapAmount(),
        allocation.getOmAmount()
    );
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMoneyId() {
    return moneyId;
  }

  public void setMoneyId(Long moneyId) {
    this.moneyId = moneyId;
  }

  public String getMoneyCode() {
    return moneyCode;
  }

  public void setMoneyCode(String moneyCode) {
    this.moneyCode = moneyCode;
  }

  public String getMoneyName() {
    return moneyName;
  }

  public void setMoneyName(String moneyName) {
    this.moneyName = moneyName;
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
  public String toString() {
    return "MoneyAllocationDTO{" +
        "id=" + id +
        ", moneyId=" + moneyId +
        ", moneyCode='" + moneyCode + '\'' +
        ", moneyName='" + moneyName + '\'' +
        ", capAmount=" + capAmount +
        ", omAmount=" + omAmount +
        '}';
  }
}
