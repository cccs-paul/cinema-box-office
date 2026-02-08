/*
 * myRC - RC Permission Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.config.LdapSecurityConfig;
import com.myrc.dto.RCAccessDTO;
import com.myrc.model.RCAccess;
import com.myrc.model.RCAccess.AccessLevel;
import com.myrc.model.RCAccess.PrincipalType;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final DirectorySearchService directorySearchService;
  private final UserService userService;

  public RCPermissionServiceImpl(RCAccessRepository accessRepository,
                                  ResponsibilityCentreRepository rcRepository,
                                  UserRepository userRepository,
                                  DirectorySearchService directorySearchService,
                                  UserService userService) {
    this.accessRepository = accessRepository;
    this.rcRepository = rcRepository;
    this.userRepository = userRepository;
    this.directorySearchService = directorySearchService;
    this.userService = userService;
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

    // Get the requesting user for audit trail
    User grantingUser = userRepository.findByUsername(requestingUsername).orElse(null);

    // Try to find the target user in the local database
    Optional<User> localUser = userRepository.findByUsername(principalIdentifier);

    if (localUser.isPresent()) {
      // Local user: use FK-based access (preserves existing behavior)
      User targetUser = localUser.get();

      Optional<RCAccess> existingAccess = accessRepository.findByResponsibilityCentreAndUser(rc, targetUser);
      if (existingAccess.isPresent()) {
        RCAccess existing = existingAccess.get();
        if (existing.getAccessLevel() == accessLevel) {
          throw new IllegalArgumentException("User '" + principalIdentifier + 
              "' already has " + accessLevel.name() + " access to this RC.");
        }
        throw new IllegalArgumentException("User '" + principalIdentifier + 
            "' already has " + existing.getAccessLevel().name() + 
            " access to this RC. Use update to change the access level.");
      }

      if (rc.getOwner().getId().equals(targetUser.getId()) && accessLevel != AccessLevel.OWNER) {
        throw new IllegalArgumentException("Cannot change access level for the original RC owner");
      }

      RCAccess access = new RCAccess(rc, targetUser, accessLevel);
      access.setGrantedBy(grantingUser);
      RCAccess saved = accessRepository.save(access);
      logger.info("Granted {} access to local user {} on RC {} by {}",
          accessLevel, principalIdentifier, rc.getName(), requestingUsername);
      return RCAccessDTO.fromEntity(saved);
    }

    // User not in local DB: look up in directory (LDAP) for validation and display name
    List<DirectorySearchService.SearchResult> results =
        directorySearchService.searchUsers(principalIdentifier, 50);
    DirectorySearchService.SearchResult match = results.stream()
        .filter(r -> r.identifier().equalsIgnoreCase(principalIdentifier))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + principalIdentifier));

    // Check if access already exists by principalIdentifier
    Optional<RCAccess> existingAccess = accessRepository
        .findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
            rc, match.identifier(), PrincipalType.USER);
    if (existingAccess.isPresent()) {
      RCAccess existing = existingAccess.get();
      if (existing.getAccessLevel() == accessLevel) {
        throw new IllegalArgumentException("User '" + principalIdentifier +
            "' already has " + accessLevel.name() + " access to this RC.");
      }
      throw new IllegalArgumentException("User '" + principalIdentifier +
          "' already has " + existing.getAccessLevel().name() +
          " access to this RC. Use update to change the access level.");
    }

    // Store as string-only access (no User FK) — same pattern as groups
    String displayName = match.displayName() != null ? match.displayName() : match.identifier();
    RCAccess access = new RCAccess(rc, match.identifier(), displayName, PrincipalType.USER, accessLevel);
    access.setGrantedBy(grantingUser);
    RCAccess saved = accessRepository.save(access);
    logger.info("Granted {} access to directory user {} on RC {} by {}",
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
      RCAccess existing = existingAccess.get();
      String principalTypeName = principalType == PrincipalType.GROUP ? "Group" : "Distribution list";
      if (existing.getAccessLevel() == accessLevel) {
        throw new IllegalArgumentException(principalTypeName + " '" + principalIdentifier + 
            "' already has " + accessLevel.name() + " access to this RC.");
      }
      throw new IllegalArgumentException(principalTypeName + " '" + principalIdentifier + 
          "' already has " + existing.getAccessLevel().name() + 
          " access to this RC. Use update to change the access level.");
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
    // Delegate to getEffectiveAccessLevel which correctly resolves access from all
    // sources: original owner FK, direct User FK, principalIdentifier (USER and GROUP).
    List<String> groupDns = extractGroupDnsFromSecurityContext();
    Optional<AccessLevel> level = getEffectiveAccessLevel(rcId, username, groupDns);
    return level.isPresent() && level.get() == AccessLevel.OWNER;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AccessLevel> getEffectiveAccessLevel(Long rcId, String username, List<String> groupIdentifiers) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return Optional.empty();
    }

    ResponsibilityCentre rc = rcOpt.get();
    List<String> identifiers = groupIdentifiers != null ? new ArrayList<>(groupIdentifiers) : new ArrayList<>();

    // Also include the username as an identifier so LDAP USER-type access records
    // stored by principalIdentifier are matched
    identifiers.add(username);

    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isPresent()) {
      User user = userOpt.get();

      // Check if user is the original owner (highest priority)
      if (rc.getOwner().getId().equals(user.getId())) {
        return Optional.of(AccessLevel.OWNER);
      }

      // Get all access records (direct User FK, principalIdentifier, and via groups)
      List<RCAccess> allAccess = accessRepository.findAllAccessForUserInRC(rc, user, identifiers);
      if (!allAccess.isEmpty()) {
        return allAccess.stream()
            .map(RCAccess::getAccessLevel)
            .max((a, b) -> Integer.compare(getAccessRank(a), getAccessRank(b)));
      }
      return Optional.empty();
    }

    // User not in local DB (LDAP user): check by principalIdentifier only
    List<RCAccess> identifierAccess = accessRepository
        .findByPrincipalIdentifierIn(identifiers);
    List<RCAccess> rcAccess = identifierAccess.stream()
        .filter(a -> a.getResponsibilityCentre().getId().equals(rcId))
        .collect(Collectors.toList());
    if (rcAccess.isEmpty()) {
      return Optional.empty();
    }
    return rcAccess.stream()
        .map(RCAccess::getAccessLevel)
        .max((a, b) -> Integer.compare(getAccessRank(a), getAccessRank(b)));
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
  public boolean canEditContent(Long rcId, String username, List<String> groupIdentifiers) {
    Optional<AccessLevel> level = getEffectiveAccessLevel(rcId, username, groupIdentifiers);
    return level.isPresent() && 
           (level.get() == AccessLevel.OWNER || level.get() == AccessLevel.READ_WRITE);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canManageRC(Long rcId, String username) {
    return isOwner(rcId, username);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasAccess(Long rcId, String username) {
    // Demo RC is accessible to all authenticated users
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isPresent() && DEMO_RC_NAME.equals(rcOpt.get().getName())) {
      return true;
    }

    List<String> groupDns = extractGroupDnsFromSecurityContext();
    Optional<AccessLevel> level = getEffectiveAccessLevel(rcId, username, groupDns);
    return level.isPresent();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasWriteAccess(Long rcId, String username) {
    List<String> groupDns = extractGroupDnsFromSecurityContext();
    Optional<AccessLevel> level = getEffectiveAccessLevel(rcId, username, groupDns);
    return level.isPresent()
        && (level.get() == AccessLevel.OWNER || level.get() == AccessLevel.READ_WRITE);
  }

  /**
   * Extract LDAP group DNs from the current security context.
   * Returns an empty list if no authentication or no LDAP group authorities are present.
   */
  private List<String> extractGroupDnsFromSecurityContext() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return List.of();
    }
    return LdapSecurityConfig.extractGroupDns(auth);
  }

  @Override
  @Transactional
  public void relinquishOwnership(Long rcId, String requestingUsername) {
    ResponsibilityCentre rc = rcRepository.findById(rcId)
        .orElseThrow(() -> new IllegalArgumentException("RC not found: " + rcId));

    // Verify the requester is the current original owner (not just any owner via group/access)
    User requestingUser = userRepository.findByUsername(requestingUsername)
        .orElseThrow(() -> new SecurityException("User not found: " + requestingUsername));

    if (!rc.getOwner().getId().equals(requestingUser.getId())) {
      throw new SecurityException("Only the original owner can relinquish ownership");
    }

    // Prevent modifications to Demo RC
    if (DEMO_RC_NAME.equals(rc.getName())) {
      throw new IllegalArgumentException("Cannot modify ownership of Demo RC");
    }

    // Find another explicit OWNER user (FK-based or identifier-based USER type) to transfer to
    List<RCAccess> owners = accessRepository.findOwnersByRC(rc);
    User newOwner = owners.stream()
        .filter(a -> a.getUser() != null && !a.getUser().getId().equals(requestingUser.getId()))
        .map(RCAccess::getUser)
        .findFirst()
        .orElse(null);

    // Also check identifier-based USER OWNER access (LDAP users with explicit OWNER)
    if (newOwner == null) {
      for (RCAccess access : owners) {
        if (access.getPrincipalType() == PrincipalType.USER
            && access.getPrincipalIdentifier() != null
            && !access.getPrincipalIdentifier().equals(requestingUsername)) {
          // Try to resolve as a local user, or create them if they're an LDAP user
          newOwner = userRepository.findByUsername(access.getPrincipalIdentifier()).orElse(null);
          if (newOwner == null) {
            // LDAP user who hasn't logged in yet - create a local record
            userService.createOrUpdateLdapUser(
                access.getPrincipalIdentifier(), null, access.getPrincipalDisplayName(),
                access.getPrincipalIdentifier());
            newOwner = userRepository.findByUsername(access.getPrincipalIdentifier()).orElse(null);
          }
          if (newOwner != null) {
            break;
          }
        }
      }
    }

    if (newOwner == null) {
      throw new IllegalArgumentException(
          "Cannot relinquish ownership: no other user with OWNER access exists. "
          + "Grant OWNER access to another user first.");
    }

    // Transfer ownership
    User oldOwner = rc.getOwner();
    final User transferTo = newOwner;
    rc.setOwner(transferTo);
    rcRepository.save(rc);

    // Remove the new owner's explicit access record (they are now the implicit owner)
    // Check both by user FK and by principal identifier (for LDAP users)
    accessRepository.findByResponsibilityCentreAndUser(rc, transferTo)
        .ifPresentOrElse(
            fkAccess -> accessRepository.deleteAccessById(fkAccess.getId()),
            () -> accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
                    rc, transferTo.getUsername(), PrincipalType.USER)
                .ifPresent(idAccess -> accessRepository.deleteAccessById(idAccess.getId()))
        );

    // Create READ_WRITE access for the former owner
    RCAccess formerOwnerAccess = new RCAccess(rc, oldOwner, AccessLevel.READ_WRITE);
    formerOwnerAccess.setGrantedBy(oldOwner);
    accessRepository.save(formerOwnerAccess);

    logger.info("Ownership of RC '{}' transferred from {} to {}",
        rc.getName(), requestingUsername, transferTo.getUsername());
  }
}
