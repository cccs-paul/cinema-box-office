/*
 * myRC - Travel Money Allocation Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.TravelMoneyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for TravelMoneyAllocation entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Repository
public interface TravelMoneyAllocationRepository extends JpaRepository<TravelMoneyAllocation, Long> {

  List<TravelMoneyAllocation> findByTravelItemId(Long travelItemId);

  void deleteByTravelItemId(Long travelItemId);
}
