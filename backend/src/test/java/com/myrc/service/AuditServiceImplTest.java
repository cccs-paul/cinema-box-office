/*
 * myRC - Audit Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.myrc.dto.AuditEventDTO;
import com.myrc.model.AuditEvent;
import com.myrc.repository.AuditEventRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AuditServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl Tests")
class AuditServiceImplTest {

  @Mock
  private AuditEventRepository auditEventRepository;

  private AuditServiceImpl auditService;

  @BeforeEach
  void setUp() {
    auditService = new AuditServiceImpl(auditEventRepository);
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(auditService);
  }

  private AuditEvent createTestEvent() {
    AuditEvent event = new AuditEvent("testuser", "CREATE_RC", "RESPONSIBILITY_CENTRE");
    event.setRcId(1L);
    event.setRcName("Test RC");
    event.setHttpMethod("POST");
    event.setEndpoint("/responsibility-centres");
    event.setIpAddress("127.0.0.1");
    event.setUserAgent("Mozilla/5.0");
    return event;
  }

  private AuditEvent createSavedEvent(Long id) {
    AuditEvent event = createTestEvent();
    event.setId(id);
    event.setOutcome("PENDING");
    return event;
  }

  @Nested
  @DisplayName("recordEvent Tests")
  class RecordEventTests {

    @Test
    @DisplayName("Should record event with PENDING outcome")
    void testRecordEvent() {
      AuditEvent input = createTestEvent();
      AuditEvent saved = createSavedEvent(1L);

      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(saved);

      AuditEvent result = auditService.recordEvent(input);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("PENDING", result.getOutcome());
      verify(auditEventRepository).save(input);
    }

    @Test
    @DisplayName("Should set outcome to PENDING if null")
    void testRecordEventSetsOutcomeIfNull() {
      AuditEvent input = createTestEvent();
      input.setOutcome(null);
      AuditEvent saved = createSavedEvent(1L);

      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(saved);

      auditService.recordEvent(input);

      assertEquals("PENDING", input.getOutcome());
    }

    @Test
    @DisplayName("Should propagate exception when save fails")
    void testRecordEventFailure() {
      AuditEvent input = createTestEvent();
      when(auditEventRepository.save(any(AuditEvent.class)))
          .thenThrow(new RuntimeException("Database error"));

      assertThrows(RuntimeException.class, () -> auditService.recordEvent(input));
    }
  }

  @Nested
  @DisplayName("markSuccess Tests")
  class MarkSuccessTests {

    @Test
    @DisplayName("Should mark event as SUCCESS")
    void testMarkSuccess() {
      AuditEvent event = createSavedEvent(1L);
      when(auditEventRepository.findById(1L)).thenReturn(Optional.of(event));
      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(event);

      auditService.markSuccess(1L);

      assertEquals("SUCCESS", event.getOutcome());
      verify(auditEventRepository).save(event);
    }

    @Test
    @DisplayName("Should mark event as SUCCESS with entity ID")
    void testMarkSuccessWithEntityId() {
      AuditEvent event = createSavedEvent(1L);
      when(auditEventRepository.findById(1L)).thenReturn(Optional.of(event));
      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(event);

      auditService.markSuccess(1L, 42L);

      assertEquals("SUCCESS", event.getOutcome());
      assertEquals(42L, event.getEntityId());
      verify(auditEventRepository).save(event);
    }

    @Test
    @DisplayName("Should mark event as SUCCESS with entity ID and name")
    void testMarkSuccessWithEntityIdAndName() {
      AuditEvent event = createSavedEvent(1L);
      when(auditEventRepository.findById(1L)).thenReturn(Optional.of(event));
      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(event);

      auditService.markSuccess(1L, 42L, "Test Entity");

      assertEquals("SUCCESS", event.getOutcome());
      assertEquals(42L, event.getEntityId());
      assertEquals("Test Entity", event.getEntityName());
      verify(auditEventRepository).save(event);
    }

    @Test
    @DisplayName("Should mark event as SUCCESS with entity ID and null name — preserves existing name")
    void testMarkSuccessWithEntityIdAndNullName() {
      AuditEvent event = createSavedEvent(1L);
      event.setEntityName("Original Name");
      when(auditEventRepository.findById(1L)).thenReturn(Optional.of(event));
      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(event);

      auditService.markSuccess(1L, 42L, null);

      assertEquals("SUCCESS", event.getOutcome());
      assertEquals(42L, event.getEntityId());
      assertEquals("Original Name", event.getEntityName());
      verify(auditEventRepository).save(event);
    }

    @Test
    @DisplayName("Should handle non-existent event gracefully")
    void testMarkSuccessNonExistent() {
      when(auditEventRepository.findById(999L)).thenReturn(Optional.empty());

      auditService.markSuccess(999L);

      verify(auditEventRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("markFailure Tests")
  class MarkFailureTests {

    @Test
    @DisplayName("Should mark event as FAILURE with error message")
    void testMarkFailure() {
      AuditEvent event = createSavedEvent(1L);
      when(auditEventRepository.findById(1L)).thenReturn(Optional.of(event));
      when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(event);

      auditService.markFailure(1L, "Something went wrong");

      assertEquals("FAILURE", event.getOutcome());
      assertEquals("Something went wrong", event.getErrorMessage());
      verify(auditEventRepository).save(event);
    }

    @Test
    @DisplayName("Should handle non-existent event gracefully")
    void testMarkFailureNonExistent() {
      when(auditEventRepository.findById(999L)).thenReturn(Optional.empty());

      auditService.markFailure(999L, "Error");

      verify(auditEventRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getAuditEventsForRC Tests")
  class GetAuditEventsForRCTests {

    @Test
    @DisplayName("Should return audit events for RC")
    void testGetAuditEventsForRC() {
      AuditEvent event1 = createSavedEvent(1L);
      AuditEvent event2 = createSavedEvent(2L);
      event2.setAction("UPDATE_RC");

      when(auditEventRepository.findByRcIdOrderByCreatedAtDesc(1L))
          .thenReturn(List.of(event1, event2));

      List<AuditEventDTO> result = auditService.getAuditEventsForRC(1L);

      assertEquals(2, result.size());
      assertEquals("CREATE_RC", result.get(0).getAction());
      assertEquals("UPDATE_RC", result.get(1).getAction());
    }

    @Test
    @DisplayName("Should return empty list when no events exist")
    void testGetAuditEventsForRCEmpty() {
      when(auditEventRepository.findByRcIdOrderByCreatedAtDesc(999L))
          .thenReturn(Collections.emptyList());

      List<AuditEventDTO> result = auditService.getAuditEventsForRC(999L);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("getAuditEventsForFiscalYear Tests")
  class GetAuditEventsForFiscalYearTests {

    @Test
    @DisplayName("Should return audit events for specific fiscal year")
    void testGetAuditEventsForFiscalYear() {
      AuditEvent event = createSavedEvent(1L);
      event.setFiscalYearId(10L);
      event.setFiscalYearName("FY 2025");

      when(auditEventRepository.findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(1L, 10L))
          .thenReturn(List.of(event));

      List<AuditEventDTO> result = auditService.getAuditEventsForFiscalYear(1L, 10L);

      assertEquals(1, result.size());
      assertEquals(10L, result.get(0).getFiscalYearId());
    }
  }

  @Nested
  @DisplayName("cloneAuditEventsForRC Tests")
  class CloneAuditEventsForRCTests {

    @Test
    @DisplayName("Should clone all audit events to new RC")
    void testCloneAuditEventsForRC() {
      AuditEvent event1 = createSavedEvent(1L);
      event1.setOutcome("SUCCESS");
      AuditEvent event2 = createSavedEvent(2L);
      event2.setAction("UPDATE_RC");
      event2.setOutcome("SUCCESS");

      when(auditEventRepository.findByRcIdOrderByCreatedAtDesc(1L))
          .thenReturn(List.of(event1, event2));
      when(auditEventRepository.save(any(AuditEvent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      auditService.cloneAuditEventsForRC(1L, 2L, "Cloned RC", "testuser");

      ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
      verify(auditEventRepository, times(2)).save(captor.capture());

      List<AuditEvent> clonedEvents = captor.getAllValues();
      assertEquals(2, clonedEvents.size());

      AuditEvent cloned1 = clonedEvents.get(0);
      assertEquals(2L, cloned1.getRcId());
      assertEquals("Cloned RC", cloned1.getRcName());
      assertEquals(1L, cloned1.getClonedFromAuditId());
      assertEquals("CREATE_RC", cloned1.getAction());

      AuditEvent cloned2 = clonedEvents.get(1);
      assertEquals(2L, cloned2.getRcId());
      assertEquals("UPDATE_RC", cloned2.getAction());
      assertEquals(2L, cloned2.getClonedFromAuditId());
    }

    @Test
    @DisplayName("Should handle empty source events")
    void testCloneAuditEventsForRCEmpty() {
      when(auditEventRepository.findByRcIdOrderByCreatedAtDesc(1L))
          .thenReturn(Collections.emptyList());

      auditService.cloneAuditEventsForRC(1L, 2L, "Cloned RC", "testuser");

      verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }
  }

  @Nested
  @DisplayName("cloneAuditEventsForFiscalYear Tests")
  class CloneAuditEventsForFiscalYearTests {

    @Test
    @DisplayName("Should clone audit events to new fiscal year")
    void testCloneAuditEventsForFiscalYear() {
      AuditEvent event = createSavedEvent(1L);
      event.setFiscalYearId(10L);
      event.setFiscalYearName("Source FY");
      event.setOutcome("SUCCESS");

      when(auditEventRepository.findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(1L, 10L))
          .thenReturn(List.of(event));
      when(auditEventRepository.save(any(AuditEvent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      auditService.cloneAuditEventsForFiscalYear(1L, 10L, 2L, "Target RC", 20L, "Target FY",
          "testuser");

      ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
      verify(auditEventRepository).save(captor.capture());

      AuditEvent cloned = captor.getValue();
      assertEquals(2L, cloned.getRcId());
      assertEquals("Target RC", cloned.getRcName());
      assertEquals(20L, cloned.getFiscalYearId());
      assertEquals("Target FY", cloned.getFiscalYearName());
      assertEquals(1L, cloned.getClonedFromAuditId());
      assertEquals("testuser", cloned.getUsername());
    }

    @Test
    @DisplayName("Should handle empty source events")
    void testCloneAuditEventsForFiscalYearEmpty() {
      when(auditEventRepository.findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(1L, 10L))
          .thenReturn(Collections.emptyList());

      auditService.cloneAuditEventsForFiscalYear(1L, 10L, 2L, "Target RC", 20L, "Target FY",
          "testuser");

      verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }
  }
}
