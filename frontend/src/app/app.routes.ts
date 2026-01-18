/*
 * Cinema Box Office - App Routes
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RCSelectionComponent } from './components/rc-selection/rc-selection.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DeveloperToolsComponent } from './components/developer-tools/developer-tools.component';
import { AuthGuardService } from './guards/auth.guard';

/**
 * Application routing configuration.
 * Provides navigation between login, RC selection, and dashboard with auth protection.
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
    path: 'rc-selection',
    component: RCSelectionComponent,
    canActivate: [AuthGuardService],
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuardService],
  },
  {
    path: 'developer-tools',
    component: DeveloperToolsComponent,
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
