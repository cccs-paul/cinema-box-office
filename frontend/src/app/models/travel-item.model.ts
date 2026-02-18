/**
 * Travel Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */

/**
 * Enum for travel item status values.
 */
export type TravelItemStatus =
  | 'PLANNED'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Enum for travel type values.
 */
export type TravelType =
  | 'DOMESTIC'
  | 'NORTH_AMERICA'
  | 'INTERNATIONAL'
  | 'LOCAL';

/**
 * Enum for traveller approval status values.
 */
export type TravelApprovalStatus =
  | 'PLANNED'
  | 'TAAC_ESTIMATE_SUBMITTED'
  | 'TAAC_ESTIMATE_APPROVED'
  | 'TAAC_FINAL_SUBMITTED'
  | 'TAAC_FINAL_APPROVED'
  | 'CANCELLED';

/**
 * Status information for display.
 */
export interface TravelStatusInfo {
  label: string;
  color: string;
  icon: string;
}

/**
 * Map of travel status to display information.
 */
export const TRAVEL_STATUS_INFO: Record<TravelItemStatus, TravelStatusInfo> = {
  PLANNED: { label: 'Planned', color: 'secondary', icon: '📋' },
  APPROVED: { label: 'Approved', color: 'primary', icon: '✅' },
  IN_PROGRESS: { label: 'In Progress', color: 'warning', icon: '🔄' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'danger', icon: '❌' }
};

/**
 * Map of travel type to display information.
 */
export const TRAVEL_TYPE_INFO: Record<TravelType, TravelStatusInfo> = {
  DOMESTIC: { label: 'Domestic', color: 'blue', icon: '🏠' },
  NORTH_AMERICA: { label: 'North America', color: 'teal', icon: '🌎' },
  INTERNATIONAL: { label: 'International', color: 'purple', icon: '🌍' },
  LOCAL: { label: 'Local', color: 'green', icon: '📍' }
};

/**
 * Map of approval status to display information.
 */
export const TRAVEL_APPROVAL_STATUS_INFO: Record<TravelApprovalStatus, TravelStatusInfo> = {
  PLANNED: { label: 'Planned', color: 'secondary', icon: '📋' },
  TAAC_ESTIMATE_SUBMITTED: { label: 'TAAC Estimate Submitted', color: 'info', icon: '📤' },
  TAAC_ESTIMATE_APPROVED: { label: 'TAAC Estimate Approved', color: 'primary', icon: '✅' },
  TAAC_FINAL_SUBMITTED: { label: 'TAAC Final Submitted', color: 'warning', icon: '📤' },
  TAAC_FINAL_APPROVED: { label: 'TAAC Final Approved', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'danger', icon: '❌' }
};

/**
 * Money allocation for a travel item.
 * Travel items only use O&M allocations.
 */
export interface TravelMoneyAllocation {
  /** ID of the allocation (if existing) */
  id?: number;

  /** ID of the money type */
  moneyId: number;

  /** Name of the money type */
  moneyName?: string;

  /** Code of the money type */
  moneyCode?: string;

  /** Whether this is the default money type */
  isDefault?: boolean;

  /** Operations and Maintenance (O&M) amount */
  omAmount: number;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Travel traveller within a travel item.
 */
export interface TravelTraveller {
  /** Unique identifier */
  id?: number;

  /** Traveller name */
  name: string;

  /** TAAC number */
  taac?: string;

  /** Estimated cost */
  estimatedCost: number | null;

  /** Final cost */
  finalCost: number | null;

  /** Currency code for estimated cost */
  estimatedCurrency: string;

  /** Exchange rate to CAD for estimated cost */
  estimatedExchangeRate: number | null;

  /** Currency code for final cost */
  finalCurrency: string;

  /** Exchange rate to CAD for final cost */
  finalExchangeRate: number | null;

  /** Approval status */
  approvalStatus: TravelApprovalStatus;

  /** Estimated cost converted to CAD */
  estimatedCostCad?: number | null;

  /** Final cost converted to CAD */
  finalCostCad?: number | null;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Travel Item interface representing a travel activity within a fiscal year.
 */
export interface TravelItem {
  /** Unique identifier */
  id: number;

  /** Name of the travel item */
  name: string;

  /** Description */
  description?: string;

  /** EMAP number */
  emap?: string;

  /** Destination */
  destination?: string;

  /** Purpose of travel */
  purpose?: string;

  /** Current status */
  status: TravelItemStatus;

  /** Type of travel */
  travelType: TravelType;

  /** Departure date */
  departureDate?: string | null;

  /** Return date */
  returnDate?: string | null;

  /** Number of travellers (computed from travellers list) */
  numberOfTravellers: number;

  /** Travellers list */
  travellers?: TravelTraveller[];

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Whether the item is active */
  active: boolean;

  /** Money allocations (OM only) */
  moneyAllocations?: TravelMoneyAllocation[];

  /** Total of all money allocation O&M amounts */
  moneyAllocationTotalOm?: number | null;

  /** Estimated cost converted to CAD (computed from travellers) */
  estimatedCostCad?: number | null;

  /** Actual cost converted to CAD (computed from travellers) */
  actualCostCad?: number | null;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}
