/**
 * Procurement Service for myRC application.
 * Handles all API communication for procurement item, quote, and file management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  ProcurementItem,
  ProcurementQuote,
  ProcurementQuoteFile,
  ProcurementItemStatus,
  QuoteStatus,
  ProcurementEvent,
  ProcurementEventRequest,
  ProcurementEventType
} from '../models/procurement.model';

/**
 * Request body for creating a procurement item.
 */
export interface ProcurementItemCreateRequest {
  purchaseRequisition: string;
  purchaseOrder?: string;
  name: string;
  description?: string;
  status?: ProcurementItemStatus;
  currency?: string;
  exchangeRate?: number | null;
  preferredVendor?: string;
  contractNumber?: string;
  contractStartDate?: string;
  contractEndDate?: string;
  procurementCompleted?: boolean;
  procurementCompletedDate?: string;
}

/**
 * Request body for creating a quote.
 */
export interface QuoteCreateRequest {
  vendorName: string;
  vendorContact?: string;
  quoteReference?: string;
  amount?: number;
  currency?: string;
  receivedDate?: string;
  expiryDate?: string;
  notes?: string;
}

/**
 * Service for managing procurement items, quotes, and files.
 */
@Injectable({
  providedIn: 'root'
})
export class ProcurementService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/procurement-items`;
  }

  constructor(private http: HttpClient) {}

  // ==========================
  // Procurement Item Methods
  // ==========================

  /**
   * Get all procurement items for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param status Optional status to filter by
   * @param search Optional search term
   * @returns Observable of procurement items array
   */
  getProcurementItems(rcId: number, fyId: number, status?: string, search?: string): Observable<ProcurementItem[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<ProcurementItem[]>(this.baseUrl(rcId, fyId), { params, withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific procurement item by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param includeQuotes Whether to include quotes
   * @returns Observable of the procurement item
   */
  getProcurementItem(rcId: number, fyId: number, procurementItemId: number, includeQuotes = false): Observable<ProcurementItem> {
    let params = new HttpParams();
    if (includeQuotes) {
      params = params.set('includeQuotes', 'true');
    }
    return this.http.get<ProcurementItem>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}`, { params, withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The creation request
   * @returns Observable of the created procurement item
   */
  createProcurementItem(rcId: number, fyId: number, request: ProcurementItemCreateRequest): Observable<ProcurementItem> {
    return this.http.post<ProcurementItem>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param request The update request
   * @returns Observable of the updated procurement item
   */
  updateProcurementItem(rcId: number, fyId: number, procurementItemId: number, request: Partial<ProcurementItemCreateRequest>): Observable<ProcurementItem> {
    return this.http.put<ProcurementItem>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update procurement item status.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param status The new status
   * @returns Observable of the updated procurement item
   */
  updateProcurementItemStatus(rcId: number, fyId: number, procurementItemId: number, status: ProcurementItemStatus): Observable<ProcurementItem> {
    return this.http.put<ProcurementItem>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/status`, { status }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @returns Observable of void
   */
  deleteProcurementItem(rcId: number, fyId: number, procurementItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // Quote Methods
  // ==========================

  /**
   * Get all quotes for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @returns Observable of quotes array
   */
  getQuotes(rcId: number, fyId: number, procurementItemId: number): Observable<ProcurementQuote[]> {
    return this.http.get<ProcurementQuote[]>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param includeFiles Whether to include files
   * @returns Observable of the quote
   */
  getQuote(rcId: number, fyId: number, procurementItemId: number, quoteId: number, includeFiles = false): Observable<ProcurementQuote> {
    let params = new HttpParams();
    if (includeFiles) {
      params = params.set('includeFiles', 'true');
    }
    return this.http.get<ProcurementQuote>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}`, { params, withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param request The creation request
   * @returns Observable of the created quote
   */
  createQuote(rcId: number, fyId: number, procurementItemId: number, request: QuoteCreateRequest): Observable<ProcurementQuote> {
    return this.http.post<ProcurementQuote>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update a quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param request The update request
   * @returns Observable of the updated quote
   */
  updateQuote(rcId: number, fyId: number, procurementItemId: number, quoteId: number, request: Partial<QuoteCreateRequest>): Observable<ProcurementQuote> {
    return this.http.put<ProcurementQuote>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @returns Observable of void
   */
  deleteQuote(rcId: number, fyId: number, procurementItemId: number, quoteId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Select a quote for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID to select
   * @returns Observable of the updated quote
   */
  selectQuote(rcId: number, fyId: number, procurementItemId: number, quoteId: number): Observable<ProcurementQuote> {
    return this.http.post<ProcurementQuote>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/select`, {}, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // File Methods
  // ==========================

  /**
   * Get all files for a quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @returns Observable of files array
   */
  getFiles(rcId: number, fyId: number, procurementItemId: number, quoteId: number): Observable<ProcurementQuoteFile[]> {
    return this.http.get<ProcurementQuoteFile[]>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/files`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Upload a file to a quote.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param file The file to upload
   * @param description Optional file description
   * @returns Observable of the uploaded file metadata
   */
  uploadFile(rcId: number, fyId: number, procurementItemId: number, quoteId: number, file: File, description?: string): Observable<ProcurementQuoteFile> {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    return this.http.post<ProcurementQuoteFile>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/files`, formData, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a file.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param fileId The file ID
   * @returns Observable of void
   */
  deleteFile(rcId: number, fyId: number, procurementItemId: number, quoteId: number, fileId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/files/${fileId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get the download URL for a file.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param fileId The file ID
   * @returns The download URL
   */
  getFileDownloadUrl(rcId: number, fyId: number, procurementItemId: number, quoteId: number, fileId: number): string {
    return `${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/files/${fileId}/download`;
  }

  /**
   * Get the view URL for a file.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param quoteId The quote ID
   * @param fileId The file ID
   * @returns The view URL
   */
  getFileViewUrl(rcId: number, fyId: number, procurementItemId: number, quoteId: number, fileId: number): string {
    return `${this.baseUrl(rcId, fyId)}/${procurementItemId}/quotes/${quoteId}/files/${fileId}/view`;
  }

  // ==========================
  // Procurement Event Operations
  // ==========================

  /**
   * Get all events for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param eventType Optional event type filter
   * @param startDate Optional start date filter (ISO date string)
   * @param endDate Optional end date filter (ISO date string)
   * @returns Observable of procurement events
   */
  getEvents(rcId: number, fyId: number, procurementItemId: number, 
            eventType?: ProcurementEventType, startDate?: string, endDate?: string): Observable<ProcurementEvent[]> {
    let params = new HttpParams();
    if (eventType) {
      params = params.set('eventType', eventType);
    }
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<ProcurementEvent[]>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events`, 
      { params, withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific event by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param eventId The event ID
   * @returns Observable of the event
   */
  getEvent(rcId: number, fyId: number, procurementItemId: number, eventId: number): Observable<ProcurementEvent> {
    return this.http.get<ProcurementEvent>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events/${eventId}`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get the event count for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @returns Observable of the event count
   */
  getEventCount(rcId: number, fyId: number, procurementItemId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events/count`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get the most recent event for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @returns Observable of the most recent event (or null if none)
   */
  getLatestEvent(rcId: number, fyId: number, procurementItemId: number): Observable<ProcurementEvent | null> {
    return this.http.get<ProcurementEvent | null>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events/latest`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new event for a procurement item.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param request The event create request
   * @returns Observable of the created event
   */
  createEvent(rcId: number, fyId: number, procurementItemId: number, 
              request: ProcurementEventRequest): Observable<ProcurementEvent> {
    return this.http.post<ProcurementEvent>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events`, 
      request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing event.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param eventId The event ID
   * @param request The event update request
   * @returns Observable of the updated event
   */
  updateEvent(rcId: number, fyId: number, procurementItemId: number, 
              eventId: number, request: ProcurementEventRequest): Observable<ProcurementEvent> {
    return this.http.put<ProcurementEvent>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events/${eventId}`, 
      request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete an event.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param procurementItemId The procurement item ID
   * @param eventId The event ID
   * @returns Observable of void
   */
  deleteEvent(rcId: number, fyId: number, procurementItemId: number, eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${procurementItemId}/events/${eventId}`, 
      { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // Error Handling
  // ==========================

  /**
   * Handle HTTP errors.
   *
   * @param error The HTTP error response
   * @returns Observable that throws an error
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      errorMessage = 'You must be logged in to perform this action';
    } else if (error.status === 403) {
      errorMessage = 'You do not have permission to perform this action';
    } else if (error.status === 404) {
      errorMessage = 'The requested resource was not found';
    } else if (error.status === 400) {
      errorMessage = error.error?.message || 'Invalid request';
    } else if (error.status >= 500) {
      errorMessage = 'Server error. Please try again later.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
