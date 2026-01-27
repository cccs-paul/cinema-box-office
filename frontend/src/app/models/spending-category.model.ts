/**
 * Spending Category model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */

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

  /** Whether the category is active */
  active: boolean;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Default spending categories.
 */
export const DEFAULT_SPENDING_CATEGORIES: string[] = [
  'Compute',
  'GPUs',
  'Storage',
  'Software Licenses',
  'Small Procurement',
  'Contractors'
];
