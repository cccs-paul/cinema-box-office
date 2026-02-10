/**
 * Audit Event model for myRC application.
 * Read-only representation of audit trail data.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 * @license MIT
 */

/**
 * Audit event outcome values.
 */
export type AuditOutcome = 'PENDING' | 'SUCCESS' | 'FAILURE';

/**
 * Audit event DTO returned from the API.
 */
export interface AuditEvent {
  id: number;
  username: string;
  action: string;
  entityType: string;
  entityId: number | null;
  entityName: string | null;
  rcId: number;
  rcName: string;
  fiscalYearId: number | null;
  fiscalYearName: string | null;
  parameters: string | null;
  httpMethod: string | null;
  endpoint: string | null;
  userAgent: string | null;
  ipAddress: string | null;
  outcome: AuditOutcome;
  errorMessage: string | null;
  clonedFromAuditId: number | null;
  createdAt: string;
}
