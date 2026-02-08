/*
 * myRC - App Routes
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LayoutComponent } from './components/layout/layout.component';
import { LayoutNoSidebarComponent } from './components/layout-no-sidebar/layout-no-sidebar.component';
import { RCSelectionComponent } from './components/rc-selection/rc-selection.component';
import { RCPermissionsComponent } from './components/rc-permissions/rc-permissions.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SpendingComponent } from './components/spending/spending.component';
import { ProcurementComponent } from './components/procurement/procurement.component';
import { InsightsComponent } from './components/insights/insights.component';
import { SummaryComponent } from './components/summary/summary.component';
import { DeveloperToolsComponent } from './components/developer-tools/developer-tools.component';
import { ConfigurationComponent } from './components/configuration/configuration.component';
import { AccessibilityComponent } from './components/accessibility/accessibility.component';
import { PreferencesComponent } from './components/preferences/preferences.component';
import { AuthGuardService } from './guards/auth.guard';

/**
 * Application routing configuration.
 * Provides navigation between login, registration, RC selection, and dashboard with auth protection.
 * RC selection uses layout without sidebar.
 * Dashboard and other main pages use layout with sidebar.
 *
 * @author myRC Team
 * @version 1.2.0
 * @since 2026-01-17
 */
export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegisterComponent,
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
        path: 'rc-permissions/:rcId',
        component: RCPermissionsComponent,
      },
      {
        path: 'preferences',
        component: PreferencesComponent,
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
        path: 'spending',
        component: SpendingComponent,
      },
      {
        path: 'procurement',
        component: ProcurementComponent,
      },
      {
        path: 'insights',
        component: InsightsComponent,
      },
      {
        path: 'summary',
        component: SummaryComponent,
      },
      {
        path: 'configuration',
        component: ConfigurationComponent,
      },
      {
        path: 'developer-tools',
        component: DeveloperToolsComponent,
      },
      {
        path: 'accessibility',
        redirectTo: '/app/preferences',
        pathMatch: 'full',
      },
      {
        path: 'preferences',
        component: PreferencesComponent,
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
