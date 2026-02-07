/**
 * myRC - Directory Search Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DirectorySearchService, DirectorySearchResult } from './directory-search.service';

describe('DirectorySearchService', () => {
  let service: DirectorySearchService;
  let httpMock: HttpTestingController;

  const mockUserResults: DirectorySearchResult[] = [
    { identifier: 'jsmith', displayName: 'John Smith', source: 'APP', email: 'john@example.com' },
    { identifier: 'jdoe', displayName: 'Jane Doe', source: 'LDAP', email: 'jane@example.com' }
  ];

  const mockGroupResults: DirectorySearchResult[] = [
    { identifier: 'cn=Finance,ou=groups,dc=example,dc=com', displayName: 'Finance', source: 'LDAP', email: null },
    { identifier: 'cn=IT-Team,ou=groups,dc=example,dc=com', displayName: 'IT Team', source: 'LDAP', email: null }
  ];

  const mockDistributionListResults: DirectorySearchResult[] = [
    { identifier: 'cn=all-staff,ou=distribution-lists,dc=example,dc=com', displayName: 'all-staff - All staff', source: 'LDAP', email: 'all-staff@example.com' },
    { identifier: 'cn=finance-team,ou=distribution-lists,dc=example,dc=com', displayName: 'finance-team - Finance team', source: 'LDAP', email: 'finance-team@example.com' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DirectorySearchService]
    });

    service = TestBed.inject(DirectorySearchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('searchUsers', () => {
    it('should return matching users', () => {
      service.searchUsers('joh').subscribe(results => {
        expect(results.length).toBe(2);
        expect(results[0].identifier).toBe('jsmith');
        expect(results[0].source).toBe('APP');
        expect(results[1].identifier).toBe('jdoe');
        expect(results[1].source).toBe('LDAP');
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('q') === 'joh' && r.params.get('max') === '10'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockUserResults);
    });

    it('should make API call for empty query (browse-all)', () => {
      service.searchUsers('').subscribe(results => {
        expect(results.length).toBe(2);
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('q') === ''
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockUserResults);
    });

    it('should return empty array for null query', () => {
      service.searchUsers(null as any).subscribe(results => {
        expect(results).toEqual([]);
      });

      httpMock.expectNone('/api/directory/users');
    });

    it('should make API call for whitespace-only query (browse-all)', () => {
      service.searchUsers('   ').subscribe(results => {
        expect(results.length).toBe(2);
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('q') === ''
      );
      req.flush(mockUserResults);
    });

    it('should send custom maxResults parameter', () => {
      service.searchUsers('test', 25).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('max') === '25'
      );
      req.flush([]);
    });

    it('should clamp maxResults to 50', () => {
      service.searchUsers('test', 100).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('max') === '50'
      );
      req.flush([]);
    });

    it('should clamp maxResults minimum to 1', () => {
      service.searchUsers('test', 0).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('max') === '1'
      );
      req.flush([]);
    });

    it('should trim query whitespace', () => {
      service.searchUsers('  test  ').subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/users' && r.params.get('q') === 'test'
      );
      req.flush([]);
    });

    it('should handle HTTP error gracefully', () => {
      service.searchUsers('test').subscribe(results => {
        expect(results).toEqual([]);
      });

      const req = httpMock.expectOne(r => r.url === '/api/directory/users');
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should include withCredentials flag', () => {
      service.searchUsers('test').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/directory/users');
      expect(req.request.withCredentials).toBe(true);
      req.flush([]);
    });
  });

  describe('searchGroups', () => {
    it('should return matching groups', () => {
      service.searchGroups('fin').subscribe(results => {
        expect(results.length).toBe(2);
        expect(results[0].identifier).toBe('cn=Finance,ou=groups,dc=example,dc=com');
        expect(results[0].source).toBe('LDAP');
        expect(results[0].email).toBeNull();
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/groups' && r.params.get('q') === 'fin' && r.params.get('max') === '10'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockGroupResults);
    });

    it('should make API call for empty query (browse-all)', () => {
      service.searchGroups('').subscribe(results => {
        expect(results.length).toBe(2);
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/groups' && r.params.get('q') === ''
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockGroupResults);
    });

    it('should return empty array for null query', () => {
      service.searchGroups(null as any).subscribe(results => {
        expect(results).toEqual([]);
      });

      httpMock.expectNone('/api/directory/groups');
    });

    it('should send custom maxResults parameter', () => {
      service.searchGroups('test', 20).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/groups' && r.params.get('max') === '20'
      );
      req.flush([]);
    });

    it('should handle HTTP error gracefully', () => {
      service.searchGroups('test').subscribe(results => {
        expect(results).toEqual([]);
      });

      const req = httpMock.expectOne(r => r.url === '/api/directory/groups');
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should include withCredentials flag', () => {
      service.searchGroups('test').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/directory/groups');
      expect(req.request.withCredentials).toBe(true);
      req.flush([]);
    });

    it('should return empty list when no results match', () => {
      service.searchGroups('xyz').subscribe(results => {
        expect(results).toEqual([]);
      });

      const req = httpMock.expectOne(r => r.url === '/api/directory/groups');
      req.flush([]);
    });
  });

  describe('searchDistributionLists', () => {
    it('should return matching distribution lists', () => {
      service.searchDistributionLists('staff').subscribe(results => {
        expect(results.length).toBe(2);
        expect(results[0].identifier).toBe('cn=all-staff,ou=distribution-lists,dc=example,dc=com');
        expect(results[0].source).toBe('LDAP');
        expect(results[0].email).toBe('all-staff@example.com');
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('q') === 'staff' && r.params.get('max') === '10'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockDistributionListResults);
    });

    it('should make API call for empty query (browse-all)', () => {
      service.searchDistributionLists('').subscribe(results => {
        expect(results.length).toBe(2);
      });

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('q') === ''
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockDistributionListResults);
    });

    it('should return empty array for null query', () => {
      service.searchDistributionLists(null as any).subscribe(results => {
        expect(results).toEqual([]);
      });

      httpMock.expectNone('/api/directory/distribution-lists');
    });

    it('should send custom maxResults parameter', () => {
      service.searchDistributionLists('test', 20).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('max') === '20'
      );
      req.flush([]);
    });

    it('should clamp maxResults to 50', () => {
      service.searchDistributionLists('test', 100).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('max') === '50'
      );
      req.flush([]);
    });

    it('should clamp maxResults minimum to 1', () => {
      service.searchDistributionLists('test', 0).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('max') === '1'
      );
      req.flush([]);
    });

    it('should handle HTTP error gracefully', () => {
      service.searchDistributionLists('test').subscribe(results => {
        expect(results).toEqual([]);
      });

      const req = httpMock.expectOne(r => r.url === '/api/directory/distribution-lists');
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should include withCredentials flag', () => {
      service.searchDistributionLists('test').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/directory/distribution-lists');
      expect(req.request.withCredentials).toBe(true);
      req.flush([]);
    });

    it('should trim query whitespace', () => {
      service.searchDistributionLists('  staff  ').subscribe();

      const req = httpMock.expectOne(r =>
        r.url === '/api/directory/distribution-lists' && r.params.get('q') === 'staff'
      );
      req.flush([]);
    });

    it('should return empty list when no results match', () => {
      service.searchDistributionLists('xyz').subscribe(results => {
        expect(results).toEqual([]);
      });

      const req = httpMock.expectOne(r => r.url === '/api/directory/distribution-lists');
      req.flush([]);
    });
  });
});
