/**
 * myRC - Sidebar Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Directive, Input } from '@angular/core';
import { BehaviorSubject, of, throwError, Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { SidebarComponent } from './sidebar.component';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';

/**
 * Stub directive for routerLink to avoid router dependency.
 */
@Directive({
  standalone: true,
  selector: '[routerLink]'
})
class RouterLinkStubDirective {
  @Input() routerLink: string | unknown[] = '';
}

/**
 * Stub directive for routerLinkActive to avoid router dependency.
 */
@Directive({
  standalone: true,
  selector: '[routerLinkActive]'
})
class RouterLinkActiveStubDirective {
  @Input() routerLinkActive: string | string[] = '';
  @Input() routerLinkActiveOptions: { exact: boolean } = { exact: false };
}

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let fyService: jasmine.SpyObj<FiscalYearService>;
  let router: jasmine.SpyObj<Router>;
  
  let selectedRCSubject: BehaviorSubject<number | null>;
  let selectedFYSubject: BehaviorSubject<number | null>;

  const mockRC: ResponsibilityCentreDTO = {
    id: 1,
    name: 'Test RC',
    description: 'Test Description',
    ownerUsername: 'admin',
    accessLevel: 'OWNER',
    isOwner: true,
    canEdit: true,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  };

  const mockFY: FiscalYear = {
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

  beforeEach(async () => {
    selectedRCSubject = new BehaviorSubject<number | null>(null);
    selectedFYSubject = new BehaviorSubject<number | null>(null);

    rcService = jasmine.createSpyObj('ResponsibilityCentreService', ['getResponsibilityCentre'], {
      selectedRC$: selectedRCSubject.asObservable(),
      selectedFY$: selectedFYSubject.asObservable(),
      rcUpdated$: new Subject<number>().asObservable()
    });
    rcService.getResponsibilityCentre.and.returnValue(of(mockRC));

    fyService = jasmine.createSpyObj('FiscalYearService', ['getFiscalYear']);
    fyService.getFiscalYear.and.returnValue(of(mockFY));

    router = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [SidebarComponent, TranslateModule.forRoot()]
    })
    .overrideComponent(SidebarComponent, {
      remove: { imports: [RouterLink, RouterLinkActive] },
      add: { imports: [RouterLinkStubDirective, RouterLinkActiveStubDirective] }
    })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(FiscalYearService, { useValue: fyService })
    .overrideProvider(Router, { useValue: router })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should have correct menu items', () => {
      expect(component.menuItems.length).toBe(7);
      expect(component.menuItems[0].labelKey).toBe('sidebar.funding');
      expect(component.menuItems[1].labelKey).toBe('sidebar.procurement');
      expect(component.menuItems[2].labelKey).toBe('sidebar.spending');
      expect(component.menuItems[3].labelKey).toBe('sidebar.training');
      expect(component.menuItems[4].labelKey).toBe('sidebar.travel');
      expect(component.menuItems[5].labelKey).toBe('sidebar.insights');
      expect(component.menuItems[6].labelKey).toBe('sidebar.summary');
    });

    it('should have correct bottom menu items', () => {
      expect(component.bottomMenuItems.length).toBe(2);
      expect(component.bottomMenuItems[0].labelKey).toBe('sidebar.configuration');
      expect(component.bottomMenuItems[1].labelKey).toBe('sidebar.developerTools');
    });

    it('should have correct routes for menu items', () => {
      expect(component.menuItems[0].route).toBe('/app/dashboard');
      expect(component.menuItems[1].route).toBe('/app/procurement');
      expect(component.menuItems[2].route).toBe('/app/spending');
      expect(component.menuItems[3].route).toBe('/app/training');
      expect(component.menuItems[4].route).toBe('/app/travel');
      expect(component.menuItems[5].route).toBe('/app/insights');
      expect(component.menuItems[6].route).toBe('/app/summary');
    });

    it('should have icons for all menu items', () => {
      component.menuItems.forEach(item => {
        expect(item.icon).toBeTruthy();
      });
      component.bottomMenuItems.forEach(item => {
        expect(item.icon).toBeTruthy();
      });
    });

    it('should have null selected RC and FY initially', () => {
      expect(component.selectedRC).toBeNull();
      expect(component.selectedFY).toBeNull();
    });
  });

  describe('RC subscription', () => {
    it('should load RC when selectedRC changes', fakeAsync(() => {
      selectedRCSubject.next(1);
      tick();
      
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(component.selectedRC).toEqual(mockRC);
    }));

    it('should not load RC when selectedRC is null', fakeAsync(() => {
      selectedRCSubject.next(null);
      tick();
      
      // Initial call count should be 0
      expect(rcService.getResponsibilityCentre).not.toHaveBeenCalled();
    }));

    it('should handle RC load error gracefully', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(throwError(() => new Error('Load error')));
      
      selectedRCSubject.next(1);
      tick();
      
      expect(component.selectedRC).toBeNull();
    }));
  });

  describe('FY subscription', () => {
    it('should load FY when both RC and FY are selected', fakeAsync(() => {
      selectedRCSubject.next(1);
      selectedFYSubject.next(1);
      tick();
      
      expect(fyService.getFiscalYear).toHaveBeenCalledWith(1, 1);
      expect(component.selectedFY).toEqual(mockFY);
    }));

    it('should not load FY when RC is null', fakeAsync(() => {
      selectedRCSubject.next(null);
      selectedFYSubject.next(1);
      tick();
      
      expect(fyService.getFiscalYear).not.toHaveBeenCalled();
    }));

    it('should not load FY when FY is null', fakeAsync(() => {
      selectedRCSubject.next(1);
      selectedFYSubject.next(null);
      tick();
      
      expect(fyService.getFiscalYear).not.toHaveBeenCalled();
    }));

    it('should handle FY load error gracefully', fakeAsync(() => {
      fyService.getFiscalYear.and.returnValue(throwError(() => new Error('Load error')));
      
      selectedRCSubject.next(1);
      selectedFYSubject.next(1);
      tick();
      
      expect(component.selectedFY).toBeNull();
    }));
  });

  describe('Navigation', () => {
    it('should navigate to RC selection', () => {
      component.navigateToRCSelection();
      expect(router.navigate).toHaveBeenCalledWith(['/rc-selection']);
    });

    it('should navigate to configuration', () => {
      component.navigateToConfiguration();
      expect(router.navigate).toHaveBeenCalledWith(['/app/configuration']);
    });
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', () => {
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });

  describe('Feature-based menu visibility', () => {
    it('should show all 7 menu items when no RC is loaded', () => {
      component.selectedRC = null;
      expect(component.menuItems.length).toBe(7);
    });

    it('should show training and travel when RC has features enabled', () => {
      component.selectedRC = { ...mockRC, trainingEnabled: true, travelEnabled: true } as any;
      expect(component.menuItems.length).toBe(7);
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.training')).toBeTruthy();
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.travel')).toBeTruthy();
    });

    it('should hide training when RC has trainingEnabled=false', () => {
      component.selectedRC = { ...mockRC, trainingEnabled: false, travelEnabled: true } as any;
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.training')).toBeFalsy();
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.travel')).toBeTruthy();
    });

    it('should hide travel when RC has travelEnabled=false', () => {
      component.selectedRC = { ...mockRC, trainingEnabled: true, travelEnabled: false } as any;
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.training')).toBeTruthy();
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.travel')).toBeFalsy();
    });

    it('should hide both training and travel when both disabled', () => {
      component.selectedRC = { ...mockRC, trainingEnabled: false, travelEnabled: false } as any;
      expect(component.menuItems.length).toBe(5);
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.training')).toBeFalsy();
      expect(component.menuItems.find(i => i.labelKey === 'sidebar.travel')).toBeFalsy();
    });
  });
});
