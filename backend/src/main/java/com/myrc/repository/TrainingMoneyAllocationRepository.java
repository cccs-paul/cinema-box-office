/*
 * myRC - Training Money Allocation Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.TrainingMoneyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for TrainingMoneyAllocation entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Repository
public interface TrainingMoneyAllocationRepository extends JpaRepository<TrainingMoneyAllocation, Long> {

  List<TrainingMoneyAllocation> findByTrainingItemId(Long trainingItemId);

  void deleteByTrainingItemId(Long trainingItemId);
}
