/**
 * Spending Item Service for myRC application.
 * Handles all API communication for spending item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SpendingItem, SpendingMoneyAllocation } from '../models/spending-item.model';

/**
 * Request body for creating a spending item.
 */
export interface SpendingItemCreateRequest {
  name: string;
  description?: string;
  vendor?: string;
  referenceNumber?: string;
  amount?: number;
  status?: string;
  currency?: string;
  exchangeRate?: number;
  categoryId: number;
  moneyAllocations?: SpendingMoneyAllocation[];
}

/**
 * Request body for updating a spending item.
 */
export interface SpendingItemUpdateRequest {
  name?: string;
  description?: string;
  vendor?: string;
  referenceNumber?: string;
  amount?: number;
  status?: string;
  currency?: string;
  exchangeRate?: number;
  categoryId?: number;
  moneyAllocations?: SpendingMoneyAllocation[];
}

/**
 * Service for managing spending items within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class SpendingItemService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`;
  }

  constructor(private http: HttpClient) {}

  /**
   * Get all spending items for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId Optional category ID to filter by
   * @returns Observable of spending items array
   */
  getSpendingItemsByFY(rcId: number, fyId: number, categoryId?: number): Observable<SpendingItem[]> {
    let params = new HttpParams();
    if (categoryId !== undefined) {
      params = params.set('categoryId', categoryId.toString());
    }
    return this.http.get<SpendingItem[]>(this.baseUrl(rcId, fyId), { params, withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific spending item by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of the spending item
   */
  getSpendingItem(rcId: number, fyId: number, spendingItemId: number): Observable<SpendingItem> {
    return this.http.get<SpendingItem>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new spending item for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The spending item creation request
   * @returns Observable of the created spending item
   */
  createSpendingItem(rcId: number, fyId: number, request: SpendingItemCreateRequest): Observable<SpendingItem> {
    return this.http.post<SpendingItem>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param request The update request
   * @returns Observable of the updated spending item
   */
  updateSpendingItem(rcId: number, fyId: number, spendingItemId: number, request: SpendingItemUpdateRequest): Observable<SpendingItem> {
    return this.http.put<SpendingItem>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of void
   */
  deleteSpendingItem(rcId: number, fyId: number, spendingItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update the status of a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param status The new status
   * @returns Observable of the updated spending item
   */
  updateStatus(rcId: number, fyId: number, spendingItemId: number, status: string): Observable<SpendingItem> {
    return this.http.put<SpendingItem>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}/status`, { status }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get money allocations for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of money allocations array
   */
  getMoneyAllocations(rcId: number, fyId: number, spendingItemId: number): Observable<SpendingMoneyAllocation[]> {
    return this.http.get<SpendingMoneyAllocation[]>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}/allocations`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update money allocations for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param allocations The new money allocations
   * @returns Observable of the updated spending item
   */
  updateMoneyAllocations(rcId: number, fyId: number, spendingItemId: number, allocations: SpendingMoneyAllocation[]): Observable<SpendingItem> {
    return this.http.put<SpendingItem>(`${this.baseUrl(rcId, fyId)}/${spendingItemId}/allocations`, allocations, { withCredentials: true })
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
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.status === 0) {
        errorMessage = 'Unable to connect to server';
      } else if (error.status === 401) {
        errorMessage = 'Unauthorized: Please log in again';
      } else if (error.status === 403) {
        errorMessage = 'Access denied';
      } else if (error.status === 404) {
        errorMessage = 'Spending item not found';
      } else {
        errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('SpendingItemService error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
