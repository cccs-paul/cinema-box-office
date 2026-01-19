/*
 * Cinema Box Office - Theme Service
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export type Theme = 'light' | 'dark';

/**
 * Theme service for managing light/dark theme preferences.
 * Persists theme choice to database via API and localStorage.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private currentThemeSubject: BehaviorSubject<Theme>;
  public currentTheme$: Observable<Theme>;
  private readonly API_URL = '/api';
  private readonly STORAGE_KEY = 'appTheme';

  /**
   * Constructor.
   *
   * @param http Angular HTTP client
   */
  constructor(private http: HttpClient) {
    const storedTheme = (localStorage.getItem(this.STORAGE_KEY) as Theme) || 'light';
    this.currentThemeSubject = new BehaviorSubject<Theme>(storedTheme);
    this.currentTheme$ = this.currentThemeSubject.asObservable();
    this.applyTheme(storedTheme);
  }

  /**
   * Get current theme value.
   */
  get currentTheme(): Theme {
    return this.currentThemeSubject.value;
  }

  /**
   * Toggle between light and dark themes.
   */
  toggleTheme(): void {
    const newTheme: Theme = this.currentTheme === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Set theme programmatically.
   *
   * @param theme Theme to set (light or dark)
   */
  setTheme(theme: Theme): void {
    this.currentThemeSubject.next(theme);
    localStorage.setItem(this.STORAGE_KEY, theme);
    this.applyTheme(theme);
  }

  /**
   * Update theme preference for authenticated user on server.
   *
   * @param username User's username
   * @param theme Theme preference
   * @returns Observable of response
   */
  updateUserTheme(username: string, theme: Theme): Observable<any> {
    return this.http
      .put(
        `${this.API_URL}/users/${encodeURIComponent(username)}/theme?theme=${theme}`,
        {}
      )
      .pipe(
        tap((response) => {
          console.log('Theme updated on server:', response);
        })
      );
  }

  /**
   * Get user's theme preference from server.
   *
   * @param username User's username
   * @returns Observable of theme response
   */
  getUserTheme(username: string): Observable<{ theme: Theme }> {
    return this.http.get<{ theme: Theme }>(
      `${this.API_URL}/users/${encodeURIComponent(username)}/theme`
    );
  }

  /**
   * Apply theme by adding/removing theme classes and CSS variables.
   *
   * @param theme Theme to apply
   */
  private applyTheme(theme: Theme): void {
    const htmlElement = document.documentElement;

    if (theme === 'dark') {
      htmlElement.classList.add('dark-theme');
      htmlElement.classList.remove('light-theme');
      htmlElement.style.colorScheme = 'dark';
    } else {
      htmlElement.classList.add('light-theme');
      htmlElement.classList.remove('dark-theme');
      htmlElement.style.colorScheme = 'light';
    }

    // Update meta theme-color
    const metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', theme === 'dark' ? '#1a1a1a' : '#667eea');
    }
  }
}
