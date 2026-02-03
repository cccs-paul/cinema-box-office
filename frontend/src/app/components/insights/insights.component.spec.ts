/**
 * myRC - Insights Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { InsightsComponent } from './insights.component';
import { AuthService } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { SpendingItemService } from '../../services/spending-item.service';
import { ProcurementService } from '../../services/procurement.service';
import { User } from '../../models/user.model';

describe('InsightsComponent', () => {
  let component: InsightsComponent;
  let fixture: ComponentFixture<InsightsComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let fundingItemService: jasmine.SpyObj<FundingItemService>;
  let spendingItemService: jasmine.SpyObj<SpendingItemService>;
  let procurementService: jasmine.SpyObj<ProcurementService>;
  let languageService: jasmine.SpyObj<LanguageService>;

  let currentUserSubject: BehaviorSubject<User | null>;
  let currentLanguageSubject: BehaviorSubject<string>;

  const mockUser: User = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    fullName: 'Test User',
    authProvider: 'LOCAL',
    enabled: true,
    accountLocked: false,
    emailVerified: true,
    roles: ['USER'],
    profileDescription: '',
    lastLoginAt: '2026-01-01T00:00:00Z',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  };

  const mockRC = {
    id: 1,
    name: 'Test RC',
    description: 'Test Description',
    ownerUsername: 'admin',
    accessLevel: 'OWNER' as const,
    isOwner: true
  };

  const mockFY = {
    id: 1,
    name: 'FY 2025-2026',
    description: 'Fiscal Year 2025-2026',
    active: true,
    responsibilityCentreId: 1,
    showSearchBox: true,
    showCategoryFilter: true,
    groupByCategory: false,
    onTargetMin: -10,
    onTargetMax: 10
  };

  const mockFundingItems = [
    { id: 1, name: 'Funding 1', totalCap: 10000, totalOm: 5000, category: { name: 'Software' } }
  ];

  const mockSpendingItems = [
    { id: 1, name: 'Spending 1', totalCap: 3000, totalOm: 1000, category: { name: 'Software' } }
  ];

  const mockProcurementItems = [
    { id: 1, prNumber: 'PR-001', status: 'COMPLETED', procurementCompleted: true }
  ];

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<User | null>(mockUser);
    currentLanguageSubject = new BehaviorSubject<string>('en');

    authService = jasmine.createSpyObj('AuthService', [], {
      currentUser$: currentUserSubject.asObservable()
    });

    router = jasmine.createSpyObj('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));

    rcService = jasmine.createSpyObj('ResponsibilityCentreService', 
      ['getSelectedRC', 'getSelectedFY', 'getResponsibilityCentre']);
    rcService.getSelectedRC.and.returnValue(1);
    rcService.getSelectedFY.and.returnValue(1);
    rcService.getResponsibilityCentre.and.returnValue(of(mockRC as any));

    fyService = jasmine.createSpyObj('FiscalYearService', ['getFiscalYear']);
    fyService.getFiscalYear.and.returnValue(of(mockFY as any));

    fundingItemService = jasmine.createSpyObj('FundingItemService', ['getFundingItemsByFY']);
    fundingItemService.getFundingItemsByFY.and.returnValue(of(mockFundingItems as any));

    spendingItemService = jasmine.createSpyObj('SpendingItemService', ['getSpendingItemsByFY']);
    spendingItemService.getSpendingItemsByFY.and.returnValue(of(mockSpendingItems as any));

    procurementService = jasmine.createSpyObj('ProcurementService', ['getProcurementItems']);
    procurementService.getProcurementItems.and.returnValue(of(mockProcurementItems as any));

    languageService = jasmine.createSpyObj('LanguageService', ['toggleLanguage'], {
      currentLanguage$: currentLanguageSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [InsightsComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(AuthService, { useValue: authService })
    .overrideProvider(Router, { useValue: router })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(FiscalYearService, { useValue: fyService })
    .overrideProvider(FundingItemService, { useValue: fundingItemService })
    .overrideProvider(SpendingItemService, { useValue: spendingItemService })
    .overrideProvider(ProcurementService, { useValue: procurementService })
    .overrideProvider(LanguageService, { useValue: languageService })
    .compileComponents();

    fixture = TestBed.createComponent(InsightsComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.isLoading).toBeFalse();
      expect(component.errorMessage).toBeNull();
      expect(component.fundingItems).toEqual([]);
      expect(component.spendingItems).toEqual([]);
      expect(component.procurementItems).toEqual([]);
    });
  });

  describe('Authentication', () => {
    it('should redirect to login if user is null', fakeAsync(() => {
      currentUserSubject.next(null);
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    }));

    it('should load context when user is authenticated', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(rcService.getSelectedRC).toHaveBeenCalled();
      expect(rcService.getSelectedFY).toHaveBeenCalled();
    }));
  });

  describe('Context loading', () => {
    it('should navigate to select page if RC is not selected', fakeAsync(() => {
      rcService.getSelectedRC.and.returnValue(null);
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    }));

    it('should navigate to select page if FY is not selected', fakeAsync(() => {
      rcService.getSelectedFY.and.returnValue(null);
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    }));

    it('should load RC and FY when context is set', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(fyService.getFiscalYear).toHaveBeenCalledWith(1, 1);
    }));

    it('should handle RC load error', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(throwError(() => new Error('RC error')));
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    }));

    it('should handle FY load error', fakeAsync(() => {
      fyService.getFiscalYear.and.returnValue(throwError(() => new Error('FY error')));
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    }));
  });

  describe('Data loading', () => {
    it('should load all data after context is loaded', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(fundingItemService.getFundingItemsByFY).toHaveBeenCalledWith(1);
      expect(spendingItemService.getSpendingItemsByFY).toHaveBeenCalledWith(1, 1);
      expect(procurementService.getProcurementItems).toHaveBeenCalledWith(1, 1);
    }));

    it('should set isLoading during data load', fakeAsync(() => {
      fixture.detectChanges();
      
      // During load
      expect(component.isLoading).toBeTrue();
      
      tick();
      
      // After load
      expect(component.isLoading).toBeFalse();
    }));
  });

  describe('Language changes', () => {
    it('should subscribe to language changes', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      // Trigger language change
      currentLanguageSubject.next('fr');
      tick(100);
      
      // Component should still be intact
      expect(component).toBeTruthy();
    }));
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    }));
  });
});
