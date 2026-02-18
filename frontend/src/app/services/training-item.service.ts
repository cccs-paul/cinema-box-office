/**
 * Training Item Service for myRC application.
 * Handles all API communication for training item management.
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
import { TrainingItem, TrainingMoneyAllocation, TrainingParticipant } from '../models/training-item.model';

/**
 * Request body for creating a training item.
 */
export interface TrainingItemCreateRequest {
  name: string;
  description?: string;
  provider?: string;
  status?: string;
  trainingType?: string;
  format?: string;
  startDate?: string | null;
  endDate?: string | null;
  location?: string;
  participants?: TrainingParticipant[];
  moneyAllocations?: TrainingMoneyAllocation[];
}

/**
 * Request body for updating a training item.
 */
export interface TrainingItemUpdateRequest {
  name?: string;
  description?: string;
  provider?: string;
  status?: string;
  trainingType?: string;
  format?: string;
  startDate?: string | null;
  endDate?: string | null;
  location?: string;
  moneyAllocations?: TrainingMoneyAllocation[];
}

/**
 * Service for managing training items within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class TrainingItemService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/training-items`;
  }

  constructor(private http: HttpClient) {}

  /**
   * Get all training items for a fiscal year.
   */
  getTrainingItemsByFY(rcId: number, fyId: number): Observable<TrainingItem[]> {
    return this.http.get<TrainingItem[]>(this.baseUrl(rcId, fyId), { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific training item by ID.
   */
  getTrainingItem(rcId: number, fyId: number, trainingItemId: number): Observable<TrainingItem> {
    return this.http.get<TrainingItem>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new training item.
   */
  createTrainingItem(rcId: number, fyId: number, request: TrainingItemCreateRequest): Observable<TrainingItem> {
    return this.http.post<TrainingItem>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing training item.
   */
  updateTrainingItem(rcId: number, fyId: number, trainingItemId: number, request: TrainingItemUpdateRequest): Observable<TrainingItem> {
    return this.http.put<TrainingItem>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a training item.
   */
  deleteTrainingItem(rcId: number, fyId: number, trainingItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update the status of a training item.
   */
  updateStatus(rcId: number, fyId: number, trainingItemId: number, status: string): Observable<TrainingItem> {
    return this.http.put<TrainingItem>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/status`, { status }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get money allocations for a training item.
   */
  getMoneyAllocations(rcId: number, fyId: number, trainingItemId: number): Observable<TrainingMoneyAllocation[]> {
    return this.http.get<TrainingMoneyAllocation[]>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/allocations`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update money allocations for a training item.
   */
  updateMoneyAllocations(rcId: number, fyId: number, trainingItemId: number, allocations: TrainingMoneyAllocation[]): Observable<TrainingItem> {
    return this.http.put<TrainingItem>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/allocations`, allocations, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  // ============================
  // Participant Management
  // ============================

  /**
   * Get all participants for a training item.
   */
  getParticipants(rcId: number, fyId: number, trainingItemId: number): Observable<TrainingParticipant[]> {
    return this.http.get<TrainingParticipant[]>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/participants`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Add a participant to a training item.
   */
  addParticipant(rcId: number, fyId: number, trainingItemId: number, participant: TrainingParticipant): Observable<TrainingParticipant> {
    return this.http.post<TrainingParticipant>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/participants`, participant, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update a participant.
   */
  updateParticipant(rcId: number, fyId: number, trainingItemId: number, participantId: number, participant: TrainingParticipant): Observable<TrainingParticipant> {
    return this.http.put<TrainingParticipant>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/participants/${participantId}`, participant, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a participant.
   */
  deleteParticipant(rcId: number, fyId: number, trainingItemId: number, participantId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${trainingItemId}/participants/${participantId}`, { withCredentials: true })
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
        errorMessage = 'Training item not found';
      } else {
        errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('TrainingItemService error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
