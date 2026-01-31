/**
 * Theme Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ThemeService, Theme } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ThemeService],
    });

    service = TestBed.inject(ThemeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should default to light theme', () => {
      expect(service.currentTheme).toBe('light');
    });

    it('should restore theme from localStorage', () => {
      localStorage.setItem('appTheme', 'dark');

      // Recreate service to pick up localStorage value
      const newService = new ThemeService(TestBed.inject(HttpTestingController) as any);
      expect(newService.currentTheme).toBe('dark');
    });
  });

  describe('toggleTheme', () => {
    it('should toggle from light to dark', () => {
      expect(service.currentTheme).toBe('light');

      service.toggleTheme();

      expect(service.currentTheme).toBe('dark');
      expect(localStorage.getItem('appTheme')).toBe('dark');
    });

    it('should toggle from dark to light', () => {
      service.setTheme('dark');
      expect(service.currentTheme).toBe('dark');

      service.toggleTheme();

      expect(service.currentTheme).toBe('light');
      expect(localStorage.getItem('appTheme')).toBe('light');
    });
  });

  describe('setTheme', () => {
    it('should set light theme', () => {
      service.setTheme('light');

      expect(service.currentTheme).toBe('light');
      expect(localStorage.getItem('appTheme')).toBe('light');
    });

    it('should set dark theme', () => {
      service.setTheme('dark');

      expect(service.currentTheme).toBe('dark');
      expect(localStorage.getItem('appTheme')).toBe('dark');
    });

    it('should emit new theme value', () => {
      let emittedTheme: Theme | null;

      service.currentTheme$.subscribe((theme) => {
        emittedTheme = theme;
      });

      service.setTheme('dark');

      expect(emittedTheme!).toBe('dark');
    });

    it('should persist to localStorage', () => {
      service.setTheme('dark');

      expect(localStorage.getItem('appTheme')).toBe('dark');
    });
  });

  describe('currentTheme$', () => {
    it('should emit initial theme', () => {
      let emittedTheme: Theme | null;

      service.currentTheme$.subscribe((theme) => {
        emittedTheme = theme;
      });

      expect(emittedTheme!).toBe('light');
    });

    it('should emit when theme changes', () => {
      const emittedThemes: Theme[] = [];

      service.currentTheme$.subscribe((theme) => {
        emittedThemes.push(theme);
      });

      service.setTheme('dark');
      service.setTheme('light');

      expect(emittedThemes).toContain('light');
      expect(emittedThemes).toContain('dark');
    });
  });

  describe('updateUserTheme', () => {
    it('should update theme on server', () => {
      service.updateUserTheme('testuser', 'dark').subscribe((response) => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/users/testuser/theme?theme=dark');
      expect(req.request.method).toBe('PUT');
      req.flush({ success: true });
    });

    it('should encode username in URL', () => {
      service.updateUserTheme('test user', 'dark').subscribe();

      const req = httpMock.expectOne('/api/users/test%20user/theme?theme=dark');
      expect(req.request.method).toBe('PUT');
      req.flush({ success: true });
    });

    it('should handle server error', () => {
      service.updateUserTheme('testuser', 'dark').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/users/testuser/theme?theme=dark');
      req.flush(
        { message: 'Server error' },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });
  });

  describe('getUserTheme', () => {
    it('should get user theme from server', () => {
      service.getUserTheme('testuser').subscribe((response) => {
        expect(response.theme).toBe('dark');
      });

      const req = httpMock.expectOne('/api/users/testuser/theme');
      expect(req.request.method).toBe('GET');
      req.flush({ theme: 'dark' });
    });

    it('should handle user not found', () => {
      service.getUserTheme('nonexistent').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/users/nonexistent/theme');
      req.flush(
        { message: 'User not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });
});
