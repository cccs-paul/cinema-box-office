/**
 * Configuration Component Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 * @license MIT
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { ConfigurationComponent } from './configuration.component';
import { MoneyService } from '../../services/money.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { Money } from '../../models/money.model';

describe('ConfigurationComponent', () => {
  let component: ConfigurationComponent;
  let fixture: ComponentFixture<ConfigurationComponent>;
  let moneyService: jasmine.SpyObj<MoneyService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;

  const selectedRC$ = new BehaviorSubject<number | null>(1);
  const selectedFY$ = new BehaviorSubject<number | null>(1);

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
    omLabel: 'AB (OM)'
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
    omLabel: 'OA (OM)'
  };

  beforeEach(async () => {
    const moneySpy = jasmine.createSpyObj('MoneyService', [
      'getMoniesByFiscalYear',
      'createMoney',
      'updateMoney',
      'deleteMoney',
      'reorderMonies'
    ]);
    const rcSpy = jasmine.createSpyObj('ResponsibilityCentreService', ['getResponsibilityCentre'], {
      selectedRC$: selectedRC$.asObservable(),
      selectedFY$: selectedFY$.asObservable()
    });
    const fySpy = jasmine.createSpyObj('FiscalYearService', ['getFiscalYear']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule, ConfigurationComponent],
      providers: [
        { provide: MoneyService, useValue: moneySpy },
        { provide: ResponsibilityCentreService, useValue: rcSpy },
        { provide: FiscalYearService, useValue: fySpy }
      ]
    }).compileComponents();

    moneyService = TestBed.inject(MoneyService) as jasmine.SpyObj<MoneyService>;
    rcService = TestBed.inject(ResponsibilityCentreService) as jasmine.SpyObj<ResponsibilityCentreService>;
    fyService = TestBed.inject(FiscalYearService) as jasmine.SpyObj<FiscalYearService>;
  });

  beforeEach(() => {
    rcService.getResponsibilityCentre.and.returnValue(of({ id: 1, name: 'Test RC', description: '', active: true } as any));
    fyService.getFiscalYear.and.returnValue(of({ id: 1, name: 'FY 2025-2026', description: '', active: true, responsibilityCentreId: 1 }));
    moneyService.getMoniesByFiscalYear.and.returnValue(of([mockDefaultMoney, mockCustomMoney]));

    fixture = TestBed.createComponent(ConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('initialization', () => {
    it('should load monies on init', fakeAsync(() => {
      tick();
      expect(component.monies.length).toBe(2);
      expect(component.monies[0].code).toBe('AB');
    }));

    it('should set the monies tab as active by default', () => {
      expect(component.activeTab).toBe('monies');
    });
  });

  describe('tab switching', () => {
    it('should switch to general tab', () => {
      component.setActiveTab('general');
      expect(component.activeTab).toBe('general');
    });

    it('should switch back to monies tab', () => {
      component.setActiveTab('general');
      component.setActiveTab('monies');
      expect(component.activeTab).toBe('monies');
    });
  });

  describe('adding money', () => {
    it('should show add form when startAddMoney is called', () => {
      component.startAddMoney();
      expect(component.isAddingMoney).toBe(true);
    });

    it('should hide add form when cancelAddMoney is called', () => {
      component.startAddMoney();
      component.cancelAddMoney();
      expect(component.isAddingMoney).toBe(false);
    });

    it('should reset form when canceling', () => {
      component.startAddMoney();
      component.newMoney = { code: 'WCF', name: 'Test', description: 'Desc' };
      component.cancelAddMoney();
      expect(component.newMoney.code).toBe('');
    });

    it('should create money successfully', fakeAsync(() => {
      const newMoney: Money = {
        ...mockCustomMoney,
        id: 3,
        code: 'WCF',
        name: 'Working Capital Fund',
        capLabel: 'WCF (CAP)',
        omLabel: 'WCF (OM)'
      };
      moneyService.createMoney.and.returnValue(of(newMoney));

      component.rcId = 1;
      component.fyId = 1;
      component.startAddMoney();
      component.newMoney = { code: 'WCF', name: 'Working Capital Fund', description: '' };
      component.saveMoney();
      tick();

      expect(component.monies.length).toBe(3);
      expect(component.isAddingMoney).toBe(false);
    }));

    it('should show error when code is empty', () => {
      component.rcId = 1;
      component.fyId = 1;
      component.startAddMoney();
      component.newMoney = { code: '', name: 'Test' };
      component.saveMoney();

      expect(component.moneyError).toContain('required');
    });
  });

  describe('editing money', () => {
    it('should show edit form when startEditMoney is called', () => {
      component.startEditMoney(mockCustomMoney);
      expect(component.editingMoneyId).toBe(2);
      expect(component.editMoney.code).toBe('OA');
    });

    it('should hide edit form when cancelEditMoney is called', () => {
      component.startEditMoney(mockCustomMoney);
      component.cancelEditMoney();
      expect(component.editingMoneyId).toBeNull();
    });

    it('should update money successfully', fakeAsync(() => {
      const updatedMoney: Money = {
        ...mockCustomMoney,
        name: 'Updated Name'
      };
      moneyService.updateMoney.and.returnValue(of(updatedMoney));

      component.rcId = 1;
      component.fyId = 1;
      component.startEditMoney(mockCustomMoney);
      component.editMoney.name = 'Updated Name';
      component.updateMoney(mockCustomMoney);
      tick();

      const money = component.monies.find(m => m.id === 2);
      expect(money?.name).toBe('Updated Name');
      expect(component.editingMoneyId).toBeNull();
    }));
  });

  describe('deleting money', () => {
    it('should not allow deleting default money', () => {
      component.rcId = 1;
      component.fyId = 1;
      component.deleteMoney(mockDefaultMoney);

      expect(component.moneyError).toContain('default');
      expect(moneyService.deleteMoney).not.toHaveBeenCalled();
    });

    it('should delete custom money successfully', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      moneyService.deleteMoney.and.returnValue(of(void 0));

      component.rcId = 1;
      component.fyId = 1;
      component.deleteMoney(mockCustomMoney);
      tick();

      expect(component.monies.length).toBe(1);
      expect(component.monies[0].code).toBe('AB');
    }));

    it('should not delete when user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.rcId = 1;
      component.fyId = 1;
      component.deleteMoney(mockCustomMoney);

      expect(moneyService.deleteMoney).not.toHaveBeenCalled();
    });
  });

  describe('error handling', () => {
    it('should display error when load fails', fakeAsync(() => {
      moneyService.getMoniesByFiscalYear.and.returnValue(throwError(() => new Error('Network error')));

      component.loadMonies();
      tick();

      expect(component.moneyError).toContain('Network error');
      expect(component.isLoadingMonies).toBe(false);
    }));

    it('should clear error when clearError is called', () => {
      component.moneyError = 'Test error';
      component.clearError();
      expect(component.moneyError).toBeNull();
    });
  });

  describe('trackByMoneyId', () => {
    it('should return money id', () => {
      const result = component.trackByMoneyId(0, mockDefaultMoney);
      expect(result).toBe(1);
    });
  });
});
