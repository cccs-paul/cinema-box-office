/*
 * myRC - App Config
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ApplicationConfig, importProvidersFrom, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withXsrfConfiguration, withInterceptors } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { firstValueFrom } from 'rxjs';
import { authInterceptor } from './interceptors/auth.interceptor';

/**
 * Initialize translations before app starts.
 * Uses firstValueFrom instead of deprecated toPromise().
 */
export function initializeApp(translate: TranslateService): () => Promise<void> {
  return async () => {
    translate.addLangs(['en', 'fr']);
    translate.setDefaultLang('en');
    const savedLang = localStorage.getItem('myrc_language') || 'en';
    await firstValueFrom(translate.use(savedLang));
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
      withInterceptors([authInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      })
    ),
    importProvidersFrom(
      TranslateModule.forRoot({
        fallbackLang: 'en'
      })
    ),
    provideTranslateHttpLoader({
      prefix: './assets/i18n/',
      suffix: '.json'
    }),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [TranslateService],
      multi: true
    }
  ],
};
