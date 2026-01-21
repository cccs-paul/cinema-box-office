/*
 * myRC - RC Access DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

package com.boxoffice.dto;

import com.boxoffice.model.RCAccess;

/**
 * Data Transfer Object for Responsibility Centre Access.
 * Represents access information for a user to a responsibility centre.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-21
 */
public class RCAccessDTO {
  private Long id;
  private Long rcId;
  private String username;
  private String accessLevel;

  public RCAccessDTO() {}

  public RCAccessDTO(Long id, Long rcId, String username, String accessLevel) {
    this.id = id;
    this.rcId = rcId;
    this.username = username;
    this.accessLevel = accessLevel;
  }

  public static RCAccessDTO fromEntity(RCAccess access) {
    return new RCAccessDTO(
        access.getId(),
        access.getResponsibilityCentre().getId(),
        access.getUser().getUsername(),
        access.getAccessLevel().name()
    );
  }

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }
}
