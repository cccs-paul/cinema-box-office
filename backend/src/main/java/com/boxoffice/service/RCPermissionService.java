/*
 * myRC - RC Permission Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import com.boxoffice.dto.RCAccessDTO;
import com.boxoffice.model.RCAccess.AccessLevel;
import com.boxoffice.model.RCAccess.PrincipalType;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing RC permissions.
 * Handles granting, revoking, and querying access permissions for RCs.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
public interface RCPermissionService {

  /**
   * Get all permissions for a specific RC.
   *
   * @param rcId the RC ID
   * @param requestingUsername the username making the request
   * @return list of access DTOs
   */
  List<RCAccessDTO> getPermissionsForRC(Long rcId, String requestingUsername);

  /**
   * Grant access to a user.
   *
   * @param rcId the RC ID
   * @param principalIdentifier the user's username
   * @param accessLevel the access level to grant
   * @param requestingUsername the username making the request (must be owner)
   * @return the created access DTO
   */
  RCAccessDTO grantUserAccess(Long rcId, String principalIdentifier, AccessLevel accessLevel, 
                               String requestingUsername);

  /**
   * Grant access to a group or distribution list.
   *
   * @param rcId the RC ID
   * @param principalIdentifier the group/DL identifier
   * @param principalDisplayName the display name for the group/DL
   * @param principalType the type (GROUP or DISTRIBUTION_LIST)
   * @param accessLevel the access level to grant
   * @param requestingUsername the username making the request (must be owner)
   * @return the created access DTO
   */
  RCAccessDTO grantGroupAccess(Long rcId, String principalIdentifier, String principalDisplayName,
                                PrincipalType principalType, AccessLevel accessLevel, 
                                String requestingUsername);

  /**
   * Update an existing permission.
   *
   * @param accessId the access record ID
   * @param newAccessLevel the new access level
   * @param requestingUsername the username making the request (must be owner)
   * @return the updated access DTO
   */
  RCAccessDTO updatePermission(Long accessId, AccessLevel newAccessLevel, String requestingUsername);

  /**
   * Revoke access.
   *
   * @param accessId the access record ID
   * @param requestingUsername the username making the request (must be owner)
   */
  void revokeAccess(Long accessId, String requestingUsername);

  /**
   * Check if a user is an owner of an RC.
   *
   * @param rcId the RC ID
   * @param username the username to check
   * @return true if the user is an owner
   */
  boolean isOwner(Long rcId, String username);

  /**
   * Get the effective access level for a user on an RC.
   * Takes into account direct access and group membership.
   *
   * @param rcId the RC ID
   * @param username the username
   * @param groupIdentifiers list of groups the user belongs to
   * @return optional containing the highest access level, empty if no access
   */
  Optional<AccessLevel> getEffectiveAccessLevel(Long rcId, String username, List<String> groupIdentifiers);

  /**
   * Check if a user can edit content (funding, spending, procurement).
   *
   * @param rcId the RC ID
   * @param username the username
   * @return true if user has OWNER or READ_WRITE access
   */
  boolean canEditContent(Long rcId, String username);

  /**
   * Check if a user can manage the RC (rename, delete, create FYs, manage permissions).
   *
   * @param rcId the RC ID
   * @param username the username
   * @return true if user has OWNER access
   */
  boolean canManageRC(Long rcId, String username);
}
