/*
 * myRC - Travel Traveller Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.TravelTraveller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TravelTraveller entities.
 */
@Repository
public interface TravelTravellerRepository extends JpaRepository<TravelTraveller, Long> {

  List<TravelTraveller> findByTravelItemId(Long travelItemId);

  void deleteByTravelItemId(Long travelItemId);
}
