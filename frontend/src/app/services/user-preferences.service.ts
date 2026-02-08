/*
 * myRC - User Preferences Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-11
 * Version: 1.0.0
 *
 * Description:
 * Service for managing user-level display preferences stored in localStorage.
 * These are per-user settings (as opposed to FY-level configuration)
 * that control the display of UI elements like search boxes, category
 * filters, and grouping options.
 */
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * User display preferences model.
 */
export interface UserDisplayPreferences {
  /** Whether to show the search box and filter bar in list views. */
  showSearchBox: boolean;
  /** Whether to show the category filter dropdown. */
  showCategoryFilter: boolean;
  /** Whether to group items by category with section headers. */
  groupByCategory: boolean;
}

/**
 * Default user display preferences.
 */
const DEFAULT_PREFERENCES: UserDisplayPreferences = {
  showSearchBox: true,
  showCategoryFilter: true,
  groupByCategory: false,
};

/**
 * Service for managing user-level display preferences.
 * Persists settings to localStorage and provides an observable stream
 * so components can react to preference changes in real time.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-11
 */
@Injectable({
  providedIn: 'root',
})
export class UserPreferencesService {
  private readonly STORAGE_KEY = 'userDisplayPreferences';

  private preferencesSubject: BehaviorSubject<UserDisplayPreferences>;

  /** Observable of the current user display preferences. */
  public preferences$: Observable<UserDisplayPreferences>;

  constructor() {
    const prefs = this.loadFromStorage();
    this.preferencesSubject = new BehaviorSubject<UserDisplayPreferences>(prefs);
    this.preferences$ = this.preferencesSubject.asObservable();
  }

  /**
   * Get a snapshot of the current preferences.
   */
  get current(): UserDisplayPreferences {
    return this.preferencesSubject.value;
  }

  /**
   * Update a single preference value.
   *
   * @param key the preference key to update
   * @param value the new value
   */
  updatePreference<K extends keyof UserDisplayPreferences>(key: K, value: UserDisplayPreferences[K]): void {
    const prefs = { ...this.current, [key]: value };
    this.preferencesSubject.next(prefs);
    this.saveToStorage(prefs);
  }

  /**
   * Restore all preferences to their default values.
   */
  restoreDefaults(): void {
    const prefs = { ...DEFAULT_PREFERENCES };
    this.preferencesSubject.next(prefs);
    this.saveToStorage(prefs);
  }

  /**
   * Check whether any preference differs from its default.
   */
  hasCustomPreferences(): boolean {
    const prefs = this.current;
    return (Object.keys(DEFAULT_PREFERENCES) as Array<keyof UserDisplayPreferences>).some(
      key => prefs[key] !== DEFAULT_PREFERENCES[key]
    );
  }

  /**
   * Load preferences from localStorage with fallback to defaults.
   */
  private loadFromStorage(): UserDisplayPreferences {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        return { ...DEFAULT_PREFERENCES, ...parsed };
      }
    } catch {
      // Corrupted data; ignore and use defaults
    }
    return { ...DEFAULT_PREFERENCES };
  }

  /**
   * Persist current preferences to localStorage.
   */
  private saveToStorage(prefs: UserDisplayPreferences): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(prefs));
  }
}
