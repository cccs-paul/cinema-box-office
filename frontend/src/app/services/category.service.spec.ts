/**
 * Category Service Tests for myRC application.
 * Tests the unified category service used for both funding and spending items.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CategoryService, CategoryCreateRequest, CategoryUpdateRequest } from './category.service';
import { Category } from '../models/category.model';

describe('CategoryService', () => {
  let service: CategoryService;
  let httpMock: HttpTestingController;

  const mockCategory: Category = {
    id: 1,
    name: 'Compute',
    description: 'Compute resources',
    isDefault: true,
    fiscalYearId: 1,
    displayOrder: 0,
    active: true,
    fundingType: 'BOTH',
    allowsCap: true,
    allowsOm: true
  };

  const mockCustomCategory: Category = {
    id: 7,
    name: 'Cloud Services',
    description: 'External cloud services',
    isDefault: false,
    fiscalYearId: 1,
    displayOrder: 6,
    active: true,
    fundingType: 'BOTH',
    allowsCap: true,
    allowsOm: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CategoryService]
    });

    service = TestBed.inject(CategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getCategoriesByFY', () => {
    it('should return categories for a fiscal year', () => {
      const rcId = 1;
      const fyId = 1;

      service.getCategoriesByFY(rcId, fyId).subscribe(categories => {
        expect(categories.length).toBe(2);
        expect(categories[0].name).toBe('Compute');
        expect(categories[1].name).toBe('Cloud Services');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush([mockCategory, mockCustomCategory]);
    });

    it('should handle access denied error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getCategoriesByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Access denied');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('getCategory', () => {
    it('should return a specific category', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 1;

      service.getCategory(rcId, fyId, categoryId).subscribe(category => {
        expect(category.name).toBe('Compute');
        expect(category.isDefault).toBeTrue();
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCategory);
    });

    it('should handle not found error', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 999;

      service.getCategory(rcId, fyId, categoryId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Category not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      req.flush({ message: 'Category not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createCategory', () => {
    it('should create a new category', () => {
      const rcId = 1;
      const fyId = 1;
      const request: CategoryCreateRequest = {
        name: 'Cloud Services',
        description: 'External cloud services'
      };

      service.createCategory(rcId, fyId, request).subscribe(category => {
        expect(category.name).toBe('Cloud Services');
        expect(category.isDefault).toBeFalse();
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockCustomCategory);
    });

    it('should handle duplicate name error', () => {
      const rcId = 1;
      const fyId = 1;
      const request: CategoryCreateRequest = {
        name: 'Compute',
        description: 'Duplicate category'
      };

      service.createCategory(rcId, fyId, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('category with this name already exists');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      req.flush({ message: 'Category name already exists' }, { status: 409, statusText: 'Conflict' });
    });
  });

  describe('updateCategory', () => {
    it('should update a category', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 7;
      const request: CategoryUpdateRequest = {
        name: 'Cloud Services Updated',
        description: 'Updated description'
      };

      const updatedCategory = { ...mockCustomCategory, name: 'Cloud Services Updated', description: 'Updated description' };

      service.updateCategory(rcId, fyId, categoryId, request).subscribe(category => {
        expect(category.name).toBe('Cloud Services Updated');
        expect(category.description).toBe('Updated description');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(updatedCategory);
    });

    it('should handle not found error on update', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 999;
      const request: CategoryUpdateRequest = { name: 'Updated' };

      service.updateCategory(rcId, fyId, categoryId, request).subscribe({
        error: (error) => {
          expect(error.message).toContain('Category not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      req.flush({ message: 'Category not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('deleteCategory', () => {
    it('should delete a category', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 7;

      service.deleteCategory(rcId, fyId, categoryId).subscribe(() => {
        expect(true).toBeTrue();
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle not found error on delete', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryId = 999;

      service.deleteCategory(rcId, fyId, categoryId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Category not found');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/${categoryId}`);
      req.flush({ message: 'Category not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('ensureDefaults', () => {
    it('should ensure default categories exist', () => {
      const rcId = 1;
      const fyId = 1;
      const defaultCategories: Category[] = [
        mockCategory,
        { ...mockCategory, id: 2, name: 'GPUs', displayOrder: 1 },
        { ...mockCategory, id: 3, name: 'Storage', displayOrder: 2 }
      ];

      service.ensureDefaults(rcId, fyId).subscribe(categories => {
        expect(categories.length).toBe(3);
        expect(categories[0].name).toBe('Compute');
        expect(categories[1].name).toBe('GPUs');
        expect(categories[2].name).toBe('Storage');
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/ensure-defaults`);
      expect(req.request.method).toBe('POST');
      req.flush(defaultCategories);
    });
  });

  describe('reorderCategories', () => {
    it('should reorder categories', () => {
      const rcId = 1;
      const fyId = 1;
      const categoryIds = [3, 1, 2];
      const reorderedCategories: Category[] = [
        { ...mockCategory, id: 3, name: 'Storage', displayOrder: 0 },
        { ...mockCategory, id: 1, name: 'Compute', displayOrder: 1 },
        { ...mockCategory, id: 2, name: 'GPUs', displayOrder: 2 }
      ];

      service.reorderCategories(rcId, fyId, categoryIds).subscribe(categories => {
        expect(categories.length).toBe(3);
        expect(categories[0].id).toBe(3);
        expect(categories[0].displayOrder).toBe(0);
        expect(categories[1].id).toBe(1);
        expect(categories[1].displayOrder).toBe(1);
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories/reorder`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ categoryIds });
      req.flush(reorderedCategories);
    });
  });

  describe('Error Handling', () => {
    it('should handle server error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getCategoriesByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Internal server error');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      req.flush({ message: 'Internal server error' }, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should handle unauthorized error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getCategoriesByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('Authentication required');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle network error', () => {
      const rcId = 1;
      const fyId = 1;

      service.getCategoriesByFY(rcId, fyId).subscribe({
        error: (error) => {
          expect(error.message).toContain('unexpected error');
        }
      });

      const req = httpMock.expectOne(`/api/responsibility-centres/${rcId}/fiscal-years/${fyId}/categories`);
      req.error(new ProgressEvent('error'), { status: 0 });
    });
  });
});
