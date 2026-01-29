/**
 * Spending Category model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */

import { FundingType } from './category.model';

/**
 * Spending Category interface representing a category for grouping spending items.
 */
export interface SpendingCategory {
  /** Unique identifier for the category */
  id: number;

  /** Name of the category */
  name: string;

  /** Description of the category */
  description: string;

  /** Whether this is a default category */
  isDefault: boolean;

  /** Display order for sorting */
  displayOrder: number;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Allowed funding type for this category */
  fundingType: FundingType;

  /** Whether CAP amounts are allowed (derived from fundingType) */
  allowsCap: boolean;

  /** Whether OM amounts are allowed (derived from fundingType) */
  allowsOm: boolean;

  /** Whether the category is active */
  active: boolean;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Default spending categories with funding types.
 */
export const DEFAULT_SPENDING_CATEGORIES: Array<{ name: string; fundingType: FundingType }> = [
  { name: 'Compute', fundingType: 'BOTH' },
  { name: 'GPUs', fundingType: 'BOTH' },
  { name: 'Storage', fundingType: 'BOTH' },
  { name: 'Software Licenses', fundingType: 'OM_ONLY' },
  { name: 'Small Procurement', fundingType: 'OM_ONLY' },
  { name: 'Contractors', fundingType: 'OM_ONLY' }
];
