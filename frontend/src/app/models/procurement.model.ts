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
 * Enum for tracking status values.
 * Used to indicate the overall health/risk of the procurement.
 */
export type TrackingStatus = 'PLANNING' | 'ON_TRACK' | 'AT_RISK' | 'COMPLETED' | 'CANCELLED';

/**
 * Map of tracking status to display information.
 */
export const TRACKING_STATUS_INFO: Record<TrackingStatus, ProcurementStatusInfo> = {
  PLANNING: { label: 'Planning', color: 'gray', icon: '📋' },
  ON_TRACK: { label: 'On Track', color: 'green', icon: '✅' },
  AT_RISK: { label: 'At Risk', color: 'yellow', icon: '⚠️' },
  COMPLETED: { label: 'Completed', color: 'blue', icon: '🏁' },
  CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' }
};

/**
 * Enum for procurement type values.
 * Indicates whether the procurement is initiated by the RC or centrally managed.
 */
export type ProcurementType = 'RC_INITIATED' | 'CENTRALLY_MANAGED';

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

  /** Username of the user who created this quote */
  createdBy?: string;

  /** Username of the user who last modified this quote */
  modifiedBy?: string;

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

  /** IDs of linked spending items */
  linkedSpendingItemIds?: number[];

  /** Names of linked spending items for display */
  linkedSpendingItemNames?: string[];

  /** Tracking status indicating the overall health/risk of the procurement */
  trackingStatus?: TrackingStatus;

  /** Procurement type indicating whether the item is RC initiated or centrally managed */
  procurementType?: ProcurementType;
}

/**
 * Enum for procurement event types.
 */
export type ProcurementEventType =
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
  | 'CONTRACT_AMENDED'
  | 'ADDITIONAL_DOCUMENT_REQUESTED'
  | 'ADDITIONAL_SECTION_32_REQUESTED'
  | 'REJECTED_INVOICE'
  | 'RECEIVED_NEW_INVOICE'
  | 'RETROACTIVE_AWARD_LETTER'
  | 'STILL_IN_PROCUREMENT'
  | 'WITH_SECURITY'
  | 'EXERCISED_OPTION'
  | 'UPDATE';

/**
 * Event type display information.
 */
export interface EventTypeInfo {
  label: string;
  description: string;
  color: string;
  icon: string;
}

/**
 * Map of event type to display information.
 */
export const EVENT_TYPE_INFO: Record<ProcurementEventType, EventTypeInfo> = {
  NOT_STARTED: { label: 'Not Started', description: 'Procurement process has not yet started.', color: 'gray', icon: '⏸️' },
  QUOTE: { label: 'Quote', description: 'Quote/estimate obtained from vendor.', color: 'blue', icon: '💬' },
  SAM_ACKNOWLEDGEMENT_REQUESTED: { label: 'SAM Acknowledgement requested', description: 'Requested acknowledgement from Software Asset Management team.', color: 'orange', icon: '📤' },
  SAM_ACKNOWLEDGEMENT_RECEIVED: { label: 'SAM Acknowledgement received', description: 'Received acknowledgement from Software Asset Management team.', color: 'green', icon: '📥' },
  PACKAGE_SENT_TO_PROCUREMENT: { label: 'Package sent to Procurement', description: 'Documentation package sent to Procurement.', color: 'blue', icon: '📦' },
  ACKNOWLEDGED_BY_PROCUREMENT: { label: 'Acknowledged by Procurement / In Progress', description: 'Procurement has accepted the package and provided a Purchase Order.', color: 'purple', icon: '✅' },
  PAUSED: { label: 'Paused', description: 'Procurement process put on pause. Reason should be detailed in comments.', color: 'yellow', icon: '⏸️' },
  CANCELLED: { label: 'Cancelled', description: 'Procurement process cancelled. Reason should be detailed in comments.', color: 'red', icon: '🚫' },
  CONTRACT_AWARDED: { label: 'Contract Awarded', description: 'Procurement process completed and contract awarded, awaiting delivery if applicable.', color: 'green', icon: '🏆' },
  GOODS_RECEIVED: { label: 'Goods received', description: 'The goods of the procurement has been received at the receiving building.', color: 'green', icon: '📦' },
  FULL_INVOICE_RECEIVED: { label: 'Full Invoice received', description: 'Invoice for all goods received.', color: 'blue', icon: '🧾' },
  PARTIAL_INVOICE_RECEIVED: { label: 'Partial Invoice received', description: 'Invoice for some, but not all goods received.', color: 'orange', icon: '🧾' },
  MONTHLY_INVOICE_RECEIVED: { label: 'Monthly Invoice received', description: 'Invoice for last delivery period of services received.', color: 'blue', icon: '📅' },
  FULL_INVOICE_SIGNED: { label: 'Full Invoice signed', description: 'Invoice for all goods/services received signed for Section 34 and submitted to Accounts Payable.', color: 'green', icon: '✍️' },
  PARTIAL_INVOICE_SIGNED: { label: 'Partial Invoice signed', description: 'Invoice for some, but not all goods signed for Section 34 and submitted to Accounts Payable.', color: 'orange', icon: '✍️' },
  MONTHLY_INVOICE_SIGNED: { label: 'Monthly Invoice signed', description: 'Invoice for last delivery period of services signed for Section 34 and submitted to Accounts Payable.', color: 'green', icon: '📅' },
  CONTRACT_AMENDED: { label: 'Contract Amended', description: 'Procurement process completed and existing contract amended, awaiting delivery if applicable.', color: 'purple', icon: '📝' },
  ADDITIONAL_DOCUMENT_REQUESTED: { label: 'Additional Document Requested', description: 'Additional documentation has been requested for the procurement.', color: 'orange', icon: '📄' },
  ADDITIONAL_SECTION_32_REQUESTED: { label: 'Additional Section 32 Requested', description: 'Additional Section 32 authorization has been requested.', color: 'orange', icon: '📋' },
  REJECTED_INVOICE: { label: 'Rejected Invoice', description: 'Invoice has been rejected and returned for correction.', color: 'red', icon: '❌' },
  RECEIVED_NEW_INVOICE: { label: 'Received New Invoice', description: 'A new or corrected invoice has been received.', color: 'blue', icon: '🧾' },
  RETROACTIVE_AWARD_LETTER: { label: 'Retroactive Award Letter', description: 'A retroactive award letter has been issued.', color: 'purple', icon: '📜' },
  STILL_IN_PROCUREMENT: { label: 'Still in Procurement', description: 'Item is still being processed by Procurement.', color: 'yellow', icon: '⏳' },
  WITH_SECURITY: { label: 'With Security', description: 'Procurement is currently with Security for review.', color: 'orange', icon: '🔒' },
  EXERCISED_OPTION: { label: 'Exercised Option', description: 'A contract option has been exercised.', color: 'green', icon: '✔️' },
  UPDATE: { label: 'Update', description: 'General update or note on the procurement.', color: 'blue', icon: '📝' }
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
