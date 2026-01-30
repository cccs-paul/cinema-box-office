/**
 * Money Service for myRC application.
 * Handles all API communication for money type management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Money, MoneyCreateRequest, MoneyUpdateRequest, MoneyReorderRequest } from '../models/money.model';

/**
 * Service for managing money types within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class MoneyService {
  private readonly baseUrl = '/api/responsibility-centres';

  constructor(private http: HttpClient) {}

  /**
   * Get all money types for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of money types array
   */
  getMoniesByFiscalYear(rcId: number, fyId: number): Observable<Money[]> {
    return this.http.get<Money[]>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific money type by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param moneyId The money type ID
   * @returns Observable of the money type
   */
  getMoney(rcId: number, fyId: number, moneyId: number): Observable<Money> {
    return this.http.get<Money>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new money type for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The money type creation request
   * @returns Observable of the created money type
   */
  createMoney(rcId: number, fyId: number, request: MoneyCreateRequest): Observable<Money> {
    return this.http.post<Money>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing money type.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param moneyId The money type ID
   * @param request The update request
   * @returns Observable of the updated money type
   */
  updateMoney(rcId: number, fyId: number, moneyId: number, request: MoneyUpdateRequest): Observable<Money> {
    return this.http.put<Money>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a money type.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param moneyId The money type ID
   * @returns Observable of void
   */
  deleteMoney(rcId: number, fyId: number, moneyId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Reorder money types within a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The reorder request containing ordered money IDs
   * @returns Observable of void
   */
  reorderMonies(rcId: number, fyId: number, request: MoneyReorderRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${rcId}/fiscal-years/${fyId}/monies/reorder`, request)
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
      // Server-side error - try to extract message from response body
      if (error.error && typeof error.error === 'object' && error.error.message) {
        // Backend returned an ErrorResponse with a message
        errorMessage = error.error.message;
      } else if (typeof error.error === 'string' && error.error.length > 0) {
        // Backend returned a plain string error
        errorMessage = error.error;
      } else {
        // Fall back to status-based messages
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
            errorMessage = 'Money type not found.';
            break;
          case 409:
            errorMessage = 'A money type with this code already exists for this fiscal year.';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = `Server error: ${error.status}`;
        }
      }
    }

    console.error('MoneyService error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
