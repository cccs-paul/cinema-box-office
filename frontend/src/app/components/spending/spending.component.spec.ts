/**
 * Spending Component Tests for myRC application.
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
import { of, BehaviorSubject, throwError } from 'rxjs';
import { SpendingComponent } from './spending.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { SpendingItemService } from '../../services/spending-item.service';
import { CategoryService } from '../../services/category.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { User } from '../../models/user.model';
import { Money } from '../../models/money.model';
import { SpendingItem, SpendingMoneyAllocation } from '../../models/spending-item.model';
import { Category } from '../../models/category.model';
import { Router } from '@angular/router';

describe('SpendingComponent', () => {
  let component: SpendingComponent;
  let fixture: ComponentFixture<SpendingComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let spendingItemService: jasmine.SpyObj<SpendingItemService>;
  let categoryService: jasmine.SpyObj<CategoryService>;
  let moneyService: jasmine.SpyObj<MoneyService>;
  let currencyService: jasmine.SpyObj<CurrencyService>;
  let router: jasmine.SpyObj<Router>;

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
    lastLoginAt: new Date().toISOString(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const currentUser$ = new BehaviorSubject<User | null>(mockUser);

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

  const mockCategories: Category[] = [
    {
      id: 1,
      name: 'Compute',
      description: 'Compute resources',
      isDefault: true,
      fiscalYearId: 1,
      displayOrder: 0,
      active: true,
      fundingType: 'BOTH',
      allowsCap: true,
      allowsOm: true
    },
    {
      id: 2,
      name: 'GPUs',
      description: 'Graphics Processing Units',
      isDefault: true,
      fiscalYearId: 1,
      displayOrder: 1,
      active: true,
      fundingType: 'BOTH',
      allowsCap: true,
      allowsOm: true
    }
  ];

  const mockSpendingItems: SpendingItem[] = [
    {
      id: 1,
      name: 'GPU Purchase',
      description: 'Purchase of NVIDIA A100 GPUs',
      vendor: 'NVIDIA',
      referenceNumber: 'PO-001',
      amount: 50000,
      status: 'DRAFT',
      currency: 'CAD',
      exchangeRate: null,
      categoryId: 2,
      categoryName: 'GPUs',
      fiscalYearId: 1,
      fiscalYearName: 'FY 2025-2026',
      responsibilityCentreId: 1,
      responsibilityCentreName: 'Demo RC',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      active: true,
      moneyAllocations: [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 50000, omAmount: 0 }
      ]
    }
  ];

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
    const spendingItemSpy = jasmine.createSpyObj('SpendingItemService', [
      'getSpendingItemsByFY',
      'createSpendingItem',
      'deleteSpendingItem',
      'updateSpendingItemStatus'
    ]);
    const spendingCategorySpy = jasmine.createSpyObj('CategoryService', [
      'getCategoriesByFY'
    ]);
    const moneySpy = jasmine.createSpyObj('MoneyService', ['getMoniesByFiscalYear']);
    const currencySpy = jasmine.createSpyObj('CurrencyService', ['getCurrencies']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        RouterTestingModule,
        SpendingComponent
      ],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy },
        { provide: SpendingItemService, useValue: spendingItemSpy },
        { provide: CategoryService, useValue: spendingCategorySpy },
        { provide: MoneyService, useValue: moneySpy },
        { provide: CurrencyService, useValue: currencySpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
    spendingItemService = TestBed.inject(SpendingItemService) as jasmine.SpyObj<SpendingItemService>;
    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
    moneyService = TestBed.inject(MoneyService) as jasmine.SpyObj<MoneyService>;
    currencyService = TestBed.inject(CurrencyService) as jasmine.SpyObj<CurrencyService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
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
      groupByCategory: false
    }));
    currencyService.getCurrencies.and.returnValue(of([
      { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true }
    ]));
    moneyService.getMoniesByFiscalYear.and.returnValue(of([mockDefaultMoney, mockCustomMoney]));
    categoryService.getCategoriesByFY.and.returnValue(of(mockCategories));
    spendingItemService.getSpendingItemsByFY.and.returnValue(of(mockSpendingItems));

    fixture = TestBed.createComponent(SpendingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    // Reset currentUser to valid user after each test
    currentUser$.next(mockUser);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('hasValidMoneyAllocation', () => {
    it('should return false when allocations array is empty', () => {
      component.newItemMoneyAllocations = [];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should return false when all allocations are zero', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should return true when at least one allocation has a CAP amount', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 1000, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should return true when at least one allocation has an OM amount', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 500 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });
  });

  describe('Initialization', () => {
    it('should load currencies on init', () => {
      expect(currencyService.getCurrencies).toHaveBeenCalled();
    });

    it('should load RC and FY on init', () => {
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(fyService.getFiscalYear).toHaveBeenCalledWith(1, 1);
    });

    it('should load monies for the selected FY', () => {
      expect(moneyService.getMoniesByFiscalYear).toHaveBeenCalledWith(1, 1);
    });

    it('should load spending categories for the selected FY', () => {
      expect(categoryService.getCategoriesByFY).toHaveBeenCalledWith(1, 1);
    });

    it('should load spending items for the selected FY', () => {
      expect(spendingItemService.getSpendingItemsByFY).toHaveBeenCalledWith(1, 1, undefined);
    });

    it('should set default category when categories are loaded', () => {
      expect(component.newItemCategoryId).toBe(1);
    });

    it('should navigate to select if no RC or FY selected', () => {
      rcService.getSelectedRC.and.returnValue(null);

      fixture = TestBed.createComponent(SpendingComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    });

    it('should redirect to login if no current user', () => {
      currentUser$.next(null);

      fixture = TestBed.createComponent(SpendingComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('filterByCategory', () => {
    it('should filter items by category', () => {
      spendingItemService.getSpendingItemsByFY.calls.reset();
      component.filterByCategory(2);

      expect(component.selectedCategoryId).toBe(2);
      expect(spendingItemService.getSpendingItemsByFY).toHaveBeenCalledWith(1, 1, 2);
    });

    it('should clear filter when category is null', () => {
      spendingItemService.getSpendingItemsByFY.calls.reset();
      component.selectedCategoryId = 2;
      component.filterByCategory(null);

      expect(component.selectedCategoryId).toBeNull();
      expect(spendingItemService.getSpendingItemsByFY).toHaveBeenCalledWith(1, 1, undefined);
    });
  });

  describe('showCreate', () => {
    it('should show create form', () => {
      component.showCreateForm = false;
      component.showCreate();

      expect(component.showCreateForm).toBeTrue();
    });

    it('should reset form fields', () => {
      component.newItemName = 'Old Name';
      component.showCreate();

      expect(component.newItemName).toBe('');
    });

    it('should initialize money allocations', fakeAsync(() => {
      tick(); // Wait for async loading to complete
      component.showCreate();

      expect(component.newItemMoneyAllocations.length).toBe(2);
    }));
  });

  describe('cancelCreate', () => {
    it('should hide create form', () => {
      component.showCreateForm = true;
      component.cancelCreate();

      expect(component.showCreateForm).toBeFalse();
    });

    it('should reset form fields', () => {
      component.newItemName = 'Test Item';
      component.cancelCreate();

      expect(component.newItemName).toBe('');
    });
  });

  describe('createSpendingItem', () => {
    beforeEach(fakeAsync(() => {
      tick(); // Ensure async initialization is complete
      component.newItemName = 'New GPU Purchase';
      component.newItemDescription = 'Description';
      component.newItemVendor = 'NVIDIA';
      component.newItemReferenceNumber = 'PO-002';
      component.newItemCategoryId = 2;
      component.newItemCurrency = 'CAD';
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 5000, omAmount: 0 }
      ];
    }));

    it('should create spending item successfully', fakeAsync(() => {
      const newItem: SpendingItem = {
        ...mockSpendingItems[0],
        id: 2,
        name: 'New GPU Purchase'
      };
      spendingItemService.createSpendingItem.and.returnValue(of(newItem));
      spendingItemService.getSpendingItemsByFY.and.returnValue(of([newItem]));

      component.createSpendingItem();
      tick();

      expect(spendingItemService.createSpendingItem).toHaveBeenCalled();
      expect(component.showCreateForm).toBeFalse();
    }));

    it('should not create if no valid allocation', fakeAsync(() => {
      tick();
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 0 }
      ];

      component.createSpendingItem();

      expect(spendingItemService.createSpendingItem).not.toHaveBeenCalled();
    }));

    it('should show error on failure', fakeAsync(() => {
      spendingItemService.createSpendingItem.and.returnValue(
        throwError(() => new Error('Failed to create'))
      );

      component.createSpendingItem();
      tick();

      expect(component.errorMessage).toBeTruthy();
    }));
  });

  describe('deleteSpendingItem', () => {
    it('should delete spending item successfully', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      spendingItemService.deleteSpendingItem.and.returnValue(of(void 0));
      spendingItemService.getSpendingItemsByFY.and.returnValue(of([]));

      component.deleteSpendingItem(mockSpendingItems[0]);
      tick();

      expect(spendingItemService.deleteSpendingItem).toHaveBeenCalledWith(1, 1, 1);
    }));

    it('should not delete if user cancels', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.deleteSpendingItem(mockSpendingItems[0]);

      expect(spendingItemService.deleteSpendingItem).not.toHaveBeenCalled();
    });

    it('should show error on delete failure', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      spendingItemService.deleteSpendingItem.and.returnValue(
        throwError(() => ({ error: { message: 'Cannot delete' } }))
      );

      component.deleteSpendingItem(mockSpendingItems[0]);
      tick();

      expect(component.errorMessage).toBeTruthy();
    }));
  });

  describe('getStatusLabel and getStatusClass', () => {
    it('should return status label for valid status', () => {
      const label = component.getStatusLabel('DRAFT');
      expect(label).toBe('Draft');
    });

    it('should return status class for valid status', () => {
      const cssClass = component.getStatusClass('DRAFT');
      expect(cssClass).toContain('status-');
    });
  });

  describe('calculateTotal', () => {
    it('should calculate total from allocations', () => {
      const item: SpendingItem = {
        ...mockSpendingItems[0],
        moneyAllocations: [
          { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 1000, omAmount: 500 },
          { moneyId: 2, moneyName: 'OA', isDefault: false, capAmount: 2000, omAmount: 300 }
        ]
      };

      const total = component.calculateTotal(item);
      expect(total).toBe(3800);
    });

    it('should return 0 for no allocations', () => {
      const item: SpendingItem = {
        ...mockSpendingItems[0],
        moneyAllocations: []
      };

      const total = component.calculateTotal(item);
      expect(total).toBe(0);
    });

    it('should return 0 for undefined allocations', () => {
      const item: SpendingItem = {
        ...mockSpendingItems[0],
        moneyAllocations: undefined
      };

      const total = component.calculateTotal(item);
      expect(total).toBe(0);
    });
  });

  describe('Error handling', () => {
    beforeEach(() => {
      // Ensure user is valid for error handling tests
      currentUser$.next(mockUser);
    });

    it('should handle RC load error', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(
        throwError(() => new Error('RC not found'))
      );

      fixture = TestBed.createComponent(SpendingComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    }));

    it('should handle category load error', fakeAsync(() => {
      categoryService.getCategoriesByFY.and.returnValue(
        throwError(() => ({ error: { message: 'Failed to load categories' } }))
      );

      fixture = TestBed.createComponent(SpendingComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      expect(component.categories).toEqual([]);
    }));

    it('should use default currencies on currency load error', fakeAsync(() => {
      currencyService.getCurrencies.and.returnValue(
        throwError(() => new Error('Failed to load currencies'))
      );

      fixture = TestBed.createComponent(SpendingComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      expect(component.currencies.length).toBeGreaterThan(0);
      expect(component.currencies[0].code).toBe('CAD');
    }));
  });

  describe('Form validation', () => {
    it('should not have valid money allocation with empty allocations', () => {
      component.newItemMoneyAllocations = [];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should not have valid money allocation with zero amounts', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeFalse();
    });

    it('should have valid money allocation with positive cap amount', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 1000, omAmount: 0 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });

    it('should have valid money allocation with positive om amount', () => {
      component.newItemMoneyAllocations = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 500 }
      ];
      expect(component.hasValidMoneyAllocation()).toBeTrue();
    });
  });
});
