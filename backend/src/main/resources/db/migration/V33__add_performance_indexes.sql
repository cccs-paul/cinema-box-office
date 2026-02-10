-- V33: Add missing indexes for query performance at scale
-- Addresses FK columns, frequently-queried columns, and composite indexes
-- that are used in repository queries but were never indexed.

-- ============================================================================
-- USERS TABLE
-- The users table has unique constraints on username and email (from JPA),
-- but several other columns are queried frequently with no index support.
-- ============================================================================

-- Used by: findByUsername (case-insensitive login lookups)
-- A functional index on LOWER(username) supports case-insensitive queries
CREATE INDEX IF NOT EXISTS idx_users_username_lower
    ON users (LOWER(username));

-- Used by: findByEmail, existsByEmailIgnoreCase (case-insensitive email lookups)
CREATE INDEX IF NOT EXISTS idx_users_email_lower
    ON users (LOWER(email));

-- Used by: findByExternalIdAndAuthProvider, findByOauthProviderAndExternalId
CREATE INDEX IF NOT EXISTS idx_users_external_id
    ON users (external_id) WHERE external_id IS NOT NULL;

-- Used by: findByAuthProvider, countByAuthProvider, findByAuthProviderAndEnabled
CREATE INDEX IF NOT EXISTS idx_users_auth_provider
    ON users (auth_provider);

-- Used by: findByEnabled
CREATE INDEX IF NOT EXISTS idx_users_enabled
    ON users (enabled);

-- ============================================================================
-- RESPONSIBILITY CENTRES TABLE
-- owner_id is already indexed; add active filter index
-- ============================================================================

-- Used by: findByActiveTrue, findByActiveTrueAndOwnerId
CREATE INDEX IF NOT EXISTS idx_responsibility_centres_active
    ON responsibility_centres (active);

-- Composite: most common query pattern (active centres for a given owner)
CREATE INDEX IF NOT EXISTS idx_responsibility_centres_owner_active
    ON responsibility_centres (owner_id, active);

-- ============================================================================
-- RC_ACCESS TABLE
-- responsibility_centre_id and user_id are indexed individually;
-- add composite index for common combined lookups.
-- ============================================================================

-- Used by: findByResponsibilityCentreIdAndAccessLevel, countByResponsibilityCentreIdAndAccessLevel
CREATE INDEX IF NOT EXISTS idx_rc_access_rc_access_level
    ON rc_access (responsibility_centre_id, access_level);

-- Used by: findByPrincipalIdentifierAndPrincipalType
CREATE INDEX IF NOT EXISTS idx_rc_access_principal_id_type
    ON rc_access (principal_identifier, principal_type);

-- ============================================================================
-- FISCAL YEARS TABLE
-- responsibility_centre_id is already indexed; add composite for active filter
-- ============================================================================

-- Used by: findByResponsibilityCentreIdAndActiveTrue (most common query)
CREATE INDEX IF NOT EXISTS idx_fiscal_years_rc_active
    ON fiscal_years (responsibility_centre_id, active);

-- ============================================================================
-- SPENDING ITEMS TABLE  *** CRITICAL - high-volume table, no FK index ***
-- This table can grow into millions of rows and has NO index on fiscal_year_id
-- ============================================================================

-- Used by: findByFiscalYearId, countByFiscalYearId (the primary access pattern)
CREATE INDEX IF NOT EXISTS idx_spending_items_fiscal_year_id
    ON spending_items (fiscal_year_id);

-- Used by: findByFiscalYearIdAndActiveTrue (most common filtered query)
CREATE INDEX IF NOT EXISTS idx_spending_items_fy_active
    ON spending_items (fiscal_year_id, active);

-- Used by: findByFiscalYearIdAndCategoryIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_spending_items_fy_category_active
    ON spending_items (fiscal_year_id, category_id, active);

-- Used by: existsByNameAndFiscalYearId, findByNameAndFiscalYearId
CREATE INDEX IF NOT EXISTS idx_spending_items_name_fy
    ON spending_items (fiscal_year_id, name);

-- Used by: countByCategoryId, existsByCategoryId
-- (category_id is already indexed from V9, but adding composite for active)
CREATE INDEX IF NOT EXISTS idx_spending_items_category_active
    ON spending_items (category_id, active);

-- ============================================================================
-- MONEY ALLOCATIONS TABLE  *** CRITICAL - no indexes at all ***
-- This is a join table heavily queried on both FK columns
-- ============================================================================

-- Used by: findByFundingItemId, deleteByFundingItemId, sumCapByFundingItemId, etc.
CREATE INDEX IF NOT EXISTS idx_money_allocations_funding_item_id
    ON money_allocations (funding_item_id);

-- Used by: findByMoneyId, existsByMoneyId, deleteByMoneyIdAndCapAmountAndOmAmount
CREATE INDEX IF NOT EXISTS idx_money_allocations_money_id
    ON money_allocations (money_id);

-- Composite covering the unique constraint columns for fast lookups
CREATE INDEX IF NOT EXISTS idx_money_allocations_fi_money
    ON money_allocations (funding_item_id, money_id);

-- ============================================================================
-- SPENDING MONEY ALLOCATIONS TABLE  *** CRITICAL - no indexes at all ***
-- Same pattern as money_allocations
-- ============================================================================

