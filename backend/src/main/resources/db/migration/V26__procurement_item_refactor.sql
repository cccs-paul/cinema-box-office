/*
 * myRC - Procurement Item Refactor Migration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-04
 * Version: 1.0.0
 *
 * Description:
 * This migration refactors the procurement_items table:
 * 1. Removes status field (status is now tracked via procurement_events.newStatus)
 * 2. Removes currency and exchange_rate fields (not needed at item level)
 * 3. Adds final_price_exchange_rate for final price currency conversion
 * 4. Adds quoted/estimated price fields with currency support
 * 5. Creates procurement_event_files table for file attachments to tracking events
 */

-- Remove status field from procurement_items (status is tracked via events)
ALTER TABLE procurement_items
DROP COLUMN IF EXISTS status;

-- Remove currency field from procurement_items
ALTER TABLE procurement_items
DROP COLUMN IF EXISTS currency;

-- Remove exchange_rate field from procurement_items
ALTER TABLE procurement_items
DROP COLUMN IF EXISTS exchange_rate;

-- Add exchange rate for final price (required when final_price_currency is not CAD)
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS final_price_exchange_rate DECIMAL(10, 6);

-- Add quoted/estimated price fields
ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS quoted_price NUMERIC(15, 2);

ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS quoted_price_currency VARCHAR(3) DEFAULT 'CAD';

ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS quoted_price_exchange_rate DECIMAL(10, 6);

ALTER TABLE procurement_items
ADD COLUMN IF NOT EXISTS quoted_price_cad NUMERIC(15, 2);

-- Create indexes for quoted price queries
CREATE INDEX IF NOT EXISTS idx_procurement_items_quoted_price 
ON procurement_items(quoted_price) WHERE quoted_price IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_procurement_items_quoted_price_currency 
ON procurement_items(quoted_price_currency) WHERE quoted_price_currency IS NOT NULL;

-- Add comments for clarity
COMMENT ON COLUMN procurement_items.final_price_exchange_rate IS 'Exchange rate to convert final_price to CAD (required when final_price_currency is not CAD)';
COMMENT ON COLUMN procurement_items.quoted_price IS 'Quoted or estimated price in the specified currency';
COMMENT ON COLUMN procurement_items.quoted_price_currency IS 'Currency code for the quoted price (e.g., CAD, USD, EUR)';
COMMENT ON COLUMN procurement_items.quoted_price_exchange_rate IS 'Exchange rate to convert quoted_price to CAD (required when quoted_price_currency is not CAD)';
COMMENT ON COLUMN procurement_items.quoted_price_cad IS 'Quoted price converted to CAD (required when quoted_price_currency is not CAD)';

-- Create table for procurement event file attachments
CREATE TABLE IF NOT EXISTS procurement_event_files (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES procurement_events(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    content BYTEA NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_event_files_event FOREIGN KEY (event_id) REFERENCES procurement_events(id) ON DELETE CASCADE
);

-- Create indexes for procurement_event_files
CREATE INDEX IF NOT EXISTS idx_event_files_event_id ON procurement_event_files(event_id);
CREATE INDEX IF NOT EXISTS idx_event_files_active ON procurement_event_files(active) WHERE active = TRUE;

-- Add comments for the new table
COMMENT ON TABLE procurement_event_files IS 'Files attached to procurement tracking events';
COMMENT ON COLUMN procurement_event_files.event_id IS 'Reference to the parent procurement event';
COMMENT ON COLUMN procurement_event_files.file_name IS 'Original filename as uploaded';
COMMENT ON COLUMN procurement_event_files.content_type IS 'MIME content type of the file';
COMMENT ON COLUMN procurement_event_files.file_size IS 'File size in bytes';
COMMENT ON COLUMN procurement_event_files.content IS 'Binary content of the file (stored as BYTEA)';
COMMENT ON COLUMN procurement_event_files.description IS 'Optional description of the file';
