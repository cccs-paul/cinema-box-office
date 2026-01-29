-- V16: Remove budget_amount column from funding_items
-- The budget is now computed from money allocation CAP/OM values instead

-- Remove the budget_amount column if it exists
ALTER TABLE funding_items DROP COLUMN IF EXISTS budget_amount;