-- Used by: findBySpendingItemId, deleteBySpendingItemId, etc.
CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_spending_item_id
    ON spending_money_allocations (spending_item_id);

-- Used by: findByMoneyId, existsByMoneyId
CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_money_id
    ON spending_money_allocations (money_id);

-- Composite covering unique constraint for fast lookups
CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_si_money
    ON spending_money_allocations (spending_item_id, money_id);

-- ============================================================================
-- FUNDING ITEMS TABLE
-- fiscal_year_id and category_id are indexed; add composite for common patterns
-- ============================================================================

-- Used by: findByFiscalYearIdAndActiveTrue (most common query)
CREATE INDEX IF NOT EXISTS idx_funding_items_fy_active
    ON funding_items (fiscal_year_id, active);

-- Used by: findByFiscalYearIdAndCategoryIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_funding_items_fy_category_active
    ON funding_items (fiscal_year_id, category_id, active);

-- Used by: existsByNameAndFiscalYearId
CREATE INDEX IF NOT EXISTS idx_funding_items_name_fy
    ON funding_items (fiscal_year_id, name);

-- ============================================================================
-- PROCUREMENT ITEMS TABLE
-- fiscal_year_id and category_id are indexed; add composite for active filter
-- ============================================================================

-- Used by: findByFiscalYearIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_items_fy_active
    ON procurement_items (fiscal_year_id, active);

-- Used by: findByFiscalYearIdAndCategoryIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_items_fy_category_active
    ON procurement_items (fiscal_year_id, category_id, active);

-- Used by: existsByNameAndFiscalYearId
CREATE INDEX IF NOT EXISTS idx_procurement_items_name_fy
    ON procurement_items (fiscal_year_id, name);

-- ============================================================================
-- PROCUREMENT EVENTS TABLE  *** CRITICAL - no indexes at all ***
-- Heavily queried child table with multiple access patterns
-- ============================================================================

-- Used by: findByProcurementItemId (primary FK, most common query)
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_id
    ON procurement_events (procurement_item_id);

-- Used by: findByProcurementItemIdAndActiveTrue (filtered by active)
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_active
    ON procurement_events (procurement_item_id, active);

-- Used by: findByProcurementItemIdAndEventTypeAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_type_active
    ON procurement_events (procurement_item_id, event_type, active);

-- Used by: findByProcurementItemIdAndNewStatusAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_status_active
    ON procurement_events (procurement_item_id, new_status, active);

-- Used by: findByProcurementItemIdAndActiveTrueOrderByEventDateDesc
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_date
    ON procurement_events (procurement_item_id, event_date DESC);

-- Used by: findByCreatedByAndActiveTrueOrderByEventDateDesc
CREATE INDEX IF NOT EXISTS idx_procurement_events_created_by
    ON procurement_events (created_by) WHERE active = TRUE;

-- ============================================================================
-- PROCUREMENT QUOTES TABLE
-- procurement_item_id is indexed; add composite for common filter patterns
-- ============================================================================

-- Used by: findByProcurementItemIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_quotes_item_active
    ON procurement_quotes (procurement_item_id, active);

-- Used by: findByProcurementItemIdAndSelectedTrueAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_quotes_item_selected
    ON procurement_quotes (procurement_item_id, selected, active);

-- ============================================================================
-- PROCUREMENT QUOTE FILES TABLE
-- quote_id is indexed; add composite for active filter
-- ============================================================================

-- Used by: findByQuoteIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_quote_files_quote_active
    ON procurement_quote_files (quote_id, active);

-- ============================================================================
-- PROCUREMENT EVENT FILES TABLE
-- event_id is indexed; add composite for active filter
-- ============================================================================

-- Used by: findByEventIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_procurement_event_files_event_active
    ON procurement_event_files (event_id, active);

-- ============================================================================
-- SPENDING EVENTS TABLE
-- spending_item_id is indexed; add composite for active filter
-- ============================================================================

-- Used by: findBySpendingItemIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_spending_events_item_active
    ON spending_events (spending_item_id, active);

-- ============================================================================
-- SPENDING INVOICES TABLE
-- spending_item_id is indexed; add composite for active filter
-- ============================================================================

-- Used by: findBySpendingItemIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_spending_invoices_item_active
    ON spending_invoices (spending_item_id, active);

-- ============================================================================
-- SPENDING INVOICE FILES TABLE
-- invoice_id is indexed; add composite for active filter
-- ============================================================================

-- Used by: findByInvoiceIdAndActiveTrue
CREATE INDEX IF NOT EXISTS idx_spending_invoice_files_inv_active
    ON spending_invoice_files (invoice_id, active);

-- ============================================================================
-- CATEGORIES TABLE
-- fiscal_year_id is indexed; add composite for common filter patterns
-- ============================================================================

-- Used by: findByFiscalYearIdAndActiveTrueOrderByDisplayOrder
CREATE INDEX IF NOT EXISTS idx_categories_fy_active_order
    ON categories (fiscal_year_id, active, display_order);

-- ============================================================================
-- MONIES TABLE
-- fiscal_year_id is indexed; add composite for common filter patterns
-- ============================================================================

-- Used by: findByFiscalYearIdAndActiveTrueOrderByDisplayOrder
CREATE INDEX IF NOT EXISTS idx_monies_fy_active_order
    ON monies (fiscal_year_id, active, display_order);
