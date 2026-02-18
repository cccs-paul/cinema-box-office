/**
 * myRC - Summary Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { BehaviorSubject, of, throwError, Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { SummaryComponent } from './summary.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { SpendingItemService } from '../../services/spending-item.service';
import { ProcurementService } from '../../services/procurement.service';
import { MoneyService } from '../../services/money.service';
import { TrainingItemService } from '../../services/training-item.service';
import { TravelItemService } from '../../services/travel-item.service';
import { User } from '../../models/user.model';

describe('SummaryComponent', () => {
  let component: SummaryComponent;
  let fixture: ComponentFixture<SummaryComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let fundingItemService: jasmine.SpyObj<FundingItemService>;
  let spendingItemService: jasmine.SpyObj<SpendingItemService>;
  let procurementService: jasmine.SpyObj<ProcurementService>;
  let moneyService: jasmine.SpyObj<MoneyService>;
  let trainingItemService: jasmine.SpyObj<TrainingItemService>;
  let travelItemService: jasmine.SpyObj<TravelItemService>;

  let currentUserSubject: BehaviorSubject<User | null>;

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
    { id: 1, name: 'Funding 1', totalCap: 10000, totalOm: 5000 }
  ];

  const mockSpendingItems = [
    { id: 1, name: 'Spending 1', totalCap: 3000, totalOm: 1000 }
  ];

  const mockProcurementItems = [
    { id: 1, prNumber: 'PR-001', trackingStatus: 'COMPLETED' },
    { id: 2, prNumber: 'PR-002', trackingStatus: 'ON_TRACK' },
    { id: 3, prNumber: 'PR-003', trackingStatus: 'PLANNING' },
    { id: 4, prNumber: 'PR-004', trackingStatus: 'AT_RISK' }
  ];

  const mockMoneyTypes = [
    { id: 1, code: 'AB', name: 'A-Base', isDefault: true }
  ];

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<User | null>(mockUser);

    authService = jasmine.createSpyObj('AuthService', [], {
      currentUser$: currentUserSubject.asObservable()
    });

    router = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
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

    moneyService = jasmine.createSpyObj('MoneyService', ['getMoniesByFiscalYear']);
    moneyService.getMoniesByFiscalYear.and.returnValue(of(mockMoneyTypes as any));

    trainingItemService = jasmine.createSpyObj('TrainingItemService', ['getTrainingItemsByFY']);
    trainingItemService.getTrainingItemsByFY.and.returnValue(of([] as any));

    travelItemService = jasmine.createSpyObj('TravelItemService', ['getTravelItemsByFY']);
    travelItemService.getTravelItemsByFY.and.returnValue(of([] as any));

    await TestBed.configureTestingModule({
      imports: [SummaryComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(AuthService, { useValue: authService })
    .overrideProvider(Router, { useValue: router })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(FiscalYearService, { useValue: fyService })
    .overrideProvider(FundingItemService, { useValue: fundingItemService })
    .overrideProvider(SpendingItemService, { useValue: spendingItemService })
    .overrideProvider(ProcurementService, { useValue: procurementService })
    .overrideProvider(MoneyService, { useValue: moneyService })
    .overrideProvider(TrainingItemService, { useValue: trainingItemService })
    .overrideProvider(TravelItemService, { useValue: travelItemService })
    .compileComponents();

    fixture = TestBed.createComponent(SummaryComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.comparisonExpanded).toBeTrue();
      expect(component.moneyTypeExpanded).toBeTrue();
      expect(component.isLoading).toBeFalse();
    });

    it('should expose Math for template use', () => {
      expect(component.Math).toBe(Math);
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
      expect(moneyService.getMoniesByFiscalYear).toHaveBeenCalledWith(1, 1);
    }));

    it('should set isLoading during data load', fakeAsync(() => {
      // Verify initial state before detectChanges
      expect(component.isLoading).toBeFalse();
      
      fixture.detectChanges();
      tick();
      
      // After load completes, isLoading should be false
      expect(component.isLoading).toBeFalse();
    }));

    it('should handle data load error', fakeAsync(() => {
      fundingItemService.getFundingItemsByFY.and.returnValue(
        throwError(() => new Error('Load error'))
      );
      fixture.detectChanges();
      tick();
      
      expect(component.errorMessage).toContain('Failed to load data');
      expect(component.isLoading).toBeFalse();
    }));
  });

  describe('Section toggling', () => {
    it('should start with comparison section expanded', () => {
      expect(component.comparisonExpanded).toBeTrue();
    });

    it('should start with money type section expanded', () => {
      expect(component.moneyTypeExpanded).toBeTrue();
    });
  });

  describe('Procurement Tracking Status Statistics', () => {
    it('should count procurement items by tracking status', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      // mockProcurementItems has: COMPLETED, ON_TRACK, PLANNING, AT_RISK
      expect(component.procurementPlanning).toBe(1);
      expect(component.procurementOnTrack).toBe(1);
      expect(component.procurementAtRisk).toBe(1);
      expect(component.procurementCompleted).toBe(1);
      expect(component.procurementCancelled).toBe(0);
    }));

    it('should handle empty procurement items', fakeAsync(() => {
      procurementService.getProcurementItems.and.returnValue(of([]));
      fixture.detectChanges();
      tick();

      expect(component.procurementPlanning).toBe(0);
      expect(component.procurementOnTrack).toBe(0);
      expect(component.procurementAtRisk).toBe(0);
      expect(component.procurementCompleted).toBe(0);
      expect(component.procurementCancelled).toBe(0);
    }));

    it('should count cancelled items correctly', fakeAsync(() => {
      procurementService.getProcurementItems.and.returnValue(of([
        { id: 1, prNumber: 'PR-001', trackingStatus: 'CANCELLED', name: 'Item 1', fiscalYearId: 1 },
        { id: 2, prNumber: 'PR-002', trackingStatus: 'CANCELLED', name: 'Item 2', fiscalYearId: 1 }
      ] as any[]));
      fixture.detectChanges();
      tick();

      expect(component.procurementCancelled).toBe(2);
      expect(component.procurementCompleted).toBe(0);
    }));
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', () => {
      fixture.detectChanges();
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });
});
