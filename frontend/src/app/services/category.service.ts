/**
 * Category Service for myRC application.
 * Handles all API communication for category management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Category, FundingType } from '../models/category.model';

/**
 * Request body for creating a category.
 */
export interface CategoryCreateRequest {
  name: string;
  description?: string;
  fundingType?: FundingType;
}

/**
 * Request body for updating a category.
 */
export interface CategoryUpdateRequest {
  name?: string;
  description?: string;
  fundingType?: FundingType;
}

/**
 * Service for managing categories within fiscal years.
 * Categories are used to group both funding and spending items.
 * Default categories are read-only; only custom categories can be added, renamed, and deleted.
 */
@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private baseUrl(rcId: number, fyId: number): string {
    return `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`;
  }

  constructor(private http: HttpClient) {}

  /**
   * Get all categories for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of categories array
   */
  getCategoriesByFY(rcId: number, fyId: number): Observable<Category[]> {
    return this.http.get<Category[]>(this.baseUrl(rcId, fyId), { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a specific category by ID.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId The category ID
   * @returns Observable of the category
   */
  getCategory(rcId: number, fyId: number, categoryId: number): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl(rcId, fyId)}/${categoryId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new category for a fiscal year.
   * Note: Only custom categories can be created. Default categories are system-managed.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param request The category creation request
   * @returns Observable of the created category
   */
  createCategory(rcId: number, fyId: number, request: CategoryCreateRequest): Observable<Category> {
    return this.http.post<Category>(this.baseUrl(rcId, fyId), request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing category.
   * Note: Only custom categories can be updated. Default categories are read-only.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryId The category ID
   * @param request The category update request
   * @returns Observable of the updated category
   */
  updateCategory(rcId: number, fyId: number, categoryId: number, request: CategoryUpdateRequest): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl(rcId, fyId)}/${categoryId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a category.
   * Note: Only custom categories can be deleted. Default categories cannot be deleted.
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
   * Ensure default categories exist for a fiscal year.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @returns Observable of categories array including defaults
   */
  ensureDefaults(rcId: number, fyId: number): Observable<Category[]> {
    return this.http.post<Category[]>(`${this.baseUrl(rcId, fyId)}/ensure-defaults`, {}, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Reorder categories.
   *
   * @param rcId The responsibility centre ID
   * @param fyId The fiscal year ID
   * @param categoryIds Array of category IDs in the new order
   * @returns Observable of reordered categories array
   */
  reorderCategories(rcId: number, fyId: number, categoryIds: number[]): Observable<Category[]> {
    return this.http.post<Category[]>(
      `${this.baseUrl(rcId, fyId)}/reorder`,
      { categoryIds },
      { withCredentials: true }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      if (error.status === 401) {
        errorMessage = 'Authentication required. Please log in.';
      } else if (error.status === 403) {
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else {
          errorMessage = 'You do not have permission to perform this action.';
        }
      } else if (error.status === 404) {
        errorMessage = 'Category not found.';
      } else if (error.status === 409) {
        errorMessage = 'A category with this name already exists for this fiscal year.';
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }
}
