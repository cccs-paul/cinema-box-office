/**
 * Travel Item Service for myRC application.
 * Handles all API communication for travel item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TravelItem, TravelMoneyAllocation } from '../models/travel-item.model';

/**
 * Request body for creating a travel item.
 */
export interface TravelItemCreateRequest {
  name: string;
  description?: string;
  travelAuthorizationNumber?: string;
  referenceNumber?: string;
  destination?: string;
  purpose?: string;
  estimatedCost?: number | null;
  actualCost?: number | null;
  status?: string;
  travelType?: string;
  currency?: string;
  exchangeRate?: number | null;
  departureDate?: string | null;
  returnDate?: string | null;
  travellerName?: string;
  numberOfTravellers?: number;
  moneyAllocations?: TravelMoneyAllocation[];
}

/**
 * Request body for updating a travel item.
 */
export interface TravelItemUpdateRequest {
  name?: string;
  description?: string;
  travelAuthorizationNumber?: string;
  referenceNumber?: string;
  destination?: string;
  purpose?: string;
  estimatedCost?: number | null;
  actualCost?: number | null;
  status?: string;
  travelType?: string;
  currency?: string;
  exchangeRate?: number | null;
  departureDate?: string | null;
  returnDate?: string | null;
  travellerName?: string;
  numberOfTravellers?: number;
  moneyAllocations?: TravelMoneyAllocation[];
}

/**
 * Service for managing travel items within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class TravelItemService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/travel-items`;
  }

  constructor(private http: HttpClient) {}

  /**
   * Get all travel items for a fiscal year.
   */
  getTravelItemsByFY(rcId: number, fyId: number): Observable<TravelItem[]> {
    return this.http.get<TravelItem[]>(this.baseUrl(rcId, fyId), { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific travel item by ID.
   */
  getTravelItem(rcId: number, fyId: number, travelItemId: number): Observable<TravelItem> {
    return this.http.get<TravelItem>(`${this.baseUrl(rcId, fyId)}/${travelItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new travel item.
   */
  createTravelItem(rcId: number, fyId: number, request: TravelItemCreateRequest): Observable<TravelItem> {
    return this.http.post<TravelItem>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing travel item.
   */
  updateTravelItem(rcId: number, fyId: number, travelItemId: number, request: TravelItemUpdateRequest): Observable<TravelItem> {
    return this.http.put<TravelItem>(`${this.baseUrl(rcId, fyId)}/${travelItemId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a travel item.
   */
  deleteTravelItem(rcId: number, fyId: number, travelItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${travelItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update the status of a travel item.
   */
  updateStatus(rcId: number, fyId: number, travelItemId: number, status: string): Observable<TravelItem> {
    return this.http.put<TravelItem>(`${this.baseUrl(rcId, fyId)}/${travelItemId}/status`, { status }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get money allocations for a travel item.
   */
  getMoneyAllocations(rcId: number, fyId: number, travelItemId: number): Observable<TravelMoneyAllocation[]> {
    return this.http.get<TravelMoneyAllocation[]>(`${this.baseUrl(rcId, fyId)}/${travelItemId}/allocations`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update money allocations for a travel item.
   */
  updateMoneyAllocations(rcId: number, fyId: number, travelItemId: number, allocations: TravelMoneyAllocation[]): Observable<TravelItem> {
    return this.http.put<TravelItem>(`${this.baseUrl(rcId, fyId)}/${travelItemId}/allocations`, allocations, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.status === 0) {
        errorMessage = 'Unable to connect to server';
      } else if (error.status === 401) {
        errorMessage = 'Unauthorized: Please log in again';
      } else if (error.status === 403) {
        errorMessage = 'Access denied';
      } else if (error.status === 404) {
        errorMessage = 'Travel item not found';
      } else {
        errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('TravelItemService error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
