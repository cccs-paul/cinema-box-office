/**
 * Travel Component Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of, BehaviorSubject, Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { TravelComponent } from './travel.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { TravelItemService } from '../../services/travel-item.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { User } from '../../models/user.model';
import { Money } from '../../models/money.model';
import { TravelItem, TravelMoneyAllocation } from '../../models/travel-item.model';

describe('TravelComponent', () => {
  let component: TravelComponent;
  let fixture: ComponentFixture<TravelComponent>;
  let travelItemService: jasmine.SpyObj<TravelItemService>;
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

  const mockAllocation: TravelMoneyAllocation = {
    moneyId: 1,
    moneyName: 'A-Base',
    moneyCode: 'AB',
    isDefault: true,
    omAmount: 3200
  };

  const mockTravelItem: TravelItem = {
    id: 1,
    name: 'Ottawa Conference Trip',
    description: 'Annual government technology conference',
    emap: 'EMAP-001',
    destination: 'Ottawa, ON',
    purpose: 'Conference attendance',
    status: 'PLANNED',
    travelType: 'DOMESTIC',
    departureDate: '2026-04-01',
    returnDate: '2026-04-05',
    travellers: [],
    numberOfTravellers: 0,
    estimatedCostCad: 3200,
    fiscalYearId: 1,
    active: true,
    moneyAllocations: [mockAllocation],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const mockTravelItems: TravelItem[] = [mockTravelItem];

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
    const travelItemSpy = jasmine.createSpyObj('TravelItemService', [
      'getTravelItemsByFY',
      'createTravelItem',
      'updateTravelItem',
      'deleteTravelItem',
      'updateStatus',
      'getMoneyAllocations',
      'updateMoneyAllocations'
    ]);
    const moneySpy = jasmine.createSpyObj('MoneyService', ['getMoniesByFiscalYear']);
    const currencySpy = jasmine.createSpyObj('CurrencyService', ['getCurrencies']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
    routerSpy.navigate.and.returnValue(Promise.resolve(true));

    const mockActivatedRoute = {
      snapshot: { params: {}, queryParams: {}, data: {} },
      params: of({}),
      queryParams: of({}),
      data: of({})
    };

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        TranslateModule.forRoot(),
        TravelComponent
      ],
      providers: [
        FuzzySearchService,
        { provide: AuthService, useValue: authSpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy },
        { provide: TravelItemService, useValue: travelItemSpy },
        { provide: MoneyService, useValue: moneySpy },
        { provide: CurrencyService, useValue: currencySpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    const rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    const fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
    travelItemService = TestBed.inject(TravelItemService) as jasmine.SpyObj<TravelItemService>;
    moneyService = TestBed.inject(MoneyService) as jasmine.SpyObj<MoneyService>;
    currencyService = TestBed.inject(CurrencyService) as jasmine.SpyObj<CurrencyService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

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
    moneyService.getMoniesByFiscalYear.and.returnValue(of([mockDefaultMoney]));
    travelItemService.getTravelItemsByFY.and.returnValue(of(mockTravelItems));

    fixture = TestBed.createComponent(TravelComponent);
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
    it('should load travel items on init', () => {
      expect(travelItemService.getTravelItemsByFY).toHaveBeenCalledWith(1, 1);
    });

    it('should load currencies on init', () => {
      expect(currencyService.getCurrencies).toHaveBeenCalled();
    });

    it('should load money types on init', () => {
      expect(moneyService.getMoniesByFiscalYear).toHaveBeenCalledWith(1, 1);
    });

    it('should redirect to login when user is null', () => {
      currentUser$.next(null);
      fixture.detectChanges();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('Data display', () => {
    it('should have travel items loaded', () => {
      expect(component.travelItems.length).toBe(1);
      expect(component.travelItems[0].name).toBe('Ottawa Conference Trip');
    });

    it('should calculate summary totals', () => {
      expect(component.grandTotalEstimated).toBe(3200);
      expect(component.totalItems).toBe(1);
    });
  });

  describe('Search', () => {
    it('should filter items by search term', () => {
      component.searchTerm = 'Ottawa';
      expect(component.filteredItems.length).toBe(1);
    });

    it('should show all items when search is empty', () => {
      component.searchTerm = '';
      expect(component.filteredItems.length).toBe(1);
    });
  });

  describe('Create travel item', () => {
    it('should show create form', () => {
      component.openCreateForm();
      expect(component.showCreateForm).toBeTrue();
    });

    it('should hide create form on cancel', () => {
      component.openCreateForm();
      component.cancelCreate();
      expect(component.showCreateForm).toBeFalse();
    });

    it('should create travel item via service', () => {
      travelItemService.createTravelItem.and.returnValue(of(mockTravelItem));
      travelItemService.getTravelItemsByFY.and.returnValue(of(mockTravelItems));

      component.openCreateForm();
      component.newItemName = 'New Trip';
      component.newItemTravelType = 'DOMESTIC';
      component.createItem();

      expect(travelItemService.createTravelItem).toHaveBeenCalled();
    });
  });

  describe('Delete travel item', () => {
    it('should delete travel item', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      travelItemService.deleteTravelItem.and.returnValue(of(void 0));
      travelItemService.getTravelItemsByFY.and.returnValue(of([]));

      component.deleteItem(mockTravelItem);

      expect(travelItemService.deleteTravelItem).toHaveBeenCalledWith(1, 1, 1);
    });
  });

  describe('Status update', () => {
    it('should update travel item status', () => {
      travelItemService.updateStatus.and.returnValue(of({ ...mockTravelItem, status: 'APPROVED' }));
      travelItemService.getTravelItemsByFY.and.returnValue(of([{ ...mockTravelItem, status: 'APPROVED' }]));

      component.updateStatus(mockTravelItem, 'APPROVED');

      expect(travelItemService.updateStatus).toHaveBeenCalledWith(1, 1, 1, 'APPROVED');
    });
  });

  describe('Budget mismatch warnings', () => {
    it('should return no warnings when allocation matches estimated cost', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 3200, estimatedCostCad: 3200, actualCostCad: null };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should warn when allocation differs from estimated cost', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 4000, estimatedCostCad: 3200, actualCostCad: null };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(1);
    });

    it('should warn when allocation differs from actual cost', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 4000, estimatedCostCad: null, actualCostCad: 3200 };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(1);
    });

    it('should return two warnings when both estimated and actual differ', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 4000, estimatedCostCad: 3200, actualCostCad: 3500 };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(2);
    });

    it('should not warn when allocation is zero', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 0, estimatedCostCad: 3200, actualCostCad: 3200 };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should not warn when both costs are zero', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 1000, estimatedCostCad: 0, actualCostCad: 0 };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should tolerate tiny float differences within $0.01', () => {
      const item = { ...mockTravelItem, moneyAllocationTotalOm: 3200, estimatedCostCad: 3200.005, actualCostCad: null };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });
  });

  describe('Summary calculations', () => {
    it('should count total travellers', () => {
      const itemWithTravellers = { ...mockTravelItem, numberOfTravellers: 4 };
      travelItemService.getTravelItemsByFY.and.returnValue(of([itemWithTravellers]));
      component.ngOnInit();
      fixture.detectChanges();
      expect(component.totalTravellers).toBe(4);
    });

    it('should sum grandTotalActual from actualCostCad', () => {
      const item = { ...mockTravelItem, estimatedCostCad: 3200, actualCostCad: 2800 };
      travelItemService.getTravelItemsByFY.and.returnValue(of([item]));
      component.ngOnInit();
      fixture.detectChanges();
      expect(component.grandTotalActual).toBe(2800);
    });
  });

  describe('Delete confirmation', () => {
    it('should not delete when confirm is cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      travelItemService.deleteTravelItem.and.returnValue(of(void 0));

      component.deleteItem(mockTravelItem);

      expect(travelItemService.deleteTravelItem).not.toHaveBeenCalled();
    });
  });

  describe('Traveller management', () => {
    it('should add new traveller with default values', () => {
      const travellers: any[] = [];
      component.addNewTraveller(travellers);
      expect(travellers.length).toBe(1);
      expect(travellers[0].approvalStatus).toBe('PLANNED');
      expect(travellers[0].estimatedCurrency).toBe('CAD');
      expect(travellers[0].finalCurrency).toBe('CAD');
      expect(travellers[0].estimatedCost).toBeNull();
      expect(travellers[0].finalCost).toBeNull();
    });

    it('should remove new traveller by index', () => {
      const travellers: any[] = [
        { name: 'Alice', approvalStatus: 'PLANNED' },
        { name: 'Bob', approvalStatus: 'PLANNED' }
      ];
      component.removeNewTraveller(travellers, 0);
      expect(travellers.length).toBe(1);
      expect(travellers[0].name).toBe('Bob');
    });
  });
});
