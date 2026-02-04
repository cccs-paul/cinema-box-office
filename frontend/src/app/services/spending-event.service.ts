/**
 * Spending Event Service for myRC application.
 * Handles API operations for spending tracking events.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 * @license MIT
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { SpendingEvent, SpendingEventRequest } from '../models/spending-event.model';

@Injectable({
  providedIn: 'root'
})
export class SpendingEventService {
  private apiUrl = '/api/v1/responsibility-centres';

  constructor(private http: HttpClient) {}

  /**
   * Build the base URL for spending item events.
   */
  private baseUrl(rcId: number, fyId: number, spendingItemId: number): string {
    return `${this.apiUrl}/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/events`;
  }

  /**
   * Handle HTTP errors.
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
    }
    console.error('SpendingEventService error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  // ==========================
  // Event Operations
  // ==========================

  /**
   * Get all events for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of spending events
   */
  getEvents(rcId: number, fyId: number, spendingItemId: number): Observable<SpendingEvent[]> {
    return this.http.get<SpendingEvent[]>(this.baseUrl(rcId, fyId, spendingItemId), 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific event by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param eventId The event ID
   * @returns Observable of the event
   */
  getEvent(rcId: number, fyId: number, spendingItemId: number, eventId: number): Observable<SpendingEvent> {
    return this.http.get<SpendingEvent>(`${this.baseUrl(rcId, fyId, spendingItemId)}/${eventId}`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get the event count for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of the event count
   */
  getEventCount(rcId: number, fyId: number, spendingItemId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl(rcId, fyId, spendingItemId)}/count`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get the most recent event for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @returns Observable of the most recent event (or null if none)
   */
  getLatestEvent(rcId: number, fyId: number, spendingItemId: number): Observable<SpendingEvent | null> {
    return this.http.get<SpendingEvent | null>(`${this.baseUrl(rcId, fyId, spendingItemId)}/latest`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new event for a spending item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param request The event create request
   * @returns Observable of the created event
   */
  createEvent(rcId: number, fyId: number, spendingItemId: number, 
              request: SpendingEventRequest): Observable<SpendingEvent> {
    return this.http.post<SpendingEvent>(this.baseUrl(rcId, fyId, spendingItemId), 
      request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing event.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param eventId The event ID
   * @param request The event update request
   * @returns Observable of the updated event
   */
  updateEvent(rcId: number, fyId: number, spendingItemId: number, 
              eventId: number, request: SpendingEventRequest): Observable<SpendingEvent> {
    return this.http.put<SpendingEvent>(`${this.baseUrl(rcId, fyId, spendingItemId)}/${eventId}`, 
      request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete an event.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param spendingItemId The spending item ID
   * @param eventId The event ID
   * @returns Observable of void
   */
  deleteEvent(rcId: number, fyId: number, spendingItemId: number, eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId, spendingItemId)}/${eventId}`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }
}
