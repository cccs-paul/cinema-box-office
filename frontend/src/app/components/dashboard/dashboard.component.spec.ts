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
import { TranslateModule } from '@ngx-translate/core';
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
        TranslateModule.forRoot(),
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
      showSearchBox: true,
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

    it('should return true when amount is a comma-formatted string', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: '2,198,957.89' as any, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return true when amount includes currency symbol', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyCode: 'AB', moneyName: 'A-Base', capAmount: '$1,500.00' as any, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });
  });

  describe('parseMoneyValue', () => {
    it('should parse plain numeric values', () => {
      expect(component.parseMoneyValue(5000)).toBe(5000);
    });

    it('should parse comma-formatted strings', () => {
      expect(component.parseMoneyValue('2,198,957.89')).toBe(2198957.89);
    });

    it('should parse strings with currency symbols', () => {
      expect(component.parseMoneyValue('$1,500.00')).toBe(1500);
    });

    it('should return 0 for null values', () => {
      expect(component.parseMoneyValue(null)).toBe(0);
    });

    it('should return 0 for undefined values', () => {
      expect(component.parseMoneyValue(undefined)).toBe(0);
    });

    it('should return 0 for NaN values', () => {
      expect(component.parseMoneyValue(NaN)).toBe(0);
    });

    it('should return 0 for invalid strings', () => {
      expect(component.parseMoneyValue('invalid')).toBe(0);
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

  describe('trackBy functions', () => {
    it('trackByItemId should return item id', () => {
      const mockItem = { id: 42, name: 'Test Item' } as any;
      expect(component.trackByItemId(0, mockItem)).toBe(42);
    });

    it('trackByGroupName should return group category name', () => {
      const mockGroup = { categoryName: 'Test Category', categoryId: 1, items: [] } as any;
      expect(component.trackByGroupName(0, mockGroup)).toBe('Test Category');
    });

    it('trackByItemId should work with different indices', () => {
      const mockItem1 = { id: 1, name: 'Item 1' } as any;
      const mockItem2 = { id: 2, name: 'Item 2' } as any;
      expect(component.trackByItemId(0, mockItem1)).toBe(1);
      expect(component.trackByItemId(5, mockItem2)).toBe(2);
    });

    it('trackByGroupName should handle empty category names', () => {
      const mockGroup = { categoryName: '', categoryId: null, items: [] } as any;
      expect(component.trackByGroupName(0, mockGroup)).toBe('');
    });
  });
});
