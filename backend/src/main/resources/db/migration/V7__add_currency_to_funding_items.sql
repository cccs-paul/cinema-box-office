-- myRC - Add Currency Fields to Funding Items
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-23
-- Version: 1.0.0
--
-- Description:
-- Adds currency and exchange rate fields to the funding_items table.
-- The currency defaults to CAD (Canadian Dollar).
-- Exchange rate is required for non-CAD currencies.

-- Add currency column with default value of 'CAD'
ALTER TABLE funding_items 
ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'CAD';

-- Add exchange_rate column (required when currency is not CAD)
ALTER TABLE funding_items 
ADD COLUMN exchange_rate DECIMAL(15, 6);

-- Add constraint to validate currency values
ALTER TABLE funding_items
ADD CONSTRAINT valid_currency CHECK (currency IN ('CAD', 'GBP', 'AUD', 'NZD', 'USD', 'EUR'));

-- Add constraint to ensure exchange_rate is positive when provided
ALTER TABLE funding_items
ADD CONSTRAINT positive_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0);

-- Add index on currency for faster filtering
CREATE INDEX idx_funding_items_currency ON funding_items(currency);

-- Add comments for documentation
COMMENT ON COLUMN funding_items.currency IS 'Currency code for the funding item (ISO 4217). Defaults to CAD.';
COMMENT ON COLUMN funding_items.exchange_rate IS 'Exchange rate to CAD. Required when currency is not CAD. Value of 1.5 means 1 unit of currency = 1.5 CAD.';
