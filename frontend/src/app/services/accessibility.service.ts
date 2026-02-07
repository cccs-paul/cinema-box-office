/*
 * myRC - Accessibility Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 *
 * Description:
 * Service for managing UI accessibility preferences such as font sizes,
 * badge sizes, and spacing. Persists settings to localStorage and injects
 * CSS custom properties on the document root so all components react
 * dynamically.
 */
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Represents a single configurable UI size setting.
 */
export interface AccessibilitySetting {
  /** Unique key used for CSS variable name and storage. */
  key: string;
  /** i18n translation key for the label. */
  labelKey: string;
  /** i18n translation key for the description. */
  descriptionKey: string;
  /** Group this setting belongs to (for UI organisation). */
  group: string;
  /** Default value in rem. */
  defaultValue: number;
  /** Current value in rem. */
  value: number;
  /** Minimum allowed value in rem. */
  min: number;
  /** Maximum allowed value in rem. */
  max: number;
  /** Step increment for the slider. */
  step: number;
  /** CSS unit to use (default: 'rem'). */
  unit: string;
}

/**
 * Full map of all accessibility settings keyed by setting key.
 */
export type AccessibilitySettings = Record<string, AccessibilitySetting>;

/**
 * Service for managing UI accessibility size preferences.
 * Injects CSS custom properties (--a11y-*) on the document root element
 * and persists choices to localStorage.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-07
 */
@Injectable({
  providedIn: 'root',
})
export class AccessibilityService {
  private readonly STORAGE_KEY = 'accessibilitySettings';

  /**
   * Default settings definitions.
   * Each entry produces a CSS variable: --a11y-{key}.
   */
  private readonly defaultSettings: AccessibilitySetting[] = [
    // ── Font Sizes ──────────────────────────────────────────────
    {
      key: 'item-name-font-size',
      labelKey: 'accessibility.itemNameFontSize',
      descriptionKey: 'accessibility.itemNameFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.9,
      value: 0.9,
      min: 0.7,
      max: 1.4,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'tracking-badge-font-size',
      labelKey: 'accessibility.trackingBadgeFontSize',
      descriptionKey: 'accessibility.trackingBadgeFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.65,
      value: 0.65,
      min: 0.5,
      max: 1.1,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'category-badge-font-size',
      labelKey: 'accessibility.categoryBadgeFontSize',
      descriptionKey: 'accessibility.categoryBadgeFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.75,
      value: 0.75,
      min: 0.55,
      max: 1.2,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'status-badge-font-size',
      labelKey: 'accessibility.statusBadgeFontSize',
      descriptionKey: 'accessibility.statusBadgeFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.7,
      value: 0.7,
      min: 0.5,
      max: 1.1,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'currency-badge-font-size',
      labelKey: 'accessibility.currencyBadgeFontSize',
      descriptionKey: 'accessibility.currencyBadgeFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.8,
      value: 0.8,
      min: 0.6,
      max: 1.2,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'total-amount-font-size',
      labelKey: 'accessibility.totalAmountFontSize',
      descriptionKey: 'accessibility.totalAmountFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.9,
      value: 0.9,
      min: 0.7,
      max: 1.4,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'body-font-size',
      labelKey: 'accessibility.bodyFontSize',
      descriptionKey: 'accessibility.bodyFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 0.875,
      value: 0.875,
      min: 0.75,
      max: 1.5,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'header-font-size',
      labelKey: 'accessibility.headerFontSize',
      descriptionKey: 'accessibility.headerFontSizeDesc',
      group: 'fontSizes',
      defaultValue: 1.25,
      value: 1.25,
      min: 1.0,
      max: 2.0,
      step: 0.05,
      unit: 'rem',
    },

    // ── Badge & Element Sizes ───────────────────────────────────
    {
      key: 'badge-padding-vertical',
      labelKey: 'accessibility.badgePaddingVertical',
      descriptionKey: 'accessibility.badgePaddingVerticalDesc',
      group: 'elementSizes',
      defaultValue: 0.15,
      value: 0.15,
      min: 0.05,
      max: 0.5,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'badge-padding-horizontal',
      labelKey: 'accessibility.badgePaddingHorizontal',
      descriptionKey: 'accessibility.badgePaddingHorizontalDesc',
      group: 'elementSizes',
      defaultValue: 0.5,
      value: 0.5,
      min: 0.2,
      max: 1.0,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'badge-border-radius',
      labelKey: 'accessibility.badgeBorderRadius',
      descriptionKey: 'accessibility.badgeBorderRadiusDesc',
      group: 'elementSizes',
      defaultValue: 0.25,
      value: 0.25,
      min: 0.0,
      max: 1.0,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'item-row-padding',
      labelKey: 'accessibility.itemRowPadding',
      descriptionKey: 'accessibility.itemRowPaddingDesc',
      group: 'elementSizes',
      defaultValue: 0.625,
      value: 0.625,
      min: 0.25,
      max: 1.5,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'item-gap',
      labelKey: 'accessibility.itemGap',
      descriptionKey: 'accessibility.itemGapDesc',
      group: 'elementSizes',
      defaultValue: 0.75,
      value: 0.75,
      min: 0.25,
      max: 2.0,
      step: 0.05,
      unit: 'rem',
    },
    {
      key: 'sidebar-width',
      labelKey: 'accessibility.sidebarWidth',
      descriptionKey: 'accessibility.sidebarWidthDesc',
      group: 'elementSizes',
      defaultValue: 14,
      value: 14,
      min: 10,
      max: 22,
      step: 0.5,
      unit: 'rem',
    },
  ];

