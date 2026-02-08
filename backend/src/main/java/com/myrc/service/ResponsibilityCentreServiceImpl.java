/*
 * myRC - Responsibility Centre Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.ResponsibilityCentreDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.ProcurementItem;
import com.myrc.model.ProcurementQuote;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingItem;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.MoneyAllocationRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import com.myrc.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ResponsibilityCentreService.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-17
 */
@Service
@Transactional
public class ResponsibilityCentreServiceImpl implements ResponsibilityCentreService {

  private static final Logger logger = LoggerFactory.getLogger(ResponsibilityCentreServiceImpl.class);

  @PersistenceContext
  private EntityManager entityManager;

  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final FiscalYearRepository fiscalYearRepository;
  private final FundingItemRepository fundingItemRepository;
  private final SpendingItemRepository spendingItemRepository;
  private final MoneyRepository moneyRepository;
  private final SpendingCategoryRepository spendingCategoryRepository;
  private final CategoryRepository fundingCategoryRepository;
  private final MoneyAllocationRepository moneyAllocationRepository;
  private final SpendingMoneyAllocationRepository spendingMoneyAllocationRepository;
  private final ProcurementItemRepository procurementItemRepository;
  private final ProcurementQuoteRepository procurementQuoteRepository;
  private final ProcurementQuoteFileRepository procurementQuoteFileRepository;
  private final FiscalYearCloneService fiscalYearCloneService;

  public ResponsibilityCentreServiceImpl(
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository,
      UserService userService,
      FiscalYearRepository fiscalYearRepository,
      FundingItemRepository fundingItemRepository,
      SpendingItemRepository spendingItemRepository,
      MoneyRepository moneyRepository,
      SpendingCategoryRepository spendingCategoryRepository,
      CategoryRepository fundingCategoryRepository,
      MoneyAllocationRepository moneyAllocationRepository,
      SpendingMoneyAllocationRepository spendingMoneyAllocationRepository,
      ProcurementItemRepository procurementItemRepository,
      ProcurementQuoteRepository procurementQuoteRepository,
      ProcurementQuoteFileRepository procurementQuoteFileRepository,
      FiscalYearCloneService fiscalYearCloneService) {
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.fiscalYearRepository = fiscalYearRepository;
    this.fundingItemRepository = fundingItemRepository;
    this.spendingItemRepository = spendingItemRepository;
    this.moneyRepository = moneyRepository;
    this.spendingCategoryRepository = spendingCategoryRepository;
    this.fundingCategoryRepository = fundingCategoryRepository;
    this.moneyAllocationRepository = moneyAllocationRepository;
    this.spendingMoneyAllocationRepository = spendingMoneyAllocationRepository;
    this.procurementItemRepository = procurementItemRepository;
    this.procurementQuoteRepository = procurementQuoteRepository;
    this.procurementQuoteFileRepository = procurementQuoteFileRepository;
    this.fiscalYearCloneService = fiscalYearCloneService;
  }

  private static final String DEMO_RC_NAME = "Demo";

  /**
   * Find a local User entity by username, or auto-provision a lightweight LDAP
   * user entity on demand. LDAP users are not persisted during login, but when
   * they perform write operations (creating or cloning an RC) they need a local
   * entity to serve as the FK owner.
   *
   * @param username the username to look up
   * @return the existing or newly created User entity
   * @throws IllegalArgumentException if the user cannot be found or provisioned
   */
  private User findOrProvisionUser(String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isPresent()) {
      return userOpt.get();
    }

    // Check if the current security context indicates this is an LDAP user.
    // LDAP users carry authorities prefixed with LDAP_GROUP_DN_ so any such
    // authority proves the caller authenticated via LDAP.
    Authentication auth = org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication();
    if (auth != null && auth.getName().equals(username)) {
      boolean isLdapUser = auth.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .anyMatch(a -> a.startsWith(
              com.myrc.config.LdapSecurityConfig.LDAP_GROUP_DN_PREFIX));

      if (isLdapUser) {
        logger.info("Auto-provisioning local User entity for LDAP user: {}", username);
        userService.createOrUpdateLdapUser(username, null, null, username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(
                "Failed to provision LDAP user: " + username));
      }
    }

