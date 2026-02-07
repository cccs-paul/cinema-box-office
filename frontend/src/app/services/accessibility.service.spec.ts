/**
 * myRC - Accessibility Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for AccessibilityService — validates CSS variable injection,
 * localStorage persistence, restore-defaults, and clamping logic.
 */
import { TestBed } from '@angular/core/testing';
import { AccessibilityService } from './accessibility.service';

describe('AccessibilityService', () => {
  let service: AccessibilityService;

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.style.cssText = '';

    TestBed.configureTestingModule({
      providers: [AccessibilityService],
    });

    service = TestBed.inject(AccessibilityService);
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.style.cssText = '';
  });

  // ── Initialisation ────────────────────────────────────────────

  describe('initialisation', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should expose settings$ observable', (done) => {
      service.settings$.subscribe((settings) => {
        expect(settings).toBeTruthy();
        expect(Object.keys(settings).length).toBeGreaterThan(0);
        done();
      });
    });

    it('should set CSS custom properties on the document root', () => {
      const value = document.documentElement.style.getPropertyValue(
        '--a11y-tracking-badge-font-size'
      );
      expect(value).toBeTruthy();
      expect(value).toContain('rem');
    });

    it('should have default value for tracking badge font size', () => {
      const settings = service.currentSettings;
      expect(settings['tracking-badge-font-size']).toBeTruthy();
      expect(settings['tracking-badge-font-size'].value).toBe(0.65);
    });

    it('should have default value for item name font size', () => {
      const settings = service.currentSettings;
      expect(settings['item-name-font-size']).toBeTruthy();
      expect(settings['item-name-font-size'].value).toBe(0.9);
    });
  });

  // ── updateSetting ─────────────────────────────────────────────

  describe('updateSetting', () => {
    it('should update a setting value', () => {
      service.updateSetting('tracking-badge-font-size', 0.85);
      expect(service.currentSettings['tracking-badge-font-size'].value).toBe(0.85);
    });

    it('should clamp value to max', () => {
      const max = service.currentSettings['tracking-badge-font-size'].max;
      service.updateSetting('tracking-badge-font-size', max + 10);
      expect(service.currentSettings['tracking-badge-font-size'].value).toBe(max);
    });

    it('should clamp value to min', () => {
      const min = service.currentSettings['tracking-badge-font-size'].min;
      service.updateSetting('tracking-badge-font-size', min - 10);
      expect(service.currentSettings['tracking-badge-font-size'].value).toBe(min);
    });

    it('should update the corresponding CSS custom property', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      const value = document.documentElement.style.getPropertyValue(
        '--a11y-tracking-badge-font-size'
      );
      expect(value).toBe('1rem');
    });

    it('should persist the change to localStorage', () => {
      service.updateSetting('tracking-badge-font-size', 0.8);
      const stored = JSON.parse(localStorage.getItem('accessibilitySettings')!);
      expect(stored['tracking-badge-font-size']).toBe(0.8);
    });

    it('should ignore unknown keys', () => {
      const before = { ...service.currentSettings };
      service.updateSetting('nonexistent-key', 99);
      expect(service.currentSettings).toEqual(before);
    });
  });

  // ── restoreDefaults ───────────────────────────────────────────

  describe('restoreDefaults', () => {
    it('should reset all values to defaults', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      service.updateSetting('item-name-font-size', 1.2);

      service.restoreDefaults();

      expect(service.currentSettings['tracking-badge-font-size'].value).toBe(0.65);
      expect(service.currentSettings['item-name-font-size'].value).toBe(0.9);
    });

    it('should update CSS variables to default values', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      service.restoreDefaults();
      const value = document.documentElement.style.getPropertyValue(
        '--a11y-tracking-badge-font-size'
      );
      expect(value).toBe('0.65rem');
    });

    it('should persist defaults to localStorage', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      service.restoreDefaults();
      const stored = JSON.parse(localStorage.getItem('accessibilitySettings')!);
      expect(stored['tracking-badge-font-size']).toBe(0.65);
    });
  });

  // ── hasCustomSettings ─────────────────────────────────────────

  describe('hasCustomSettings', () => {
    it('should return false when all values are default', () => {
      expect(service.hasCustomSettings()).toBeFalse();
    });

    it('should return true when any value differs from default', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      expect(service.hasCustomSettings()).toBeTrue();
    });

    it('should return false after restoring defaults', () => {
      service.updateSetting('tracking-badge-font-size', 1.0);
      service.restoreDefaults();
      expect(service.hasCustomSettings()).toBeFalse();
    });
  });

  // ── getGroupedSettings ────────────────────────────────────────

  describe('getGroupedSettings', () => {
    it('should return settings grouped by group key', () => {
      const grouped = service.getGroupedSettings();
      expect(grouped['fontSizes']).toBeTruthy();
      expect(grouped['elementSizes']).toBeTruthy();
    });

    it('should include all font size settings in the fontSizes group', () => {
      const grouped = service.getGroupedSettings();
      const keys = grouped['fontSizes'].map((s) => s.key);
      expect(keys).toContain('tracking-badge-font-size');
      expect(keys).toContain('item-name-font-size');
      expect(keys).toContain('body-font-size');
    });

    it('should include element size settings in the elementSizes group', () => {
      const grouped = service.getGroupedSettings();
      const keys = grouped['elementSizes'].map((s) => s.key);
      expect(keys).toContain('badge-padding-vertical');
      expect(keys).toContain('badge-padding-horizontal');
      expect(keys).toContain('item-row-padding');
    });
  });

  // ── localStorage restoration ──────────────────────────────────

  describe('localStorage restoration', () => {
    it('should restore saved settings on construction', () => {
      localStorage.setItem(
        'accessibilitySettings',
        JSON.stringify({ 'tracking-badge-font-size': 0.9 })
      );

      const newService = new AccessibilityService();
      expect(newService.currentSettings['tracking-badge-font-size'].value).toBe(0.9);
    });

    it('should clamp restored values within valid range', () => {
      localStorage.setItem(
        'accessibilitySettings',
        JSON.stringify({ 'tracking-badge-font-size': 999 })
      );

      const newService = new AccessibilityService();
      const max = newService.currentSettings['tracking-badge-font-size'].max;
      expect(newService.currentSettings['tracking-badge-font-size'].value).toBe(max);
    });

    it('should handle corrupted localStorage gracefully', () => {
      localStorage.setItem('accessibilitySettings', 'NOT_JSON');

      const newService = new AccessibilityService();
      // Should fall back to defaults without throwing
      expect(newService.currentSettings['tracking-badge-font-size'].value).toBe(0.65);
    });

    it('should ignore unknown keys in localStorage', () => {
      localStorage.setItem(
        'accessibilitySettings',
        JSON.stringify({ 'unknown-setting': 42 })
      );

      const newService = new AccessibilityService();
      expect(newService.currentSettings['unknown-setting']).toBeUndefined();
    });
  });

  // ── Observable emission ───────────────────────────────────────

  describe('observable emission', () => {
    it('should emit updated settings when a value changes', (done) => {
      let emitCount = 0;
      service.settings$.subscribe((settings) => {
        emitCount++;
        if (emitCount === 2) {
          expect(settings['tracking-badge-font-size'].value).toBe(0.8);
          done();
        }
      });
      service.updateSetting('tracking-badge-font-size', 0.8);
    });

    it('should emit when restoring defaults', (done) => {
      service.updateSetting('tracking-badge-font-size', 1.0);

      let emitCount = 0;
      service.settings$.subscribe((settings) => {
        emitCount++;
        if (emitCount === 2) {
          expect(settings['tracking-badge-font-size'].value).toBe(0.65);
          done();
        }
      });
      service.restoreDefaults();
    });
  });
});
