/*
 * myRC - Language Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Supported languages for the application.
 */
export type Language = 'en' | 'fr';

/**
 * Language configuration.
 */
export interface LanguageConfig {
  code: Language;
  name: string;
  nativeName: string;
}

/**
 * Available languages in the application.
 */
export const LANGUAGES: LanguageConfig[] = [
  { code: 'en', name: 'English', nativeName: 'English' },
  { code: 'fr', name: 'French', nativeName: 'Français' }
];

/**
 * Service for managing application language and translations.
 * Provides language switching, persistence, and translation utilities.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private readonly STORAGE_KEY = 'myrc_language';
  private readonly DEFAULT_LANGUAGE: Language = 'en';
  
  private currentLanguageSubject = new BehaviorSubject<Language>(this.DEFAULT_LANGUAGE);
  
  /**
   * Observable for current language changes.
   */
  currentLanguage$: Observable<Language> = this.currentLanguageSubject.asObservable();
  
  /**
   * Get list of available languages.
   */
  readonly availableLanguages = LANGUAGES;

  constructor(private translate: TranslateService) {
    this.initializeLanguage();
  }

  /**
   * Initialize the language service.
   * Sets up available languages and loads the saved or default language.
   */
  private initializeLanguage(): void {
    // Set available languages
    this.translate.addLangs(['en', 'fr']);
    this.translate.setDefaultLang(this.DEFAULT_LANGUAGE);
    
    // Load saved language or use default
    const savedLanguage = this.getSavedLanguage();
    this.setLanguage(savedLanguage);
  }

  /**
   * Get the currently saved language from storage.
   */
  private getSavedLanguage(): Language {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      if (saved && (saved === 'en' || saved === 'fr')) {
        return saved as Language;
      }
    } catch (e) {
      // localStorage not available
    }
    return this.DEFAULT_LANGUAGE;
  }

  /**
   * Get the current language.
   */
  getCurrentLanguage(): Language {
    return this.currentLanguageSubject.value;
  }

  /**
   * Get the language to switch to (the other language).
   */
  getOtherLanguage(): Language {
    return this.currentLanguageSubject.value === 'en' ? 'fr' : 'en';
  }

  /**
   * Get the native name of the other language (for button display).
   */
  getOtherLanguageNativeName(): string {
    const otherLang = this.getOtherLanguage();
    const langConfig = LANGUAGES.find(l => l.code === otherLang);
    return langConfig?.nativeName || otherLang;
  }

  /**
   * Get the language configuration by code.
   */
  getLanguageConfig(code: Language): LanguageConfig | undefined {
    return LANGUAGES.find(l => l.code === code);
  }

  /**
   * Set the application language.
   * @param language The language code to set
   */
  setLanguage(language: Language): void {
    this.translate.use(language);
    this.currentLanguageSubject.next(language);
    this.saveLanguage(language);
  }

  /**
   * Toggle between English and French.
   */
  toggleLanguage(): void {
    const newLanguage = this.getOtherLanguage();
    this.setLanguage(newLanguage);
  }

  /**
   * Save the language preference to storage.
   */
  private saveLanguage(language: Language): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, language);
    } catch (e) {
      // localStorage not available
    }
  }

  /**
   * Get instant translation for a key.
   * @param key Translation key
   * @param params Optional interpolation parameters
   */
  instant(key: string, params?: object): string {
    return this.translate.instant(key, params);
  }

  /**
   * Get translation observable for a key.
   * @param key Translation key
   * @param params Optional interpolation parameters
   */
  get(key: string, params?: object): Observable<string> {
    return this.translate.get(key, params);
  }
}
