/*
 * Cinema Box Office - Landing Page Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ThemeService, Theme } from '../../services/theme.service';

/**
 * Landing page component shown after successful authentication.
 * Displays user information, API/database status, and developer tools.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  title = 'Cinema Box Office';
  currentUser: User | null = null;
  currentTheme: Theme = 'light';
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;
  isLoggingOut = false;
  isUserMenuOpen = false;
  isProfileCardVisible = false;
  private destroy$ = new Subject<void>();

  /**
   * Constructor.
   *
   * @param authService Authentication service
   * @param router Angular router
   * @param http HTTP client
   */
  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private themeService: ThemeService
  ) {}

  /**
   * Component initialization.
   */
  ngOnInit(): void {
    // Subscribe to current user
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        // Load user's theme preference from server
        this.themeService.getUserTheme(user.username).subscribe({
          next: (response) => {
            this.currentTheme = response.theme;
            this.themeService.setTheme(response.theme);
          },
          error: (error) => {
            console.error('Failed to load theme preference:', error);
            // Continue with default theme
          },
        });
      }
    });

    // Subscribe to theme changes
    this.themeService.currentTheme$.pipe(takeUntil(this.destroy$)).subscribe((theme: Theme) => {
      this.currentTheme = theme;
    });

    // Check API and database health
    this.checkApiHealth();
    this.checkDatabaseHealth();
  }

  /**
   * Component cleanup.
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Toggle user menu dropdown.
   */
  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  /**
   * Close user menu.
   */
  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  /**
   * Toggle profile card visibility.
   */
  toggleProfileCard(): void {
    this.isProfileCardVisible = !this.isProfileCardVisible;
    this.closeUserMenu();
  }

  /**
   * Navigate to developer tools page.
   */
  goToDeveloperTools(): void {
    this.router.navigate(['/developer-tools']);
  }

  /**
   * Toggle between light and dark theme.
   */
  toggleTheme(): void {
    if (!this.currentUser) {
      console.error('User not authenticated');
      return;
    }

    const newTheme: Theme = this.currentTheme === 'light' ? 'dark' : 'light';

    // Update in service (updates UI immediately)
    this.themeService.setTheme(newTheme);

    // Persist to database
    this.themeService.updateUserTheme(this.currentUser.username, newTheme).subscribe({
      next: () => {
        console.log(`Theme updated to ${newTheme} for user ${this.currentUser!.username}`);
      },
      error: (error) => {
        console.error('Failed to update theme on server:', error);
        // Revert to previous theme on failure
        this.themeService.setTheme(this.currentTheme === 'light' ? 'dark' : 'light');
      },
    });
    this.closeUserMenu();
  }

  /**
   * Get theme icon based on current theme.
   */
  getThemeIcon(): string {
    return this.currentTheme === 'light' ? '🌙' : '☀️';
  }

  /**
   * Handle user logout.
   */
  logout(): void {
    this.isLoggingOut = true;
    setTimeout(() => {
      this.authService.logout();
      this.router.navigate(['/login']);
    }, 300);
  }

  /**
   * Format date for display.
   *
   * @param dateString ISO date string
   */
  formatDate(dateString: string): string {
    try {
      return new Date(dateString).toLocaleString();
    } catch {
      return dateString;
    }
  }

  /**
   * Check API health status.
   */
  private checkApiHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health').subscribe({
      next: (response) => {
        this.isApiHealthy = response.status === 'UP';
        this.apiStatus = response.message;
      },
      error: (error) => {
        this.isApiHealthy = false;
        this.apiStatus = 'API is not available';
        console.error('API health check failed:', error);
      },
    });
  }

  /**
   * Check database health status.
   */
  private checkDatabaseHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health/db').subscribe({
      next: (response) => {
        this.isDatabaseHealthy = response.status === 'UP';
        this.databaseStatus = response.message;
      },
      error: (error) => {
        this.isDatabaseHealthy = false;
        this.databaseStatus = 'Database is not available';
        console.error('Database health check failed:', error);
      },
    });
  }
}
