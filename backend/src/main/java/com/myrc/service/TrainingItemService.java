/*
 * myRC - Training Item Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.TrainingItemDTO;
import com.myrc.dto.TrainingMoneyAllocationDTO;
import com.myrc.dto.TrainingParticipantDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Training Item management operations.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-02-16
 */
public interface TrainingItemService {

  List<TrainingItemDTO> getTrainingItemsByFiscalYearId(Long fiscalYearId, String username);

  Optional<TrainingItemDTO> getTrainingItemById(Long trainingItemId, String username);

  TrainingItemDTO createTrainingItem(TrainingItemDTO trainingItemDTO, String username);

  TrainingItemDTO updateTrainingItem(Long trainingItemId, TrainingItemDTO trainingItemDTO, String username);

  void deleteTrainingItem(Long trainingItemId, String username);

  TrainingItemDTO updateTrainingItemStatus(Long trainingItemId, String status, String username);

  List<TrainingMoneyAllocationDTO> getMoneyAllocations(Long trainingItemId, String username);

  TrainingItemDTO updateMoneyAllocations(Long trainingItemId, List<TrainingMoneyAllocationDTO> allocations, String username);

  // Participant management
  List<TrainingParticipantDTO> getParticipants(Long trainingItemId, String username);

  TrainingParticipantDTO addParticipant(Long trainingItemId, TrainingParticipantDTO participantDTO, String username);

  TrainingParticipantDTO updateParticipant(Long trainingItemId, Long participantId, TrainingParticipantDTO participantDTO, String username);

  void deleteParticipant(Long trainingItemId, Long participantId, String username);
}
