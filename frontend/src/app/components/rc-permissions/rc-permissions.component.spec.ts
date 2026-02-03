/**
 * myRC - RC Permissions Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { RCPermissionsComponent } from './rc-permissions.component';
import { RCPermissionService, RCAccess } from '../../services/rc-permission.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';

describe('RCPermissionsComponent', () => {
  let component: RCPermissionsComponent;
  let fixture: ComponentFixture<RCPermissionsComponent>;
  let permissionService: jasmine.SpyObj<RCPermissionService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let router: jasmine.SpyObj<Router>;
  let translateService: jasmine.SpyObj<TranslateService>;

  let routeParams$: BehaviorSubject<any>;
  let selectedRC$: BehaviorSubject<number | null>;

  const mockRC = {
    id: 1,
    name: 'Test RC',
    description: 'Test Description',
    ownerUsername: 'admin',
    accessLevel: 'OWNER' as const,
    isOwner: true
  };

  const mockPermissions: RCAccess[] = [
    {
      id: 1,
      rcId: 1,
      rcName: 'Test RC',
      principalType: 'USER',
      principalIdentifier: 'admin',
      principalDisplayName: 'Admin User',
      accessLevel: 'OWNER',
      grantedAt: '2026-01-01T00:00:00Z',
      grantedBy: null
    },
    {
      id: 2,
      rcId: 1,
      rcName: 'Test RC',
      principalType: 'USER',
      principalIdentifier: 'user1',
      principalDisplayName: 'User One',
      accessLevel: 'READ_WRITE',
      grantedAt: '2026-01-01T00:00:00Z',
      grantedBy: 'admin'
    }
  ];

  beforeEach(async () => {
    routeParams$ = new BehaviorSubject<any>({ rcId: '1' });
    selectedRC$ = new BehaviorSubject<number | null>(1);

    permissionService = jasmine.createSpyObj('RCPermissionService', 
      ['getPermissionsForRC', 'grantUserAccess', 'grantGroupAccess', 'updatePermission', 'revokeAccess']);
    permissionService.getPermissionsForRC.and.returnValue(of(mockPermissions));
    permissionService.grantUserAccess.and.returnValue(of(mockPermissions[1]));
    permissionService.updatePermission.and.returnValue(of(mockPermissions[1]));
    permissionService.revokeAccess.and.returnValue(of(undefined));

    rcService = jasmine.createSpyObj('ResponsibilityCentreService', 
      ['getResponsibilityCentre'], {
      selectedRC$: selectedRC$.asObservable()
    });
    rcService.getResponsibilityCentre.and.returnValue(of(mockRC as any));

    router = jasmine.createSpyObj('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));

    translateService = jasmine.createSpyObj('TranslateService', ['instant']);
    translateService.instant.and.callFake((key: string) => key);

    await TestBed.configureTestingModule({
      imports: [RCPermissionsComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(RCPermissionService, { useValue: permissionService })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(Router, { useValue: router })
    .overrideProvider(TranslateService, { useValue: translateService })
    .overrideProvider(ActivatedRoute, { 
      useValue: { params: routeParams$.asObservable() }
    })
    .compileComponents();

    fixture = TestBed.createComponent(RCPermissionsComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.isLoading).toBeTrue();
      expect(component.showGrantForm).toBeFalse();
      expect(component.grantFormType).toBe('USER');
      expect(component.newAccessLevel).toBe('READ_WRITE');
    });
  });

  describe('Route Parameter Handling', () => {
    it('should load RC from route params', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(component.rcId).toBe(1);
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
    }));

    it('should load RC from selected RC when no route param', fakeAsync(() => {
      routeParams$.next({});
      fixture.detectChanges();
      tick();
      
      expect(rcService.getResponsibilityCentre).toHaveBeenCalledWith(1);
    }));

    it('should navigate to selection if no RC available', fakeAsync(() => {
      routeParams$.next({});
      selectedRC$.next(null);
      fixture.detectChanges();
      tick();
      
      expect(router.navigate).toHaveBeenCalledWith(['/rc-selection']);
    }));
  });

  describe('Loading RC', () => {
    it('should load RC details', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(component.rc).toEqual(mockRC as any);
    }));

    it('should handle RC load error', fakeAsync(() => {
      rcService.getResponsibilityCentre.and.returnValue(throwError(() => new Error('RC error')));
      fixture.detectChanges();
      tick();
      
      expect(component.errorMessage).toBe('rcPermissions.loadRCError');
    }));

    it('should set error message if user is not owner', fakeAsync(() => {
      const nonOwnerRC = { ...mockRC, isOwner: false };
      rcService.getResponsibilityCentre.and.returnValue(of(nonOwnerRC as any));
      fixture.detectChanges();
      tick();
      
      expect(component.errorMessage).toBe('rcPermissions.noPermissionToManage');
    }));
  });

  describe('Loading Permissions', () => {
    it('should load permissions', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      expect(component.permissions).toEqual(mockPermissions);
      expect(component.isLoading).toBeFalse();
    }));

    it('should handle permissions load error', fakeAsync(() => {
      permissionService.getPermissionsForRC.and.returnValue(
        throwError(() => ({ message: 'Permission error' }))
      );
      fixture.detectChanges();
      tick();
      
      expect(component.errorMessage).toBe('Permission error');
      expect(component.isLoading).toBeFalse();
    }));
  });

  describe('Grant Form', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should open grant form with default type', () => {
      component.openGrantForm();
      expect(component.showGrantForm).toBeTrue();
      expect(component.grantFormType).toBe('USER');
      expect(component.newAccessLevel).toBe('READ_WRITE');
    });

    it('should open grant form with specified type', () => {
      component.openGrantForm('GROUP');
      expect(component.showGrantForm).toBeTrue();
      expect(component.grantFormType).toBe('GROUP');
    });

    it('should close grant form', () => {
      component.openGrantForm();
      component.closeGrantForm();
      expect(component.showGrantForm).toBeFalse();
      expect(component.newPrincipalIdentifier).toBe('');
    });
  });

  describe('Edit Permission', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should have editingPermissionId as null initially', () => {
      expect(component.editingPermissionId).toBeNull();
    });

    it('should have default edit access level', () => {
      expect(component.editAccessLevel).toBe('READ_WRITE');
    });
  });

  describe('Delete Confirmation', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should have deleteConfirmId as null initially', () => {
      expect(component.deleteConfirmId).toBeNull();
    });

    it('should have isDeleting as false initially', () => {
      expect(component.isDeleting).toBeFalse();
    });
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    }));
  });
});
