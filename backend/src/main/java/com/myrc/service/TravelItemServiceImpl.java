/*
 * myRC - Travel Item Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.TravelItemDTO;
import com.myrc.dto.TravelMoneyAllocationDTO;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.TravelItem;
import com.myrc.model.TravelMoneyAllocation;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.TravelItemRepository;
import com.myrc.repository.TravelMoneyAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service implementation for Travel Item management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Service
public class TravelItemServiceImpl implements TravelItemService {

  private static final Logger logger = Logger.getLogger(TravelItemServiceImpl.class.getName());

  private final TravelItemRepository travelItemRepository;
  private final TravelMoneyAllocationRepository allocationRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final MoneyRepository moneyRepository;
  private final RCPermissionService rcPermissionService;

  public TravelItemServiceImpl(
      TravelItemRepository travelItemRepository,
      TravelMoneyAllocationRepository allocationRepository,
      FiscalYearRepository fiscalYearRepository,
      MoneyRepository moneyRepository,
      RCPermissionService rcPermissionService) {
    this.travelItemRepository = travelItemRepository;
    this.allocationRepository = allocationRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.moneyRepository = moneyRepository;
    this.rcPermissionService = rcPermissionService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TravelItemDTO> getTravelItemsByFiscalYearId(Long fiscalYearId, String username) {
    FiscalYear fy = fiscalYearRepository.findById(fiscalYearId)
        .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + fiscalYearId));

    Long rcId = fy.getResponsibilityCentre().getId();
    if (!rcPermissionService.hasAccess(rcId, username)) {
      throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
    }

    return travelItemRepository.findByFiscalYearIdOrderByNameAsc(fiscalYearId)
        .stream()
        .map(TravelItemDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TravelItemDTO> getTravelItemById(Long travelItemId, String username) {
    return travelItemRepository.findById(travelItemId)
        .map(item -> {
          Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
          if (!rcPermissionService.hasAccess(rcId, username)) {
            throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
          }
          return TravelItemDTO.fromEntity(item);
        });
  }

  @Override
  @Transactional
  public TravelItemDTO createTravelItem(TravelItemDTO dto, String username) {
    FiscalYear fy = fiscalYearRepository.findById(dto.getFiscalYearId())
        .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + dto.getFiscalYearId()));

    Long rcId = fy.getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    if (travelItemRepository.existsByNameAndFiscalYearId(dto.getName(), fy.getId())) {
      throw new IllegalArgumentException("A travel item with name '" + dto.getName() + "' already exists in this fiscal year");
    }

    TravelItem item = new TravelItem();
    item.setName(dto.getName());
    item.setDescription(dto.getDescription());
    item.setTravelAuthorizationNumber(dto.getTravelAuthorizationNumber());
    item.setReferenceNumber(dto.getReferenceNumber());
    item.setDestination(dto.getDestination());
    item.setPurpose(dto.getPurpose());
    item.setEstimatedCost(dto.getEstimatedCost());
    item.setActualCost(dto.getActualCost());
    item.setStatus(dto.getStatus() != null ? TravelItem.Status.valueOf(dto.getStatus()) : TravelItem.Status.PLANNED);
    item.setTravelType(dto.getTravelType() != null ? TravelItem.TravelType.valueOf(dto.getTravelType()) : TravelItem.TravelType.OTHER);
    item.setCurrency(dto.getCurrency() != null ? Currency.valueOf(dto.getCurrency()) : Currency.CAD);
    item.setExchangeRate(dto.getExchangeRate());
    item.setDepartureDate(dto.getDepartureDate());
    item.setReturnDate(dto.getReturnDate());
    item.setTravellerName(dto.getTravellerName());
    item.setNumberOfTravellers(dto.getNumberOfTravellers());
    item.setFiscalYear(fy);

    item = travelItemRepository.save(item);

    if (dto.getMoneyAllocations() != null && !dto.getMoneyAllocations().isEmpty()) {
      saveMoneyAllocations(item, dto.getMoneyAllocations());
    }

    logger.info("Created travel item: " + item.getName() + " (ID: " + item.getId() + ") by user: " + username);
    return TravelItemDTO.fromEntity(item);
  }

  @Override
  @Transactional
  public TravelItemDTO updateTravelItem(Long travelItemId, TravelItemDTO dto, String username) {
    TravelItem item = travelItemRepository.findById(travelItemId)
        .orElseThrow(() -> new IllegalArgumentException("Travel item not found: " + travelItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    if (dto.getName() != null && !dto.getName().equals(item.getName())) {
      if (travelItemRepository.existsByNameAndFiscalYearId(dto.getName(), item.getFiscalYear().getId())) {
        throw new IllegalArgumentException("A travel item with name '" + dto.getName() + "' already exists in this fiscal year");
      }
    }

    if (dto.getName() != null) item.setName(dto.getName());
    if (dto.getDescription() != null) item.setDescription(dto.getDescription());
    if (dto.getTravelAuthorizationNumber() != null) item.setTravelAuthorizationNumber(dto.getTravelAuthorizationNumber());
    if (dto.getReferenceNumber() != null) item.setReferenceNumber(dto.getReferenceNumber());
    if (dto.getDestination() != null) item.setDestination(dto.getDestination());
    if (dto.getPurpose() != null) item.setPurpose(dto.getPurpose());
    if (dto.getEstimatedCost() != null) item.setEstimatedCost(dto.getEstimatedCost());
    if (dto.getActualCost() != null) item.setActualCost(dto.getActualCost());
    if (dto.getStatus() != null) item.setStatus(TravelItem.Status.valueOf(dto.getStatus()));
    if (dto.getTravelType() != null) item.setTravelType(TravelItem.TravelType.valueOf(dto.getTravelType()));
    if (dto.getCurrency() != null) item.setCurrency(Currency.valueOf(dto.getCurrency()));
    if (dto.getExchangeRate() != null) item.setExchangeRate(dto.getExchangeRate());
    if (dto.getDepartureDate() != null) item.setDepartureDate(dto.getDepartureDate());
    if (dto.getReturnDate() != null) item.setReturnDate(dto.getReturnDate());
    if (dto.getTravellerName() != null) item.setTravellerName(dto.getTravellerName());
    if (dto.getNumberOfTravellers() != null) item.setNumberOfTravellers(dto.getNumberOfTravellers());

    if (dto.getMoneyAllocations() != null) {
      item.getMoneyAllocations().clear();
      item = travelItemRepository.save(item);
      saveMoneyAllocations(item, dto.getMoneyAllocations());
    }

    item = travelItemRepository.save(item);
    logger.info("Updated travel item: " + item.getName() + " (ID: " + item.getId() + ") by user: " + username);
    return TravelItemDTO.fromEntity(item);
  }

  @Override
  @Transactional
  public void deleteTravelItem(Long travelItemId, String username) {
    TravelItem item = travelItemRepository.findById(travelItemId)
        .orElseThrow(() -> new IllegalArgumentException("Travel item not found: " + travelItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    travelItemRepository.delete(item);
    logger.info("Deleted travel item: " + item.getName() + " (ID: " + travelItemId + ") by user: " + username);
  }

  @Override
  @Transactional
  public TravelItemDTO updateTravelItemStatus(Long travelItemId, String status, String username) {
    TravelItem item = travelItemRepository.findById(travelItemId)
        .orElseThrow(() -> new IllegalArgumentException("Travel item not found: " + travelItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    item.setStatus(TravelItem.Status.valueOf(status));
    item = travelItemRepository.save(item);
    logger.info("Updated travel item status to " + status + " for item: " + item.getName() + " by user: " + username);
    return TravelItemDTO.fromEntity(item);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TravelMoneyAllocationDTO> getMoneyAllocations(Long travelItemId, String username) {
    TravelItem item = travelItemRepository.findById(travelItemId)
        .orElseThrow(() -> new IllegalArgumentException("Travel item not found: " + travelItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasAccess(rcId, username)) {
      throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
    }

    return item.getMoneyAllocations().stream()
        .map(TravelMoneyAllocationDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public TravelItemDTO updateMoneyAllocations(Long travelItemId, List<TravelMoneyAllocationDTO> allocations, String username) {
    TravelItem item = travelItemRepository.findById(travelItemId)
        .orElseThrow(() -> new IllegalArgumentException("Travel item not found: " + travelItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    item.getMoneyAllocations().clear();
    item = travelItemRepository.save(item);
    saveMoneyAllocations(item, allocations);

    item = travelItemRepository.findById(travelItemId).orElseThrow();
    logger.info("Updated money allocations for travel item: " + item.getName() + " by user: " + username);
    return TravelItemDTO.fromEntity(item);
  }

  private void saveMoneyAllocations(TravelItem item, List<TravelMoneyAllocationDTO> allocDtos) {
    for (TravelMoneyAllocationDTO allocDto : allocDtos) {
      Money money = moneyRepository.findById(allocDto.getMoneyId())
          .orElseThrow(() -> new IllegalArgumentException("Money type not found: " + allocDto.getMoneyId()));

      TravelMoneyAllocation alloc = new TravelMoneyAllocation(
          item, money,
          allocDto.getOmAmount() != null ? allocDto.getOmAmount() : BigDecimal.ZERO
      );
      item.addMoneyAllocation(alloc);
    }
    travelItemRepository.save(item);
  }
}
