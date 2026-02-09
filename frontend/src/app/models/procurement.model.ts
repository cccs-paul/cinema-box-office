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
  PENDING_QUOTES: { label: 'Pending Quotes', color: 'yellow', icon: '⏳' },
  QUOTES_RECEIVED: { label: 'Quotes Received', color: 'blue', icon: '📥' },
  UNDER_REVIEW: { label: 'Under Review', color: 'orange', icon: '🔍' },
  APPROVED: { label: 'Approved', color: 'green', icon: '✅' },
  PO_ISSUED: { label: 'PO Issued', color: 'purple', icon: '📋' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' }
};

/**
 * Tracking status type for procurement items.
 * Indicates the overall health/risk of the procurement.
 */
export type TrackingStatus =
  | 'PLANNING'
  | 'ON_TRACK'
  | 'AT_RISK'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Map of tracking status to display information.
 */
export const TRACKING_STATUS_INFO: Record<TrackingStatus, ProcurementStatusInfo> = {
  PLANNING: { label: 'Planning', color: 'gray', icon: '📋' },
  ON_TRACK: { label: 'On Track', color: 'green', icon: '✅' },
  AT_RISK: { label: 'At Risk', color: 'orange', icon: '⚠️' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' }
};

/**
 * Procurement type for procurement items.
 * Indicates whether the procurement was RC-initiated or centrally managed.
 */
export type ProcurementType =
  | 'RC_INITIATED'
  | 'CENTRALLY_MANAGED';

/**
 * Map of procurement type to display information.
 */
export const PROCUREMENT_TYPE_INFO: Record<ProcurementType, ProcurementStatusInfo> = {
  RC_INITIATED: { label: 'RC Initiated', color: 'blue', icon: '🏢' },
  CENTRALLY_MANAGED: { label: 'Centrally Managed', color: 'purple', icon: '🏛️' }
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

  /** Quote amount for CAP (capital) funding */
  amountCap?: number | null;

  /** Quote amount for OM (operations & maintenance) funding */
  amountOm?: number | null;

  /** Currency code */
  currency: string;

  /** Exchange rate to CAD (only required when currency is not CAD) */
  exchangeRate?: number | null;

  /** CAP amount converted to CAD (only required when currency is not CAD) */
  amountCapCad?: number | null;

  /** OM amount converted to CAD (only required when currency is not CAD) */
  amountOmCad?: number | null;

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

  /** User who created the quote */
  createdBy?: string;

  /** User who last modified the quote */
  modifiedBy?: string;

  /** Attached files */
  files?: ProcurementQuoteFile[];

  /** Number of attached files */
  fileCount?: number;
}

/**
 * Procurement item interface.
 * Note: Status is now tracked via ProcurementEvent records - use the most recent
 * event's newStatus to get the current status.
 */
export interface ProcurementItem {
  /** Unique identifier for the procurement item */
  id: number;

  /** Purchase Requisition number (optional) */
  purchaseRequisition?: string;

  /** Purchase Order number */
  purchaseOrder?: string;

  /** Name of the procurement item */
  name: string;

  /** Description */
  description?: string;

  /** Vendor name */
  vendor?: string;

  /** Final price in the specified currency */
  finalPrice?: number;

  /** Currency code for the final price (defaults to CAD) */
  finalPriceCurrency?: string;

  /** Exchange rate to convert final price to CAD (required when finalPriceCurrency is not CAD) */
  finalPriceExchangeRate?: number;

  /** Final price converted to CAD (required when finalPriceCurrency is not CAD) */
  finalPriceCad?: number;

  /** Quoted or estimated price in the specified currency */
  quotedPrice?: number;

  /** Currency code for the quoted price (defaults to CAD) */
  quotedPriceCurrency?: string;

  /** Exchange rate to convert quoted price to CAD (required when quotedPriceCurrency is not CAD) */
  quotedPriceExchangeRate?: number;

  /** Quoted price converted to CAD (required when quotedPriceCurrency is not CAD) */
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

  /** Current status derived from most recent procurement event */
  currentStatus?: ProcurementItemStatus;

  /** Tracking status indicating overall health/risk */
  trackingStatus?: TrackingStatus;

  /** Procurement type (RC Initiated or Centrally Managed) */
  procurementType?: ProcurementType;

  /** IDs of linked spending items */
  linkedSpendingItemIds?: number[];

  /** Names of linked spending items for display */
  linkedSpendingItemNames?: string[];
}

/**
 * Enum for procurement event types.
 */
export type ProcurementEventType =
  | 'ACKNOWLEDGED_BY_PROCUREMENT'
  | 'ADDITIONAL_DOCUMENT_REQUESTED'
  | 'ADDITIONAL_SECTION_32_REQUESTED'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'CONTRACT_AMENDED'
  | 'CONTRACT_AWARDED'
  | 'CREATED'
  | 'DELIVERED'
  | 'EXERCISED_OPTION'
  | 'FULL_INVOICE_RECEIVED'
  | 'FULL_INVOICE_SIGNED'
  | 'GOODS_RECEIVED'
  | 'INVOICED'
  | 'MONTHLY_INVOICE_RECEIVED'
  | 'MONTHLY_INVOICE_SIGNED'
  | 'NOT_STARTED'
  | 'NOTE_ADDED'
  | 'OTHER'
  | 'PACKAGE_SENT_TO_PROCUREMENT'
  | 'PARTIAL_INVOICE_RECEIVED'
  | 'PARTIAL_INVOICE_SIGNED'
  | 'PAUSED'
  | 'PAYMENT_MADE'
  | 'PO_ISSUED'
  | 'QUOTE'
  | 'QUOTE_RECEIVED'
  | 'QUOTE_REJECTED'
  | 'QUOTE_SELECTED'
  | 'RECEIVED_NEW_INVOICE'
  | 'REJECTED_INVOICE'
  | 'RETROACTIVE_AWARD_LETTER'
  | 'SAM_ACKNOWLEDGEMENT_RECEIVED'
  | 'SAM_ACKNOWLEDGEMENT_REQUESTED'
  | 'STATUS_CHANGE'
  | 'STILL_IN_PROCUREMENT'
  | 'UPDATE'
  | 'WITH_SECURITY';

/**
 * Event type display information.
 */
export interface EventTypeInfo {
  label: string;
  color: string;
  icon: string;
  description?: string;
}

/**
 * Map of event type to display information.
 */
export const EVENT_TYPE_INFO: Record<ProcurementEventType, EventTypeInfo> = {
  ACKNOWLEDGED_BY_PROCUREMENT: { label: 'Acknowledged by Procurement', color: 'blue', icon: '✅', description: 'Procurement has acknowledged receipt of the request' },
  ADDITIONAL_DOCUMENT_REQUESTED: { label: 'Additional Document Requested', color: 'orange', icon: '📄', description: 'Additional documentation has been requested' },
  ADDITIONAL_SECTION_32_REQUESTED: { label: 'Additional Section 32 Requested', color: 'orange', icon: '📋', description: 'An additional Section 32 approval has been requested' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '🚫', description: 'The procurement has been cancelled' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️', description: 'The procurement has been completed' },
  CONTRACT_AMENDED: { label: 'Contract Amended', color: 'purple', icon: '📝', description: 'The contract has been amended' },
  CONTRACT_AWARDED: { label: 'Contract Awarded', color: 'green', icon: '🏆', description: 'A contract has been awarded' },
  CREATED: { label: 'Created', color: 'blue', icon: '🆕', description: 'The procurement item was created' },
  DELIVERED: { label: 'Delivered', color: 'green', icon: '📦', description: 'Goods or services have been delivered' },
  EXERCISED_OPTION: { label: 'Exercised Option', color: 'purple', icon: '🔄', description: 'A contract option has been exercised' },
  FULL_INVOICE_RECEIVED: { label: 'Full Invoice Received', color: 'yellow', icon: '🧾', description: 'A full invoice has been received' },
  FULL_INVOICE_SIGNED: { label: 'Full Invoice Signed', color: 'green', icon: '✍️', description: 'A full invoice has been signed' },
  GOODS_RECEIVED: { label: 'Goods Received', color: 'green', icon: '📦', description: 'Goods have been received' },
  INVOICED: { label: 'Invoiced', color: 'yellow', icon: '🧾', description: 'An invoice has been issued' },
  MONTHLY_INVOICE_RECEIVED: { label: 'Monthly Invoice Received', color: 'yellow', icon: '📅', description: 'A monthly invoice has been received' },
  MONTHLY_INVOICE_SIGNED: { label: 'Monthly Invoice Signed', color: 'green', icon: '✍️', description: 'A monthly invoice has been signed' },
  NOT_STARTED: { label: 'Not Started', color: 'gray', icon: '⏸️', description: 'The procurement has not yet started' },
  NOTE_ADDED: { label: 'Note Added', color: 'gray', icon: '📝', description: 'A note has been added' },
  OTHER: { label: 'Other', color: 'gray', icon: '📌', description: 'Other event type' },
  PACKAGE_SENT_TO_PROCUREMENT: { label: 'Package Sent to Procurement', color: 'blue', icon: '📤', description: 'The procurement package has been sent' },
  PARTIAL_INVOICE_RECEIVED: { label: 'Partial Invoice Received', color: 'yellow', icon: '🧾', description: 'A partial invoice has been received' },
  PARTIAL_INVOICE_SIGNED: { label: 'Partial Invoice Signed', color: 'green', icon: '✍️', description: 'A partial invoice has been signed' },
  PAUSED: { label: 'Paused', color: 'orange', icon: '⏸️', description: 'The procurement has been paused' },
  PAYMENT_MADE: { label: 'Payment Made', color: 'green', icon: '💰', description: 'A payment has been made' },
  PO_ISSUED: { label: 'PO Issued', color: 'purple', icon: '📋', description: 'A purchase order has been issued' },
  QUOTE: { label: 'Quote', color: 'blue', icon: '💬', description: 'A quote has been provided' },
  QUOTE_RECEIVED: { label: 'Quote Received', color: 'blue', icon: '📥', description: 'A quote has been received' },
  QUOTE_REJECTED: { label: 'Quote Rejected', color: 'red', icon: '❌', description: 'A quote has been rejected' },
  QUOTE_SELECTED: { label: 'Quote Selected', color: 'green', icon: '✅', description: 'A quote has been selected' },
  RECEIVED_NEW_INVOICE: { label: 'Received New Invoice', color: 'yellow', icon: '🧾', description: 'A new invoice has been received' },
  REJECTED_INVOICE: { label: 'Rejected Invoice', color: 'red', icon: '❌', description: 'An invoice has been rejected' },
  RETROACTIVE_AWARD_LETTER: { label: 'Retroactive Award Letter', color: 'purple', icon: '📜', description: 'A retroactive award letter has been issued' },
  SAM_ACKNOWLEDGEMENT_RECEIVED: { label: 'SAM Acknowledgement Received', color: 'green', icon: '✅', description: 'SAM acknowledgement has been received' },
  SAM_ACKNOWLEDGEMENT_REQUESTED: { label: 'SAM Acknowledgement Requested', color: 'blue', icon: '📨', description: 'SAM acknowledgement has been requested' },
  STATUS_CHANGE: { label: 'Status Change', color: 'orange', icon: '🔄', description: 'The status has changed' },
  STILL_IN_PROCUREMENT: { label: 'Still in Procurement', color: 'blue', icon: '⏳', description: 'The item is still in the procurement process' },
  UPDATE: { label: 'Update', color: 'blue', icon: '🔄', description: 'An update has been made' },
  WITH_SECURITY: { label: 'With Security', color: 'orange', icon: '🔒', description: 'The item is with security for review' }
};

/**
 * Procurement event file interface.
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
  formattedFileSize: string;

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

  /** Files attached to this event */
  files?: ProcurementEventFile[];

  /** Number of files attached */
  fileCount?: number;

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
