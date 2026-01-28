/**
 * Fiscal Year model for myRC application.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 * @license MIT
 */
export interface FiscalYear {
  /** Unique identifier for the fiscal year */
  id: number;

  /** Name of the fiscal year */
  name: string;

  /** Description of the fiscal year */
  description: string;

  /** Whether the fiscal year is active */
  active: boolean;

  /** ID of the parent responsibility centre */
  responsibilityCentreId: number;

  /** Whether to show category filter in funding/spending pages */
  showCategoryFilter: boolean;

  /** Whether to group items by category in funding/spending pages */
  groupByCategory: boolean;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Request payload for creating a new fiscal year.
 */
export interface FiscalYearCreateRequest {
  /** Name of the fiscal year */
  name: string;

  /** Description of the fiscal year */
  description?: string;
}

/**
 * Request payload for updating a fiscal year.
 */
export interface FiscalYearUpdateRequest {
  /** Name of the fiscal year */
  name?: string;

  /** Description of the fiscal year */
  description?: string;
}

/**
 * Request payload for updating fiscal year display settings.
 */
export interface DisplaySettingsRequest {
  /** Whether to show category filter in funding/spending pages */
  showCategoryFilter?: boolean;

  /** Whether to group items by category in funding/spending pages */
  groupByCategory?: boolean;
}
