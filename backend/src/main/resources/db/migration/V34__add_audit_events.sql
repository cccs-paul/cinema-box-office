-- V34: Add audit_events table for pre-emptive action auditing
-- This table is fully independent — no foreign keys to any other table.
-- All references are stored as denormalized values (names, IDs as plain columns).
-- Audit records are immutable once written.

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,

    -- Who performed the action
    username VARCHAR(255) NOT NULL,

    -- What action was performed (e.g., CREATE_RC, DELETE_FY, UPDATE_FUNDING_ITEM)
    action VARCHAR(100) NOT NULL,

    -- The entity type targeted (e.g., RESPONSIBILITY_CENTRE, FISCAL_YEAR, FUNDING_ITEM)
    entity_type VARCHAR(100) NOT NULL,

    -- The ID of the entity targeted (nullable for create actions before ID is assigned)
    entity_id BIGINT,

    -- Human-readable name/label of the entity at the time of the action
    entity_name VARCHAR(500),

    -- Context: which RC and FY the action occurred in (denormalized, no FK)
    rc_id BIGINT,
    rc_name VARCHAR(255),
    fiscal_year_id BIGINT,
    fiscal_year_name VARCHAR(255),

    -- Serialized JSON of the request parameters/payload (excluding file content)
    parameters TEXT,

    -- The HTTP method and endpoint path
    http_method VARCHAR(10),
    endpoint VARCHAR(500),

    -- Browser User-Agent string
    user_agent VARCHAR(1000),

    -- IP address of the client
    ip_address VARCHAR(45),

    -- Outcome: PENDING (pre-emptive insert), SUCCESS, FAILURE
    outcome VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Error message if the action failed
    error_message TEXT,

    -- Source audit event ID if this record was created by cloning
    cloned_from_audit_id BIGINT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_audit_events_username ON audit_events(username);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_id ON audit_events(rc_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_fy ON audit_events(rc_id, fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action);
CREATE INDEX IF NOT EXISTS idx_audit_events_entity_type ON audit_events(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_created_at ON audit_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_events_outcome ON audit_events(outcome);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_created ON audit_events(rc_id, created_at DESC);
