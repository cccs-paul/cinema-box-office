/*
 * Cinema Box Office - App Routes
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AuthGuardService } from './guards/auth.guard';

/**
 * Application routing configuration.
 * Provides navigation between login and dashboard with auth protection.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuardService],
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: '/login',
  },
];
