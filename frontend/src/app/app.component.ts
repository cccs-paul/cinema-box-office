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
  isHealthy = false;

  /**
   * Constructor.
   *
   * @param http Angular HTTP client
   */
  constructor(private http: HttpClient) {}

  /**
   * Component initialization.
   * Checks health status of API.
   */
  ngOnInit(): void {
    this.checkApiHealth();
  }

  /**
   * Check API health status.
   */
  private checkApiHealth(): void {
    this.http.get<{ status: string; message: string }>('/api/health').subscribe(
      (response) => {
        this.isHealthy = response.status === 'UP';
        this.apiStatus = response.message;
      },
      (error) => {
        this.isHealthy = false;
        this.apiStatus = 'API is not available';
        console.error('API health check failed:', error);
      }
    );
  }
}
