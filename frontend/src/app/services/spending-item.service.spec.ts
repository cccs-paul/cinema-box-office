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
import { SpendingItem, SpendingMoneyAllocation, SpendingInvoice, SpendingInvoiceFile } from '../models/spending-item.model';

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
    ecoAmount: null,
    status: 'PLANNING',
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
    status: 'COMMITTED',
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
        expect(item.status).toBe('PLANNING');
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
        expect(item.status).toBe('PLANNING');
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

  // ==========================
  // Invoice Methods Tests
  // ==========================

  describe('Invoice CRUD Methods', () => {
    const rcId = 1;
    const fyId = 2;
    const spendingItemId = 3;
    const invoiceId = 10;
    const baseInvoiceUrl = `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/invoices`;

    const mockInvoice: SpendingInvoice = {
      id: invoiceId,
      spendingItemId: spendingItemId,
      spendingItemName: 'GPU Purchase',
      dateReceived: '2026-01-15',
      dateProcessed: null,
      comments: 'Initial invoice',
      amount: 5000,
      currency: 'CAD',
      exchangeRate: null,
      amountCad: 5000,
      active: true,
      files: [],
      fileCount: 0
    };

    it('should get all invoices for a spending item', () => {
      service.getInvoices(rcId, fyId, spendingItemId).subscribe(invoices => {
        expect(invoices.length).toBe(1);
        expect(invoices[0].id).toBe(invoiceId);
      });

      const req = httpMock.expectOne(baseInvoiceUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush([mockInvoice]);
    });

    it('should get a specific invoice by ID', () => {
      service.getInvoice(rcId, fyId, spendingItemId, invoiceId).subscribe(invoice => {
        expect(invoice.id).toBe(invoiceId);
        expect(invoice.amount).toBe(5000);
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockInvoice);
    });

    it('should create a new invoice', () => {
      const newInvoice: Partial<SpendingInvoice> = {
        amount: 3000,
        currency: 'CAD',
        comments: 'New invoice'
      };

      service.createInvoice(rcId, fyId, spendingItemId, newInvoice).subscribe(invoice => {
        expect(invoice.id).toBe(invoiceId);
      });

      const req = httpMock.expectOne(baseInvoiceUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newInvoice);
      req.flush(mockInvoice);
    });

    it('should update an existing invoice', () => {
      const updates: Partial<SpendingInvoice> = { comments: 'Updated' };
      const updatedInvoice = { ...mockInvoice, comments: 'Updated' };

      service.updateInvoice(rcId, fyId, spendingItemId, invoiceId, updates).subscribe(invoice => {
        expect(invoice.comments).toBe('Updated');
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}`);
      expect(req.request.method).toBe('PUT');
      req.flush(updatedInvoice);
    });

    it('should delete an invoice', () => {
      service.deleteInvoice(rcId, fyId, spendingItemId, invoiceId).subscribe(() => {
        expect(true).toBeTrue();
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('Invoice File Methods', () => {
    const rcId = 1;
    const fyId = 2;
    const spendingItemId = 3;
    const invoiceId = 10;
    const fileId = 20;
    const baseInvoiceUrl = `/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/invoices`;

    const mockFile: SpendingInvoiceFile = {
      id: fileId,
      fileName: 'receipt.pdf',
      contentType: 'application/pdf',
      fileSize: 12345,
      formattedFileSize: '12 KB',
      description: 'Receipt scan',
      invoiceId: invoiceId,
      active: true
    };

    it('should upload a file to an invoice', () => {
      const file = new File(['test content'], 'receipt.pdf', { type: 'application/pdf' });

      service.uploadInvoiceFile(rcId, fyId, spendingItemId, invoiceId, file, 'Receipt scan').subscribe(result => {
        expect(result.id).toBe(fileId);
        expect(result.fileName).toBe('receipt.pdf');
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}/files`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body instanceof FormData).toBeTrue();
      req.flush(mockFile);
    });

    it('should upload a file without description', () => {
      const file = new File(['test content'], 'receipt.pdf', { type: 'application/pdf' });

      service.uploadInvoiceFile(rcId, fyId, spendingItemId, invoiceId, file).subscribe(result => {
        expect(result.id).toBe(fileId);
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}/files`);
      expect(req.request.method).toBe('POST');
      req.flush(mockFile);
    });

    it('should delete an invoice file', () => {
      service.deleteInvoiceFile(rcId, fyId, spendingItemId, invoiceId, fileId).subscribe(() => {
        expect(true).toBeTrue();
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}/files/${fileId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should replace an invoice file', () => {
      const file = new File(['new content'], 'updated-receipt.pdf', { type: 'application/pdf' });
      const replacedFile = { ...mockFile, fileName: 'updated-receipt.pdf' };

      service.replaceInvoiceFile(rcId, fyId, spendingItemId, invoiceId, fileId, file, 'Updated receipt').subscribe(result => {
        expect(result.fileName).toBe('updated-receipt.pdf');
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}/files/${fileId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body instanceof FormData).toBeTrue();
      req.flush(replacedFile);
    });

    it('should replace an invoice file without description', () => {
      const file = new File(['new content'], 'updated-receipt.pdf', { type: 'application/pdf' });

      service.replaceInvoiceFile(rcId, fyId, spendingItemId, invoiceId, fileId, file).subscribe(result => {
        expect(result.id).toBe(fileId);
      });

      const req = httpMock.expectOne(`${baseInvoiceUrl}/${invoiceId}/files/${fileId}`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockFile);
    });
  });

  describe('Invoice File URL Methods', () => {
    const rcId = 1;
    const fyId = 2;
    const spendingItemId = 3;
    const invoiceId = 10;
    const fileId = 20;

    it('should generate correct download URL without double /api prefix', () => {
      const url = service.getInvoiceFileDownloadUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url).toBe(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/invoices/${invoiceId}/files/${fileId}/download`);
    });

    it('should generate correct view URL without double /api prefix', () => {
      const url = service.getInvoiceFileViewUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url).toBe(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/spending-items/${spendingItemId}/invoices/${invoiceId}/files/${fileId}/view`);
    });

    it('should not contain double /api in download URL', () => {
      const url = service.getInvoiceFileDownloadUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url).not.toContain('/api/api/');
      expect(url.indexOf('/api/')).toBe(0);
    });

    it('should not contain double /api in view URL', () => {
      const url = service.getInvoiceFileViewUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url).not.toContain('/api/api/');
      expect(url.indexOf('/api/')).toBe(0);
    });

    it('should generate download URL that starts with /api/', () => {
      const url = service.getInvoiceFileDownloadUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url.startsWith('/api/')).toBeTrue();
    });

    it('should generate view URL that starts with /api/', () => {
      const url = service.getInvoiceFileViewUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url.startsWith('/api/')).toBeTrue();
    });

    it('should generate download URL ending with /download', () => {
      const url = service.getInvoiceFileDownloadUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url.endsWith('/download')).toBeTrue();
    });

    it('should generate view URL ending with /view', () => {
      const url = service.getInvoiceFileViewUrl(rcId, fyId, spendingItemId, invoiceId, fileId);
      expect(url.endsWith('/view')).toBeTrue();
    });

    it('should include all path parameters in download URL', () => {
      const url = service.getInvoiceFileDownloadUrl(5, 10, 15, 20, 25);
      expect(url).toContain('/responsibility-centres/5/');
      expect(url).toContain('/fiscal-years/10/');
      expect(url).toContain('/spending-items/15/');
      expect(url).toContain('/invoices/20/');
      expect(url).toContain('/files/25/');
    });

    it('should include all path parameters in view URL', () => {
      const url = service.getInvoiceFileViewUrl(5, 10, 15, 20, 25);
      expect(url).toContain('/responsibility-centres/5/');
      expect(url).toContain('/fiscal-years/10/');
      expect(url).toContain('/spending-items/15/');
      expect(url).toContain('/invoices/20/');
      expect(url).toContain('/files/25/');
    });
  });
});
