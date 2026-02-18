/*
 * myRC - Responsibility Centre Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ResponsibilityCentreDTO } from '../models/responsibility-centre.model';

@Injectable({
  providedIn: 'root'
})
export class ResponsibilityCentreService {
  private apiUrl = '/api/responsibility-centres';
  private selectedRCSubject = new BehaviorSubject<number | null>(
    this.getStoredSelectedRC()
  );
  public selectedRC$ = this.selectedRCSubject.asObservable();

  private selectedFYSubject = new BehaviorSubject<number | null>(
    this.getStoredSelectedFY()
  );
  public selectedFY$ = this.selectedFYSubject.asObservable();

  /** Emits when RC data is updated (e.g. feature toggles changed), so subscribers can refresh. */
  private rcUpdatedSubject = new Subject<number>();
  public rcUpdated$ = this.rcUpdatedSubject.asObservable();

  /** Notify subscribers that an RC has been updated (e.g. after toggling features). */
  notifyRCUpdated(rcId: number): void {
    this.rcUpdatedSubject.next(rcId);
  }

  constructor(private http: HttpClient) {}

  getAllResponsibilityCentres(): Observable<ResponsibilityCentreDTO[]> {
    return this.http.get<ResponsibilityCentreDTO[]>(this.apiUrl, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  getResponsibilityCentre(id: number): Observable<ResponsibilityCentreDTO> {
    return this.http.get<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  createResponsibilityCentre(
    name: string,
    description: string
  ): Observable<ResponsibilityCentreDTO> {
    return this.http.post<ResponsibilityCentreDTO>(this.apiUrl, {
      name,
      description
    }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  updateResponsibilityCentre(
    id: number,
    name: string,
    description: string
  ): Observable<ResponsibilityCentreDTO> {
    return this.http.put<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, {
      name,
      description
    }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  deleteResponsibilityCentre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  cloneResponsibilityCentre(id: number, newName: string): Observable<ResponsibilityCentreDTO> {
    return this.http.post<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}/clone`, {
      newName
    }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  grantAccess(
    rcId: number,
    username: string,
    accessLevel: string
  ): Observable<any> {
    return this.http.post(`${this.apiUrl}/${rcId}/access/grant`, {
      username,
      accessLevel
    }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  revokeAccess(rcId: number, username: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${rcId}/access/revoke`, {
      username
    }, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  getResponsibilityCentreAccess(rcId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${rcId}/access`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  setSelectedRC(rcId: number): void {
    localStorage.setItem('selectedRC', rcId.toString());
    this.selectedRCSubject.next(rcId);
  }

  getSelectedRC(): number | null {
    return this.selectedRCSubject.value;
  }

  setSelectedFY(fyId: number): void {
    localStorage.setItem('selectedFY', fyId.toString());
    this.selectedFYSubject.next(fyId);
  }

  getSelectedFY(): number | null {
    return this.selectedFYSubject.value;
  }

  clearSelection(): void {
    localStorage.removeItem('selectedRC');
    localStorage.removeItem('selectedFY');
    this.selectedRCSubject.next(null);
    this.selectedFYSubject.next(null);
  }

  setTrainingEnabled(rcId: number, enabled: boolean): Observable<ResponsibilityCentreDTO> {
    return this.http.patch<ResponsibilityCentreDTO>(
      `${this.apiUrl}/${rcId}/training-enabled`,
      { enabled },
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  setTravelEnabled(rcId: number, enabled: boolean): Observable<ResponsibilityCentreDTO> {
    return this.http.patch<ResponsibilityCentreDTO>(
      `${this.apiUrl}/${rcId}/travel-enabled`,
      { enabled },
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  setTrainingIncludeInSummary(rcId: number, include: boolean): Observable<ResponsibilityCentreDTO> {
    return this.http.patch<ResponsibilityCentreDTO>(
      `${this.apiUrl}/${rcId}/training-include-in-summary`,
      { include },
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  setTravelIncludeInSummary(rcId: number, include: boolean): Observable<ResponsibilityCentreDTO> {
    return this.http.patch<ResponsibilityCentreDTO>(
      `${this.apiUrl}/${rcId}/travel-include-in-summary`,
      { include },
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  private getStoredSelectedRC(): number | null {
    const stored = localStorage.getItem('selectedRC');
    return stored ? parseInt(stored, 10) : null;
  }

  private getStoredSelectedFY(): number | null {
    const stored = localStorage.getItem('selectedFY');
    return stored ? parseInt(stored, 10) : null;
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
      // Server-side error - check for specific error messages from backend
      const backendMessage = error.error?.message || error.error?.error || '';
      
      switch (error.status) {
        case 400:
          // Check if it's a duplicate name error
          if (backendMessage.toLowerCase().includes('already exists') || 
              backendMessage.toLowerCase().includes('duplicate')) {
            errorMessage = backendMessage;
          } else {
            errorMessage = backendMessage || 'Invalid request. Please check your input.';
          }
          break;
        case 401:
          errorMessage = 'Your session has expired. Please log in again.';
          break;
        case 403:
          errorMessage = 'You do not have permission to perform this action.';
          break;
        case 404:
          errorMessage = 'Responsibility Centre not found.';
          break;
        case 409:
          // Conflict - typically duplicate name
          errorMessage = backendMessage || 'A Responsibility Centre with this name already exists.';
          break;
        case 500:
          errorMessage = 'Server error occurred. Please try again later.';
          break;
        default:
          errorMessage = backendMessage || `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error('RC Service Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
