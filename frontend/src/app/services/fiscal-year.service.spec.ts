import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FiscalYearService } from './fiscal-year.service';

describe('FiscalYearService', () => {
  let service: FiscalYearService;
  let httpMock: HttpTestingController;

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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all fiscal years for an RC', () => {
    const mockFYs = [{ id: 10, name: '2024' }];
    service.getAllForRc(1).subscribe(fys => {
      expect(fys.length).toBe(1);
      expect(fys[0].name).toBe('2024');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/fiscal-years/rc/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockFYs);
  });
});
