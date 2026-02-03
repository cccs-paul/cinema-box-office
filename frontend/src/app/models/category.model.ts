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
 * Funding type enum representing the allowed money allocation types for a category.
 */
export type FundingType = 'CAP_ONLY' | 'OM_ONLY' | 'BOTH';

/**
 * Display labels for funding types.
 */
export const FUNDING_TYPE_LABELS: Record<FundingType, string> = {
  'CAP_ONLY': 'Capital Only',
  'OM_ONLY': 'O&M Only',
  'BOTH': 'Both CAP & OM'
};

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
 * Default categories with their funding types.
 */
export const DEFAULT_CATEGORIES: Array<{ name: string; fundingType: FundingType }> = [
  { name: 'Compute', fundingType: 'BOTH' },
  { name: 'GPUs', fundingType: 'BOTH' },
  { name: 'Storage', fundingType: 'BOTH' },
  { name: 'Software Licenses', fundingType: 'OM_ONLY' },
  { name: 'Hardware Support/Licensing', fundingType: 'OM_ONLY' },
  { name: 'Small Procurement', fundingType: 'OM_ONLY' },
  { name: 'Contractors', fundingType: 'OM_ONLY' }
];

/**
 * Helper function to check if a category allows CAP amounts.
 */
export function categoryAllowsCap(category: Category): boolean {
  return category.allowsCap ?? (category.fundingType === 'CAP_ONLY' || category.fundingType === 'BOTH');
}

/**
 * Helper function to check if a category allows OM amounts.
 */
export function categoryAllowsOm(category: Category): boolean {
  return category.allowsOm ?? (category.fundingType === 'OM_ONLY' || category.fundingType === 'BOTH');
}
