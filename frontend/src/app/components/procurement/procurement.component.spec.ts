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
import { Router, ActivatedRoute } from '@angular/router';
import { DomSanitizer } from '@angular/platform-browser';
import { of, BehaviorSubject, throwError, Subject } from 'rxjs';
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
import { ProcurementItem, ProcurementEvent, ProcurementEventFile, ProcurementEventType } from '../../models/procurement.model';
import { Category } from '../../models/category.model';

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
      currentStatus: 'DRAFT',
      trackingStatus: 'ON_TRACK',
      vendor: 'NVIDIA',
      finalPrice: 50000,
      finalPriceCurrency: 'CAD',
      finalPriceCad: undefined,
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
      currentStatus: 'PENDING_QUOTES',
      trackingStatus: 'AT_RISK',
      vendor: 'Dell',
      finalPrice: 75000,
      finalPriceCurrency: 'USD',
      finalPriceExchangeRate: 1.35,
      finalPriceCad: 101250,
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
      'deleteEvent',
      'getEventFiles',
      'uploadEventFile',
      'deleteEventFile',
      'updateEventFileDescription',
      'downloadEventFile',
      'getEventFileDownloadUrl'
    ]);
    const currencySpy = jasmine.createSpyObj('CurrencyService', ['getCurrencies']);
    const fuzzySpy = jasmine.createSpyObj('FuzzySearchService', ['filter']);
    const categorySpy = jasmine.createSpyObj('CategoryService', ['getCategoriesByFY']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
    routerSpy.navigate.and.returnValue(Promise.resolve(true));
    const sanitizerSpy = jasmine.createSpyObj('DomSanitizer', ['bypassSecurityTrustResourceUrl']);

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
        { provide: DomSanitizer, useValue: sanitizerSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
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
      showSearchBox: true,
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

  describe('filterByTrackingStatus', () => {
    it('should set selected tracking status', () => {
      component.filterByTrackingStatus('ON_TRACK');

      expect(component.selectedTrackingStatus).toBe('ON_TRACK');
    });

    it('should clear tracking status filter when null', () => {
      component.selectedTrackingStatus = 'AT_RISK';
      component.filterByTrackingStatus(null);

      expect(component.selectedTrackingStatus).toBeNull();
    });

    it('should toggle off when clicking the same status', () => {
      component.selectedTrackingStatus = 'ON_TRACK';
      component.filterByTrackingStatus('ON_TRACK');

      expect(component.selectedTrackingStatus).toBeNull();
    });

    it('should switch to new status when clicking different status', () => {
      component.selectedTrackingStatus = 'ON_TRACK';
      component.filterByTrackingStatus('AT_RISK');

      expect(component.selectedTrackingStatus).toBe('AT_RISK');
    });
  });

  describe('clearSearch', () => {
    it('should clear search term', () => {
      component.searchTerm = 'test search';
      component.clearSearch();

      expect(component.searchTerm).toBe('');
    });

    it('should clear category filter', () => {
      component.selectedCategoryId = 2;
      component.clearSearch();

      expect(component.selectedCategoryId).toBeNull();
    });

    it('should clear tracking status filter', () => {
      component.selectedTrackingStatus = 'ON_TRACK';
      component.clearSearch();

      expect(component.selectedTrackingStatus).toBeNull();
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
    it('should create item without PR (PR is optional)', fakeAsync(() => {
      procurementService.createProcurementItem.and.returnValue(of(mockProcurementItems[0]));
      
      component.newItemPR = '';
      component.newItemName = 'Test';
      component.newItemFinalPriceCurrency = 'CAD';
      
      component.createProcurementItem();
      tick();

      expect(procurementService.createProcurementItem).toHaveBeenCalled();
    }));

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
      component.newItemFinalPriceCurrency = 'CAD';
      
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
      component.selectedTrackingStatus = null;
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(2);
    });

    it('should filter by category', () => {
      component.searchTerm = '';
      component.selectedCategoryId = 2; // Software category
      component.selectedTrackingStatus = null;
      
      const filtered = component.filteredProcurementItems;
      
      // Both mock items are in category 1, so none should match
      expect(filtered.length).toBe(0);
    });

    it('should use fuzzy search when search term provided', () => {
      component.searchTerm = 'GPU';
      component.selectedTrackingStatus = null;
      fuzzySearchService.filter.and.returnValue([mockProcurementItems[0]]);
      
      const filtered = component.filteredProcurementItems;
      
      expect(fuzzySearchService.filter).toHaveBeenCalled();
      expect(filtered.length).toBe(1);
    });

    it('should filter by tracking status ON_TRACK', () => {
      component.searchTerm = '';
      component.selectedCategoryId = null;
      component.selectedTrackingStatus = 'ON_TRACK';
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(1);
      expect(filtered[0].trackingStatus).toBe('ON_TRACK');
    });

    it('should filter by tracking status AT_RISK', () => {
      component.searchTerm = '';
      component.selectedCategoryId = null;
      component.selectedTrackingStatus = 'AT_RISK';
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(1);
      expect(filtered[0].trackingStatus).toBe('AT_RISK');
    });

    it('should combine category and tracking status filters', () => {
      component.searchTerm = '';
      component.selectedCategoryId = 1; // Hardware
      component.selectedTrackingStatus = 'ON_TRACK';
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(1);
      expect(filtered[0].trackingStatus).toBe('ON_TRACK');
      expect(filtered[0].categoryId).toBe(1);
    });

    it('should return empty when tracking status has no matches', () => {
      component.searchTerm = '';
      component.selectedCategoryId = null;
      component.selectedTrackingStatus = 'CANCELLED';
      
      const filtered = component.filteredProcurementItems;
      
      expect(filtered.length).toBe(0);
    });
  });

  describe('Status Options', () => {
    it('should have all status options defined', () => {
      expect(component.statusOptions).toContain('DRAFT');
      expect(component.statusOptions).toContain('PENDING_QUOTES');
      expect(component.statusOptions).toContain('APPROVED');
      expect(component.statusOptions).toContain('CANCELLED');
    });
  });

  describe('Currency Handling', () => {
    it('should set default final price currency to CAD', () => {
      component.showCreateItem();
      
      expect(component.newItemFinalPriceCurrency).toBe('CAD');
    });

    it('should handle exchange rate for non-CAD final price currencies', () => {
      component.newItemFinalPriceCurrency = 'USD';
      component.newItemFinalPriceExchangeRate = 1.35;
      
      expect(component.newItemFinalPriceExchangeRate).toBe(1.35);
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

  describe('Event File Operations', () => {
    const mockEvent: ProcurementEvent = {
      id: 1,
      procurementItemId: 1,
      eventType: 'NOTE_ADDED' as ProcurementEventType,
      eventDate: '2026-01-15',
      comment: 'Test event',
      fileCount: 2
    };

    const mockEventFiles: ProcurementEventFile[] = [
      {
        id: 1,
        fileName: 'test-document.pdf',
        contentType: 'application/pdf',
        fileSize: 1048576,
        formattedFileSize: '1.00 MB',
        description: 'Test file',
        eventId: 1
      },
      {
        id: 2,
        fileName: 'spreadsheet.xlsx',
        contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        fileSize: 512000,
        formattedFileSize: '500.00 KB',
        description: '',
        eventId: 1
      }
    ];

    beforeEach(() => {
      procurementService.getEventFiles.and.returnValue(of(mockEventFiles));
      procurementService.uploadEventFile.and.returnValue(of(mockEventFiles[0]));
      procurementService.deleteEventFile.and.returnValue(of(void 0));
      procurementService.updateEventFileDescription.and.returnValue(of({
        ...mockEventFiles[0],
        description: 'Updated description'
      }));
      procurementService.downloadEventFile.and.returnValue(of(new Blob(['test'], { type: 'application/pdf' })));
      procurementService.getEventFileDownloadUrl.and.returnValue('/api/files/1');
      
      // Set component state for event file tests
      component.selectedRC = {
        id: 1,
        name: 'Test RC',
        description: '',
        active: true,
        isOwner: true,
        accessLevel: 'READ_WRITE'
      } as any;
      component.selectedFY = {
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
      } as any;
      component.procurementItems = mockProcurementItems;
      // Set selectedItem which is needed by the file methods
      component.selectedItem = mockProcurementItems[0];
    });

    describe('toggleEventFiles', () => {
      it('should expand files section when not expanded', fakeAsync(() => {
        component.expandedEventFiles = {};
        
        component.toggleEventFiles(mockEvent);
        tick();
        
        expect(component.expandedEventFiles[mockEvent.id]).toBe(true);
        expect(procurementService.getEventFiles).toHaveBeenCalledWith(1, 1, 1, mockEvent.id);
        expect(component.eventFiles[mockEvent.id]).toEqual(mockEventFiles);
      }));

      it('should collapse files section when already expanded', () => {
        component.expandedEventFiles = { [mockEvent.id]: true };
        component.eventFiles = { [mockEvent.id]: mockEventFiles };
        
        component.toggleEventFiles(mockEvent);
        
        expect(component.expandedEventFiles[mockEvent.id]).toBe(false);
        expect(procurementService.getEventFiles).not.toHaveBeenCalled();
      });

      it('should load files after expanding', fakeAsync(() => {
        component.expandedEventFiles = {};
        component.isLoadingEventFiles = {};
        
        component.toggleEventFiles(mockEvent);
        tick();
        
        // After tick, files should be loaded and loading state should be false
        expect(component.isLoadingEventFiles[mockEvent.id]).toBe(false);
        expect(component.eventFiles[mockEvent.id]).toEqual(mockEventFiles);
      }));

      it('should handle error when loading files fails', fakeAsync(() => {
        procurementService.getEventFiles.and.returnValue(
          throwError(() => new Error('Failed to load'))
        );
        component.expandedEventFiles = {};
        
        component.toggleEventFiles(mockEvent);
        tick();
        
        expect(component.isLoadingEventFiles[mockEvent.id]).toBe(false);
        expect(component.eventFiles[mockEvent.id]).toBeUndefined();
      }));
    });

    describe('loadEventFiles', () => {
      it('should load files for specific event', fakeAsync(() => {
        component.loadEventFiles(mockEvent);
        tick();
        
        expect(procurementService.getEventFiles).toHaveBeenCalledWith(1, 1, 1, mockEvent.id);
        expect(component.eventFiles[mockEvent.id]).toEqual(mockEventFiles);
      }));
    });

    describe('showEventFileUpload', () => {
      it('should show upload form for event', () => {
        component.showEventFileUpload(mockEvent);
        
        expect(component.selectedEventForUpload).toBe(mockEvent);
        expect(component.showEventFileUploadForm).toBe(true);
        expect(component.selectedEventFile).toBeNull();
        expect(component.eventFileDescription).toBe('');
      });
    });

    describe('cancelEventFileUpload', () => {
      it('should hide upload form and reset state', () => {
        component.selectedEventForUpload = mockEvent;
        component.showEventFileUploadForm = true;
        component.selectedEventFile = new File(['test'], 'test.pdf');
        component.eventFileDescription = 'Some description';
        
        component.cancelEventFileUpload();
        
        expect(component.showEventFileUploadForm).toBe(false);
        expect(component.selectedEventForUpload).toBeNull();
        expect(component.selectedEventFile).toBeNull();
        expect(component.eventFileDescription).toBe('');
      });
    });

    describe('onEventFileSelected', () => {
      it('should set selected file from input event', () => {
        const file = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
        const event = { target: { files: [file] } } as unknown as Event;
        
        component.onEventFileSelected(event);
        
        expect(component.selectedEventFile).toEqual(file);
      });

      it('should handle null files', () => {
        const event = { target: { files: null } } as unknown as Event;
        
        component.onEventFileSelected(event);
        
        expect(component.selectedEventFile).toBeNull();
      });

      it('should handle empty files array', () => {
        const event = { target: { files: [] } } as unknown as Event;
        
        component.onEventFileSelected(event);
        
        expect(component.selectedEventFile).toBeNull();
      });
    });

    describe('uploadEventFile', () => {
      it('should upload file to event', fakeAsync(() => {
        const testFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });
        component.selectedEventForUpload = mockEvent;
        component.selectedEventFile = testFile;
        component.eventFileDescription = 'Test description';
        component.expandedEventFiles = { [mockEvent.id]: true };
        component.eventFiles = { [mockEvent.id]: [] };
        
        component.uploadEventFile();
        tick();
        
        expect(procurementService.uploadEventFile).toHaveBeenCalledWith(
          1, 1, 1, mockEvent.id,
          testFile,
          'Test description'
        );
        expect(component.showEventFileUploadForm).toBe(false);
        expect(component.eventFiles[mockEvent.id]).toEqual([mockEventFiles[0]]);
      }));

      it('should not upload if no file selected', () => {
        component.selectedEventForUpload = mockEvent;
        component.selectedEventFile = null;
        
        component.uploadEventFile();
        
        expect(procurementService.uploadEventFile).not.toHaveBeenCalled();
      });

      it('should not upload if no event selected', () => {
        component.selectedEventForUpload = null;
        component.selectedEventFile = new File(['test'], 'test.pdf');
        
        component.uploadEventFile();
        
        expect(procurementService.uploadEventFile).not.toHaveBeenCalled();
      });

      it('should clear uploading state after successful upload', fakeAsync(() => {
        component.selectedEventForUpload = mockEvent;
        component.selectedEventFile = new File(['test'], 'test.pdf');
        component.eventFiles = { [mockEvent.id]: [] };
        
        component.uploadEventFile();
        tick();
        
        expect(component.isUploadingEventFile).toBe(false);
      }));

      it('should handle upload error', fakeAsync(() => {
        procurementService.uploadEventFile.and.returnValue(
          throwError(() => new Error('Upload failed'))
        );
        component.selectedEventForUpload = mockEvent;
        component.selectedEventFile = new File(['test'], 'test.pdf');
        
        component.uploadEventFile();
        tick();
        
        expect(component.isUploadingEventFile).toBe(false);
        // On error, the form should remain visible
      }));
    });

    describe('deleteEventFile', () => {
      it('should delete file after confirmation', fakeAsync(() => {
        spyOn(window, 'confirm').and.returnValue(true);
        component.eventFiles = { [mockEvent.id]: [...mockEventFiles] };
        
        component.deleteEventFile(mockEvent, mockEventFiles[0]);
        tick();
        
        expect(procurementService.deleteEventFile).toHaveBeenCalledWith(
          1, 1, mockEvent.procurementItemId, mockEvent.id, mockEventFiles[0].id
        );
        expect(component.eventFiles[mockEvent.id].length).toBe(1);
      }));

      it('should not delete file if confirmation cancelled', () => {
        spyOn(window, 'confirm').and.returnValue(false);
        component.eventFiles = { [mockEvent.id]: mockEventFiles };
        
        component.deleteEventFile(mockEvent, mockEventFiles[0]);
        
        expect(procurementService.deleteEventFile).not.toHaveBeenCalled();
      });

      it('should handle delete error', fakeAsync(() => {
        spyOn(window, 'confirm').and.returnValue(true);
        procurementService.deleteEventFile.and.returnValue(
          throwError(() => new Error('Delete failed'))
        );
        component.eventFiles = { [mockEvent.id]: mockEventFiles };
        
        component.deleteEventFile(mockEvent, mockEventFiles[0]);
        tick();
        
        // Files should remain unchanged on error
        expect(component.eventFiles[mockEvent.id].length).toBe(2);
      }));
    });

    describe('editEventFileDescription', () => {
      it('should enter edit mode for file', () => {
        component.editEventFileDescription(mockEventFiles[0]);
        
        expect(component.editingEventFile).toEqual(mockEventFiles[0]);
        expect(component.editingEventFileDescription).toBe(mockEventFiles[0].description ?? '');
      });

      it('should handle file with no description', () => {
        const fileWithoutDesc = { ...mockEventFiles[1], description: undefined };
        
        component.editEventFileDescription(fileWithoutDesc);
        
        expect(component.editingEventFileDescription).toBe('');
      });
    });

    describe('cancelEditEventFileDescription', () => {
      it('should exit edit mode and clear state', () => {
        component.editingEventFile = mockEventFiles[0];
        component.editingEventFileDescription = 'Some text';
        
        component.cancelEditEventFileDescription();
        
        expect(component.editingEventFile).toBeNull();
        expect(component.editingEventFileDescription).toBe('');
      });
    });

    describe('saveEventFileDescription', () => {
      it('should save updated description', fakeAsync(() => {
        component.editingEventFile = mockEventFiles[0];
        component.editingEventFileDescription = 'Updated description';
        component.eventFiles = { [mockEvent.id]: [...mockEventFiles] };
        
        component.saveEventFileDescription(mockEvent);
        tick();
        
        expect(procurementService.updateEventFileDescription).toHaveBeenCalledWith(
          1, 1, mockEvent.procurementItemId, mockEvent.id,
          mockEventFiles[0].id, 'Updated description'
        );
        expect(component.editingEventFile).toBeNull();
        expect(component.eventFiles[mockEvent.id][0].description).toBe('Updated description');
      }));

      it('should not save if no file being edited', () => {
        component.editingEventFile = null;
        
        component.saveEventFileDescription(mockEvent);
        
        expect(procurementService.updateEventFileDescription).not.toHaveBeenCalled();
      });

      it('should handle save error', fakeAsync(() => {
        procurementService.updateEventFileDescription.and.returnValue(
          throwError(() => new Error('Save failed'))
        );
        component.editingEventFile = mockEventFiles[0];
        component.editingEventFileDescription = 'New description';
        component.eventFiles = { [mockEvent.id]: mockEventFiles };
        
        component.saveEventFileDescription(mockEvent);
        tick();
        
        // Edit mode should remain active on error
        expect(component.editingEventFile).toBeTruthy();
      }));
    });

    describe('getFileIcon', () => {
      it('should return correct icon for PDF', () => {
        expect(component.getFileIcon('application/pdf')).toBe('📄');
      });

      it('should return correct icon for images', () => {
        expect(component.getFileIcon('image/png')).toBe('🖼️');
        expect(component.getFileIcon('image/jpeg')).toBe('🖼️');
      });

      it('should return correct icon for spreadsheets', () => {
        expect(component.getFileIcon('application/vnd.ms-excel')).toBe('📊');
        expect(component.getFileIcon('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')).toBe('📊');
      });

      it('should return correct icon for documents', () => {
        expect(component.getFileIcon('application/msword')).toBe('📝');
        expect(component.getFileIcon('application/vnd.openxmlformats-officedocument.wordprocessingml.document')).toBe('📝');
      });

      it('should return default icon for unknown types', () => {
        // Default icon is the paperclip emoji
        const paperclipIcon = '📎';
        expect(component.getFileIcon('application/octet-stream')).toBe(paperclipIcon);
        expect(component.getFileIcon('unknown/type')).toBe(paperclipIcon);
      });
    });

    describe('downloadEventFile', () => {
      it('should call download service for file', fakeAsync(() => {
        component.downloadEventFile(mockEvent, mockEventFiles[0]);
        tick();
        
        expect(procurementService.downloadEventFile).toHaveBeenCalledWith(
          1, 1, mockEvent.procurementItemId, mockEvent.id, mockEventFiles[0].id
        );
      }));
    });
  });
});
