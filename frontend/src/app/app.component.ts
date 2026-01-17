/*
 * Cinema Box Office - App Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

/**
 * Root application component.
 * Provides the main UI and integrates with the REST API.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  title = 'Cinema Box Office';
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;

  /**
   * Constructor.
   *
   * @param http Angular HTTP client
   */
  constructor(private http: HttpClient) {}

  /**
   * Component initialization.
   * Checks health status of API and database.
   */
  ngOnInit(): void {
    this.checkApiHealth();
    this.checkDatabaseHealth();
  }

  /**
   * Check API health status.
   */
  private checkApiHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health').subscribe(
      (response) => {
        this.isApiHealthy = response.status === 'UP';
        this.apiStatus = response.message;
      },
      (error) => {
        this.isApiHealthy = false;
        this.apiStatus = 'API is not available';
        console.error('API health check failed:', error);
      }
    );
  }

  /**
   * Check database health status.
   */
  private checkDatabaseHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health/db').subscribe(
      (response) => {
        this.isDatabaseHealthy = response.status === 'UP';
        this.databaseStatus = response.message;
      },
      (error) => {
        this.isDatabaseHealthy = false;
        this.databaseStatus = 'Database is not available';
        console.error('Database health check failed:', error);
      }
    );
  }
}
