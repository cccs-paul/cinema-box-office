/*
 * myRC - Audit Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.AuditEventDTO;
import com.myrc.model.AuditEvent;
import com.myrc.repository.AuditEventRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of AuditService that persists audit events
 * to the local database.
 *
 * <p>In the future, this implementation can be extended or replaced
 * with one that additionally calls an external audit REST API before
 * or after persisting locally.</p>
 *
 * <p>Audit recording uses {@code REQUIRES_NEW} transaction propagation
 * to ensure audit records are committed independently of the main
 * business transaction. This guarantees that even if the business action
 * fails and rolls back, the audit record (with FAILURE outcome) persists.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

  private final AuditEventRepository auditEventRepository;

  public AuditServiceImpl(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AuditEvent recordEvent(AuditEvent event) {
    if (event.getOutcome() == null) {
      event.setOutcome("PENDING");
    }
    AuditEvent saved = auditEventRepository.save(event);
    logger.debug("Recorded audit event: {} {} on {} (ID: {})",
        saved.getAction(), saved.getEntityType(), saved.getEntityName(), saved.getId());

    // Future extension point: call external audit REST API here
    // externalAuditClient.recordEvent(saved);

    return saved;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSuccess(Long auditEventId) {
    Optional<AuditEvent> eventOpt = auditEventRepository.findById(auditEventId);
    if (eventOpt.isPresent()) {
      AuditEvent event = eventOpt.get();
      event.setOutcome("SUCCESS");
      auditEventRepository.save(event);
      logger.debug("Audit event {} marked as SUCCESS", auditEventId);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSuccess(Long auditEventId, Long entityId) {
    Optional<AuditEvent> eventOpt = auditEventRepository.findById(auditEventId);
    if (eventOpt.isPresent()) {
      AuditEvent event = eventOpt.get();
      event.setOutcome("SUCCESS");
      event.setEntityId(entityId);
      auditEventRepository.save(event);
      logger.debug("Audit event {} marked as SUCCESS with entityId {}", auditEventId, entityId);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSuccess(Long auditEventId, Long entityId, String entityName) {
    Optional<AuditEvent> eventOpt = auditEventRepository.findById(auditEventId);
    if (eventOpt.isPresent()) {
      AuditEvent event = eventOpt.get();
      event.setOutcome("SUCCESS");
      event.setEntityId(entityId);
      if (entityName != null) {
        event.setEntityName(entityName);
      }
      auditEventRepository.save(event);
      logger.debug("Audit event {} marked as SUCCESS with entityId {} entityName '{}'",
          auditEventId, entityId, entityName);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailure(Long auditEventId, String errorMessage) {
    Optional<AuditEvent> eventOpt = auditEventRepository.findById(auditEventId);
    if (eventOpt.isPresent()) {
      AuditEvent event = eventOpt.get();
      event.setOutcome("FAILURE");
      event.setErrorMessage(errorMessage);
      auditEventRepository.save(event);
      logger.debug("Audit event {} marked as FAILURE: {}", auditEventId, errorMessage);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditEventDTO> getAuditEventsForRC(Long rcId) {
    List<AuditEvent> events = auditEventRepository.findByRcIdOrderByCreatedAtDesc(rcId);
    return events.stream()
        .map(AuditEventDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditEventDTO> getAuditEventsForFiscalYear(Long rcId, Long fiscalYearId) {
    List<AuditEvent> events =
        auditEventRepository.findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(rcId, fiscalYearId);
    return events.stream()
        .map(AuditEventDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void cloneAuditEventsForRC(Long sourceRcId, Long targetRcId, String targetRcName,
      String username) {
    List<AuditEvent> sourceEvents = auditEventRepository.findByRcIdOrderByCreatedAtDesc(sourceRcId);
    logger.info("Cloning {} audit events from RC {} to RC {}", sourceEvents.size(),
        sourceRcId, targetRcId);

    for (AuditEvent source : sourceEvents) {
      AuditEvent cloned = cloneAuditEvent(source, targetRcId, targetRcName, null, null);
      auditEventRepository.save(cloned);
    }
  }

  @Override
  public void cloneAuditEventsForFiscalYear(Long sourceRcId, Long sourceFiscalYearId,
      Long targetRcId, String targetRcName,
      Long targetFiscalYearId, String targetFiscalYearName,
      String username) {
    List<AuditEvent> sourceEvents =
        auditEventRepository.findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(
            sourceRcId, sourceFiscalYearId);
    logger.info("Cloning {} audit events from FY {} to FY {}", sourceEvents.size(),
        sourceFiscalYearId, targetFiscalYearId);

    for (AuditEvent source : sourceEvents) {
      AuditEvent cloned = cloneAuditEvent(source, targetRcId, targetRcName,
          targetFiscalYearId, targetFiscalYearName);
      auditEventRepository.save(cloned);
    }
  }

  /**
   * Create a clone of an audit event with updated RC/FY context.
   *
   * @param source the source audit event
   * @param targetRcId the target RC ID
   * @param targetRcName the target RC name
   * @param targetFyId the target FY ID (null to preserve original)
   * @param targetFyName the target FY name (null to preserve original)
   * @return the cloned audit event (not yet persisted)
   */
  private AuditEvent cloneAuditEvent(AuditEvent source, Long targetRcId, String targetRcName,
      Long targetFyId, String targetFyName) {
    AuditEvent cloned = new AuditEvent();
    cloned.setUsername(source.getUsername());
    cloned.setAction(source.getAction());
    cloned.setEntityType(source.getEntityType());
    cloned.setEntityId(source.getEntityId());
    cloned.setEntityName(source.getEntityName());
    cloned.setRcId(targetRcId);
    cloned.setRcName(targetRcName);
    cloned.setFiscalYearId(targetFyId != null ? targetFyId : source.getFiscalYearId());
    cloned.setFiscalYearName(targetFyName != null ? targetFyName : source.getFiscalYearName());
    cloned.setParameters(source.getParameters());
    cloned.setHttpMethod(source.getHttpMethod());
    cloned.setEndpoint(source.getEndpoint());
    cloned.setUserAgent(source.getUserAgent());
    cloned.setIpAddress(source.getIpAddress());
    cloned.setOutcome(source.getOutcome());
    cloned.setErrorMessage(source.getErrorMessage());
    cloned.setClonedFromAuditId(source.getId());
    return cloned;
  }
}
