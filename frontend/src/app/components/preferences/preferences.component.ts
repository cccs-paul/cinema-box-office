/*
 * myRC - Preferences Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-11
 * Version: 1.0.0
 *
 * Description:
 * User preferences page combining General Settings and Accessibility
 * settings into a tabbed interface. General settings are persisted to
 * localStorage as user-level preferences. Accessibility settings are
 * managed by the AccessibilityService.
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  UserPreferencesService,
  UserDisplayPreferences,
} from '../../services/user-preferences.service';
import {
  AccessibilityService,
  AccessibilitySetting,
} from '../../services/accessibility.service';

/**
 * Preferences page component.
 * Provides a tabbed interface for General Settings (display preferences)
 * and Accessibility settings (font sizes, element sizes).
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-11
 */
@Component({
  selector: 'app-preferences',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './preferences.component.html',
  styleUrls: ['./preferences.component.scss'],
})
export class PreferencesComponent implements OnInit, OnDestroy {
  /** Active tab key. */
  activeTab: 'general' | 'accessibility' = 'general';

  // ── General Settings ──────────────────────────────────────────
  /** Current display preferences snapshot. */
  preferences: UserDisplayPreferences = {
    showSearchBox: true,
    showCategoryFilter: true,
    groupByCategory: false,
  };

  // ── Accessibility ─────────────────────────────────────────────
  /** Accessibility settings organised by group. */
  groupedSettings: Record<string, AccessibilitySetting[]> = {};

  /** Whether any accessibility setting has been customised. */
  hasCustomAccessibility = false;

  /** Success message for user feedback. */
  successMessage: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private userPreferencesService: UserPreferencesService,
    private accessibilityService: AccessibilityService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    // Subscribe to display preferences
    this.userPreferencesService.preferences$
      .pipe(takeUntil(this.destroy$))
      .subscribe(prefs => {
        this.preferences = { ...prefs };
      });

    // Subscribe to accessibility settings
    this.accessibilityService.settings$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.groupedSettings = this.accessibilityService.getGroupedSettings();
        this.hasCustomAccessibility = this.accessibilityService.hasCustomSettings();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Switch the active tab.
   *
   * @param tab the tab key to activate
   */
  setActiveTab(tab: 'general' | 'accessibility'): void {
    this.activeTab = tab;
  }

  // ── General Settings Methods ──────────────────────────────────

  /**
   * Update a display preference toggle.
   *
   * @param key the preference key
   * @param value the new boolean value
   */
  updateDisplayPreference(key: keyof UserDisplayPreferences, value: boolean): void {
    this.userPreferencesService.updatePreference(key, value);
    this.showSuccess(this.translate.instant('preferences.settingUpdated'));
  }

  // ── Accessibility Methods ─────────────────────────────────────

  /**
   * Handle slider / input value change for a single accessibility setting.
   *
   * @param key setting key
   * @param value new numeric value
   */
  onAccessibilitySettingChange(key: string, value: number): void {
    this.accessibilityService.updateSetting(key, value);
  }

  /**
   * Reset a single accessibility setting to its default value.
   *
   * @param key setting key
   */
  resetAccessibilitySetting(key: string): void {
    const setting = this.accessibilityService.currentSettings[key];
    if (setting) {
      this.accessibilityService.updateSetting(key, setting.defaultValue);
    }
  }

  /**
   * Restore all accessibility settings to defaults.
   */
  restoreAccessibilityDefaults(): void {
    this.accessibilityService.restoreDefaults();
    this.showSuccess(this.translate.instant('preferences.accessibilityRestored'));
  }

  // ── Helpers ───────────────────────────────────────────────────

  /**
   * Show a success message and auto-clear after 3 seconds.
   */
  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      if (this.successMessage === message) {
        this.successMessage = null;
      }
    }, 3000);
  }
}
