/*
 * myRC - App Config
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ApplicationConfig, importProvidersFrom, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';
import { TranslateModule, TranslateService, TranslateLoader } from '@ngx-translate/core';
import { provideTranslateHttpLoader, TranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';

/**
 * Initialize translations before app starts.
 */
export function initializeApp(translate: TranslateService): () => Promise<void> {
  return () => {
    translate.addLangs(['en', 'fr']);
    translate.setDefaultLang('en');
    const savedLang = localStorage.getItem('myrc_language') || 'en';
    return translate.use(savedLang).toPromise().then(() => {});
  };
}

/**
 * Application configuration for Bootstrap.
 * Provides router, HTTP client, and other essential services.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-17
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      })
    ),
    provideTranslateHttpLoader({
      prefix: './assets/i18n/',
      suffix: '.json'
    }),
    importProvidersFrom(
      TranslateModule.forRoot({
        defaultLanguage: 'en',
        loader: {
          provide: TranslateLoader,
          useClass: TranslateHttpLoader
        }
      })
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [TranslateService],
      multi: true
    }
  ],
};
