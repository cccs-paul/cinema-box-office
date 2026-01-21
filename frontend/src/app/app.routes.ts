/*
 * Cinema Box Office - App Routes
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { LayoutComponent } from './components/layout/layout.component';
import { RCSelectionComponent } from './components/rc-selection/rc-selection.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DeveloperToolsComponent } from './components/developer-tools/developer-tools.component';
import { AuthGuardService } from './guards/auth.guard';

/**
 * Application routing configuration.
 * Provides navigation between login, RC selection, and dashboard with auth protection.
 * Authenticated routes use LayoutComponent which includes header.
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
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuardService],
    children: [
      {
        path: 'rc-selection',
        component: RCSelectionComponent,
      },
      {
        path: 'dashboard',
        component: DashboardComponent,
      },
      {
        path: 'developer-tools',
        component: DeveloperToolsComponent,
      },
      {
        path: '',
        redirectTo: 'rc-selection',
        pathMatch: 'full',
      },
    ],
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
