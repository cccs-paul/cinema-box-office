/**
 * Funding Item Service for myRC application.
 * Handles all API communication for funding item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FundingItem, FundingItemCreateRequest, FundingItemUpdateRequest } from '../models/funding-item.model';

/**
 * Service for managing funding items within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class FundingItemService {
  private readonly baseUrl = '/api/fiscal-years';

  constructor(private http: HttpClient) {}

  /**
   * Get all funding items for a fiscal year.
   *
   * @param fyId The fiscal year ID
   * @returns Observable of funding items array
   */
  getFundingItemsByFY(fyId: number): Observable<FundingItem[]> {
    return this.http.get<FundingItem[]>(`${this.baseUrl}/${fyId}/funding-items`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific funding item by ID.
   *
   * @param fyId The fiscal year ID
   * @param fiId The funding item ID
   * @returns Observable of the funding item
   */
  getFundingItem(fyId: number, fiId: number): Observable<FundingItem> {
    return this.http.get<FundingItem>(`${this.baseUrl}/${fyId}/funding-items/${fiId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new funding item for a fiscal year.
   *
   * @param fyId The fiscal year ID
   * @param request The funding item creation request
   * @returns Observable of the created funding item
   */
  createFundingItem(fyId: number, request: FundingItemCreateRequest): Observable<FundingItem> {
    return this.http.post<FundingItem>(`${this.baseUrl}/${fyId}/funding-items`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing funding item.
   *
   * @param fyId The fiscal year ID
   * @param fiId The funding item ID
   * @param request The update request
   * @returns Observable of the updated funding item
   */
  updateFundingItem(fyId: number, fiId: number, request: FundingItemUpdateRequest): Observable<FundingItem> {
    return this.http.put<FundingItem>(`${this.baseUrl}/${fyId}/funding-items/${fiId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a funding item.
   *
   * @param fyId The fiscal year ID
   * @param fiId The funding item ID
   * @returns Observable of void
   */
  deleteFundingItem(fyId: number, fiId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${fyId}/funding-items/${fiId}`, { withCredentials: true })
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
          errorMessage = error.error?.message || 'Invalid request data';
          break;
        case 401:
          errorMessage = 'Your session has expired. Please log in again.';
          break;
        case 403:
          errorMessage = 'You do not have permission to perform this action';
          break;
        case 404:
          errorMessage = 'Funding item not found';
          break;
        case 409:
          errorMessage = 'A funding item with this name already exists';
          break;
        default:
          errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
