/*
 * myRC - Procurement Item Service Tests
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

import com.myrc.dto.ProcurementItemDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.ProcurementItem;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ProcurementItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class ProcurementItemServiceTest {

  @Mock
  private ProcurementItemRepository procurementItemRepository;

  @Mock
  private ProcurementQuoteRepository quoteRepository;

  @Mock
  private ProcurementQuoteFileRepository fileRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CategoryRepository categoryRepository;

  private ProcurementItemServiceImpl service;
  private User testUser;
  private User ownerUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFiscalYear;
  private ProcurementItem testProcurementItem;

  @BeforeEach
  void setUp() {
    service = new ProcurementItemServiceImpl(
        procurementItemRepository,
        quoteRepository,
        fileRepository,
        fiscalYearRepository,
        rcRepository,
        accessRepository,
        userRepository,
        categoryRepository
    );

    ownerUser = new User();
    ownerUser.setId(1L);
    ownerUser.setUsername("owner");
    ownerUser.setEmail("owner@example.com");

    testUser = new User();
    testUser.setId(2L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(ownerUser);

    testFiscalYear = new FiscalYear();
    testFiscalYear.setId(1L);
    testFiscalYear.setName("FY 2026-27");
    testFiscalYear.setResponsibilityCentre(testRC);

    testProcurementItem = new ProcurementItem();
    testProcurementItem.setId(1L);
    testProcurementItem.setPurchaseRequisition("PR-001");
    testProcurementItem.setName("Test Item");
    testProcurementItem.setDescription("Test Description");
    testProcurementItem.setFiscalYear(testFiscalYear);
    testProcurementItem.setStatus(ProcurementItem.Status.DRAFT);
    testProcurementItem.setActive(true);
  }

  /**
   * Helper method to set up owner access mocking.
   */
  private void setupOwnerAccess() {
    when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
  }

  /**
   * Helper method to set up READ_WRITE user access mocking.
   */
  private void setupReadWriteAccess() {
    RCAccess access = new RCAccess();
    access.setUser(testUser);
    access.setResponsibilityCentre(testRC);
    access.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
        .thenReturn(Optional.of(access));
  }

  /**
   * Helper method to set up READ_ONLY user access mocking.
   */
  private void setupReadOnlyAccess() {
    RCAccess access = new RCAccess();
    access.setUser(testUser);
    access.setResponsibilityCentre(testRC);
    access.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
        .thenReturn(Optional.of(access));
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(service);
  }

  @Nested
  @DisplayName("getProcurementItemsByFiscalYearId Tests")
  class GetProcurementItemsTests {

    @Test
    @DisplayName("Should return procurement items when user is owner")
    void shouldReturnProcurementItemsWhenOwner() {
      setupOwnerAccess();
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(1L))
          .thenReturn(Arrays.asList(testProcurementItem));

      List<ProcurementItemDTO> result = service.getProcurementItemsByFiscalYearId(1L, "owner");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("PR-001", result.get(0).getPurchaseRequisition());
    }

    @Test
    @DisplayName("Should return procurement items when user has read access")
    void shouldReturnProcurementItemsWhenReadAccess() {
      setupReadOnlyAccess();
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(1L))
          .thenReturn(Arrays.asList(testProcurementItem));

      List<ProcurementItemDTO> result = service.getProcurementItemsByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception when user has no access")
    void shouldThrowExceptionWhenNoAccess() {
      User noAccessUser = new User();
      noAccessUser.setId(99L);
      noAccessUser.setUsername("noaccess");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
      when(userRepository.findByUsername("noaccess")).thenReturn(Optional.of(noAccessUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, noAccessUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          service.getProcurementItemsByFiscalYearId(1L, "noaccess"));
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          service.getProcurementItemsByFiscalYearId(999L, "owner"));
    }

    @Test
    @DisplayName("Should return empty list when no items exist")
    void shouldReturnEmptyListWhenNoItems() {
      setupOwnerAccess();
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(1L))
          .thenReturn(Collections.emptyList());

      List<ProcurementItemDTO> result = service.getProcurementItemsByFiscalYearId(1L, "owner");

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("getProcurementItemById Tests")
  class GetProcurementItemByIdTests {

    @Test
    @DisplayName("Should return procurement item when found and user has access")
    void shouldReturnProcurementItemWhenFoundAndHasAccess() {
      setupOwnerAccess();
      when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));

      Optional<ProcurementItemDTO> result = service.getProcurementItemById(1L, "owner");

      assertTrue(result.isPresent());
      assertEquals("PR-001", result.get().getPurchaseRequisition());
    }

    @Test
    @DisplayName("Should return empty when item not found")
    void shouldReturnEmptyWhenNotFound() {
      when(procurementItemRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<ProcurementItemDTO> result = service.getProcurementItemById(999L, "owner");

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when user has no access")
    void shouldReturnEmptyWhenNoAccess() {
      User noAccessUser = new User();
      noAccessUser.setId(99L);
      noAccessUser.setUsername("noaccess");

      when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
      when(userRepository.findByUsername("noaccess")).thenReturn(Optional.of(noAccessUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, noAccessUser))
          .thenReturn(Optional.empty());

      // getProcurementItemById returns empty optional when no access, doesn't throw
      Optional<ProcurementItemDTO> result = service.getProcurementItemById(1L, "noaccess");
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("deleteProcurementItem Tests")
  class DeleteProcurementItemTests {

    @Test
    @DisplayName("Should delete (soft) procurement item successfully when owner")
    void shouldDeleteProcurementItemSuccessfully() {
      setupOwnerAccess();
      when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
      when(procurementItemRepository.save(any(ProcurementItem.class))).thenReturn(testProcurementItem);

      assertDoesNotThrow(() -> service.deleteProcurementItem(1L, "owner"));
      verify(procurementItemRepository).save(any(ProcurementItem.class));
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    void shouldThrowExceptionWhenNotFound() {
      when(procurementItemRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          service.deleteProcurementItem(999L, "owner"));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      setupReadOnlyAccess();
      when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));

      assertThrows(IllegalArgumentException.class, () ->
          service.deleteProcurementItem(1L, "testuser"));
    }
  }
}
