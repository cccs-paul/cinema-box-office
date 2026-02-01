/*
 * myRC - Currency REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for currency reference data.
 * Provides read-only access to the list of supported currencies.
 */
package com.myrc.controller;

import com.myrc.config.CurrencyConfig;
import com.myrc.dto.CurrencyDTO;
import com.myrc.model.Currency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for currency reference data.
 * Provides read-only endpoints to retrieve the list of supported currencies
 * configured in the application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@RestController
@RequestMapping("/currencies")
@Tag(name = "Currency Reference", description = "APIs for retrieving supported currencies")
public class CurrencyController {

    private static final Logger logger = Logger.getLogger(CurrencyController.class.getName());
    private final CurrencyConfig currencyConfig;

    /**
     * Constructs a CurrencyController with the required dependencies.
     *
     * @param currencyConfig the currency configuration
     */
    public CurrencyController(CurrencyConfig currencyConfig) {
        this.currencyConfig = currencyConfig;
    }

    /**
     * Get all supported currencies.
     *
     * @return list of supported currencies
     */
    @GetMapping
    @Operation(summary = "Get all supported currencies",
        description = "Retrieves the list of all currencies supported by the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Currencies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyDTO>> getSupportedCurrencies() {
        logger.info("GET /currencies - Fetching supported currencies");

        String defaultCurrencyCode = currencyConfig.getDefaultCurrency();
        List<CurrencyDTO> currencies = currencyConfig.getSupportedCurrencies()
            .stream()
            .map(currency -> CurrencyDTO.fromCurrency(currency, 
                currency.getCode().equalsIgnoreCase(defaultCurrencyCode)))
            .collect(Collectors.toList());

        logger.info("Returning " + currencies.size() + " supported currencies");
        return ResponseEntity.ok(currencies);
    }

    /**
     * Get the default currency.
     *
     * @return the default currency
     */
    @GetMapping("/default")
    @Operation(summary = "Get the default currency",
        description = "Retrieves the default currency configured for the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Default currency retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyDTO> getDefaultCurrency() {
        logger.info("GET /currencies/default - Fetching default currency");

        Currency defaultCurrency = currencyConfig.getDefaultCurrencyEnum();
        CurrencyDTO dto = CurrencyDTO.fromCurrency(defaultCurrency, true);

        logger.info("Returning default currency: " + defaultCurrency.getCode());
        return ResponseEntity.ok(dto);
    }
}
