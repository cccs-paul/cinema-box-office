/**
 * myRC - User Preferences Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-11
 * Version: 1.0.0
 */
import { TestBed } from '@angular/core/testing';
import { UserPreferencesService, UserDisplayPreferences } from './user-preferences.service';

describe('UserPreferencesService', () => {
  let service: UserPreferencesService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [UserPreferencesService]
    });
    service = TestBed.inject(UserPreferencesService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should have default preferences', () => {
      const prefs = service.current;
      expect(prefs.showSearchBox).toBeTrue();
      expect(prefs.showCategoryFilter).toBeTrue();
      expect(prefs.groupByCategory).toBeFalse();
    });

    it('should emit default preferences via observable', (done: DoneFn) => {
      service.preferences$.subscribe(prefs => {
        expect(prefs.showSearchBox).toBeTrue();
        expect(prefs.showCategoryFilter).toBeTrue();
        expect(prefs.groupByCategory).toBeFalse();
        done();
      });
    });

    it('should load saved preferences from localStorage', () => {
      const saved: UserDisplayPreferences = {
        showSearchBox: false,
        showCategoryFilter: false,
        groupByCategory: true
      };
      localStorage.setItem('userDisplayPreferences', JSON.stringify(saved));

      const freshService = new UserPreferencesService();
      expect(freshService.current.showSearchBox).toBeFalse();
      expect(freshService.current.showCategoryFilter).toBeFalse();
      expect(freshService.current.groupByCategory).toBeTrue();
    });

    it('should fallback to defaults for invalid localStorage data', () => {
      localStorage.setItem('userDisplayPreferences', 'invalid-json');

      const freshService = new UserPreferencesService();
      expect(freshService.current.showSearchBox).toBeTrue();
      expect(freshService.current.groupByCategory).toBeFalse();
    });
  });

  describe('updatePreference', () => {
    it('should update a single preference', () => {
      service.updatePreference('showSearchBox', false);
      expect(service.current.showSearchBox).toBeFalse();
      expect(service.current.showCategoryFilter).toBeTrue();
    });

    it('should persist changes to localStorage', () => {
      service.updatePreference('groupByCategory', true);

      const stored = JSON.parse(localStorage.getItem('userDisplayPreferences')!);
      expect(stored.groupByCategory).toBeTrue();
    });

    it('should emit updated preferences via observable', (done: DoneFn) => {
      let emitCount = 0;
      service.preferences$.subscribe(prefs => {
        emitCount++;
        if (emitCount === 2) {
          expect(prefs.showCategoryFilter).toBeFalse();
          done();
        }
      });

      service.updatePreference('showCategoryFilter', false);
    });
  });

  describe('restoreDefaults', () => {
    it('should restore all preferences to defaults', () => {
      service.updatePreference('showSearchBox', false);
      service.updatePreference('showCategoryFilter', false);
      service.updatePreference('groupByCategory', true);

      service.restoreDefaults();

      expect(service.current.showSearchBox).toBeTrue();
      expect(service.current.showCategoryFilter).toBeTrue();
      expect(service.current.groupByCategory).toBeFalse();
    });

    it('should persist restored defaults to localStorage', () => {
      service.updatePreference('showSearchBox', false);
      service.restoreDefaults();

      const stored = JSON.parse(localStorage.getItem('userDisplayPreferences')!);
      expect(stored.showSearchBox).toBeTrue();
    });
  });

  describe('hasCustomPreferences', () => {
    it('should return false when all defaults', () => {
      expect(service.hasCustomPreferences()).toBeFalse();
    });

    it('should return true when a preference differs from default', () => {
      service.updatePreference('groupByCategory', true);
      expect(service.hasCustomPreferences()).toBeTrue();
    });

    it('should return false after restoring defaults', () => {
      service.updatePreference('showSearchBox', false);
      expect(service.hasCustomPreferences()).toBeTrue();

      service.restoreDefaults();
      expect(service.hasCustomPreferences()).toBeFalse();
    });
  });
});
