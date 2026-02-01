/*
 * myRC - Category Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.CategoryDTO;
import com.myrc.model.Category;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingType;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CategoryServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  private CategoryServiceImpl categoryService;
  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFY;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    categoryService = new CategoryServiceImpl(
        categoryRepository,
        fiscalYearRepository,
        rcRepository,
        accessRepository,
        userRepository
    );

    // Set up test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFullName("Test User");

    // Set up test RC
    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    // Set up test fiscal year
    testFY = new FiscalYear();
    testFY.setId(1L);
    testFY.setName("FY 2026-2027");
    testFY.setResponsibilityCentre(testRC);

    // Set up test category
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setDescription("Test category description");
    testCategory.setFiscalYear(testFY);
    testCategory.setIsDefault(false);
    testCategory.setDisplayOrder(0);
    testCategory.setFundingType(FundingType.BOTH);
    testCategory.setActive(true);
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(categoryService);
  }

  @Nested
  @DisplayName("getCategoriesByFiscalYearId Tests")
  class GetCategoriesTests {

    @Test
    @DisplayName("Should return categories for fiscal year when user is owner")
    void shouldReturnCategoriesForFiscalYearWhenOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(testCategory));

      List<CategoryDTO> result = categoryService.getCategoriesByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Test Category", result.get(0).getName());
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.getCategoriesByFiscalYearId(999L, "testuser"));
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategories() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList());

      List<CategoryDTO> result = categoryService.getCategoriesByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when user has no access")
    void shouldThrowExceptionWhenNoAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.getCategoriesByFiscalYearId(1L, "otheruser"));
    }
  }

  @Nested
  @DisplayName("getCategoryById Tests")
  class GetCategoryByIdTests {

    @Test
    @DisplayName("Should return category by ID when user is owner")
    void shouldReturnCategoryByIdWhenOwner() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      Optional<CategoryDTO> result = categoryService.getCategoryById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Test Category", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when category not found")
    void shouldReturnEmptyWhenNotFound() {
      when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<CategoryDTO> result = categoryService.getCategoryById(999L, "testuser");

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when user has no access")
    void shouldReturnEmptyWhenNoAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      Optional<CategoryDTO> result = categoryService.getCategoryById(1L, "otheruser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createCategory Tests")
  class CreateCategoryTests {

    @Test
    @DisplayName("Should create category successfully when user is owner")
    void shouldCreateCategorySuccessfullyWhenOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("New Category", testFY)).thenReturn(false);
      when(categoryRepository.getMaxDisplayOrderByFiscalYearId(1L)).thenReturn(0);
      when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

      CategoryDTO result = categoryService.createCategory(1L, "testuser", "New Category", "Description");

      assertNotNull(result);
      verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should create category with funding type")
    void shouldCreateCategoryWithFundingType() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("New Category", testFY)).thenReturn(false);
      when(categoryRepository.getMaxDisplayOrderByFiscalYearId(1L)).thenReturn(0);
      when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

      CategoryDTO result = categoryService.createCategory(1L, "testuser", "New Category", "Description", FundingType.CAP_ONLY);

      assertNotNull(result);
      verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.createCategory(999L, "testuser", "New Category", "Description"));
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowExceptionWhenNameEmpty() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.createCategory(1L, "testuser", "", "Description"));
    }

    @Test
    @DisplayName("Should throw exception when duplicate name")
    void shouldThrowExceptionWhenDuplicateName() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("Test Category", testFY)).thenReturn(true);

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.createCategory(1L, "testuser", "Test Category", "Description"));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.createCategory(1L, "otheruser", "New Category", "Description"));
    }
  }

  @Nested
  @DisplayName("updateCategory Tests")
  class UpdateCategoryTests {

    @Test
    @DisplayName("Should update category successfully when user is owner")
    void shouldUpdateCategorySuccessfullyWhenOwner() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

      CategoryDTO result = categoryService.updateCategory(1L, "testuser", "Updated Name", "Updated Description");

      assertNotNull(result);
      verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenNotFound() {
      when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.updateCategory(999L, "testuser", "Updated Name", "Description"));
    }

    @Test
    @DisplayName("Should throw exception when trying to modify default category")
    void shouldThrowExceptionWhenModifyingDefault() {
      testCategory.setIsDefault(true);
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.updateCategory(1L, "testuser", "Updated Name", "Description"));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.updateCategory(1L, "otheruser", "Updated Name", "Description"));
    }
  }

  @Nested
  @DisplayName("deleteCategory Tests")
  class DeleteCategoryTests {

    @Test
    @DisplayName("Should delete category successfully when user is owner")
    void shouldDeleteCategorySuccessfullyWhenOwner() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      doNothing().when(categoryRepository).delete(testCategory);

      assertDoesNotThrow(() -> categoryService.deleteCategory(1L, "testuser"));
      verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenNotFound() {
      when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.deleteCategory(999L, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when trying to delete default category")
    void shouldThrowExceptionWhenDeletingDefault() {
      testCategory.setIsDefault(true);
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.deleteCategory(1L, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.deleteCategory(1L, "otheruser"));
    }
  }

  @Nested
  @DisplayName("ensureDefaultCategoriesExist Tests")
  class EnsureDefaultCategoriesTests {

    @Test
    @DisplayName("Should create default categories when none exist")
    void shouldCreateDefaultCategoriesWhenNoneExist() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L)).thenReturn(Arrays.asList());
      when(categoryRepository.existsByNameAndFiscalYear(anyString(), eq(testFY))).thenReturn(false);
      when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(testCategory));

      List<CategoryDTO> result = categoryService.ensureDefaultCategoriesExist(1L, "testuser");

      assertNotNull(result);
      verify(categoryRepository, atLeast(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should not create duplicates when defaults already exist")
    void shouldNotCreateDuplicatesWhenDefaultsExist() {
      Category defaultCategory = new Category();
      defaultCategory.setIsDefault(true);
      defaultCategory.setFiscalYear(testFY);
      defaultCategory.setName("Compute");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L)).thenReturn(Arrays.asList(defaultCategory));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(defaultCategory));

      List<CategoryDTO> result = categoryService.ensureDefaultCategoriesExist(1L, "testuser");

      assertNotNull(result);
      verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.ensureDefaultCategoriesExist(999L, "testuser"));
    }
  }

  @Nested
  @DisplayName("reorderCategories Tests")
  class ReorderCategoriesTests {

    @Test
    @DisplayName("Should reorder categories successfully")
    void shouldReorderCategoriesSuccessfully() {
      Category category2 = new Category();
      category2.setId(2L);
      category2.setName("Category 2");
      category2.setFiscalYear(testFY);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));
      when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(testCategory, category2));

      List<CategoryDTO> result = categoryService.reorderCategories(1L, "testuser", Arrays.asList(2L, 1L));

      assertNotNull(result);
      verify(categoryRepository, times(2)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      User rcOwner = new User();
      rcOwner.setId(3L);
      rcOwner.setUsername("owner");
      testRC.setOwner(rcOwner);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          categoryService.reorderCategories(1L, "otheruser", Arrays.asList(1L, 2L)));
    }
  }
}
