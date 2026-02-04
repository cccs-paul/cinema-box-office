-- V28: Add user tracking fields to procurement quotes
-- This migration adds created_by and modified_by columns to track who created/modified quotes

-- Add created_by and modified_by columns to procurement_quotes table
ALTER TABLE procurement_quotes ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE procurement_quotes ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

-- Add comments for documentation
COMMENT ON COLUMN procurement_quotes.created_by IS 'Username of the user who created this quote';
COMMENT ON COLUMN procurement_quotes.modified_by IS 'Username of the user who last modified this quote';
