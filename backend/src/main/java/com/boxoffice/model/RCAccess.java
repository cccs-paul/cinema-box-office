/*
 * Cinema Box Office - RC Access Entity
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity representing access to a Responsibility Centre.
 * Defines the relationship between users and responsibility centres with specific access levels.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Entity
@Table(name = "rc_access", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"responsibility_centre_id", "user_id"})
})
public class RCAccess {
  public enum AccessLevel {
    READ_ONLY, READ_WRITE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "responsibility_centre_id", nullable = false)
  private ResponsibilityCentre responsibilityCentre;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccessLevel accessLevel;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime grantedAt;

  // Constructors
  public RCAccess() {}

  public RCAccess(ResponsibilityCentre responsibilityCentre, User user, AccessLevel accessLevel) {
    this.responsibilityCentre = responsibilityCentre;
    this.user = user;
    this.accessLevel = accessLevel;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ResponsibilityCentre getResponsibilityCentre() {
    return responsibilityCentre;
  }

  public void setResponsibilityCentre(ResponsibilityCentre responsibilityCentre) {
    this.responsibilityCentre = responsibilityCentre;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public AccessLevel getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(AccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }

  public LocalDateTime getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(LocalDateTime grantedAt) {
    this.grantedAt = grantedAt;
  }

  @Override
  public String toString() {
    return "RCAccess{" +
        "id=" + id +
        ", responsibilityCentre=" + (responsibilityCentre != null ? responsibilityCentre.getName() : null) +
        ", user=" + (user != null ? user.getUsername() : null) +
        ", accessLevel=" + accessLevel +
        ", grantedAt=" + grantedAt +
        '}';
  }
}
