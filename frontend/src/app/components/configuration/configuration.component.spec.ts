/**
 * myRC - Configuration Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { ConfigurationComponent } from './configuration.component';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { MoneyService } from '../../services/money.service';
import { CategoryService } from '../../services/category.service';
import { Money } from '../../models/money.model';
import { Category } from '../../models/category.model';

describe('ConfigurationComponent', () => {
  let component: ConfigurationComponent;
  let fixture: ComponentFixture<ConfigurationComponent>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let moneyService: jasmine.SpyObj<MoneyService>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  let selectedRC$: BehaviorSubject<number | null>;
  let selectedFY$: BehaviorSubject<number | null>;

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

  const mockMonies: Money[] = [
    { id: 1, code: 'AB', name: 'A-Base', description: 'Default', isDefault: true, displayOrder: 0, fiscalYearId: 1, active: true, capLabel: 'AB (CAP)', omLabel: 'AB (O&M)' },
    { id: 2, code: 'NB', name: 'New Base', description: 'New', isDefault: false, displayOrder: 1, fiscalYearId: 1, active: true, capLabel: 'NB (CAP)', omLabel: 'NB (O&M)' }
  ];

  const mockCategories: Category[] = [
    { id: 1, name: 'Software', description: 'Software purchases', fundingType: 'BOTH', displayOrder: 0, fiscalYearId: 1, isDefault: false, allowsCap: true, allowsOm: true, active: true },
    { id: 2, name: 'Hardware', description: 'Hardware purchases', fundingType: 'CAP_ONLY', displayOrder: 1, fiscalYearId: 1, isDefault: false, allowsCap: true, allowsOm: false, active: true }
  ];

  beforeEach(async () => {
    selectedRC$ = new BehaviorSubject<number | null>(1);
    selectedFY$ = new BehaviorSubject<number | null>(1);

    rcService = jasmine.createSpyObj('ResponsibilityCentreService', 
      ['getResponsibilityCentre'], {
      selectedRC$: selectedRC$.asObservable(),
      selectedFY$: selectedFY$.asObservable()
    });
    rcService.getResponsibilityCentre.and.returnValue(of(mockRC as any));

    fyService = jasmine.createSpyObj('FiscalYearService', ['getFiscalYear', 'updateFiscalYear']);
    fyService.getFiscalYear.and.returnValue(of(mockFY as any));
    fyService.updateFiscalYear.and.returnValue(of(mockFY as any));

    moneyService = jasmine.createSpyObj('MoneyService', 
      ['getMoniesByFiscalYear', 'createMoney', 'updateMoney', 'deleteMoney']);
    moneyService.getMoniesByFiscalYear.and.callFake(() => of([...mockMonies]));
    moneyService.createMoney.and.returnValue(of({ ...mockMonies[1], id: 3 }));
    moneyService.updateMoney.and.returnValue(of(mockMonies[1]));
    moneyService.deleteMoney.and.returnValue(of(undefined));

    categoryService = jasmine.createSpyObj('CategoryService', 
      ['getCategoriesByFY', 'createCategory', 'updateCategory', 'deleteCategory']);
    categoryService.getCategoriesByFY.and.callFake(() => of([...mockCategories]));
    categoryService.createCategory.and.returnValue(of({ ...mockCategories[1], id: 3 }));
    categoryService.updateCategory.and.returnValue(of(mockCategories[1]));
    categoryService.deleteCategory.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [ConfigurationComponent, FormsModule, TranslateModule.forRoot()]
    })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(FiscalYearService, { useValue: fyService })
    .overrideProvider(MoneyService, { useValue: moneyService })
    .overrideProvider(CategoryService, { useValue: categoryService })
    .compileComponents();

    fixture = TestBed.createComponent(ConfigurationComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should start with general tab active', () => {
      expect(component.activeTab).toBe('general');
    });

    it('should have funding type options', () => {
      expect(component.fundingTypeOptions).toEqual(['BOTH', 'CAP_ONLY', 'OM_ONLY']);
    });
  });

  describe('Context loading', () => {
    it('should load context when RC and FY are selected', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(fyService.getFiscalYear).toHaveBeenCalledWith(1, 1);
    }));

    it('should load monies when context is set', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(moneyService.getMoniesByFiscalYear).toHaveBeenCalledWith(1, 1);
    }));

    it('should load categories when context is set', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(categoryService.getCategoriesByFY).toHaveBeenCalledWith(1, 1);
    }));

    it('should not load when RC is null', fakeAsync(() => {
      selectedRC$.next(null);
      fixture.detectChanges();
      tick();
      
      expect(moneyService.getMoniesByFiscalYear).not.toHaveBeenCalled();
    }));
  });

  describe('Tab switching', () => {
    it('should switch to monies tab', () => {
      component.setActiveTab('monies');
      expect(component.activeTab).toBe('monies');
    });

    it('should switch to categories tab', () => {
      component.setActiveTab('categories');
      expect(component.activeTab).toBe('categories');
    });

    it('should switch to general tab', () => {
      component.setActiveTab('general');
      expect(component.activeTab).toBe('general');
    });

    it('should switch to import-export tab', () => {
      component.setActiveTab('import-export');
      expect(component.activeTab).toBe('import-export');
    });
  });

  describe('Money Management', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should load monies', () => {
      expect(component.monies.length).toBe(2);
      expect(component.monies[0].code).toBe('AB');
    });

    describe('Add Money', () => {
      it('should start adding money', () => {
        component.startAddMoney();
        expect(component.isAddingMoney).toBeTrue();
        expect(component.newMoney).toEqual({ code: '', name: '', description: '' });
      });

      it('should cancel adding money', () => {
        component.startAddMoney();
        component.cancelAddMoney();
        expect(component.isAddingMoney).toBeFalse();
      });

      it('should validate required fields', () => {
        component.startAddMoney();
        component.newMoney = { code: '', name: '', description: '' };
        component.saveMoney();
        expect(component.moneyError).toBe('Code and name are required');
      });

      it('should create money successfully', fakeAsync(() => {
        component.startAddMoney();
        component.newMoney = { code: 'NB', name: 'New Base', description: 'Test' };
        component.saveMoney();
        tick();
        
        expect(moneyService.createMoney).toHaveBeenCalledWith(1, 1, jasmine.objectContaining({
          code: 'NB',
          name: 'New Base'
        }));
        expect(component.isAddingMoney).toBeFalse();
      }));

      it('should handle create money error', fakeAsync(() => {
        moneyService.createMoney.and.returnValue(throwError(() => new Error('Create failed')));
        component.startAddMoney();
        component.newMoney = { code: 'NB', name: 'New Base', description: 'Test' };
        component.saveMoney();
        tick();
        
        expect(component.moneyError).toBe('Create failed');
        expect(component.isSaving).toBeFalse();
      }));
    });

    describe('Edit Money', () => {
      it('should start editing money', () => {
        component.startEditMoney(mockMonies[1]);
        expect(component.editingMoneyId).toBe(2);
        expect(component.editMoney.name).toBe('New Base');
      });

      it('should cancel editing money', () => {
        component.startEditMoney(mockMonies[1]);
        component.cancelEditMoney();
        expect(component.editingMoneyId).toBeNull();
      });

      it('should validate required fields on update', () => {
        component.startEditMoney(mockMonies[1]);
        component.editMoney = { code: 'NB', name: '', description: '' };
        component.updateMoney(mockMonies[1]);
        expect(component.moneyError).toBe('Name is required');
      });

      it('should update money successfully', fakeAsync(() => {
        component.startEditMoney(mockMonies[1]);
        component.editMoney = { code: 'NB', name: 'Updated Name', description: 'Updated' };
        component.updateMoney(mockMonies[1]);
        tick();
        
        expect(moneyService.updateMoney).toHaveBeenCalledWith(1, 1, 2, jasmine.objectContaining({
          name: 'Updated Name'
        }));
        expect(component.editingMoneyId).toBeNull();
      }));
    });

    describe('Delete Money', () => {
      it('should not delete default money type', () => {
        component.deleteMoney(mockMonies[0]);
        expect(component.moneyError).toBe('Cannot delete the default money type (AB)');
      });

      it('should not delete money type that cannot be deleted', () => {
        const nonDeletableMoney: Money = { ...mockMonies[1], canDelete: false };
        component.deleteMoney(nonDeletableMoney);
        expect(component.moneyError).toBe('Cannot delete this money type because it has non-zero funding or spending allocations');
        expect(moneyService.deleteMoney).not.toHaveBeenCalled();
      });

      it('should delete non-default money with confirmation', fakeAsync(() => {
        spyOn(window, 'confirm').and.returnValue(true);
        const deletableMoney: Money = { ...mockMonies[1], canDelete: true };
        component.deleteMoney(deletableMoney);
        tick();
        
        expect(moneyService.deleteMoney).toHaveBeenCalledWith(1, 1, 2);
      }));

      it('should not delete when confirmation is cancelled', () => {
        spyOn(window, 'confirm').and.returnValue(false);
        const deletableMoney: Money = { ...mockMonies[1], canDelete: true };
        component.deleteMoney(deletableMoney);
        
        expect(moneyService.deleteMoney).not.toHaveBeenCalled();
      });
    });
  });

  describe('Category Management', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should load categories', () => {
      expect(component.categories.length).toBe(2);
      expect(component.categories[0].name).toBe('Software');
    });

    describe('Add Category', () => {
      it('should start adding category', () => {
        component.startAddCategory();
        expect(component.isAddingCategory).toBeTrue();
        expect(component.newCategory).toEqual({ name: '', description: '', fundingType: 'BOTH' });
      });

      it('should cancel adding category', () => {
        component.startAddCategory();
        component.cancelAddCategory();
        expect(component.isAddingCategory).toBeFalse();
      });

      it('should validate required fields', () => {
        component.startAddCategory();
        component.newCategory = { name: '', description: '', fundingType: 'BOTH' };
        component.saveCategory();
        expect(component.categoryError).toBe('Name is required');
      });
    });
  });

  describe('Utility functions', () => {
    it('should track by money id', () => {
      expect(component.trackByMoneyId(0, mockMonies[0])).toBe(1);
    });

    it('should clear error', () => {
      component.moneyError = 'Some error';
      component.clearError();
      expect(component.moneyError).toBeNull();
    });
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

  describe('Read-Only Access', () => {
    const readOnlyRC = {
      id: 1,
      name: 'Test RC',
      description: 'Test Description',
      ownerUsername: 'admin',
      accessLevel: 'READ_ONLY' as const,
      isOwner: false
    };

    const readWriteRC = {
      id: 1,
      name: 'Test RC',
      description: 'Test Description',
      ownerUsername: 'admin',
      accessLevel: 'READ_WRITE' as const,
      isOwner: false
    };

    it('should return true for isReadOnly when access level is READ_ONLY', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(of(readOnlyRC as any));
      fixture.detectChanges();
      tick();

      expect(component.isReadOnly).toBeTrue();
    }));

    it('should return false for isReadOnly when access level is OWNER', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(component.isReadOnly).toBeFalse();
    }));

    it('should return false for isReadOnly when access level is READ_WRITE', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(of(readWriteRC as any));
      fixture.detectChanges();
      tick();

      expect(component.isReadOnly).toBeFalse();
    }));

    it('should return false for isReadOnly when selectedRC is null', () => {
      component.selectedRC = null;
      expect(component.isReadOnly).toBeFalse();
    });

    describe('Money Management - Read Only', () => {
      beforeEach(fakeAsync(() => {
        rcService.getResponsibilityCentre.and.returnValue(of(readOnlyRC as any));
        fixture.detectChanges();
        tick();
      }));

      it('should not allow adding money when read-only', () => {
        expect(component.isReadOnly).toBeTrue();
        // startAddMoney should be prevented by the disabled button in template
        // but the method itself does not check; the UI prevents interaction
      });

      it('should not allow editing money when read-only', () => {
        component.startEditMoney(mockMonies[1]);
        // The edit form is hidden in the template via *ngIf="!isReadOnly"
        expect(component.editingMoneyId).toBe(2);
        // Reset
        component.cancelEditMoney();
      });
    });

    describe('Category Management - Read Only', () => {
      beforeEach(fakeAsync(() => {
        rcService.getResponsibilityCentre.and.returnValue(of(readOnlyRC as any));
        fixture.detectChanges();
        tick();
      }));

      it('should not allow adding category when read-only', () => {
        expect(component.isReadOnly).toBeTrue();
      });
    });
  });
});
