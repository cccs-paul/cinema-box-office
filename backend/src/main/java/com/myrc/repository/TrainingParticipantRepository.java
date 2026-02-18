/*
 * myRC - Training Participant Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.TrainingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TrainingParticipant entities.
 */
@Repository
public interface TrainingParticipantRepository extends JpaRepository<TrainingParticipant, Long> {

  List<TrainingParticipant> findByTrainingItemId(Long trainingItemId);

  void deleteByTrainingItemId(Long trainingItemId);
}
