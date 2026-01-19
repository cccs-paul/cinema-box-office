/*
 * Cinema Box Office - Auth Guard
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Injectable, inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Auth guard to protect routes requiring authentication.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

/**
 * Injectable auth guard service.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthGuardService {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Check if user can access route.
   */
  canActivate(): boolean {
    if (this.authService.isLoggedIn) {
      return true;
    }

    this.router.navigate(['/login']);
    return false;
  }
}
