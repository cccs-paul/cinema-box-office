/**
 * Money Service Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MoneyService } from './money.service';
import { Money, MoneyCreateRequest, MoneyUpdateRequest, MoneyReorderRequest } from '../models/money.model';

describe('MoneyService', () => {
  let service: MoneyService;
  let httpMock: HttpTestingController;

  const mockMoney: Money = {
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
    omLabel: 'AB (O&M)'
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
    omLabel: 'OA (O&M)'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MoneyService]
    });

    service = TestBed.inject(MoneyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getMoniesByFiscalYear', () => {
    it('should return monies for a fiscal year', () => {
      const rcId = 1;
      const fyId = 1;

      service.getMoniesByFiscalYear(rcId, fyId).subscribe(monies => {
        expect(monies.length).toBe(2);
        expect(monies[0].code).toBe('AB');
        expect(monies[1].code).toBe('OA');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies`);
      expect(req.request.method).toBe('GET');
      req.flush([mockMoney, mockCustomMoney]);
    });

    it('should handle errors', () => {
      const rcId = 1;
      const fyId = 1;

      service.getMoniesByFiscalYear(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies`);
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getMoney', () => {
    it('should return a specific money', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 1;

      service.getMoney(rcId, fyId, moneyId).subscribe(money => {
        expect(money.code).toBe('AB');
        expect(money.isDefault).toBe(true);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockMoney);
    });

    it('should handle 404 errors', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 99;

      service.getMoney(rcId, fyId, moneyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      req.flush({}, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createMoney', () => {
    it('should create a new money', () => {
      const rcId = 1;
      const fyId = 1;
      const request: MoneyCreateRequest = {
        code: 'WCF',
        name: 'Working Capital Fund',
        description: 'Test'
      };

      const createdMoney: Money = {
        ...mockCustomMoney,
        id: 3,
        code: 'WCF',
        name: 'Working Capital Fund',
        capLabel: 'WCF (CAP)',
        omLabel: 'WCF (O&M)'
      };

      service.createMoney(rcId, fyId, request).subscribe(money => {
        expect(money.code).toBe('WCF');
        expect(money.name).toBe('Working Capital Fund');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(createdMoney);
    });

    it('should handle conflict errors', () => {
      const rcId = 1;
      const fyId = 1;
      const request: MoneyCreateRequest = {
        code: 'AB',
        name: 'Duplicate'
      };

      service.createMoney(rcId, fyId, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('already exists');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies`);
      req.flush({}, { status: 409, statusText: 'Conflict' });
    });
  });

  describe('updateMoney', () => {
    it('should update a money', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 2;
      const request: MoneyUpdateRequest = {
        name: 'Updated Name',
        description: 'Updated description'
      };

      const updatedMoney: Money = {
        ...mockCustomMoney,
        name: 'Updated Name',
        description: 'Updated description'
      };

      service.updateMoney(rcId, fyId, moneyId, request).subscribe(money => {
        expect(money.name).toBe('Updated Name');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(updatedMoney);
    });
  });

  describe('deleteMoney', () => {
    it('should delete a money', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 2;

      service.deleteMoney(rcId, fyId, moneyId).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle forbidden errors when deleting default', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 1;

      service.deleteMoney(rcId, fyId, moneyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('permission');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      req.flush({}, { status: 403, statusText: 'Forbidden' });
    });

    it('should handle conflict errors when deleting money in use', () => {
      const rcId = 1;
      const fyId = 1;
      const moneyId = 2;

      service.deleteMoney(rcId, fyId, moneyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('in use');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/${moneyId}`);
      req.flush(
        { message: 'Cannot delete money type "OA" because it is in use with non-zero funding or spending allocations' },
        { status: 409, statusText: 'Conflict' }
      );
    });
  });

  describe('reorderMonies', () => {
    it('should reorder monies', () => {
      const rcId = 1;
      const fyId = 1;
      const request: MoneyReorderRequest = {
        moneyIds: [2, 1]
      };

      service.reorderMonies(rcId, fyId, request).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/monies/reorder`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(null);
    });
  });
});
