/**
 * myRC - Language Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { LanguageService, Language, LANGUAGES } from './language.service';

describe('LanguageService', () => {
  let service: LanguageService;
  let translateService: jasmine.SpyObj<TranslateService>;
  let localStorageGetSpy: jasmine.Spy;
  let localStorageSetSpy: jasmine.Spy;

  beforeEach(() => {
    // Create spy for TranslateService
    translateService = jasmine.createSpyObj('TranslateService', [
      'addLangs', 'setDefaultLang', 'use', 'instant', 'get'
    ]);
    translateService.get.and.returnValue(of('translated'));
    translateService.instant.and.returnValue('instant translation');

    // Spy on localStorage
    localStorageGetSpy = spyOn(localStorage, 'getItem').and.returnValue(null);
    localStorageSetSpy = spyOn(localStorage, 'setItem');

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        LanguageService,
        { provide: TranslateService, useValue: translateService }
      ]
    });

    service = TestBed.inject(LanguageService);
  });

  describe('Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should set available languages on TranslateService', () => {
      expect(translateService.addLangs).toHaveBeenCalledWith(['en', 'fr']);
    });

    it('should set default language to English', () => {
      expect(translateService.setDefaultLang).toHaveBeenCalledWith('en');
    });

    it('should load saved language from localStorage', () => {
      expect(localStorageGetSpy).toHaveBeenCalledWith('myrc_language');
    });

    it('should use English when no saved language', () => {
      expect(service.getCurrentLanguage()).toBe('en');
    });
  });

  describe('Saved language loading', () => {
    it('should use saved English language', () => {
      localStorageGetSpy.and.returnValue('en');
      
      // Recreate service to trigger initialization with new localStorage value
      service = new LanguageService(translateService);
      
      expect(translateService.use).toHaveBeenCalledWith('en');
    });

    it('should use saved French language', () => {
      localStorageGetSpy.and.returnValue('fr');
      
      service = new LanguageService(translateService);
      
      expect(translateService.use).toHaveBeenCalledWith('fr');
    });

    it('should fallback to English for invalid saved value', () => {
      localStorageGetSpy.and.returnValue('invalid');
      
      service = new LanguageService(translateService);
      
      expect(translateService.use).toHaveBeenCalledWith('en');
    });

    it('should handle localStorage errors gracefully', () => {
      localStorageGetSpy.and.throwError('Storage error');
      
      expect(() => {
        service = new LanguageService(translateService);
      }).not.toThrow();
      
      expect(service.getCurrentLanguage()).toBe('en');
    });
  });

  describe('getCurrentLanguage', () => {
    it('should return current language', () => {
      expect(service.getCurrentLanguage()).toBe('en');
    });
  });

  describe('getOtherLanguage', () => {
    it('should return French when current is English', () => {
      expect(service.getOtherLanguage()).toBe('fr');
    });

    it('should return English when current is French', () => {
      service.setLanguage('fr');
      expect(service.getOtherLanguage()).toBe('en');
    });
  });

  describe('getOtherLanguageNativeName', () => {
    it('should return "Français" when current is English', () => {
      expect(service.getOtherLanguageNativeName()).toBe('Français');
    });

    it('should return "English" when current is French', () => {
      service.setLanguage('fr');
      expect(service.getOtherLanguageNativeName()).toBe('English');
    });
  });

  describe('getLanguageConfig', () => {
    it('should return English config', () => {
      const config = service.getLanguageConfig('en');
      expect(config).toEqual({ code: 'en', name: 'English', nativeName: 'English' });
    });

    it('should return French config', () => {
      const config = service.getLanguageConfig('fr');
      expect(config).toEqual({ code: 'fr', name: 'French', nativeName: 'Français' });
    });

    it('should return undefined for invalid code', () => {
      const config = service.getLanguageConfig('invalid' as Language);
      expect(config).toBeUndefined();
    });
  });

  describe('setLanguage', () => {
    it('should call translate.use with language code', () => {
      service.setLanguage('fr');
      expect(translateService.use).toHaveBeenCalledWith('fr');
    });

    it('should update currentLanguageSubject', () => {
      service.setLanguage('fr');
      expect(service.getCurrentLanguage()).toBe('fr');
    });

    it('should save language to localStorage', () => {
      service.setLanguage('fr');
      expect(localStorageSetSpy).toHaveBeenCalledWith('myrc_language', 'fr');
    });

    it('should handle localStorage errors when saving', () => {
      localStorageSetSpy.and.throwError('Storage error');
      
      expect(() => {
        service.setLanguage('fr');
      }).not.toThrow();
    });
  });

  describe('toggleLanguage', () => {
    it('should switch from English to French', () => {
      service.toggleLanguage();
      expect(service.getCurrentLanguage()).toBe('fr');
    });

    it('should switch from French to English', () => {
      service.setLanguage('fr');
      service.toggleLanguage();
      expect(service.getCurrentLanguage()).toBe('en');
    });
  });

  describe('currentLanguage$', () => {
    it('should emit current language', (done) => {
      service.currentLanguage$.subscribe(lang => {
        expect(lang).toBe('en');
        done();
      });
    });

    it('should emit when language changes', (done) => {
      const emitted: Language[] = [];
      
      service.currentLanguage$.subscribe(lang => {
        emitted.push(lang);
        if (emitted.length === 2) {
          expect(emitted).toEqual(['en', 'fr']);
          done();
        }
      });
      
      service.setLanguage('fr');
    });
  });

  describe('instant', () => {
    it('should delegate to TranslateService.instant', () => {
      const result = service.instant('test.key');
      expect(translateService.instant).toHaveBeenCalledWith('test.key', undefined);
      expect(result).toBe('instant translation');
    });

    it('should pass params to TranslateService', () => {
      const params = { name: 'John' };
      service.instant('test.key', params);
      expect(translateService.instant).toHaveBeenCalledWith('test.key', params);
    });
  });

  describe('get', () => {
    it('should delegate to TranslateService.get', (done) => {
      service.get('test.key').subscribe(result => {
        expect(translateService.get).toHaveBeenCalledWith('test.key', undefined);
        expect(result).toBe('translated');
        done();
      });
    });

    it('should pass params to TranslateService', () => {
      const params = { count: 5 };
      service.get('test.key', params);
      expect(translateService.get).toHaveBeenCalledWith('test.key', params);
    });
  });

  describe('availableLanguages', () => {
    it('should expose LANGUAGES constant', () => {
      expect(service.availableLanguages).toBe(LANGUAGES);
    });

    it('should have 2 languages', () => {
      expect(service.availableLanguages.length).toBe(2);
    });
  });
});