  private settingsSubject: BehaviorSubject<AccessibilitySettings>;

  /** Observable of the current accessibility settings map. */
  public settings$: Observable<AccessibilitySettings>;

  constructor() {
    const settings = this.buildSettingsMap(this.defaultSettings);
    this.loadFromStorage(settings);
    this.settingsSubject = new BehaviorSubject<AccessibilitySettings>(settings);
    this.settings$ = this.settingsSubject.asObservable();
    this.applyAllCssVariables(settings);
  }

  /**
   * Get the current settings snapshot.
   */
  get currentSettings(): AccessibilitySettings {
    return this.settingsSubject.value;
  }

  /**
   * Update a single setting value.
   *
   * @param key Setting key
   * @param value New value
   */
  updateSetting(key: string, value: number): void {
    const settings = { ...this.currentSettings };
    const setting = settings[key];
    if (!setting) {
      return;
    }
    const clamped = Math.min(setting.max, Math.max(setting.min, value));
    settings[key] = { ...setting, value: clamped };
    this.settingsSubject.next(settings);
    this.applyCssVariable(key, clamped, setting.unit);
    this.saveToStorage(settings);
  }

  /**
   * Restore all settings to their defaults.
   */
  restoreDefaults(): void {
    const settings = this.buildSettingsMap(this.defaultSettings);
    this.settingsSubject.next(settings);
    this.applyAllCssVariables(settings);
    this.saveToStorage(settings);
  }

  /**
   * Check whether any setting differs from its default.
   */
  hasCustomSettings(): boolean {
    const settings = this.currentSettings;
    return Object.values(settings).some(s => s.value !== s.defaultValue);
  }

  /**
   * Get settings grouped by their group key.
   */
  getGroupedSettings(): Record<string, AccessibilitySetting[]> {
    const settings = this.currentSettings;
    const groups: Record<string, AccessibilitySetting[]> = {};
    for (const setting of Object.values(settings)) {
      if (!groups[setting.group]) {
        groups[setting.group] = [];
      }
      groups[setting.group].push(setting);
    }
    return groups;
  }

  /**
   * Build a keyed map from the flat default settings list.
   */
  private buildSettingsMap(defaults: AccessibilitySetting[]): AccessibilitySettings {
    const map: AccessibilitySettings = {};
    for (const def of defaults) {
      map[def.key] = { ...def };
    }
    return map;
  }

  /**
   * Load saved values from localStorage into the settings map.
   */
  private loadFromStorage(settings: AccessibilitySettings): void {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const parsed: Record<string, number> = JSON.parse(stored);
        for (const [key, value] of Object.entries(parsed)) {
          if (settings[key] && typeof value === 'number') {
            settings[key].value = Math.min(
              settings[key].max,
              Math.max(settings[key].min, value)
            );
          }
        }
      }
    } catch {
      // Corrupted data; ignore and use defaults
    }
  }

  /**
   * Persist current values to localStorage.
   */
  private saveToStorage(settings: AccessibilitySettings): void {
    const values: Record<string, number> = {};
    for (const [key, setting] of Object.entries(settings)) {
      values[key] = setting.value;
    }
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(values));
  }

  /**
   * Apply a single CSS custom property on the document root.
   */
  private applyCssVariable(key: string, value: number, unit: string): void {
    document.documentElement.style.setProperty(
      `--a11y-${key}`,
      `${value}${unit}`
    );
  }

  /**
   * Apply all CSS custom properties at once.
   */
  private applyAllCssVariables(settings: AccessibilitySettings): void {
    for (const [key, setting] of Object.entries(settings)) {
      this.applyCssVariable(key, setting.value, setting.unit);
    }
  }
}
