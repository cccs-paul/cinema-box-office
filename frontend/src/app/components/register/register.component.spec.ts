/**
 * Register Component Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 * @license MIT
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { of, throwError, BehaviorSubject, Subject } from 'rxjs';
import { Directive, Input } from '@angular/core';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { User } from '../../models/user.model';
import { RegistrationResponse, UsernameAvailabilityResponse } from '../../models/auth.model';

// Stub directive to replace routerLink
@Directive({
  standalone: true,
  selector: '[routerLink]'
})
class RouterLinkStubDirective {
  @Input() routerLink: string | any[] = '';
}

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let themeService: jasmine.SpyObj<ThemeService>;
  let router: jasmine.SpyObj<Router>;
  let httpMock: HttpTestingController;

  const currentUser$ = new BehaviorSubject<User | null>(null);

  const mockRegistrationResponse: RegistrationResponse = {
    success: true,
    message: 'Registration successful! You can now log in.',
    userId: 1
  };

  const mockUsernameAvailable: UsernameAvailabilityResponse = {
    available: true,
    message: 'Username is available'
  };

  const mockUsernameUnavailable: UsernameAvailabilityResponse = {
    available: false,
    message: 'Username is already taken'
  };

  const mockActivatedRoute = {
    snapshot: { params: {}, queryParams: {}, data: {} },
    params: of({}),
    queryParams: of({}),
    data: of({}),
    url: of([]),
    fragment: of(''),
    outlet: 'primary',
    component: null
  };

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', 
      ['register', 'checkUsernameAvailability'], 
      { currentUser$: currentUser$.asObservable(), isLoggedIn: false }
    );
    authSpy.checkUsernameAvailability.and.returnValue(of(mockUsernameAvailable));
    authSpy.register.and.returnValue(of(mockRegistrationResponse));
    
    const themeSpy = jasmine.createSpyObj('ThemeService', ['setTheme']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate', 'navigateByUrl'], {
      events: new Subject(),
      routerState: { root: {} }
    });

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: ThemeService, useValue: themeSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    })
    .overrideComponent(RegisterComponent, {
      remove: { imports: [RouterModule] },
      add: { imports: [RouterLinkStubDirective] }
    })
    .compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    themeService = TestBed.inject(ThemeService) as jasmine.SpyObj<ThemeService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    httpMock = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
    currentUser$.next(null);
  });

  it('should create', () => {
    fixture.detectChanges();
    // Flush health check requests
    httpMock.expectOne('/api/health').flush({ status: 'UP' });
    httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should set light theme on init', () => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
      
      expect(themeService.setTheme).toHaveBeenCalledWith('light');
    });

    it('should initialize form with empty values', () => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
      
      expect(component.registerForm.get('username')?.value).toBe('');
      expect(component.registerForm.get('email')?.value).toBe('');
      expect(component.registerForm.get('password')?.value).toBe('');
      expect(component.registerForm.get('confirmPassword')?.value).toBe('');
      expect(component.registerForm.get('fullName')?.value).toBe('');
    });
  });

  describe('Health Checks', () => {
    it('should set API status to healthy when health check succeeds', fakeAsync(() => {
      fixture.detectChanges();
      
      httpMock.expectOne('/api/health').flush({ status: 'UP', message: 'API is healthy' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP', message: 'Database is healthy' });
      
      tick();
      
      expect(component.isApiHealthy).toBeTrue();
    }));

    it('should set database status to healthy when health check succeeds', fakeAsync(() => {
      fixture.detectChanges();
      
      httpMock.expectOne('/api/health').flush({ status: 'UP', message: 'API is healthy' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP', message: 'Database is healthy' });
      
      tick();
      
      expect(component.isDatabaseHealthy).toBeTrue();
    }));
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should mark form as invalid when username is empty', () => {
      component.registerForm.patchValue({
        username: '',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as invalid when username is too short', () => {
      component.registerForm.patchValue({
        username: 'ab',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as invalid when username has invalid characters', () => {
      component.registerForm.patchValue({
        username: 'user@name!',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should accept username with underscores and hyphens', () => {
      component.registerForm.patchValue({
        username: 'user_name-123',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.get('username')?.valid).toBeTrue();
    });

    it('should mark form as invalid when email is empty', () => {
      component.registerForm.patchValue({
        username: 'testuser',
        email: '',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as invalid when email format is invalid', () => {
      component.registerForm.patchValue({
        username: 'testuser',
        email: 'not-an-email',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as invalid when password is too short', () => {
      component.registerForm.patchValue({
        username: 'testuser',
        email: 'test@example.com',
        password: 'short',
        confirmPassword: 'short'
      });
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as invalid when passwords do not match', () => {
      component.registerForm.patchValue({
        username: 'testuser',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'DifferentPassword'
      });
      
      // Trigger validation
      component.registerForm.updateValueAndValidity();
      
      expect(component.registerForm.valid).toBeFalse();
    });

    it('should mark form as valid with correct data', () => {
      component.registerForm.patchValue({
        username: 'testuser',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      
      expect(component.registerForm.valid).toBeTrue();
    });
  });

  describe('Username Availability', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should check username availability on change', fakeAsync(() => {
      component.registerForm.patchValue({ username: 'newuser' });
      component.onUsernameChange();
      tick(600); // Wait for debounce
      
      expect(authService.checkUsernameAvailability).toHaveBeenCalledWith('newuser');
    }));

    it('should not check username if too short', fakeAsync(() => {
      component.registerForm.patchValue({ username: 'ab' });
      component.onUsernameChange();
      tick(600);
      
      expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
    }));

    it('should show available status when username is available', fakeAsync(() => {
      authService.checkUsernameAvailability.and.returnValue(of(mockUsernameAvailable));
      
      component.registerForm.patchValue({ username: 'newuser' });
      component.onUsernameChange();
      tick(600);
      
      expect(component.usernameAvailable).toBeTrue();
    }));

    it('should show unavailable status when username is taken', fakeAsync(() => {
      authService.checkUsernameAvailability.and.returnValue(of(mockUsernameUnavailable));
      
      component.registerForm.patchValue({ username: 'existinguser' });
      component.onUsernameChange();
      tick(600);
      
      expect(component.usernameAvailable).toBeFalse();
    }));
  });

  describe('Registration', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should call authService.register with correct data', fakeAsync(() => {
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Password123',
        confirmPassword: 'Password123',
        fullName: 'New User'
      });
      component.usernameAvailable = true;
      
      component.onSubmit();
      tick();
      
      expect(authService.register).toHaveBeenCalled();
    }));

    it('should show success message on successful registration', fakeAsync(() => {
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      component.usernameAvailable = true;
      
      component.onSubmit();
      tick();
      
      expect(component.successMessage).toContain('successful');
    }));

    it('should navigate to login on successful registration', fakeAsync(() => {
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      component.usernameAvailable = true;
      
      component.onSubmit();
      tick(2500); // Wait for setTimeout
      
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    }));

    it('should show error message on failed registration', fakeAsync(() => {
      authService.register.and.returnValue(throwError(() => new Error('Registration failed')));
      
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      component.usernameAvailable = true;
      
      component.onSubmit();
      tick();
      
      expect(component.errorMessage).toBe('Registration failed');
    }));

    it('should not submit if form is invalid', () => {
      component.registerForm.patchValue({
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      });
      
      component.onSubmit();
      
      expect(authService.register).not.toHaveBeenCalled();
      expect(component.errorMessage).toContain('fix the errors');
    });

    it('should not submit if username is unavailable', () => {
      component.registerForm.patchValue({
        username: 'existinguser',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      component.usernameAvailable = false;
      
      component.onSubmit();
      
      expect(authService.register).not.toHaveBeenCalled();
      expect(component.errorMessage).toContain('not available');
    });

    it('should set loading state during registration', fakeAsync(() => {
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      component.usernameAvailable = true;
      
      expect(component.isLoading).toBeFalse();
      
      component.onSubmit();
      tick(2500);
      
      expect(component.isLoading).toBeFalse();
    }));
  });

  describe('Password Visibility', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should toggle password visibility', () => {
      expect(component.showPassword).toBeFalse();
      
      component.togglePasswordVisibility();
      expect(component.showPassword).toBeTrue();
      
      component.togglePasswordVisibility();
      expect(component.showPassword).toBeFalse();
    });

    it('should toggle confirm password visibility', () => {
      expect(component.showConfirmPassword).toBeFalse();
      
      component.toggleConfirmPasswordVisibility();
      expect(component.showConfirmPassword).toBeTrue();
      
      component.toggleConfirmPasswordVisibility();
      expect(component.showConfirmPassword).toBeFalse();
    });
  });

  describe('Navigation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should navigate to login page', () => {
      component.goToLogin();
      
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('Error Messages', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should return empty string for valid field', () => {
      component.registerForm.patchValue({ username: 'validuser' });
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.getErrorMessage('username')).toBe('');
    });

    it('should return required error message', () => {
      component.registerForm.get('username')?.setValue('');
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.getErrorMessage('username')).toContain('required');
    });

    it('should return minlength error message', () => {
      component.registerForm.get('username')?.setValue('ab');
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.getErrorMessage('username')).toContain('at least');
    });

    it('should return pattern error message for username', () => {
      component.registerForm.get('username')?.setValue('user@name!');
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.getErrorMessage('username')).toContain('only contain');
    });

    it('should return email error message', () => {
      component.registerForm.get('email')?.setValue('invalid');
      component.registerForm.get('email')?.markAsTouched();
      
      expect(component.getErrorMessage('email')).toContain('valid email');
    });
  });

  describe('Has Error', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne('/api/health').flush({ status: 'UP' });
      httpMock.expectOne('/api/health/db').flush({ status: 'UP' });
    });

    it('should return false for untouched field', () => {
      component.registerForm.get('username')?.setValue('');
      
      expect(component.hasError('username')).toBeFalse();
    });

    it('should return true for touched invalid field', () => {
      component.registerForm.get('username')?.setValue('');
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.hasError('username')).toBeTrue();
    });

    it('should return false for touched valid field', () => {
      component.registerForm.get('username')?.setValue('validuser');
      component.registerForm.get('username')?.markAsTouched();
      
      expect(component.hasError('username')).toBeFalse();
    });
  });
});
