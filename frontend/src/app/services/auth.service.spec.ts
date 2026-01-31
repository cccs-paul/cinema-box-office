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
});
