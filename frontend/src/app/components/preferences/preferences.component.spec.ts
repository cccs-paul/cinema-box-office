/**
 * myRC - Preferences Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-11
 * Version: 1.0.0
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { PreferencesComponent } from './preferences.component';
import {
  UserPreferencesService,
  UserDisplayPreferences,
} from '../../services/user-preferences.service';
import {
  AccessibilityService,
  AccessibilitySetting,
} from '../../services/accessibility.service';

describe('PreferencesComponent', () => {
  let component: PreferencesComponent;
  let fixture: ComponentFixture<PreferencesComponent>;
  let userPrefsService: jasmine.SpyObj<UserPreferencesService>;
  let accessibilityService: jasmine.SpyObj<AccessibilityService>;

  let preferences$: BehaviorSubject<UserDisplayPreferences>;
  let settings$: BehaviorSubject<Record<string, AccessibilitySetting>>;

  const defaultPrefs: UserDisplayPreferences = {
    showSearchBox: true,
    showCategoryFilter: true,
    groupByCategory: false,
  };

  const mockSettings: Record<string, AccessibilitySetting> = {
    fontSize: {
      key: 'fontSize',
      labelKey: 'accessibility.fontSize',
      descriptionKey: 'accessibility.fontSizeDesc',
      group: 'Font',
      value: 16,
      defaultValue: 16,
      min: 10,
      max: 32,
      step: 1,
      unit: 'rem',
    },
  };

  beforeEach(async () => {
    preferences$ = new BehaviorSubject<UserDisplayPreferences>({ ...defaultPrefs });
    settings$ = new BehaviorSubject<Record<string, AccessibilitySetting>>({ ...mockSettings });

    userPrefsService = jasmine.createSpyObj('UserPreferencesService',
      ['updatePreference', 'restoreDefaults', 'hasCustomPreferences'],
      { preferences$: preferences$.asObservable() }
    );
    userPrefsService.hasCustomPreferences.and.returnValue(false);

    accessibilityService = jasmine.createSpyObj('AccessibilityService',
      ['updateSetting', 'restoreDefaults', 'hasCustomSettings', 'getGroupedSettings'],
      {
        settings$: settings$.asObservable(),
        currentSettings: mockSettings,
      }
    );
    accessibilityService.hasCustomSettings.and.returnValue(false);
    accessibilityService.getGroupedSettings.and.returnValue({ Font: [mockSettings['fontSize']] });

    await TestBed.configureTestingModule({
      imports: [PreferencesComponent, FormsModule, TranslateModule.forRoot()],
    })
      .overrideProvider(UserPreferencesService, { useValue: userPrefsService })
      .overrideProvider(AccessibilityService, { useValue: accessibilityService })
      .compileComponents();

    fixture = TestBed.createComponent(PreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should start with general tab active', () => {
      expect(component.activeTab).toBe('general');
    });

    it('should initialise preferences from service', () => {
      expect(component.preferences.showSearchBox).toBeTrue();
      expect(component.preferences.showCategoryFilter).toBeTrue();
      expect(component.preferences.groupByCategory).toBeFalse();
    });

    it('should initialise accessibility grouped settings', () => {
      expect(component.groupedSettings).toBeTruthy();
      expect(accessibilityService.getGroupedSettings).toHaveBeenCalled();
    });

    it('should set hasCustomAccessibility from service', () => {
      expect(component.hasCustomAccessibility).toBeFalse();
    });
  });

  describe('Tab switching', () => {
    it('should switch to accessibility tab', () => {
      component.setActiveTab('accessibility');
      expect(component.activeTab).toBe('accessibility');
    });

    it('should switch to general tab', () => {
      component.setActiveTab('accessibility');
      component.setActiveTab('general');
      expect(component.activeTab).toBe('general');
    });
  });

  describe('General Settings', () => {
    it('should update display preference via service', () => {
      component.updateDisplayPreference('showSearchBox', false);
      expect(userPrefsService.updatePreference).toHaveBeenCalledWith('showSearchBox', false);
    });

    it('should show success message after preference update', () => {
      component.updateDisplayPreference('groupByCategory', true);
      expect(component.successMessage).toBeTruthy();
    });

    it('should auto-clear success message after timeout', fakeAsync(() => {
      component.updateDisplayPreference('showSearchBox', false);
      expect(component.successMessage).toBeTruthy();

      tick(3000);
      expect(component.successMessage).toBeNull();
    }));

    it('should react to preference changes from service', () => {
      const updated: UserDisplayPreferences = {
        showSearchBox: false,
        showCategoryFilter: false,
        groupByCategory: true,
      };
      preferences$.next(updated);

      expect(component.preferences.showSearchBox).toBeFalse();
      expect(component.preferences.showCategoryFilter).toBeFalse();
      expect(component.preferences.groupByCategory).toBeTrue();
    });
  });

  describe('Accessibility Settings', () => {
    it('should call accessibilityService.updateSetting on change', () => {
      component.onAccessibilitySettingChange('fontSize', 20);
      expect(accessibilityService.updateSetting).toHaveBeenCalledWith('fontSize', 20);
    });

    it('should reset a single accessibility setting', () => {
      component.resetAccessibilitySetting('fontSize');
      expect(accessibilityService.updateSetting).toHaveBeenCalledWith('fontSize', 16);
    });

    it('should restore all accessibility defaults', () => {
      component.restoreAccessibilityDefaults();
      expect(accessibilityService.restoreDefaults).toHaveBeenCalled();
    });

    it('should show success message after restoring defaults', () => {
      component.restoreAccessibilityDefaults();
      expect(component.successMessage).toBeTruthy();
    });

    it('should react to settings changes from service', () => {
      accessibilityService.hasCustomSettings.and.returnValue(true);
      settings$.next({ ...mockSettings });

      expect(component.hasCustomAccessibility).toBeTrue();
    });
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', () => {
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });
});
