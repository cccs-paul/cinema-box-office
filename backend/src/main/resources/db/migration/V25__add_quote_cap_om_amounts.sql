-- V25__add_quote_cap_om_amounts.sql
-- Migration to add CAP/OM amounts to procurement quotes
-- Author: myRC Team
-- Version: 1.0.0
-- Date: 2026-02-02

-- Add CAP amount field (capital funding)
ALTER TABLE procurement_quotes ADD COLUMN amount_cap DECIMAL(15, 2);

-- Add OM amount field (operations & maintenance funding)
ALTER TABLE procurement_quotes ADD COLUMN amount_om DECIMAL(15, 2);

-- Add exchange rate field for currency conversion
ALTER TABLE procurement_quotes ADD COLUMN exchange_rate DECIMAL(10, 6);

-- Add CAP amount in CAD (for non-CAD currencies)
ALTER TABLE procurement_quotes ADD COLUMN amount_cap_cad DECIMAL(15, 2);

-- Add OM amount in CAD (for non-CAD currencies)
ALTER TABLE procurement_quotes ADD COLUMN amount_om_cad DECIMAL(15, 2);

-- Comments
COMMENT ON COLUMN procurement_quotes.amount_cap IS 'Quote amount for CAP (capital) funding in the specified currency';
COMMENT ON COLUMN procurement_quotes.amount_om IS 'Quote amount for OM (operations & maintenance) funding in the specified currency';
COMMENT ON COLUMN procurement_quotes.exchange_rate IS 'Exchange rate to CAD (only required when currency is not CAD)';
COMMENT ON COLUMN procurement_quotes.amount_cap_cad IS 'CAP amount converted to CAD (only required when currency is not CAD)';
COMMENT ON COLUMN procurement_quotes.amount_om_cad IS 'OM amount converted to CAD (only required when currency is not CAD)';
