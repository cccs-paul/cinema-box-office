/**
 * Spending Item Service Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SpendingItemService, SpendingItemCreateRequest, SpendingItemUpdateRequest } from './spending-item.service';
import { SpendingItem, SpendingMoneyAllocation } from '../models/spending-item.model';

describe('SpendingItemService', () => {
  let service: SpendingItemService;
  let httpMock: HttpTestingController;

  const mockAllocation: SpendingMoneyAllocation = {
    moneyId: 1,
    moneyName: 'A-Base',
    isDefault: true,
    capAmount: 50000,
    omAmount: 0
  };

  const mockSpendingItem: SpendingItem = {
    id: 1,
    name: 'GPU Purchase',
    description: 'Purchase of NVIDIA A100 GPUs',
    vendor: 'NVIDIA',
    referenceNumber: 'PO-001',
    amount: 50000,
    status: 'DRAFT',
    currency: 'CAD',
    exchangeRate: null,
    categoryId: 2,
    categoryName: 'GPUs',
    fiscalYearId: 1,
    fiscalYearName: 'FY 2025-2026',
    responsibilityCentreId: 1,
    responsibilityCentreName: 'Demo RC',
    createdAt: '2026-01-26T12:00:00Z',
    updatedAt: '2026-01-26T12:00:00Z',
    active: true,
    moneyAllocations: [mockAllocation]
  };

  const mockPendingItem: SpendingItem = {
    ...mockSpendingItem,
    id: 2,
    name: 'Software Licenses',
    description: 'Annual software licenses',
    vendor: 'Microsoft',
    referenceNumber: 'INV-002',
    amount: 25000,
    status: 'PENDING',
    categoryId: 4,
    categoryName: 'Software Licenses'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SpendingItemService]
    });

    service = TestBed.inject(SpendingItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getSpendingItemsByFY', () => {
    it('should return all spending items for a fiscal year', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe(items => {
        expect(items.length).toBe(2);
        expect(items[0].name).toBe('GPU Purchase');
        expect(items[1].name).toBe('Software Licenses');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush([mockSpendingItem, mockPendingItem]);
    });

    it('should filter by category when categoryId is provided', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 2;

      service.getSpendingItemsByFY(rcId, fyId, categoryId).subscribe(items => {
        expect(items.length).toBe(1);
        expect(items[0].categoryId).toBe(2);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items?categoryId=${categoryId}`);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('categoryId')).toBe('2');
      req.flush([mockSpendingItem]);
    });

    it('should handle access denied error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getSpendingItem', () => {
    it('should return a specific spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;

      service.getSpendingItem(rcId, fyId, spendingItemId).subscribe(item => {
        expect(item.name).toBe('GPU Purchase');
        expect(item.status).toBe('DRAFT');
        expect(item.moneyAllocations?.length).toBe(1);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockSpendingItem);
    });

    it('should handle not found error', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 999;

      service.getSpendingItem(rcId, fyId, spendingItemId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Spending item not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      req.flush({ message: 'Spending item not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createSpendingItem', () => {
    it('should create a new spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const request: SpendingItemCreateRequest = {
        name: 'GPU Purchase',
        description: 'Purchase of NVIDIA A100 GPUs',
        vendor: 'NVIDIA',
        referenceNumber: 'PO-001',
        categoryId: 2,
        currency: 'CAD',
        moneyAllocations: [{ moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 50000, omAmount: 0 }]
      };

      service.createSpendingItem(rcId, fyId, request).subscribe(item => {
        expect(item.name).toBe('GPU Purchase');
        expect(item.status).toBe('DRAFT');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockSpendingItem);
    });

    it('should handle validation error', () => {
      const rcId = 1;
      const fyId = 1;
      const request: SpendingItemCreateRequest = {
        name: '',
        categoryId: 2
      };

      service.createSpendingItem(rcId, fyId, request).subscribe({
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.flush({ message: 'Name is required' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateSpendingItem', () => {
    it('should update a spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const request: SpendingItemUpdateRequest = {
        name: 'GPU Purchase Updated',
        description: 'Updated description'
      };

      const updatedItem = { ...mockSpendingItem, name: 'GPU Purchase Updated', description: 'Updated description' };

      service.updateSpendingItem(rcId, fyId, spendingItemId, request).subscribe(item => {
        expect(item.name).toBe('GPU Purchase Updated');
        expect(item.description).toBe('Updated description');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(updatedItem);
    });

    it('should handle not found error on update', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 999;
      const request: SpendingItemUpdateRequest = { name: 'Updated' };

      service.updateSpendingItem(rcId, fyId, spendingItemId, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('Spending item not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      req.flush({ message: 'Spending item not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('deleteSpendingItem', () => {
    it('should delete a spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;

      service.deleteSpendingItem(rcId, fyId, spendingItemId).subscribe(() => {
        expect(true).toBeTrue();
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle not found error on delete', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 999;

      service.deleteSpendingItem(rcId, fyId, spendingItemId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Spending item not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}`);
      req.flush({ message: 'Spending item not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('updateStatus', () => {
    it('should update the status of a spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const status = 'PENDING';

      const updatedItem = { ...mockSpendingItem, status: 'PENDING' };

      service.updateStatus(rcId, fyId, spendingItemId, status).subscribe(item => {
        expect(item.status).toBe('PENDING');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/status`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status });
      req.flush(updatedItem);
    });

    it('should handle invalid status error', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const status = 'INVALID';

      service.updateStatus(rcId, fyId, spendingItemId, status).subscribe({
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/status`);
      req.flush({ message: 'Invalid status: INVALID' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('getMoneyAllocations', () => {
    it('should return money allocations for a spending item', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const allocations: SpendingMoneyAllocation[] = [
        mockAllocation,
        { moneyId: 2, moneyName: 'Operating Allotment', isDefault: false, capAmount: 10000, omAmount: 5000 }
      ];

      service.getMoneyAllocations(rcId, fyId, spendingItemId).subscribe(result => {
        expect(result.length).toBe(2);
        expect(result[0].capAmount).toBe(50000);
        expect(result[1].omAmount).toBe(5000);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/allocations`);
      expect(req.request.method).toBe('GET');
      req.flush(allocations);
    });
  });

  describe('updateMoneyAllocations', () => {
    it('should update money allocations', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const allocations: SpendingMoneyAllocation[] = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 30000, omAmount: 10000 }
      ];

      const updatedItem = { ...mockSpendingItem, moneyAllocations: allocations };

      service.updateMoneyAllocations(rcId, fyId, spendingItemId, allocations).subscribe(item => {
        expect(item.moneyAllocations?.length).toBe(1);
        expect(item.moneyAllocations?.[0].capAmount).toBe(30000);
        expect(item.moneyAllocations?.[0].omAmount).toBe(10000);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/allocations`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(allocations);
      req.flush(updatedItem);
    });

    it('should handle validation error for allocations', () => {
      const rcId = 1;
      const fyId = 1;
      const spendingItemId = 1;
      const allocations: SpendingMoneyAllocation[] = [
        { moneyId: 1, moneyName: 'A-Base', isDefault: true, capAmount: 0, omAmount: 0 }
      ];

      service.updateMoneyAllocations(rcId, fyId, spendingItemId, allocations).subscribe({
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/allocations`);
      req.flush({ message: 'At least one money type must have a CAP or OM amount' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('Error Handling', () => {
    it('should handle server error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Internal server error');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.flush({ message: 'Internal server error' }, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should handle unauthorized error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Unauthorized');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle network error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Unable to connect');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.error(new ProgressEvent('error'), { status: 0 });
    });

    it('should handle error with custom message from server', () => {
      const rcId = 1;
      const fyId = 1;

      service.getSpendingItemsByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toBe('Custom error message from server');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items`);
      req.flush({ message: 'Custom error message from server' }, { status: 400, statusText: 'Bad Request' });
    });
  });
});
