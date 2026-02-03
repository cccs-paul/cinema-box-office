/**
 * Procurement Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import {
  ProcurementService,
  ProcurementItemCreateRequest,
  QuoteCreateRequest,
} from './procurement.service';
import {
  ProcurementItem,
  ProcurementQuote,
  ProcurementQuoteFile,
  ProcurementItemStatus,
} from '../models/procurement.model';

describe('ProcurementService', () => {
  let service: ProcurementService;
  let httpMock: HttpTestingController;

  const mockProcurementItem: ProcurementItem = {
    id: 1,
    purchaseRequisition: 'PR-001',
    purchaseOrder: 'PO-001',
    name: 'Test Procurement Item',
    description: 'Test Description',
    currentStatus: 'DRAFT',
    fiscalYearId: 1,
    finalPriceCurrency: 'CAD',
    quotes: [],
  };

  const mockQuote: ProcurementQuote = {
    id: 1,
    vendorName: 'Test Vendor',
    vendorContact: 'contact@vendor.com',
    amount: 10000,
    currency: 'CAD',
    procurementItemId: 1,
    status: 'PENDING',
    selected: false,
    files: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProcurementService],
    });

    service = TestBed.inject(ProcurementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getProcurementItems', () => {
    it('should return procurement items', () => {
      const mockItems: ProcurementItem[] = [mockProcurementItem];

      service.getProcurementItems(1, 1).subscribe((items) => {
        expect(items.length).toBe(1);
        expect(items[0].purchaseRequisition).toBe('PR-001');
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockItems);
    });

    it('should filter by status', () => {
      service.getProcurementItems(1, 1, 'NOT_STARTED').subscribe();

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items?status=NOT_STARTED'
      );
      expect(req.request.method).toBe('GET');
      req.flush([mockProcurementItem]);
    });

    it('should filter by search term', () => {
      service.getProcurementItems(1, 1, undefined, 'PR-001').subscribe();

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items?search=PR-001'
      );
      expect(req.request.method).toBe('GET');
      req.flush([mockProcurementItem]);
    });

    it('should handle 403 error', () => {
      service.getProcurementItems(1, 1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      req.flush(
        { message: 'Access denied' },
        { status: 403, statusText: 'Forbidden' }
      );
    });
  });

  describe('getProcurementItem', () => {
    it('should return specific procurement item', () => {
      service.getProcurementItem(1, 1, 1).subscribe((item) => {
        expect(item.name).toBe('Test Procurement Item');
      });

      // Without includeQuotes=true, no query param is added
      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockProcurementItem);
    });

    it('should return item with quotes when requested', () => {
      service.getProcurementItem(1, 1, 1, true).subscribe((item) => {
        expect(item).toBeTruthy();
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1?includeQuotes=true'
      );
      expect(req.request.method).toBe('GET');
      req.flush({ ...mockProcurementItem, quotes: [mockQuote] });
    });

    it('should handle 404 error', () => {
      service.getProcurementItem(1, 1, 999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      // Without includeQuotes=true, no query param is added
      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/999'
      );
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('createProcurementItem', () => {
    it('should create new procurement item', () => {
      const createRequest: ProcurementItemCreateRequest = {
        purchaseRequisition: 'PR-002',
        name: 'New Procurement Item',
        description: 'New Description',
      };

      service.createProcurementItem(1, 1, createRequest).subscribe((item) => {
        expect(item.purchaseRequisition).toBe('PR-001');
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockProcurementItem);
    });

    it('should handle 400 error for missing PR', () => {
      const createRequest: ProcurementItemCreateRequest = {
        purchaseRequisition: '',
        name: 'New Item',
      };

      service.createProcurementItem(1, 1, createRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      req.flush(
        { message: 'PR is required' },
        { status: 400, statusText: 'Bad Request' }
      );
    });

    it('should handle 409 error for duplicate PR', () => {
      const createRequest: ProcurementItemCreateRequest = {
        purchaseRequisition: 'PR-001',
        name: 'Duplicate Item',
      };

      service.createProcurementItem(1, 1, createRequest).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      req.flush(
        { message: 'PR already exists' },
        { status: 409, statusText: 'Conflict' }
      );
    });
  });

  describe('updateProcurementItem', () => {
    it('should update existing procurement item', () => {
      const updateRequest: Partial<ProcurementItemCreateRequest> = {
        name: 'Updated Name',
        description: 'Updated Description',
      };

      service.updateProcurementItem(1, 1, 1, updateRequest).subscribe((item) => {
        expect(item).toBeTruthy();
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(mockProcurementItem);
    });

    it('should handle 404 error when item not found', () => {
      service.updateProcurementItem(1, 1, 999, { name: 'Updated' }).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/999'
      );
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('deleteProcurementItem', () => {
    it('should delete procurement item', () => {
      service.deleteProcurementItem(1, 1, 1).subscribe(() => {
        // Success
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1'
      );
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle 404 error when item not found', () => {
      service.deleteProcurementItem(1, 1, 999).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/999'
      );
      req.flush(
        { message: 'Not found' },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('updateProcurementItemStatus', () => {
    it('should update item status', () => {
      service.updateProcurementItemStatus(1, 1, 1, 'PENDING_QUOTES').subscribe((item) => {
        expect(item).toBeTruthy();
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/status'
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'PENDING_QUOTES' });
      req.flush(mockProcurementItem);
    });

    it('should handle 400 error for invalid status', () => {
      // Cast to bypass TypeScript checking for error handling test
      service.updateProcurementItemStatus(1, 1, 1, 'INVALID' as ProcurementItemStatus).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/status'
      );
      req.flush(
        { message: 'Invalid status' },
        { status: 400, statusText: 'Bad Request' }
      );
    });
  });

  describe('Quote Operations', () => {
    describe('getQuotes', () => {
      it('should return quotes for procurement item', () => {
        service.getQuotes(1, 1, 1).subscribe((quotes) => {
          expect(quotes.length).toBe(1);
          expect(quotes[0].vendorName).toBe('Test Vendor');
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/quotes'
        );
        expect(req.request.method).toBe('GET');
        req.flush([mockQuote]);
      });
    });

    describe('createQuote', () => {
      it('should create new quote', () => {
        const quoteRequest: QuoteCreateRequest = {
          vendorName: 'New Vendor',
          amount: 15000,
          currency: 'CAD',
        };

        service.createQuote(1, 1, 1, quoteRequest).subscribe((quote) => {
          expect(quote.vendorName).toBe('Test Vendor');
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/quotes'
        );
        expect(req.request.method).toBe('POST');
        req.flush(mockQuote);
      });

      it('should handle 400 error for missing vendor name', () => {
        const quoteRequest: QuoteCreateRequest = {
          vendorName: '',
        };

        service.createQuote(1, 1, 1, quoteRequest).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/quotes'
        );
        req.flush(
          { message: 'Vendor name is required' },
          { status: 400, statusText: 'Bad Request' }
        );
      });
    });

    describe('deleteQuote', () => {
      it('should delete quote', () => {
        service.deleteQuote(1, 1, 1, 1).subscribe(() => {
          // Success
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/quotes/1'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
      });
    });
  });

  describe('error handling', () => {
    it('should handle server error', () => {
      service.getProcurementItems(1, 1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      req.flush(
        { message: 'Server error' },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });

    it('should handle network error', () => {
      service.getProcurementItems(1, 1).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(
        '/api/responsibility-centres/1/fiscal-years/1/procurement-items'
      );
      req.error(new ProgressEvent('network error'));
    });
  });
});
