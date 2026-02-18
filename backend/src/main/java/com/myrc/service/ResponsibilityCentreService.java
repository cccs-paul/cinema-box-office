/*
 * myRC - Responsibility Centre Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.ResponsibilityCentreDTO;
import com.myrc.model.RCAccess;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Responsibility Centres.
 * Defines operations for creating, reading, updating, and managing access to responsibility centres.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
public interface ResponsibilityCentreService {

  /**
   * Get all responsibility centres accessible to a user.
   * Includes RCs owned by the user, RCs with direct user access, and RCs
   * accessible via group membership (LDAP group DNs).
   *
   * @param username the username of the user
   * @param groupIdentifiers list of LDAP group DNs the user belongs to (may be empty)
   * @return list of responsibility centres the user owns or has access to
   */
  List<ResponsibilityCentreDTO> getUserResponsibilityCentres(String username,
      List<String> groupIdentifiers);

  /**
   * Create a new responsibility centre.
   *
   * @param username the username of the owner
   * @param name the name of the responsibility centre
   * @param description the description of the responsibility centre
   * @return the created responsibility centre DTO
   */
  ResponsibilityCentreDTO createResponsibilityCentre(String username, String name, 
      String description);

  /**
   * Get a specific responsibility centre by ID.
   * Checks ownership, direct user access, and group-based access.
   *
   * @param rcId the ID of the responsibility centre
   * @param username the username of the requesting user
   * @param groupIdentifiers list of LDAP group DNs the user belongs to (may be empty)
   * @return optional containing the responsibility centre if found and user has access
   */
  Optional<ResponsibilityCentreDTO> getResponsibilityCentre(Long rcId, String username,
      List<String> groupIdentifiers);

  /**
   * Update a responsibility centre.
   *
   * @param rcId the ID of the responsibility centre
   * @param username the username of the requesting user (must be owner)
   * @param name the new name
   * @param description the new description
   * @return optional containing the updated responsibility centre if successful
   */
  Optional<ResponsibilityCentreDTO> updateResponsibilityCentre(Long rcId, String username,
      String name, String description);

  /**
   * Delete a responsibility centre.
   *
   * @param rcId the ID of the responsibility centre
   * @param username the username of the requesting user (must be owner)
   * @return true if deletion was successful, false otherwise
   */
  boolean deleteResponsibilityCentre(Long rcId, String username);

  /**
   * Grant access to a responsibility centre to another user.
   *
   * @param rcId the ID of the responsibility centre
   * @param ownerUsername the username of the owner
   * @param grantedToUsername the username of the user to grant access to
   * @param accessLevel the access level (READ_ONLY or READ_WRITE)
   * @return optional containing the created access record if successful
   */
  Optional<RCAccess> grantAccess(Long rcId, String ownerUsername, String grantedToUsername,
      String accessLevel);

  /**
   * Revoke access to a responsibility centre from a user.
   *
   * @param rcId the ID of the responsibility centre
   * @param ownerUsername the username of the owner
   * @param revokeFromUsername the username of the user to revoke access from
   * @return true if revocation was successful, false otherwise
   */
  boolean revokeAccess(Long rcId, String ownerUsername, String revokeFromUsername);

  /**
   * Get all access records for a responsibility centre.
   *
   * @param rcId the ID of the responsibility centre
   * @param ownerUsername the username of the owner
   * @return list of access records for the responsibility centre
   */
  List<RCAccess> getResponsibilityCentreAccess(Long rcId, String ownerUsername);

  /**
   * Clone a responsibility centre with a new name.
   * Creates a new RC owned by the requesting user with the same structure but no fiscal years.
   *
   * @param sourceRcId the ID of the responsibility centre to clone
   * @param username the username of the user creating the clone
   * @param newName the name for the cloned responsibility centre
   * @return the cloned responsibility centre DTO
   */
  ResponsibilityCentreDTO cloneResponsibilityCentre(Long sourceRcId, String username, String newName);

  /**
   * Toggle the training enabled status for a responsibility centre.
   *
   * @param rcId the ID of the responsibility centre
   * @param username the username of the requesting user (must be owner)
   * @param enabled the new training enabled status
   * @return optional containing the updated responsibility centre if successful
   */
  Optional<ResponsibilityCentreDTO> setTrainingEnabled(Long rcId, String username, boolean enabled);

  /**
   * Toggle the travel enabled status for a responsibility centre.
   *
   * @param rcId the ID of the responsibility centre
   * @param username the username of the requesting user (must be owner)
   * @param enabled the new travel enabled status
   * @return optional containing the updated responsibility centre if successful
   */
  Optional<ResponsibilityCentreDTO> setTravelEnabled(Long rcId, String username, boolean enabled);

  /**
   * Toggle training include in summary for a responsibility centre.
   */
  Optional<ResponsibilityCentreDTO> setTrainingIncludeInSummary(Long rcId, String username, boolean include);

  /**
   * Toggle travel include in summary for a responsibility centre.
   */
  Optional<ResponsibilityCentreDTO> setTravelIncludeInSummary(Long rcId, String username, boolean include);
}
