/**
 * Responsibility Centre Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ResponsibilityCentreService } from './responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../models/responsibility-centre.model';

describe('ResponsibilityCentreService', () => {
  let service: ResponsibilityCentreService;
  let httpMock: HttpTestingController;

  const mockRC: ResponsibilityCentreDTO = {
    id: 1,
    name: 'Test RC',
    description: 'Test Description',
    ownerUsername: 'testuser',
    accessLevel: 'READ_WRITE',
    isOwner: true,
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
  };

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ResponsibilityCentreService],
    });

    service = TestBed.inject(ResponsibilityCentreService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('getAllResponsibilityCentres', () => {
    it('should return all responsibility centres', () => {
      const mockRCs: ResponsibilityCentreDTO[] = [mockRC];

      service.getAllResponsibilityCentres().subscribe((rcs) => {
        expect(rcs.length).toBe(1);
        expect(rcs[0].name).toBe('Test RC');
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      expect(req.request.method).toBe('GET');
      req.flush(mockRCs);
    });

    it('should handle empty response', () => {
      service.getAllResponsibilityCentres().subscribe((rcs) => {
        expect(rcs.length).toBe(0);
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.flush([]);
    });

    it('should handle 403 error', () => {
      service.getAllResponsibilityCentres().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('getResponsibilityCentre', () => {
    it('should return specific responsibility centre', () => {
      service.getResponsibilityCentre(1).subscribe((rc) => {
        expect(rc.name).toBe('Test RC');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockRC);
    });

    it('should handle 404 error', () => {
      service.getResponsibilityCentre(999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('createResponsibilityCentre', () => {
    it('should create new responsibility centre', () => {
      service.createResponsibilityCentre('New RC', 'New Description').subscribe((rc) => {
        expect(rc.name).toBe('Test RC');
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        name: 'New RC',
        description: 'New Description',
      });
      req.flush(mockRC);
    });

    it('should handle 400 error for empty name', () => {
      service.createResponsibilityCentre('', 'Description').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.flush(
        { message: 'Name is required' },
        { status: 400, statusText: 'Bad Request' }
      );
    });

    it('should handle 409 error for duplicate name', () => {
      service.createResponsibilityCentre('Existing RC', 'Description').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.flush(
        { message: 'RC already exists' },
        { status: 409, statusText: 'Conflict' }
      );
    });
  });

  describe('updateResponsibilityCentre', () => {
    it('should update responsibility centre', () => {
      service.updateResponsibilityCentre(1, 'Updated RC', 'Updated Description').subscribe((rc) => {
        expect(rc).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({
        name: 'Updated RC',
        description: 'Updated Description',
      });
      req.flush(mockRC);
    });

    it('should handle 404 error when RC not found', () => {
      service.updateResponsibilityCentre(999, 'Updated', 'Description').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 403 error when not owner', () => {
      service.updateResponsibilityCentre(1, 'Updated', 'Description').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('deleteResponsibilityCentre', () => {
    it('should delete responsibility centre', () => {
      service.deleteResponsibilityCentre(1).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle 404 error when RC not found', () => {
      service.deleteResponsibilityCentre(999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 403 error when not owner', () => {
      service.deleteResponsibilityCentre(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('cloneResponsibilityCentre', () => {
    it('should clone responsibility centre', () => {
      service.cloneResponsibilityCentre(1, 'Cloned RC').subscribe((rc) => {
        expect(rc).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/clone');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ newName: 'Cloned RC' });
      req.flush({ ...mockRC, id: 2, name: 'Cloned RC' });
    });

    it('should handle 404 error when source RC not found', () => {
      service.cloneResponsibilityCentre(999, 'Cloned RC').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/999/clone');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('grantAccess', () => {
    it('should grant access to user', () => {
      service.grantAccess(1, 'newuser', 'READ_WRITE').subscribe((result) => {
        expect(result).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/access/grant');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        username: 'newuser',
        accessLevel: 'READ_WRITE',
      });
      req.flush({ success: true });
    });

    it('should handle 403 error when not owner', () => {
      service.grantAccess(1, 'newuser', 'READ_WRITE').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/access/grant');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('revokeAccess', () => {
    it('should revoke access from user', () => {
      service.revokeAccess(1, 'user').subscribe((result) => {
        expect(result).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/access/revoke');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ username: 'user' });
      req.flush({ success: true });
    });

    it('should handle 403 error when not owner', () => {
      service.revokeAccess(1, 'user').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/access/revoke');
      req.flush(
        { message: 'Not an owner' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('getResponsibilityCentreAccess', () => {
    it('should return access list for RC', () => {
      const mockAccess = [
        { username: 'user1', accessLevel: 'READ_WRITE' },
        { username: 'user2', accessLevel: 'READ_ONLY' },
      ];

      service.getResponsibilityCentreAccess(1).subscribe((access) => {
        expect(access.length).toBe(2);
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/access');
      expect(req.request.method).toBe('GET');
      req.flush(mockAccess);
    });
  });

  describe('selectedRC$', () => {
    it('should emit selected RC', () => {
      let emittedValue: number | null;

      service.selectedRC$.subscribe((value) => {
        emittedValue = value;
      });

      service.setSelectedRC(1);
      expect(emittedValue!).toBe(1);
    });

    it('should persist selected RC to localStorage', () => {
      service.setSelectedRC(1);
      expect(localStorage.getItem('selectedRC')).toBe('1');
    });

    it('should clear selected RC', () => {
      service.setSelectedRC(1);
      service.clearSelection();

      expect(localStorage.getItem('selectedRC')).toBeNull();
    });
  });

  describe('selectedFY$', () => {
    it('should emit selected FY', () => {
      let emittedValue: number | null;

      service.selectedFY$.subscribe((value) => {
        emittedValue = value;
      });

      service.setSelectedFY(1);
      expect(emittedValue!).toBe(1);
    });

    it('should persist selected FY to localStorage', () => {
      service.setSelectedFY(1);
      expect(localStorage.getItem('selectedFY')).toBe('1');
    });

    it('should clear selected FY', () => {
      service.setSelectedFY(1);
      service.clearSelection();

      expect(localStorage.getItem('selectedFY')).toBeNull();
    });
  });

  describe('error handling', () => {
    it('should handle server error', () => {
      service.getAllResponsibilityCentres().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.flush(
        { message: 'Server error' },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });

    it('should handle network error', () => {
      service.getAllResponsibilityCentres().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/responsibility-centres');
      req.error(new ProgressEvent('network error'));
    });
  });
});
