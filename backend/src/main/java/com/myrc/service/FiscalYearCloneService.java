/*
 * myRC - Fiscal Year Clone Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.model.FiscalYear;

/**
 * Service interface for deep-cloning fiscal years.
 * Used by both RC cloning (clones all FYs to a new RC) and FY cloning
 * (clones a single FY within the same RC).
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
public interface FiscalYearCloneService {

  /**
   * Deep-clone a fiscal year to a target RC.
   * Copies all child data: Money types, Categories, Spending Categories,
   * Funding Items (with Money Allocations), Spending Items (with Money Allocations
   * and Spending Events), Procurement Items (with Quotes, Quote Files,
   * Events, and Event Files).
   *
   * <p>Remaps all internal references (Money IDs, Category IDs, ProcurementItem IDs)
   * from the source to the cloned entities.</p>
   *
   * @param sourceFY the source fiscal year to clone
   * @param targetFYName the name for the cloned fiscal year
   * @param targetRC the target responsibility centre (may be the same RC for FY clone)
   * @return the saved cloned fiscal year with all child data
   */
  FiscalYear deepCloneFiscalYear(FiscalYear sourceFY, String targetFYName,
      com.myrc.model.ResponsibilityCentre targetRC);
}
