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
  ProcurementEventFile,
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

  describe('Event File Operations', () => {
    const mockEventFile: ProcurementEventFile = {
      id: 1,
      fileName: 'test-document.pdf',
      contentType: 'application/pdf',
      fileSize: 1048576,
      formattedFileSize: '1.00 MB',
      description: 'Test file description',
      eventId: 1,
      createdAt: '2026-01-15T10:30:00Z',
      updatedAt: '2026-01-15T10:30:00Z'
    };

    describe('uploadEventFile', () => {
      it('should upload file to event', () => {
        const file = new File(['test content'], 'test-file.pdf', { type: 'application/pdf' });
        const description = 'Test upload description';

        service.uploadEventFile(1, 1, 1, 1, file, description).subscribe((result) => {
          expect(result.id).toBe(1);
          expect(result.fileName).toBe('test-document.pdf');
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files'
        );
        expect(req.request.method).toBe('POST');
        expect(req.request.body instanceof FormData).toBe(true);
        req.flush(mockEventFile);
      });

      it('should upload file without description', () => {
        const file = new File(['test content'], 'test-file.pdf', { type: 'application/pdf' });

        service.uploadEventFile(1, 1, 1, 1, file).subscribe((result) => {
          expect(result.id).toBe(1);
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files'
        );
        expect(req.request.method).toBe('POST');
        req.flush(mockEventFile);
      });

      it('should handle 413 error for file too large', () => {
        const file = new File(['large content'], 'large-file.pdf', { type: 'application/pdf' });

        service.uploadEventFile(1, 1, 1, 1, file).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files'
        );
        req.flush(
          { message: 'File too large' },
          { status: 413, statusText: 'Payload Too Large' }
        );
      });

      it('should handle 404 error when event not found', () => {
        const file = new File(['test content'], 'test-file.pdf', { type: 'application/pdf' });

        service.uploadEventFile(1, 1, 1, 999, file).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/999/files'
        );
        req.flush(
          { message: 'Event not found' },
          { status: 404, statusText: 'Not Found' }
        );
      });
    });

    describe('getEventFiles', () => {
      it('should return files for event', () => {
        const mockFiles: ProcurementEventFile[] = [mockEventFile];

        service.getEventFiles(1, 1, 1, 1).subscribe((files) => {
          expect(files.length).toBe(1);
          expect(files[0].fileName).toBe('test-document.pdf');
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files'
        );
        expect(req.request.method).toBe('GET');
        req.flush(mockFiles);
      });

      it('should return empty array when no files', () => {
        service.getEventFiles(1, 1, 1, 1).subscribe((files) => {
          expect(files.length).toBe(0);
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files'
        );
        req.flush([]);
      });

      it('should handle 404 error when event not found', () => {
        service.getEventFiles(1, 1, 1, 999).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/999/files'
        );
        req.flush(
          { message: 'Event not found' },
          { status: 404, statusText: 'Not Found' }
        );
      });
    });

    describe('downloadEventFile', () => {
      it('should download file as blob', () => {
        const mockBlob = new Blob(['test content'], { type: 'application/pdf' });

        service.downloadEventFile(1, 1, 1, 1, 1).subscribe((blob) => {
          expect(blob.size).toBeGreaterThan(0);
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1'
        );
        expect(req.request.method).toBe('GET');
        expect(req.request.responseType).toBe('blob');
        req.flush(mockBlob);
      });

      it('should handle 404 error when file not found', () => {
        service.downloadEventFile(1, 1, 1, 1, 999).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/999'
        );
        // For blob response type, use error() instead of flush() for error responses
        req.error(new ProgressEvent('error'), { status: 404, statusText: 'Not Found' });
      });
    });

    describe('getEventFileDownloadUrl', () => {
      it('should return correct download URL', () => {
        const url = service.getEventFileDownloadUrl(1, 2, 3, 4, 5);
        expect(url).toBe('/api/responsibility-centres/1/fiscal-years/2/procurement-items/3/events/4/files/5');
      });
    });

    describe('getEventFileMetadata', () => {
      it('should return file metadata', () => {
        service.getEventFileMetadata(1, 1, 1, 1, 1).subscribe((file) => {
          expect(file.id).toBe(1);
          expect(file.fileName).toBe('test-document.pdf');
          expect(file.contentType).toBe('application/pdf');
          expect(file.fileSize).toBe(1048576);
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1/metadata'
        );
        expect(req.request.method).toBe('GET');
        req.flush(mockEventFile);
      });

      it('should handle 404 error when file not found', () => {
        service.getEventFileMetadata(1, 1, 1, 1, 999).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/999/metadata'
        );
        req.flush(
          { message: 'File not found' },
          { status: 404, statusText: 'Not Found' }
        );
      });
    });

    describe('updateEventFileDescription', () => {
      it('should update file description', () => {
        const newDescription = 'Updated description';

        service.updateEventFileDescription(1, 1, 1, 1, 1, newDescription).subscribe((file) => {
          expect(file.id).toBe(1);
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1'
        );
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual({ description: newDescription });
        req.flush({ ...mockEventFile, description: newDescription });
      });

      it('should handle 404 error when file not found', () => {
        service.updateEventFileDescription(1, 1, 1, 1, 999, 'New desc').subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/999'
        );
        req.flush(
          { message: 'File not found' },
          { status: 404, statusText: 'Not Found' }
        );
      });

      it('should handle 403 error when access denied', () => {
        service.updateEventFileDescription(1, 1, 1, 1, 1, 'New desc').subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1'
        );
        req.flush(
          { message: 'Access denied' },
          { status: 403, statusText: 'Forbidden' }
        );
      });
    });

    describe('deleteEventFile', () => {
      it('should delete file', () => {
        service.deleteEventFile(1, 1, 1, 1, 1).subscribe(() => {
          // Success - void response
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
      });

      it('should handle 404 error when file not found', () => {
        service.deleteEventFile(1, 1, 1, 1, 999).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/999'
        );
        req.flush(
          { message: 'File not found' },
          { status: 404, statusText: 'Not Found' }
        );
      });

      it('should handle 403 error when access denied', () => {
        service.deleteEventFile(1, 1, 1, 1, 1).subscribe({
          error: (error) => {
            expect(error).toBeTruthy();
          },
        });

        const req = httpMock.expectOne(
          '/api/responsibility-centres/1/fiscal-years/1/procurement-items/1/events/1/files/1'
        );
        req.flush(
          { message: 'Access denied' },
          { status: 403, statusText: 'Forbidden' }
        );
      });
    });
  });
});
