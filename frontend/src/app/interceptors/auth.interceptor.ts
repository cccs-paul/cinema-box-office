/*
 * myRC - Auth Interceptor
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Global HTTP interceptor that catches 401 Unauthorized responses.
 * On session expiry, clears auth state and redirects to login page.
 */
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Functional HTTP interceptor that handles 401 responses globally.
 * When a 401 is received (session expired), it logs the user out
 * and redirects to the login page instead of showing empty content.
 *
 * Excluded endpoints:
 * - /api/users/me (session check on startup — expected to 401)
 * - /api/users/authenticate (login — expected to 401 on bad creds)
 * - /api/auth/ (login methods, registration — public endpoints)
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Don't redirect for auth-related endpoints (login, session check, registration)
        const url = req.url;
        const isAuthEndpoint =
          url.includes('/api/users/me') ||
          url.includes('/api/users/authenticate') ||
          url.includes('/api/auth/');

        if (!isAuthEndpoint && authService.isLoggedIn) {
          // Session expired while user was logged in — redirect to login
          authService.logout();
          router.navigate(['/login'], {
            queryParams: { sessionExpired: 'true' }
          });
        }
      }
      return throwError(() => error);
    })
  );
};
