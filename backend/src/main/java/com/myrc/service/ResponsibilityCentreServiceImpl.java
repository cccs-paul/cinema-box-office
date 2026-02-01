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

  public ResponsibilityCentreServiceImpl(
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository,
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
      ProcurementQuoteFileRepository procurementQuoteFileRepository) {
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
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
      // Demo RC is always read-only for all users, otherwise owner has OWNER access
      String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "OWNER";
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

    return ResponsibilityCentreDTO.fromEntity(saved, username, "OWNER");
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
      // Demo RC is always read-only for all users, otherwise owner has OWNER access
      String accessLevel = DEMO_RC_NAME.equals(rc.getName()) ? "READ_ONLY" : "OWNER";
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

    return ResponsibilityCentreDTO.fromEntity(saved, username, "OWNER");
  }
}
