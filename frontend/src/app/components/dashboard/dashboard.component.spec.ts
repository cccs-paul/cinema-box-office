/**
 * Dashboard Component Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of, BehaviorSubject } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { CurrencyService } from '../../services/currency.service';
import { MoneyService } from '../../services/money.service';
import { User } from '../../models/user.model';
import { MoneyAllocation } from '../../models/funding-item.model';
import { Money } from '../../models/money.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let fundingItemService: jasmine.SpyObj<FundingItemService>;
  let currencyService: jasmine.SpyObj<CurrencyService>;
  let moneyService: jasmine.SpyObj<MoneyService>;

  const currentUser$ = new BehaviorSubject<User | null>({
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
    lastLoginAt: new Date().toISOString(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  });

  const mockDefaultMoney: Money = {
    id: 1,
    code: 'AB',
    name: 'A-Base',
    description: 'Default money',
    isDefault: true,
    fiscalYearId: 1,
    fiscalYearName: 'FY 2025-2026',
    responsibilityCentreId: 1,
    displayOrder: 0,
    active: true,
    capLabel: 'AB (CAP)',
    omLabel: 'AB (O&M)'
  };

  const mockCustomMoney: Money = {
    id: 2,
    code: 'OA',
    name: 'Operating Allotment',
    description: 'Custom money',
    isDefault: false,
    fiscalYearId: 1,
    fiscalYearName: 'FY 2025-2026',
    responsibilityCentreId: 1,
    displayOrder: 1,
    active: true,
    capLabel: 'OA (CAP)',
    omLabel: 'OA (O&M)'
  };

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser$: currentUser$.asObservable()
    });
    const rcSpy = jasmine.createSpyObj('ResponsibilityCentreService', [
      'getSelectedRC',
      'getSelectedFY',
      'getResponsibilityCentre'
    ]);
    const fySpy = jasmine.createSpyObj('FiscalYearService', ['getFiscalYear']);
    const fundingItemSpy = jasmine.createSpyObj('FundingItemService', [
      'getFundingItemsByFY',
      'createFundingItem',
      'deleteFundingItem'
    ]);
    const currencySpy = jasmine.createSpyObj('CurrencyService', ['getCurrencies']);
    const moneySpy = jasmine.createSpyObj('MoneyService', ['getMoniesByFiscalYear']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        RouterTestingModule,
        DashboardComponent
      ],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy },
        { provide: FundingItemService, useValue: fundingItemSpy },
        { provide: CurrencyService, useValue: currencySpy },
        { provide: MoneyService, useValue: moneySpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
    fundingItemService = TestBed.inject(FundingItemService) as jasmine.SpyObj<FundingItemService>;
    currencyService = TestBed.inject(CurrencyService) as jasmine.SpyObj<CurrencyService>;
    moneyService = TestBed.inject(MoneyService) as jasmine.SpyObj<MoneyService>;
  });

  beforeEach(() => {
    rcService.getSelectedRC.and.returnValue(1);
    rcService.getSelectedFY.and.returnValue(1);
    rcService.getResponsibilityCentre.and.returnValue(of({
      id: 1,
      name: 'Test RC',
      description: '',
      active: true,
      isOwner: true,
      accessLevel: 'READ_WRITE'
    } as any));
    fyService.getFiscalYear.and.returnValue(of({
      id: 1,
      name: 'FY 2025-2026',
      description: '',
      active: true,
      responsibilityCentreId: 1,
      showCategoryFilter: true,
      groupByCategory: false,
      onTargetMin: -2,
      onTargetMax: 2
    }));
    currencyService.getCurrencies.and.returnValue(of([
      { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true }
    ]));
    moneyService.getMoniesByFiscalYear.and.returnValue(of([mockDefaultMoney, mockCustomMoney]));
    fundingItemService.getFundingItemsByFY.and.returnValue(of([]));

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('hasValidMoneyAllocation', () => {
    it('should return false when allocations array is empty', () => {
      component.newItemMoneyAllocations = [];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should return false when all allocations have zero amounts', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: 0, omAmount: 0 },
        { moneyId: 2, moneyCode: 'OA', moneyName: 'Operating Allotment', capAmount: 0, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should return true when CAP amount is positive', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: 5000, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return true when OM amount is positive', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: 0, omAmount: 3000 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return true when both CAP and OM are positive', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: 5000, omAmount: 3000 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return true when only one of multiple allocations has positive value', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: 0, omAmount: 0 },
        { moneyId: 2, moneyCode: 'OA', moneyName: 'Operating Allotment', capAmount: 1000, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return false when allocations have null amounts', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: null as any, omAmount: null as any }
      ];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should return false when allocations have undefined amounts', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: undefined as any, omAmount: undefined as any }
      ];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });
  });

  describe('initializeMoneyAllocations', () => {
    it('should set isDefault flag from money type', fakeAsync(() => {
      // Trigger money loading
      component.loadMonies();
      tick();
      
      expect(component.newItemMoneyAllocations.length).toBe(2);
      
      const abAllocation = component.newItemMoneyAllocations.find(a => a.moneyCode === 'AB');
      const oaAllocation = component.newItemMoneyAllocations.find(a => a.moneyCode === 'OA');
      
      expect(abAllocation?.isDefault).toBeTrue();
      expect(oaAllocation?.isDefault).toBeFalse();
    }));

    it('should initialize all allocations with zero amounts', fakeAsync(() => {
      component.loadMonies();
      tick();
      
      for (const allocation of component.newItemMoneyAllocations) {
        expect(allocation.capAmount).toBe(0);
        expect(allocation.omAmount).toBe(0);
      }
    }));
  });
});
