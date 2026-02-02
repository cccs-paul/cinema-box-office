-- V23: Add show_search_box setting to fiscal_years
-- This setting controls visibility of the search box and category filters in list pages

-- First add the column as nullable
ALTER TABLE fiscal_years
ADD COLUMN show_search_box BOOLEAN;

-- Set default value for existing rows
UPDATE fiscal_years SET show_search_box = true;

-- Now make it NOT NULL
ALTER TABLE fiscal_years
ALTER COLUMN show_search_box SET NOT NULL;

-- Set default for future rows
ALTER TABLE fiscal_years
ALTER COLUMN show_search_box SET DEFAULT true;

COMMENT ON COLUMN fiscal_years.show_search_box IS 'Whether to show search box and category filters in Funding, Spending, and Procurement pages';
