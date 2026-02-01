/*
 * myRC - Spending Category Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for SpendingCategoryServiceImpl.
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myrc.dto.SpendingCategoryDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingCategory;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for SpendingCategoryServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpendingCategoryService Tests")
class SpendingCategoryServiceTest {

  @Mock
  private SpendingCategoryRepository categoryRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SpendingCategoryServiceImpl categoryService;

  private User testUser;
  private User otherUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFY;
  private SpendingCategory computeCategory;
  private SpendingCategory gpuCategory;
  private SpendingCategory customCategory;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    otherUser = new User();
    otherUser.setId(2L);
    otherUser.setUsername("otheruser");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    testFY = new FiscalYear("FY 2025-2026", "Test Fiscal Year", testRC);
    testFY.setId(1L);

    computeCategory = new SpendingCategory("Compute", "Computing infrastructure and services", testFY, true);
    computeCategory.setId(1L);
    computeCategory.setDisplayOrder(0);

    gpuCategory = new SpendingCategory("GPUs", "Graphics Processing Units for AI/ML and rendering", testFY, true);
    gpuCategory.setId(2L);
    gpuCategory.setDisplayOrder(1);

    customCategory = new SpendingCategory("Custom Category", "A custom spending category", testFY, false);
    customCategory.setId(3L);
    customCategory.setDisplayOrder(2);
  }

  @Nested
  @DisplayName("getCategoriesByFiscalYearId Tests")
  class GetCategoriesTests {

    @Test
    @DisplayName("Returns categories for RC owner")
    void returnsCategoriesForOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(computeCategory, gpuCategory, customCategory));

      List<SpendingCategoryDTO> result = categoryService.getCategoriesByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(3, result.size());
      assertEquals("Compute", result.get(0).getName());
      assertEquals("GPUs", result.get(1).getName());
      assertEquals("Custom Category", result.get(2).getName());
    }

    @Test
    @DisplayName("Returns categories for user with access")
    void returnsCategoriesForUserWithAccess() {
      testRC.setOwner(otherUser);
      RCAccess access = new RCAccess(testRC, testUser, RCAccess.AccessLevel.READ_ONLY);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(access));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(computeCategory, gpuCategory));

      List<SpendingCategoryDTO> result = categoryService.getCategoriesByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Throws exception when fiscal year not found")
    void throwsWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.getCategoriesByFiscalYearId(99L, "testuser"));

      assertEquals("Fiscal Year not found", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when user has no access")
    void throwsWhenUserHasNoAccess() {
      testRC.setOwner(otherUser);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.getCategoriesByFiscalYearId(1L, "testuser"));

      assertEquals("User does not have access to this Responsibility Centre", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("getCategoryById Tests")
  class GetCategoryByIdTests {

    @Test
    @DisplayName("Returns category for RC owner")
    void returnsCategoryForOwner() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      Optional<SpendingCategoryDTO> result = categoryService.getCategoryById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Compute", result.get().getName());
      assertTrue(result.get().getIsDefault());
    }

    @Test
    @DisplayName("Returns empty when category not found")
    void returnsEmptyWhenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

      Optional<SpendingCategoryDTO> result = categoryService.getCategoryById(99L, "testuser");

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when user has no access")
    void returnsEmptyWhenNoAccess() {
      testRC.setOwner(otherUser);

      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.empty());

      Optional<SpendingCategoryDTO> result = categoryService.getCategoryById(1L, "testuser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createCategory Tests")
  class CreateCategoryTests {

    @Test
    @DisplayName("Creates category successfully for RC owner")
    void createsCategorySuccessfully() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("New Category", testFY)).thenReturn(false);
      when(categoryRepository.getMaxDisplayOrderByFiscalYearId(1L)).thenReturn(2);
      when(categoryRepository.save(any(SpendingCategory.class))).thenAnswer(invocation -> {
        SpendingCategory saved = invocation.getArgument(0);
        saved.setId(10L);
        return saved;
      });

      SpendingCategoryDTO result = categoryService.createCategory(1L, "testuser", "New Category", "New description");

      assertNotNull(result);
      assertEquals("New Category", result.getName());
      assertEquals("New description", result.getDescription());
      assertFalse(result.getIsDefault());

      ArgumentCaptor<SpendingCategory> captor = ArgumentCaptor.forClass(SpendingCategory.class);
      verify(categoryRepository).save(captor.capture());
      assertEquals(3, captor.getValue().getDisplayOrder());
    }

    @Test
    @DisplayName("Throws exception when category name already exists")
    void throwsWhenNameExists() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("Compute", testFY)).thenReturn(true);

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.createCategory(1L, "testuser", "Compute", "Duplicate"));

      assertEquals("A category with this name already exists for this Fiscal Year", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when name is empty")
    void throwsWhenNameEmpty() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.createCategory(1L, "testuser", "", "Description"));

      assertEquals("Category name is required", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when user has no write access")
    void throwsWhenNoWriteAccess() {
      testRC.setOwner(otherUser);
      RCAccess readOnlyAccess = new RCAccess(testRC, testUser, RCAccess.AccessLevel.READ_ONLY);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(readOnlyAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.createCategory(1L, "testuser", "New Category", "Description"));

      assertEquals("User does not have write access to this Responsibility Centre", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("updateCategory Tests")
  class UpdateCategoryTests {

    @Test
    @DisplayName("Updates custom category name and description")
    void updatesCustomCategory() {
      when(categoryRepository.findById(3L)).thenReturn(Optional.of(customCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.existsByNameAndFiscalYear("Updated Name", testFY)).thenReturn(false);
      when(categoryRepository.save(any(SpendingCategory.class))).thenReturn(customCategory);

      SpendingCategoryDTO result = categoryService.updateCategory(3L, "testuser", "Updated Name", "Updated description");

      assertNotNull(result);
      verify(categoryRepository).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Cannot change name of default category")
    void cannotChangeDefaultCategoryName() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.updateCategory(1L, "testuser", "New Name", null));

      assertEquals("Cannot change the name of a default category", exception.getMessage());
    }

    @Test
    @DisplayName("Can update description of default category")
    void canUpdateDefaultCategoryDescription() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.save(any(SpendingCategory.class))).thenReturn(computeCategory);

      SpendingCategoryDTO result = categoryService.updateCategory(1L, "testuser", null, "Updated description");

      assertNotNull(result);
      verify(categoryRepository).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Throws exception when category not found")
    void throwsWhenCategoryNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.updateCategory(99L, "testuser", "Name", "Description"));

      assertEquals("Category not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("deleteCategory Tests")
  class DeleteCategoryTests {

    @Test
    @DisplayName("Deletes custom category successfully")
    void deletesCustomCategory() {
      when(categoryRepository.findById(3L)).thenReturn(Optional.of(customCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      categoryService.deleteCategory(3L, "testuser");

      verify(categoryRepository).delete(customCategory);
    }

    @Test
    @DisplayName("Cannot delete default category")
    void cannotDeleteDefaultCategory() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.deleteCategory(1L, "testuser"));

      assertEquals("Cannot delete a default category", exception.getMessage());
      verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Throws exception when category not found")
    void throwsWhenCategoryNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.deleteCategory(99L, "testuser"));

      assertEquals("Category not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("ensureDefaultCategoriesExist Tests")
  class EnsureDefaultCategoriesTests {

    @Test
    @DisplayName("Creates default categories when none exist")
    void createsDefaultCategoriesWhenNoneExist() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L)).thenReturn(Collections.emptyList());
      when(categoryRepository.existsByNameAndFiscalYear(anyString(), eq(testFY))).thenReturn(false);
      when(categoryRepository.save(any(SpendingCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Collections.emptyList());

      categoryService.ensureDefaultCategoriesExist(1L, "testuser");

      // Should save 6 default categories
      verify(categoryRepository, times(6)).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Does not create categories when defaults already exist")
    void doesNotCreateWhenDefaultsExist() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L))
          .thenReturn(Arrays.asList(computeCategory, gpuCategory));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(computeCategory, gpuCategory));

      categoryService.ensureDefaultCategoriesExist(1L, "testuser");

      verify(categoryRepository, never()).save(any(SpendingCategory.class));
    }
  }

  @Nested
  @DisplayName("initializeDefaultCategories Tests")
  class InitializeDefaultCategoriesTests {

    @Test
    @DisplayName("Creates default categories for new fiscal year")
    void createsDefaultCategoriesForNewFY() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L)).thenReturn(Collections.emptyList());
      when(categoryRepository.existsByNameAndFiscalYear(anyString(), eq(testFY))).thenReturn(false);
      when(categoryRepository.save(any(SpendingCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

      categoryService.initializeDefaultCategories(1L);

      verify(categoryRepository, times(6)).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Does nothing when fiscal year not found")
    void doesNothingWhenFYNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      categoryService.initializeDefaultCategories(99L);

      verify(categoryRepository, never()).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Does not duplicate categories when defaults already exist")
    void doesNotDuplicateCategories() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(categoryRepository.findDefaultCategoriesByFiscalYearId(1L))
          .thenReturn(Arrays.asList(computeCategory, gpuCategory));

      categoryService.initializeDefaultCategories(1L);

      verify(categoryRepository, never()).save(any(SpendingCategory.class));
    }
  }

  @Nested
  @DisplayName("reorderCategories Tests")
  class ReorderCategoriesTests {

    @Test
    @DisplayName("Reorders categories successfully")
    void reordersCategoriesSuccessfully() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(computeCategory));
      when(categoryRepository.findById(2L)).thenReturn(Optional.of(gpuCategory));
      when(categoryRepository.findById(3L)).thenReturn(Optional.of(customCategory));
      when(categoryRepository.save(any(SpendingCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(1L))
          .thenReturn(Arrays.asList(gpuCategory, computeCategory, customCategory));

      List<Long> newOrder = Arrays.asList(2L, 1L, 3L);
      List<SpendingCategoryDTO> result = categoryService.reorderCategories(1L, "testuser", newOrder);

      assertNotNull(result);
      verify(categoryRepository, times(3)).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("Throws exception when user has no write access")
    void throwsWhenNoWriteAccess() {
      testRC.setOwner(otherUser);
      RCAccess readOnlyAccess = new RCAccess(testRC, testUser, RCAccess.AccessLevel.READ_ONLY);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(readOnlyAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> categoryService.reorderCategories(1L, "testuser", Arrays.asList(1L, 2L)));

      assertEquals("User does not have write access to this Responsibility Centre", exception.getMessage());
    }
  }
}
