-- V29__add_spending_events.sql
-- Add spending_events table for tracking events on spending items not linked to procurement

CREATE TABLE spending_events (
    id BIGSERIAL PRIMARY KEY,
    spending_item_id BIGINT NOT NULL,
    event_type VARCHAR(35) NOT NULL DEFAULT 'PENDING',
    event_date DATE NOT NULL DEFAULT CURRENT_DATE,
    comment VARCHAR(2000),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_spending_event_spending_item
        FOREIGN KEY (spending_item_id)
        REFERENCES spending_items(id)
        ON DELETE CASCADE
);

-- Create indexes for common queries
CREATE INDEX idx_spending_events_item ON spending_events(spending_item_id);
CREATE INDEX idx_spending_events_active ON spending_events(spending_item_id, active);
CREATE INDEX idx_spending_events_date ON spending_events(event_date DESC);
CREATE INDEX idx_spending_events_type ON spending_events(event_type);

-- Add comment describing the event types
COMMENT ON COLUMN spending_events.event_type IS 'Event type: PENDING, ECO_REQUESTED, ECO_RECEIVED, EXTERNAL_APPROVAL_REQUESTED, EXTERNAL_APPROVAL_RECEIVED, SECTION_32_PROVIDED, RECEIVED_GOODS_SERVICES, SECTION_34_PROVIDED, CREDIT_CARD_CLEARED, CANCELLED, ON_HOLD';
