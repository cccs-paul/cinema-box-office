/*
 * myRC - Developer Tools Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { DeveloperToolsComponent } from './developer-tools.component';

/**
 * Test suite for DeveloperToolsComponent.
 * Tests health checks and navigation.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
describe('DeveloperToolsComponent', () => {
  let component: DeveloperToolsComponent;
  let fixture: ComponentFixture<DeveloperToolsComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DeveloperToolsComponent,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'dashboard', component: DeveloperToolsComponent },
        ]),
        TranslateModule.forRoot(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeveloperToolsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    
    spyOn(router, 'navigate');
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Helper to flush health check requests.
   */
  function flushHealthChecks(): void {
    const apiReq = httpMock.expectOne('/api/health');
    apiReq.flush({ status: 'UP', message: 'API is healthy' });
    
    const dbReq = httpMock.expectOne('/api/health/db');
    dbReq.flush({ status: 'UP', message: 'Database is healthy' });
  }

  describe('Component Initialization', () => {
    it('should create the component', () => {
      fixture.detectChanges();
      flushHealthChecks();
      expect(component).toBeTruthy();
    });

    it('should have correct title', () => {
      fixture.detectChanges();
      flushHealthChecks();
      expect(component.title).toBe('myRC - Developer Tools');
    });

    it('should initialize with checking status', () => {
      expect(component.apiStatus).toBe('Checking...');
      expect(component.databaseStatus).toBe('Checking...');
    });

    it('should check API health on init', () => {
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      expect(apiReq.request.method).toBe('GET');
      apiReq.flush({ status: 'UP', message: 'API is healthy' });
      
      const dbReq = httpMock.expectOne('/api/health/db');
      dbReq.flush({ status: 'UP', message: 'Database is healthy' });
      
      expect(component.isApiHealthy).toBeTrue();
      expect(component.apiStatus).toBe('API is healthy');
    });

    it('should check database health on init', () => {
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      apiReq.flush({ status: 'UP', message: 'API is healthy' });
      
      const dbReq = httpMock.expectOne('/api/health/db');
      expect(dbReq.request.method).toBe('GET');
      dbReq.flush({ status: 'UP', message: 'Database is healthy' });
      
      expect(component.isDatabaseHealthy).toBeTrue();
      expect(component.databaseStatus).toBe('Database is healthy');
    });
  });

  describe('Health Check Status', () => {
    it('should handle API health check failure', () => {
      spyOn(console, 'error');
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      apiReq.error(new ErrorEvent('Network error'));
      
      const dbReq = httpMock.expectOne('/api/health/db');
      dbReq.flush({ status: 'UP', message: 'Database is healthy' });
      
      expect(component.isApiHealthy).toBeFalse();
      expect(component.apiStatus).toBe('API is not available');
      expect(console.error).toHaveBeenCalled();
    });

    it('should handle database health check failure', () => {
      spyOn(console, 'error');
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      apiReq.flush({ status: 'UP', message: 'API is healthy' });
      
      const dbReq = httpMock.expectOne('/api/health/db');
      dbReq.error(new ErrorEvent('Network error'));
      
      expect(component.isDatabaseHealthy).toBeFalse();
      expect(component.databaseStatus).toBe('Database is not available');
      expect(console.error).toHaveBeenCalled();
    });

    it('should display DOWN status for API', () => {
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      apiReq.flush({ status: 'DOWN', message: 'API is down' });
      
      const dbReq = httpMock.expectOne('/api/health/db');
      dbReq.flush({ status: 'UP', message: 'Database is healthy' });
      
      expect(component.isApiHealthy).toBeFalse();
      expect(component.apiStatus).toBe('API is down');
    });

    it('should display DOWN status for database', () => {
      fixture.detectChanges();
      
      const apiReq = httpMock.expectOne('/api/health');
      apiReq.flush({ status: 'UP', message: 'API is healthy' });
      
      const dbReq = httpMock.expectOne('/api/health/db');
      dbReq.flush({ status: 'DOWN', message: 'Database connection failed' });
      
      expect(component.isDatabaseHealthy).toBeFalse();
      expect(component.databaseStatus).toBe('Database connection failed');
    });
  });

  describe('Navigation', () => {
    it('should navigate back to dashboard', () => {
      fixture.detectChanges();
      flushHealthChecks();
      
      component.goBack();
      
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    });
  });
});
