/**
 * Auth Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { LoginMethods, RegistrationRequest, RegistrationResponse, UsernameAvailabilityResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    id: 1,
    username: 'testuser',
    fullName: 'Test User',
    email: 'test@example.com',
    authProvider: 'LOCAL',
    enabled: true,
    accountLocked: false,
    emailVerified: true,
    roles: ['USER'],
    profileDescription: '',
    lastLoginAt: '2026-01-01T00:00:00',
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
  };

  /**
   * Helper to set a test cookie to trigger session check.
   */
  function setSessionCookie(): void {
    document.cookie = 'test-session=active; path=/';
  }

  /**
   * Helper to clear test cookies.
   */
  function clearSessionCookie(): void {
    document.cookie = 'test-session=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  }

  beforeEach(() => {
    // Set a cookie so that session check is triggered
    setSessionCookie();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    clearSessionCookie();
  });

  describe('initialization', () => {
    it('should be created', () => {
      // Handle the session check request
      const req = httpMock.expectOne('/api/users/me');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });

      expect(service).toBeTruthy();
    });

    it('should check session on init', fakeAsync(() => {
      // Handle the session check request
      const req = httpMock.expectOne('/api/users/me');
      req.flush(mockUser);
      tick();

      expect(service.currentUserValue).toEqual(mockUser);
      expect(service.isLoggedIn).toBe(true);
    }));

    it('should handle 401 on session check', fakeAsync(() => {
      // Handle the session check request
      const req = httpMock.expectOne('/api/users/me');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      expect(service.currentUserValue).toBeNull();
      expect(service.isLoggedIn).toBe(false);
    }));
  });

  describe('loginLocal', () => {
    it('should authenticate user with local credentials', fakeAsync(() => {
      // First handle the session check
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.loginLocal('testuser', 'password').subscribe((user) => {
        expect(user).toEqual(mockUser);
        expect(service.isLoggedIn).toBe(true);
      });

      const req = httpMock.expectOne(
        '/api/users/authenticate?username=testuser&password=password'
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockUser);
    }));

    it('should handle login error', fakeAsync(() => {
      // First handle the session check
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.loginLocal('testuser', 'wrongpassword').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/users/authenticate?username=testuser&password=wrongpassword'
      );
      req.flush(
        { message: 'Invalid credentials' },
        { status: 401, statusText: 'Unauthorized' }
      );
    }));
  });

  describe('loginLdap', () => {
    it('should authenticate user with LDAP credentials', fakeAsync(() => {
      // First handle the session check
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.loginLdap('ldapuser', 'ldappassword').subscribe((user) => {
        expect(user).toEqual(mockUser);
      });

      const req = httpMock.expectOne(
        '/api/users/authenticate/ldap?username=ldapuser&password=ldappassword'
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockUser);
    }));

    it('should handle LDAP login error', fakeAsync(() => {
      // First handle the session check
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.loginLdap('ldapuser', 'wrongpassword').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/users/authenticate/ldap?username=ldapuser&password=wrongpassword'
      );
      req.flush(
        { message: 'LDAP authentication failed' },
        { status: 401, statusText: 'Unauthorized' }
      );
    }));
  });

  describe('logout', () => {
    it('should logout user and clear session', fakeAsync(() => {
      // First handle the session check and set user
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(mockUser);
      tick();

      expect(service.isLoggedIn).toBe(true);

      service.logout();
      
      expect(service.currentUserValue).toBeNull();
      expect(service.isLoggedIn).toBe(false);
    }));
  });

  describe('currentUser$', () => {
    it('should emit null when not logged in', fakeAsync(() => {
      // Handle the session check
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.currentUser$.subscribe((user) => {
        expect(user).toBeNull();
      });
    }));

    it('should emit user when logged in', fakeAsync(() => {
      // Handle the session check with user
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(mockUser);
      tick();

      service.currentUser$.subscribe((user) => {
        expect(user).toEqual(mockUser);
      });
    }));
  });

  describe('isLoggedIn', () => {
    it('should return false when no user', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      expect(service.isLoggedIn).toBe(false);
    }));

    it('should return true when user is set', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(mockUser);
      tick();

      expect(service.isLoggedIn).toBe(true);
    }));
  });

  describe('getLoginMethods', () => {
    const mockLoginMethods: LoginMethods = {
      appAccount: { enabled: true, allowRegistration: true },
      ldapEnabled: true,
      oauth2Enabled: false
    };

    it('should return login methods from server', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.getLoginMethods().subscribe((methods) => {
        expect(methods).toEqual(mockLoginMethods);
        expect(methods.appAccount.enabled).toBe(true);
        expect(methods.appAccount.allowRegistration).toBe(true);
        expect(methods.ldapEnabled).toBe(true);
        expect(methods.oauth2Enabled).toBe(false);
      });

      const req = httpMock.expectOne('/api/auth/login-methods');
      expect(req.request.method).toBe('GET');
      req.flush(mockLoginMethods);
    }));

    it('should return default config on error', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.getLoginMethods().subscribe((methods) => {
        expect(methods.appAccount.enabled).toBe(true);
        expect(methods.appAccount.allowRegistration).toBe(false);
        expect(methods.ldapEnabled).toBe(true);
        expect(methods.oauth2Enabled).toBe(true);
      });

      const req = httpMock.expectOne('/api/auth/login-methods');
      req.flush(null, { status: 500, statusText: 'Server Error' });
    }));
  });

  describe('checkUsernameAvailability', () => {
    it('should return available for non-existent username', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      const mockResponse: UsernameAvailabilityResponse = {
        available: true,
        message: 'Username is available'
      };

      service.checkUsernameAvailability('newuser').subscribe((response) => {
        expect(response.available).toBe(true);
      });

      const req = httpMock.expectOne('/api/auth/check-username/newuser');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    }));

    it('should return unavailable for existing username', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      const mockResponse: UsernameAvailabilityResponse = {
        available: false,
        message: 'Username is already taken'
      };

      service.checkUsernameAvailability('existinguser').subscribe((response) => {
        expect(response.available).toBe(false);
      });

      const req = httpMock.expectOne('/api/auth/check-username/existinguser');
      req.flush(mockResponse);
    }));

    it('should handle error gracefully', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.checkUsernameAvailability('testuser').subscribe((response) => {
        expect(response.available).toBe(false);
        expect(response.message).toContain('Unable to verify');
      });

      const req = httpMock.expectOne('/api/auth/check-username/testuser');
      req.flush(null, { status: 500, statusText: 'Server Error' });
    }));
  });

  describe('register', () => {
    const mockRequest: RegistrationRequest = {
      username: 'newuser',
      email: 'newuser@example.com',
      password: 'Password123',
      confirmPassword: 'Password123',
      fullName: 'New User'
    };

    const mockResponse: RegistrationResponse = {
      success: true,
      message: 'Registration successful',
      userId: 1
    };

    it('should register a new user successfully', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.register(mockRequest).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.message).toContain('successful');
      });

      const req = httpMock.expectOne('/api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      req.flush(mockResponse);
    }));

    it('should handle 409 conflict error (username exists)', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.register(mockRequest).subscribe({
        error: (error) => {
          expect(error.message).toContain('already exists');
        }
      });

      const req = httpMock.expectOne('/api/auth/register');
      req.flush(
        { message: 'Username already exists' },
        { status: 409, statusText: 'Conflict' }
      );
    }));

    it('should handle 400 bad request error', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.register(mockRequest).subscribe({
        error: (error) => {
          expect(error.message).toContain('Invalid');
        }
      });

      const req = httpMock.expectOne('/api/auth/register');
      req.flush(
        { message: 'Invalid registration data' },
        { status: 400, statusText: 'Bad Request' }
      );
    }));

    it('should handle server error message', fakeAsync(() => {
      const sessionReq = httpMock.expectOne('/api/users/me');
      sessionReq.flush(null, { status: 401, statusText: 'Unauthorized' });
      tick();

      service.register(mockRequest).subscribe({
        error: (error) => {
          expect(error.message).toBe('Custom error message');
        }
      });

      const req = httpMock.expectOne('/api/auth/register');
      req.flush(
        { message: 'Custom error message' },
        { status: 500, statusText: 'Server Error' }
      );
    }));
  });
});
