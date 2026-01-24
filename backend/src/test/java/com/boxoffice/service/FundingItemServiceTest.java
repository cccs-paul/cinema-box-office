/*
 * myRC - Funding Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boxoffice.dto.FundingItemDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.FundingItem;
import com.boxoffice.model.FundingItem.Status;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.FundingItemRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for FundingItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FundingItemService Tests")
class FundingItemServiceTest {

  @Mock
  private FundingItemRepository fundingItemRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private FundingItemServiceImpl fundingItemService;

  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFiscalYear;
  private FundingItem testFundingItem;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    testFiscalYear = new FiscalYear(
        "FY 2025-2026",
        "Test Fiscal Year",
        testRC
    );
    testFiscalYear.setId(1L);

    testFundingItem = new FundingItem(
        "Test Funding Item",
        "Test description",
        new BigDecimal("10000.00"),
        Status.DRAFT,
        testFiscalYear
    );
    testFundingItem.setId(1L);
  }

  @Test
  @DisplayName("getFundingItemsByFiscalYearId - Returns funding items for owner")
  void getFundingItemsByFiscalYearId_ReturnsForOwner() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fundingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
        .thenReturn(Arrays.asList(testFundingItem));

    List<FundingItemDTO> result = fundingItemService.getFundingItemsByFiscalYearId(1L, "testuser");

    assertEquals(1, result.size());
    assertEquals("Test Funding Item", result.get(0).getName());
  }

  @Test
  @DisplayName("getFundingItemsByFiscalYearId - Returns funding items for user with access")
  void getFundingItemsByFiscalYearId_ReturnsForUserWithAccess() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    RCAccess access = new RCAccess();
    access.setUser(anotherUser);
    access.setResponsibilityCentre(testRC);
    access.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);

    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.of(access));
    when(fundingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
        .thenReturn(Arrays.asList(testFundingItem));

    List<FundingItemDTO> result = fundingItemService.getFundingItemsByFiscalYearId(1L, "anotheruser");

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("getFundingItemsByFiscalYearId - Throws exception when user has no access")
  void getFundingItemsByFiscalYearId_ThrowsWhenNoAccess() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> fundingItemService.getFundingItemsByFiscalYearId(1L, "anotheruser"));
  }

  @Test
  @DisplayName("createFundingItem - Creates funding item for owner")
  void createFundingItem_CreatesForOwner() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fundingItemRepository.existsByNameAndFiscalYear(anyString(), any()))
        .thenReturn(false);
    when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

    FundingItemDTO result = fundingItemService.createFundingItem(
        1L,
        "testuser",
        "Test Funding Item",
        "Test description",
        new BigDecimal("10000.00"),
        "DRAFT",
        "CAD",
        null
    );

    assertNotNull(result);
    assertEquals("Test Funding Item", result.getName());
    verify(fundingItemRepository).save(any(FundingItem.class));
  }

  @Test
  @DisplayName("createFundingItem - Throws exception on duplicate name")
  void createFundingItem_ThrowsOnDuplicateName() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fundingItemRepository.existsByNameAndFiscalYear(anyString(), any()))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> fundingItemService.createFundingItem(
            1L,
            "testuser",
            "Test Funding Item",
            "Test description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        ));
  }

  @Test
  @DisplayName("createFundingItem - Throws exception for user without write access")
  void createFundingItem_ThrowsForReadOnlyUser() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    RCAccess readOnlyAccess = new RCAccess();
    readOnlyAccess.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);
    readOnlyAccess.setUser(anotherUser);
    readOnlyAccess.setResponsibilityCentre(testRC);

    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.of(readOnlyAccess));

    assertThrows(IllegalArgumentException.class,
        () -> fundingItemService.createFundingItem(
            1L,
            "anotheruser",
            "Test Funding Item",
            "Test description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        ));
  }

  @Test
  @DisplayName("updateFundingItem - Updates funding item for owner")
  void updateFundingItem_UpdatesForOwner() {
    when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

    Optional<FundingItemDTO> result = fundingItemService.updateFundingItem(
        1L,
        "testuser",
        "Updated Name",
        "Updated description",
        new BigDecimal("20000.00"),
        "APPROVED",
        "CAD",
        null
    );

    assertTrue(result.isPresent());
    verify(fundingItemRepository).save(any(FundingItem.class));
  }

  @Test
  @DisplayName("deleteFundingItem - Deletes funding item for owner")
  void deleteFundingItem_DeletesForOwner() {
    when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

    fundingItemService.deleteFundingItem(1L, "testuser");

    verify(fundingItemRepository).delete(testFundingItem);
  }

  @Test
  @DisplayName("deleteFundingItem - Throws exception when funding item not found")
  void deleteFundingItem_ThrowsWhenNotFound() {
    when(fundingItemRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> fundingItemService.deleteFundingItem(999L, "testuser"));
  }
}
