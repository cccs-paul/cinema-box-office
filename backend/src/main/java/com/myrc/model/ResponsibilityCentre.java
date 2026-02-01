/*
 * myRC - Responsibility Centre Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.model;

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

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a Responsibility Centre.
 * A responsibility centre is an organizational unit that manages box office operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Entity
@Table(name = "responsibility_centres", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "owner_id"}, name = "uk_rc_name_owner")
})
public class ResponsibilityCentre {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

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
  public ResponsibilityCentre() {}

  public ResponsibilityCentre(String name, String description, User owner) {
    this.name = name;
    this.description = description;
    this.owner = owner;
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

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
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
    return "ResponsibilityCentre{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", owner=" + (owner != null ? owner.getUsername() : null) +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", active=" + active +
        '}';
  }
}
