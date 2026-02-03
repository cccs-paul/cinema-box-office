/**
 * Procurement Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-28
 * @license MIT
 */

/**
 * Enum for procurement item status values.
 * Status is now tracked via ProcurementEvent records, not stored directly on the item.
 * 
 * Status Flow:
 * - DRAFT: Procurement item created, requirements being finalized.
 * - PENDING_QUOTES: Waiting for quotes from vendors.
 * - QUOTES_RECEIVED: Quotes received from vendors.
 * - UNDER_REVIEW: Quotes under review.
 * - APPROVED: Quote approved, preparing PO.
 * - PO_ISSUED: Purchase Order issued to vendor.
 * - COMPLETED: Goods/services received and paid.
 * - CANCELLED: Procurement cancelled.
 */
export type ProcurementItemStatus =
  | 'DRAFT'
  | 'PENDING_QUOTES'
  | 'QUOTES_RECEIVED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'PO_ISSUED'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Status information for display.
 */
export interface ProcurementStatusInfo {
  label: string;
  color: string;
  icon: string;
}

/**
 * Map of procurement status to display information.
 */
export const PROCUREMENT_STATUS_INFO: Record<ProcurementItemStatus, ProcurementStatusInfo> = {
  DRAFT: { label: 'Draft', color: 'gray', icon: '📝' },
  PENDING_QUOTES: { label: 'Pending Quotes', color: 'blue', icon: '📄' },
  QUOTES_RECEIVED: { label: 'Quotes Received', color: 'cyan', icon: '📬' },
  UNDER_REVIEW: { label: 'Under Review', color: 'yellow', icon: '🔍' },
  APPROVED: { label: 'Approved', color: 'green', icon: '✅' },
  PO_ISSUED: { label: 'PO Issued', color: 'purple', icon: '📋' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' }
};

/**
 * Enum for quote status values.
 */
export type QuoteStatus =
  | 'PENDING'
  | 'UNDER_REVIEW'
  | 'SELECTED'
  | 'REJECTED';

/**
 * Map of quote status to display information.
 */
export const QUOTE_STATUS_INFO: Record<QuoteStatus, ProcurementStatusInfo> = {
  PENDING: { label: 'Pending', color: 'yellow', icon: '⏳' },
  UNDER_REVIEW: { label: 'Under Review', color: 'orange', icon: '🔍' },
  SELECTED: { label: 'Selected', color: 'green', icon: '✅' },
  REJECTED: { label: 'Rejected', color: 'red', icon: '❌' }
};

/**
 * Quote file interface representing an uploaded file.
 */
export interface ProcurementQuoteFile {
  /** Unique identifier for the file */
  id: number;

  /** Original filename */
  fileName: string;

  /** MIME content type */
  contentType: string;

  /** File size in bytes */
  fileSize: number;

  /** Human-readable file size */
  formattedFileSize: string;

  /** Optional description */
  description?: string;

  /** ID of the parent quote */
  quoteId: number;

  /** Vendor name of the parent quote */
  quoteVendorName?: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Download URL for the file */
  downloadUrl?: string;
}

/**
 * Event file interface representing an uploaded file attached to a tracking event.
 */
export interface ProcurementEventFile {
  /** Unique identifier for the file */
  id: number;

  /** Original filename */
  fileName: string;

  /** MIME content type */
  contentType: string;

  /** File size in bytes */
  fileSize: number;

  /** Human-readable file size */
  formattedFileSize?: string;

  /** Optional description */
  description?: string;

  /** ID of the parent event */
  eventId: number;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Download URL for the file */
  downloadUrl?: string;
}

/**
 * Quote interface representing a vendor quote.
 */
export interface ProcurementQuote {
  /** Unique identifier for the quote */
  id: number;

  /** Vendor name */
  vendorName: string;

  /** Vendor contact information */
  vendorContact?: string;

  /** Quote reference number */
  quoteReference?: string;

  /** Quoted amount */
  amount: number | null;

  /** Currency code */
  currency: string;

  /** Date when quote was received */
  receivedDate?: string;

  /** Date when quote expires */
  expiryDate?: string;

  /** Notes about the quote */
  notes?: string;

  /** Quote status */
  status: QuoteStatus;

  /** Whether this quote is selected */
  selected: boolean;

  /** ID of the parent procurement item */
  procurementItemId: number;

  /** Name of the parent procurement item */
  procurementItemName?: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Attached files */
  files?: ProcurementQuoteFile[];

  /** Number of attached files */
  fileCount?: number;
}

/**
 * Procurement item interface.
 * Note: Status is now tracked via ProcurementEvent records.
 * Use currentStatus to get the most recent status from events.
 */
export interface ProcurementItem {
  /** Unique identifier for the procurement item */
  id: number;

  /** Purchase Requisition number */
  purchaseRequisition?: string;

  /** Purchase Order number */
  purchaseOrder?: string;

  /** Name of the procurement item */
  name: string;

  /** Description */
  description?: string;

  /** Current status (derived from most recent tracking event) */
  currentStatus?: ProcurementItemStatus;

  /** Vendor name */
  vendor?: string;

  /** Final price in the specified currency */
  finalPrice?: number;

  /** Currency code for the final price (defaults to CAD) */
  finalPriceCurrency?: string;

  /** Exchange rate to convert final price to CAD */
  finalPriceExchangeRate?: number;

  /** Final price converted to CAD (calculated when finalPriceCurrency is not CAD) */
  finalPriceCad?: number;

  /** Quoted/estimated price in the specified currency */
  quotedPrice?: number;

  /** Currency code for the quoted price (defaults to CAD) */
  quotedPriceCurrency?: string;

  /** Exchange rate to convert quoted price to CAD */
  quotedPriceExchangeRate?: number;

  /** Quoted price converted to CAD (calculated when quotedPriceCurrency is not CAD) */
  quotedPriceCad?: number;

  /** Contract number */
  contractNumber?: string;

  /** Contract start date (ISO date string) */
  contractStartDate?: string;

  /** Contract end date (ISO date string) */
  contractEndDate?: string;

  /** Whether procurement is completed */
  procurementCompleted?: boolean;

  /** Date when procurement was completed (ISO date string) */
  procurementCompletedDate?: string;

  /** ID of the category */
  categoryId?: number;

  /** Name of the category */
  categoryName?: string;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Name of the parent fiscal year */
  fiscalYearName?: string;

  /** ID of the responsibility centre */
  responsibilityCentreId?: number;

  /** Name of the responsibility centre */
  responsibilityCentreName?: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Quotes associated with this item */
  quotes?: ProcurementQuote[];

  /** Number of quotes */
  quoteCount?: number;

  /** Events associated with this item */
  events?: ProcurementEvent[];

  /** Number of events */
  eventCount?: number;
}

/**
 * Enum for procurement event types.
 */
export type ProcurementEventType =
  | 'CREATED'
  | 'STATUS_CHANGE'
  | 'NOTE_ADDED'
  | 'QUOTE_RECEIVED'
  | 'QUOTE_SELECTED'
  | 'QUOTE_REJECTED'
  | 'PO_ISSUED'
  | 'DELIVERED'
  | 'INVOICED'
  | 'PAYMENT_MADE'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'OTHER';

/**
 * Event type display information.
 */
export interface EventTypeInfo {
  label: string;
  color: string;
  icon: string;
}

/**
 * Map of event type to display information.
 */
export const EVENT_TYPE_INFO: Record<ProcurementEventType, EventTypeInfo> = {
  CREATED: { label: 'Created', color: 'blue', icon: '🆕' },
  STATUS_CHANGE: { label: 'Status Change', color: 'orange', icon: '🔄' },
  NOTE_ADDED: { label: 'Note Added', color: 'gray', icon: '📝' },
  QUOTE_RECEIVED: { label: 'Quote Received', color: 'blue', icon: '📥' },
  QUOTE_SELECTED: { label: 'Quote Selected', color: 'green', icon: '✅' },
  QUOTE_REJECTED: { label: 'Quote Rejected', color: 'red', icon: '❌' },
  PO_ISSUED: { label: 'PO Issued', color: 'purple', icon: '📋' },
  DELIVERED: { label: 'Delivered', color: 'green', icon: '📦' },
  INVOICED: { label: 'Invoiced', color: 'yellow', icon: '🧾' },
  PAYMENT_MADE: { label: 'Payment Made', color: 'green', icon: '💰' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '🚫' },
  OTHER: { label: 'Other', color: 'gray', icon: '📌' }
};

/**
 * Procurement event interface.
 */
export interface ProcurementEvent {
  /** Unique identifier for the event */
  id: number;

  /** ID of the parent procurement item */
  procurementItemId: number;

  /** Name of the parent procurement item */
  procurementItemName?: string;

  /** Event type */
  eventType: ProcurementEventType;

  /** Date of the event (ISO date string) */
  eventDate: string;

  /** Optional comment/description */
  comment?: string;

  /** Old status for status change events */
  oldStatus?: string;

  /** New status for status change events */
  newStatus?: string;

  /** Username who created the event */
  createdBy?: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;

  /** Whether the event is active */
  active?: boolean;

  /** Files attached to this event */
  files?: ProcurementEventFile[];

  /** Number of attached files */
  fileCount?: number;
}

/**
 * Request payload for creating/updating a procurement event.
 */
export interface ProcurementEventRequest {
  /** Event type */
  eventType?: ProcurementEventType;

  /** Date of the event (ISO date string, defaults to today) */
  eventDate?: string;

  /** Optional comment/description */
  comment?: string;

  /** Old status for status change events */
  oldStatus?: string;

  /** New status for status change events */
  newStatus?: string;
}
