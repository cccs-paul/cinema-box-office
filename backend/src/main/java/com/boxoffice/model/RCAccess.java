/*
 * myRC - RC Access Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity representing access to a Responsibility Centre.
 * Defines the relationship between users/groups and responsibility centres with specific access levels.
 * Supports individual users, LDAP groups, and distribution lists.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-01-17
 */
@Entity
@Table(name = "rc_access", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"responsibility_centre_id", "user_id"}),
    @UniqueConstraint(columnNames = {"responsibility_centre_id", "principal_identifier", "principal_type"})
})
public class RCAccess {
  /**
   * Access levels for RC permissions.
   * OWNER: Full control - can grant roles, rename, delete RC, create FYs, manage Money Types/Categories
   * READ_WRITE: Can create/edit Funding, Spending, Procurement items and quotes
   * READ_ONLY: Can view everything but cannot modify anything
   */
  public enum AccessLevel {
    OWNER, READ_WRITE, READ_ONLY
  }

  /**
   * Type of principal (who is being granted access).
   * USER: Individual user account (local, LDAP, or OAuth)
   * GROUP: Security group (typically from LDAP)
   * DISTRIBUTION_LIST: Distribution list/email group
   */
  public enum PrincipalType {
    USER, GROUP, DISTRIBUTION_LIST
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "responsibility_centre_id", nullable = false)
  private ResponsibilityCentre responsibilityCentre;

  // For USER type - links to actual user entity
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  // For GROUP and DISTRIBUTION_LIST types - stores the identifier
  @Column(name = "principal_identifier", length = 255)
  private String principalIdentifier;

  // Human-readable name for the principal
  @Column(name = "principal_display_name", length = 255)
  private String principalDisplayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "principal_type", nullable = false)
  private PrincipalType principalType = PrincipalType.USER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccessLevel accessLevel;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime grantedAt;

  @ManyToOne
  @JoinColumn(name = "granted_by_id")
  private User grantedBy;

  // Constructors
  public RCAccess() {}

  /**
   * Constructor for user-based access.
   */
  public RCAccess(ResponsibilityCentre responsibilityCentre, User user, AccessLevel accessLevel) {
    this.responsibilityCentre = responsibilityCentre;
    this.user = user;
    this.principalType = PrincipalType.USER;
    this.principalIdentifier = user.getUsername();
    this.principalDisplayName = user.getFullName() != null ? user.getFullName() : user.getUsername();
    this.accessLevel = accessLevel;
  }

  /**
   * Constructor for group or distribution list access.
   */
  public RCAccess(ResponsibilityCentre responsibilityCentre, String principalIdentifier, 
                  String principalDisplayName, PrincipalType principalType, AccessLevel accessLevel) {
    this.responsibilityCentre = responsibilityCentre;
    this.principalIdentifier = principalIdentifier;
    this.principalDisplayName = principalDisplayName;
    this.principalType = principalType;
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

  public String getPrincipalIdentifier() {
    return principalIdentifier;
  }

  public void setPrincipalIdentifier(String principalIdentifier) {
    this.principalIdentifier = principalIdentifier;
  }

  public String getPrincipalDisplayName() {
    return principalDisplayName;
  }

  public void setPrincipalDisplayName(String principalDisplayName) {
    this.principalDisplayName = principalDisplayName;
  }

  public PrincipalType getPrincipalType() {
    return principalType;
  }

  public void setPrincipalType(PrincipalType principalType) {
    this.principalType = principalType;
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

  public User getGrantedBy() {
    return grantedBy;
  }

  public void setGrantedBy(User grantedBy) {
    this.grantedBy = grantedBy;
  }

  @Override
  public String toString() {
    return "RCAccess{" +
        "id=" + id +
        ", responsibilityCentre=" + (responsibilityCentre != null ? responsibilityCentre.getName() : null) +
        ", principalIdentifier=" + principalIdentifier +
        ", principalType=" + principalType +
        ", accessLevel=" + accessLevel +
        ", grantedAt=" + grantedAt +
        '}';
  }
}
