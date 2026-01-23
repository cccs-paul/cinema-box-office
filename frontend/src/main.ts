/*
 * myRC - Main
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig).catch((err) =>
  console.error(err)
);
