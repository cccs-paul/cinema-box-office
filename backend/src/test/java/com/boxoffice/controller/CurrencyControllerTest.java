/*
 * myRC - Currency Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.boxoffice.config.CurrencyConfig;
import com.boxoffice.dto.CurrencyDTO;
import com.boxoffice.model.Currency;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for CurrencyController.
 * Uses manually constructed CurrencyConfig instances for testing.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@DisplayName("CurrencyController Tests")
class CurrencyControllerTest {

    private CurrencyConfig currencyConfig;
    private CurrencyController controller;

    /**
     * Creates a CurrencyConfig with the specified settings.
     *
     * @param defaultCurrency the default currency code
     * @param supported the list of supported currency codes
     * @return a configured CurrencyConfig instance
     */
    private CurrencyConfig createConfig(String defaultCurrency, List<String> supported) {
        CurrencyConfig config = new CurrencyConfig();
        config.setDefaultCurrency(defaultCurrency);
        config.setSupported(supported);
        config.validate();
        return config;
    }

    @BeforeEach
    void setUp() {
        currencyConfig = createConfig("CAD", Arrays.asList("CAD", "GBP", "USD"));
        controller = new CurrencyController(currencyConfig);
    }

    @Test
    @DisplayName("Should create controller successfully")
    void testControllerCreation() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("getSupportedCurrencies - Returns all supported currencies")
    void getSupportedCurrencies_ReturnsAllCurrencies() {
        ResponseEntity<List<CurrencyDTO>> response = controller.getSupportedCurrencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        
        // Verify CAD is marked as default
        CurrencyDTO cadDto = response.getBody().stream()
            .filter(c -> c.getCode().equals("CAD"))
            .findFirst()
            .orElse(null);
        assertNotNull(cadDto);
        assertTrue(cadDto.isDefault());
    }

    @Test
    @DisplayName("getSupportedCurrencies - Returns currencies in correct order")
    void getSupportedCurrencies_ReturnsCurrenciesInOrder() {
        currencyConfig = createConfig("CAD", Arrays.asList("CAD", "GBP", "EUR"));
        controller = new CurrencyController(currencyConfig);

        ResponseEntity<List<CurrencyDTO>> response = controller.getSupportedCurrencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CurrencyDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals("CAD", body.get(0).getCode());
        assertEquals("GBP", body.get(1).getCode());
        assertEquals("EUR", body.get(2).getCode());
    }

    @Test
    @DisplayName("getDefaultCurrency - Returns default currency")
    void getDefaultCurrency_ReturnsDefaultCurrency() {
        ResponseEntity<CurrencyDTO> response = controller.getDefaultCurrency();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CAD", response.getBody().getCode());
        assertEquals("Canadian Dollar", response.getBody().getName());
        assertEquals("$", response.getBody().getSymbol());
        assertTrue(response.getBody().isDefault());
    }

    @Test
    @DisplayName("getSupportedCurrencies - Non-default currencies are not marked as default")
    void getSupportedCurrencies_NonDefaultCurrenciesNotMarkedAsDefault() {
        ResponseEntity<List<CurrencyDTO>> response = controller.getSupportedCurrencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CurrencyDTO> body = response.getBody();
        assertNotNull(body);

        // GBP should not be default
        CurrencyDTO gbpDto = body.stream()
            .filter(c -> c.getCode().equals("GBP"))
            .findFirst()
            .orElse(null);
        assertNotNull(gbpDto);
        assertFalse(gbpDto.isDefault());

        // USD should not be default
        CurrencyDTO usdDto = body.stream()
            .filter(c -> c.getCode().equals("USD"))
            .findFirst()
            .orElse(null);
        assertNotNull(usdDto);
        assertFalse(usdDto.isDefault());
    }

    @Test
    @DisplayName("getSupportedCurrencies - With minimal config uses all currencies")
    void getSupportedCurrencies_WithMinimalConfigUsesAllCurrencies() {
        // When no supported currencies are specified, all currencies should be used
        currencyConfig = createConfig("CAD", Arrays.asList());
        controller = new CurrencyController(currencyConfig);

        ResponseEntity<List<CurrencyDTO>> response = controller.getSupportedCurrencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should have all Currency enum values
        assertEquals(Currency.values().length, response.getBody().size());
    }

    @Test
    @DisplayName("CurrencyDTO fromCurrency - Creates DTO from Currency enum")
    void currencyDto_fromCurrency_CreatesCorrectDTO() {
        CurrencyDTO dto = CurrencyDTO.fromCurrency(Currency.EUR, false);

        assertNotNull(dto);
        assertEquals("EUR", dto.getCode());
        assertEquals("Euro", dto.getName());
        assertEquals("€", dto.getSymbol());
        assertFalse(dto.isDefault());
    }

    @Test
    @DisplayName("CurrencyDTO fromCurrency - Returns null for null currency")
    void currencyDto_fromCurrency_ReturnsNullForNullCurrency() {
        CurrencyDTO dto = CurrencyDTO.fromCurrency(null, true);
        assertEquals(null, dto);
    }
}
