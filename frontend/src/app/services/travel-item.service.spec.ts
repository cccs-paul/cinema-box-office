/**
 * Travel Item Service Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TravelItemService, TravelItemCreateRequest, TravelItemUpdateRequest } from './travel-item.service';
import { TravelItem, TravelMoneyAllocation } from '../models/travel-item.model';

describe('TravelItemService', () => {
  let service: TravelItemService;
  let httpMock: HttpTestingController;

  const mockAllocation: TravelMoneyAllocation = {
    moneyId: 1,
    moneyName: 'A-Base',
    moneyCode: 'AB',
    isDefault: true,
    omAmount: 3200
  };

  const mockTravelItem: TravelItem = {
    id: 1,
    name: 'Ottawa Conference Trip',
    description: 'Annual government technology conference',
    emap: 'EMAP-001',
    destination: 'Ottawa, ON',
    purpose: 'Conference attendance',
    status: 'PLANNED',
    travelType: 'DOMESTIC',
    departureDate: '2026-04-01',
    returnDate: '2026-04-05',
    travellers: [],
    numberOfTravellers: 0,
    fiscalYearId: 1,
    createdAt: '2026-02-16T12:00:00Z',
    updatedAt: '2026-02-16T12:00:00Z',
    active: true,
    moneyAllocations: [mockAllocation]
  };

  const mockSecondItem: TravelItem = {
    ...mockTravelItem,
    id: 2,
    name: 'Vancouver Training',
    description: 'DevOps training course',
    travelType: 'NORTH_AMERICA',
    destination: 'Vancouver, BC'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TravelItemService]
    });

    service = TestBed.inject(TravelItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getTravelItemsByFY', () => {
    it('should return all travel items for a fiscal year', () => {
      const rcId = 1;
      const fyId = 1;

      service.getTravelItemsByFY(rcId, fyId).subscribe(items => {
        expect(items.length).toBe(2);
        expect(items[0].name).toBe('Ottawa Conference Trip');
        expect(items[1].name).toBe('Vancouver Training');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/travel-items`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush([mockTravelItem, mockSecondItem]);
    });

    it('should handle access denied error', () => {
      service.getTravelItemsByFY(1, 1).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items');
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getTravelItem', () => {
    it('should return a specific travel item', () => {
      service.getTravelItem(1, 1, 1).subscribe(item => {
        expect(item.name).toBe('Ottawa Conference Trip');
        expect(item.status).toBe('PLANNED');
        expect(item.moneyAllocations?.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockTravelItem);
    });

    it('should handle not found error', () => {
      service.getTravelItem(1, 1, 999).subscribe({
        error: (error) => {
          expect(error.message).toContain('Travel item not found');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/999');
      req.flush({ message: 'Travel item not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createTravelItem', () => {
    it('should create a new travel item', () => {
      const request: TravelItemCreateRequest = {
        name: 'Ottawa Conference Trip',
        description: 'Annual government technology conference',
        destination: 'Ottawa, ON',
        emap: 'EMAP-001',
        travelType: 'DOMESTIC',
        moneyAllocations: [mockAllocation]
      };

      service.createTravelItem(1, 1, request).subscribe(item => {
        expect(item.name).toBe('Ottawa Conference Trip');
        expect(item.status).toBe('PLANNED');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockTravelItem);
    });

    it('should handle duplicate name error', () => {
      const request: TravelItemCreateRequest = { name: 'Ottawa Conference Trip' };

      service.createTravelItem(1, 1, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('already exists');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items');
      req.flush({ message: 'already exists' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateTravelItem', () => {
    it('should update a travel item', () => {
      const request: TravelItemUpdateRequest = {
        name: 'Updated Ottawa Trip'
      };

      service.updateTravelItem(1, 1, 1, request).subscribe(item => {
        expect(item.name).toBe('Ottawa Conference Trip');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(mockTravelItem);
    });
  });

  describe('deleteTravelItem', () => {
    it('should delete a travel item', () => {
      service.deleteTravelItem(1, 1, 1).subscribe();

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('updateStatus', () => {
    it('should update travel item status', () => {
      service.updateStatus(1, 1, 1, 'APPROVED').subscribe(item => {
        expect(item.name).toBe('Ottawa Conference Trip');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/status');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'APPROVED' });
      req.flush(mockTravelItem);
    });
  });

  describe('getMoneyAllocations', () => {
    it('should return allocations for a travel item', () => {
      service.getMoneyAllocations(1, 1, 1).subscribe(allocations => {
        expect(allocations.length).toBe(1);
        expect(allocations[0].moneyName).toBe('A-Base');
        expect(allocations[0].omAmount).toBe(3200);
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/allocations');
      expect(req.request.method).toBe('GET');
      req.flush([mockAllocation]);
    });
  });

  describe('updateMoneyAllocations', () => {
    it('should update allocations for a travel item', () => {
      const allocations: TravelMoneyAllocation[] = [{ ...mockAllocation, omAmount: 4000 }];

      service.updateMoneyAllocations(1, 1, 1, allocations).subscribe(item => {
        expect(item.name).toBe('Ottawa Conference Trip');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/allocations');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(allocations);
      req.flush(mockTravelItem);
    });
  });

  describe('Traveller management', () => {
    const mockTraveller = {
      id: 10,
      name: 'Test Traveller',
      taac: 'TAAC-001',
      approvalStatus: 'PLANNED',
      estimatedCost: 2000,
      finalCost: null,
      estimatedCurrency: 'CAD',
      estimatedExchangeRate: 1.0,
      finalCurrency: 'CAD',
      finalExchangeRate: null
    };

    it('should add a traveller to a travel item', () => {
      const newTraveller = { name: 'New Traveller', taac: '', approvalStatus: 'PLANNED', estimatedCost: null, finalCost: null, estimatedCurrency: 'CAD', estimatedExchangeRate: null, finalCurrency: 'CAD', finalExchangeRate: null };

      service.addTraveller(1, 1, 1, newTraveller as any).subscribe(t => {
        expect(t.id).toBe(10);
        expect(t.name).toBe('Test Traveller');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/travellers');
      expect(req.request.method).toBe('POST');
      req.flush(mockTraveller);
    });

    it('should update a traveller', () => {
      service.updateTraveller(1, 1, 1, 10, mockTraveller as any).subscribe(t => {
        expect(t.name).toBe('Test Traveller');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/travellers/10');
      expect(req.request.method).toBe('PUT');
      req.flush(mockTraveller);
    });

    it('should delete a traveller', () => {
      service.deleteTraveller(1, 1, 1, 10).subscribe();

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/travel-items/1/travellers/10');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
