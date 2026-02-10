/*
 * myRC - Audit Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.dto.AuditEventDTO;
import com.myrc.dto.ErrorResponse;
import com.myrc.service.AuditService;
import com.myrc.service.RCPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for audit event read-only access.
 *
 * <p>Only users with OWNER access to a responsibility centre can view
 * audit events for that RC. Audit data is read-only — there are no
 * endpoints to create, update, or delete audit records via this controller.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/audit")
@Tag(name = "Audit Events", description = "Read-only audit trail for responsibility centres")
public class AuditController {

  private static final Logger logger = Logger.getLogger(AuditController.class.getName());

  private final AuditService auditService;
  private final RCPermissionService rcPermissionService;

  public AuditController(AuditService auditService, RCPermissionService rcPermissionService) {
    this.auditService = auditService;
    this.rcPermissionService = rcPermissionService;
  }

  /**
   * Get all audit events for a responsibility centre.
   * Requires OWNER access to the RC.
   *
   * @param rcId the responsibility centre ID
   * @param authentication the current user's authentication
   * @return list of audit events ordered by most recent first
   */
  @GetMapping
  @Operation(summary = "Get audit events for an RC",
      description = "Returns all audit events for the specified responsibility centre. "
          + "Requires OWNER access.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Audit events retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "User does not have OWNER access")
  })
  public ResponseEntity<?> getAuditEvents(
      @Parameter(description = "Responsibility Centre ID")
      @PathVariable("rcId") Long rcId,
      Authentication authentication) {

    String username = authentication != null ? authentication.getName() : "default-user";

    // Check OWNER access
    if (!rcPermissionService.isOwner(rcId, username)) {
      return ResponseEntity.status(403)
          .body(new ErrorResponse("Only owners can view audit data", "FORBIDDEN"));
    }

    List<AuditEventDTO> events = auditService.getAuditEventsForRC(rcId);
    return ResponseEntity.ok(events);
  }

  /**
   * Get audit events for a specific fiscal year within an RC.
   * Requires OWNER access to the RC.
   *
   * @param rcId the responsibility centre ID
   * @param fiscalYearId the fiscal year ID to filter by
   * @param authentication the current user's authentication
   * @return list of audit events ordered by most recent first
   */
  @GetMapping("/fiscal-year/{fiscalYearId}")
  @Operation(summary = "Get audit events for a fiscal year",
      description = "Returns audit events for a specific fiscal year. Requires OWNER access.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Audit events retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "User does not have OWNER access")
  })
  public ResponseEntity<?> getAuditEventsForFiscalYear(
      @Parameter(description = "Responsibility Centre ID")
      @PathVariable("rcId") Long rcId,
      @Parameter(description = "Fiscal Year ID")
      @PathVariable("fiscalYearId") Long fiscalYearId,
      Authentication authentication) {

    String username = authentication != null ? authentication.getName() : "default-user";

    // Check OWNER access
    if (!rcPermissionService.isOwner(rcId, username)) {
      return ResponseEntity.status(403)
          .body(new ErrorResponse("Only owners can view audit data", "FORBIDDEN"));
    }

    List<AuditEventDTO> events = auditService.getAuditEventsForFiscalYear(rcId, fiscalYearId);
    return ResponseEntity.ok(events);
  }
}
