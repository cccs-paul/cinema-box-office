/**
 * Training Component Tests for myRC application.
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
import { TrainingComponent } from './training.component';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { TrainingItemService } from '../../services/training-item.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { User } from '../../models/user.model';
import { Money } from '../../models/money.model';
import { TrainingItem, TrainingMoneyAllocation } from '../../models/training-item.model';

describe('TrainingComponent', () => {
  let component: TrainingComponent;
  let fixture: ComponentFixture<TrainingComponent>;
  let trainingItemService: jasmine.SpyObj<TrainingItemService>;
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

  const mockAllocation: TrainingMoneyAllocation = {
    moneyId: 1,
    moneyName: 'A-Base',
    moneyCode: 'AB',
    isDefault: true,
    omAmount: 2500
  };

  const mockTrainingItem: TrainingItem = {
    id: 1,
    name: 'Java Certification',
    description: 'Oracle Java SE certification course',
    provider: 'Oracle',
    format: 'ONLINE',
    status: 'PLANNED',
    trainingType: 'COURSE_TRAINING',
    startDate: '2026-03-01',
    endDate: '2026-03-15',
    location: 'Online',
    numberOfParticipants: 0,
    participants: [],
    estimatedCostCad: 2500,
    fiscalYearId: 1,
    active: true,
    moneyAllocations: [mockAllocation],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const mockTrainingItems: TrainingItem[] = [mockTrainingItem];

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
    const trainingItemSpy = jasmine.createSpyObj('TrainingItemService', [
      'getTrainingItemsByFY',
      'createTrainingItem',
      'updateTrainingItem',
      'deleteTrainingItem',
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
        TrainingComponent
      ],
      providers: [
        FuzzySearchService,
        { provide: AuthService, useValue: authSpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy },
        { provide: TrainingItemService, useValue: trainingItemSpy },
        { provide: MoneyService, useValue: moneySpy },
        { provide: CurrencyService, useValue: currencySpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    const rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    const fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
    trainingItemService = TestBed.inject(TrainingItemService) as jasmine.SpyObj<TrainingItemService>;
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
    trainingItemService.getTrainingItemsByFY.and.returnValue(of(mockTrainingItems));

    fixture = TestBed.createComponent(TrainingComponent);
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
    it('should load training items on init', () => {
      expect(trainingItemService.getTrainingItemsByFY).toHaveBeenCalledWith(1, 1);
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
    it('should have training items loaded', () => {
      expect(component.trainingItems.length).toBe(1);
      expect(component.trainingItems[0].name).toBe('Java Certification');
    });

    it('should calculate summary totals', () => {
      expect(component.grandTotalEstimated).toBe(2500);
      expect(component.totalItems).toBe(1);
    });
  });

  describe('Search', () => {
    it('should filter items by search term', () => {
      component.searchTerm = 'Java';
      expect(component.filteredItems.length).toBe(1);
    });

    it('should show all items when search is empty', () => {
      component.searchTerm = '';
      expect(component.filteredItems.length).toBe(1);
    });
  });

  describe('Create training item', () => {
    it('should show create form', () => {
      component.openCreateForm();
      expect(component.showCreateForm).toBeTrue();
    });

    it('should hide create form on cancel', () => {
      component.openCreateForm();
      component.cancelCreate();
      expect(component.showCreateForm).toBeFalse();
    });

    it('should create training item via service', () => {
      trainingItemService.createTrainingItem.and.returnValue(of(mockTrainingItem));
      trainingItemService.getTrainingItemsByFY.and.returnValue(of(mockTrainingItems));

      component.openCreateForm();
      component.newItemName = 'New Training';
      component.newItemTrainingType = 'COURSE_TRAINING';
      component.createItem();

      expect(trainingItemService.createTrainingItem).toHaveBeenCalled();
    });
  });

  describe('Delete training item', () => {
    it('should delete training item', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      trainingItemService.deleteTrainingItem.and.returnValue(of(void 0));
      trainingItemService.getTrainingItemsByFY.and.returnValue(of([]));

      component.deleteItem(mockTrainingItem);

      expect(trainingItemService.deleteTrainingItem).toHaveBeenCalledWith(1, 1, 1);
    });
  });

  describe('Status update', () => {
    it('should update training item status', () => {
      trainingItemService.updateStatus.and.returnValue(of({ ...mockTrainingItem, status: 'APPROVED' }));
      trainingItemService.getTrainingItemsByFY.and.returnValue(of([{ ...mockTrainingItem, status: 'APPROVED' }]));

      component.updateStatus(mockTrainingItem, 'APPROVED');

      expect(trainingItemService.updateStatus).toHaveBeenCalledWith(1, 1, 1, 'APPROVED');
    });
  });

  describe('Budget mismatch warnings', () => {
    it('should return no warnings when allocation matches estimated cost', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 2500, estimatedCostCad: 2500, actualCostCad: null };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should warn when allocation differs from estimated cost', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 3000, estimatedCostCad: 2500, actualCostCad: null };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(1);
    });

    it('should warn when allocation differs from actual cost', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 3000, estimatedCostCad: null, actualCostCad: 2500 };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(1);
    });

    it('should return two warnings when both estimated and actual differ', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 3000, estimatedCostCad: 2500, actualCostCad: 2800 };
      const warnings = component.getItemWarnings(item as any);
      expect(warnings.length).toBe(2);
    });

    it('should not warn when allocation is zero', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 0, estimatedCostCad: 2500, actualCostCad: 2500 };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should not warn when estimated cost is zero', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 3000, estimatedCostCad: 0, actualCostCad: null };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });

    it('should tolerate tiny float differences within $0.01', () => {
      const item = { ...mockTrainingItem, moneyAllocationTotalOm: 2500, estimatedCostCad: 2500.005, actualCostCad: null };
      expect(component.getItemWarnings(item as any)).toEqual([]);
    });
  });

  describe('Summary calculations', () => {
    it('should count total participants', () => {
      const itemWithParticipants = { ...mockTrainingItem, numberOfParticipants: 3 };
      trainingItemService.getTrainingItemsByFY.and.returnValue(of([itemWithParticipants]));
      component.ngOnInit();
      fixture.detectChanges();
      expect(component.totalParticipants).toBe(3);
    });

    it('should sum grandTotalActual from actualCostCad', () => {
      const item = { ...mockTrainingItem, estimatedCostCad: 2500, actualCostCad: 2200 };
      trainingItemService.getTrainingItemsByFY.and.returnValue(of([item]));
      component.ngOnInit();
      fixture.detectChanges();
      expect(component.grandTotalActual).toBe(2200);
    });
  });

  describe('Delete confirmation', () => {
    it('should not delete when confirm is cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      trainingItemService.deleteTrainingItem.and.returnValue(of(void 0));

      component.deleteItem(mockTrainingItem);

      expect(trainingItemService.deleteTrainingItem).not.toHaveBeenCalled();
    });
  });

  describe('Participant management', () => {
    it('should add new participant with default values', () => {
      const participants: any[] = [];
      component.addNewParticipant(participants);
      expect(participants.length).toBe(1);
      expect(participants[0].status).toBe('PLANNED');
      expect(participants[0].estimatedCurrency).toBe('CAD');
      expect(participants[0].finalCurrency).toBe('CAD');
      expect(participants[0].estimatedCost).toBeNull();
      expect(participants[0].finalCost).toBeNull();
    });

    it('should remove new participant by index', () => {
      const participants: any[] = [
        { name: 'Alice', status: 'PLANNED' },
        { name: 'Bob', status: 'PLANNED' }
      ];
      component.removeNewParticipant(participants, 0);
      expect(participants.length).toBe(1);
      expect(participants[0].name).toBe('Bob');
    });
  });
});
