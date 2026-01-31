/**
 * Funding Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { FundingItemService } from './funding-item.service';
import {
  FundingItem,
  FundingItemCreateRequest,
  FundingItemUpdateRequest,
} from '../models/funding-item.model';

describe('FundingItemService', () => {
  let service: FundingItemService;
  let httpMock: HttpTestingController;

  const mockFundingItem: FundingItem = {
    id: 1,
    name: 'Test Funding Item',
    description: 'Test Description',
    fiscalYearId: 1,
    source: 'BUSINESS_PLAN',
    comments: null,
    currency: 'CAD',
    exchangeRate: 1,
    categoryId: 1,
    categoryName: 'Test Category',
    moneyAllocations: [],
    totalCap: 10000,
    totalOm: 5000,
    active: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FundingItemService],
    });

    service = TestBed.inject(FundingItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getFundingItemsByFY', () => {
    it('should return funding items for fiscal year', () => {
      const mockItems: FundingItem[] = [mockFundingItem];

      service.getFundingItemsByFY(1).subscribe((items) => {
        expect(items.length).toBe(1);
        expect(items[0].name).toBe('Test Funding Item');
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      expect(req.request.method).toBe('GET');
      req.flush(mockItems);
    });

    it('should handle 403 error', () => {
      service.getFundingItemsByFY(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle empty response', () => {
      service.getFundingItemsByFY(1).subscribe((items) => {
        expect(items.length).toBe(0);
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.flush([]);
    });
  });

  describe('getFundingItem', () => {
    it('should return specific funding item', () => {
      service.getFundingItem(1, 1).subscribe((item) => {
        expect(item.name).toBe('Test Funding Item');
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockFundingItem);
    });

    it('should handle 404 error', () => {
      service.getFundingItem(1, 999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('createFundingItem', () => {
    it('should create new funding item', () => {
      const createRequest: FundingItemCreateRequest = {
        name: 'New Funding Item',
        description: 'New Description',
        source: 'BUSINESS_PLAN',
        currency: 'CAD',
        categoryId: 1,
        moneyAllocations: [],
      };

      service.createFundingItem(1, createRequest).subscribe((item) => {
        expect(item.name).toBe('Test Funding Item');
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockFundingItem);
    });

    it('should handle 400 error for invalid request', () => {
      const createRequest: FundingItemCreateRequest = {
        name: '',
        source: 'BUSINESS_PLAN',
        currency: 'CAD',
        moneyAllocations: [],
      };

      service.createFundingItem(1, createRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.flush(
        { message: 'Name is required' },
        { status: 400, statusText: 'Bad Request' }
      );
    });

    it('should handle 403 error when no write access', () => {
      const createRequest: FundingItemCreateRequest = {
        name: 'New Item',
        source: 'BUSINESS_PLAN',
        currency: 'CAD',
        moneyAllocations: [],
      };

      service.createFundingItem(1, createRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('updateFundingItem', () => {
    it('should update existing funding item', () => {
      const updateRequest: FundingItemUpdateRequest = {
        name: 'Updated Funding Item',
        description: 'Updated Description',
      };

      service.updateFundingItem(1, 1, updateRequest).subscribe((item) => {
        expect(item).toBeTruthy();
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(mockFundingItem);
    });

    it('should handle 404 error when item not found', () => {
      const updateRequest: FundingItemUpdateRequest = {
        name: 'Updated Name',
      };

      service.updateFundingItem(1, 999, updateRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 403 error when no write access', () => {
      const updateRequest: FundingItemUpdateRequest = {
        name: 'Updated Name',
      };

      service.updateFundingItem(1, 1, updateRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/1');
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('deleteFundingItem', () => {
    it('should delete funding item', () => {
      service.deleteFundingItem(1, 1).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle 404 error when item not found', () => {
      service.deleteFundingItem(1, 999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/999');
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 403 error when no write access', () => {
      service.deleteFundingItem(1, 1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items/1');
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('error handling', () => {
    it('should handle server error', () => {
      service.getFundingItemsByFY(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.flush(
        { message: 'Server error' },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });

    it('should handle network error', () => {
      service.getFundingItemsByFY(1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne('/api/fiscal-years/1/funding-items');
      req.error(new ProgressEvent('network error'));
    });
  });
});
