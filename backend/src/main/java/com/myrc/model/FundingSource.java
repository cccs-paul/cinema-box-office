/*
 * myRC - Funding Source Enum
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Enum representing the source of a Funding Item.
 */
package com.myrc.model;

/**
 * Enumeration of funding source types.
 * Indicates the origin/source of a funding item.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
public enum FundingSource {
  /**
   * Business Plan - The default funding source.
   * Represents funding allocated through the regular business planning process.
   */
  BUSINESS_PLAN,

  /**
   * On-Ramp funding source.
   * Represents funding received through on-ramp processes.
   */
  ON_RAMP,

  /**
   * Approved Deficit funding source.
   * Represents funding from an approved deficit.
   */
  APPROVED_DEFICIT;

  /**
   * Get the display label for this funding source.
   *
   * @return the human-readable label
   */
  public String getDisplayLabel() {
    switch (this) {
      case BUSINESS_PLAN:
        return "Business Plan";
      case ON_RAMP:
        return "On-Ramp";
      case APPROVED_DEFICIT:
        return "Approved Deficit";
      default:
        return this.name();
    }
  }

  /**
   * Parse a funding source from a string value.
   *
   * @param value the string value (can be enum name or display label)
   * @return the FundingSource, or BUSINESS_PLAN if not found
   */
  public static FundingSource fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return BUSINESS_PLAN;
    }
    
    // Try exact enum name match
    try {
      return FundingSource.valueOf(value.toUpperCase().replace(" ", "_").replace("-", "_"));
    } catch (IllegalArgumentException e) {
      // Try matching display labels
      for (FundingSource source : values()) {
        if (source.getDisplayLabel().equalsIgnoreCase(value)) {
          return source;
        }
      }
      return BUSINESS_PLAN;
    }
  }
}
