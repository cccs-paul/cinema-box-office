/*
 * myRC - Travel Item Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.TravelItemDTO;
import com.myrc.dto.TravelMoneyAllocationDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Travel Item management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
public interface TravelItemService {

  List<TravelItemDTO> getTravelItemsByFiscalYearId(Long fiscalYearId, String username);

  Optional<TravelItemDTO> getTravelItemById(Long travelItemId, String username);

  TravelItemDTO createTravelItem(TravelItemDTO travelItemDTO, String username);

  TravelItemDTO updateTravelItem(Long travelItemId, TravelItemDTO travelItemDTO, String username);

  void deleteTravelItem(Long travelItemId, String username);

  TravelItemDTO updateTravelItemStatus(Long travelItemId, String status, String username);

  List<TravelMoneyAllocationDTO> getMoneyAllocations(Long travelItemId, String username);

  TravelItemDTO updateMoneyAllocations(Long travelItemId, List<TravelMoneyAllocationDTO> allocations, String username);
}