    throw new IllegalArgumentException("User not found: " + username);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponsibilityCentreDTO> getUserResponsibilityCentres(String username,
      List<String> groupIdentifiers) {
    List<ResponsibilityCentreDTO> result = new java.util.ArrayList<>();
    java.util.Set<Long> addedRcIds = new java.util.HashSet<>();

    Optional<User> userOpt = userRepository.findByUsername(username);

    if (userOpt.isPresent()) {
      User user = userOpt.get();

      // Get RCs owned by the user
      List<ResponsibilityCentre> ownedRCs = rcRepository.findByOwner(user);
      for (ResponsibilityCentre rc : ownedRCs) {
        // Demo RC is always read-only for all users, otherwise owner has OWNER access
        String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "OWNER";
        result.add(ResponsibilityCentreDTO.fromEntity(rc, username, accessLevel));
        addedRcIds.add(rc.getId());
      }

      // Get RCs shared with the user via direct User FK
      List<RCAccess> accessRecords = accessRepository.findByUser(user);
      for (RCAccess access : accessRecords) {
        ResponsibilityCentre rc = access.getResponsibilityCentre();
        if (addedRcIds.add(rc.getId())) {
          result.add(ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, access));
        }
      }
    }

    // Get RCs accessible via principalIdentifier (group DNs, distribution lists,
    // or LDAP USER-type access stored by identifier)
    List<String> identifiers = groupIdentifiers != null
        ? new java.util.ArrayList<>(groupIdentifiers) : new java.util.ArrayList<>();
    identifiers.add(username);

