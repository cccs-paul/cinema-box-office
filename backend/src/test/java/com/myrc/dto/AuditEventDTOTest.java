/*
 * myRC - Audit Event DTO Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.myrc.model.AuditEvent;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuditEventDTO.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@DisplayName("AuditEventDTO Tests")
class AuditEventDTOTest {

  @Test
  @DisplayName("Should create DTO from entity with all fields")
  void testFromEntityAllFields() {
    AuditEvent entity = new AuditEvent("testuser", "CREATE_RC", "RESPONSIBILITY_CENTRE");
    entity.setId(1L);
    entity.setEntityId(42L);
    entity.setEntityName("Test RC");
    entity.setRcId(1L);
    entity.setRcName("Test RC");
    entity.setFiscalYearId(10L);
    entity.setFiscalYearName("FY 2025");
    entity.setParameters("{\"name\":\"test\"}");
    entity.setHttpMethod("POST");
    entity.setEndpoint("/responsibility-centres");
    entity.setUserAgent("Mozilla/5.0");
    entity.setIpAddress("127.0.0.1");
    entity.setOutcome("SUCCESS");
    entity.setErrorMessage(null);
    entity.setClonedFromAuditId(99L);

    AuditEventDTO dto = AuditEventDTO.fromEntity(entity);

    assertEquals(1L, dto.getId());
    assertEquals("testuser", dto.getUsername());
    assertEquals("CREATE_RC", dto.getAction());
    assertEquals("RESPONSIBILITY_CENTRE", dto.getEntityType());
    assertEquals(42L, dto.getEntityId());
    assertEquals("Test RC", dto.getEntityName());
    assertEquals(1L, dto.getRcId());
    assertEquals("Test RC", dto.getRcName());
    assertEquals(10L, dto.getFiscalYearId());
    assertEquals("FY 2025", dto.getFiscalYearName());
    assertEquals("{\"name\":\"test\"}", dto.getParameters());
    assertEquals("POST", dto.getHttpMethod());
    assertEquals("/responsibility-centres", dto.getEndpoint());
    assertEquals("Mozilla/5.0", dto.getUserAgent());
    assertEquals("127.0.0.1", dto.getIpAddress());
    assertEquals("SUCCESS", dto.getOutcome());
    assertNull(dto.getErrorMessage());
    assertEquals(99L, dto.getClonedFromAuditId());
  }

  @Test
  @DisplayName("Should create DTO from entity with minimal fields")
  void testFromEntityMinimalFields() {
    AuditEvent entity = new AuditEvent("admin", "DELETE_FY", "FISCAL_YEAR");
    entity.setId(2L);
    entity.setOutcome("PENDING");

    AuditEventDTO dto = AuditEventDTO.fromEntity(entity);

    assertEquals(2L, dto.getId());
    assertEquals("admin", dto.getUsername());
    assertEquals("DELETE_FY", dto.getAction());
    assertEquals("FISCAL_YEAR", dto.getEntityType());
    assertEquals("PENDING", dto.getOutcome());
    assertNull(dto.getEntityId());
    assertNull(dto.getEntityName());
    assertNull(dto.getRcId());
    assertNull(dto.getRcName());
    assertNull(dto.getFiscalYearId());
    assertNull(dto.getFiscalYearName());
    assertNull(dto.getParameters());
    assertNull(dto.getHttpMethod());
    assertNull(dto.getEndpoint());
    assertNull(dto.getUserAgent());
    assertNull(dto.getIpAddress());
    assertNull(dto.getErrorMessage());
    assertNull(dto.getClonedFromAuditId());
  }

  @Test
  @DisplayName("Should create DTO from entity with failure outcome")
  void testFromEntityFailure() {
    AuditEvent entity = new AuditEvent("testuser", "UPDATE_FUNDING_ITEM", "FUNDING_ITEM");
    entity.setId(3L);
    entity.setOutcome("FAILURE");
    entity.setErrorMessage("Validation error: amount must be positive");
    entity.setRcId(5L);
    entity.setFiscalYearId(15L);

    AuditEventDTO dto = AuditEventDTO.fromEntity(entity);

    assertEquals("FAILURE", dto.getOutcome());
    assertEquals("Validation error: amount must be positive", dto.getErrorMessage());
    assertEquals(5L, dto.getRcId());
    assertEquals(15L, dto.getFiscalYearId());
  }

  @Test
  @DisplayName("Should have default constructor")
  void testDefaultConstructor() {
    AuditEventDTO dto = new AuditEventDTO();
    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getUsername());
    assertNull(dto.getAction());
  }
}
