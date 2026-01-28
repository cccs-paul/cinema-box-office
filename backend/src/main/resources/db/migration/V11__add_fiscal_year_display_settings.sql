--
-- myRC - Database Migration V11
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Add display settings columns to fiscal_years table
-- These settings control category filtering and grouping in the UI
--

-- Add show_category_filter column (default true - show the filter)
ALTER TABLE fiscal_years
ADD COLUMN IF NOT EXISTS show_category_filter BOOLEAN NOT NULL DEFAULT true;

-- Add group_by_category column (default false - don't group)
ALTER TABLE fiscal_years
ADD COLUMN IF NOT EXISTS group_by_category BOOLEAN NOT NULL DEFAULT false;

-- Add comments for documentation
COMMENT ON COLUMN fiscal_years.show_category_filter IS 'Controls whether the category filter is displayed in funding/spending pages';
COMMENT ON COLUMN fiscal_years.group_by_category IS 'Controls whether funding/spending items are grouped by category';
