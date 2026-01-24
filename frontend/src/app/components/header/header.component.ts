/*
 * myRC - Global Header Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ThemeService, Theme } from '../../services/theme.service';

/**
 * Global header component with user dropdown menu.
 * Displayed on authenticated pages to provide navigation and user options.
 * Hidden on login page for full-screen login experience.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-18
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  isUserMenuOpen = false;
  currentUser: any = null;
  currentTheme: Theme = 'light';
  isLoggingOut = false;
  isLoggedIn = false;
  isHeaderVisible = true;
  private destroy$ = new Subject<void>();
  private lastScrollPosition = 0;
  private readonly scrollThreshold = 50;

  constructor(
    private authService: AuthService,
    private themeService: ThemeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get current user from auth service
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        this.currentUser = user;
        this.isLoggedIn = !!user;
      });

    // Get current theme
    this.themeService.currentTheme$
      .pipe(takeUntil(this.destroy$))
      .subscribe((theme: Theme) => {
        this.currentTheme = theme;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu-container')) {
      this.closeUserMenu();
    }
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const currentScrollPosition = window.pageYOffset || document.documentElement.scrollTop;
    
    // If scrolled past threshold, hide header when scrolling down, show when scrolling up
    if (currentScrollPosition > this.scrollThreshold) {
      this.isHeaderVisible = currentScrollPosition < this.lastScrollPosition;
    } else {
      // Always show header when near the top
      this.isHeaderVisible = true;
    }
    
    this.lastScrollPosition = currentScrollPosition;
  }

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  toggleTheme(): void {
    const newTheme: Theme = this.currentTheme === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(newTheme);
    this.closeUserMenu();
  }

  getThemeIcon(): string {
    return this.currentTheme === 'light' ? '🌙' : '☀️';
  }

  openApiDocs(): void {
    // Open API documentation in new tab
    // The API docs are typically available at /api/swagger-ui.html
    window.open('http://localhost:8080/api/swagger-ui.html', '_blank');
    this.closeUserMenu();
  }

  async logout(): Promise<void> {
    this.isLoggingOut = true;
    try {
      this.authService.logout();
      await this.router.navigate(['/login']);
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      this.isLoggingOut = false;
    }
  }
}
