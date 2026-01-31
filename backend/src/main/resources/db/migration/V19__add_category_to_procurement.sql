/*
 * myRC - Database Migration V19
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-31
 * Version: 1.0.0
 *
 * Description:
 * Add category_id to procurement_items table to support categorization
 * of procurement items similar to funding and spending items.
 * Category is optional (nullable) for procurement items.
 */

-- Add category_id column to procurement_items table (nullable)
ALTER TABLE procurement_items
ADD COLUMN category_id BIGINT NULL;

-- Add foreign key constraint
ALTER TABLE procurement_items
ADD CONSTRAINT fk_procurement_items_category
FOREIGN KEY (category_id) REFERENCES categories(id);

-- Create index for category lookups
CREATE INDEX idx_procurement_items_category ON procurement_items(category_id);
