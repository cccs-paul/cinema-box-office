/*
 * Cinema Box Office - App Config
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

/**
 * Application configuration for Bootstrap.
 * Provides router, HTTP client, and other essential services.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
  ],
};
