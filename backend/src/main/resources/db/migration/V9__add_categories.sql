-- myRC - Categories Migration
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-27
-- Version: 1.0.0
--
-- Description:
-- Creates the categories table for storing categories within fiscal years.
-- Categories are used to group both funding and spending items.
-- Default categories are system-managed and read-only.

-- Create the categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    fiscal_year_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_categories_fiscal_year 
        FOREIGN KEY (fiscal_year_id) 
        REFERENCES fiscal_years(id) 
        ON DELETE CASCADE,
    CONSTRAINT uk_category_name_fy 
        UNIQUE (name, fiscal_year_id)
);

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_categories_fiscal_year_id ON categories(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_categories_display_order ON categories(display_order);
CREATE INDEX IF NOT EXISTS idx_categories_is_default ON categories(is_default);

-- Add comments for documentation
COMMENT ON TABLE categories IS 'Stores categories associated with fiscal years. Categories group both funding and spending items.';
COMMENT ON COLUMN categories.name IS 'Name of the category (e.g., Compute, GPUs, Storage)';
COMMENT ON COLUMN categories.description IS 'Optional detailed description of the category';
COMMENT ON COLUMN categories.is_default IS 'Whether this is a system-managed default category (read-only)';
COMMENT ON COLUMN categories.display_order IS 'Order in which the category is displayed in the UI';
COMMENT ON COLUMN categories.fiscal_year_id IS 'Reference to the fiscal year this category belongs to';
COMMENT ON COLUMN categories.active IS 'Whether the category is currently active';

-- Add category_id column to funding_items table (nullable to support migration)
ALTER TABLE funding_items ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE funding_items ADD CONSTRAINT fk_funding_items_category 
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_funding_items_category_id ON funding_items(category_id);
COMMENT ON COLUMN funding_items.category_id IS 'Reference to the category this funding item belongs to (optional)';

-- Migrate data from spending_categories to categories if spending_categories exists
DO $$
DECLARE
    fk_constraint_name TEXT;
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'spending_categories') THEN
        INSERT INTO categories (name, description, is_default, display_order, fiscal_year_id, created_at, updated_at, active)
        SELECT name, description, is_default, display_order, fiscal_year_id, created_at, updated_at, active
        FROM spending_categories
        ON CONFLICT (name, fiscal_year_id) DO NOTHING;
        
        -- Update spending_items to reference new categories table
        -- This assumes spending_items has a spending_category_id column
        IF EXISTS (SELECT FROM information_schema.columns 
                   WHERE table_name = 'spending_items' 
                   AND column_name = 'spending_category_id') THEN
            -- Add new category_id column if it doesn't exist
            IF NOT EXISTS (SELECT FROM information_schema.columns 
                           WHERE table_name = 'spending_items' 
                           AND column_name = 'category_id') THEN
                ALTER TABLE spending_items ADD COLUMN category_id BIGINT;
            END IF;
            
            -- Migrate the category references
            UPDATE spending_items si
            SET category_id = c.id
            FROM spending_categories sc, categories c
            WHERE si.spending_category_id = sc.id
              AND sc.name = c.name
              AND sc.fiscal_year_id = c.fiscal_year_id;
              
            -- Find and drop any foreign key constraint that references spending_categories
            SELECT conname INTO fk_constraint_name
            FROM pg_constraint pc
            JOIN pg_class prel ON pc.conrelid = prel.oid
            JOIN pg_class frel ON pc.confrelid = frel.oid
            WHERE prel.relname = 'spending_items'
              AND frel.relname = 'spending_categories'
              AND pc.contype = 'f';
            
            IF fk_constraint_name IS NOT NULL THEN
                EXECUTE format('ALTER TABLE spending_items DROP CONSTRAINT %I', fk_constraint_name);
            END IF;
            
            -- Drop the old spending_category_id column
            ALTER TABLE spending_items DROP COLUMN IF EXISTS spending_category_id;
            
            -- Add the new foreign key constraint if it doesn't exist
            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint 
                WHERE conname = 'fk_spending_items_category' 
                AND conrelid = 'spending_items'::regclass
            ) THEN
                ALTER TABLE spending_items ADD CONSTRAINT fk_spending_items_category 
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
            END IF;
        END IF;
    ELSE
        -- If spending_categories doesn't exist, we still need to set up spending_items with category_id
        -- Add category_id column to spending_items table if it doesn't exist
        IF NOT EXISTS (SELECT FROM information_schema.columns 
                       WHERE table_name = 'spending_items' 
                       AND column_name = 'category_id') THEN
            -- Check if spending_category_id exists (from older migrations)
            IF EXISTS (SELECT FROM information_schema.columns 
                       WHERE table_name = 'spending_items' 
                       AND column_name = 'spending_category_id') THEN
                -- Find and drop any foreign key constraint that references spending_categories
                SELECT conname INTO fk_constraint_name
                FROM pg_constraint pc
                JOIN pg_class prel ON pc.conrelid = prel.oid
                WHERE prel.relname = 'spending_items'
                  AND pc.contype = 'f'
                  AND EXISTS (
                      SELECT 1 FROM pg_attribute pa 
                      WHERE pa.attrelid = pc.conrelid 
                      AND pa.attnum = ANY(pc.conkey)
                      AND pa.attname = 'spending_category_id'
                  );
                
                IF fk_constraint_name IS NOT NULL THEN
                    EXECUTE format('ALTER TABLE spending_items DROP CONSTRAINT %I', fk_constraint_name);
                END IF;
                
                -- Rename the column
                ALTER TABLE spending_items RENAME COLUMN spending_category_id TO category_id;
                
                -- Add new foreign key constraint to categories
                IF NOT EXISTS (
                    SELECT 1 FROM pg_constraint 
                    WHERE conname = 'fk_spending_items_category' 
                    AND conrelid = 'spending_items'::regclass
                ) THEN
                    ALTER TABLE spending_items ADD CONSTRAINT fk_spending_items_category 
                        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
                END IF;
            ELSE
                -- Neither column exists, add category_id as new column
                ALTER TABLE spending_items ADD COLUMN category_id BIGINT;
                ALTER TABLE spending_items ADD CONSTRAINT fk_spending_items_category 
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
END $$;

-- Create index on spending_items.category_id
CREATE INDEX IF NOT EXISTS idx_spending_items_category_id ON spending_items(category_id);
COMMENT ON COLUMN spending_items.category_id IS 'Reference to the category this spending item belongs to';
