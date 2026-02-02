/**
 * RC Selection Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { RCSelectionComponent } from './rc-selection.component';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { AuthService } from '../../services/auth.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { Router } from '@angular/router';

describe('RCSelectionComponent', () => {
  let component: RCSelectionComponent;
  let fixture: ComponentFixture<RCSelectionComponent>;
  let rcServiceMock: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyServiceMock: jasmine.SpyObj<FiscalYearService>;
  let router: Router;

  // Factory functions to create fresh mock data for each test
  const createMockRCs = (): ResponsibilityCentreDTO[] => [
    { id: 1, name: 'Alpha RC', description: 'First', accessLevel: 'READ_WRITE', isOwner: true, ownerUsername: 'testuser' },
    { id: 2, name: 'Beta RC', description: 'Second', accessLevel: 'READ_ONLY', isOwner: false, ownerUsername: 'other' },
    { id: 3, name: 'Charlie RC', description: 'Third', accessLevel: 'READ_WRITE', isOwner: false, ownerUsername: 'other' }
  ];

  const createMockFiscalYears = (): FiscalYear[] => [
    { id: 1, name: 'FY 2025-2026', description: 'Current year', active: true, responsibilityCentreId: 1, showSearchBox: true, showCategoryFilter: true, groupByCategory: false, onTargetMin: -2, onTargetMax: 2 },
    { id: 2, name: 'FY 2024-2025', description: 'Previous year', active: false, responsibilityCentreId: 1, showSearchBox: true, showCategoryFilter: true, groupByCategory: false, onTargetMin: -2, onTargetMax: 2 }
  ];

  beforeEach(async () => {
    // Create mock services
    rcServiceMock = jasmine.createSpyObj('ResponsibilityCentreService', [
      'getAllResponsibilityCentres',
      'createResponsibilityCentre',
      'updateResponsibilityCentre',
      'deleteResponsibilityCentre',
      'cloneResponsibilityCentre',
      'setSelectedRC',
      'setSelectedFY'
    ]);
    fyServiceMock = jasmine.createSpyObj('FiscalYearService', [
      'getFiscalYearsByRC',
      'createFiscalYear',
      'updateFiscalYear'
    ]);

    // Create AuthService mock with BehaviorSubject for currentUser$
    const currentUserSubject = new BehaviorSubject({ id: 1, username: 'testuser', email: 'test@example.com' });
    const authServiceMock = {
      currentUser$: currentUserSubject.asObservable(),
      checkSession: jasmine.createSpy('checkSession'),
      logout: jasmine.createSpy('logout')
    };

    // Return fresh copies for each call
    rcServiceMock.getAllResponsibilityCentres.and.callFake(() => of(createMockRCs()));
    fyServiceMock.getFiscalYearsByRC.and.callFake(() => of(createMockFiscalYears()));

    await TestBed.configureTestingModule({
      imports: [
        RCSelectionComponent,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ResponsibilityCentreService, useValue: rcServiceMock },
        { provide: FiscalYearService, useValue: fyServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RCSelectionComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  describe('Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load responsibility centres on init', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(rcServiceMock.getAllResponsibilityCentres).toHaveBeenCalled();
      expect(component.responsibilityCentres.length).toBeGreaterThanOrEqual(3);
      expect(component.isLoading).toBeFalse();
    }));

    it('should handle error when loading RCs', fakeAsync(() => {
      rcServiceMock.getAllResponsibilityCentres.and.returnValue(
        throwError(() => ({ status: 500 }))
      );

      fixture.detectChanges();
      tick();

      expect(component.errorMessage).toContain('Failed to load');
    }));
  });

  describe('RC List Sorting', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should sort RCs alphabetically', () => {
      const sorted = component.sortedResponsibilityCentres;
      
      expect(sorted[0].name).toBe('Alpha RC');
      expect(sorted[1].name).toBe('Beta RC');
      expect(sorted[2].name).toBe('Charlie RC');
    });

    it('should sort Demo RC to the bottom', fakeAsync(() => {
      // Add Demo RC to the list
      component.responsibilityCentres = [
        { id: 1, name: 'Alpha RC', description: 'First', accessLevel: 'READ_WRITE', isOwner: true, ownerUsername: 'testuser' },
        { id: 4, name: 'Demo', description: 'Demo RC', accessLevel: 'READ_ONLY', isOwner: false, ownerUsername: 'admin' },
        { id: 2, name: 'Beta RC', description: 'Second', accessLevel: 'READ_ONLY', isOwner: false, ownerUsername: 'other' }
      ];
      tick();

      const sorted = component.sortedResponsibilityCentres;
      
      expect(sorted[0].name).toBe('Alpha RC');
      expect(sorted[1].name).toBe('Beta RC');
      expect(sorted[2].name).toBe('Demo'); // Demo should be at the bottom
    }));
  });

  describe('RC Selection', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should select RC and load fiscal years', fakeAsync(() => {
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();

      expect(component.selectedRCId).toBe(1);
      expect(fyServiceMock.getFiscalYearsByRC).toHaveBeenCalledWith(1);
      expect(component.fiscalYears.length).toBeGreaterThanOrEqual(2);
    }));

    it('should reset fiscal year selection when changing RC', fakeAsync(() => {
      const betaRC = component.responsibilityCentres.find(rc => rc.name === 'Beta RC')!;
      component.selectedFYId = 1;
      component.selectRCWithoutNavigate(betaRC);
      tick();

      expect(component.selectedFYId).toBeNull();
    }));

    it('should not reload if same RC is selected', fakeAsync(() => {
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
      fyServiceMock.getFiscalYearsByRC.calls.reset();

      component.selectRCWithoutNavigate(alphaRC);
      tick();

      expect(fyServiceMock.getFiscalYearsByRC).not.toHaveBeenCalled();
    }));
  });

  describe('Fiscal Year Selection', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should select fiscal year', () => {
      const fy = component.fiscalYears.find(f => f.name === 'FY 2025-2026')!;
      component.selectFY(fy);
      expect(component.selectedFYId).toBe(1);
    });

    it('should sort fiscal years alphabetically', () => {
      const sorted = component.sortedFiscalYears;
      
      expect(sorted[0].name).toBe('FY 2024-2025');
      expect(sorted[1].name).toBe('FY 2025-2026');
    });
  });

  describe('RC Creation', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should toggle create form visibility', () => {
      expect(component.showCreateForm).toBeFalse();
      
      component.toggleCreateForm();
      expect(component.showCreateForm).toBeTrue();
      
      component.toggleCreateForm();
      expect(component.showCreateForm).toBeFalse();
    });

    it('should create new RC and select it', fakeAsync(() => {
      const newRC: ResponsibilityCentreDTO = {
        id: 4,
        name: 'New RC',
        description: 'New Description',
        accessLevel: 'READ_WRITE',
        isOwner: true,
        ownerUsername: 'testuser'
      };

      rcServiceMock.createResponsibilityCentre.and.returnValue(of(newRC));

      component.newRCName = 'New RC';
      component.newRCDescription = 'New Description';
      component.createRC();
      tick();

      expect(rcServiceMock.createResponsibilityCentre).toHaveBeenCalledWith('New RC', 'New Description');
      expect(component.responsibilityCentres).toContain(newRC);
      expect(component.selectedRCId).toBe(4);
      expect(component.showCreateForm).toBeFalse();
    }));

    it('should handle duplicate RC name error', fakeAsync(() => {
      // Service now returns Error objects with message from handleError
      rcServiceMock.createResponsibilityCentre.and.returnValue(
        throwError(() => new Error('A Responsibility Centre with this name already exists.'))
      );

      component.newRCName = 'Alpha RC';
      component.createRC();
      tick();

      expect(component.errorMessage).toContain('already exists');
    }));

    it('should not create RC with empty name', () => {
      component.newRCName = '   ';
      component.createRC();

      expect(rcServiceMock.createResponsibilityCentre).not.toHaveBeenCalled();
      expect(component.errorMessage).toBe('RC name is required');
    });
  });

  describe('RC Cloning', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should toggle clone form and pre-fill name', () => {
      component.toggleCloneForm();
      expect(component.showCloneForm).toBeTrue();
      expect(component.cloneNewName).toBe('Alpha RC (Copy)');
    });

    it('should clone RC and select it', fakeAsync(() => {
      const clonedRC: ResponsibilityCentreDTO = {
        id: 5,
        name: 'Alpha RC Clone',
        description: 'First',
        accessLevel: 'READ_WRITE',
        isOwner: true,
        ownerUsername: 'testuser'
      };

      rcServiceMock.cloneResponsibilityCentre.and.returnValue(of(clonedRC));

      component.cloneNewName = 'Alpha RC Clone';
      component.cloneRC();
      tick();

      expect(rcServiceMock.cloneResponsibilityCentre).toHaveBeenCalledWith(1, 'Alpha RC Clone');
      expect(component.responsibilityCentres).toContain(clonedRC);
      expect(component.selectedRCId).toBe(5);
      expect(component.showCloneForm).toBeFalse();
    }));

    it('should handle clone error', fakeAsync(() => {
      rcServiceMock.cloneResponsibilityCentre.and.returnValue(
        throwError(() => ({ status: 400 }))
      );

      component.cloneNewName = 'Alpha RC';
      component.cloneRC();
      tick();

      expect(component.errorMessage).toContain('already exists');
    }));
  });

  describe('RC Renaming', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should toggle rename form and pre-fill values', () => {
      component.toggleRenameForm();
      expect(component.showRenameForm).toBeTrue();
      expect(component.renameNewName).toBe('Alpha RC');
      expect(component.renameNewDescription).toBe('First');
    });

    it('should rename RC', fakeAsync(() => {
      const updatedRC: ResponsibilityCentreDTO = {
        id: 1,
        name: 'Renamed RC',
        description: 'Updated',
        accessLevel: 'READ_WRITE',
        isOwner: true,
        ownerUsername: 'testuser'
      };

      rcServiceMock.updateResponsibilityCentre.and.returnValue(of(updatedRC));

      component.renameNewName = 'Renamed RC';
      component.renameNewDescription = 'Updated';
      component.renameRC();
      tick();

      expect(rcServiceMock.updateResponsibilityCentre).toHaveBeenCalledWith(1, 'Renamed RC', 'Updated');
      expect(component.showRenameForm).toBeFalse();
    }));
  });

  describe('RC Deletion', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should show delete confirmation', () => {
      component.confirmDelete();
      expect(component.showDeleteConfirm).toBeTrue();
    });

    it('should cancel delete', () => {
      component.confirmDelete();
      component.cancelDelete();
      expect(component.showDeleteConfirm).toBeFalse();
    });

    it('should delete RC', fakeAsync(() => {
      rcServiceMock.deleteResponsibilityCentre.and.returnValue(of(void 0));

      component.deleteRC();
      tick();

      expect(rcServiceMock.deleteResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(component.selectedRCId).toBeNull();
      expect(component.showDeleteConfirm).toBeFalse();
    }));
  });

  describe('Fiscal Year Creation', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should toggle FY create form visibility', () => {
      expect(component.showFYCreateForm).toBeFalse();
      
      component.toggleFYCreateForm();
      expect(component.showFYCreateForm).toBeTrue();
      
      component.toggleFYCreateForm();
      expect(component.showFYCreateForm).toBeFalse();
    });

    it('should validate FY form', () => {
      expect(component.isValidFYForm()).toBeFalse();

      component.newFYName = 'FY 2026-2027';
      expect(component.isValidFYForm()).toBeTrue();
    });

    it('should create new FY and select it', fakeAsync(() => {
      const newFY: FiscalYear = {
        id: 3,
        name: 'FY 2026-2027',
        description: 'Next year',
        active: true,
        responsibilityCentreId: 1,
        showSearchBox: true,
        showCategoryFilter: true,
        groupByCategory: false,
        onTargetMin: -2,
        onTargetMax: 2
      };

      fyServiceMock.createFiscalYear.and.returnValue(of(newFY));

      component.newFYName = 'FY 2026-2027';
      component.newFYDescription = 'Next year';
      component.createFY();
      tick();

      expect(fyServiceMock.createFiscalYear).toHaveBeenCalled();
      expect(component.fiscalYears).toContain(newFY);
      expect(component.selectedFYId).toBe(3);
      expect(component.showFYCreateForm).toBeFalse();
    }));
  });

  describe('Fiscal Year Renaming', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
      const fy = component.fiscalYears.find(f => f.name === 'FY 2025-2026')!;
      component.selectFY(fy);
    }));

    it('should toggle FY rename form and pre-fill values', () => {
      component.toggleFYRenameForm();
      expect(component.showFYRenameForm).toBeTrue();
      expect(component.renameFYNewName).toBe('FY 2025-2026');
      expect(component.renameFYNewDescription).toBe('Current year');
    });

    it('should rename FY', fakeAsync(() => {
      const updatedFY: FiscalYear = {
        id: 1,
        name: 'FY 2025-2026 (Updated)',
        description: 'Updated description',
        active: true,
        responsibilityCentreId: 1,
        showSearchBox: true,
        showCategoryFilter: true,
        groupByCategory: false,
        onTargetMin: -2,
        onTargetMax: 2
      };

      fyServiceMock.updateFiscalYear.and.returnValue(of(updatedFY));

      component.renameFYNewName = 'FY 2025-2026 (Updated)';
      component.renameFYNewDescription = 'Updated description';
      component.renameFY();
      tick();

      expect(fyServiceMock.updateFiscalYear).toHaveBeenCalled();
      expect(component.showFYRenameForm).toBeFalse();
    }));
  });

  describe('Navigation', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      const alphaRC = component.responsibilityCentres.find(rc => rc.name === 'Alpha RC')!;
      component.selectRCWithoutNavigate(alphaRC);
      tick();
    }));

    it('should navigate to dashboard with selected RC and FY', fakeAsync(() => {
      spyOn(router, 'navigate');
      
      const fy = component.fiscalYears.find(f => f.name === 'FY 2025-2026')!;
      component.selectFY(fy);
      component.navigateToDashboard();
      tick();

      expect(rcServiceMock.setSelectedRC).toHaveBeenCalledWith(1);
      expect(rcServiceMock.setSelectedFY).toHaveBeenCalledWith(1);
      expect(router.navigate).toHaveBeenCalledWith(['/app/dashboard']);
    }));

    it('should not navigate without FY selected', () => {
      spyOn(router, 'navigate');
      
      component.navigateToDashboard();

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('Access Level Display', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should return correct access level label', () => {
      expect(component.getAccessLevelLabel('READ_WRITE')).toBe('Read & Write');
      expect(component.getAccessLevelLabel('READ_ONLY')).toBe('Read Only');
    });

    it('should return correct access level class', () => {
      expect(component.getAccessLevelClass('READ_WRITE')).toBe('access-readwrite');
      expect(component.getAccessLevelClass('READ_ONLY')).toBe('access-readonly');
    });

    it('should correctly determine write access', () => {
      // Owner has write access
      component.selectedRCId = 1;
      expect(component.selectedRCCanWrite).toBeTrue();

      // Read-only user does not have write access
      component.selectedRCId = 2;
      expect(component.selectedRCCanWrite).toBeFalse();

      // Non-owner with READ_WRITE has write access
      component.selectedRCId = 3;
      expect(component.selectedRCCanWrite).toBeTrue();
    });

    it('should correctly determine owner status', () => {
      component.selectedRCId = 1;
      expect(component.selectedRCIsOwner).toBeTrue();

      component.selectedRCId = 2;
      expect(component.selectedRCIsOwner).toBeFalse();

      component.selectedRCId = 3;
      expect(component.selectedRCIsOwner).toBeFalse();
    });
  });

  describe('Message Handling', () => {
    it('should clear error message', () => {
      component.errorMessage = 'Test error';
      component.clearError();
      expect(component.errorMessage).toBeNull();
    });

    it('should clear success message', () => {
      component.successMessage = 'Test success';
      component.clearSuccess();
      expect(component.successMessage).toBeNull();
    });
  });

  describe('Form Closing', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should close all forms', () => {
      component.showCreateForm = true;
      component.showCloneForm = true;
      component.showRenameForm = true;
      component.showDeleteConfirm = true;
      component.showFYCreateForm = true;
      component.showFYRenameForm = true;

      component.closeAllForms();

      expect(component.showCreateForm).toBeFalse();
      expect(component.showCloneForm).toBeFalse();
      expect(component.showRenameForm).toBeFalse();
      expect(component.showDeleteConfirm).toBeFalse();
      expect(component.showFYCreateForm).toBeFalse();
      expect(component.showFYRenameForm).toBeFalse();
    });
  });
});
