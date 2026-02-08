/*
 * myRC - Fiscal Year Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myrc.dto.FiscalYearDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for FiscalYearServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

  @Mock
  private RCPermissionService permissionService;

  @Mock
  private FiscalYearCloneService fiscalYearCloneService;

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

    // By default, grant full access for happy-path tests.
    // Denied-access tests override these with thenReturn(false).
    org.mockito.Mockito.lenient()
        .when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(true);
    org.mockito.Mockito.lenient()
        .when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(true);
    org.mockito.Mockito.lenient()
        .when(permissionService.isOwner(anyLong(), anyString())).thenReturn(true);
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
    when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(false);

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
    when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(false);

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
    when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(false);

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
        true,   // showSearchBox
        true,   // showCategoryFilter
        false,  // groupByCategory
        -2,
        2
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
        true,   // showSearchBox
        true,   // showCategoryFilter
        false,  // groupByCategory
        null,
        null
    );

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("updateDisplaySettings - Throws exception when user is not RC owner")
  void updateDisplaySettings_ThrowsWhenNotOwner() {
    when(permissionService.isOwner(anyLong(), anyString())).thenReturn(false);

    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setUsername("anotheruser");

    RCAccess access = new RCAccess();
    access.setUser(anotherUser);
    access.setResponsibilityCentre(testRC);
    access.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);

    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherUser))
        .thenReturn(Optional.of(access));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.updateDisplaySettings(1L, "anotheruser", true, true, false, null, null));
    assertTrue(ex.getMessage().contains("owner"));
  }

  @Test
  @DisplayName("updateDisplaySettings - Updates on target thresholds")
  void updateDisplaySettings_UpdatesOnTargetThresholds() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.save(any(FiscalYear.class))).thenAnswer(invocation -> {
      FiscalYear fy = invocation.getArgument(0);
      return fy;
    });

    Optional<FiscalYearDTO> result = fiscalYearService.updateDisplaySettings(
        1L,
        "testuser",
        null,   // showSearchBox
        null,   // showCategoryFilter
        null,   // groupByCategory
        -20,
        15
    );

    assertTrue(result.isPresent());
    verify(fiscalYearRepository).save(argThat(fy -> 
        fy.getOnTargetMin().equals(-20) && fy.getOnTargetMax().equals(15)));
  }

  @Test
  @DisplayName("updateDisplaySettings - Clamps on target values to valid range")
  void updateDisplaySettings_ClampsOnTargetValues() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(fiscalYearRepository.save(any(FiscalYear.class))).thenAnswer(invocation -> {
      FiscalYear fy = invocation.getArgument(0);
      return fy;
    });

    // Test with values outside the -100 to +100 range
    Optional<FiscalYearDTO> result = fiscalYearService.updateDisplaySettings(
        1L,
        "testuser",
        null,   // showSearchBox
        null,   // showCategoryFilter
        null,   // groupByCategory
        -150,
        200
    );

    assertTrue(result.isPresent());
    verify(fiscalYearRepository).save(argThat(fy -> 
        fy.getOnTargetMin().equals(-100) && fy.getOnTargetMax().equals(100)));
  }

  // =========== Clone Fiscal Year Tests ===========

  @Test
  @DisplayName("cloneFiscalYear - Clones fiscal year successfully")
  void cloneFiscalYear_ClonesSuccessfully() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(fiscalYearRepository.existsByNameAndResponsibilityCentre(anyString(), any()))
        .thenReturn(false);

    FiscalYear clonedFY = new FiscalYear("FY 2025-2026 (Copy)", "Test Fiscal Year", testRC);
    clonedFY.setId(2L);
    when(fiscalYearCloneService.deepCloneFiscalYear(any(), anyString(), any()))
        .thenReturn(clonedFY);

    FiscalYearDTO result = fiscalYearService.cloneFiscalYear(
        1L, 1L, "testuser", "FY 2025-2026 (Copy)");

    assertNotNull(result);
    assertEquals("FY 2025-2026 (Copy)", result.getName());
    verify(fiscalYearCloneService).deepCloneFiscalYear(
        any(FiscalYear.class), eq("FY 2025-2026 (Copy)"), any(ResponsibilityCentre.class));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws when user has no access")
  void cloneFiscalYear_ThrowsWhenNoAccess() {
    when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(false);

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 1L, "testuser", "FY Copy"));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws when fiscal year not found")
  void cloneFiscalYear_ThrowsWhenFYNotFound() {
    when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 999L, "testuser", "FY Copy"));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws when FY does not belong to RC")
  void cloneFiscalYear_ThrowsWhenFYNotInRC() {
    ResponsibilityCentre otherRC = new ResponsibilityCentre();
    otherRC.setId(99L);
    otherRC.setName("Other RC");
    otherRC.setOwner(testUser);

    FiscalYear otherFY = new FiscalYear("Other FY", "Other", otherRC);
    otherFY.setId(2L);

    when(fiscalYearRepository.findById(2L)).thenReturn(Optional.of(otherFY));

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 2L, "testuser", "FY Copy"));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws on duplicate name")
  void cloneFiscalYear_ThrowsOnDuplicateName() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));
    when(fiscalYearRepository.existsByNameAndResponsibilityCentre(anyString(), any()))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 1L, "testuser", "FY 2025-2026"));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws on null name")
  void cloneFiscalYear_ThrowsOnNullName() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 1L, "testuser", null));
  }

  @Test
  @DisplayName("cloneFiscalYear - Throws on empty name")
  void cloneFiscalYear_ThrowsOnEmptyName() {
    when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFiscalYear));

    assertThrows(IllegalArgumentException.class,
        () -> fiscalYearService.cloneFiscalYear(1L, 1L, "testuser", "   "));
  }
}
