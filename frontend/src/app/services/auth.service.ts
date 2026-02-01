/*
 * myRC - Authentication Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { User, AuthResponse } from '../models/user.model';
import { LoginMethods, RegistrationRequest, RegistrationResponse, UsernameAvailabilityResponse } from '../models/auth.model';

/**
 * Authentication service for managing user login and session.
 * Supports LOCAL (app-managed), LDAP, and OAUTH2 authentication.
 *
 * @author myRC Team
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
    // Start with no user - will check for active session
    this.currentUserSubject = new BehaviorSubject<User | null>(null);
    this.currentUser$ = this.currentUserSubject.asObservable();
    
    // Check if there's an active session on initialization
    // Only check if there's a session indicator to avoid unnecessary 401 errors
    if (this.hasSessionIndicator()) {
      this.checkSession();
    }
  }

  /**
   * Check if there's an indicator that a session might exist.
   * This helps avoid unnecessary 401 errors on the login page.
   *
   * @returns true if a session indicator exists
   */
  private hasSessionIndicator(): boolean {
    // Check for JSESSIONID cookie or any session-related cookie
    // Note: HttpOnly cookies aren't visible to JavaScript, but we can check
    // if any cookies exist that might indicate an active session
    const cookies = document.cookie;
    // If there are any cookies set by the application, check for session
    // We can't read HttpOnly cookies directly, but the presence of any
    // cookies from our domain suggests the user may have a session
    return cookies.length > 0;
  }

  /**
   * Check if there's an active session and restore user.
   */
  private checkSession(): void {
    this.http.get<User>(`${this.API_URL}/users/me`, { withCredentials: true })
      .pipe(
        catchError((error) => {
          // No active session, user needs to login - this is expected behavior
          // Suppress 401 errors in console as they're normal for unauthenticated users
          if (error.status === 401) {
            // Silently handle - user is not logged in, which is expected
            return of(null);
          }
          // For other errors, log them as they might indicate a real problem
          console.error('Session check error:', error);
          return of(null);
        })
      )
      .subscribe({
        next: (user) => {
          if (user) {
            this.setCurrentUser(user);
          } else {
            this.currentUserSubject.next(null);
          }
        },
        error: () => {
          // Shouldn't reach here due to catchError, but handle just in case
          this.currentUserSubject.next(null);
        }
      });
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
        {},
        { withCredentials: true }
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
        {},
        { withCredentials: true }
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
    return this.http.get<User>(`${this.API_URL}/users/me`, { withCredentials: true }).pipe(
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
   * Get available login methods from the server.
   * Determines which authentication methods are enabled (App Account, LDAP, OAuth2).
   *
   * @returns Observable of LoginMethods configuration
   */
  getLoginMethods(): Observable<LoginMethods> {
    return this.http.get<LoginMethods>(`${this.API_URL}/auth/login-methods`).pipe(
      catchError((error) => {
        console.error('Failed to fetch login methods:', error);
        // Return default config with all methods enabled on error
        return of({
          appAccount: { enabled: true, allowRegistration: false },
          ldapEnabled: true,
          oauth2Enabled: true
        });
      })
    );
  }

  /**
   * Check if a username is available for registration.
   *
   * @param username Username to check
   * @returns Observable of availability response
   */
  checkUsernameAvailability(username: string): Observable<UsernameAvailabilityResponse> {
    return this.http.get<UsernameAvailabilityResponse>(
      `${this.API_URL}/auth/check-username/${encodeURIComponent(username)}`
    ).pipe(
      catchError((error) => {
        console.error('Username check failed:', error);
        return of({ available: false, message: 'Unable to verify username availability' });
      })
    );
  }

  /**
   * Register a new user account.
   *
   * @param request Registration request data
   * @returns Observable of registration response
   */
  register(request: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.API_URL}/auth/register`, request).pipe(
      catchError((error) => {
        let errorMessage = 'Registration failed';
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.status === 409) {
          errorMessage = 'Username or email already exists';
        } else if (error.status === 400) {
          errorMessage = 'Invalid registration data';
        }
        return throwError(() => new Error(errorMessage));
      })
    );
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
