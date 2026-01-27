/**
 * Spending Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */

/**
 * Money allocation for a spending item.
 * Represents the CAP and OM amounts for a specific money type.
 */
export interface SpendingMoneyAllocation {
  /** ID of the allocation (if existing) */
  id?: number;

  /** ID of the money type */
  moneyId: number;

  /** Name of the money type */
  moneyName: string;

  /** Whether this is the default money type (AB) */
  isDefault?: boolean;

  /** Capital (CAP) amount */
  capAmount: number;

  /** Operations and Maintenance (OM) amount */
  omAmount: number;

  /** Total amount (capAmount + omAmount) */
  totalAmount?: number;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Enum for spending item status values.
 */
export type SpendingItemStatus = 
  | 'DRAFT' 
  | 'PENDING' 
  | 'APPROVED' 
  | 'COMMITTED' 
  | 'PAID' 
  | 'CANCELLED';

/**
 * Spending Item interface representing an expenditure within a fiscal year.
 */
export interface SpendingItem {
  /** Unique identifier for the spending item */
  id: number;

  /** Name of the spending item */
  name: string;

  /** Description of the spending item */
  description: string;

  /** Vendor name */
  vendor: string;

  /** Reference number (PO number, invoice number, etc.) */
  referenceNumber: string;

  /** Total amount of the spending item */
  amount: number | null;

  /** Current status of the spending item */
  status: SpendingItemStatus;

  /** Currency code (ISO 4217) for this spending item */
  currency: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate: number | null;

  /** ID of the spending category */
  categoryId: number;

  /** Name of the spending category */
  categoryName?: string;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Name of the parent fiscal year */
  fiscalYearName?: string;

  /** ID of the responsibility centre */
  responsibilityCentreId?: number;

  /** Name of the responsibility centre */
  responsibilityCentreName?: string;

  /** Whether the spending item is active */
  active: boolean;

  /** Money allocations for this spending item */
  moneyAllocations?: SpendingMoneyAllocation[];

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Spending item status display information.
 */
export const SPENDING_STATUS_INFO: Record<SpendingItemStatus, { label: string; color: string }> = {
  DRAFT: { label: 'Draft', color: 'secondary' },
  PENDING: { label: 'Pending', color: 'warning' },
  APPROVED: { label: 'Approved', color: 'info' },
  COMMITTED: { label: 'Committed', color: 'primary' },
  PAID: { label: 'Paid', color: 'success' },
  CANCELLED: { label: 'Cancelled', color: 'danger' }
};
