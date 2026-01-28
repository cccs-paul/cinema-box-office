/*
 * myRC - Fiscal Year Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

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
 * Unit tests for FiscalYearServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FiscalYearService Tests")
class FiscalYearServiceTest {

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MoneyService moneyService;

  @Mock
  private SpendingCategoryService spendingCategoryService;

  @Mock
  private CategoryService categoryService;

  @InjectMocks
  private FiscalYearServiceImpl fiscalYearService;

  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFiscalYear;

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
  }

  @Test
  @DisplayName("getFiscalYearsByRCId - Returns fiscal years for owner")
  void getFiscalYearsByRCId_ReturnsForOwner() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.findByResponsibilityCentreId(1L))
        .thenReturn(Arrays.asList(testFiscalYear));

    List<FiscalYearDTO> result = fiscalYearService.getFiscalYearsByRCId(1L, "testuser");

    assertEquals(1, result.size());
    assertEquals("FY 2025-2026", result.get(0).getName());
  }

  @Test
  @DisplayName("getFiscalYearsByRCId - Returns fiscal years for user with access")
  void getFiscalYearsByRCId_ReturnsForUserWithAccess() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    RCAccess access = new RCAccess();
    access.setUser(anotherUser);
    access.setResponsibilityCentre(testRC);
    access.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);

    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.of(access));
    when(fiscalYearRepository.findByResponsibilityCentreId(1L))
        .thenReturn(Arrays.asList(testFiscalYear));

    List<FiscalYearDTO> result = fiscalYearService.getFiscalYearsByRCId(1L, "anotheruser");

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("getFiscalYearsByRCId - Throws exception when user has no access")
  void getFiscalYearsByRCId_ThrowsWhenNoAccess() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.getFiscalYearsByRCId(1L, "anotheruser"));
  }

  @Test
  @DisplayName("getFiscalYearById - Returns fiscal year for authorized user")
  void getFiscalYearById_ReturnsForAuthorizedUser() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

    Optional<FiscalYearDTO> result = fiscalYearService.getFiscalYearById(1L, "testuser");

    assertTrue(result.isPresent());
    assertEquals("FY 2025-2026", result.get().getName());
  }

  @Test
  @DisplayName("getFiscalYearById - Returns empty for unauthorized user")
  void getFiscalYearById_ReturnsEmptyForUnauthorizedUser() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.empty());

    Optional<FiscalYearDTO> result = fiscalYearService.getFiscalYearById(1L, "anotheruser");

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("createFiscalYear - Creates fiscal year for owner")
  void createFiscalYear_CreatesForOwner() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.existsByNameAndResponsibilityCentre(anyString(), any()))
        .thenReturn(false);
    when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(testFiscalYear);
    doNothing().when(moneyService).ensureDefaultMoneyExists(anyLong());

    FiscalYearDTO result = fiscalYearService.createFiscalYear(
        1L,
        "testuser",
        "FY 2025-2026",
        "Test Fiscal Year"
    );

    assertNotNull(result);
    assertEquals("FY 2025-2026", result.getName());
    verify(fiscalYearRepository).save(any(FiscalYear.class));
  }

  @Test
  @DisplayName("createFiscalYear - Throws exception on duplicate name")
  void createFiscalYear_ThrowsOnDuplicateName() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.existsByNameAndResponsibilityCentre(anyString(), any()))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.createFiscalYear(
            1L,
            "testuser",
            "FY 2025-2026",
            "Test Fiscal Year"
        ));
  }

  @Test
  @DisplayName("createFiscalYear - Throws exception for user without write access")
  void createFiscalYear_ThrowsForReadOnlyUser() {
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    RCAccess readOnlyAccess = new RCAccess();
    readOnlyAccess.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);
    readOnlyAccess.setUser(anotherUser);
    readOnlyAccess.setResponsibilityCentre(testRC);

    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.of(readOnlyAccess));

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.createFiscalYear(
            1L,
            "anotheruser",
            "FY 2025-2026",
            "Test Fiscal Year"
        ));
  }

  @Test
  @DisplayName("updateFiscalYear - Updates fiscal year for owner")
  void updateFiscalYear_UpdatesForOwner() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(testFiscalYear);

    Optional<FiscalYearDTO> result = fiscalYearService.updateFiscalYear(
        1L,
        "testuser",
        "FY 2025-2026 Updated",
        "Updated description"
    );

    assertTrue(result.isPresent());
    verify(fiscalYearRepository).save(any(FiscalYear.class));
  }

  @Test
  @DisplayName("deleteFiscalYear - Deletes fiscal year for owner")
  void deleteFiscalYear_DeletesForOwner() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

    fiscalYearService.deleteFiscalYear(1L, "testuser");

    verify(fiscalYearRepository).delete(testFiscalYear);
  }

  @Test
  @DisplayName("deleteFiscalYear - Throws exception when fiscal year not found")
  void deleteFiscalYear_ThrowsWhenNotFound() {
    when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.deleteFiscalYear(999L, "testuser"));
  }

  @Test
  @DisplayName("updateDisplaySettings - Updates display settings for owner")
  void updateDisplaySettings_UpdatesForOwner() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(testFiscalYear);

    Optional<FiscalYearDTO> result = fiscalYearService.updateDisplaySettings(
        1L,
        "testuser",
        true,
        false
    );

    assertTrue(result.isPresent());
    verify(fiscalYearRepository).save(any(FiscalYear.class));
  }

  @Test
  @DisplayName("updateDisplaySettings - Returns empty when fiscal year not found")
  void updateDisplaySettings_ReturnsEmptyWhenNotFound() {
    when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<FiscalYearDTO> result = fiscalYearService.updateDisplaySettings(
        999L,
        "testuser",
        true,
        false
    );

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("updateDisplaySettings - Throws exception when user has no write access")
  void updateDisplaySettings_ThrowsWhenNoWriteAccess() {
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

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.updateDisplaySettings(1L, "anotheruser", true, false));
  }
}
