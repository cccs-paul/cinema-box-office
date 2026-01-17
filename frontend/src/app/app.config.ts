/*
 * Cinema Box Office - App Config
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';
import { routes } from './app.routes';

/**
 * Application configuration for Bootstrap.
 * Provides router, HTTP client, and other essential services.
 *
 * @author Box Office Team
 * @version 1.0.0
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
  ],
};
