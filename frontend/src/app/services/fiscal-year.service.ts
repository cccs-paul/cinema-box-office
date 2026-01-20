/*
 * Cinema Box Office - Fiscal Year Service
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { FiscalYearDTO } from '../models/fiscal-year.model';

@Injectable({
  providedIn: 'root'
})
export class FiscalYearService {
  private apiUrl = 'http://localhost:8080/api/fiscal-years';
  private selectedFYSubject = new BehaviorSubject<number | null>(
    this.getStoredSelectedFY()
  );
  public selectedFY$ = this.selectedFYSubject.asObservable();

  constructor(private http: HttpClient) {}

  getAllForRc(rcId: number): Observable<FiscalYearDTO[]> {
    return this.http.get<FiscalYearDTO[]>(`${this.apiUrl}/rc/${rcId}`);
  }

  createFiscalYear(name: string, rcId: number): Observable<FiscalYearDTO> {
    return this.http.post<FiscalYearDTO>(this.apiUrl, {
      name,
      rcId
    });
  }

  updateFiscalYear(id: number, name: string): Observable<FiscalYearDTO> {
    return this.http.put<FiscalYearDTO>(`${this.apiUrl}/${id}`, {
      name
    });
  }

  deleteFiscalYear(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  setSelectedFY(fyId: number): void {
    localStorage.setItem('selectedFY', fyId.toString());
    this.selectedFYSubject.next(fyId);
  }

  getSelectedFY(): number | null {
    return this.selectedFYSubject.value;
  }

  private getStoredSelectedFY(): number | null {
    const stored = localStorage.getItem('selectedFY');
    return stored ? parseInt(stored, 10) : null;
  }

  clearSelection(): void {
    localStorage.removeItem('selectedFY');
    this.selectedFYSubject.next(null);
  }
}
