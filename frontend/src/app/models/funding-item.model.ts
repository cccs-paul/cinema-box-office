/**
 * Funding Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 * @license MIT
 */

/**
 * Money allocation for a funding item.
 * Represents the CAP and OM amounts for a specific money type.
 */
export interface MoneyAllocation {
  /** ID of the money type */
  moneyId: number;
  
  /** Code of the money type (e.g., AB, OA, WCF) */
  moneyCode: string;
  
  /** Name of the money type */
  moneyName: string;
  
  /** Capital (CAP) amount */
  capAmount: number;
  
  /** Operations and Maintenance (OM) amount */
  omAmount: number;
}

/**
 * Enum for funding item status values.
 */
export type FundingItemStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'ACTIVE' | 'CLOSED';

/**
 * Funding Item interface representing a budget allocation within a fiscal year.
 */
export interface FundingItem {
  /** Unique identifier for the funding item */
  id: number;

  /** Name of the funding item */
  name: string;

  /** Description of the funding item */
  description: string;

  /** Budget amount allocated for this funding item */
  budgetAmount: number | null;

  /** Current status of the funding item */
  status: FundingItemStatus;

  /** Currency code (ISO 4217) for this funding item */
  currency: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate: number | null;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Name of the parent fiscal year */
  fiscalYearName?: string;

  /** ID of the responsibility centre */
  responsibilityCentreId?: number;

  /** Name of the responsibility centre */
  responsibilityCentreName?: string;

  /** Whether the funding item is active */
  active: boolean;

  /** Money allocations for this funding item */
  moneyAllocations?: MoneyAllocation[];

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Request payload for creating a new funding item.
 */
export interface FundingItemCreateRequest {
  /** Name of the funding item */
  name: string;

  /** Description of the funding item */
  description?: string;

  /** Budget amount allocated for this funding item */
  budgetAmount?: number;

  /** Initial status of the funding item */
  status?: FundingItemStatus;

  /** Currency code (ISO 4217) for this funding item */
  currency?: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate?: number;

  /** Money allocations for this funding item */
  moneyAllocations?: MoneyAllocation[];
}

/**
 * Request payload for updating a funding item.
 */
export interface FundingItemUpdateRequest {
  /** Name of the funding item */
  name?: string;

  /** Description of the funding item */
  description?: string;

  /** Budget amount allocated for this funding item */
  budgetAmount?: number;

  /** Status of the funding item */
  status?: FundingItemStatus;

  /** Currency code (ISO 4217) for this funding item */
  currency?: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate?: number;

  /** Money allocations for this funding item */
  moneyAllocations?: MoneyAllocation[];
}

/**
 * Get display label for a status.
 */
export function getStatusLabel(status: FundingItemStatus): string {
  switch (status) {
    case 'DRAFT':
      return 'Draft';
    case 'PENDING':
      return 'Pending Approval';
    case 'APPROVED':
      return 'Approved';
    case 'ACTIVE':
      return 'Active';
    case 'CLOSED':
      return 'Closed';
    default:
      return status;
  }
}

/**
 * Get CSS class for a status badge.
 */
export function getStatusClass(status: FundingItemStatus): string {
  switch (status) {
    case 'DRAFT':
      return 'status-draft';
    case 'PENDING':
      return 'status-pending';
    case 'APPROVED':
      return 'status-approved';
    case 'ACTIVE':
      return 'status-active';
    case 'CLOSED':
      return 'status-closed';
    default:
      return 'status-draft';
  }
}
