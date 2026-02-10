/*
 * myRC - Audit Event DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.dto;

import com.myrc.model.AuditEvent;
import java.time.Instant;

/**
 * Data Transfer Object for audit events.
 * Provides a read-only view of audit data for the API.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
public class AuditEventDTO {

  private Long id;
  private String username;
  private String action;
  private String entityType;
  private Long entityId;
  private String entityName;
  private Long rcId;
  private String rcName;
  private Long fiscalYearId;
  private String fiscalYearName;
  private String parameters;
  private String httpMethod;
  private String endpoint;
  private String userAgent;
  private String ipAddress;
  private String outcome;
  private String errorMessage;
  private Long clonedFromAuditId;
  private Instant createdAt;

  /** Default constructor. */
  public AuditEventDTO() {
  }

  /**
   * Create a DTO from an AuditEvent entity.
   *
   * @param entity the audit event entity
   * @return the DTO
   */
  public static AuditEventDTO fromEntity(AuditEvent entity) {
    AuditEventDTO dto = new AuditEventDTO();
    dto.id = entity.getId();
    dto.username = entity.getUsername();
    dto.action = entity.getAction();
    dto.entityType = entity.getEntityType();
    dto.entityId = entity.getEntityId();
    dto.entityName = entity.getEntityName();
    dto.rcId = entity.getRcId();
    dto.rcName = entity.getRcName();
    dto.fiscalYearId = entity.getFiscalYearId();
    dto.fiscalYearName = entity.getFiscalYearName();
    dto.parameters = entity.getParameters();
    dto.httpMethod = entity.getHttpMethod();
    dto.endpoint = entity.getEndpoint();
    dto.userAgent = entity.getUserAgent();
    dto.ipAddress = entity.getIpAddress();
    dto.outcome = entity.getOutcome();
    dto.errorMessage = entity.getErrorMessage();
    dto.clonedFromAuditId = entity.getClonedFromAuditId();
    dto.createdAt = entity.getCreatedAt();
    return dto;
  }

  // --- Getters ---

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getAction() {
    return action;
  }

  public String getEntityType() {
    return entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getEntityName() {
    return entityName;
  }

  public Long getRcId() {
    return rcId;
  }

  public String getRcName() {
    return rcName;
  }

  public Long getFiscalYearId() {
    return fiscalYearId;
  }

  public String getFiscalYearName() {
    return fiscalYearName;
  }

  public String getParameters() {
    return parameters;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getOutcome() {
    return outcome;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Long getClonedFromAuditId() {
    return clonedFromAuditId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
