/**
 * Training Item Service Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TrainingItemService, TrainingItemCreateRequest, TrainingItemUpdateRequest } from './training-item.service';
import { TrainingItem, TrainingMoneyAllocation } from '../models/training-item.model';

describe('TrainingItemService', () => {
  let service: TrainingItemService;
  let httpMock: HttpTestingController;

  const mockAllocation: TrainingMoneyAllocation = {
    moneyId: 1,
    moneyName: 'A-Base',
    moneyCode: 'AB',
    isDefault: true,
    omAmount: 2500
  };

  const mockTrainingItem: TrainingItem = {
    id: 1,
    name: 'Java Certification',
    description: 'Oracle Java SE certification course',
    provider: 'Oracle',
    eco: 'ECO-001',
    format: 'ONLINE',
    status: 'PLANNED',
    trainingType: 'COURSE_TRAINING',
    startDate: '2026-03-01',
    endDate: '2026-03-15',
    location: 'Online',
    participants: [],
    fiscalYearId: 1,
    createdAt: '2026-02-16T12:00:00Z',
    updatedAt: '2026-02-16T12:00:00Z',
    active: true,
    moneyAllocations: [mockAllocation]
  };

  const mockSecondItem: TrainingItem = {
    ...mockTrainingItem,
    id: 2,
    name: 'Leadership Workshop',
    description: 'Management training',
    provider: 'Internal',
    trainingType: 'CONFERENCE_REGISTRATION'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TrainingItemService]
    });

    service = TestBed.inject(TrainingItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getTrainingItemsByFY', () => {
    it('should return all training items for a fiscal year', () => {
      const rcId = 1;
      const fyId = 1;

      service.getTrainingItemsByFY(rcId, fyId).subscribe(items => {
        expect(items.length).toBe(2);
        expect(items[0].name).toBe('Java Certification');
        expect(items[1].name).toBe('Leadership Workshop');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/training-items`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush([mockTrainingItem, mockSecondItem]);
    });

    it('should handle access denied error', () => {
      service.getTrainingItemsByFY(1, 1).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items');
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getTrainingItem', () => {
    it('should return a specific training item', () => {
      service.getTrainingItem(1, 1, 1).subscribe(item => {
        expect(item.name).toBe('Java Certification');
        expect(item.status).toBe('PLANNED');
        expect(item.moneyAllocations?.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockTrainingItem);
    });

    it('should handle not found error', () => {
      service.getTrainingItem(1, 1, 999).subscribe({
        error: (error) => {
          expect(error.message).toContain('Training item not found');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/999');
      req.flush({ message: 'Training item not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createTrainingItem', () => {
    it('should create a new training item', () => {
      const request: TrainingItemCreateRequest = {
        name: 'Java Certification',
        description: 'Oracle Java SE certification course',
        provider: 'Oracle',
        eco: 'ECO-001',
        format: 'ONLINE',
        trainingType: 'COURSE_TRAINING',
        moneyAllocations: [mockAllocation]
      };

      service.createTrainingItem(1, 1, request).subscribe(item => {
        expect(item.name).toBe('Java Certification');
        expect(item.status).toBe('PLANNED');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockTrainingItem);
    });

    it('should handle duplicate name error', () => {
      const request: TrainingItemCreateRequest = { name: 'Java Certification' };

      service.createTrainingItem(1, 1, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('already exists');
        }
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items');
      req.flush({ message: 'already exists' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateTrainingItem', () => {
    it('should update a training item', () => {
      const request: TrainingItemUpdateRequest = {
        name: 'Updated Java Cert'
      };

      service.updateTrainingItem(1, 1, 1, request).subscribe(item => {
        expect(item.name).toBe('Java Certification');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(mockTrainingItem);
    });
  });

  describe('deleteTrainingItem', () => {
    it('should delete a training item', () => {
      service.deleteTrainingItem(1, 1, 1).subscribe();

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('updateStatus', () => {
    it('should update training item status', () => {
      service.updateStatus(1, 1, 1, 'APPROVED').subscribe(item => {
        expect(item.name).toBe('Java Certification');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1/status');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'APPROVED' });
      req.flush(mockTrainingItem);
    });
  });

  describe('getMoneyAllocations', () => {
    it('should return allocations for a training item', () => {
      service.getMoneyAllocations(1, 1, 1).subscribe(allocations => {
        expect(allocations.length).toBe(1);
        expect(allocations[0].moneyName).toBe('A-Base');
        expect(allocations[0].omAmount).toBe(2500);
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1/allocations');
      expect(req.request.method).toBe('GET');
      req.flush([mockAllocation]);
    });
  });

  describe('updateMoneyAllocations', () => {
    it('should update allocations for a training item', () => {
      const allocations: TrainingMoneyAllocation[] = [{ ...mockAllocation, omAmount: 3000 }];

      service.updateMoneyAllocations(1, 1, 1, allocations).subscribe(item => {
        expect(item.name).toBe('Java Certification');
      });

      const req = httpMock.expectOne('/api/responsibility-centres/1/fiscal-years/1/training-items/1/allocations');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(allocations);
      req.flush(mockTrainingItem);
    });
  });
});
