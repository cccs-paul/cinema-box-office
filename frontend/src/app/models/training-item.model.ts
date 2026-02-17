/**
 * Training Item model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */

/**
 * Enum for training item status values.
 */
export type TrainingItemStatus =
  | 'PLANNED'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Enum for training type values.
 */
export type TrainingType =
  | 'COURSE'
  | 'CONFERENCE'
  | 'CERTIFICATION'
  | 'WORKSHOP'
  | 'SEMINAR'
  | 'ONLINE'
  | 'OTHER';

/**
 * Status information for display.
 */
export interface TrainingStatusInfo {
  label: string;
  color: string;
  icon: string;
}

/**
 * Map of training status to display information.
 */
export const TRAINING_STATUS_INFO: Record<TrainingItemStatus, TrainingStatusInfo> = {
  PLANNED: { label: 'Planned', color: 'secondary', icon: '📋' },
  APPROVED: { label: 'Approved', color: 'primary', icon: '✅' },
  IN_PROGRESS: { label: 'In Progress', color: 'warning', icon: '🔄' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'danger', icon: '❌' }
};

/**
 * Map of training type to display information.
 */
export const TRAINING_TYPE_INFO: Record<TrainingType, TrainingStatusInfo> = {
  COURSE: { label: 'Course', color: 'blue', icon: '📚' },
  CONFERENCE: { label: 'Conference', color: 'purple', icon: '🎤' },
  CERTIFICATION: { label: 'Certification', color: 'green', icon: '📜' },
  WORKSHOP: { label: 'Workshop', color: 'orange', icon: '🔧' },
  SEMINAR: { label: 'Seminar', color: 'teal', icon: '🎓' },
  ONLINE: { label: 'Online', color: 'cyan', icon: '💻' },
  OTHER: { label: 'Other', color: 'gray', icon: '📝' }
};

/**
 * Money allocation for a training item.
 * Training items only use O&M allocations.
 */
export interface TrainingMoneyAllocation {
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
 * Training Item interface representing a training activity within a fiscal year.
 */
export interface TrainingItem {
  /** Unique identifier */
  id: number;

  /** Name of the training item */
  name: string;

  /** Description */
  description?: string;

  /** Training provider */
  provider?: string;

  /** Reference number */
  referenceNumber?: string;

  /** Estimated cost */
  estimatedCost: number | null;

  /** Actual cost */
  actualCost: number | null;

  /** Current status */
  status: TrainingItemStatus;

  /** Type of training */
  trainingType: TrainingType;

  /** Currency code (ISO 4217) */
  currency: string;

  /** Exchange rate to CAD */
  exchangeRate: number | null;

  /** Start date of training */
  startDate?: string | null;

  /** End date of training */
  endDate?: string | null;

  /** Location of training */
  location?: string;

  /** Employee name */
  employeeName?: string;

  /** Number of participants */
  numberOfParticipants: number;

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Whether the item is active */
  active: boolean;

  /** Money allocations (OM only) */
  moneyAllocations?: TrainingMoneyAllocation[];

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
