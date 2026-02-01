--
-- myRC - Optimistic Locking Migration
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Description:
-- Add version columns to all mutable entities for optimistic locking.
-- This prevents lost updates when multiple users edit the same record concurrently.
--

-- Add version column to users table (use two-step approach for existing data)
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE users SET version = 0 WHERE version IS NULL;
ALTER TABLE users ALTER COLUMN version SET NOT NULL;
ALTER TABLE users ALTER COLUMN version SET DEFAULT 0;

-- Add version column to responsibility_centres table
ALTER TABLE responsibility_centres ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE responsibility_centres SET version = 0 WHERE version IS NULL;
ALTER TABLE responsibility_centres ALTER COLUMN version SET NOT NULL;
ALTER TABLE responsibility_centres ALTER COLUMN version SET DEFAULT 0;

-- Add version column to fiscal_years table
ALTER TABLE fiscal_years ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE fiscal_years SET version = 0 WHERE version IS NULL;
ALTER TABLE fiscal_years ALTER COLUMN version SET NOT NULL;
ALTER TABLE fiscal_years ALTER COLUMN version SET DEFAULT 0;

-- Add version column to funding_items table
ALTER TABLE funding_items ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE funding_items SET version = 0 WHERE version IS NULL;
ALTER TABLE funding_items ALTER COLUMN version SET NOT NULL;
ALTER TABLE funding_items ALTER COLUMN version SET DEFAULT 0;

-- Add version column to spending_items table
ALTER TABLE spending_items ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE spending_items SET version = 0 WHERE version IS NULL;
ALTER TABLE spending_items ALTER COLUMN version SET NOT NULL;
ALTER TABLE spending_items ALTER COLUMN version SET DEFAULT 0;

-- Add version column to procurement_items table
ALTER TABLE procurement_items ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE procurement_items SET version = 0 WHERE version IS NULL;
ALTER TABLE procurement_items ALTER COLUMN version SET NOT NULL;
ALTER TABLE procurement_items ALTER COLUMN version SET DEFAULT 0;

-- Add version column to procurement_events table
ALTER TABLE procurement_events ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE procurement_events SET version = 0 WHERE version IS NULL;
ALTER TABLE procurement_events ALTER COLUMN version SET NOT NULL;
ALTER TABLE procurement_events ALTER COLUMN version SET DEFAULT 0;

-- Add version column to procurement_quotes table
ALTER TABLE procurement_quotes ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE procurement_quotes SET version = 0 WHERE version IS NULL;
ALTER TABLE procurement_quotes ALTER COLUMN version SET NOT NULL;
ALTER TABLE procurement_quotes ALTER COLUMN version SET DEFAULT 0;

-- Add version column to categories table
ALTER TABLE categories ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE categories SET version = 0 WHERE version IS NULL;
ALTER TABLE categories ALTER COLUMN version SET NOT NULL;
ALTER TABLE categories ALTER COLUMN version SET DEFAULT 0;

-- Add version column to spending_categories table
ALTER TABLE spending_categories ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE spending_categories SET version = 0 WHERE version IS NULL;
ALTER TABLE spending_categories ALTER COLUMN version SET NOT NULL;
ALTER TABLE spending_categories ALTER COLUMN version SET DEFAULT 0;

-- Add version column to monies table
ALTER TABLE monies ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE monies SET version = 0 WHERE version IS NULL;
ALTER TABLE monies ALTER COLUMN version SET NOT NULL;
ALTER TABLE monies ALTER COLUMN version SET DEFAULT 0;

-- Add version column to money_allocations table
ALTER TABLE money_allocations ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE money_allocations SET version = 0 WHERE version IS NULL;
ALTER TABLE money_allocations ALTER COLUMN version SET NOT NULL;
ALTER TABLE money_allocations ALTER COLUMN version SET DEFAULT 0;

-- Add version column to spending_money_allocations table
ALTER TABLE spending_money_allocations ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE spending_money_allocations SET version = 0 WHERE version IS NULL;
ALTER TABLE spending_money_allocations ALTER COLUMN version SET NOT NULL;
ALTER TABLE spending_money_allocations ALTER COLUMN version SET DEFAULT 0;

-- Add version column to rc_access table
ALTER TABLE rc_access ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE rc_access SET version = 0 WHERE version IS NULL;
ALTER TABLE rc_access ALTER COLUMN version SET NOT NULL;
ALTER TABLE rc_access ALTER COLUMN version SET DEFAULT 0;
