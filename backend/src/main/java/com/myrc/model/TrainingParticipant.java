/*
 * myRC - Training Participant Entity
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
 * Entity representing a participant in a Training Item.
 * Each training item has 1..n participants, each with their own
 * name, estimated cost, final cost, currency, and exchange rate.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "training_participants")
public class TrainingParticipant {

  /**
   * Status lifecycle for a training participant.
   */
  public enum ParticipantStatus {
    PLANNED,
    ECO_CREATED,
    REGISTERED,
    COMPLETED,
    CANCELLED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "training_item_id", nullable = false)
  private TrainingItem trainingItem;

  @Column(nullable = false, length = 500)
  private String name;

  /**
   * ECO number for this participant.
   */
  @Column(length = 100)
  private String eco;

  /**
   * Status of the participant.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ParticipantStatus status = ParticipantStatus.PLANNED;

  @Column(precision = 15, scale = 2)
  private BigDecimal estimatedCost;

  @Column(precision = 15, scale = 2)
  private BigDecimal finalCost;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency estimatedCurrency = Currency.CAD;

  @Column(precision = 15, scale = 6)
  private BigDecimal estimatedExchangeRate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency finalCurrency = Currency.CAD;

  @Column(precision = 15, scale = 6)
  private BigDecimal finalExchangeRate;

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
  public TrainingParticipant() {}

  public TrainingParticipant(String name, TrainingItem trainingItem) {
    this.name = name;
    this.trainingItem = trainingItem;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public TrainingItem getTrainingItem() { return trainingItem; }
  public void setTrainingItem(TrainingItem trainingItem) { this.trainingItem = trainingItem; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public BigDecimal getEstimatedCost() { return estimatedCost; }
  public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

  public BigDecimal getFinalCost() { return finalCost; }
  public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

  public String getEco() { return eco; }
  public void setEco(String eco) { this.eco = eco; }

  public ParticipantStatus getStatus() { return status; }
  public void setStatus(ParticipantStatus status) { this.status = status; }

  public Currency getEstimatedCurrency() { return estimatedCurrency; }
  public void setEstimatedCurrency(Currency estimatedCurrency) { this.estimatedCurrency = estimatedCurrency; }

  public BigDecimal getEstimatedExchangeRate() { return estimatedExchangeRate; }
  public void setEstimatedExchangeRate(BigDecimal estimatedExchangeRate) { this.estimatedExchangeRate = estimatedExchangeRate; }

  public Currency getFinalCurrency() { return finalCurrency; }
  public void setFinalCurrency(Currency finalCurrency) { this.finalCurrency = finalCurrency; }

  public BigDecimal getFinalExchangeRate() { return finalExchangeRate; }
  public void setFinalExchangeRate(BigDecimal finalExchangeRate) { this.finalExchangeRate = finalExchangeRate; }

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
    if (estimatedCurrency == Currency.CAD || estimatedExchangeRate == null) return estimatedCost;
    return estimatedCost.multiply(estimatedExchangeRate);
  }

  /**
   * Get the final cost in CAD, applying exchange rate if necessary.
   */
  public BigDecimal getFinalCostInCAD() {
    if (finalCost == null) return BigDecimal.ZERO;
    if (finalCurrency == Currency.CAD || finalExchangeRate == null) return finalCost;
    return finalCost.multiply(finalExchangeRate);
  }

  @Override
  public String toString() {
    return "TrainingParticipant{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", eco='" + eco + '\'' +
        ", status=" + status +
        ", estimatedCost=" + estimatedCost +
        ", finalCost=" + finalCost +
        ", estimatedCurrency=" + estimatedCurrency +
        ", finalCurrency=" + finalCurrency +
        '}';
  }
}
