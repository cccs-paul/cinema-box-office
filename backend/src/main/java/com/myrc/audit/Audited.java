/*
 * myRC - Audited Annotation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method for pre-emptive audit logging.
 *
 * <p>When placed on a REST controller method, the {@link AuditAspect}
 * will intercept the call, record an audit event with outcome=PENDING
 * before the method executes, and then update the outcome to SUCCESS
 * or FAILURE after execution.</p>
 *
 * <p>If the audit record cannot be saved, the method will not execute
 * and an error response will be returned.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

  /**
   * The action being performed (e.g., "CREATE_RC", "DELETE_FY").
   */
  String action();

  /**
   * The entity type being acted upon (e.g., "RESPONSIBILITY_CENTRE", "FISCAL_YEAR").
   */
  String entityType();
}
