/*
 * myRC - Accessibility Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 *
 * Description:
 * Page component for configuring UI accessibility preferences.
 * Allows users to adjust font sizes, badge sizes, spacing, and other
 * visual parameters. Changes are applied live via CSS custom properties.
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateModule } from '@ngx-translate/core';
import {
  AccessibilityService,
  AccessibilitySetting,
} from '../../services/accessibility.service';

/**
 * Accessibility settings page component.
 * Provides sliders for configuring UI element sizes with live preview
 * and restore-to-defaults functionality.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-07
 */
@Component({
  selector: 'app-accessibility',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './accessibility.component.html',
  styleUrls: ['./accessibility.component.scss'],
})
export class AccessibilityComponent implements OnInit, OnDestroy {
  /** Settings organised by group for template rendering. */
  groupedSettings: Record<string, AccessibilitySetting[]> = {};

  /** Whether any setting has been customised (enables restore button). */
  hasCustom = false;

  private destroy$ = new Subject<void>();

  constructor(private accessibilityService: AccessibilityService) {}

  ngOnInit(): void {
    this.accessibilityService.settings$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.groupedSettings = this.accessibilityService.getGroupedSettings();
        this.hasCustom = this.accessibilityService.hasCustomSettings();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle slider / input value change for a single setting.
   *
   * @param key Setting key
   * @param value New numeric value
   */
  onSettingChange(key: string, value: number): void {
    this.accessibilityService.updateSetting(key, value);
  }

  /**
   * Reset a single setting to its default value.
   *
   * @param key Setting key
   */
  resetSetting(key: string): void {
    const setting = this.accessibilityService.currentSettings[key];
    if (setting) {
      this.accessibilityService.updateSetting(key, setting.defaultValue);
    }
  }

  /**
   * Restore all settings to defaults.
   */
  restoreDefaults(): void {
    this.accessibilityService.restoreDefaults();
  }
}
