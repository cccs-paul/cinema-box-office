/**
 * Fiscal Year Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FiscalYearService } from './fiscal-year.service';
import { FiscalYear, FiscalYearCreateRequest } from '../models/fiscal-year.model';

describe('FiscalYearService', () => {
  let service: FiscalYearService;
  let httpMock: HttpTestingController;

  const mockFiscalYear: FiscalYear = {
    id: 1,
    name: 'FY 2025-2026',
    description: 'Test Fiscal Year',
    active: true,
    responsibilityCentreId: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FiscalYearService]
    });

    service = TestBed.inject(FiscalYearService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getFiscalYearsByRC', () => {
    it('should return fiscal years for RC', () => {
      const mockFiscalYears: FiscalYear[] = [mockFiscalYear];

      service.getFiscalYearsByRC(1).subscribe(years => {
        expect(years.length).toBe(1);
        expect(years[0].name).toBe('FY 2025-2026');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years');
      expect(req.request.method).toBe('GET');
      req.flush(mockFiscalYears);
    });

    it('should handle 403 error', () => {
      service.getFiscalYearsByRC(1).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years');
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getFiscalYear', () => {
    it('should return specific fiscal year', () => {
      service.getFiscalYear(1, 1).subscribe(fy => {
        expect(fy.name).toBe('FY 2025-2026');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockFiscalYear);
    });

    it('should handle 404 error', () => {
      service.getFiscalYear(1, 999).subscribe({
        error: (error) => {
          expect(error.message).toContain('not found');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/999');
      req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createFiscalYear', () => {
    it('should create new fiscal year', () => {
      const createRequest: FiscalYearCreateRequest = {
        name: 'FY 2025-2026',
        description: 'Test Fiscal Year'
      };

      service.createFiscalYear(1, createRequest).subscribe(fy => {
        expect(fy.name).toBe('FY 2025-2026');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockFiscalYear);
    });

    it('should handle 400 error for invalid data', () => {
      const createRequest: FiscalYearCreateRequest = {
        name: '',
        description: ''
      };

      service.createFiscalYear(1, createRequest).subscribe({
        error: (error) => {
          expect(error.message).toContain('Invalid request');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years');
      req.flush({ message: 'Invalid' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateFiscalYear', () => {
    it('should update fiscal year', () => {
      const updateRequest = { name: 'FY 2025-2026 Updated' };

      service.updateFiscalYear(1, 1, updateRequest).subscribe(fy => {
        expect(fy.name).toBe('FY 2025-2026');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1');
      expect(req.request.method).toBe('PUT');
      req.flush(mockFiscalYear);
    });
  });

  describe('deleteFiscalYear', () => {
    it('should delete fiscal year', () => {
      service.deleteFiscalYear(1, 1).subscribe(result => {
        // void response
        expect(result).toBeNull();
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
