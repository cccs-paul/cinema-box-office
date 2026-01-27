/*
 * myRC - Spending Category Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for SpendingCategoryController.
 */
package com.boxoffice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.boxoffice.dto.SpendingCategoryDTO;
import com.boxoffice.service.SpendingCategoryService;
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
 * Unit tests for SpendingCategoryController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpendingCategoryController Tests")
class SpendingCategoryControllerTest {

  @Mock
  private SpendingCategoryService categoryService;

  private SpendingCategoryController controller;
  private SpendingCategoryDTO computeCategory;
  private SpendingCategoryDTO customCategory;
  private Authentication authentication;

  /**
   * Simple test implementation of Authentication.
   */
  private static class TestAuthentication implements Authentication {
    private final String name;

    TestAuthentication(String name) {
      this.name = name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return name;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      // No-op for testing
    }

    @Override
    public String getName() {
      return name;
    }
  }

  @BeforeEach
  void setUp() {
    controller = new SpendingCategoryController(categoryService);
    authentication = new TestAuthentication("testuser");

    computeCategory = new SpendingCategoryDTO(
        1L, "Compute", "Compute resources", true,
        0, 1L, "FY 2025-2026", true
    );

    customCategory = new SpendingCategoryDTO(
        7L, "Cloud Services", "External cloud services", false,
        6, 1L, "FY 2025-2026", true
    );
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Nested
  @DisplayName("getCategories Tests")
  class GetCategoriesTests {

    @Test
    @DisplayName("Returns all categories")
    void returnsAllCategories() {
      List<SpendingCategoryDTO> categories = Arrays.asList(computeCategory, customCategory);
      when(categoryService.getCategoriesByFiscalYearId(anyLong(), anyString())).thenReturn(categories);

      ResponseEntity<List<SpendingCategoryDTO>> response = controller.getCategories(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());
      assertEquals("Compute", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("Returns 403 on access denied")
    void returnsForbiddenOnAccessDenied() {
      when(categoryService.getCategoriesByFiscalYearId(anyLong(), anyString()))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<SpendingCategoryDTO>> response = controller.getCategories(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getCategory Tests")
  class GetCategoryTests {

    @Test
    @DisplayName("Returns specific category")
    void returnsSpecificCategory() {
      when(categoryService.getCategoryById(anyLong(), anyString()))
          .thenReturn(Optional.of(computeCategory));

      ResponseEntity<SpendingCategoryDTO> response = controller.getCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("Compute", response.getBody().getName());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      when(categoryService.getCategoryById(anyLong(), anyString()))
          .thenReturn(Optional.empty());

      ResponseEntity<SpendingCategoryDTO> response = controller.getCategory(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("createCategory Tests")
  class CreateCategoryTests {

    @Test
    @DisplayName("Creates category successfully")
    void createsCategory() {
      SpendingCategoryController.CategoryCreateRequest request = new SpendingCategoryController.CategoryCreateRequest();
      request.name = "New Category";
      request.description = "A new category";

      when(categoryService.createCategory(eq(1L), anyString(), eq("New Category"), eq("A new category")))
          .thenReturn(customCategory);

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 400 on validation error")
    void returnsBadRequestOnValidation() {
      SpendingCategoryController.CategoryCreateRequest request = new SpendingCategoryController.CategoryCreateRequest();
      request.name = "";

      when(categoryService.createCategory(eq(1L), anyString(), eq(""), eq(null)))
          .thenThrow(new IllegalArgumentException("Category name is required"));

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 409 on duplicate name")
    void returnsConflictOnDuplicate() {
      SpendingCategoryController.CategoryCreateRequest request = new SpendingCategoryController.CategoryCreateRequest();
      request.name = "Compute";

      when(categoryService.createCategory(eq(1L), anyString(), eq("Compute"), eq(null)))
          .thenThrow(new IllegalArgumentException("A category with name 'Compute' already exists"));

      ResponseEntity<?> response = controller.createCategory(1L, 1L, authentication, request);

      assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateCategory Tests")
  class UpdateCategoryTests {

    @Test
    @DisplayName("Updates category successfully")
    void updatesCategory() {
      SpendingCategoryController.CategoryUpdateRequest request = new SpendingCategoryController.CategoryUpdateRequest();
      request.name = "Updated Name";
      request.description = "Updated description";

      when(categoryService.updateCategory(eq(7L), anyString(), eq("Updated Name"), eq("Updated description")))
          .thenReturn(customCategory);

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 7L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      SpendingCategoryController.CategoryUpdateRequest request = new SpendingCategoryController.CategoryUpdateRequest();
      request.name = "Updated";
      request.description = "Updated description";

      when(categoryService.updateCategory(eq(99L), anyString(), anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Category not found"));

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 99L, authentication, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 403 for default category name change")
    void returnsForbiddenForDefaultNameChange() {
      SpendingCategoryController.CategoryUpdateRequest request = new SpendingCategoryController.CategoryUpdateRequest();
      request.name = "Changed";
      request.description = "Changed description";

      when(categoryService.updateCategory(eq(1L), anyString(), anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Cannot change the name of a default category"));

      ResponseEntity<?> response = controller.updateCategory(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteCategory Tests")
  class DeleteCategoryTests {

    @Test
    @DisplayName("Deletes category successfully")
    void deletesCategory() {
      doNothing().when(categoryService).deleteCategory(eq(7L), anyString());

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 7L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      doThrow(new IllegalArgumentException("Category not found"))
          .when(categoryService).deleteCategory(eq(99L), anyString());

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 403 for default category")
    void returnsForbiddenForDefault() {
      doThrow(new IllegalArgumentException("Cannot delete a default category"))
          .when(categoryService).deleteCategory(eq(1L), anyString());

      ResponseEntity<?> response = controller.deleteCategory(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("ensureDefaults Tests")
  class EnsureDefaultsTests {

    @Test
    @DisplayName("Ensures defaults successfully")
    void ensuresDefaults() {
      List<SpendingCategoryDTO> categories = Arrays.asList(computeCategory, customCategory);
      when(categoryService.ensureDefaultCategoriesExist(eq(1L), anyString())).thenReturn(categories);

      ResponseEntity<?> response = controller.ensureDefaults(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 403 on access denied")
    void returnsForbiddenOnAccessDenied() {
      when(categoryService.ensureDefaultCategoriesExist(eq(1L), anyString()))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<?> response = controller.ensureDefaults(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("reorderCategories Tests")
  class ReorderCategoriesTests {

    @Test
    @DisplayName("Reorders categories successfully")
    void reordersCategories() {
      SpendingCategoryController.ReorderRequest request = new SpendingCategoryController.ReorderRequest();
      request.categoryIds = Arrays.asList(2L, 1L, 3L);

      List<SpendingCategoryDTO> categories = Arrays.asList(computeCategory, customCategory);
      when(categoryService.reorderCategories(eq(1L), anyString(), eq(request.categoryIds)))
          .thenReturn(categories);

      ResponseEntity<?> response = controller.reorderCategories(1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 403 on access denied")
    void returnsForbiddenOnAccessDenied() {
      SpendingCategoryController.ReorderRequest request = new SpendingCategoryController.ReorderRequest();
      request.categoryIds = Arrays.asList(2L, 1L);

      when(categoryService.reorderCategories(eq(1L), anyString(), eq(request.categoryIds)))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<?> response = controller.reorderCategories(1L, 1L, authentication, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }
}
