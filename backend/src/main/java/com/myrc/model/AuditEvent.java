/*
 * myRC - Audit Event Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents an audit event record. This entity is fully independent from
 * all other entities — it has no foreign keys or JPA relationships.
 * All references are stored as denormalized values.
 *
 * <p>Audit records are immutable once written. The table is append-only
 * and records cannot be modified or deleted through the application.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The username of the user who performed the action. */
  @Column(name = "username", nullable = false, length = 255)
  private String username;

  /** The action performed (e.g., CREATE_RC, DELETE_FY, UPDATE_FUNDING_ITEM). */
  @Column(name = "action", nullable = false, length = 100)
  private String action;

  /** The type of entity targeted (e.g., RESPONSIBILITY_CENTRE, FISCAL_YEAR). */
  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  /** The ID of the entity targeted. Nullable for create actions before ID is assigned. */
  @Column(name = "entity_id")
  private Long entityId;

  /** Human-readable name/label of the entity at the time of the action. */
  @Column(name = "entity_name", length = 500)
  private String entityName;

  /** The RC ID context (denormalized, no FK). */
  @Column(name = "rc_id")
  private Long rcId;

  /** The RC name at the time of the action. */
  @Column(name = "rc_name", length = 255)
  private String rcName;

  /** The fiscal year ID context (denormalized, no FK). */
  @Column(name = "fiscal_year_id")
  private Long fiscalYearId;

  /** The fiscal year name at the time of the action. */
  @Column(name = "fiscal_year_name", length = 255)
  private String fiscalYearName;

  /** Serialized JSON of the request parameters/payload (excluding file content). */
  @Column(name = "parameters", columnDefinition = "TEXT")
  private String parameters;

  /** The HTTP method used (GET, POST, PUT, DELETE, PATCH). */
  @Column(name = "http_method", length = 10)
  private String httpMethod;

  /** The endpoint path called. */
  @Column(name = "endpoint", length = 500)
  private String endpoint;

  /** The User-Agent header from the browser. */
  @Column(name = "user_agent", length = 1000)
  private String userAgent;

  /** The IP address of the client. */
  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  /** The outcome of the action: PENDING, SUCCESS, or FAILURE. */
  @Column(name = "outcome", nullable = false, length = 20)
  private String outcome = "PENDING";

  /** Error message if the action failed. */
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  /** Source audit event ID if this record was created by cloning. */
  @Column(name = "cloned_from_audit_id")
  private Long clonedFromAuditId;

  /** Timestamp when the audit event was created. */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Default constructor. */
  public AuditEvent() {
  }

  /**
   * Create an audit event with the minimum required fields.
   *
   * @param username the user performing the action
   * @param action the action being performed
   * @param entityType the type of entity being acted upon
   */
  public AuditEvent(String username, String action, String entityType) {
    this.username = username;
    this.action = action;
    this.entityType = entityType;
    this.outcome = "PENDING";
  }

  // --- Getters and Setters ---

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public Long getRcId() {
    return rcId;
  }

  public void setRcId(Long rcId) {
    this.rcId = rcId;
  }

  public String getRcName() {
    return rcName;
  }

  public void setRcName(String rcName) {
    this.rcName = rcName;
  }

  public Long getFiscalYearId() {
    return fiscalYearId;
  }

  public void setFiscalYearId(Long fiscalYearId) {
    this.fiscalYearId = fiscalYearId;
  }

  public String getFiscalYearName() {
    return fiscalYearName;
  }

  public void setFiscalYearName(String fiscalYearName) {
    this.fiscalYearName = fiscalYearName;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getOutcome() {
    return outcome;
  }

  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Long getClonedFromAuditId() {
    return clonedFromAuditId;
  }

  public void setClonedFromAuditId(Long clonedFromAuditId) {
    this.clonedFromAuditId = clonedFromAuditId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "AuditEvent{"
        + "id=" + id
        + ", username='" + username + '\''
        + ", action='" + action + '\''
        + ", entityType='" + entityType + '\''
        + ", entityId=" + entityId
        + ", entityName='" + entityName + '\''
        + ", rcId=" + rcId
        + ", outcome='" + outcome + '\''
        + ", createdAt=" + createdAt
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuditEvent that = (AuditEvent) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
