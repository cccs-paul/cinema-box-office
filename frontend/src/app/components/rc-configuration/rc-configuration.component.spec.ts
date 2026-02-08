/**
 * myRC - RC Configuration Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-08
 * Version: 1.0.0
 *
 * Description:
 * Tests for the RC Configuration page component, which provides a tabbed
 * interface for RC-level settings including Permissions.
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { BehaviorSubject, of, throwError, Subject } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { RCConfigurationComponent } from './rc-configuration.component';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { RCPermissionService } from '../../services/rc-permission.service';
import { DirectorySearchService } from '../../services/directory-search.service';

describe('RCConfigurationComponent', () => {
  let component: RCConfigurationComponent;
  let fixture: ComponentFixture<RCConfigurationComponent>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let router: jasmine.SpyObj<Router>;
  let routeParams$: BehaviorSubject<any>;

  const mockRC = {
    id: 1,
    name: 'Test RC',
    description: 'Test Description',
    ownerUsername: 'admin',
    accessLevel: 'OWNER' as const,
    isOwner: true
  };

  beforeEach(async () => {
    routeParams$ = new BehaviorSubject<any>({ rcId: '1' });

    const selectedRC$ = new BehaviorSubject<number | null>(1);
    rcService = jasmine.createSpyObj('ResponsibilityCentreService',
      ['getResponsibilityCentre'], {
      selectedRC$: selectedRC$.asObservable()
    });
    rcService.getResponsibilityCentre.and.returnValue(of(mockRC as any));

    router = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
    router.navigate.and.returnValue(Promise.resolve(true));

    const permissionService = jasmine.createSpyObj('RCPermissionService',
      ['getPermissionsForRC', 'grantUserAccess', 'grantGroupAccess',
       'updatePermission', 'revokeAccess', 'relinquishOwnership',
       'getPrincipalTypeIcon', 'getAccessLevelIcon']);
    permissionService.getPermissionsForRC.and.returnValue(of([]));
    permissionService.getPrincipalTypeIcon.and.returnValue('👤');
    permissionService.getAccessLevelIcon.and.returnValue('📝');

    const directorySearchService = jasmine.createSpyObj('DirectorySearchService',
      ['searchUsers', 'searchGroups', 'searchDistributionLists', 'searchAllGroups']);
    directorySearchService.searchUsers.and.returnValue(of([]));
    directorySearchService.searchGroups.and.returnValue(of([]));
    directorySearchService.searchDistributionLists.and.returnValue(of([]));
    directorySearchService.searchAllGroups.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [RCConfigurationComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(Router, { useValue: router })
    .overrideProvider(ActivatedRoute, {
      useValue: { params: routeParams$.asObservable() }
    })
    .overrideProvider(RCPermissionService, { useValue: permissionService })
    .overrideProvider(DirectorySearchService, { useValue: directorySearchService })
    .compileComponents();

    fixture = TestBed.createComponent(RCConfigurationComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.rcId).toBeNull();
      expect(component.rc).toBeNull();
      expect(component.activeTab).toBe('permissions');
      expect(component.isLoading).toBeTrue();
      expect(component.errorMessage).toBeNull();
    });
  });

  describe('Route Parameter Handling', () => {
    it('should parse rcId from route params', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(component.rcId).toBe(1);
    }));

    it('should load RC when rcId is available', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
      expect(component.rc).toEqual(mockRC as any);
    }));

    it('should navigate to rc-selection when no rcId in route', fakeAsync(() => {
      routeParams$.next({});
      fixture.detectChanges();
      tick();

      expect(router.navigate).toHaveBeenCalledWith(['/rc-selection']);
    }));

    it('should handle route param changes', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      const newRC = { ...mockRC, id: 2, name: 'Other RC' };
      rcService.getResponsibilityCentre.and.returnValue(of(newRC as any));

      routeParams$.next({ rcId: '2' });
      tick();

      expect(component.rcId).toBe(2);
      expect(component.rc).toEqual(newRC as any);
    }));
  });

  describe('Loading RC', () => {
    it('should set isLoading to true during load', fakeAsync(() => {
      const delayedRC$ = new Subject<any>();
      rcService.getResponsibilityCentre.and.returnValue(delayedRC$.asObservable());
      fixture.detectChanges();

      expect(component.isLoading).toBeTrue();

      delayedRC$.next(mockRC);
      delayedRC$.complete();
      tick();

      expect(component.isLoading).toBeFalse();
    }));

    it('should set isLoading to false after successful load', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(component.isLoading).toBeFalse();
    }));

    it('should set rc after successful load', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(component.rc).toEqual(mockRC as any);
    }));

    it('should set error message on load failure', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(
        throwError(() => new Error('Load failed'))
      );
      fixture.detectChanges();
      tick();

      expect(component.errorMessage).toBe('rcConfiguration.loadError');
      expect(component.isLoading).toBeFalse();
    }));

    it('should not load when rcId is null', () => {
      component.rcId = null;
      component.loadRC();

      expect(rcService.getResponsibilityCentre).not.toHaveBeenCalled();
    });
  });

  describe('Tab Management', () => {
    it('should default to permissions tab', () => {
      expect(component.activeTab).toBe('permissions');
    });

    it('should switch tabs via setActiveTab', () => {
      component.setActiveTab('permissions');
      expect(component.activeTab).toBe('permissions');
    });
  });

  describe('Navigation', () => {
    it('should navigate back to rc-selection', () => {
      component.goBack();
      expect(router.navigate).toHaveBeenCalledWith(['/rc-selection']);
    });
  });

  describe('Template Rendering', () => {
    it('should display RC name as subtitle when loaded', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const subtitle = fixture.nativeElement.querySelector('.subtitle');
      expect(subtitle).toBeTruthy();
      expect(subtitle.textContent).toContain('Test RC');
    }));

    it('should display error message when present', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(
        throwError(() => new Error('Load failed'))
      );
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const alert = fixture.nativeElement.querySelector('.alert-error');
      expect(alert).toBeTruthy();
    }));

    it('should show loading state while loading', fakeAsync(() => {
      const delayedRC$ = new Subject<any>();
      rcService.getResponsibilityCentre.and.returnValue(delayedRC$.asObservable());
      fixture.detectChanges();

      const loading = fixture.nativeElement.querySelector('.loading-state');
      expect(loading).toBeTruthy();

      delayedRC$.next(mockRC);
      delayedRC$.complete();
      tick();
    }));

    it('should show tab content when loaded', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const tabContent = fixture.nativeElement.querySelector('.tab-content');
      expect(tabContent).toBeTruthy();
    }));

    it('should show permissions tab button', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const tabBtn = fixture.nativeElement.querySelector('.tab-btn');
      expect(tabBtn).toBeTruthy();
    }));

    it('should have active class on permissions tab by default', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const activeTab = fixture.nativeElement.querySelector('.tab-btn.active');
      expect(activeTab).toBeTruthy();
    }));

    it('should embed rc-permissions component', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      const permissionsComponent = fixture.nativeElement.querySelector('app-rc-permissions');
      expect(permissionsComponent).toBeTruthy();
    }));

    it('should have back button', () => {
      fixture.detectChanges();
      const backBtn = fixture.nativeElement.querySelector('.btn-back');
      expect(backBtn).toBeTruthy();
    });
  });

  describe('Cleanup', () => {
    it('should complete destroy$ on component destroy', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      const spy = spyOn(component['destroy$'], 'complete').and.callThrough();
      component.ngOnDestroy();

      expect(spy).toHaveBeenCalled();
    }));
  });
});
