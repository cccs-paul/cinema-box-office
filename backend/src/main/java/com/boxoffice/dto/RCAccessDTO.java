/*
 * myRC - RC Access DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

package com.boxoffice.dto;

import com.boxoffice.model.RCAccess;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Responsibility Centre Access.
 * Represents access information for a user, group, or distribution list to a responsibility centre.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-01-21
 */
public class RCAccessDTO {
  private Long id;
  private Long rcId;
  private String rcName;
  private String principalIdentifier;
  private String principalDisplayName;
  private String principalType;  // USER, GROUP, DISTRIBUTION_LIST
  private String accessLevel;    // OWNER, READ_WRITE, READ_ONLY
  private LocalDateTime grantedAt;
  private String grantedBy;

  public RCAccessDTO() {}

  public RCAccessDTO(Long id, Long rcId, String rcName, String principalIdentifier, 
                     String principalDisplayName, String principalType, 
                     String accessLevel, LocalDateTime grantedAt, String grantedBy) {
    this.id = id;
    this.rcId = rcId;
    this.rcName = rcName;
    this.principalIdentifier = principalIdentifier;
    this.principalDisplayName = principalDisplayName;
    this.principalType = principalType;
    this.accessLevel = accessLevel;
    this.grantedAt = grantedAt;
    this.grantedBy = grantedBy;
  }

  public static RCAccessDTO fromEntity(RCAccess access) {
    String identifier = access.getPrincipalIdentifier();
    String displayName = access.getPrincipalDisplayName();
    
    // For backward compatibility with user-based access
    if (access.getUser() != null && identifier == null) {
      identifier = access.getUser().getUsername();
      displayName = access.getUser().getFullName() != null ? 
          access.getUser().getFullName() : access.getUser().getUsername();
    }
    
    return new RCAccessDTO(
        access.getId(),
        access.getResponsibilityCentre().getId(),
        access.getResponsibilityCentre().getName(),
        identifier,
        displayName,
        access.getPrincipalType().name(),
        access.getAccessLevel().name(),
        access.getGrantedAt(),
        access.getGrantedBy() != null ? access.getGrantedBy().getUsername() : null
    );
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getRcId() {
    return rcId;
  }

  public void setRcId(Long rcId) {
    this.rcId = rcId;
  }

  public String getRcName() {
    return rcName;
  }

  public void setRcName(String rcName) {
    this.rcName = rcName;
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

  public String getPrincipalType() {
    return principalType;
  }

  public void setPrincipalType(String principalType) {
    this.principalType = principalType;
  }

  public String getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }

  public LocalDateTime getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(LocalDateTime grantedAt) {
    this.grantedAt = grantedAt;
  }

  public String getGrantedBy() {
    return grantedBy;
  }

  public void setGrantedBy(String grantedBy) {
    this.grantedBy = grantedBy;
  }

  // Backward compatibility method
  public String getUsername() {
    return principalIdentifier;
  }

  public void setUsername(String username) {
    this.principalIdentifier = username;
  }
}
