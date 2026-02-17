/*
 * myRC - Responsibility Centre DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ResponsibilityCentre.
 * Used for API responses and requests related to responsibility centres.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
public class ResponsibilityCentreDTO {

  private Long id;
  private String name;
  private String description;
  private String ownerUsername;
  private String currentUsername; // The username viewing this DTO
  private String accessLevel; // READ_ONLY or READ_WRITE
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean active;
  private Boolean trainingEnabled;
  private Boolean travelEnabled;
  private static final String DEMO_RC_NAME = "Demo";  // Name of the demo RC

  // Constructors
  public ResponsibilityCentreDTO() {}

  public ResponsibilityCentreDTO(Long id, String name, String description, 
      String ownerUsername, String accessLevel, LocalDateTime createdAt,
      LocalDateTime updatedAt, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.ownerUsername = ownerUsername;
    this.accessLevel = accessLevel;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
  }

  public ResponsibilityCentreDTO(Long id, String name, String description, 
      String ownerUsername, String currentUsername, String accessLevel, LocalDateTime createdAt,
      LocalDateTime updatedAt, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.ownerUsername = ownerUsername;
    this.currentUsername = currentUsername;
    this.accessLevel = accessLevel;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.active = active;
  }

  // Factory methods
  /**
   * Create a DTO from a ResponsibilityCentre entity.
   * Used when the current user is the owner.
   *
   * @param rc the responsibility centre entity
   * @param currentUsername the current user's username
   * @param accessLevel the access level for the current user
   * @return the DTO
   */
  public static ResponsibilityCentreDTO fromEntity(ResponsibilityCentre rc, 
      String currentUsername, String accessLevel) {
    ResponsibilityCentreDTO dto = new ResponsibilityCentreDTO(
        rc.getId(),
        rc.getName(),
        rc.getDescription(),
        rc.getOwner().getUsername(),
        accessLevel,
        rc.getCreatedAt(),
        rc.getUpdatedAt(),
        rc.getActive()
    );
    dto.setCurrentUsername(currentUsername);
    dto.setTrainingEnabled(rc.getTrainingEnabled());
    dto.setTravelEnabled(rc.getTravelEnabled());
    return dto;
  }

  /**
   * Create a DTO from a ResponsibilityCentre entity with access information.
   * Used when the current user has shared access.
   *
   * @param rc the responsibility centre entity
   * @param currentUsername the current user's username
   * @param access the access record
   * @return the DTO
   */
  public static ResponsibilityCentreDTO fromEntityWithAccess(ResponsibilityCentre rc,
      String currentUsername, RCAccess access) {
    ResponsibilityCentreDTO dto = new ResponsibilityCentreDTO(
        rc.getId(),
        rc.getName(),
        rc.getDescription(),
        rc.getOwner().getUsername(),
        access.getAccessLevel().toString(),
        rc.getCreatedAt(),
        rc.getUpdatedAt(),
        rc.getActive()
    );
    dto.setCurrentUsername(currentUsername);
    dto.setTrainingEnabled(rc.getTrainingEnabled());
    dto.setTravelEnabled(rc.getTravelEnabled());
    return dto;
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

  public String getOwnerUsername() {
    return ownerUsername;
  }

  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }

  public String getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
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

  public String getCurrentUsername() {
    return currentUsername;
  }

  public void setCurrentUsername(String currentUsername) {
    this.currentUsername = currentUsername;
  }

  public Boolean getTrainingEnabled() {
    return trainingEnabled;
  }

  public void setTrainingEnabled(Boolean trainingEnabled) {
    this.trainingEnabled = trainingEnabled;
  }

  public Boolean getTravelEnabled() {
    return travelEnabled;
  }

  public void setTravelEnabled(Boolean travelEnabled) {
    this.travelEnabled = travelEnabled;
  }

  /**
   * Check if the current user is the owner of this responsibility centre.
   * Note: Demo RC always returns false as it's read-only for all users.
   *
   * @return true if current user is the owner (either original owner or has OWNER access), false otherwise
   */
  @JsonProperty("isOwner")
  public boolean isOwner() {
    // Demo RC is always read-only, so isOwner is always false
    if (DEMO_RC_NAME.equals(name)) {
      return false;
    }
    // Check if access level is OWNER, or if current user is the original owner
    return "OWNER".equals(accessLevel) || 
           (currentUsername != null && currentUsername.equals(ownerUsername));
  }

  /**
   * Check if the current user can edit content (has OWNER or READ_WRITE access).
   *
   * @return true if user can edit content
   */
  @JsonProperty("canEdit")
  public boolean canEdit() {
    if (DEMO_RC_NAME.equals(name)) {
      return false;
    }
    return "OWNER".equals(accessLevel) || "READ_WRITE".equals(accessLevel);
  }

  @Override
  public String toString() {
    return "ResponsibilityCentreDTO{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", ownerUsername='" + ownerUsername + '\'' +
        ", accessLevel='" + accessLevel + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", active=" + active +
        '}';
  }
}
