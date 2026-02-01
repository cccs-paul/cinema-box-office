/*
 * myRC - Currency Enum Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Currency enum.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@DisplayName("Currency Enum Tests")
class CurrencyTest {

    @Test
    @DisplayName("Should have all expected currencies")
    void shouldHaveAllExpectedCurrencies() {
        Currency[] currencies = Currency.values();
        assertEquals(6, currencies.length);
    }

    @Test
    @DisplayName("CAD should have correct properties")
    void cadShouldHaveCorrectProperties() {
        Currency cad = Currency.CAD;
        assertEquals("CAD", cad.getCode());
        assertEquals("Canadian Dollar", cad.getName());
        assertEquals("$", cad.getSymbol());
    }

    @Test
    @DisplayName("GBP should have correct properties")
    void gbpShouldHaveCorrectProperties() {
        Currency gbp = Currency.GBP;
        assertEquals("GBP", gbp.getCode());
        assertEquals("Pound Sterling", gbp.getName());
        assertEquals("£", gbp.getSymbol());
    }

    @Test
    @DisplayName("EUR should have correct properties")
    void eurShouldHaveCorrectProperties() {
        Currency eur = Currency.EUR;
        assertEquals("EUR", eur.getCode());
        assertEquals("Euro", eur.getName());
        assertEquals("€", eur.getSymbol());
    }

    @Test
    @DisplayName("USD should have correct properties")
    void usdShouldHaveCorrectProperties() {
        Currency usd = Currency.USD;
        assertEquals("USD", usd.getCode());
        assertEquals("US Dollar", usd.getName());
        assertEquals("$", usd.getSymbol());
    }

    @Test
    @DisplayName("AUD should have correct properties")
    void audShouldHaveCorrectProperties() {
        Currency aud = Currency.AUD;
        assertEquals("AUD", aud.getCode());
        assertEquals("Australian Dollar", aud.getName());
        assertEquals("A$", aud.getSymbol());
    }

    @Test
    @DisplayName("NZD should have correct properties")
    void nzdShouldHaveCorrectProperties() {
        Currency nzd = Currency.NZD;
        assertEquals("NZD", nzd.getCode());
        assertEquals("New Zealand Dollar", nzd.getName());
        assertEquals("NZ$", nzd.getSymbol());
    }

    @Test
    @DisplayName("fromCode should return correct currency")
    void fromCodeShouldReturnCorrectCurrency() {
        assertEquals(Currency.CAD, Currency.fromCode("CAD"));
        assertEquals(Currency.GBP, Currency.fromCode("GBP"));
        assertEquals(Currency.EUR, Currency.fromCode("EUR"));
        assertEquals(Currency.USD, Currency.fromCode("USD"));
        assertEquals(Currency.AUD, Currency.fromCode("AUD"));
        assertEquals(Currency.NZD, Currency.fromCode("NZD"));
    }

    @Test
    @DisplayName("fromCode should be case insensitive")
    void fromCodeShouldBeCaseInsensitive() {
        assertEquals(Currency.CAD, Currency.fromCode("cad"));
        assertEquals(Currency.GBP, Currency.fromCode("Gbp"));
        assertEquals(Currency.EUR, Currency.fromCode("eur"));
    }

    @Test
    @DisplayName("fromCode should return null for invalid code")
    void fromCodeShouldReturnNullForInvalidCode() {
        assertNull(Currency.fromCode("INVALID"));
        assertNull(Currency.fromCode(""));
        assertNull(Currency.fromCode(null));
    }

    @Test
    @DisplayName("isValidCode should return true for valid codes")
    void isValidCodeShouldReturnTrueForValidCodes() {
        assertTrue(Currency.isValidCode("CAD"));
        assertTrue(Currency.isValidCode("GBP"));
        assertTrue(Currency.isValidCode("EUR"));
        assertTrue(Currency.isValidCode("USD"));
        assertTrue(Currency.isValidCode("AUD"));
        assertTrue(Currency.isValidCode("NZD"));
    }

    @Test
    @DisplayName("isValidCode should be case insensitive")
    void isValidCodeShouldBeCaseInsensitive() {
        assertTrue(Currency.isValidCode("cad"));
        assertTrue(Currency.isValidCode("Cad"));
        assertTrue(Currency.isValidCode("CAD"));
    }

    @Test
    @DisplayName("isValidCode should return false for invalid codes")
    void isValidCodeShouldReturnFalseForInvalidCodes() {
        assertFalse(Currency.isValidCode("INVALID"));
        assertFalse(Currency.isValidCode(""));
        assertFalse(Currency.isValidCode(null));
        assertFalse(Currency.isValidCode("JPY"));
    }
}
