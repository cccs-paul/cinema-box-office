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
  | 'PLANNING' 
  | 'COMMITTED' 
  | 'COMPLETED' 
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

  /** ECO estimated amount (for standalone items not linked to procurement) */
  ecoAmount: number | null;

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

  /** Optional link to a procurement item (null if discrete spending item) */
  procurementItemId?: number | null;

  /** Name of the linked procurement item */
  procurementItemName?: string | null;

  /** Final price from procurement item (if available) */
  procurementFinalPrice?: number | null;

  /** Quoted/estimated price from procurement item */
  procurementQuotedPrice?: number | null;

  /** Currency of procurement prices */
  procurementPriceCurrency?: string | null;

  /** Final price in CAD (converted) */
  procurementFinalPriceCad?: number | null;

  /** Quoted price in CAD (converted) */
  procurementQuotedPriceCad?: number | null;

  /** Count of tracking events for this item (for non-procurement items) */
  eventCount?: number;

  /** Most recent event type for this item (for non-procurement items) */
  mostRecentEventType?: string;

  /** Most recent event date for this item (for non-procurement items) */
  mostRecentEventDate?: string;

  /** Tracking status from linked procurement item (for procurement-linked items) */
  procurementTrackingStatus?: string;

  /** Most recent event type from linked procurement item */
  procurementMostRecentEventType?: string;

  /** Most recent event date from linked procurement item */
  procurementMostRecentEventDate?: string;

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
 * Check if a spending item is linked to procurement.
 * @param item The spending item to check
 * @returns true if linked to procurement, false if discrete
 */
export function isLinkedToProcurement(item: SpendingItem): boolean {
  return item.procurementItemId != null;
}

/**
 * Spending item status display information.
 */
export const SPENDING_STATUS_INFO: Record<SpendingItemStatus, { label: string; color: string }> = {
  PLANNING: { label: 'Planning', color: 'secondary' },
  COMMITTED: { label: 'Committed', color: 'primary' },
  COMPLETED: { label: 'Completed', color: 'success' },
  CANCELLED: { label: 'Cancelled', color: 'danger' }
};
