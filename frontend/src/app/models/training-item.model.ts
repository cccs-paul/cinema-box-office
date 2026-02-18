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
  | 'COURSE_TRAINING'
  | 'CONFERENCE_REGISTRATION'
  | 'OTHER';

/**
 * Enum for training format values.
 */
export type TrainingFormat = 'IN_PERSON' | 'ONLINE';

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
  COURSE_TRAINING: { label: 'Course / Training', color: 'blue', icon: '📚' },
  CONFERENCE_REGISTRATION: { label: 'Conference Registration', color: 'purple', icon: '🎤' },
  OTHER: { label: 'Other', color: 'gray', icon: '📝' }
};

/**
 * Map of training format to display information.
 */
export const TRAINING_FORMAT_INFO: Record<TrainingFormat, TrainingStatusInfo> = {
  IN_PERSON: { label: 'In Person', color: 'green', icon: '🏢' },
  ONLINE: { label: 'Online', color: 'cyan', icon: '💻' }
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
 * Enum for training participant status values.
 */
export type TrainingParticipantStatus =
  | 'PLANNED'
  | 'ECO_CREATED'
  | 'REGISTERED'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Map of participant status to display information.
 */
export const PARTICIPANT_STATUS_INFO: Record<TrainingParticipantStatus, TrainingStatusInfo> = {
  PLANNED: { label: 'Planned', color: 'secondary', icon: '📋' },
  ECO_CREATED: { label: 'ECO Created', color: 'info', icon: '📄' },
  REGISTERED: { label: 'Registered', color: 'primary', icon: '✅' },
  COMPLETED: { label: 'Completed', color: 'success', icon: '✔️' },
  CANCELLED: { label: 'Cancelled', color: 'danger', icon: '❌' }
};

/**
 * Training participant within a training item.
 */
export interface TrainingParticipant {
  /** Unique identifier */
  id?: number;

  /** Participant name */
  name: string;

  /** ECO number (per participant) */
  eco?: string;

  /** Participant status */
  status: TrainingParticipantStatus;

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

  /** Current status */
  status: TrainingItemStatus;

  /** Type of training */
  trainingType: TrainingType;

  /** Format (in-person or online) */
  format?: TrainingFormat;

  /** Start date of training */
  startDate?: string | null;

  /** End date of training */
  endDate?: string | null;

  /** Location of training */
  location?: string;

  /** Number of participants (computed from participants list) */
  numberOfParticipants: number;

  /** Participants list */
  participants?: TrainingParticipant[];

  /** ID of the parent fiscal year */
  fiscalYearId: number;

  /** Whether the item is active */
  active: boolean;

  /** Money allocations (OM only) */
  moneyAllocations?: TrainingMoneyAllocation[];

  /** Total of all money allocation O&M amounts */
  moneyAllocationTotalOm?: number | null;

  /** Estimated cost converted to CAD (computed from participants) */
  estimatedCostCad?: number | null;

  /** Actual cost converted to CAD (computed from participants) */
  actualCostCad?: number | null;

  /** Creation timestamp */
  createdAt?: string;

  /** Last update timestamp */
  updatedAt?: string;
}
