/**
 * Procurement Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 * @license MIT
 */

/**
 * Enum for procurement item status values.
 * 
 * Status Flow:
 * - NOT_STARTED: Procurement process has not yet started.
 * - QUOTE: Quote/estimate obtained from vendor.
 * - SAM_ACKNOWLEDGEMENT_REQUESTED: Requested acknowledgement from Software Asset Management team.
 * - SAM_ACKNOWLEDGEMENT_RECEIVED: Received acknowledgement from Software Asset Management team.
 * - PACKAGE_SENT_TO_PROCUREMENT: Documentation package sent to Procurement.
 * - ACKNOWLEDGED_BY_PROCUREMENT: Procurement has accepted the package and provided a Purchase Order.
 * - PAUSED: Procurement process put on pause. Reason should be detailed in comments.
 * - CANCELLED: Procurement process cancelled. Reason should be detailed in comments.
 * - CONTRACT_AWARDED: Procurement process completed and contract awarded, awaiting delivery if applicable.
 * - GOODS_RECEIVED: The goods of the procurement has been received at the receiving building.
 * - FULL_INVOICE_RECEIVED: Invoice for all goods received.
 * - PARTIAL_INVOICE_RECEIVED: Invoice for some, but not all goods received.
 * - MONTHLY_INVOICE_RECEIVED: Invoice for last delivery period of services received.
 * - FULL_INVOICE_SIGNED: Invoice for all goods/services received signed for Section 34 and submitted to Accounts Payable.
 * - PARTIAL_INVOICE_SIGNED: Invoice for some, but not all goods signed for Section 34 and submitted to Accounts Payable.
 * - MONTHLY_INVOICE_SIGNED: Invoice for last delivery period of services signed for Section 34 and submitted to Accounts Payable.
 * - CONTRACT_AMENDED: Procurement process completed and existing contract amended, awaiting delivery if applicable.
 */
export type ProcurementItemStatus =
  | 'NOT_STARTED'
  | 'QUOTE'
  | 'SAM_ACKNOWLEDGEMENT_REQUESTED'
  | 'SAM_ACKNOWLEDGEMENT_RECEIVED'
  | 'PACKAGE_SENT_TO_PROCUREMENT'
  | 'ACKNOWLEDGED_BY_PROCUREMENT'
  | 'PAUSED'
  | 'CANCELLED'
  | 'CONTRACT_AWARDED'
  | 'GOODS_RECEIVED'
  | 'FULL_INVOICE_RECEIVED'
  | 'PARTIAL_INVOICE_RECEIVED'
  | 'MONTHLY_INVOICE_RECEIVED'
  | 'FULL_INVOICE_SIGNED'
  | 'PARTIAL_INVOICE_SIGNED'
  | 'MONTHLY_INVOICE_SIGNED'
  | 'CONTRACT_AMENDED';

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
  NOT_STARTED: { label: 'Not Started', color: 'gray', icon: '⭕' },
  QUOTE: { label: 'Quote', color: 'blue', icon: '📄' },
  SAM_ACKNOWLEDGEMENT_REQUESTED: { label: 'SAM Acknowledgement Requested', color: 'yellow', icon: '📨' },
  SAM_ACKNOWLEDGEMENT_RECEIVED: { label: 'SAM Acknowledgement Received', color: 'blue', icon: '📬' },
  PACKAGE_SENT_TO_PROCUREMENT: { label: 'Package Sent to Procurement', color: 'purple', icon: '📦' },
  ACKNOWLEDGED_BY_PROCUREMENT: { label: 'Acknowledged by Procurement / In Progress', color: 'orange', icon: '⏳' },
  PAUSED: { label: 'Paused', color: 'warning', icon: '⏸️' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' },
  CONTRACT_AWARDED: { label: 'Contract Awarded', color: 'green', icon: '🏆' },
  GOODS_RECEIVED: { label: 'Goods Received', color: 'blue', icon: '📥' },
  FULL_INVOICE_RECEIVED: { label: 'Full Invoice Received', color: 'purple', icon: '🧾' },
  PARTIAL_INVOICE_RECEIVED: { label: 'Partial Invoice Received', color: 'purple', icon: '📋' },
  MONTHLY_INVOICE_RECEIVED: { label: 'Monthly Invoice Received', color: 'purple', icon: '📅' },
  FULL_INVOICE_SIGNED: { label: 'Full Invoice Signed', color: 'green', icon: '✅' },
  PARTIAL_INVOICE_SIGNED: { label: 'Partial Invoice Signed', color: 'green', icon: '✔️' },
  MONTHLY_INVOICE_SIGNED: { label: 'Monthly Invoice Signed', color: 'green', icon: '☑️' },
  CONTRACT_AMENDED: { label: 'Contract Amended', color: 'info', icon: '📝' }
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
 */
export interface ProcurementItem {
  /** Unique identifier for the procurement item */
  id: number;

  /** Purchase Requisition number */
  purchaseRequisition: string;

  /** Purchase Order number */
  purchaseOrder?: string;

  /** Name of the procurement item */
  name: string;

  /** Description */
  description?: string;

  /** Current status */
  status: ProcurementItemStatus;

  /** Currency code (defaults to CAD) */
  currency: string;

  /** Exchange rate to CAD */
  exchangeRate?: number;

  /** Preferred vendor name */
  preferredVendor?: string;

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
