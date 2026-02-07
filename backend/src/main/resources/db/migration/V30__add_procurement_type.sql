-- V30: Add procurement_type column to procurement_items table
-- Procurement Type indicates whether the procurement was initiated by the RC or centrally managed.
-- Values: RC_INITIATED, CENTRALLY_MANAGED
-- Default: RC_INITIATED
ALTER TABLE procurement_items ADD COLUMN procurement_type VARCHAR(20) DEFAULT 'RC_INITIATED';
