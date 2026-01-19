/*
 * Cinema Box Office - Authentication Service
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { User, AuthResponse } from '../models/user.model';

/**
 * Authentication service for managing user login and session.
 * Supports LOCAL (app-managed), LDAP, and OAUTH2 authentication.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;
  private readonly API_URL = '/api';
  private readonly STORAGE_KEY = 'currentUser';

  /**
   * Constructor.
   *
   * @param http Angular HTTP client
   */
  constructor(private http: HttpClient) {
    // Start with no user - don't trust localStorage on initial load
    // The user must login or have a valid session restored
    this.currentUserSubject = new BehaviorSubject<User | null>(null);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  /**
   * Get current user value.
   */
  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user is logged in.
   */
  get isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  /**
   * Authenticate with LOCAL (app-managed) credentials.
   *
   * @param username User's username
   * @param password User's password
   * @returns Observable of authentication response
   */
  loginLocal(username: string, password: string): Observable<User> {
    return this.http
      .post<User>(
        `${this.API_URL}/users/authenticate?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
        {}
      )
      .pipe(
        tap((user) => this.setCurrentUser(user)),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Authenticate with LDAP credentials.
   *
   * @param username LDAP username
   * @param password LDAP password
   * @returns Observable of authentication response
   */
  loginLdap(username: string, password: string): Observable<User> {
    return this.http
      .post<User>(
        `${this.API_URL}/users/authenticate/ldap?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
        {}
      )
      .pipe(
        tap((user) => this.setCurrentUser(user)),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Initiate OAuth2 authentication flow.
   * Redirects to OAuth provider.
   *
   * @param provider OAuth provider (google, github, etc.)
   */
  initiateOAuth2(provider: string): void {
    // Redirect to OAuth provider authorization endpoint
    window.location.href = `${this.API_URL}/oauth2/authorization/${provider}`;
  }

  /**
   * Handle OAuth2 callback and authenticate user.
   * Called after redirect from OAuth provider.
   *
   * @returns Observable of authentication response
   */
  handleOAuth2Callback(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/me`).pipe(
      tap((user) => this.setCurrentUser(user)),
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Logout current user.
   */
  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Set current user and persist to storage.
   *
   * @param user User object
   */
  private setCurrentUser(user: User): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  /**
   * Handle HTTP errors.
   *
   * @param error HTTP error
   * @returns Observable error
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'Authentication failed';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      errorMessage = 'Invalid credentials';
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to server';
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    }

    console.error('Auth error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
