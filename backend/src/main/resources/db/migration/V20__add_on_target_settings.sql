-- V20__add_on_target_settings.sql
-- Adds On Target threshold settings to fiscal years for Summary page status indicator.
-- 
-- Author: myRC Team
-- Version: 1.0.0
-- Since: 2026-01-24
-- License: MIT

-- Add on_target_min column with default value -10 (representing -10%)
ALTER TABLE fiscal_years
ADD COLUMN on_target_min INTEGER NOT NULL DEFAULT -10;

-- Add on_target_max column with default value 10 (representing +10%)
ALTER TABLE fiscal_years
ADD COLUMN on_target_max INTEGER NOT NULL DEFAULT 10;

-- Add constraint to ensure on_target_min is between -100 and 100
ALTER TABLE fiscal_years
ADD CONSTRAINT chk_on_target_min CHECK (on_target_min >= -100 AND on_target_min <= 100);

-- Add constraint to ensure on_target_max is between -100 and 100
ALTER TABLE fiscal_years
ADD CONSTRAINT chk_on_target_max CHECK (on_target_max >= -100 AND on_target_max <= 100);

-- Add constraint to ensure on_target_min <= on_target_max
ALTER TABLE fiscal_years
ADD CONSTRAINT chk_on_target_range CHECK (on_target_min <= on_target_max);
