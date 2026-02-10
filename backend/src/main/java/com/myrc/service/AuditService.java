/*
 * myRC - Audit Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.AuditEventDTO;
import com.myrc.model.AuditEvent;
import java.util.List;

/**
 * Service interface for audit event management.
 *
 * <p>This interface defines the contract for recording and querying audit events.
 * The default implementation persists to the local database. In the future,
 * an alternative implementation may additionally call an external audit REST API.</p>
 *
 * <p>Audit recording is <strong>pre-emptive</strong>: the audit event is persisted
 * before the actual action is executed. If the audit insert fails, the action
 * must not proceed.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
public interface AuditService {

  /**
   * Record an audit event pre-emptively (before the action is executed).
   * The event is saved with outcome=PENDING.
   *
   * @param event the audit event to record
   * @return the persisted audit event with its generated ID
   * @throws RuntimeException if the audit record cannot be saved
   */
  AuditEvent recordEvent(AuditEvent event);

  /**
   * Mark an audit event as successfully completed.
   *
   * @param auditEventId the ID of the audit event to mark as successful
   */
  void markSuccess(Long auditEventId);

  /**
   * Mark an audit event as successfully completed and update the entity ID
   * (used when the entity was just created and the ID is now known).
   *
   * @param auditEventId the ID of the audit event
   * @param entityId the newly created entity's ID
   */
  void markSuccess(Long auditEventId, Long entityId);

  /**
   * Mark an audit event as successfully completed and update the entity ID and name
   * (used when the entity was just created and the ID/name are now known).
   *
   * @param auditEventId the ID of the audit event
   * @param entityId the newly created entity's ID
   * @param entityName the newly created entity's name
   */
  void markSuccess(Long auditEventId, Long entityId, String entityName);

  /**
   * Mark an audit event as failed with an error message.
   *
   * @param auditEventId the ID of the audit event to mark as failed
   * @param errorMessage the error description
   */
  void markFailure(Long auditEventId, String errorMessage);

  /**
   * Get all audit events for a responsibility centre.
   * Only accessible by users with OWNER access to the RC.
   *
   * @param rcId the responsibility centre ID
   * @return list of audit event DTOs ordered by most recent first
   */
  List<AuditEventDTO> getAuditEventsForRC(Long rcId);

  /**
   * Get all audit events for a specific fiscal year within an RC.
   *
   * @param rcId the responsibility centre ID
   * @param fiscalYearId the fiscal year ID
   * @return list of audit event DTOs ordered by most recent first
   */
  List<AuditEventDTO> getAuditEventsForFiscalYear(Long rcId, Long fiscalYearId);

  /**
   * Clone all audit events associated with a source RC to a target RC.
   * Used during RC cloning to preserve audit trail.
   *
   * @param sourceRcId the source RC ID
   * @param targetRcId the target RC ID
   * @param targetRcName the target RC name
   * @param username the user performing the clone
   */
  void cloneAuditEventsForRC(Long sourceRcId, Long targetRcId, String targetRcName,
      String username);

  /**
   * Clone all audit events associated with a source fiscal year to a target fiscal year.
   * Used during FY cloning to preserve audit trail.
   *
   * @param sourceRcId the source RC ID
   * @param sourceFiscalYearId the source FY ID
   * @param targetRcId the target RC ID
   * @param targetRcName the target RC name
   * @param targetFiscalYearId the target FY ID
   * @param targetFiscalYearName the target FY name
   * @param username the user performing the clone
   */
  void cloneAuditEventsForFiscalYear(Long sourceRcId, Long sourceFiscalYearId,
      Long targetRcId, String targetRcName,
      Long targetFiscalYearId, String targetFiscalYearName,
      String username);
}
