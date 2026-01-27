/**
 * Spending Category Service for myRC application.
 * Handles all API communication for spending category management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SpendingCategory } from '../models/spending-category.model';

/**
 * Request body for creating a spending category.
 */
export interface CategoryCreateRequest {
  name: string;
  description?: string;
}

/**
 * Request body for updating a spending category.
 */
export interface CategoryUpdateRequest {
  name?: string;
  description?: string;
}

/**
 * Service for managing spending categories within fiscal years.
 */
@Injectable({
  providedIn: 'root'
})
export class SpendingCategoryService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-categories`;
  }

  constructor(private http: HttpClient) {}

  /**
   * Get all spending categories for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of spending categories array
   */
  getCategoriesByFY(rcId: number, fyId: number): Observable<SpendingCategory[]> {
    return this.http.get<SpendingCategory[]>(this.baseUrl(rcId, fyId), { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific spending category by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId The category ID
   * @returns Observable of the spending category
   */
  getCategory(rcId: number, fyId: number, categoryId: number): Observable<SpendingCategory> {
    return this.http.get<SpendingCategory>(`${this.baseUrl(rcId, fyId)}/${categoryId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new spending category for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The category creation request
   * @returns Observable of the created spending category
   */
  createCategory(rcId: number, fyId: number, request: CategoryCreateRequest): Observable<SpendingCategory> {
    return this.http.post<SpendingCategory>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing spending category.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId The category ID
   * @param request The update request
   * @returns Observable of the updated spending category
   */
  updateCategory(rcId: number, fyId: number, categoryId: number, request: CategoryUpdateRequest): Observable<SpendingCategory> {
    return this.http.put<SpendingCategory>(`${this.baseUrl(rcId, fyId)}/${categoryId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a spending category.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId The category ID
   * @returns Observable of void
   */
  deleteCategory(rcId: number, fyId: number, categoryId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(rcId, fyId)}/${categoryId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Ensure default spending categories exist for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of all spending categories
   */
  ensureDefaults(rcId: number, fyId: number): Observable<SpendingCategory[]> {
    return this.http.post<SpendingCategory[]>(`${this.baseUrl(rcId, fyId)}/ensure-defaults`, {}, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Reorder spending categories.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryIds The ordered list of category IDs
   * @returns Observable of reordered spending categories
   */
  reorderCategories(rcId: number, fyId: number, categoryIds: number[]): Observable<SpendingCategory[]> {
    return this.http.post<SpendingCategory[]>(`${this.baseUrl(rcId, fyId)}/reorder`, { categoryIds }, { withCredentials: true })
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
        errorMessage = 'Category not found';
      } else if (error.status === 409) {
        errorMessage = 'Category name already exists';
      } else {
        errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('SpendingCategoryService error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
