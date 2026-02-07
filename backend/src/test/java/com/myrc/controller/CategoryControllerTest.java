/*
 * myRC - Category Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.CategoryDTO;
import com.myrc.model.FundingType;
import com.myrc.service.CategoryService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for CategoryController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  @Mock
  private CategoryService categoryService;

  private Authentication authentication;
  private CategoryController controller;
  private CategoryDTO testCategory;

  @BeforeEach
  void setUp() {
    controller = new CategoryController(categoryService);
    authentication = createAuthentication("testuser");

    testCategory = new CategoryDTO();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setDescription("Test Description");
    testCategory.setIsDefault(false);
    testCategory.setDisplayOrder(0);
    testCategory.setFundingType("BOTH");
    testCategory.setFiscalYearId(1L);
    testCategory.setFiscalYearName("FY 2026-2027");
    testCategory.setActive(true);
  }
  
  private Authentication createAuthentication(String username) {
    return new Authentication() {
      @Override
      public String getName() { return username; }
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
      @Override
      public Object getCredentials() { return null; }
      @Override
      public Object getDetails() { return null; }
      @Override
      public Object getPrincipal() { return username; }
      @Override
      public boolean isAuthenticated() { return true; }
      @Override
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
    };
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Nested
  @DisplayName("GET /categories Tests")
  class GetCategoriesTests {

    @Test
    @DisplayName("Should return categories successfully")
    void shouldReturnCategoriesSuccessfully() {
      when(categoryService.getCategoriesByFiscalYearId(1L, "testuser"))
          .thenReturn(Arrays.asList(testCategory));

      ResponseEntity<List<CategoryDTO>> response = controller.getCategories(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should return empty list when no categories")
    void shouldReturnEmptyListWhenNoCategories() {
      when(categoryService.getCategoriesByFiscalYearId(1L, "testuser"))
          .thenReturn(Arrays.asList());

      ResponseEntity<List<CategoryDTO>> response = controller.getCategories(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should return forbidden when no access")
    void shouldReturnForbiddenWhenNoAccess() {
      when(categoryService.getCategoriesByFiscalYearId(1L, "testuser"))
          .thenThrow(new IllegalArgumentException("User does not have access"));

      ResponseEntity<List<CategoryDTO>> response = controller.getCategories(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /categories/{id} Tests")
  class GetCategoryTests {

    @Test
    @DisplayName("Should return category when found")
    void shouldReturnCategoryWhenFound() {
      when(categoryService.getCategoryById(1L, "testuser"))
          .thenReturn(Optional.of(testCategory));

      ResponseEntity<CategoryDTO> response = controller.getCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when category doesn't exist")
    void shouldReturnNotFoundWhenCategoryDoesntExist() {
      when(categoryService.getCategoryById(999L, "testuser"))
          .thenReturn(Optional.empty());

      ResponseEntity<CategoryDTO> response = controller.getCategory(1L, 1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /categories Tests")
  class CreateCategoryTests {

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
      CategoryController.CategoryCreateRequest request = new CategoryController.CategoryCreateRequest();
      request.name = "New Category";
      request.description = "New Description";
      request.fundingType = "BOTH";

      when(categoryService.createCategory(eq(1L), eq("testuser"), eq("New Category"), 
          eq("New Description"), eq(FundingType.BOTH)))
          .thenReturn(testCategory);

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request for invalid funding type")
    void shouldReturnBadRequestForInvalidFundingType() {
      CategoryController.CategoryCreateRequest request = new CategoryController.CategoryCreateRequest();
      request.name = "Test Category";
      request.description = "Description";
      request.fundingType = "INVALID";

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return conflict for duplicate name")
    void shouldReturnConflictForDuplicateName() {
      CategoryController.CategoryCreateRequest request = new CategoryController.CategoryCreateRequest();
      request.name = "Existing Category";
      request.description = "Description";

      when(categoryService.createCategory(eq(1L), eq("testuser"), anyString(), anyString(), any()))
          .thenThrow(new IllegalArgumentException("already exists"));

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PUT /categories/{id} Tests")
  class UpdateCategoryTests {

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
      CategoryController.CategoryUpdateRequest request = new CategoryController.CategoryUpdateRequest();
      request.name = "Updated Name";
      request.description = "Updated Description";

      when(categoryService.updateCategory(eq(1L), eq("testuser"), eq("Updated Name"), 
          eq("Updated Description"), any()))
          .thenReturn(testCategory);

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when category doesn't exist")
    void shouldReturnNotFoundWhenCategoryDoesntExist() {
      CategoryController.CategoryUpdateRequest request = new CategoryController.CategoryUpdateRequest();
      request.name = "Updated Name";

      when(categoryService.updateCategory(eq(999L), eq("testuser"), anyString(), any(), any()))
          .thenThrow(new IllegalArgumentException("not found"));

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 999L, authentication, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when modifying default category")
    void shouldReturnForbiddenWhenModifyingDefault() {
      CategoryController.CategoryUpdateRequest request = new CategoryController.CategoryUpdateRequest();
      request.name = "Updated Name";

      when(categoryService.updateCategory(eq(1L), eq("testuser"), anyString(), any(), any()))
          .thenThrow(new IllegalArgumentException("default category"));

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /categories/{id} Tests")
  class DeleteCategoryTests {

    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategorySuccessfully() {
      doNothing().when(categoryService).deleteCategory(1L, "testuser");

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when category doesn't exist")
    void shouldReturnNotFoundWhenCategoryDoesntExist() {
      doThrow(new IllegalArgumentException("not found"))
          .when(categoryService).deleteCategory(999L, "testuser");

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when deleting default category")
    void shouldReturnForbiddenWhenDeletingDefault() {
      doThrow(new IllegalArgumentException("default category"))
          .when(categoryService).deleteCategory(1L, "testuser");

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request when deleting category in use")
    void shouldReturnBadRequestWhenDeletingCategoryInUse() {
      doThrow(new IllegalArgumentException("Cannot delete category \"Compute\" because it is in use by funding, procurement, or spending items"))
          .when(categoryService).deleteCategory(1L, "testuser");

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /categories/ensure-defaults Tests")
  class EnsureDefaultsTests {

    @Test
    @DisplayName("Should ensure default categories successfully")
    void shouldEnsureDefaultCategoriesSuccessfully() {
      when(categoryService.ensureDefaultCategoriesExist(1L, "testuser"))
          .thenReturn(Arrays.asList(testCategory));

      // The controller method is ensureDefaults, not ensureDefaultCategories
      ResponseEntity<?> response = controller.ensureDefaults(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when no access")
    void shouldReturnForbiddenWhenNoAccess() {
      when(categoryService.ensureDefaultCategoriesExist(1L, "testuser"))
          .thenThrow(new IllegalArgumentException("User does not have access"));

      ResponseEntity<?> response = controller.ensureDefaults(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /categories/reorder Tests")
  class ReorderCategoriesTests {

    @Test
    @DisplayName("Should reorder categories successfully")
    void shouldReorderCategoriesSuccessfully() {
      CategoryController.ReorderRequest request = new CategoryController.ReorderRequest();
      request.categoryIds = Arrays.asList(2L, 1L);

      when(categoryService.reorderCategories(1L, "testuser", Arrays.asList(2L, 1L)))
          .thenReturn(Arrays.asList(testCategory));

      ResponseEntity<?> response = controller.reorderCategories(1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when no write access")
    void shouldReturnForbiddenWhenNoWriteAccess() {
      CategoryController.ReorderRequest request = new CategoryController.ReorderRequest();
      request.categoryIds = Arrays.asList(2L, 1L);

      when(categoryService.reorderCategories(1L, "testuser", Arrays.asList(2L, 1L)))
          .thenThrow(new IllegalArgumentException("User does not have write access"));

      ResponseEntity<?> response = controller.reorderCategories(1L, 1L, authentication, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }
}
