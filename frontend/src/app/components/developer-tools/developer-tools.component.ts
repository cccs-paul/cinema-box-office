/*
 * myRC - Developer Tools Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

/**
 * Developer tools page providing links to API documentation, health checks, and database management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-developer-tools',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './developer-tools.component.html',
  styleUrls: ['./developer-tools.component.scss'],
})
export class DeveloperToolsComponent implements OnInit {
  title = 'myRC - Developer Tools';
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;
  authMethods = 'Loading...';

  /**
   * Constructor.
   *
   * @param router Angular router
   * @param http HTTP client
   */
  constructor(private router: Router, private http: HttpClient) {}

  /**
   * Component initialization.
   */
  ngOnInit(): void {
    this.checkApiHealth();
    this.checkDatabaseHealth();
    this.loadAuthMethods();
  }

  /**
   * Navigate back to dashboard.
   */
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  /**
   * Check API health status.
   */
  private checkApiHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health').subscribe({
      next: (response) => {
        this.isApiHealthy = response.status === 'UP';
        this.apiStatus = response.message;
      },
      error: (error) => {
        this.isApiHealthy = false;
        this.apiStatus = 'API is not available';
        console.error('API health check failed:', error);
      },
    });
  }

  /**
   * Check database health status.
   */
  private checkDatabaseHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health/db').subscribe({
      next: (response) => {
        this.isDatabaseHealthy = response.status === 'UP';
        this.databaseStatus = response.message;
      },
      error: (error) => {
        this.isDatabaseHealthy = false;
        this.databaseStatus = 'Database is not available';
        console.error('Database health check failed:', error);
      },
    });
  }

  /**
   * Load enabled authentication methods from the API.
   */
  private loadAuthMethods(): void {
    this.http.get<{
      appAccount: { enabled: boolean; allowRegistration: boolean };
      ldapEnabled: boolean;
      oauth2Enabled: boolean;
    }>('/api/auth/login-methods').subscribe({
      next: (response) => {
        const methods: string[] = [];
        if (response.appAccount?.enabled) {
          methods.push('LOCAL (App-managed)');
        }
        if (response.ldapEnabled) {
          methods.push('LDAP');
        }
        if (response.oauth2Enabled) {
          methods.push('OAuth2');
        }
        this.authMethods = methods.length > 0 ? methods.join(', ') : 'None configured';
      },
      error: (error) => {
        this.authMethods = 'Unable to determine';
        console.error('Failed to load auth methods:', error);
      },
    });
  }
}
