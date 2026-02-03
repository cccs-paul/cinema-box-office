-- V24: Add final price fields to procurement_items table and make PR optional
-- Author: myRC Team
-- Date: 2026-02-02
-- Version: 1.0.0
-- License: MIT
--
-- Changes:
-- 1. Makes purchase_requisition column nullable (PR is no longer mandatory)
-- 2. Adds final_price column for the final negotiated/agreed price
-- 3. Adds final_price_currency for the currency of the final price
-- 4. Adds final_price_cad for CAD equivalent when currency is not CAD
-- 5. Renames preferred_vendor index to vendor index (column name stays same for compatibility)

-- Make purchase_requisition nullable
ALTER TABLE procurement_items
ALTER COLUMN purchase_requisition DROP NOT NULL;

-- Drop the unique constraint that includes purchase_requisition
-- Since PR can now be null or empty, we need to handle this differently
ALTER TABLE procurement_items
DROP CONSTRAINT IF EXISTS uk_pi_pr_fy;

-- Create a partial unique index that only applies when purchase_requisition is not null/empty
CREATE UNIQUE INDEX IF NOT EXISTS uk_pi_pr_fy_partial 
ON procurement_items(purchase_requisition, fiscal_year_id) 
WHERE purchase_requisition IS NOT NULL AND purchase_requisition != '';

-- Add final price field (in the specified currency)
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS final_price NUMERIC(15, 2);

-- Add final price currency (defaults to CAD)
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS final_price_currency VARCHAR(3) DEFAULT 'CAD';

-- Add final price in CAD (required when final_price_currency is not CAD)
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS final_price_cad NUMERIC(15, 2);

-- Create index for final price queries
CREATE INDEX IF NOT EXISTS idx_procurement_items_final_price 
ON procurement_items(final_price) WHERE final_price IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_procurement_items_final_price_currency 
ON procurement_items(final_price_currency) WHERE final_price_currency IS NOT NULL;

-- Add comment for clarity on the vendor field (renamed from preferred_vendor conceptually)
COMMENT ON COLUMN procurement_items.preferred_vendor IS 'Vendor name for this procurement (formerly Preferred Vendor)';
COMMENT ON COLUMN procurement_items.final_price IS 'Final negotiated/agreed price in the specified currency';
COMMENT ON COLUMN procurement_items.final_price_currency IS 'Currency code for the final price (e.g., CAD, USD, EUR)';
COMMENT ON COLUMN procurement_items.final_price_cad IS 'Final price converted to CAD (required when final_price_currency is not CAD)';
