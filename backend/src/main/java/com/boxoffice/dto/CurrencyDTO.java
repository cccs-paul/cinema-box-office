/*
 * myRC - Currency DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for currency information.
 */
package com.boxoffice.dto;

import com.boxoffice.model.Currency;

/**
 * Data Transfer Object for Currency.
 * Used for transferring currency information to the frontend.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
public class CurrencyDTO {

    private String code;
    private String name;
    private String symbol;
    private boolean isDefault;

    /**
     * Default constructor.
     */
    public CurrencyDTO() {}

    /**
     * Constructs a CurrencyDTO with all fields.
     *
     * @param code      the currency code
     * @param name      the currency name
     * @param symbol    the currency symbol
     * @param isDefault whether this is the default currency
     */
    public CurrencyDTO(String code, String name, String symbol, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.isDefault = isDefault;
    }

    /**
     * Creates a CurrencyDTO from a Currency enum.
     *
     * @param currency  the Currency enum
     * @param isDefault whether this is the default currency
     * @return the CurrencyDTO
     */
    public static CurrencyDTO fromCurrency(Currency currency, boolean isDefault) {
        if (currency == null) {
            return null;
        }
        return new CurrencyDTO(
            currency.getCode(),
            currency.getName(),
            currency.getSymbol(),
            isDefault
        );
    }

    /**
     * Gets the currency code.
     *
     * @return the currency code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the currency code.
     *
     * @param code the currency code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the currency name.
     *
     * @return the currency name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the currency name.
     *
     * @param name the currency name
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the currency symbol.
     *
     * @param symbol the currency symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Checks if this is the default currency.
     *
     * @return true if this is the default currency
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Sets whether this is the default currency.
     *
     * @param isDefault whether this is the default currency
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "CurrencyDTO{" +
               "code='" + code + '\'' +
               ", name='" + name + '\'' +
               ", symbol='" + symbol + '\'' +
               ", isDefault=" + isDefault +
               '}';
    }
}
