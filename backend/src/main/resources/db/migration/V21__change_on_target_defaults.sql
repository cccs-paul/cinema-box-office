-- V21__change_on_target_defaults.sql
-- Changes the default On Target threshold values from -10%/+10% to -2%/+2%.
-- 
-- Author: myRC Team
-- Version: 1.0.0
-- Since: 2026-01-31
-- License: MIT

-- Update existing rows that still have the old defaults to the new defaults
UPDATE fiscal_years 
SET on_target_min = -2, on_target_max = 2 
WHERE on_target_min = -10 AND on_target_max = 10;

-- Change the default constraint for on_target_min
ALTER TABLE fiscal_years 
ALTER COLUMN on_target_min SET DEFAULT -2;

-- Change the default constraint for on_target_max
ALTER TABLE fiscal_years 
ALTER COLUMN on_target_max SET DEFAULT 2;
