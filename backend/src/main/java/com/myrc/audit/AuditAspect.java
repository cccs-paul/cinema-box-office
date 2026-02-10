/*
 * myRC - Audit Aspect
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.myrc.dto.ErrorResponse;
import com.myrc.model.AuditEvent;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.security.Principal;

/**
 * AOP aspect that intercepts methods annotated with {@link Audited}
 * to provide pre-emptive audit logging.
 *
 * <p>The aspect performs the following steps:</p>
 * <ol>
 *   <li>Extracts request context (username, HTTP method, endpoint, User-Agent, IP)</li>
 *   <li>Extracts path variables (rcId, fiscalYearId, entity IDs) and request body parameters</li>
 *   <li>Creates and persists an audit event with outcome=PENDING</li>
 *   <li>If audit insert fails, returns an error response without executing the action</li>
 *   <li>Executes the actual controller method</li>
 *   <li>Updates the audit event outcome to SUCCESS or FAILURE</li>
 * </ol>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Aspect
@Component
public class AuditAspect {

  private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

  private final AuditService auditService;
  private final ResponsibilityCentreRepository rcRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ObjectMapper objectMapper;

  public AuditAspect(AuditService auditService,
      ResponsibilityCentreRepository rcRepository,
      FiscalYearRepository fiscalYearRepository) {
    this.auditService = auditService;
    this.rcRepository = rcRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  /**
   * Around advice for methods annotated with {@link Audited}.
   * Records audit event before execution, then updates outcome after.
   */
  @Around("@annotation(audited)")
  public Object auditAction(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {

    // Extract HTTP request context
    HttpServletRequest request = getCurrentRequest();
    String username = extractUsername(request);
    String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
    String endpoint = request != null ? request.getRequestURI() : "UNKNOWN";
    String userAgent = request != null ? request.getHeader("User-Agent") : null;
    String ipAddress = request != null ? extractClientIp(request) : null;

    // Extract path variables and parameters from method arguments
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Parameter[] parameters = signature.getMethod().getParameters();
    Object[] args = joinPoint.getArgs();

    Long rcId = null;
    Long fiscalYearId = null;
    Long entityId = null;
    String entityName = null;
    String rcName = null;
    String fyName = null;
    Map<String, Object> paramMap = new LinkedHashMap<>();

    for (int i = 0; i < parameters.length; i++) {
      Object arg = args[i];
      if (arg == null) continue;

      String paramName = parameters[i].getName();

      // Extract path variables
      PathVariable pathVar = parameters[i].getAnnotation(PathVariable.class);
      if (pathVar != null) {
        String varName = pathVar.value().isEmpty() ? paramName : pathVar.value();
        if ("rcId".equals(varName) && arg instanceof Long) {
          rcId = (Long) arg;
        } else if (("fyId".equals(varName) || "fiscalYearId".equals(varName)) && arg instanceof Long) {
          fiscalYearId = (Long) arg;
        } else if ("id".equals(varName) && arg instanceof Long) {
          entityId = (Long) arg;
        }
        paramMap.put(varName, arg);
        continue;
      }

      // Extract request body — serialize to JSON, excluding file content
      RequestBody bodyAnn = parameters[i].getAnnotation(RequestBody.class);
      if (bodyAnn != null) {
        try {
          paramMap.put("requestBody", arg);
          // Try to extract entity name from the body
          entityName = extractEntityName(arg);
        } catch (Exception e) {
          paramMap.put("requestBody", arg.toString());
        }
        continue;
      }

      // Handle MultipartFile — record filename only, not content
      if (arg instanceof MultipartFile file) {
        paramMap.put(paramName, Map.of(
            "fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
            "contentType", file.getContentType() != null ? file.getContentType() : "unknown",
            "size", file.getSize()
        ));
        if (entityName == null) {
          entityName = file.getOriginalFilename();
        }
        continue;
      }

      // Skip Authentication/Principal objects
      if (arg instanceof Principal || arg instanceof org.springframework.security.core.Authentication) {
        continue;
      }
    }

    // Resolve RC name from ID
    if (rcId != null) {
      try {
        rcName = rcRepository.findById(rcId)
            .map(rc -> rc.getName())
            .orElse(null);
      } catch (Exception e) {
        logger.debug("Could not resolve RC name for rcId {}: {}", rcId, e.getMessage());
      }
    }

    // Resolve fiscal year name from ID
    if (fiscalYearId != null) {
      try {
        fyName = fiscalYearRepository.findById(fiscalYearId)
            .map(fy -> fy.getName())
            .orElse(null);
      } catch (Exception e) {
        logger.debug("Could not resolve FY name for fiscalYearId {}: {}", fiscalYearId, e.getMessage());
      }
    }

    // Serialize parameters to JSON
    String parametersJson = null;
    try {
      if (!paramMap.isEmpty()) {
        parametersJson = objectMapper.writeValueAsString(paramMap);
        // Truncate if too long
        if (parametersJson.length() > 10000) {
          parametersJson = parametersJson.substring(0, 10000) + "...(truncated)";
        }
      }
    } catch (Exception e) {
      parametersJson = paramMap.toString();
    }

    // Build the audit event
    AuditEvent auditEvent = new AuditEvent(username, audited.action(), audited.entityType());
    auditEvent.setEntityId(entityId);
    auditEvent.setEntityName(entityName);
    auditEvent.setRcId(rcId);
    auditEvent.setRcName(rcName);
    auditEvent.setFiscalYearId(fiscalYearId);
    auditEvent.setFiscalYearName(fyName);
    auditEvent.setParameters(parametersJson);
    auditEvent.setHttpMethod(httpMethod);
    auditEvent.setEndpoint(endpoint);
    auditEvent.setUserAgent(userAgent);
    auditEvent.setIpAddress(ipAddress);

    // Step 1: Pre-emptively persist the audit event
    AuditEvent savedEvent;
    try {
      savedEvent = auditService.recordEvent(auditEvent);
    } catch (Exception e) {
      logger.error("Failed to record audit event for {} {} — blocking action execution",
          audited.action(), audited.entityType(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Audit recording failed. Action was not performed. "
              + "Please try again or contact your administrator.",
              "AUDIT_FAILURE"));
    }

    // Step 2: Execute the actual controller method
    try {
      Object result = joinPoint.proceed();

      // Step 3: Mark the audit event as successful
      // Try to extract entity ID and name from the response for create operations
      Long createdEntityId = extractEntityIdFromResponse(result);
      String createdEntityName = extractEntityNameFromResponse(result);
      if (createdEntityId != null && savedEvent.getEntityId() == null) {
        auditService.markSuccess(savedEvent.getId(), createdEntityId,
            createdEntityName != null ? createdEntityName : savedEvent.getEntityName());
      } else {
        auditService.markSuccess(savedEvent.getId());
      }

      return result;

    } catch (Exception e) {
      // Step 3 (failure path): Mark the audit event as failed
      auditService.markFailure(savedEvent.getId(), e.getMessage());
      throw e;
    }
  }

  /**
   * Extract the username from the current request.
   */
  private String extractUsername(HttpServletRequest request) {
    if (request != null && request.getUserPrincipal() != null) {
      return request.getUserPrincipal().getName();
    }
    // Fallback for dev mode
    return "default-user";
  }

  /**
   * Get the current HTTP request from the request context.
   */
  private HttpServletRequest getCurrentRequest() {
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attrs != null ? attrs.getRequest() : null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Extract the client IP address, handling proxied requests.
   */
  private String extractClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // If X-Forwarded-For contains multiple IPs, take the first one
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  /**
   * Try to extract a name field from a request body object via reflection.
   * Tries getName(), then getDescription(), then the raw name field.
   */
  private String extractEntityName(Object body) {
    if (body == null) return null;
    // Try getName() method first
    try {
      var method = body.getClass().getMethod("getName");
      Object name = method.invoke(body);
      if (name != null) return name.toString();
    } catch (Exception ignored) {
      // Method not found, continue to next attempt
    }
    // Try getDescription() method (e.g., ProcurementItem)
    try {
      var method = body.getClass().getMethod("getDescription");
      Object desc = method.invoke(body);
      if (desc != null) return desc.toString();
    } catch (Exception ignored) {
      // Method not found, continue to next attempt
    }
    // Try getCode() method (e.g., Money/FundingType)
    try {
      var method = body.getClass().getMethod("getCode");
      Object code = method.invoke(body);
      if (code != null) return code.toString();
    } catch (Exception ignored) {
      // Method not found, continue to next attempt
    }
    // Try name field directly
    try {
      var field = body.getClass().getDeclaredField("name");
      field.setAccessible(true);
      Object name = field.get(body);
      if (name != null) return name.toString();
    } catch (Exception ignored) {
      // Field not found
    }
    return null;
  }

  /**
   * Try to extract entity ID from the response body.
   * Works with ResponseEntity containing DTOs that have getId() methods.
   */
  private Long extractEntityIdFromResponse(Object result) {
    if (result instanceof ResponseEntity<?> response) {
      Object body = response.getBody();
      if (body != null) {
        try {
          var method = body.getClass().getMethod("getId");
          Object id = method.invoke(body);
          if (id instanceof Long) {
            return (Long) id;
          } else if (id instanceof Number) {
            return ((Number) id).longValue();
          }
        } catch (Exception ignored) {
          // Not all responses have getId()
        }
      }
    }
    return null;
  }

  /**
   * Try to extract entity name from the response body.
   * Works with ResponseEntity containing DTOs that have getName() methods.
   */
  private String extractEntityNameFromResponse(Object result) {
    if (result instanceof ResponseEntity<?> response) {
      Object body = response.getBody();
      if (body != null) {
        return extractEntityName(body);
      }
    }
    return null;
  }
}
