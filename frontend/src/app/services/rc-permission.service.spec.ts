/**
 * RC Permission Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import {
  RCPermissionService,
  RCAccess,
  GrantUserAccessRequest,
  GrantGroupAccessRequest,
  UpdatePermissionRequest,
} from './rc-permission.service';

describe('RCPermissionService', () => {
  let service: RCPermissionService;
  let httpMock: HttpTestingController;

  const mockAccess: RCAccess = {
    id: 1,
    rcId: 1,
    rcName: 'Test RC',
    principalIdentifier: 'testuser',
    principalDisplayName: 'Test User',
    principalType: 'USER',
    accessLevel: 'READ_WRITE',
    grantedAt: '2026-01-29T10:00:00',
    grantedBy: 'admin',
  };

  const mockGroupAccess: RCAccess = {
    id: 2,
    rcId: 1,
    rcName: 'Test RC',
    principalIdentifier: 'TestGroup',
    principalDisplayName: 'Test Group',
    principalType: 'GROUP',
    accessLevel: 'READ_ONLY',
    grantedAt: '2026-01-29T10:00:00',
    grantedBy: 'admin',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RCPermissionService],
    });

    service = TestBed.inject(RCPermissionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getPermissionsForRC', () => {
    it('should return permissions for RC', () => {
      const mockPermissions: RCAccess[] = [mockAccess, mockGroupAccess];

      service.getPermissionsForRC(1).subscribe((permissions) => {
        expect(permissions.length).toBe(2);
        expect(permissions[0].principalIdentifier).toBe('testuser');
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockPermissions);
    });

    it('should handle 403 error when not owner', () => {
      service.getPermissionsForRC(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle 404 error when RC not found', () => {
      service.getPermissionsForRC(999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/999');
      req.flush(
        { message: 'RC not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should return empty array when no permissions', () => {
      service.getPermissionsForRC(1).subscribe((permissions) => {
        expect(permissions.length).toBe(0);
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1');
      req.flush([]);
    });
  });

  describe('grantUserAccess', () => {
    it('should grant user access', () => {
      const request: GrantUserAccessRequest = {
        username: 'newuser',
        accessLevel: 'READ_WRITE',
      };

      service.grantUserAccess(1, request).subscribe((access) => {
        expect(access.principalIdentifier).toBe('testuser');
        expect(access.accessLevel).toBe('READ_WRITE');
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/user');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockAccess);
    });

    it('should handle 403 error when not owner', () => {
      const request: GrantUserAccessRequest = {
        username: 'newuser',
        accessLevel: 'READ_WRITE',
      };

      service.grantUserAccess(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/user');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle 400 error when user not found', () => {
      const request: GrantUserAccessRequest = {
        username: 'nonexistent',
        accessLevel: 'READ_WRITE',
      };

      service.grantUserAccess(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/user');
      req.flush(
        { message: 'User not found' },
        { status: 400, statusText: 'Bad Request' }
      );
    });

    it('should handle 409 error when user already has access', () => {
      const request: GrantUserAccessRequest = {
        username: 'existinguser',
        accessLevel: 'READ_WRITE',
      };

      service.grantUserAccess(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/user');
      req.flush(
        { message: 'User already has access' },
        { status: 409, statusText: 'Conflict' }
      );
    });
  });

  describe('grantGroupAccess', () => {
    it('should grant group access', () => {
      const request: GrantGroupAccessRequest = {
        principalIdentifier: 'NewGroup',
        principalDisplayName: 'New Group',
        principalType: 'GROUP',
        accessLevel: 'READ_ONLY',
      };

      service.grantGroupAccess(1, request).subscribe((access) => {
        expect(access.principalType).toBe('GROUP');
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/group');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockGroupAccess);
    });

    it('should grant distribution list access', () => {
      const request: GrantGroupAccessRequest = {
        principalIdentifier: 'team@example.com',
        principalDisplayName: 'Team DL',
        principalType: 'DISTRIBUTION_LIST',
        accessLevel: 'READ_ONLY',
      };

      service.grantGroupAccess(1, request).subscribe((access) => {
        expect(access).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/group');
      expect(req.request.method).toBe('POST');
      req.flush({ ...mockGroupAccess, principalType: 'DISTRIBUTION_LIST' });
    });

    it('should handle 403 error when not owner', () => {
      const request: GrantGroupAccessRequest = {
        principalIdentifier: 'NewGroup',
        principalDisplayName: 'New Group',
        principalType: 'GROUP',
        accessLevel: 'READ_ONLY',
      };

      service.grantGroupAccess(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1/group');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('updatePermission', () => {
    it('should update permission', () => {
      const request: UpdatePermissionRequest = {
        accessLevel: 'READ_ONLY',
      };

      service.updatePermission(1, request).subscribe((access) => {
        expect(access).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush({ ...mockAccess, accessLevel: 'READ_ONLY' });
    });

    it('should handle 403 error when not owner', () => {
      const request: UpdatePermissionRequest = {
        accessLevel: 'READ_ONLY',
      };

      service.updatePermission(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle 404 error when access not found', () => {
      const request: UpdatePermissionRequest = {
        accessLevel: 'READ_ONLY',
      };

      service.updatePermission(999, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/999');
      req.flush(
        { message: 'Access not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 400 error when modifying Demo RC', () => {
      const request: UpdatePermissionRequest = {
        accessLevel: 'READ_ONLY',
      };

      service.updatePermission(1, request).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      req.flush(
        { message: 'Cannot modify Demo RC' },
        { status: 400, statusText: 'Bad Request' }
      );
    });
  });

  describe('revokeAccess', () => {
    it('should revoke access', () => {
      service.revokeAccess(1).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle 403 error when not owner', () => {
      service.revokeAccess(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle 404 error when access not found', () => {
      service.revokeAccess(999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/999');
      req.flush(
        { message: 'Access not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 400 error when revoking owner access', () => {
      service.revokeAccess(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/1');
      req.flush(
        { message: 'Cannot revoke owner access' },
        { status: 400, statusText: 'Bad Request' }
      );
    });
  });

  describe('error handling', () => {
    it('should handle server error', () => {
      service.getPermissionsForRC(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1');
      req.flush(
        { message: 'Server error' },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });

    it('should handle network error', () => {
      service.getPermissionsForRC(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/rc-permissions/rc/1');
      req.error(new ProgressEvent('network error'));
    });
  });
});
