-- myRC - Fix Cascade Delete Constraints
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-27
-- Version: 1.0.0
--
-- Description:
-- Fixes foreign key constraints to properly cascade deletes from fiscal_years.
-- This ensures that when a fiscal year is deleted, all related entities are also deleted.

-- Drop and recreate FK constraints on monies table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from monies to fiscal_years
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'monies'
      AND frel.relname = 'fiscal_years'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE monies DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    ALTER TABLE monies ADD CONSTRAINT fk_monies_fiscal_year 
        FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE CASCADE;
END $$;

-- Drop and recreate FK constraints on categories table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from categories to fiscal_years
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'categories'
      AND frel.relname = 'fiscal_years'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE categories DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    ALTER TABLE categories ADD CONSTRAINT fk_categories_fiscal_year 
        FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE CASCADE;
END $$;

-- Drop and recreate FK constraints on funding_items table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from funding_items to fiscal_years
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'funding_items'
      AND frel.relname = 'fiscal_years'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE funding_items DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    ALTER TABLE funding_items ADD CONSTRAINT fk_funding_items_fiscal_year 
        FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE CASCADE;
END $$;

-- Drop and recreate FK constraints on spending_items table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from spending_items to fiscal_years
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'spending_items'
      AND frel.relname = 'fiscal_years'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE spending_items DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    ALTER TABLE spending_items ADD CONSTRAINT fk_spending_items_fiscal_year 
        FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE CASCADE;
END $$;

-- Drop and recreate FK constraints on money_allocations table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from money_allocations to funding_items
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'money_allocations'
      AND frel.relname = 'funding_items'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE money_allocations DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'money_allocations') THEN
        ALTER TABLE money_allocations ADD CONSTRAINT fk_money_allocations_funding_item 
            FOREIGN KEY (funding_item_id) REFERENCES funding_items(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Drop and recreate FK constraints on money_allocations table to monies to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from money_allocations to monies
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'money_allocations'
      AND frel.relname = 'monies'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE money_allocations DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'money_allocations') THEN
        ALTER TABLE money_allocations ADD CONSTRAINT fk_money_allocations_money 
            FOREIGN KEY (money_id) REFERENCES monies(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Drop and recreate FK constraints on spending_money_allocations table to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from spending_money_allocations to spending_items
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'spending_money_allocations'
      AND frel.relname = 'spending_items'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE spending_money_allocations DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'spending_money_allocations') THEN
        ALTER TABLE spending_money_allocations ADD CONSTRAINT fk_spending_money_allocations_spending_item 
            FOREIGN KEY (spending_item_id) REFERENCES spending_items(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Drop and recreate FK constraints on spending_money_allocations table to monies to add CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from spending_money_allocations to monies
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'spending_money_allocations'
      AND frel.relname = 'monies'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE spending_money_allocations DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'spending_money_allocations') THEN
        ALTER TABLE spending_money_allocations ADD CONSTRAINT fk_spending_money_allocations_money 
            FOREIGN KEY (money_id) REFERENCES monies(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Also fix fiscal_years FK to responsibility_centres if not already CASCADE
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    -- Find the FK constraint from fiscal_years to responsibility_centres
    SELECT conname INTO fk_constraint_name
    FROM pg_constraint pc
    JOIN pg_class prel ON pc.conrelid = prel.oid
    JOIN pg_class frel ON pc.confrelid = frel.oid
    WHERE prel.relname = 'fiscal_years'
      AND frel.relname = 'responsibility_centres'
      AND pc.contype = 'f';
    
    IF fk_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE fiscal_years DROP CONSTRAINT %I', fk_constraint_name);
    END IF;
    
    -- Add the constraint back with CASCADE
    ALTER TABLE fiscal_years ADD CONSTRAINT fk_fiscal_years_rc 
        FOREIGN KEY (responsibility_centre_id) REFERENCES responsibility_centres(id) ON DELETE CASCADE;
END $$;

-- Add comments
COMMENT ON CONSTRAINT fk_fiscal_years_rc ON fiscal_years IS 'Cascade delete fiscal years when RC is deleted';
