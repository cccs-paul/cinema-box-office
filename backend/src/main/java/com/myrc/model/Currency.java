/*
 * myRC - Currency Enumeration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Enumeration representing supported currencies in the application.
 * The default currency is Canadian Dollar (CAD).
 */
package com.myrc.model;

/**
 * Enumeration of supported currencies in the application.
 * Each currency has a code, name, and symbol for display purposes.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
public enum Currency {

    /**
     * Canadian Dollar - the default currency.
     */
    CAD("CAD", "Canadian Dollar", "$"),

    /**
     * Pound Sterling.
     */
    GBP("GBP", "Pound Sterling", "£"),

    /**
     * Australian Dollar.
     */
    AUD("AUD", "Australian Dollar", "A$"),

    /**
     * New Zealand Dollar.
     */
    NZD("NZD", "New Zealand Dollar", "NZ$"),

    /**
     * United States Dollar.
     */
    USD("USD", "US Dollar", "$"),

    /**
     * Euro.
     */
    EUR("EUR", "Euro", "€");

    private final String code;
    private final String name;
    private final String symbol;

    /**
     * Constructs a Currency enum constant.
     *
     * @param code   the ISO 4217 currency code
     * @param name   the full name of the currency
     * @param symbol the currency symbol
     */
    Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }

    /**
     * Gets the ISO 4217 currency code.
     *
     * @return the currency code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the full name of the currency.
     *
     * @return the currency name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the currency symbol.
     *
     * @return the currency symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Finds a Currency by its code.
     *
     * @param code the currency code to search for
     * @return the Currency if found, or null if not found
     */
    public static Currency fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Currency currency : values()) {
            if (currency.code.equalsIgnoreCase(code)) {
                return currency;
            }
        }
        return null;
    }

    /**
     * Checks if the given code represents a valid currency.
     *
     * @param code the currency code to validate
     * @return true if the code is valid, false otherwise
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}
