/**
 * myRC - Accessibility Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for the Accessibility settings page component.
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { AccessibilityComponent } from './accessibility.component';
import {
  AccessibilityService,
  AccessibilitySettings,
  AccessibilitySetting,
} from '../../services/accessibility.service';

describe('AccessibilityComponent', () => {
  let component: AccessibilityComponent;
  let fixture: ComponentFixture<AccessibilityComponent>;
  let accessibilityService: jasmine.SpyObj<AccessibilityService>;
  let settingsSubject: BehaviorSubject<AccessibilitySettings>;

  const mockSetting = (overrides: Partial<AccessibilitySetting> = {}): AccessibilitySetting => ({
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
    ...overrides,
  });

  const mockSettings: AccessibilitySettings = {
    'tracking-badge-font-size': mockSetting(),
    'item-name-font-size': mockSetting({
      key: 'item-name-font-size',
      labelKey: 'accessibility.itemNameFontSize',
      descriptionKey: 'accessibility.itemNameFontSizeDesc',
      defaultValue: 0.9,
      value: 0.9,
    }),
    'badge-padding-vertical': mockSetting({
      key: 'badge-padding-vertical',
      labelKey: 'accessibility.badgePaddingVertical',
      descriptionKey: 'accessibility.badgePaddingVerticalDesc',
      group: 'elementSizes',
      defaultValue: 0.15,
      value: 0.15,
    }),
  };

  const mockGrouped: Record<string, AccessibilitySetting[]> = {
    fontSizes: [
      mockSettings['tracking-badge-font-size'],
      mockSettings['item-name-font-size'],
    ],
    elementSizes: [mockSettings['badge-padding-vertical']],
  };

  beforeEach(async () => {
    settingsSubject = new BehaviorSubject<AccessibilitySettings>(mockSettings);

    accessibilityService = jasmine.createSpyObj(
      'AccessibilityService',
      ['updateSetting', 'restoreDefaults', 'hasCustomSettings', 'getGroupedSettings'],
      { settings$: settingsSubject.asObservable(), currentSettings: mockSettings }
    );
    accessibilityService.hasCustomSettings.and.returnValue(false);
    accessibilityService.getGroupedSettings.and.returnValue(mockGrouped);

    await TestBed.configureTestingModule({
      imports: [AccessibilityComponent, FormsModule, TranslateModule.forRoot()],
    })
      .overrideProvider(AccessibilityService, { useValue: accessibilityService })
      .compileComponents();

    fixture = TestBed.createComponent(AccessibilityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Creation ──────────────────────────────────────────────────

  describe('creation', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load grouped settings on init', () => {
      expect(component.groupedSettings).toBeTruthy();
      expect(component.groupedSettings['fontSizes']).toBeTruthy();
      expect(component.groupedSettings['elementSizes']).toBeTruthy();
    });

    it('should set hasCustom to false when no custom settings', () => {
      expect(component.hasCustom).toBeFalse();
    });
  });

  // ── Rendering ─────────────────────────────────────────────────

  describe('rendering', () => {
    it('should render font size settings group', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const groups = compiled.querySelectorAll('.settings-group');
      expect(groups.length).toBe(2);
    });

    it('should render setting sliders', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const sliders = compiled.querySelectorAll('.setting-slider');
      expect(sliders.length).toBe(3);
    });

    it('should render the preview section', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const preview = compiled.querySelector('.preview-section');
      expect(preview).toBeTruthy();
    });

    it('should render restore defaults button', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const btn = compiled.querySelector('.btn-secondary');
      expect(btn).toBeTruthy();
    });

    it('should disable restore button when no custom settings', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const btn = compiled.querySelector('.btn-secondary') as HTMLButtonElement;
      expect(btn.disabled).toBeTrue();
    });

    it('should enable restore button when custom settings exist', () => {
      accessibilityService.hasCustomSettings.and.returnValue(true);
      settingsSubject.next(mockSettings);
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const btn = compiled.querySelector('.btn-secondary') as HTMLButtonElement;
      expect(btn.disabled).toBeFalse();
    });
  });

  // ── Interaction ───────────────────────────────────────────────

  describe('interaction', () => {
    it('should call updateSetting when onSettingChange is invoked', () => {
      component.onSettingChange('tracking-badge-font-size', 0.8);
      expect(accessibilityService.updateSetting).toHaveBeenCalledWith(
        'tracking-badge-font-size',
        0.8
      );
    });

    it('should call restoreDefaults when restore button is clicked', () => {
      accessibilityService.hasCustomSettings.and.returnValue(true);
      settingsSubject.next(mockSettings);
      fixture.detectChanges();

      component.restoreDefaults();
      expect(accessibilityService.restoreDefaults).toHaveBeenCalled();
    });

    it('should call updateSetting with defaultValue when resetSetting is invoked', () => {
      component.resetSetting('tracking-badge-font-size');
      expect(accessibilityService.updateSetting).toHaveBeenCalledWith(
        'tracking-badge-font-size',
        0.65
      );
    });
  });

  // ── Lifecycle ─────────────────────────────────────────────────

  describe('lifecycle', () => {
    it('should complete destroy$ on ngOnDestroy', () => {
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });

    it('should update groupedSettings when settings change', () => {
      const updatedGrouped = {
        fontSizes: [mockSettings['tracking-badge-font-size']],
        elementSizes: [],
      };
      accessibilityService.getGroupedSettings.and.returnValue(updatedGrouped);

      settingsSubject.next(mockSettings);
      fixture.detectChanges();

      expect(component.groupedSettings['fontSizes'].length).toBe(1);
    });
  });
});
