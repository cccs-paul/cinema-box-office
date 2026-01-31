/*
 * myRC - Login Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { User } from '../../models/user.model';
import { HttpClient } from '@angular/common/http';

/**
 * Login component supporting LOCAL, LDAP, and OAUTH2 authentication.
 * Displays API and database status.
 * Provides tab-based interface for switching between auth methods.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  title = 'myRC';
  activeTab: 'local' | 'ldap' | 'oauth2' = 'local';
  
  // Form groups
  localForm!: FormGroup;
  ldapForm!: FormGroup;
  
  // Loading and error states
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  // Status indicators
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;

  /**
   * Constructor.
   *
   * @param authService Authentication service
   * @param router Angular router
   * @param formBuilder Form builder service
   * @param http HTTP client
   * @param themeService Theme service
   */
  constructor(
    private authService: AuthService,
    private router: Router,
    private formBuilder: FormBuilder,
    private http: HttpClient,
    private themeService: ThemeService
  ) {}

  /**
   * Component initialization.
   */
  ngOnInit(): void {
    this.initializeForms();
    this.checkApiHealth();
    this.checkDatabaseHealth();

    // Force light theme on login page - dark theme is only available after RC/FY selection
    this.themeService.setTheme('light');

    // If already logged in, redirect to RC selection page
    if (this.authService.isLoggedIn) {
      this.router.navigate(['/rc-selection']);
    }
  }

  /**
   * Initialize form groups.
   */
  private initializeForms(): void {
    this.localForm = this.formBuilder.group({
      username: ['admin', [Validators.required, Validators.minLength(3)]],
      password: ['Admin@123', Validators.required],
      rememberMe: [false],
    });

    this.ldapForm = this.formBuilder.group({
      ldapUsername: ['', [Validators.required, Validators.minLength(3)]],
      ldapPassword: ['', Validators.required],
    });
  }

  /**
   * Disable form controls during loading.
   */
  private disableFormControls(): void {
    Object.keys(this.localForm.controls).forEach((key) => {
      this.localForm.get(key)?.disable();
    });
    Object.keys(this.ldapForm.controls).forEach((key) => {
      this.ldapForm.get(key)?.disable();
    });
  }

  /**
   * Enable form controls after loading.
   */
  private enableFormControls(): void {
    Object.keys(this.localForm.controls).forEach((key) => {
      this.localForm.get(key)?.enable();
    });
    Object.keys(this.ldapForm.controls).forEach((key) => {
      this.ldapForm.get(key)?.enable();
    });
  }

  /**
   * Handle LOCAL authentication.
   */
  loginLocal(): void {
    if (this.localForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.disableFormControls();

    const { username, password } = this.localForm.value;

    this.authService.loginLocal(username, password).subscribe({
      next: (user: User) => {
        this.isLoading = false;
        this.successMessage = `Welcome, ${user.fullName || user.username}!`;
        setTimeout(() => {
          this.router.navigate(['/rc-selection']);
        }, 500);
      },
      error: (error: Error) => {
        this.isLoading = false;
        this.enableFormControls();
        this.errorMessage = error.message || 'Login failed. Please check your credentials.';
      },
    });
  }

  /**
   * Handle LDAP authentication.
   */
  loginLdap(): void {
    if (this.ldapForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.disableFormControls();

    const username = this.ldapForm.value.ldapUsername;
    const password = this.ldapForm.value.ldapPassword;

    this.authService.loginLdap(username, password).subscribe({
      next: (user: User) => {
        this.isLoading = false;
        this.successMessage = `Welcome, ${user.fullName || user.username}!`;
        setTimeout(() => {
          this.router.navigate(['/rc-selection']);
        }, 500);
      },
      error: (error: Error) => {
        this.isLoading = false;
        this.enableFormControls();
        this.errorMessage = error.message || 'LDAP authentication failed. Please check your credentials.';
      },
    });
  }

  /**
   * Handle OAuth2 authentication.
   *
   * @param provider OAuth provider name (google, github, etc.)
   */
  loginOAuth2(provider: string): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    try {
      this.authService.initiateOAuth2(provider);
    } catch (error: any) {
      this.isLoading = false;
      this.errorMessage = error.message || `${provider} authentication failed`;
    }
  }

  /**
   * Switch active tab.
   *
   * @param tab Tab name
   */
  switchTab(tab: 'local' | 'ldap' | 'oauth2'): void {
    this.activeTab = tab;
    this.errorMessage = '';
    this.successMessage = '';
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
