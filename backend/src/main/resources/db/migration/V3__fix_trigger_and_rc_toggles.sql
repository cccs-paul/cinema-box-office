-- ============================================================================
-- V3: Fix default money trigger + Add training/travel enabled columns to RC
-- ============================================================================
-- 
-- 1. Fix the ensure_default_money() trigger function.
--    The V1 trigger only inserted (code, name, description, is_default,
--    fiscal_year_id, display_order) into monies. However, Hibernate's
--    schema validation removes DEFAULT values from columns, so `active`,
--    `created_at`, `updated_at`, and `version` columns have no defaults.
--    This caused a NOT NULL violation on `active` when creating a new FY.
--
-- 2. Add training_enabled and travel_enabled columns to responsibility_centres.
--    These boolean toggles allow RC owners to enable/disable Training and
--    Travel features per RC. Both default to TRUE (enabled).
-- ============================================================================

-- 1. Fix the trigger function to include all required columns
CREATE OR REPLACE FUNCTION ensure_default_money()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO monies (code, name, description, is_default, fiscal_year_id, display_order, active, created_at, updated_at, version)
    VALUES ('AB', 'A-Base', 'Default A-Base funding allocation', TRUE, NEW.id, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Add training_enabled and travel_enabled columns to responsibility_centres
ALTER TABLE responsibility_centres ADD COLUMN IF NOT EXISTS training_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE responsibility_centres ADD COLUMN IF NOT EXISTS travel_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- 3. Fix column name mismatches between entities and V1 migration schema
--    Rename SQL columns to match Java entity field names so Hibernate validates correctly.

-- Comment/description field renames
ALTER TABLE funding_items RENAME COLUMN source_details TO comments;
ALTER TABLE spending_events RENAME COLUMN description TO comment;
ALTER TABLE spending_invoices RENAME COLUMN description TO comments;
ALTER TABLE procurement_events RENAME COLUMN description TO comment;
ALTER TABLE procurement_quotes RENAME COLUMN description TO notes;

-- Binary data field renames
ALTER TABLE spending_invoice_files RENAME COLUMN data TO content;
ALTER TABLE procurement_event_files RENAME COLUMN data TO content;
ALTER TABLE procurement_quote_files RENAME COLUMN data TO content;
