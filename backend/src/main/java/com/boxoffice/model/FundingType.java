/*
 * myRC - Funding Type Enum
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Enum representing the allowed funding types for a category.
 * Categories can support CAP only, OM only, or both.
 */
package com.boxoffice.model;

/**
 * Enum representing the allowed funding types for a category.
 * Determines which money allocation fields (CAP, OM, or both) are available
 * when entering funding, spending, or procurement items.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
public enum FundingType {
    /**
     * Capital only - only CAP amounts can be entered.
     */
    CAP_ONLY,

    /**
     * Operations & Maintenance only - only OM amounts can be entered.
     */
    OM_ONLY,

    /**
     * Both CAP and OM amounts can be entered.
     * This is the default for most categories.
     */
    BOTH;

    /**
     * Check if this funding type allows CAP amounts.
     *
     * @return true if CAP amounts are allowed
     */
    public boolean allowsCap() {
        return this == CAP_ONLY || this == BOTH;
    }

    /**
     * Check if this funding type allows OM amounts.
     *
     * @return true if OM amounts are allowed
     */
    public boolean allowsOm() {
        return this == OM_ONLY || this == BOTH;
    }
}
