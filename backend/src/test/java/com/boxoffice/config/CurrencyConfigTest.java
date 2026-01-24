/*
 * myRC - Currency Configuration Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.boxoffice.model.Currency;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CurrencyConfig.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@DisplayName("CurrencyConfig Tests")
class CurrencyConfigTest {

    private CurrencyConfig config;

    @BeforeEach
    void setUp() {
        config = new CurrencyConfig();
    }

    @Test
    @DisplayName("Default currency should be CAD")
    void defaultCurrencyShouldBeCAD() {
        assertEquals("CAD", config.getDefaultCurrency());
    }

    @Test
    @DisplayName("validate() - Should use all currencies when none configured")
    void validate_ShouldUseAllCurrenciesWhenNoneConfigured() {
        config.validate();

        List<String> supported = config.getSupported();
        assertNotNull(supported);
        assertEquals(6, supported.size());
        assertTrue(supported.contains("CAD"));
        assertTrue(supported.contains("GBP"));
        assertTrue(supported.contains("AUD"));
        assertTrue(supported.contains("NZD"));
        assertTrue(supported.contains("USD"));
        assertTrue(supported.contains("EUR"));
    }

    @Test
    @DisplayName("validate() - Should keep only valid currencies from config")
    void validate_ShouldKeepOnlyValidCurrencies() {
        config.setSupported(Arrays.asList("CAD", "INVALID", "GBP", "XYZ", "USD"));
        config.validate();

        List<String> supported = config.getSupported();
        assertEquals(3, supported.size());
        assertTrue(supported.contains("CAD"));
        assertTrue(supported.contains("GBP"));
        assertTrue(supported.contains("USD"));
        assertFalse(supported.contains("INVALID"));
        assertFalse(supported.contains("XYZ"));
    }

    @Test
    @DisplayName("validate() - Should add default currency if not in supported list")
    void validate_ShouldAddDefaultCurrencyIfNotInSupportedList() {
        config.setSupported(Arrays.asList("GBP", "USD"));
        config.setDefaultCurrency("CAD");
        config.validate();

        List<String> supported = config.getSupported();
        assertTrue(supported.contains("CAD"));
        assertEquals("CAD", supported.get(0)); // Should be first
    }

    @Test
    @DisplayName("validate() - Should reset invalid default to CAD")
    void validate_ShouldResetInvalidDefaultToCAD() {
        config.setDefaultCurrency("INVALID");
        config.validate();

        assertEquals("CAD", config.getDefaultCurrency());
    }

    @Test
    @DisplayName("getDefaultCurrencyEnum() - Returns correct Currency enum")
    void getDefaultCurrencyEnum_ReturnsCorrectEnum() {
        config.validate();

        Currency defaultCurrency = config.getDefaultCurrencyEnum();
        assertNotNull(defaultCurrency);
        assertEquals(Currency.CAD, defaultCurrency);
    }

    @Test
    @DisplayName("getSupportedCurrencies() - Returns list of Currency enums")
    void getSupportedCurrencies_ReturnsListOfEnums() {
        config.setSupported(Arrays.asList("CAD", "GBP", "EUR"));
        config.validate();

        List<Currency> currencies = config.getSupportedCurrencies();
        assertNotNull(currencies);
        assertEquals(3, currencies.size());
        assertTrue(currencies.contains(Currency.CAD));
        assertTrue(currencies.contains(Currency.GBP));
        assertTrue(currencies.contains(Currency.EUR));
    }

    @Test
    @DisplayName("isSupported() - Returns true for supported currencies")
    void isSupported_ReturnsTrueForSupportedCurrencies() {
        config.setSupported(Arrays.asList("CAD", "GBP"));
        config.validate();

        assertTrue(config.isSupported("CAD"));
        assertTrue(config.isSupported("GBP"));
        assertTrue(config.isSupported("cad")); // Case insensitive
    }

    @Test
    @DisplayName("isSupported() - Returns false for unsupported currencies")
    void isSupported_ReturnsFalseForUnsupportedCurrencies() {
        config.setSupported(Arrays.asList("CAD", "GBP"));
        config.validate();

        assertFalse(config.isSupported("EUR"));
        assertFalse(config.isSupported("USD"));
        assertFalse(config.isSupported(null));
        assertFalse(config.isSupported(""));
    }

    @Test
    @DisplayName("setDefault() - Alias for setDefaultCurrency()")
    void setDefault_AliasForSetDefaultCurrency() {
        config.setDefault("GBP");
        assertEquals("GBP", config.getDefault());
        assertEquals("GBP", config.getDefaultCurrency());
    }

    @Test
    @DisplayName("getSupported() - Returns unmodifiable list")
    void getSupported_ReturnsUnmodifiableList() {
        config.setSupported(Arrays.asList("CAD"));
        config.validate();

        List<String> supported = config.getSupported();
        assertNotNull(supported);

        // Attempting to modify should throw an exception
        try {
            supported.add("GBP");
            // If we get here without an exception, fail the test
            assertTrue(false, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("setSupported() - Handles null input")
    void setSupported_HandlesNullInput() {
        config.setSupported(null);
        config.validate();

        // Should fall back to all currencies
        List<String> supported = config.getSupported();
        assertNotNull(supported);
        assertEquals(6, supported.size());
    }

    @Test
    @DisplayName("validate() - Uppercases all currency codes")
    void validate_UppercasesAllCurrencyCodes() {
        config.setSupported(Arrays.asList("cad", "gbp", "usd"));
        config.validate();

        List<String> supported = config.getSupported();
        assertTrue(supported.contains("CAD"));
        assertTrue(supported.contains("GBP"));
        assertTrue(supported.contains("USD"));
        assertFalse(supported.contains("cad"));
        assertFalse(supported.contains("gbp"));
        assertFalse(supported.contains("usd"));
    }
}
