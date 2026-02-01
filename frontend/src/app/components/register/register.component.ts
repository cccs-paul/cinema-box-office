/*
 * myRC - Register Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { RegistrationRequest } from '../../models/auth.model';
import { HttpClient } from '@angular/common/http';
import { Subject, debounceTime, takeUntil, distinctUntilChanged } from 'rxjs';

/**
 * Registration component for self-registration of App Account users.
 * Validates username availability, email format, and password requirements.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent implements OnInit, OnDestroy {
  title = 'myRC';
  registerForm!: FormGroup;

  // Loading and error states
  isLoading = false;
  isCheckingUsername = false;
  errorMessage = '';
  successMessage = '';

  // Username availability
  usernameAvailable: boolean | null = null;
  usernameMessage = '';

  // Status indicators
  apiStatus = 'Checking...';
  isApiHealthy = false;
  databaseStatus = 'Checking...';
  isDatabaseHealthy = false;

  // Password visibility toggles
  showPassword = false;
  showConfirmPassword = false;

  // Destroy subject for cleanup
  private destroy$ = new Subject<void>();
  private usernameCheck$ = new Subject<string>();

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
    this.initializeForm();
    this.setupUsernameCheck();
    this.checkApiHealth();
    this.checkDatabaseHealth();

    // Force light theme on registration page
    this.themeService.setTheme('light');

    // If already logged in, redirect to RC selection page
    if (this.authService.isLoggedIn) {
      this.router.navigate(['/rc-selection']);
    }
  }

  /**
   * Cleanup on component destruction.
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize the registration form.
   */
  private initializeForm(): void {
    this.registerForm = this.formBuilder.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z0-9_-]+$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
      ]],
      fullName: ['', [
        Validators.maxLength(100)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(100)
      ]],
      confirmPassword: ['', [
        Validators.required
      ]]
    }, { validators: this.passwordMatchValidator });
  }

  /**
   * Custom validator to check if passwords match.
   */
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    if (confirmPassword && confirmPassword.hasError('passwordMismatch')) {
      delete confirmPassword.errors?.['passwordMismatch'];
      if (Object.keys(confirmPassword.errors || {}).length === 0) {
        confirmPassword.setErrors(null);
      }
    }

    return null;
  }

  /**
   * Setup debounced username availability check.
   */
  private setupUsernameCheck(): void {
    this.usernameCheck$.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe((username) => {
      this.checkUsernameAvailability(username);
    });
  }

  /**
   * Handle username input changes.
   */
  onUsernameChange(): void {
    const username = this.registerForm.get('username')?.value;
    this.usernameAvailable = null;
    this.usernameMessage = '';

    if (username && username.length >= 3 && this.registerForm.get('username')?.valid) {
      this.usernameCheck$.next(username);
    }
  }

  /**
   * Check if username is available.
   */
  private checkUsernameAvailability(username: string): void {
    this.isCheckingUsername = true;
    this.authService.checkUsernameAvailability(username).subscribe({
      next: (response) => {
        this.isCheckingUsername = false;
        this.usernameAvailable = response.available;
        this.usernameMessage = response.message;
      },
      error: () => {
        this.isCheckingUsername = false;
        this.usernameAvailable = null;
        this.usernameMessage = 'Unable to verify username';
      }
    });
  }

  /**
   * Handle form submission.
   */
  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched();
      this.errorMessage = 'Please fix the errors in the form';
      return;
    }

    if (this.usernameAvailable === false) {
      this.errorMessage = 'Username is not available';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: RegistrationRequest = {
      username: this.registerForm.get('username')?.value,
      email: this.registerForm.get('email')?.value,
      password: this.registerForm.get('password')?.value,
      confirmPassword: this.registerForm.get('confirmPassword')?.value,
      fullName: this.registerForm.get('fullName')?.value || undefined
    };

    this.authService.register(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message || 'Registration successful! You can now log in.';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error: Error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Registration failed. Please try again.';
      }
    });
  }

  /**
   * Mark all form controls as touched to show validation errors.
   */
  private markFormGroupTouched(): void {
    Object.keys(this.registerForm.controls).forEach((key) => {
      const control = this.registerForm.get(key);
      control?.markAsTouched();
      control?.updateValueAndValidity();
    });
  }

  /**
   * Toggle password visibility.
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Toggle confirm password visibility.
   */
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  /**
   * Navigate back to login page.
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * Get validation error message for a form field.
   */
  getErrorMessage(fieldName: string): string {
    const control = this.registerForm.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return `${this.getFieldLabel(fieldName)} is required`;
    }
    if (control.errors['minlength']) {
      const minLength = control.errors['minlength'].requiredLength;
      return `${this.getFieldLabel(fieldName)} must be at least ${minLength} characters`;
    }
    if (control.errors['maxlength']) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `${this.getFieldLabel(fieldName)} cannot exceed ${maxLength} characters`;
    }
    if (control.errors['email'] || control.errors['pattern']) {
      if (fieldName === 'email') {
        return 'Please enter a valid email address';
      }
      if (fieldName === 'username') {
        return 'Username can only contain letters, numbers, underscores, and hyphens';
      }
    }
    if (control.errors['passwordMismatch']) {
      return 'Passwords do not match';
    }

    return '';
  }

  /**
   * Get human-readable field label.
   */
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      username: 'Username',
      email: 'Email',
      fullName: 'Full Name',
      password: 'Password',
      confirmPassword: 'Confirm Password'
    };
    return labels[fieldName] || fieldName;
  }

  /**
   * Check if a field has errors and has been touched.
   */
  hasError(fieldName: string): boolean {
    const control = this.registerForm.get(fieldName);
    return !!(control && control.errors && control.touched);
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
