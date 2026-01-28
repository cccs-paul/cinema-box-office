/**
 * Fiscal Year Service for myRC application.
 * Handles all API communication for fiscal year management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FiscalYear, FiscalYearCreateRequest, FiscalYearUpdateRequest, DisplaySettingsRequest } from '../models/fiscal-year.model';

/**
 * Service for managing fiscal years within responsibility centres.
 */
@Injectable({
  providedIn: 'root'
})
export class FiscalYearService {
  private readonly baseUrl = '/api/responsibility-centres';

  constructor(private http: HttpClient) {}

  /**
   * Get all fiscal years for a responsibility centre.
   *
   * @param rcId The responsibility centre ID
   * @returns Observable of fiscal years array
   */
  getFiscalYearsByRC(rcId: number): Observable<FiscalYear[]> {
    return this.http.get<FiscalYear[]>(`${this.baseUrl}/${rcId}/fiscal-years`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific fiscal year by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of the fiscal year
   */
  getFiscalYear(rcId: number, fyId: number): Observable<FiscalYear> {
    return this.http.get<FiscalYear>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new fiscal year for a responsibility centre.
   *
   * @param rcId The responsibility centre ID
   * @param request The fiscal year creation request
   * @returns Observable of the created fiscal year
   */
  createFiscalYear(rcId: number, request: FiscalYearCreateRequest): Observable<FiscalYear> {
    return this.http.post<FiscalYear>(`${this.baseUrl}/${rcId}/fiscal-years`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The update request
   * @returns Observable of the updated fiscal year
   */
  updateFiscalYear(rcId: number, fyId: number, request: FiscalYearUpdateRequest): Observable<FiscalYear> {
    return this.http.put<FiscalYear>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of void
   */
  deleteFiscalYear(rcId: number, fyId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update display settings for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The display settings request
   * @returns Observable of the updated fiscal year
   */
  updateDisplaySettings(rcId: number, fyId: number, request: DisplaySettingsRequest): Observable<FiscalYear> {
    return this.http.patch<FiscalYear>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/display-settings`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors.
   *
   * @param error The HTTP error response
   * @returns Observable that throws an error
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = 'Invalid request. Please check your input.';
          break;
        case 401:
          errorMessage = 'Unauthorized. Please log in again.';
          break;
        case 403:
          errorMessage = 'Access denied. You do not have permission to perform this action.';
          break;
        case 404:
          errorMessage = 'Fiscal year not found.';
          break;
        case 409:
          errorMessage = 'A fiscal year with this name already exists.';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later.';
          break;
        default:
          errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('FiscalYearService error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
