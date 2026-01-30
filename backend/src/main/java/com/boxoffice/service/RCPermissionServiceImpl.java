/*
 * myRC - RC Permission Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import com.boxoffice.dto.RCAccessDTO;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.RCAccess.AccessLevel;
import com.boxoffice.model.RCAccess.PrincipalType;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of RCPermissionService.
 * Provides business logic for managing RC permissions.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Service
@Transactional
public class RCPermissionServiceImpl implements RCPermissionService {

  private static final Logger logger = LoggerFactory.getLogger(RCPermissionServiceImpl.class);
  private static final String DEMO_RC_NAME = "Demo";

  private final RCAccessRepository accessRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final UserRepository userRepository;

  public RCPermissionServiceImpl(RCAccessRepository accessRepository,
                                  ResponsibilityCentreRepository rcRepository,
                                  UserRepository userRepository) {
    this.accessRepository = accessRepository;
    this.rcRepository = rcRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RCAccessDTO> getPermissionsForRC(Long rcId, String requestingUsername) {
    ResponsibilityCentre rc = rcRepository.findById(rcId)
        .orElseThrow(() -> new IllegalArgumentException("RC not found: " + rcId));

    // Verify requester has access to view permissions
    if (!isOwner(rcId, requestingUsername)) {
      throw new SecurityException("Only owners can view RC permissions");
    }

    List<RCAccess> accessRecords = accessRepository.findByResponsibilityCentre(rc);
    
    // Also include the RC owner as an implicit OWNER
    List<RCAccessDTO> result = new ArrayList<>();
    
    // Add the original owner (from ResponsibilityCentre.owner)
    User originalOwner = rc.getOwner();
    boolean ownerInAccessList = accessRecords.stream()
        .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(originalOwner.getId())
                    && a.getAccessLevel() == AccessLevel.OWNER);
    
    if (!ownerInAccessList) {
      // Create a synthetic DTO for the original owner
      RCAccessDTO ownerDto = new RCAccessDTO(
          null, // No ID since it's implied
          rcId,
          rc.getName(),
          originalOwner.getUsername(),
          originalOwner.getFullName() != null ? originalOwner.getFullName() : originalOwner.getUsername(),
          PrincipalType.USER.name(),
          AccessLevel.OWNER.name(),
          rc.getCreatedAt(),
          null // No grantedBy for original owner
      );
      result.add(ownerDto);
    }

    // Add all explicit access records
    result.addAll(accessRecords.stream()
        .map(RCAccessDTO::fromEntity)
        .collect(Collectors.toList()));

    return result;
  }

  @Override
  public RCAccessDTO grantUserAccess(Long rcId, String principalIdentifier, AccessLevel accessLevel,
                                      String requestingUsername) {
    ResponsibilityCentre rc = rcRepository.findById(rcId)
        .orElseThrow(() -> new IllegalArgumentException("RC not found: " + rcId));

    // Verify requester is an owner
    if (!isOwner(rcId, requestingUsername)) {
      throw new SecurityException("Only owners can grant permissions");
    }

    // Prevent modifications to Demo RC
    if (DEMO_RC_NAME.equals(rc.getName())) {
      throw new IllegalArgumentException("Cannot modify permissions for Demo RC");
    }

    // Find the target user
    User targetUser = userRepository.findByUsername(principalIdentifier)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + principalIdentifier));

    // Check if access already exists
    Optional<RCAccess> existingAccess = accessRepository.findByResponsibilityCentreAndUser(rc, targetUser);
    if (existingAccess.isPresent()) {
      throw new IllegalArgumentException("User already has access to this RC. Use update to change access level.");
    }

    // Don't allow granting access to the original owner (they already have implicit OWNER)
    if (rc.getOwner().getId().equals(targetUser.getId()) && accessLevel != AccessLevel.OWNER) {
      throw new IllegalArgumentException("Cannot change access level for the original RC owner");
    }

    // Get the requesting user for audit trail
    User grantingUser = userRepository.findByUsername(requestingUsername).orElse(null);

    RCAccess access = new RCAccess(rc, targetUser, accessLevel);
    access.setGrantedBy(grantingUser);
    
    RCAccess saved = accessRepository.save(access);
    logger.info("Granted {} access to user {} on RC {} by {}", 
        accessLevel, principalIdentifier, rc.getName(), requestingUsername);

    return RCAccessDTO.fromEntity(saved);
  }

  @Override
  public RCAccessDTO grantGroupAccess(Long rcId, String principalIdentifier, String principalDisplayName,
                                       PrincipalType principalType, AccessLevel accessLevel,
                                       String requestingUsername) {
    if (principalType == PrincipalType.USER) {
      throw new IllegalArgumentException("Use grantUserAccess for user principals");
    }

    ResponsibilityCentre rc = rcRepository.findById(rcId)
        .orElseThrow(() -> new IllegalArgumentException("RC not found: " + rcId));

    // Verify requester is an owner
    if (!isOwner(rcId, requestingUsername)) {
      throw new SecurityException("Only owners can grant permissions");
    }

    // Prevent modifications to Demo RC
    if (DEMO_RC_NAME.equals(rc.getName())) {
      throw new IllegalArgumentException("Cannot modify permissions for Demo RC");
    }

    // Check if access already exists
    Optional<RCAccess> existingAccess = accessRepository
        .findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(rc, principalIdentifier, principalType);
    if (existingAccess.isPresent()) {
      throw new IllegalArgumentException("This " + principalType.name().toLowerCase() + 
          " already has access to this RC. Use update to change access level.");
    }

    // Get the requesting user for audit trail
    User grantingUser = userRepository.findByUsername(requestingUsername).orElse(null);

    RCAccess access = new RCAccess(rc, principalIdentifier, principalDisplayName, principalType, accessLevel);
    access.setGrantedBy(grantingUser);

    RCAccess saved = accessRepository.save(access);
    logger.info("Granted {} access to {} {} on RC {} by {}",
        accessLevel, principalType, principalIdentifier, rc.getName(), requestingUsername);

    return RCAccessDTO.fromEntity(saved);
  }

  @Override
  public RCAccessDTO updatePermission(Long accessId, AccessLevel newAccessLevel, String requestingUsername) {
    RCAccess access = accessRepository.findById(accessId)
        .orElseThrow(() -> new IllegalArgumentException("Access record not found: " + accessId));

    ResponsibilityCentre rc = access.getResponsibilityCentre();

    // Verify requester is an owner
    if (!isOwner(rc.getId(), requestingUsername)) {
      throw new SecurityException("Only owners can update permissions");
    }

    // Prevent modifications to Demo RC
    if (DEMO_RC_NAME.equals(rc.getName())) {
      throw new IllegalArgumentException("Cannot modify permissions for Demo RC");
    }

    // Prevent user from demoting their own OWNER permissions if they are the sole owner
    if (access.getAccessLevel() == AccessLevel.OWNER && newAccessLevel != AccessLevel.OWNER) {
      // Check if the user is trying to demote their own owner permissions
      User requestingUser = userRepository.findByUsername(requestingUsername).orElse(null);
      if (requestingUser != null && access.getUser() != null 
          && access.getUser().getId().equals(requestingUser.getId())) {
        // User is trying to demote their own OWNER permissions
        long ownerCount = accessRepository.countOwnersByRC(rc);
        boolean originalOwnerHasExplicitAccess = accessRepository.findOwnersByRC(rc).stream()
            .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(rc.getOwner().getId()));
        long effectiveOwnerCount = originalOwnerHasExplicitAccess ? ownerCount : ownerCount + 1;

        if (effectiveOwnerCount <= 1) {
          throw new IllegalArgumentException(
              "Cannot demote your own owner permissions when you are the sole owner. " +
              "Grant owner access to another user first.");
        }
      }

      // General check: prevent removing the last owner
      long ownerCount = accessRepository.countOwnersByRC(rc);
      boolean originalOwnerHasExplicitAccess = accessRepository.findOwnersByRC(rc).stream()
          .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(rc.getOwner().getId()));
      long effectiveOwnerCount = originalOwnerHasExplicitAccess ? ownerCount : ownerCount + 1;
      
      if (effectiveOwnerCount <= 1) {
        throw new IllegalArgumentException("Cannot remove the last owner from an RC");
      }
    }

    // Don't allow changing the original owner's access
    if (access.getUser() != null && access.getUser().getId().equals(rc.getOwner().getId())) {
      throw new IllegalArgumentException("Cannot change access level for the original RC owner");
    }

    access.setAccessLevel(newAccessLevel);
    RCAccess saved = accessRepository.save(access);
    logger.info("Updated access {} to {} by {}", accessId, newAccessLevel, requestingUsername);

    return RCAccessDTO.fromEntity(saved);
  }

  @Override
  public void revokeAccess(Long accessId, String requestingUsername) {
    RCAccess access = accessRepository.findById(accessId)
        .orElseThrow(() -> new IllegalArgumentException("Access record not found: " + accessId));

    ResponsibilityCentre rc = access.getResponsibilityCentre();

    // Verify requester is an owner
    if (!isOwner(rc.getId(), requestingUsername)) {
      throw new SecurityException("Only owners can revoke permissions");
    }

    // Prevent modifications to Demo RC
    if (DEMO_RC_NAME.equals(rc.getName())) {
      throw new IllegalArgumentException("Cannot modify permissions for Demo RC");
    }

    // Don't allow revoking the original owner's access
    if (access.getUser() != null && access.getUser().getId().equals(rc.getOwner().getId())) {
      throw new IllegalArgumentException("Cannot revoke access for the original RC owner");
    }

    // Prevent owner from removing their own OWNER permissions if they are the sole owner
    if (access.getAccessLevel() == AccessLevel.OWNER) {
      // Check if the user is trying to remove their own owner permissions
      User requestingUser = userRepository.findByUsername(requestingUsername).orElse(null);
      if (requestingUser != null && access.getUser() != null 
          && access.getUser().getId().equals(requestingUser.getId())) {
        // User is trying to remove their own OWNER permissions
        long ownerCount = accessRepository.countOwnersByRC(rc);
        boolean originalOwnerHasExplicitAccess = accessRepository.findOwnersByRC(rc).stream()
            .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(rc.getOwner().getId()));
        long effectiveOwnerCount = originalOwnerHasExplicitAccess ? ownerCount : ownerCount + 1;

        if (effectiveOwnerCount <= 1) {
          throw new IllegalArgumentException(
              "Cannot remove your own owner permissions when you are the sole owner. " +
              "Grant owner access to another user first.");
        }
      }

      // General check: prevent removing the last owner
      long ownerCount = accessRepository.countOwnersByRC(rc);
      boolean originalOwnerHasExplicitAccess = accessRepository.findOwnersByRC(rc).stream()
          .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(rc.getOwner().getId()));
      long effectiveOwnerCount = originalOwnerHasExplicitAccess ? ownerCount : ownerCount + 1;

      if (effectiveOwnerCount <= 1) {
        throw new IllegalArgumentException("Cannot remove the last owner from an RC");
      }
    }

    accessRepository.deleteAccessById(accessId);
    logger.info("Revoked access {} by {}", accessId, requestingUsername);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isOwner(Long rcId, String username) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return false;
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();

    // Check if user is the original owner
    if (rc.getOwner().getId().equals(user.getId())) {
      return true;
    }

    // Check if user has explicit OWNER access
    Optional<RCAccess> access = accessRepository.findByResponsibilityCentreAndUser(rc, user);
    return access.isPresent() && access.get().getAccessLevel() == AccessLevel.OWNER;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AccessLevel> getEffectiveAccessLevel(Long rcId, String username, List<String> groupIdentifiers) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return Optional.empty();
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return Optional.empty();
    }

    User user = userOpt.get();

    // Check if user is the original owner (highest priority)
    if (rc.getOwner().getId().equals(user.getId())) {
      return Optional.of(AccessLevel.OWNER);
    }

    // Get all access records (direct and via groups)
    List<String> identifiers = groupIdentifiers != null ? new ArrayList<>(groupIdentifiers) : new ArrayList<>();
    List<RCAccess> allAccess = accessRepository.findAllAccessForUserInRC(rc, user, identifiers);

    if (allAccess.isEmpty()) {
      return Optional.empty();
    }

    // Find the highest access level (OWNER > READ_WRITE > READ_ONLY)
    return allAccess.stream()
        .map(RCAccess::getAccessLevel)
        .max((a, b) -> {
          int aRank = getAccessRank(a);
          int bRank = getAccessRank(b);
          return Integer.compare(aRank, bRank);
        });
  }

  private int getAccessRank(AccessLevel level) {
    return switch (level) {
      case OWNER -> 3;
      case READ_WRITE -> 2;
      case READ_ONLY -> 1;
    };
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canEditContent(Long rcId, String username) {
    Optional<AccessLevel> level = getEffectiveAccessLevel(rcId, username, List.of());
    return level.isPresent() && 
           (level.get() == AccessLevel.OWNER || level.get() == AccessLevel.READ_WRITE);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canManageRC(Long rcId, String username) {
    return isOwner(rcId, username);
  }
}
