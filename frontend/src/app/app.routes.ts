/*
 * myRC - App Routes
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { LayoutComponent } from './components/layout/layout.component';
import { LayoutNoSidebarComponent } from './components/layout-no-sidebar/layout-no-sidebar.component';
import { RCSelectionComponent } from './components/rc-selection/rc-selection.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DeveloperToolsComponent } from './components/developer-tools/developer-tools.component';
import { AuthGuardService } from './guards/auth.guard';

/**
 * Application routing configuration.
 * Provides navigation between login, RC selection, and dashboard with auth protection.
 * RC selection uses layout without sidebar.
 * Dashboard and other main pages use layout with sidebar.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-17
 */
export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: '',
    component: LayoutNoSidebarComponent,
    canActivate: [AuthGuardService],
    children: [
      {
        path: 'rc-selection',
        component: RCSelectionComponent,
      },
      {
        path: '',
        redirectTo: 'rc-selection',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: 'app',
    component: LayoutComponent,
    canActivate: [AuthGuardService],
    children: [
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
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '/login',
  },
];
