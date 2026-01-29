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
}
