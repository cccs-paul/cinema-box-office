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

  /** Whether this is the default money type (AB) - optional, used for display */
  isDefault?: boolean;
}

/**
 * Enum for funding item source values.
 */
export type FundingSource = 'BUSINESS_PLAN' | 'ON_RAMP' | 'APPROVED_DEFICIT' | 'PRESSURE';

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

  /** Source of the funding item (mandatory) */
  source: FundingSource;

  /** Optional comments for this funding item */
  comments: string | null;

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

  /** ID of the category (optional) */
  categoryId?: number | null;

  /** Name of the category (optional) */
  categoryName?: string | null;

  /** Whether the funding item is active */
  active: boolean;

  /** Money allocations for this funding item */
  moneyAllocations?: MoneyAllocation[];

  /** Computed total CAP amount from all money allocations */
  totalCap?: number;

  /** Computed total OM amount from all money allocations */
  totalOm?: number;

  /** Computed total amount (CAP + OM) from all money allocations */
  totalAmount?: number;

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

  /** Source of the funding item */
  source?: FundingSource;

  /** Optional comments for this funding item */
  comments?: string;

  /** Currency code (ISO 4217) for this funding item */
  currency?: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate?: number;

  /** ID of the category (optional) */
  categoryId?: number | null;

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

  /** Source of the funding item */
  source?: FundingSource;

  /** Optional comments for this funding item */
  comments?: string;

  /** Currency code (ISO 4217) for this funding item */
  currency?: string;

  /** Exchange rate to CAD (required when currency is not CAD) */
  exchangeRate?: number;

  /** ID of the category (optional) */
  categoryId?: number | null;

  /** Money allocations for this funding item */
  moneyAllocations?: MoneyAllocation[];
}

/**
 * Get display label for a funding source.
 */
export function getSourceLabel(source: FundingSource): string {
  switch (source) {
    case 'BUSINESS_PLAN':
      return 'Business Plan';
    case 'ON_RAMP':
      return 'On-Ramp';
    case 'APPROVED_DEFICIT':
      return 'Approved Deficit';
    case 'PRESSURE':
      return 'Pressure';
    default:
      return source;
  }
}

/**
 * Get CSS class for a source badge.
 */
export function getSourceClass(source: FundingSource): string {
  switch (source) {
    case 'BUSINESS_PLAN':
      return 'source-business-plan';
    case 'ON_RAMP':
      return 'source-on-ramp';
    case 'APPROVED_DEFICIT':
      return 'source-approved-deficit';
    case 'PRESSURE':
      return 'source-pressure';
    default:
      return 'source-business-plan';
  }
}
