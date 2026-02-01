-- myRC - Add Funding Items Table
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-23
-- Version: 1.0.0
--
-- Description:
-- Creates the funding_items table for storing funding items within fiscal years.
-- Each fiscal year can have 0..n funding items.

-- Create funding_items table
CREATE TABLE funding_items (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    budget_amount DECIMAL(15, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    fiscal_year_id INTEGER NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_funding_item_name_per_fy UNIQUE(name, fiscal_year_id),
    CONSTRAINT valid_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'ACTIVE', 'CLOSED'))
);

-- Create indexes for faster queries
CREATE INDEX idx_funding_items_fiscal_year_id ON funding_items(fiscal_year_id);
CREATE INDEX idx_funding_items_status ON funding_items(status);
CREATE INDEX idx_funding_items_active ON funding_items(active);

-- Add comments for documentation
COMMENT ON TABLE funding_items IS 'Stores funding items associated with fiscal years';
COMMENT ON COLUMN funding_items.name IS 'Name of the funding item, unique within a fiscal year';
COMMENT ON COLUMN funding_items.description IS 'Detailed description of the funding item';
COMMENT ON COLUMN funding_items.budget_amount IS 'Budget amount allocated for this funding item';
COMMENT ON COLUMN funding_items.status IS 'Current status of the funding item (DRAFT, PENDING, APPROVED, ACTIVE, CLOSED)';
COMMENT ON COLUMN funding_items.active IS 'Whether the funding item is currently active';
