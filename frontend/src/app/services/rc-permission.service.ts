/*
 * myRC - RC Permission Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * Model for RC Access/Permission.
 */
export interface RCAccess {
  id: number | null;
  rcId: number;
  rcName: string;
  principalIdentifier: string;
  principalDisplayName: string;
  principalType: 'USER' | 'GROUP' | 'DISTRIBUTION_LIST';
  accessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY';
  grantedAt: string;
  grantedBy: string | null;
}

/**
 * Request to grant user access.
 */
export interface GrantUserAccessRequest {
  username: string;
  accessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY';
}

/**
 * Request to grant group/DL access.
 */
export interface GrantGroupAccessRequest {
  principalIdentifier: string;
  principalDisplayName: string;
  principalType: 'GROUP' | 'DISTRIBUTION_LIST';
  accessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY';
}

/**
 * Request to update permission.
 */
export interface UpdatePermissionRequest {
  accessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY';
}

/**
 * Service for managing RC permissions.
 * Provides methods for granting, updating, and revoking access to RCs.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Injectable({
  providedIn: 'root'
})
export class RCPermissionService {
  private apiUrl = '/api/rc-permissions';

  constructor(private http: HttpClient) {}

  /**
   * Get all permissions for a specific RC.
   *
   * @param rcId the RC ID
   * @returns Observable of permission array
   */
  getPermissionsForRC(rcId: number): Observable<RCAccess[]> {
    return this.http.get<RCAccess[]>(`${this.apiUrl}/rc/${rcId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Grant access to a user.
   *
   * @param rcId the RC ID
   * @param request the grant request
   * @returns Observable of the created access
   */
  grantUserAccess(rcId: number, request: GrantUserAccessRequest): Observable<RCAccess> {
    return this.http.post<RCAccess>(`${this.apiUrl}/rc/${rcId}/user`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Grant access to a group or distribution list.
   *
   * @param rcId the RC ID
   * @param request the grant request
   * @returns Observable of the created access
   */
  grantGroupAccess(rcId: number, request: GrantGroupAccessRequest): Observable<RCAccess> {
    return this.http.post<RCAccess>(`${this.apiUrl}/rc/${rcId}/group`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing permission.
   *
   * @param accessId the access record ID
   * @param request the update request
   * @returns Observable of the updated access
   */
  updatePermission(accessId: number, request: UpdatePermissionRequest): Observable<RCAccess> {
    return this.http.put<RCAccess>(`${this.apiUrl}/${accessId}`, request, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Revoke access.
   *
   * @param accessId the access record ID
   * @returns Observable<void>
   */
  revokeAccess(accessId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${accessId}`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Check if current user is an owner of the RC.
   *
   * @param rcId the RC ID
   * @returns Observable<boolean>
   */
  isOwner(rcId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/rc/${rcId}/is-owner`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Check if current user can edit content in the RC.
   *
   * @param rcId the RC ID
   * @returns Observable<boolean>
   */
  canEdit(rcId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/rc/${rcId}/can-edit`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get human-readable label for access level.
   */
  getAccessLevelLabel(level: string): string {
    switch (level) {
      case 'OWNER':
        return 'Owner';
      case 'READ_WRITE':
        return 'Read/Write';
      case 'READ_ONLY':
        return 'Read Only';
      default:
        return level;
    }
  }

  /**
   * Get human-readable label for principal type.
   */
  getPrincipalTypeLabel(type: string): string {
    switch (type) {
      case 'USER':
        return 'User';
      case 'GROUP':
        return 'Security Group';
      case 'DISTRIBUTION_LIST':
        return 'Distribution List';
      default:
        return type;
    }
  }

  /**
   * Get icon for principal type.
   */
  getPrincipalTypeIcon(type: string): string {
    switch (type) {
      case 'USER':
        return '👤';
      case 'GROUP':
        return '👥';
      case 'DISTRIBUTION_LIST':
        return '📧';
      default:
        return '❓';
    }
  }

  /**
   * Get icon for access level.
   */
  getAccessLevelIcon(level: string): string {
    switch (level) {
      case 'OWNER':
        return '👑';
      case 'READ_WRITE':
        return '✏️';
      case 'READ_ONLY':
        return '👁️';
      default:
        return '❓';
    }
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error || `Error Code: ${error.status}`;
    }
    console.error('RC Permission Service Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
