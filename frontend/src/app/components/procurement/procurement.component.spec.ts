/**
 * Procurement Component Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 * @license MIT
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { of, BehaviorSubject, throwError } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { ProcurementComponent } from './procurement.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { ProcurementService } from '../../services/procurement.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { CategoryService } from '../../services/category.service';
import { User } from '../../models/user.model';
import { ProcurementItem } from '../../models/procurement.model';
import { Category } from '../../models/category.model';
import { Router } from '@angular/router';

describe('ProcurementComponent', () => {
  let component: ProcurementComponent;
  let fixture: ComponentFixture<ProcurementComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let procurementService: jasmine.SpyObj<ProcurementService>;
  let currencyService: jasmine.SpyObj<CurrencyService>;
  let fuzzySearchService: jasmine.SpyObj<FuzzySearchService>;
  let categoryService: jasmine.SpyObj<CategoryService>;
  let router: jasmine.SpyObj<Router>;
  let sanitizer: jasmine.SpyObj<DomSanitizer>;

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

  const mockCategories: Category[] = [
    {
      id: 1,
      name: 'Hardware',
      description: 'Hardware purchases',
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
      name: 'Software',
      description: 'Software licenses',
      isDefault: false,
      fiscalYearId: 1,
      displayOrder: 1,
      active: true,
      fundingType: 'BOTH',
      allowsCap: true,
      allowsOm: true
    }
  ];

  const mockProcurementItems: ProcurementItem[] = [
    {
      id: 1,
      purchaseRequisition: 'PR-2026-001',
      purchaseOrder: 'PO-2026-001',
      name: 'GPU Purchase',
      description: 'NVIDIA A100 GPUs',
      status: 'NOT_STARTED',
      currency: 'CAD',
      exchangeRate: undefined,
      preferredVendor: 'NVIDIA',
      contractNumber: 'CN-001',
      contractStartDate: '2026-01-01',
      contractEndDate: '2026-12-31',
      procurementCompleted: false,
      procurementCompletedDate: undefined,
      categoryId: 1,
      categoryName: 'Hardware',
      fiscalYearId: 1,
      fiscalYearName: 'FY 2025-2026',
      responsibilityCentreId: 1,
      responsibilityCentreName: 'Demo RC',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      quoteCount: 2
    },
    {
      id: 2,
      purchaseRequisition: 'PR-2026-002',
      purchaseOrder: undefined,
      name: 'Server Purchase',
      description: 'Dell PowerEdge Servers',
      status: 'QUOTE',
      currency: 'USD',
      exchangeRate: 1.35,
      preferredVendor: 'Dell',
      contractNumber: undefined,
      contractStartDate: undefined,
      contractEndDate: undefined,
      procurementCompleted: false,
      procurementCompletedDate: undefined,
      categoryId: 1,
      categoryName: 'Hardware',
      fiscalYearId: 1,
      fiscalYearName: 'FY 2025-2026',
      responsibilityCentreId: 1,
      responsibilityCentreName: 'Demo RC',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      quoteCount: 0
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
    const procurementSpy = jasmine.createSpyObj('ProcurementService', [
      'getProcurementItems',
      'createProcurementItem',
      'updateProcurementItem',
      'deleteProcurementItem',
      'updateProcurementItemStatus',
      'getQuotes',
      'createQuote',
      'deleteQuote',
      'updateQuoteStatus',
      'getQuoteFiles',
      'uploadQuoteFile',
      'deleteQuoteFile',
      'getEvents',
      'createEvent',
      'updateEvent',
      'deleteEvent'
    ]);
    const currencySpy = jasmine.createSpyObj('CurrencyService', ['getCurrencies']);
    const fuzzySpy = jasmine.createSpyObj('FuzzySearchService', ['filter']);
    const categorySpy = jasmine.createSpyObj('CategoryService', ['getCategoriesByFY']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const sanitizerSpy = jasmine.createSpyObj('DomSanitizer', ['bypassSecurityTrustResourceUrl']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        ProcurementComponent
      ],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy },
        { provide: ProcurementService, useValue: procurementSpy },
        { provide: CurrencyService, useValue: currencySpy },
        { provide: FuzzySearchService, useValue: fuzzySpy },
        { provide: CategoryService, useValue: categorySpy },
        { provide: Router, useValue: routerSpy },
        { provide: DomSanitizer, useValue: sanitizerSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
    procurementService = TestBed.inject(ProcurementService) as jasmine.SpyObj<ProcurementService>;
    currencyService = TestBed.inject(CurrencyService) as jasmine.SpyObj<CurrencyService>;
    fuzzySearchService = TestBed.inject(FuzzySearchService) as jasmine.SpyObj<FuzzySearchService>;
    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    sanitizer = TestBed.inject(DomSanitizer) as jasmine.SpyObj<DomSanitizer>;
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
      { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true },
      { code: 'USD', name: 'US Dollar', symbol: '$', isDefault: false }
    ]));
    categoryService.getCategoriesByFY.and.returnValue(of(mockCategories));
    procurementService.getProcurementItems.and.returnValue(of(mockProcurementItems));
    fuzzySearchService.filter.and.callFake((items: any[]) => items);

    fixture = TestBed.createComponent(ProcurementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    currentUser$.next(mockUser);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should load currencies on init', () => {
      expect(currencyService.getCurrencies).toHaveBeenCalled();
    });

    it('should load RC and FY on init', () => {
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(fyService.getFiscalYear).toHaveBeenCalledWith(1, 1);
    });

    it('should load categories for the selected FY', () => {
      expect(categoryService.getCategoriesByFY).toHaveBeenCalledWith(1, 1);
    });

    it('should load procurement items for the selected FY', () => {
      expect(procurementService.getProcurementItems).toHaveBeenCalledWith(1, 1, undefined, undefined);
    });

    it('should navigate to select if no RC or FY selected', () => {
      rcService.getSelectedRC.and.returnValue(null);

      fixture = TestBed.createComponent(ProcurementComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(router.navigate).toHaveBeenCalledWith(['/app/select']);
    });

    it('should redirect to login if no current user', () => {
      currentUser$.next(null);

      fixture = TestBed.createComponent(ProcurementComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('canWrite', () => {
    it('should return true for owner', () => {
      component.selectedRC = { id: 1, name: 'Test', isOwner: true, accessLevel: 'READ_ONLY' } as any;
      
      expect(component.canWrite).toBeTrue();
    });

    it('should return true for READ_WRITE access', () => {
      component.selectedRC = { id: 1, name: 'Test', isOwner: false, accessLevel: 'READ_WRITE' } as any;
      
      expect(component.canWrite).toBeTrue();
    });

    it('should return false for READ_ONLY access', () => {
      component.selectedRC = { id: 1, name: 'Test', isOwner: false, accessLevel: 'READ_ONLY' } as any;
      
      expect(component.canWrite).toBeFalse();
    });

    it('should return false when no RC selected', () => {
      component.selectedRC = null;
      
      expect(component.canWrite).toBeFalse();
    });
  });

  describe('filterByCategory', () => {
    it('should set selected category ID', () => {
      component.filterByCategory(2);

      expect(component.selectedCategoryId).toBe(2);
    });

    it('should clear category filter when null', () => {
      component.selectedCategoryId = 2;
      component.filterByCategory(null);

      expect(component.selectedCategoryId).toBeNull();
    });
  });

  describe('clearSearch', () => {
    it('should clear search term', () => {
      component.searchTerm = 'test search';
      component.clearSearch();

      expect(component.searchTerm).toBe('');
    });
  });

  describe('showCreateItem', () => {
    it('should show create form', () => {
      component.showCreateItemForm = false;
      component.showCreateItem();

      expect(component.showCreateItemForm).toBeTrue();
    });

    it('should reset form fields', () => {
      component.newItemPR = 'Old PR';
      component.newItemName = 'Old Name';
      component.showCreateItem();

      expect(component.newItemPR).toBe('');
      expect(component.newItemName).toBe('');
    });
  });

  describe('cancelCreateItem', () => {
    it('should hide create form', () => {
      component.showCreateItemForm = true;
      component.cancelCreateItem();

      expect(component.showCreateItemForm).toBeFalse();
    });

    it('should reset form fields', () => {
      component.newItemPR = 'Some PR';
      component.cancelCreateItem();

      expect(component.newItemPR).toBe('');
    });
  });

  describe('createProcurementItem', () => {
    it('should not create item without PR', () => {
      component.newItemPR = '';
      component.newItemName = 'Test';
      
      component.createProcurementItem();

      expect(procurementService.createProcurementItem).not.toHaveBeenCalled();
    });

    it('should not create item without name', () => {
      component.newItemPR = 'PR-001';
      component.newItemName = '';
      
      component.createProcurementItem();

      expect(procurementService.createProcurementItem).not.toHaveBeenCalled();
    });

    it('should create item with valid data', fakeAsync(() => {
      procurementService.createProcurementItem.and.returnValue(of(mockProcurementItems[0]));
      
      component.newItemPR = 'PR-003';
      component.newItemName = 'New Item';
      component.newItemDescription = 'Description';
      component.newItemCurrency = 'CAD';
      
      component.createProcurementItem();
      tick();

      expect(procurementService.createProcurementItem).toHaveBeenCalled();
      expect(component.showCreateItemForm).toBeFalse();
    }));

    it('should show error on create failure', fakeAsync(() => {
      procurementService.createProcurementItem.and.returnValue(
        throwError(() => new Error('Create failed'))
      );
      
      component.newItemPR = 'PR-003';
      component.newItemName = 'New Item';
      
      component.createProcurementItem();
      tick();

      expect(component.errorMessage).toContain('Failed');
    }));
  });

  describe('filteredProcurementItems', () => {
    it('should return all items when no filters', () => {
      component.searchTerm = '';
      component.selectedCategoryId = null;
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(2);
    });

    it('should filter by category', () => {
      component.searchTerm = '';
      component.selectedCategoryId = 2; // Software category
      
      const filtered = component.filteredProcurementItems;
      
      // Both mock items are in category 1, so none should match
      expect(filtered.length).toBe(0);
    });

    it('should use fuzzy search when search term provided', () => {
      component.searchTerm = 'GPU';
      fuzzySearchService.filter.and.returnValue([mockProcurementItems[0]]);
      
      const filtered = component.filteredProcurementItems;
      
      expect(fuzzySearchService.filter).toHaveBeenCalled();
      expect(filtered.length).toBe(1);
    });
  });

  describe('Status Options', () => {
    it('should have all status options defined', () => {
      expect(component.statusOptions).toContain('NOT_STARTED');
      expect(component.statusOptions).toContain('QUOTE');
      expect(component.statusOptions).toContain('CONTRACT_AWARDED');
      expect(component.statusOptions).toContain('CANCELLED');
    });
  });

  describe('Currency Handling', () => {
    it('should set default currency to CAD', () => {
      component.showCreateItem();
      
      expect(component.newItemCurrency).toBe('CAD');
    });

    it('should handle exchange rate for non-CAD currencies', () => {
      component.newItemCurrency = 'USD';
      component.newItemExchangeRate = 1.35;
      
      expect(component.newItemExchangeRate).toBe(1.35);
    });
  });

  describe('Error Handling', () => {
    it('should set error message on items load failure', fakeAsync(() => {
      procurementService.getProcurementItems.and.returnValue(
        throwError(() => new Error('Load failed'))
      );
      
      component.loadProcurementItems();
      tick();
      
      expect(component.errorMessage).toContain('Failed to load');
      expect(component.procurementItems.length).toBe(0);
    }));

    it('should use default currencies on currency load failure', fakeAsync(() => {
      currencyService.getCurrencies.and.returnValue(
        throwError(() => new Error('Currencies failed'))
      );
      
      fixture = TestBed.createComponent(ProcurementComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();
      
      expect(component.currencies.length).toBeGreaterThan(0);
      expect(component.currencies[0].code).toBe('CAD');
    }));
  });

  describe('Category Loading', () => {
    it('should load categories for selected FY', () => {
      expect(component.categories.length).toBe(2);
      expect(component.categories[0].name).toBe('Hardware');
    });

    it('should set empty categories on load failure', fakeAsync(() => {
      categoryService.getCategoriesByFY.and.returnValue(
        throwError(() => new Error('Categories failed'))
      );
      
      fixture = TestBed.createComponent(ProcurementComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();
      
      expect(component.categories.length).toBe(0);
    }));
  });

  describe('Cleanup', () => {
    it('should complete destroy$ subject on destroy', () => {
      spyOn(component['destroy$'], 'next');
      spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(component['destroy$'].next).toHaveBeenCalled();
      expect(component['destroy$'].complete).toHaveBeenCalled();
    });
  });
});
