/*
 * Cinema Box Office - Developer Tools Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

/**
 * Developer tools page providing links to API documentation, health checks, and database management.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-developer-tools',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './developer-tools.component.html',
  styleUrls: ['./developer-tools.component.scss'],
})
export class DeveloperToolsComponent implements OnInit {
  title = 'Cinema Box Office - Developer Tools';
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;

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
}
