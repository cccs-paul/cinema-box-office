/*
 * myRC - Training Item Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.TrainingItemDTO;
import com.myrc.dto.TrainingMoneyAllocationDTO;
import com.myrc.dto.TrainingParticipantDTO;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.TrainingItem;
import com.myrc.model.TrainingMoneyAllocation;
import com.myrc.model.TrainingParticipant;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.TrainingItemRepository;
import com.myrc.repository.TrainingMoneyAllocationRepository;
import com.myrc.repository.TrainingParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service implementation for Training Item management operations.
 * Training items now have 1..n participants with individual costs.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
@Service
public class TrainingItemServiceImpl implements TrainingItemService {

  private static final Logger logger = Logger.getLogger(TrainingItemServiceImpl.class.getName());

  private final TrainingItemRepository trainingItemRepository;
  private final TrainingMoneyAllocationRepository allocationRepository;
  private final TrainingParticipantRepository participantRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final MoneyRepository moneyRepository;
  private final RCPermissionService rcPermissionService;

  public TrainingItemServiceImpl(
      TrainingItemRepository trainingItemRepository,
      TrainingMoneyAllocationRepository allocationRepository,
      TrainingParticipantRepository participantRepository,
      FiscalYearRepository fiscalYearRepository,
      MoneyRepository moneyRepository,
      RCPermissionService rcPermissionService) {
    this.trainingItemRepository = trainingItemRepository;
    this.allocationRepository = allocationRepository;
    this.participantRepository = participantRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.moneyRepository = moneyRepository;
    this.rcPermissionService = rcPermissionService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrainingItemDTO> getTrainingItemsByFiscalYearId(Long fiscalYearId, String username) {
    FiscalYear fy = fiscalYearRepository.findById(fiscalYearId)
        .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + fiscalYearId));

    Long rcId = fy.getResponsibilityCentre().getId();
    if (!rcPermissionService.hasAccess(rcId, username)) {
      throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
    }

    return trainingItemRepository.findByFiscalYearIdOrderByNameAsc(fiscalYearId)
        .stream()
        .map(TrainingItemDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TrainingItemDTO> getTrainingItemById(Long trainingItemId, String username) {
    return trainingItemRepository.findById(trainingItemId)
        .map(item -> {
          Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
          if (!rcPermissionService.hasAccess(rcId, username)) {
            throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
          }
          return TrainingItemDTO.fromEntity(item);
        });
  }

  @Override
  @Transactional
  public TrainingItemDTO createTrainingItem(TrainingItemDTO dto, String username) {
    FiscalYear fy = fiscalYearRepository.findById(dto.getFiscalYearId())
        .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + dto.getFiscalYearId()));

    Long rcId = fy.getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    // Check for duplicate name
    if (trainingItemRepository.existsByNameAndFiscalYearId(dto.getName(), fy.getId())) {
      throw new IllegalArgumentException("A training item with name '" + dto.getName() + "' already exists in this fiscal year");
    }

    TrainingItem item = new TrainingItem();
    item.setName(dto.getName());
    item.setDescription(dto.getDescription());
    item.setProvider(dto.getProvider());
    item.setStatus(dto.getStatus() != null ? TrainingItem.Status.valueOf(dto.getStatus()) : TrainingItem.Status.PLANNED);
    item.setTrainingType(dto.getTrainingType() != null ? TrainingItem.TrainingType.valueOf(dto.getTrainingType()) : TrainingItem.TrainingType.OTHER);
    item.setFormat(dto.getFormat() != null ? TrainingItem.TrainingFormat.valueOf(dto.getFormat()) : TrainingItem.TrainingFormat.IN_PERSON);
    item.setStartDate(dto.getStartDate());
    item.setEndDate(dto.getEndDate());
    item.setLocation(dto.getLocation());
    item.setFiscalYear(fy);

    item = trainingItemRepository.save(item);

    // Save participants
    if (dto.getParticipants() != null && !dto.getParticipants().isEmpty()) {
      saveParticipants(item, dto.getParticipants());
    }

    // Save money allocations
    if (dto.getMoneyAllocations() != null && !dto.getMoneyAllocations().isEmpty()) {
      saveMoneyAllocations(item, dto.getMoneyAllocations());
    }

    logger.info("Created training item: " + item.getName() + " (ID: " + item.getId() + ") by user: " + username);
    return TrainingItemDTO.fromEntity(item);
  }

  @Override
  @Transactional
  public TrainingItemDTO updateTrainingItem(Long trainingItemId, TrainingItemDTO dto, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    // Check for duplicate name (if name is changing)
    if (dto.getName() != null && !dto.getName().equals(item.getName())) {
      if (trainingItemRepository.existsByNameAndFiscalYearId(dto.getName(), item.getFiscalYear().getId())) {
        throw new IllegalArgumentException("A training item with name '" + dto.getName() + "' already exists in this fiscal year");
      }
    }

    if (dto.getName() != null) item.setName(dto.getName());
    if (dto.getDescription() != null) item.setDescription(dto.getDescription());
    if (dto.getProvider() != null) item.setProvider(dto.getProvider());
    if (dto.getStatus() != null) item.setStatus(TrainingItem.Status.valueOf(dto.getStatus()));
    if (dto.getTrainingType() != null) item.setTrainingType(TrainingItem.TrainingType.valueOf(dto.getTrainingType()));
    if (dto.getFormat() != null) item.setFormat(TrainingItem.TrainingFormat.valueOf(dto.getFormat()));
    if (dto.getStartDate() != null) item.setStartDate(dto.getStartDate());
    if (dto.getEndDate() != null) item.setEndDate(dto.getEndDate());
    if (dto.getLocation() != null) item.setLocation(dto.getLocation());

    // Update money allocations if provided
    if (dto.getMoneyAllocations() != null) {
      item.getMoneyAllocations().clear();
      item = trainingItemRepository.save(item);
      saveMoneyAllocations(item, dto.getMoneyAllocations());
    }

    item = trainingItemRepository.save(item);
    logger.info("Updated training item: " + item.getName() + " (ID: " + item.getId() + ") by user: " + username);
    return TrainingItemDTO.fromEntity(item);
  }

  @Override
  @Transactional
  public void deleteTrainingItem(Long trainingItemId, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    trainingItemRepository.delete(item);
    logger.info("Deleted training item: " + item.getName() + " (ID: " + trainingItemId + ") by user: " + username);
  }

  @Override
  @Transactional
  public TrainingItemDTO updateTrainingItemStatus(Long trainingItemId, String status, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    item.setStatus(TrainingItem.Status.valueOf(status));
    item = trainingItemRepository.save(item);
    logger.info("Updated training item status to " + status + " for item: " + item.getName() + " by user: " + username);
    return TrainingItemDTO.fromEntity(item);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrainingMoneyAllocationDTO> getMoneyAllocations(Long trainingItemId, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasAccess(rcId, username)) {
      throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
    }

    return item.getMoneyAllocations().stream()
        .map(TrainingMoneyAllocationDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public TrainingItemDTO updateMoneyAllocations(Long trainingItemId, List<TrainingMoneyAllocationDTO> allocations, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    item.getMoneyAllocations().clear();
    item = trainingItemRepository.save(item);
    saveMoneyAllocations(item, allocations);

    item = trainingItemRepository.findById(trainingItemId).orElseThrow();
    logger.info("Updated money allocations for training item: " + item.getName() + " by user: " + username);
    return TrainingItemDTO.fromEntity(item);
  }

  // ========== Participant management ==========

  @Override
  @Transactional(readOnly = true)
  public List<TrainingParticipantDTO> getParticipants(Long trainingItemId, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasAccess(rcId, username)) {
      throw new IllegalArgumentException("Access denied to responsibility centre: " + rcId);
    }

    return item.getParticipants().stream()
        .map(TrainingParticipantDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public TrainingParticipantDTO addParticipant(Long trainingItemId, TrainingParticipantDTO participantDTO, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    TrainingParticipant participant = new TrainingParticipant();
    participant.setName(participantDTO.getName());
    participant.setEco(participantDTO.getEco());
    participant.setStatus(participantDTO.getStatus() != null ? TrainingParticipant.ParticipantStatus.valueOf(participantDTO.getStatus()) : TrainingParticipant.ParticipantStatus.PLANNED);
    participant.setEstimatedCost(participantDTO.getEstimatedCost());
    participant.setFinalCost(participantDTO.getFinalCost());
    participant.setEstimatedCurrency(participantDTO.getEstimatedCurrency() != null ? Currency.valueOf(participantDTO.getEstimatedCurrency()) : Currency.CAD);
    participant.setEstimatedExchangeRate(participantDTO.getEstimatedExchangeRate());
    participant.setFinalCurrency(participantDTO.getFinalCurrency() != null ? Currency.valueOf(participantDTO.getFinalCurrency()) : Currency.CAD);
    participant.setFinalExchangeRate(participantDTO.getFinalExchangeRate());

    item.addParticipant(participant);
    trainingItemRepository.save(item);

    logger.info("Added participant '" + participant.getName() + "' to training item: " + item.getName() + " by user: " + username);
    return TrainingParticipantDTO.fromEntity(participant);
  }

  @Override
  @Transactional
  public TrainingParticipantDTO updateParticipant(Long trainingItemId, Long participantId, TrainingParticipantDTO participantDTO, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    TrainingParticipant participant = participantRepository.findById(participantId)
        .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));

    if (!participant.getTrainingItem().getId().equals(trainingItemId)) {
      throw new IllegalArgumentException("Participant does not belong to this training item");
    }

    if (participantDTO.getName() != null) participant.setName(participantDTO.getName());
    if (participantDTO.getEco() != null) participant.setEco(participantDTO.getEco());
    if (participantDTO.getStatus() != null) participant.setStatus(TrainingParticipant.ParticipantStatus.valueOf(participantDTO.getStatus()));
    if (participantDTO.getEstimatedCost() != null) participant.setEstimatedCost(participantDTO.getEstimatedCost());
    if (participantDTO.getFinalCost() != null) participant.setFinalCost(participantDTO.getFinalCost());
    if (participantDTO.getEstimatedCurrency() != null) participant.setEstimatedCurrency(Currency.valueOf(participantDTO.getEstimatedCurrency()));
    if (participantDTO.getEstimatedExchangeRate() != null) participant.setEstimatedExchangeRate(participantDTO.getEstimatedExchangeRate());
    if (participantDTO.getFinalCurrency() != null) participant.setFinalCurrency(Currency.valueOf(participantDTO.getFinalCurrency()));
    if (participantDTO.getFinalExchangeRate() != null) participant.setFinalExchangeRate(participantDTO.getFinalExchangeRate());

    participant = participantRepository.save(participant);
    logger.info("Updated participant '" + participant.getName() + "' in training item: " + item.getName() + " by user: " + username);
    return TrainingParticipantDTO.fromEntity(participant);
  }

  @Override
  @Transactional
  public void deleteParticipant(Long trainingItemId, Long participantId, String username) {
    TrainingItem item = trainingItemRepository.findById(trainingItemId)
        .orElseThrow(() -> new IllegalArgumentException("Training item not found: " + trainingItemId));

    Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();
    if (!rcPermissionService.hasWriteAccess(rcId, username)) {
      throw new IllegalArgumentException("Write access denied to responsibility centre: " + rcId);
    }

    TrainingParticipant participant = participantRepository.findById(participantId)
        .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));

    if (!participant.getTrainingItem().getId().equals(trainingItemId)) {
      throw new IllegalArgumentException("Participant does not belong to this training item");
    }

    item.removeParticipant(participant);
    participantRepository.delete(participant);
    logger.info("Deleted participant '" + participant.getName() + "' from training item: " + item.getName() + " by user: " + username);
  }

  // ========== Private helpers ==========

  private void saveParticipants(TrainingItem item, List<TrainingParticipantDTO> participantDtos) {
    for (TrainingParticipantDTO dto : participantDtos) {
      TrainingParticipant participant = new TrainingParticipant();
      participant.setName(dto.getName());
      participant.setEco(dto.getEco());
      participant.setStatus(dto.getStatus() != null ? TrainingParticipant.ParticipantStatus.valueOf(dto.getStatus()) : TrainingParticipant.ParticipantStatus.PLANNED);
      participant.setEstimatedCost(dto.getEstimatedCost());
      participant.setFinalCost(dto.getFinalCost());
      participant.setEstimatedCurrency(dto.getEstimatedCurrency() != null ? Currency.valueOf(dto.getEstimatedCurrency()) : Currency.CAD);
      participant.setEstimatedExchangeRate(dto.getEstimatedExchangeRate());
      participant.setFinalCurrency(dto.getFinalCurrency() != null ? Currency.valueOf(dto.getFinalCurrency()) : Currency.CAD);
      participant.setFinalExchangeRate(dto.getFinalExchangeRate());
      item.addParticipant(participant);
    }
    trainingItemRepository.save(item);
  }

  private void saveMoneyAllocations(TrainingItem item, List<TrainingMoneyAllocationDTO> allocDtos) {
    for (TrainingMoneyAllocationDTO allocDto : allocDtos) {
      Money money = moneyRepository.findById(allocDto.getMoneyId())
          .orElseThrow(() -> new IllegalArgumentException("Money type not found: " + allocDto.getMoneyId()));

      TrainingMoneyAllocation alloc = new TrainingMoneyAllocation(
          item, money,
          allocDto.getOmAmount() != null ? allocDto.getOmAmount() : BigDecimal.ZERO
      );
      item.addMoneyAllocation(alloc);
    }
    trainingItemRepository.save(item);
  }
}
