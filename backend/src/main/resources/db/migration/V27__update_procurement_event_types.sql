-- V27: Update procurement event types
-- Drops old check constraint and adds new one with all current event types
-- Also expands event_type column to accommodate longer enum values

-- Expand event_type column to accommodate longer values (SAM_ACKNOWLEDGEMENT_REQUESTED = 30 chars)
ALTER TABLE procurement_events ALTER COLUMN event_type TYPE VARCHAR(35);

-- Drop the old check constraint
ALTER TABLE procurement_events DROP CONSTRAINT IF EXISTS procurement_events_event_type_check;

-- Add new check constraint with all current event types (including legacy types for backward compatibility)
ALTER TABLE procurement_events ADD CONSTRAINT procurement_events_event_type_check CHECK (
    event_type IN (
        -- Current event types
        'NOT_STARTED',
        'QUOTE',
        'SAM_ACKNOWLEDGEMENT_REQUESTED',
        'SAM_ACKNOWLEDGEMENT_RECEIVED',
        'PACKAGE_SENT_TO_PROCUREMENT',
        'ACKNOWLEDGED_BY_PROCUREMENT',
        'PAUSED',
        'CANCELLED',
        'CONTRACT_AWARDED',
        'GOODS_RECEIVED',
        'FULL_INVOICE_RECEIVED',
        'PARTIAL_INVOICE_RECEIVED',
        'MONTHLY_INVOICE_RECEIVED',
        'FULL_INVOICE_SIGNED',
        'PARTIAL_INVOICE_SIGNED',
        'MONTHLY_INVOICE_SIGNED',
        'CONTRACT_AMENDED',
        -- Legacy event types (for backward compatibility)
        'CREATED',
        'STATUS_CHANGE',
        'NOTE_ADDED',
        'QUOTE_RECEIVED',
        'QUOTE_SELECTED',
        'QUOTE_REJECTED',
        'PO_ISSUED',
        'DELIVERED',
        'INVOICED',
        'PAYMENT_MADE',
        'COMPLETED',
        'OTHER'
    )
);
