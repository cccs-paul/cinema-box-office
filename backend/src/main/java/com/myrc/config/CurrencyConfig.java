/*
 * myRC - Currency Configuration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Spring configuration for currency settings.
 * The list of supported currencies is configurable via application properties.
 */
package com.myrc.config;

import com.myrc.model.Currency;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for currency settings.
 * The default currency is CAD (Canadian Dollar), and the supported currencies
 * are configurable via the application.yml file.
 *
 * <p>Example configuration in application.yml:
 * <pre>
 * app:
 *   currency:
 *     default: CAD
 *     supported:
 *       - CAD
 *       - GBP
 *       - AUD
 *       - NZD
 *       - USD
 *       - EUR
 * </pre>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyConfig {

    private static final Logger logger = Logger.getLogger(CurrencyConfig.class.getName());

    /**
     * The default currency code. Defaults to CAD if not configured.
     */
    private String defaultCurrency = "CAD";

    /**
     * List of supported currency codes. If not configured, defaults to all currencies.
     */
    private List<String> supported = new ArrayList<>();

    /**
     * Validates the configuration after properties are set.
     */
    @PostConstruct
    public void validate() {
        // If no supported currencies are configured, use all available currencies
        if (supported.isEmpty()) {
            for (Currency currency : Currency.values()) {
                supported.add(currency.getCode());
            }
            logger.info("No currencies configured, using all available currencies: " + supported);
        } else {
            // Validate that all configured currencies are valid
            List<String> validCurrencies = new ArrayList<>();
            for (String code : supported) {
                if (Currency.isValidCode(code)) {
                    validCurrencies.add(code.toUpperCase());
                } else {
                    logger.warning("Invalid currency code configured: " + code + " (ignored)");
                }
            }
            supported = validCurrencies;
        }

        // Validate default currency
        if (!Currency.isValidCode(defaultCurrency)) {
            logger.warning("Invalid default currency configured: " + defaultCurrency + ", using CAD");
            defaultCurrency = "CAD";
        }

        // Ensure default currency is in the supported list
        if (!supported.contains(defaultCurrency.toUpperCase())) {
            supported.add(0, defaultCurrency.toUpperCase());
            logger.info("Added default currency " + defaultCurrency + " to supported list");
        }

        logger.info("Currency configuration initialized: default=" + defaultCurrency + 
                    ", supported=" + supported);
    }

    /**
     * Gets the default currency code.
     *
     * @return the default currency code
     */
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    /**
     * Sets the default currency code.
     *
     * @param defaultCurrency the default currency code
     */
    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    /**
     * Alias for getDefaultCurrency() to match YAML property name.
     *
     * @return the default currency code
     */
    public String getDefault() {
        return defaultCurrency;
    }

    /**
     * Alias for setDefaultCurrency() to match YAML property name.
     *
     * @param defaultCurrency the default currency code
     */
    public void setDefault(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    /**
     * Gets the list of supported currency codes.
     *
     * @return unmodifiable list of supported currency codes
     */
    public List<String> getSupported() {
        return Collections.unmodifiableList(supported);
    }

    /**
     * Sets the list of supported currency codes.
     *
     * @param supported the list of supported currency codes
     */
    public void setSupported(List<String> supported) {
        this.supported = supported != null ? new ArrayList<>(supported) : new ArrayList<>();
    }

    /**
     * Gets the default Currency enum value.
     *
     * @return the default Currency
     */
    public Currency getDefaultCurrencyEnum() {
        return Currency.fromCode(defaultCurrency);
    }

    /**
     * Gets the list of supported Currency enum values.
     *
     * @return list of supported Currency enums
     */
    public List<Currency> getSupportedCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        for (String code : supported) {
            Currency currency = Currency.fromCode(code);
            if (currency != null) {
                currencies.add(currency);
            }
        }
        return currencies;
    }

    /**
     * Checks if a currency code is supported.
     *
     * @param code the currency code to check
     * @return true if the currency is supported, false otherwise
     */
    public boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        return supported.contains(code.toUpperCase());
    }
}
