/*
 * myRC - Add Currency to Procurement Items
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Adds currency and exchange_rate columns to the procurement_items table.
 */

-- Add currency column (defaults to CAD)
ALTER TABLE procurement_items
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'CAD';

-- Add exchange_rate column for currency conversion
ALTER TABLE procurement_items
    ADD COLUMN exchange_rate DECIMAL(10, 6);

-- Add comment for documentation
COMMENT ON COLUMN procurement_items.currency IS 'Currency code for the procurement item (defaults to CAD)';
COMMENT ON COLUMN procurement_items.exchange_rate IS 'Exchange rate to convert to CAD (1.0 means 1 unit equals 1 CAD)';
