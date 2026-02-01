/*
 * myRC - Money Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for MoneyServiceImpl.
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myrc.dto.MoneyDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.util.Arrays;
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
 * Unit tests for MoneyServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MoneyService Tests")
class MoneyServiceTest {

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MoneyServiceImpl moneyService;

  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFY;
  private Money defaultMoney;
  private Money customMoney;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    testFY = new FiscalYear("FY 2025-2026", "Test Fiscal Year", testRC);
    testFY.setId(1L);

    defaultMoney = new Money("AB", "A-Base", "Default money", testFY, true);
    defaultMoney.setId(1L);
    defaultMoney.setDisplayOrder(0);

    customMoney = new Money("OA", "Operating Allotment", "Custom money", testFY, false);
    customMoney.setId(2L);
    customMoney.setDisplayOrder(1);
  }

  @Nested
  @DisplayName("getMoniesByFiscalYearId Tests")
  class GetMoniesTests {

    @Test
    @DisplayName("Returns monies for RC owner")
    void returnsMoniesForOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.findByFiscalYearId(1L))
          .thenReturn(Arrays.asList(defaultMoney, customMoney));

      List<MoneyDTO> result = moneyService.getMoniesByFiscalYearId(1L, "testuser");

      assertEquals(2, result.size());
      assertEquals("AB", result.get(0).getCode());
      assertEquals("OA", result.get(1).getCode());
    }

    @Test
    @DisplayName("Throws exception when FY not found")
    void throwsWhenFYNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.getMoniesByFiscalYearId(99L, "testuser"));
    }

    @Test
    @DisplayName("Throws exception when user has no access")
    void throwsWhenNoAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.getMoniesByFiscalYearId(1L, "otheruser"));
    }
  }

  @Nested
  @DisplayName("createMoney Tests")
  class CreateMoneyTests {

    @Test
    @DisplayName("Creates money successfully")
    void createsMoney() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.existsByCodeAndFiscalYear("WCF", testFY)).thenReturn(false);
      when(moneyRepository.findMaxDisplayOrderByFiscalYearId(1L)).thenReturn(1);

      ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
      when(moneyRepository.save(moneyCaptor.capture())).thenAnswer(inv -> {
        Money saved = inv.getArgument(0);
        saved.setId(3L);
        return saved;
      });

      MoneyDTO result = moneyService.createMoney(1L, "testuser", "wcf", "Working Capital Fund", "Test");

      assertEquals("WCF", result.getCode());
      assertEquals("Working Capital Fund", result.getName());
      assertEquals(2, moneyCaptor.getValue().getDisplayOrder());
      assertFalse(moneyCaptor.getValue().getIsDefault());
    }

    @Test
    @DisplayName("Throws when code already exists")
    void throwsWhenCodeExists() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.existsByCodeAndFiscalYear("AB", testFY)).thenReturn(true);

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.createMoney(1L, "testuser", "AB", "Duplicate", null));
    }

    @Test
    @DisplayName("Throws when code is empty")
    void throwsWhenCodeEmpty() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.createMoney(1L, "testuser", "", "Test", null));
    }

    @Test
    @DisplayName("Throws when name is empty")
    void throwsWhenNameEmpty() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.createMoney(1L, "testuser", "WCF", "", null));
    }
  }

  @Nested
  @DisplayName("updateMoney Tests")
  class UpdateMoneyTests {

    @Test
    @DisplayName("Updates money successfully")
    void updatesMoney() {
      when(moneyRepository.findById(2L)).thenReturn(Optional.of(customMoney));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.existsByCodeAndFiscalYear("WCF", testFY)).thenReturn(false);
      when(moneyRepository.save(any(Money.class))).thenAnswer(inv -> inv.getArgument(0));

      MoneyDTO result = moneyService.updateMoney(2L, "testuser", "WCF", "Working Capital Fund", "Updated");

      assertEquals("WCF", result.getCode());
      assertEquals("Working Capital Fund", result.getName());
    }

    @Test
    @DisplayName("Cannot change default money code")
    void cannotChangeDefaultCode() {
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(defaultMoney));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.updateMoney(1L, "testuser", "XX", "Changed", null));
    }

    @Test
    @DisplayName("Can update default money name")
    void canUpdateDefaultName() {
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(defaultMoney));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.save(any(Money.class))).thenAnswer(inv -> inv.getArgument(0));

      MoneyDTO result = moneyService.updateMoney(1L, "testuser", null, "Updated A-Base", "New description");

      assertEquals("AB", result.getCode());
      assertEquals("Updated A-Base", result.getName());
    }
  }

  @Nested
  @DisplayName("deleteMoney Tests")
  class DeleteMoneyTests {

    @Test
    @DisplayName("Deletes custom money successfully")
    void deletesCustomMoney() {
      when(moneyRepository.findById(2L)).thenReturn(Optional.of(customMoney));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      moneyService.deleteMoney(2L, "testuser");

      verify(moneyRepository).delete(customMoney);
    }

    @Test
    @DisplayName("Cannot delete default money")
    void cannotDeleteDefault() {
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(defaultMoney));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.deleteMoney(1L, "testuser"));

      verify(moneyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Throws when money not found")
    void throwsWhenNotFound() {
      when(moneyRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> moneyService.deleteMoney(99L, "testuser"));
    }
  }

  @Nested
  @DisplayName("ensureDefaultMoneyExists Tests")
  class EnsureDefaultTests {

    @Test
    @DisplayName("Creates default money when none exists")
    void createsDefaultMoney() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(moneyRepository.findByFiscalYearAndIsDefaultTrue(testFY)).thenReturn(Optional.empty());
      when(moneyRepository.findByCodeAndFiscalYear("AB", testFY)).thenReturn(Optional.empty());

      ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
      when(moneyRepository.save(moneyCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

      moneyService.ensureDefaultMoneyExists(1L);

      assertEquals("AB", moneyCaptor.getValue().getCode());
      assertTrue(moneyCaptor.getValue().getIsDefault());
      assertEquals(0, moneyCaptor.getValue().getDisplayOrder());
    }

    @Test
    @DisplayName("Does nothing when default exists")
    void doesNothingWhenExists() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(moneyRepository.findByFiscalYearAndIsDefaultTrue(testFY)).thenReturn(Optional.of(defaultMoney));

      moneyService.ensureDefaultMoneyExists(1L);

      verify(moneyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Marks existing AB as default")
    void marksExistingAsDefault() {
      Money abWithoutDefault = new Money("AB", "A-Base", null, testFY, false);
      abWithoutDefault.setId(1L);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(moneyRepository.findByFiscalYearAndIsDefaultTrue(testFY)).thenReturn(Optional.empty());
      when(moneyRepository.findByCodeAndFiscalYear("AB", testFY)).thenReturn(Optional.of(abWithoutDefault));
      when(moneyRepository.save(any(Money.class))).thenAnswer(inv -> inv.getArgument(0));

      moneyService.ensureDefaultMoneyExists(1L);

      verify(moneyRepository).save(abWithoutDefault);
      assertTrue(abWithoutDefault.getIsDefault());
    }
  }

  @Nested
  @DisplayName("reorderMonies Tests")
  class ReorderTests {

    @Test
    @DisplayName("Reorders monies successfully")
    void reordersMonies() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(defaultMoney));
      when(moneyRepository.findById(2L)).thenReturn(Optional.of(customMoney));
      when(moneyRepository.save(any(Money.class))).thenAnswer(inv -> inv.getArgument(0));

      moneyService.reorderMonies(1L, "testuser", Arrays.asList(2L, 1L));

      assertEquals(0, customMoney.getDisplayOrder());
      assertEquals(1, defaultMoney.getDisplayOrder());
    }
  }

  @Nested
  @DisplayName("MoneyDTO Tests")
  class DTOTests {

    @Test
    @DisplayName("DTO has correct CAP and OM labels")
    void hasCorrectLabels() {
      MoneyDTO dto = MoneyDTO.fromEntity(defaultMoney);

      assertEquals("AB (CAP)", dto.getCapLabel());
      assertEquals("AB (OM)", dto.getOmLabel());
    }

    @Test
    @DisplayName("fromEntity handles null")
    void fromEntityHandlesNull() {
      MoneyDTO dto = MoneyDTO.fromEntity(null);

      assertEquals(null, dto);
    }
  }
}
