/*
 * myRC - Login Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { User } from '../../models/user.model';
import { LoginMethods } from '../../models/auth.model';
import { HttpClient } from '@angular/common/http';

/**
 * Login component supporting LOCAL, LDAP, and OAUTH2 authentication.
 * Displays API and database status.
 * Provides tab-based interface for switching between auth methods.
 * Conditionally shows authentication methods based on server configuration.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
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
  isLoadingLoginMethods = true;
  errorMessage = '';
  successMessage = '';
  
  // Login methods configuration
  loginMethods: LoginMethods = {
    appAccount: { enabled: true, allowRegistration: false },
    ldapEnabled: true,
    oauth2Enabled: true
  };
  
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
    this.loadLoginMethods();
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
   * Load available login methods from the server.
   */
  private loadLoginMethods(): void {
    this.isLoadingLoginMethods = true;
    this.authService.getLoginMethods().subscribe({
      next: (methods) => {
        this.loginMethods = methods;
        this.isLoadingLoginMethods = false;
        // Set default active tab to first enabled method
        this.setDefaultActiveTab();
      },
      error: () => {
        // On error, show all methods (default behavior)
        this.isLoadingLoginMethods = false;
      }
    });
  }

  /**
   * Set the default active tab to the first enabled authentication method.
   */
  private setDefaultActiveTab(): void {
    if (this.loginMethods.appAccount.enabled) {
      this.activeTab = 'local';
    } else if (this.loginMethods.ldapEnabled) {
      this.activeTab = 'ldap';
    } else if (this.loginMethods.oauth2Enabled) {
      this.activeTab = 'oauth2';
    }
  }

  /**
   * Check if any login method is enabled.
   */
  get hasAnyLoginMethod(): boolean {
    return this.loginMethods.appAccount.enabled || 
           this.loginMethods.ldapEnabled || 
           this.loginMethods.oauth2Enabled;
  }

  /**
   * Check if registration is allowed.
   */
  get allowRegistration(): boolean {
    return this.loginMethods.appAccount.enabled && 
           this.loginMethods.appAccount.allowRegistration;
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
