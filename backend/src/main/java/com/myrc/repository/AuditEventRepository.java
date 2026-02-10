/*
 * myRC - Audit Event Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.AuditEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for audit event records.
 * Provides read-only query methods — audit records are only created
 * via the AuditService, never modified or deleted through the repository.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

  /**
   * Find all audit events for a given responsibility centre, ordered by most recent first.
   *
   * @param rcId the responsibility centre ID
   * @return list of audit events
   */
  List<AuditEvent> findByRcIdOrderByCreatedAtDesc(Long rcId);

  /**
   * Find all audit events for a given responsibility centre and fiscal year,
   * ordered by most recent first.
   *
   * @param rcId the responsibility centre ID
   * @param fiscalYearId the fiscal year ID
   * @return list of audit events
   */
  List<AuditEvent> findByRcIdAndFiscalYearIdOrderByCreatedAtDesc(Long rcId, Long fiscalYearId);

  /**
   * Find all audit events for a given responsibility centre,
   * including those with no fiscal year context (RC-level actions),
   * ordered by most recent first.
   *
   * @param rcId the responsibility centre ID
   * @return list of audit events
   */
  @Query("SELECT a FROM AuditEvent a WHERE a.rcId = :rcId ORDER BY a.createdAt DESC")
  List<AuditEvent> findAllByRcId(@Param("rcId") Long rcId);

  /**
   * Find all audit events performed by a specific user, ordered by most recent first.
   *
   * @param username the username
   * @return list of audit events
   */
  List<AuditEvent> findByUsernameOrderByCreatedAtDesc(String username);

  /**
   * Find all audit events for a specific action type, ordered by most recent first.
   *
   * @param action the action type
   * @return list of audit events
   */
  List<AuditEvent> findByActionOrderByCreatedAtDesc(String action);
}
