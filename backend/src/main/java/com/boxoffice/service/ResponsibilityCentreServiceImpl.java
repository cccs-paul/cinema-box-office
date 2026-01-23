/*
 * Cinema Box Office - Responsibility Centre Service Implementation
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ResponsibilityCentreService.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Service
@Transactional
public class ResponsibilityCentreServiceImpl implements ResponsibilityCentreService {

  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;

  public ResponsibilityCentreServiceImpl(ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository, UserRepository userRepository) {
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
  }

  private static final String DEMO_RC_NAME = "Demo";

  @Override
  @Transactional(readOnly = true)
  public List<ResponsibilityCentreDTO> getUserResponsibilityCentres(String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return List.of();
    }

    User user = userOpt.get();
    List<ResponsibilityCentreDTO> result = new java.util.ArrayList<>();

    // Get RCs owned by the user
    List<ResponsibilityCentre> ownedRCs = rcRepository.findByOwner(user);
    for (ResponsibilityCentre rc : ownedRCs) {
      // Demo RC is always read-only for all users
      String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "READ_WRITE";
      result.add(ResponsibilityCentreDTO.fromEntity(rc, username, accessLevel));
    }

    // Get RCs shared with the user
    List<RCAccess> accessRecords = accessRepository.findByUser(user);
    for (RCAccess access : accessRecords) {
      ResponsibilityCentre rc = access.getResponsibilityCentre();
      result.add(ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, access));
    }

    return result;
  }

  @Override
  public ResponsibilityCentreDTO createResponsibilityCentre(String username, String name,
      String description) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("User not found: " + username);
    }

    User user = userOpt.get();

    // Check if name already exists for this user
    if (rcRepository.existsByNameAndOwner(name, user)) {
      throw new IllegalArgumentException(
          "A Responsibility Centre with this name already exists for this user");
    }

    ResponsibilityCentre rc = new ResponsibilityCentre(name, description, user);
    ResponsibilityCentre saved = rcRepository.save(rc);

    return ResponsibilityCentreDTO.fromEntity(saved, username, "READ_WRITE");
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ResponsibilityCentreDTO> getResponsibilityCentre(Long rcId, String username) {
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

    // Check if user is the owner
    if (rc.getOwner().getId().equals(user.getId())) {
      // Demo RC is always read-only for all users
      String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "READ_WRITE";
      return Optional.of(ResponsibilityCentreDTO.fromEntity(rc, username, accessLevel));
    }

    // Check if user has access
    Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
    if (accessOpt.isPresent()) {
      return Optional.of(
          ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, accessOpt.get()));
    }

    return Optional.empty();
  }

  @Override
  public Optional<ResponsibilityCentreDTO> updateResponsibilityCentre(Long rcId, String username,
      String name, String description) {
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

    // Only owner can update
    if (!rc.getOwner().getId().equals(user.getId())) {
      throw new IllegalAccessError("Only the owner can update this RC");
    }

    rc.setName(name);
    rc.setDescription(description);
    ResponsibilityCentre updated = rcRepository.save(rc);

    return Optional.of(ResponsibilityCentreDTO.fromEntity(updated, username, "READ_WRITE"));
  }

  @Override
  public boolean deleteResponsibilityCentre(Long rcId, String username) {
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

    // Only owner can delete
    if (!rc.getOwner().getId().equals(user.getId())) {
      throw new IllegalAccessError("Only the owner can delete this RC");
    }

    rcRepository.deleteById(rcId);
    return true;
  }

  @Override
  public Optional<RCAccess> grantAccess(Long rcId, String ownerUsername,
      String grantedToUsername, String accessLevel) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return Optional.empty();
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> ownerOpt = userRepository.findByUsername(ownerUsername);
    if (ownerOpt.isEmpty()) {
      return Optional.empty();
    }

    User owner = ownerOpt.get();

    // Verify requesting user is the owner
    if (!rc.getOwner().getId().equals(owner.getId())) {
      throw new IllegalAccessError("Only the owner can grant access");
    }

    Optional<User> grantedToOpt = userRepository.findByUsername(grantedToUsername);
    if (grantedToOpt.isEmpty()) {
      return Optional.empty();
    }

    User grantedTo = grantedToOpt.get();

    // Validate access level
    RCAccess.AccessLevel level;
    try {
      level = RCAccess.AccessLevel.valueOf(accessLevel);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid access level: " + accessLevel);
    }

    // Remove existing access if any
    accessRepository.deleteByResponsibilityCentreAndUser(rc, grantedTo);

    // Create new access record
    RCAccess access = new RCAccess(rc, grantedTo, level);
    RCAccess saved = accessRepository.save(access);

    return Optional.of(saved);
  }

  @Override
  public boolean revokeAccess(Long rcId, String ownerUsername, String revokeFromUsername) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return false;
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> ownerOpt = userRepository.findByUsername(ownerUsername);
    if (ownerOpt.isEmpty()) {
      return false;
    }

    User owner = ownerOpt.get();

    // Verify requesting user is the owner
    if (!rc.getOwner().getId().equals(owner.getId())) {
      throw new IllegalAccessError("Only the owner can revoke access");
    }

    Optional<User> revokeFromOpt = userRepository.findByUsername(revokeFromUsername);
    if (revokeFromOpt.isEmpty()) {
      return false;
    }

    User revokeFrom = revokeFromOpt.get();
    accessRepository.deleteByResponsibilityCentreAndUser(rc, revokeFrom);

    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RCAccess> getResponsibilityCentreAccess(Long rcId, String ownerUsername) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return List.of();
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> ownerOpt = userRepository.findByUsername(ownerUsername);
    if (ownerOpt.isEmpty()) {
      return List.of();
    }

    User owner = ownerOpt.get();

    // Verify requesting user is the owner
    if (!rc.getOwner().getId().equals(owner.getId())) {
      throw new IllegalAccessError("Only the owner can view access records");
    }

    return accessRepository.findByResponsibilityCentre(rc);
  }

  @Override
  public ResponsibilityCentreDTO cloneResponsibilityCentre(Long sourceRcId, String username, String newName) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("User not found: " + username);
    }

    User user = userOpt.get();

    Optional<ResponsibilityCentre> sourceRcOpt = rcRepository.findById(sourceRcId);
    if (sourceRcOpt.isEmpty()) {
      throw new IllegalArgumentException("Source responsibility centre not found: " + sourceRcId);
    }

    ResponsibilityCentre sourceRc = sourceRcOpt.get();

    // Check if user has access to the source RC
    boolean hasAccess = sourceRc.getOwner().getId().equals(user.getId());
    if (!hasAccess) {
      Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(sourceRc, user);
      hasAccess = accessOpt.isPresent();
    }

    if (!hasAccess) {
      throw new IllegalAccessError("User does not have access to clone this RC");
    }

    // Check if name already exists for this user
    if (rcRepository.existsByNameAndOwner(newName, user)) {
      throw new IllegalArgumentException(
          "A Responsibility Centre with this name already exists for this user");
    }

    // Create the cloned RC
    ResponsibilityCentre clonedRc = new ResponsibilityCentre(
        newName,
        sourceRc.getDescription(),
        user
    );

    ResponsibilityCentre saved = rcRepository.save(clonedRc);

    return ResponsibilityCentreDTO.fromEntity(saved, username, "READ_WRITE");
  }
}
