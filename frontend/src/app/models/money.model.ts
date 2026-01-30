/**
 * Money model for myRC application.
 * Represents a money type configured at the Fiscal Year level.
 * Each money has two parts: Capital (CAP) and O&M (Operations & Maintenance).
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 * @license MIT
 */

/**
 * Represents a money type in the system.
 */
export interface Money {
  /** Unique identifier for the money */
  id: number;

  /** Short code for the money type (e.g., "AB", "OA", "WCF") */
  code: string;

  /** Descriptive name for the money type (e.g., "A-Base", "Operating Allotment") */
  name: string;

  /** Optional detailed description */
  description?: string;

  /** Whether this is the system default money (AB) */
  isDefault: boolean;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Name of the parent fiscal year */
  fiscalYearName?: string;

  /** ID of the parent responsibility centre */
  responsibilityCentreId?: number;

  /** Display order for sorting */
  displayOrder: number;

  /** Whether the money type is active */
  active: boolean;

  /** Capital (CAP) label derived from code (e.g., "AB (CAP)") */
  capLabel: string;

  /** O&M label derived from code (e.g., "AB (O&M)") */
  omLabel: string;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}

/**
 * Request payload for creating a new money type.
 */
export interface MoneyCreateRequest {
  /** Short code for the money type (will be uppercased) */
  code: string;

  /** Descriptive name for the money type */
  name: string;

  /** Optional detailed description */
  description?: string;
}

/**
 * Request payload for updating a money type.
 */
export interface MoneyUpdateRequest {
  /** Short code for the money type (cannot change for default AB) */
  code?: string;

  /** Descriptive name for the money type */
  name?: string;

  /** Optional detailed description */
  description?: string;
}

/**
 * Request payload for reordering money types.
 */
export interface MoneyReorderRequest {
  /** Ordered list of money IDs in desired display order */
  moneyIds: number[];
}

/**
 * Represents the two parts of a money type.
 */
export enum MoneyPart {
  /** Capital portion */
  CAP = 'CAP',

  /** Operations & Maintenance portion */
  OM = 'OM'
}

/**
 * Helper function to get the display label for a money part.
 *
 * @param money The money object
 * @param part The money part (CAP or OM)
 * @returns The formatted label (e.g., "AB (CAP)")
 */
export function getMoneyPartLabel(money: Money, part: MoneyPart): string {
  return `${money.code} (${part})`;
}

/**
 * Helper function to parse a money label into code and part.
 *
 * @param label The money label (e.g., "AB (CAP)")
 * @returns Object with code and part, or null if invalid format
 */
export function parseMoneyLabel(label: string): { code: string; part: MoneyPart } | null {
  const match = label.match(/^(\w+)\s*\((CAP|OM)\)$/);
  if (match) {
    return {
      code: match[1],
      part: match[2] as MoneyPart
    };
  }
  return null;
}
