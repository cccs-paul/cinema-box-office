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
  | 'INTERNATIONAL'
  | 'LOCAL'
  | 'CONFERENCE'
  | 'TRAINING'
  | 'OTHER';

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
  INTERNATIONAL: { label: 'International', color: 'purple', icon: '🌍' },
  LOCAL: { label: 'Local', color: 'green', icon: '📍' },
  CONFERENCE: { label: 'Conference', color: 'orange', icon: '🎤' },
  TRAINING: { label: 'Training', color: 'teal', icon: '🎓' },
  OTHER: { label: 'Other', color: 'gray', icon: '📝' }
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
 * Travel Item interface representing a travel activity within a fiscal year.
 */
export interface TravelItem {
  /** Unique identifier */
  id: number;

  /** Name of the travel item */
  name: string;

  /** Description */
  description?: string;

  /** Travel authorization number */
  travelAuthorizationNumber?: string;

  /** Reference number */
  referenceNumber?: string;

  /** Destination */
  destination?: string;

  /** Purpose of travel */
  purpose?: string;

  /** Estimated cost */
  estimatedCost: number | null;

  /** Actual cost */
  actualCost: number | null;

  /** Current status */
  status: TravelItemStatus;

  /** Type of travel */
  travelType: TravelType;

  /** Currency code (ISO 4217) */
  currency: string;

  /** Exchange rate to CAD */
  exchangeRate: number | null;

  /** Departure date */
  departureDate?: string | null;

  /** Return date */
  returnDate?: string | null;

  /** Traveller name */
  travellerName?: string;

  /** Number of travellers */
  numberOfTravellers: number;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Whether the item is active */
  active: boolean;

  /** Money allocations (OM only) */
  moneyAllocations?: TravelMoneyAllocation[];

  /** Total of all money allocation O&M amounts */
  moneyAllocationTotalOm?: number | null;

  /** Estimated cost converted to CAD */
  estimatedCostCad?: number | null;

  /** Actual cost converted to CAD */
  actualCostCad?: number | null;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}
