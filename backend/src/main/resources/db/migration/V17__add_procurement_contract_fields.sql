-- V17: Add contract and completion fields to procurement_items table
-- These fields support vendor/contract tracking and completion status

-- Add preferred vendor field
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS preferred_vendor VARCHAR(200);

-- Add contract number field
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS contract_number VARCHAR(100);

-- Add contract date fields
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS contract_start_date DATE;

ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS contract_end_date DATE;

-- Add procurement completion fields
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS procurement_completed BOOLEAN DEFAULT FALSE;

ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS procurement_completed_date DATE;

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_procurement_items_preferred_vendor 
ON procurement_items(preferred_vendor) WHERE preferred_vendor IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_procurement_items_contract_number 
ON procurement_items(contract_number) WHERE contract_number IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_procurement_items_procurement_completed 
ON procurement_items(procurement_completed) WHERE procurement_completed = TRUE;
