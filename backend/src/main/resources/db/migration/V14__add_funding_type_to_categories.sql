-- myRC - Add Funding Type to Categories Migration
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-29
-- Version: 1.0.0
--
-- Description:
-- Adds funding_type column to categories table to configure which money types
-- (CAP, OM, or both) are allowed for each category.
-- Default categories Software Licenses, Small Procurement, and Contractors
-- are set to OM_ONLY.

-- Add funding_type column to categories table
ALTER TABLE categories ADD COLUMN IF NOT EXISTS funding_type VARCHAR(20) NOT NULL DEFAULT 'BOTH';

-- Add comment for documentation
COMMENT ON COLUMN categories.funding_type IS 'Allowed funding type for the category: CAP_ONLY, OM_ONLY, or BOTH. Defaults to BOTH.';

-- Update default categories to have appropriate funding types
-- Software Licenses, Small Procurement, and Contractors are OM_ONLY
UPDATE categories 
SET funding_type = 'OM_ONLY' 
WHERE is_default = true 
  AND name IN ('Software Licenses', 'Small Procurement', 'Contractors');

-- Compute, GPUs, and Storage allow both CAP and OM
UPDATE categories 
SET funding_type = 'BOTH' 
WHERE is_default = true 
  AND name IN ('Compute', 'GPUs', 'Storage');

-- Create index for filtering by funding type
CREATE INDEX IF NOT EXISTS idx_categories_funding_type ON categories(funding_type);
