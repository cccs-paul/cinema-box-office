/*
 * myRC - Directory Search Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

/**
 * Search result from the directory search API.
 * Represents a user or group entry from the app database or LDAP.
 */
export interface DirectorySearchResult {
  /** Unique identifier (username or group DN). */
  identifier: string;
  /** Human-readable display name. */
  displayName: string;
  /** Source of the entry: 'APP' or 'LDAP'. */
  source: 'APP' | 'LDAP';
  /** Email address (may be null for groups). */
  email: string | null;
}

/**
 * Service for searching users and groups from the directory.
 * Provides autocomplete/typeahead functionality for the permissions UI.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-02-07
 */
@Injectable({
  providedIn: 'root'
})
export class DirectorySearchService {
  private readonly apiUrl = '/api/directory';

  constructor(private http: HttpClient) {}

  /**
   * Search for users matching a query string.
   * Returns results from both the application database and LDAP directory.
   * An empty query returns all available entries up to the max limit.
   *
   * @param query the search query (empty string returns all entries)
   * @param maxResults the maximum number of results to return (default 10)
   * @returns Observable of matching user search results
   */
  searchUsers(query: string, maxResults: number = 10): Observable<DirectorySearchResult[]> {
    if (query == null) {
      return of([]);
    }

    const params = new HttpParams()
      .set('q', query.trim())
      .set('max', Math.min(Math.max(maxResults, 1), 50).toString());

    return this.http.get<DirectorySearchResult[]>(`${this.apiUrl}/users`, {
      params,
      withCredentials: true
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search for distribution lists matching a query string.
   * Returns results from the LDAP directory when enabled.
   * Distribution lists are searched in a separate OU from security groups
   * and may include email addresses.
   * An empty query returns all available entries up to the max limit.
   *
   * @param query the search query (empty string returns all entries)
   * @param maxResults the maximum number of results to return (default 10)
   * @returns Observable of matching distribution list search results
   */
  searchDistributionLists(query: string, maxResults: number = 10): Observable<DirectorySearchResult[]> {
    if (query == null) {
      return of([]);
    }

    const params = new HttpParams()
      .set('q', query.trim())
      .set('max', Math.min(Math.max(maxResults, 1), 50).toString());

    return this.http.get<DirectorySearchResult[]>(`${this.apiUrl}/distribution-lists`, {
      params,
      withCredentials: true
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search for security groups matching a query string.
   * Returns results from the LDAP directory when enabled.
   * An empty query returns all available entries up to the max limit.
   *
   * @param query the search query (empty string returns all entries)
   * @param maxResults the maximum number of results to return (default 10)
   * @returns Observable of matching group search results
   */
  searchGroups(query: string, maxResults: number = 10): Observable<DirectorySearchResult[]> {
    if (query == null) {
      return of([]);
    }

    const params = new HttpParams()
      .set('q', query.trim())
      .set('max', Math.min(Math.max(maxResults, 1), 50).toString());

    return this.http.get<DirectorySearchResult[]>(`${this.apiUrl}/groups`, {
      params,
      withCredentials: true
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search for all groups (security groups + distribution lists) matching a query.
   * Combines results from both endpoints and deduplicates by identifier.
   * An empty query returns all available entries up to the max limit.
   *
   * @param query the search query (empty string returns all entries)
   * @param maxResults the maximum number of results to return (default 20)
   * @returns Observable of combined, deduplicated group search results
   */
  searchAllGroups(query: string, maxResults: number = 20): Observable<DirectorySearchResult[]> {
    if (query == null) {
      return of([]);
    }

    return forkJoin([
      this.searchGroups(query, maxResults),
      this.searchDistributionLists(query, maxResults)
    ]).pipe(
      map(([groups, distLists]) => {
        const seen = new Set<string>();
        const combined: DirectorySearchResult[] = [];
        for (const item of [...groups, ...distLists]) {
          if (!seen.has(item.identifier)) {
            seen.add(item.identifier);
            combined.push(item);
          }
        }
        combined.sort((a, b) => a.identifier.localeCompare(b.identifier));
        return combined.slice(0, maxResults);
      })
    );
  }

  /**
   * Handle HTTP errors from the directory search API.
   */
  private handleError(error: HttpErrorResponse): Observable<DirectorySearchResult[]> {
    console.error('Directory search error:', error.message);
    return of([]);
  }
}
