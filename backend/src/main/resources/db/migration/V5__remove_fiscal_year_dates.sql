-- Cinema Box Office - Remove Fiscal Year Date Fields
-- Copyright (c) 2026 Box Office Team
-- Licensed under MIT License
--
-- Author: Box Office Team
-- Date: 2026-01-23
-- Version: 1.0.0
--
-- Description:
-- Simplifies the fiscal_years table by removing start_date and end_date columns.
-- Fiscal years are now identified by name only (unique per responsibility centre).

-- Drop the date-related index first
DROP INDEX IF EXISTS idx_fiscal_years_dates;

-- Drop the date validation constraint
ALTER TABLE fiscal_years DROP CONSTRAINT IF EXISTS fiscal_year_dates_valid;

-- Drop the date columns
ALTER TABLE fiscal_years DROP COLUMN IF EXISTS start_date;
ALTER TABLE fiscal_years DROP COLUMN IF EXISTS end_date;

-- Update column comments
COMMENT ON COLUMN fiscal_years.name IS 'Name of the fiscal year, unique within a responsibility centre';

-- Drop old date-related comments (they are automatically removed with columns)
