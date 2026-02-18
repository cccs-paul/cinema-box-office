/*
 * myRC - Training Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-16
 * Version: 2.0.0
 *
 * Description:
 * Entity representing a Training Item associated with a Fiscal Year.
 * Each fiscal year can have 0..n training items.
 * Training items track training costs with 1..n participants,
 * each with their own cost, currency, and exchange rate.
 * Training items use OM-only money type breakdowns.
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
 * Entity representing a Training Item associated with a Fiscal Year.
 * Training items represent training activities such as course training,
 * conference registration, or other training within a fiscal year.
 * Each training item has 1..n participants with individual costs.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
@Entity
@Table(name = "training_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "fiscal_year_id"}, name = "uk_ti_name_fy")
})
public class TrainingItem {

  /**
   * Enumeration of training item status values.
   */
  public enum Status {
    PLANNED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
  }

  /**
   * Enumeration of training type values.
   */
  public enum TrainingType {
    COURSE_TRAINING,
    CONFERENCE_REGISTRATION,
    OTHER
  }

  /**
   * Enumeration of training format values.
   */
  public enum TrainingFormat {
    IN_PERSON,
    ONLINE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  /**
   * Training provider or institution name.
   */
  @Column(length = 200)
  private String provider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PLANNED;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private TrainingType trainingType = TrainingType.OTHER;

  /**
   * Format of the training: in-person or online.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TrainingFormat format = TrainingFormat.IN_PERSON;

  /**
   * Start date of the training.
   */
  @Column
  private LocalDate startDate;

  /**
   * End date of the training.
   */
  @Column
  private LocalDate endDate;

  /**
   * Location of the training.
   */
  @Column(length = 500)
  private String location;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  /**
   * Participants for this training item.
   * Each participant has their own name, costs, currency, and exchange rate.
   */
  @OneToMany(mappedBy = "trainingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TrainingParticipant> participants = new ArrayList<>();

  /**
   * Money allocations for this training item (OM only).
   * Each allocation tracks the OM amount for a specific money type.
   */
  @OneToMany(mappedBy = "trainingItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TrainingMoneyAllocation> moneyAllocations = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Version for optimistic locking.
   */
  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private Boolean active = true;

  // Constructors
  public TrainingItem() {}

  public TrainingItem(String name, String description, Status status, FiscalYear fiscalYear) {
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

  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }

  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }

  public TrainingType getTrainingType() { return trainingType; }
  public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

  public TrainingFormat getFormat() { return format; }
  public void setFormat(TrainingFormat format) { this.format = format; }

  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }

  public FiscalYear getFiscalYear() { return fiscalYear; }
  public void setFiscalYear(FiscalYear fiscalYear) { this.fiscalYear = fiscalYear; }

  public List<TrainingParticipant> getParticipants() { return participants; }
  public void setParticipants(List<TrainingParticipant> participants) { this.participants = participants; }

  public List<TrainingMoneyAllocation> getMoneyAllocations() { return moneyAllocations; }
  public void setMoneyAllocations(List<TrainingMoneyAllocation> moneyAllocations) { this.moneyAllocations = moneyAllocations; }

  /**
   * Add a participant to this training item.
   */
  public void addParticipant(TrainingParticipant participant) {
    participants.add(participant);
    participant.setTrainingItem(this);
  }

  /**
   * Remove a participant from this training item.
   */
  public void removeParticipant(TrainingParticipant participant) {
    participants.remove(participant);
    participant.setTrainingItem(null);
  }

  /**
   * Add a money allocation to this training item.
   */
  public void addMoneyAllocation(TrainingMoneyAllocation allocation) {
    moneyAllocations.add(allocation);
    allocation.setTrainingItem(this);
  }

  /**
   * Remove a money allocation from this training item.
   */
  public void removeMoneyAllocation(TrainingMoneyAllocation allocation) {
    moneyAllocations.remove(allocation);
    allocation.setTrainingItem(null);
  }

  /**
   * Get the total estimated cost in CAD, computed from all participants.
   */
  public BigDecimal getEstimatedCostInCAD() {
    return participants.stream()
        .map(TrainingParticipant::getEstimatedCostInCAD)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get the total actual/final cost in CAD, computed from all participants.
   */
  public BigDecimal getActualCostInCAD() {
    return participants.stream()
        .map(TrainingParticipant::getFinalCostInCAD)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get the number of participants.
   */
  public int getNumberOfParticipants() {
    return participants.size();
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
    return "TrainingItem{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", trainingType=" + trainingType +
        ", format=" + format +
        ", status=" + status +
        ", participants=" + (participants != null ? participants.size() : 0) +
        ", fiscalYear=" + (fiscalYear != null ? fiscalYear.getName() : null) +
        ", active=" + active +
        '}';
  }
}
