import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ResponsibilityCentreService } from './responsibility-centre.service';

describe('ResponsibilityCentreService', () => {
  let service: ResponsibilityCentreService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ResponsibilityCentreService]
    });
    service = TestBed.inject(ResponsibilityCentreService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all responsibility centres', () => {
    const mockRCs = [{ id: 1, name: 'RC 1' }];
    service.getAllResponsibilityCentres().subscribe(rcs => {
      expect(rcs.length).toBe(1);
      expect(rcs[0].name).toBe('RC 1');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/responsibility-centres');
    expect(req.request.method).toBe('GET');
    req.flush(mockRCs);
  });
});
