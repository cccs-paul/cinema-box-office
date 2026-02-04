/**
 * Spending Event model for myRC application.
 * Used for tracking events on spending items that are NOT linked to procurement.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 * @license MIT
 */

/**
 * Enum for spending event types.
 */
export type SpendingEventType =
  | 'PENDING'
  | 'ECO_REQUESTED'
  | 'ECO_RECEIVED'
  | 'EXTERNAL_APPROVAL_REQUESTED'
  | 'EXTERNAL_APPROVAL_RECEIVED'
  | 'SECTION_32_PROVIDED'
  | 'RECEIVED_GOODS_SERVICES'
  | 'SECTION_34_PROVIDED'
  | 'CREDIT_CARD_CLEARED'
  | 'CANCELLED'
  | 'ON_HOLD';

/**
 * Event type display information.
 */
export interface SpendingEventTypeInfo {
  label: string;
  description: string;
  color: string;
  icon: string;
}

/**
 * Map of spending event type to display information.
 */
export const SPENDING_EVENT_TYPE_INFO: Record<SpendingEventType, SpendingEventTypeInfo> = {
  PENDING: {
    label: 'Pending',
    description: 'Spending is pending, waiting to be processed.',
    color: 'blue',
    icon: '⏳'
  },
  ECO_REQUESTED: {
    label: 'ECO Requested',
    description: 'Expenditure Control Officer (ECO) approval has been requested.',
    color: 'orange',
    icon: '📤'
  },
  ECO_RECEIVED: {
    label: 'ECO Received',
    description: 'Expenditure Control Officer (ECO) approval has been received.',
    color: 'green',
    icon: '📥'
  },
  EXTERNAL_APPROVAL_REQUESTED: {
    label: 'External Approval Requested',
    description: 'External approval (outside the organization) has been requested.',
    color: 'orange',
    icon: '📨'
  },
  EXTERNAL_APPROVAL_RECEIVED: {
    label: 'External Approval Received',
    description: 'External approval has been received.',
    color: 'green',
    icon: '📬'
  },
  SECTION_32_PROVIDED: {
    label: 'Section 32 Provided',
    description: 'Section 32 certification (commitment authority) has been provided.',
    color: 'purple',
    icon: '📝'
  },
  RECEIVED_GOODS_SERVICES: {
    label: 'Received Goods/Services',
    description: 'Goods or services have been received.',
    color: 'green',
    icon: '📦'
  },
  SECTION_34_PROVIDED: {
    label: 'Section 34 Provided',
    description: 'Section 34 certification (performance authority) has been provided.',
    color: 'green',
    icon: '✅'
  },
  CREDIT_CARD_CLEARED: {
    label: 'Credit Card Cleared',
    description: 'Credit card payment has cleared.',
    color: 'success',
    icon: '💳'
  },
  CANCELLED: {
    label: 'Cancelled',
    description: 'Spending has been cancelled.',
    color: 'red',
    icon: '🚫'
  },
  ON_HOLD: {
    label: 'On Hold',
    description: 'Spending is on hold.',
    color: 'yellow',
    icon: '⏸️'
  }
};

/**
 * Spending event interface.
 */
export interface SpendingEvent {
  /** Unique identifier for the event */
  id: number;

  /** ID of the parent spending item */
  spendingItemId: number;

  /** Name of the parent spending item */
  spendingItemName?: string;

  /** Event type */
  eventType: SpendingEventType;

  /** Date of the event (ISO date string) */
  eventDate: string;

  /** Optional comment/description */
  comment?: string;

  /** Username who created the event */
  createdBy?: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Whether the event is active */
  active?: boolean;
}

/**
 * Request payload for creating/updating a spending event.
 */
export interface SpendingEventRequest {
  /** Event type */
  eventType?: SpendingEventType;

  /** Date of the event (ISO date string, defaults to today) */
  eventDate?: string;

  /** Optional comment/description */
  comment?: string;
}
