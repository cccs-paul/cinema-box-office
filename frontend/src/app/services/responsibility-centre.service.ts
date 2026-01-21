/*
 * Cinema Box Office - Responsibility Centre Service
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { ResponsibilityCentreDTO } from '../models/responsibility-centre.model';

@Injectable({
  providedIn: 'root'
})
export class ResponsibilityCentreService {
  private apiUrl = 'http://localhost:8080/api/responsibility-centres';
  private selectedRCSubject = new BehaviorSubject<number | null>(
    this.getStoredSelectedRC()
  );
  public selectedRC$ = this.selectedRCSubject.asObservable();

  constructor(private http: HttpClient) {}

  getAllResponsibilityCentres(): Observable<ResponsibilityCentreDTO[]> {
    return this.http.get<ResponsibilityCentreDTO[]>(this.apiUrl, { withCredentials: true });
  }

  getResponsibilityCentre(id: number): Observable<ResponsibilityCentreDTO> {
    return this.http.get<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  createResponsibilityCentre(
    name: string,
    description: string
  ): Observable<ResponsibilityCentreDTO> {
    return this.http.post<ResponsibilityCentreDTO>(this.apiUrl, {
      name,
      description
    }, { withCredentials: true });
  }

  updateResponsibilityCentre(
    id: number,
    name: string,
    description: string
  ): Observable<ResponsibilityCentreDTO> {
    return this.http.put<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, {
      name,
      description
    }, { withCredentials: true });
  }

  deleteResponsibilityCentre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  grantAccess(
    rcId: number,
    username: string,
    accessLevel: string
  ): Observable<any> {
    return this.http.post(`${this.apiUrl}/${rcId}/access/grant`, {
      username,
      accessLevel
    }, { withCredentials: true });
  }

  revokeAccess(rcId: number, username: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${rcId}/access/revoke`, {
      username
    }, { withCredentials: true });
  }

  getResponsibilityCentreAccess(rcId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${rcId}/access`, { withCredentials: true });
  }

  setSelectedRC(rcId: number): void {
    localStorage.setItem('selectedRC', rcId.toString());
    this.selectedRCSubject.next(rcId);
  }

  getSelectedRC(): number | null {
    return this.selectedRCSubject.value;
  }

  private getStoredSelectedRC(): number | null {
    const stored = localStorage.getItem('selectedRC');
    return stored ? parseInt(stored, 10) : null;
  }
}
