-- V15: Replace status with source in funding_items and add comments field
-- This migration:
-- 1. Adds the 'source' column with default BUSINESS_PLAN
-- 2. Adds the 'comments' column for optional notes
-- 3. Removes the 'status' column

-- Add the source column (mandatory with default value)
ALTER TABLE funding_items ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'BUSINESS_PLAN';

-- Add the comments column (optional)
ALTER TABLE funding_items ADD COLUMN IF NOT EXISTS comments VARCHAR(2000);

-- Remove the status column if it exists
ALTER TABLE funding_items DROP COLUMN IF EXISTS status;
