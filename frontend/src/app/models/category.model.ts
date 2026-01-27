/**
 * Category model for myRC application.
 * Categories are used to group both funding and spending items.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 * @license MIT
 */

/**
 * Category interface representing a category for grouping funding and spending items.
 */
export interface Category {
  /** Unique identifier for the category */
  id: number;

  /** Name of the category */
  name: string;

  /** Description of the category */
  description: string;

  /** Whether this is a default category (read-only) */
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
 * Default categories.
 */
export const DEFAULT_CATEGORIES: string[] = [
  'Compute',
  'GPUs',
  'Storage',
  'Software Licenses',
  'Small Procurement',
  'Contractors'
];