    if (!identifiers.isEmpty()) {
      List<RCAccess> identifierAccess = accessRepository.findByPrincipalIdentifierIn(identifiers);
      for (RCAccess access : identifierAccess) {
        ResponsibilityCentre rc = access.getResponsibilityCentre();
        if (addedRcIds.add(rc.getId())) {
          result.add(ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, access));
        }
      }
    }

    // Demo RC is always visible to every authenticated user with READ_ONLY access,
    // regardless of whether they have a local User entity or explicit access grants.
    Optional<ResponsibilityCentre> demoRcOpt = rcRepository.findByName(DEMO_RC_NAME);
    if (demoRcOpt.isPresent()) {
      ResponsibilityCentre demoRc = demoRcOpt.get();
      if (addedRcIds.add(demoRc.getId())) {
        result.add(ResponsibilityCentreDTO.fromEntity(demoRc, username, "READ_ONLY"));
      }
    }

    return result;
  }

  @Override
  public ResponsibilityCentreDTO createResponsibilityCentre(String username, String name,
      String description) {
    User user = findOrProvisionUser(username);

    // Check if name already exists globally (RC names must be unique across all users)
    if (rcRepository.existsByName(name)) {
      throw new IllegalArgumentException(
          "A Responsibility Centre with this name already exists. RC names must be unique.");
    }

    ResponsibilityCentre rc = new ResponsibilityCentre(name, description, user);
    ResponsibilityCentre saved = rcRepository.save(rc);

    return ResponsibilityCentreDTO.fromEntity(saved, username, "OWNER");
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ResponsibilityCentreDTO> getResponsibilityCentre(Long rcId, String username,
      List<String> groupIdentifiers) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return Optional.empty();
    }

    ResponsibilityCentre rc = rcOpt.get();
    Optional<User> userOpt = userRepository.findByUsername(username);

    if (userOpt.isPresent()) {
      User user = userOpt.get();

      // Check if user is the owner
      if (rc.getOwner().getId().equals(user.getId())) {
        // Demo RC is always read-only for all users, otherwise owner has OWNER access
        String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "OWNER";
        return Optional.of(ResponsibilityCentreDTO.fromEntity(rc, username, accessLevel));
      }

      // Check if user has direct access via User FK
      Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
      if (accessOpt.isPresent()) {
        return Optional.of(
            ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, accessOpt.get()));
      }
    }

    // Check access via principalIdentifier (group DNs, distribution lists,
    // or LDAP USER-type access stored by identifier)
    List<String> identifiers = groupIdentifiers != null
        ? new java.util.ArrayList<>(groupIdentifiers) : new java.util.ArrayList<>();
    identifiers.add(username);

    List<RCAccess> identifierAccess = accessRepository
        .findByResponsibilityCentreAndPrincipalIdentifierIn(rc, identifiers);
    if (!identifierAccess.isEmpty()) {
      // Use the highest access level found
      RCAccess bestAccess = identifierAccess.stream()
          .max((a, b) -> Integer.compare(
              getAccessRank(a.getAccessLevel()), getAccessRank(b.getAccessLevel())))
          .orElse(identifierAccess.getFirst());
      return Optional.of(
          ResponsibilityCentreDTO.fromEntityWithAccess(rc, username, bestAccess));
    }

    // Demo RC is always accessible to every authenticated user with READ_ONLY access
    if (DEMO_RC_NAME.equals(rc.getName())) {
      return Optional.of(ResponsibilityCentreDTO.fromEntity(rc, username, "READ_ONLY"));
    }

    return Optional.empty();
  }

  private int getAccessRank(RCAccess.AccessLevel level) {
    return switch (level) {
      case OWNER -> 3;
      case READ_WRITE -> 2;
      case READ_ONLY -> 1;
    };
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

    // Check if the new name already exists (excluding the current RC)
    if (!rc.getName().equals(name) && rcRepository.existsByNameAndIdNot(name, rcId)) {
      throw new IllegalArgumentException(
          "A Responsibility Centre with this name already exists. RC names must be unique.");
    }

    rc.setName(name);
    rc.setDescription(description);
    ResponsibilityCentre updated = rcRepository.save(rc);

    return Optional.of(ResponsibilityCentreDTO.fromEntity(updated, username, "OWNER"));
  }

  @Override
  public boolean deleteResponsibilityCentre(Long rcId, String username) {
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return false;
    }

    ResponsibilityCentre rc = rcOpt.get();
    String rcName = rc.getName(); // Store name before entity manager clear
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();

    // Only owner can delete
    if (!rc.getOwner().getId().equals(user.getId())) {
      throw new IllegalAccessError("Only the owner can delete this RC");
    }

    logger.info("Deleting responsibility centre {} (ID: {}) and all related entities", rcName, rcId);

    // Delete access records first
    accessRepository.deleteByResponsibilityCentre(rc);
    logger.debug("Deleted access records for RC {}", rcId);

    // Get all fiscal years for this RC to cascade delete their children
    List<FiscalYear> fiscalYears = fiscalYearRepository.findByResponsibilityCentreId(rcId);
    
    for (FiscalYear fiscalYear : fiscalYears) {
      Long fyId = fiscalYear.getId();
      logger.debug("Deleting data for fiscal year {} (ID: {})", fiscalYear.getName(), fyId);
      
      // Delete procurement data (deepest level first: files -> quotes -> items)
      List<ProcurementItem> procurementItems = procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(fyId);
      for (ProcurementItem item : procurementItems) {
        List<ProcurementQuote> quotes = procurementQuoteRepository.findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(item.getId());
        for (ProcurementQuote quote : quotes) {
          procurementQuoteFileRepository.deleteByQuoteId(quote.getId());
        }
        procurementQuoteRepository.deleteByProcurementItemId(item.getId());
      }
      procurementItemRepository.deleteByFiscalYearId(fyId);
      
      // Delete spending money allocations for all spending items in this fiscal year
      List<SpendingItem> spendingItems = spendingItemRepository.findByFiscalYearIdOrderByNameAsc(fyId);
      for (SpendingItem spendingItem : spendingItems) {
        spendingMoneyAllocationRepository.deleteBySpendingItemId(spendingItem.getId());
      }
      
      // Delete funding money allocations for all funding items in this fiscal year
      List<FundingItem> fundingItems = fundingItemRepository.findByFiscalYearId(fyId);
      for (FundingItem fundingItem : fundingItems) {
        moneyAllocationRepository.deleteByFundingItemId(fundingItem.getId());
      }
      
      // Delete spending items (must be before categories due to FK)
      spendingItemRepository.deleteByFiscalYearId(fyId);
      
      // Delete funding items
      fundingItemRepository.deleteByFiscalYearId(fyId);
      
      // Delete spending categories
      spendingCategoryRepository.deleteByFiscalYearId(fyId);
      
      // Delete funding categories (the main categories table)
      fundingCategoryRepository.deleteByFiscalYearId(fyId);
      
      // Delete monies
      moneyRepository.deleteByFiscalYearId(fyId);
    }
    
    // Delete all fiscal years using ID-based deletion
    fiscalYearRepository.deleteByResponsibilityCentreId(rcId);
    logger.debug("Deleted {} fiscal years for RC {}", fiscalYears.size(), rcId);

    // Flush pending changes and clear the persistence context
    // This ensures all child deletions are committed and avoids TransientObjectException
    entityManager.flush();
    entityManager.clear();

    // Finally, delete the RC itself
    rcRepository.deleteById(rcId);
    logger.info("Successfully deleted responsibility centre {} (ID: {})", rcName, rcId);
    
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
    User user = findOrProvisionUser(username);

    Optional<ResponsibilityCentre> sourceRcOpt = rcRepository.findById(sourceRcId);
    if (sourceRcOpt.isEmpty()) {
      throw new IllegalArgumentException("Source responsibility centre not found: " + sourceRcId);
    }

    ResponsibilityCentre sourceRc = sourceRcOpt.get();

    // Check if user has access to the source RC (owner, direct access, or Demo RC)
    boolean hasAccess = sourceRc.getOwner().getId().equals(user.getId());
    if (!hasAccess) {
      Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(sourceRc, user);
      hasAccess = accessOpt.isPresent();
    }
    if (!hasAccess) {
      // Demo RC is accessible to all authenticated users
      hasAccess = DEMO_RC_NAME.equals(sourceRc.getName());
    }

    if (!hasAccess) {
      throw new IllegalAccessError("User does not have access to clone this RC");
    }

    // Check if name already exists globally (RC names must be unique across all users)
    if (rcRepository.existsByName(newName)) {
      throw new IllegalArgumentException(
          "A Responsibility Centre with this name already exists. RC names must be unique.");
    }

    // Create the cloned RC
    ResponsibilityCentre clonedRc = new ResponsibilityCentre(
        newName,
        sourceRc.getDescription(),
        user
    );

    ResponsibilityCentre saved = rcRepository.save(clonedRc);

    // Deep-clone all fiscal years and their child data
    List<FiscalYear> sourceFiscalYears = fiscalYearRepository.findByResponsibilityCentreId(sourceRcId);
    logger.info("Deep cloning {} fiscal years from RC '{}' to '{}'",
        sourceFiscalYears.size(), sourceRc.getName(), newName);

    for (FiscalYear sourceFY : sourceFiscalYears) {
      fiscalYearCloneService.deepCloneFiscalYear(sourceFY, sourceFY.getName(), saved);
    }

    logger.info("Successfully deep-cloned RC '{}' (ID: {}) as '{}' (ID: {}) with {} fiscal years",
        sourceRc.getName(), sourceRcId, newName, saved.getId(), sourceFiscalYears.size());

    return ResponsibilityCentreDTO.fromEntity(saved, username, "OWNER");
  }
}
