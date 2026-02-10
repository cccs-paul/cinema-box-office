/**
 * Audit Service for myRC application.
 * Provides read-only access to audit trail data.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 * @license MIT
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuditEvent } from '../models/audit-event.model';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private apiUrl = '/api/responsibility-centres';

  constructor(private http: HttpClient) {}

  /**
   * Get all audit events for a responsibility centre (owner only).
   *
   * @param rcId the RC ID
   * @returns observable of audit events
   */
  getAuditEventsForRC(rcId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(
      `${this.apiUrl}/${rcId}/audit`,
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Get audit events filtered by fiscal year.
   *
   * @param rcId the RC ID
   * @param fiscalYearId the fiscal year ID
   * @returns observable of audit events
   */
  getAuditEventsForFiscalYear(rcId: number, fiscalYearId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(
      `${this.apiUrl}/${rcId}/audit/fiscal-year/${fiscalYearId}`,
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors.
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
    }
    console.error('AuditService error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
