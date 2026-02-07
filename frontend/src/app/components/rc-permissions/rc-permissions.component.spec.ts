/**
 * myRC - RC Permissions Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { BehaviorSubject, of, throwError, Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { RCPermissionsComponent } from './rc-permissions.component';
import { RCPermissionService, RCAccess } from '../../services/rc-permission.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { DirectorySearchService, DirectorySearchResult } from '../../services/directory-search.service';

describe('RCPermissionsComponent', () => {
  let component: RCPermissionsComponent;
  let fixture: ComponentFixture<RCPermissionsComponent>;
  let permissionService: jasmine.SpyObj<RCPermissionService>;
  let rcService: jasmine.SpyObj<ResponsibilityCentreService>;
  let directorySearchService: jasmine.SpyObj<DirectorySearchService>;
  let router: jasmine.SpyObj<Router>;

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
      ['getPermissionsForRC', 'grantUserAccess', 'grantGroupAccess', 'updatePermission', 'revokeAccess', 'getPrincipalTypeIcon', 'getAccessLevelIcon']);
    permissionService.getPermissionsForRC.and.returnValue(of(mockPermissions));
    permissionService.grantUserAccess.and.returnValue(of(mockPermissions[1]));
    permissionService.updatePermission.and.returnValue(of(mockPermissions[1]));
    permissionService.revokeAccess.and.returnValue(of(undefined));
    permissionService.getPrincipalTypeIcon.and.callFake((type: string) => type === 'USER' ? '👤' : '👥');
    permissionService.getAccessLevelIcon.and.callFake((level: string) => '📝');

    directorySearchService = jasmine.createSpyObj('DirectorySearchService',
      ['searchUsers', 'searchGroups', 'searchDistributionLists']);
    directorySearchService.searchUsers.and.returnValue(of([]));
    directorySearchService.searchGroups.and.returnValue(of([]));
    directorySearchService.searchDistributionLists.and.returnValue(of([]));

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

    await TestBed.configureTestingModule({
      imports: [RCPermissionsComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(RCPermissionService, { useValue: permissionService })
    .overrideProvider(ResponsibilityCentreService, { useValue: rcService })
    .overrideProvider(DirectorySearchService, { useValue: directorySearchService })
    .overrideProvider(Router, { useValue: router })
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

    it('should unsubscribe from search subscription', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      const subscription = component['searchSubscription'];
      expect(subscription).toBeTruthy();

      component.ngOnDestroy();
      expect(subscription!.closed).toBeTrue();
    }));
  });

  describe('Autocomplete', () => {
    const mockUserSuggestions: DirectorySearchResult[] = [
      { identifier: 'jdoe', displayName: 'John Doe', source: 'APP', email: 'jdoe@test.com' },
      { identifier: 'jsmith', displayName: 'Jane Smith', source: 'LDAP', email: 'jsmith@test.com' }
    ];

    const mockGroupSuggestions: DirectorySearchResult[] = [
      { identifier: 'cn=finance,ou=groups,dc=example,dc=com', displayName: 'Finance Team', source: 'LDAP', email: null }
    ];

    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should initialize autocomplete properties', () => {
      expect(component.suggestions).toEqual([]);
      expect(component.showSuggestions).toBeFalse();
      expect(component.isSearching).toBeFalse();
      expect(component.activeSuggestionIndex).toBe(-1);
    });

    it('should search users on input when grant form type is USER', fakeAsync(() => {
      directorySearchService.searchUsers.and.returnValue(of(mockUserSuggestions));
      component.openGrantForm('USER');

      component.newPrincipalIdentifier = 'j';
      component.onIdentifierInput();
      tick(300);

      expect(directorySearchService.searchUsers).toHaveBeenCalledWith('j');
      expect(component.suggestions).toEqual(mockUserSuggestions);
      expect(component.showSuggestions).toBeTrue();
      expect(component.isSearching).toBeFalse();
    }));

    it('should search groups on input when grant form type is GROUP', fakeAsync(() => {
      directorySearchService.searchGroups.and.returnValue(of(mockGroupSuggestions));
      component.openGrantForm('GROUP');

      component.newPrincipalIdentifier = 'fin';
      component.onIdentifierInput();
      tick(300);

      expect(directorySearchService.searchGroups).toHaveBeenCalledWith('fin');
      expect(component.suggestions).toEqual(mockGroupSuggestions);
      expect(component.showSuggestions).toBeTrue();
    }));

    it('should search distribution lists on input when grant form type is DISTRIBUTION_LIST', fakeAsync(() => {
      const mockDistListSuggestions: DirectorySearchResult[] = [
        { identifier: 'cn=all-staff,ou=distribution-lists,dc=example,dc=com', displayName: 'all-staff - All staff', source: 'LDAP', email: 'all-staff@example.com' }
      ];
      directorySearchService.searchDistributionLists.and.returnValue(of(mockDistListSuggestions));
      component.openGrantForm('DISTRIBUTION_LIST');

      component.newPrincipalIdentifier = 'staff';
      component.onIdentifierInput();
      tick(300);

      expect(directorySearchService.searchDistributionLists).toHaveBeenCalledWith('staff');
      expect(component.suggestions).toEqual(mockDistListSuggestions);
      expect(component.showSuggestions).toBeTrue();
    }));

    it('should debounce search input', fakeAsync(() => {
      directorySearchService.searchUsers.and.returnValue(of(mockUserSuggestions));
      component.openGrantForm('USER');

      component.newPrincipalIdentifier = 'a';
      component.onIdentifierInput();
      tick(100);
      component.newPrincipalIdentifier = 'ab';
      component.onIdentifierInput();
      tick(100);
      component.newPrincipalIdentifier = 'abc';
      component.onIdentifierInput();
      tick(300);

      expect(directorySearchService.searchUsers).toHaveBeenCalledTimes(1);
      expect(directorySearchService.searchUsers).toHaveBeenCalledWith('abc');
    }));

    it('should select a suggestion and update identifier', () => {
      component.openGrantForm('USER');
      component.suggestions = mockUserSuggestions;
      component.showSuggestions = true;

      component.selectSuggestion(mockUserSuggestions[0]);

      expect(component.newPrincipalIdentifier).toBe('jdoe');
      expect(component.showSuggestions).toBeFalse();
      expect(component.suggestions).toEqual([]);
    });

    it('should set display name for group suggestions', () => {
      component.openGrantForm('GROUP');
      component.suggestions = mockGroupSuggestions;
      component.showSuggestions = true;

      component.selectSuggestion(mockGroupSuggestions[0]);

      expect(component.newPrincipalIdentifier).toBe('cn=finance,ou=groups,dc=example,dc=com');
      expect(component.newPrincipalDisplayName).toBe('Finance Team');
      expect(component.showSuggestions).toBeFalse();
    });

    it('should set display name for distribution list suggestions', () => {
      const mockDistListSuggestion: DirectorySearchResult = {
        identifier: 'cn=all-staff,ou=distribution-lists,dc=example,dc=com',
        displayName: 'all-staff - All staff',
        source: 'LDAP',
        email: 'all-staff@example.com'
      };
      component.openGrantForm('DISTRIBUTION_LIST');
      component.suggestions = [mockDistListSuggestion];
      component.showSuggestions = true;

      component.selectSuggestion(mockDistListSuggestion);

      expect(component.newPrincipalIdentifier).toBe('cn=all-staff,ou=distribution-lists,dc=example,dc=com');
      expect(component.newPrincipalDisplayName).toBe('all-staff - All staff');
      expect(component.showSuggestions).toBeFalse();
    });

    it('should not set display name for user suggestions', () => {
      component.openGrantForm('USER');
      component.newPrincipalDisplayName = '';

      component.selectSuggestion(mockUserSuggestions[0]);

      expect(component.newPrincipalDisplayName).toBe('');
    });

    it('should close suggestions on closeSuggestions call', () => {
      component.suggestions = mockUserSuggestions;
      component.showSuggestions = true;
      component.isSearching = true;
      component.activeSuggestionIndex = 1;

      component.closeSuggestions();

      expect(component.suggestions).toEqual([]);
      expect(component.showSuggestions).toBeFalse();
      expect(component.isSearching).toBeFalse();
      expect(component.activeSuggestionIndex).toBe(-1);
    });

    it('should close suggestions when opening grant form', () => {
      component.suggestions = mockUserSuggestions;
      component.showSuggestions = true;

      component.openGrantForm('USER');

      expect(component.showSuggestions).toBeFalse();
      expect(component.suggestions).toEqual([]);
    });

    it('should close suggestions when closing grant form', () => {
      component.openGrantForm('USER');
      component.suggestions = mockUserSuggestions;
      component.showSuggestions = true;

      component.closeGrantForm();

      expect(component.showSuggestions).toBeFalse();
      expect(component.suggestions).toEqual([]);
    });

    describe('Keyboard Navigation', () => {
      beforeEach(() => {
        component.openGrantForm('USER');
        component.suggestions = mockUserSuggestions;
        component.showSuggestions = true;
        component.activeSuggestionIndex = -1;
      });

      it('should navigate down on ArrowDown', () => {
        const event = new KeyboardEvent('keydown', { key: 'ArrowDown' });
        spyOn(event, 'preventDefault');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.activeSuggestionIndex).toBe(0);
      });

      it('should not go beyond last suggestion on ArrowDown', () => {
        component.activeSuggestionIndex = mockUserSuggestions.length - 1;

        const event = new KeyboardEvent('keydown', { key: 'ArrowDown' });
        component.onSuggestionKeydown(event);

        expect(component.activeSuggestionIndex).toBe(mockUserSuggestions.length - 1);
      });

      it('should navigate up on ArrowUp', () => {
        component.activeSuggestionIndex = 1;

        const event = new KeyboardEvent('keydown', { key: 'ArrowUp' });
        spyOn(event, 'preventDefault');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.activeSuggestionIndex).toBe(0);
      });

      it('should not go below -1 on ArrowUp', () => {
        component.activeSuggestionIndex = 0;

        const event = new KeyboardEvent('keydown', { key: 'ArrowUp' });
        component.onSuggestionKeydown(event);

        expect(component.activeSuggestionIndex).toBe(-1);
      });

      it('should select suggestion on Enter', () => {
        component.activeSuggestionIndex = 0;

        const event = new KeyboardEvent('keydown', { key: 'Enter' });
        spyOn(event, 'preventDefault');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.newPrincipalIdentifier).toBe('jdoe');
        expect(component.showSuggestions).toBeFalse();
      });

      it('should not select suggestion on Enter when no suggestion is active', () => {
        component.activeSuggestionIndex = -1;

        const event = new KeyboardEvent('keydown', { key: 'Enter' });
        component.onSuggestionKeydown(event);

        expect(component.showSuggestions).toBeTrue();
      });

      it('should close suggestions on Escape', () => {
        const event = new KeyboardEvent('keydown', { key: 'Escape' });
        spyOn(event, 'preventDefault');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.showSuggestions).toBeFalse();
      });

      it('should trigger browse-all on Enter when suggestions are not shown', () => {
        component.showSuggestions = false;
        component.suggestions = [];
        component.newPrincipalIdentifier = '';

        const event = new KeyboardEvent('keydown', { key: 'Enter' });
        spyOn(event, 'preventDefault');
        spyOn(component, 'triggerBrowseAll');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.triggerBrowseAll).toHaveBeenCalled();
      });

      it('should trigger browse-all on ArrowDown when suggestions are not shown', () => {
        component.showSuggestions = false;
        component.suggestions = [];

        const event = new KeyboardEvent('keydown', { key: 'ArrowDown' });
        spyOn(event, 'preventDefault');
        spyOn(component, 'triggerBrowseAll');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).toHaveBeenCalled();
        expect(component.triggerBrowseAll).toHaveBeenCalled();
      });

      it('should do nothing for other keys if suggestions are not shown', () => {
        component.showSuggestions = false;

        const event = new KeyboardEvent('keydown', { key: 'Escape' });
        spyOn(event, 'preventDefault');

        component.onSuggestionKeydown(event);

        expect(event.preventDefault).not.toHaveBeenCalled();
      });
    });

    describe('Source Label', () => {
      it('should return translated App label', () => {
        const label = component.getSuggestionSourceLabel('APP');
        expect(label).toBeTruthy();
      });

      it('should return translated LDAP label', () => {
        const label = component.getSuggestionSourceLabel('LDAP');
        expect(label).toBeTruthy();
      });

      it('should return raw source for unknown type', () => {
        const label = component.getSuggestionSourceLabel('UNKNOWN');
        expect(label).toBe('UNKNOWN');
      });
    });

    it('should handle empty search results', fakeAsync(() => {
      directorySearchService.searchUsers.and.returnValue(of([]));
      component.openGrantForm('USER');

      component.newPrincipalIdentifier = 'xyz';
      component.onIdentifierInput();
      tick(300);

      expect(component.suggestions).toEqual([]);
      expect(component.showSuggestions).toBeFalse();
    }));

    it('should reset activeSuggestionIndex on new results', fakeAsync(() => {
      directorySearchService.searchUsers.and.returnValue(of(mockUserSuggestions));
      component.openGrantForm('USER');
      component.activeSuggestionIndex = 1;

      component.newPrincipalIdentifier = 'j';
      component.onIdentifierInput();
      tick(300);

      expect(component.activeSuggestionIndex).toBe(-1);
    }));

    it('should show all entries when triggerBrowseAll is called with empty field', fakeAsync(() => {
      directorySearchService.searchUsers.and.returnValue(of(mockUserSuggestions));
      component.openGrantForm('USER');
      component.newPrincipalIdentifier = '';

      component.triggerBrowseAll();
      tick(300);

      expect(directorySearchService.searchUsers).toHaveBeenCalledWith('');
      expect(component.suggestions.length).toBe(2);
      expect(component.showSuggestions).toBeTrue();
    }));

    it('should show all groups when triggerBrowseAll is called for GROUP form', fakeAsync(() => {
      const mockGroups: DirectorySearchResult[] = [
        { identifier: 'cn=Finance,ou=groups,dc=example,dc=com', displayName: 'Finance', source: 'LDAP', email: null }
      ];
      directorySearchService.searchGroups.and.returnValue(of(mockGroups));
      component.openGrantForm('GROUP');
      component.newPrincipalIdentifier = '';

      component.triggerBrowseAll();
      tick(300);

      expect(directorySearchService.searchGroups).toHaveBeenCalledWith('');
      expect(component.suggestions.length).toBe(1);
      expect(component.showSuggestions).toBeTrue();
    }));

    it('should show all distribution lists when triggerBrowseAll is called for DISTRIBUTION_LIST form', fakeAsync(() => {
      const mockDLists: DirectorySearchResult[] = [
        { identifier: 'cn=all-staff,ou=distribution-lists,dc=example,dc=com', displayName: 'all-staff', source: 'LDAP', email: 'all-staff@example.com' }
      ];
      directorySearchService.searchDistributionLists.and.returnValue(of(mockDLists));
      component.openGrantForm('DISTRIBUTION_LIST');
      component.newPrincipalIdentifier = '';

      component.triggerBrowseAll();
      tick(300);

      expect(directorySearchService.searchDistributionLists).toHaveBeenCalledWith('');
      expect(component.suggestions.length).toBe(1);
      expect(component.showSuggestions).toBeTrue();
    }));
  });
});
