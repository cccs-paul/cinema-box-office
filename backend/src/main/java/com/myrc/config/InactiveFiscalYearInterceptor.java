/*
 * myRC - Inactive Fiscal Year Write Interceptor
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myrc.dto.ErrorResponse;
import com.myrc.model.FiscalYear;
import com.myrc.repository.FiscalYearRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor that enforces read-only access for inactive fiscal years.
 * Blocks all mutating HTTP methods (POST, PUT, PATCH, DELETE) on endpoints
 * scoped to an inactive fiscal year.
 *
 * <p>The toggle-active endpoint itself is exempted so that an owner can
 * re-activate an inactive fiscal year.</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-09
 */
@Component
public class InactiveFiscalYearInterceptor implements HandlerInterceptor {

    private static final Logger logger = Logger.getLogger(InactiveFiscalYearInterceptor.class.getName());

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    /**
     * Matches URLs like:
     *   /responsibility-centres/{rcId}/fiscal-years/{fyId}/...
     *   /fiscal-years/{fyId}/...
     */
    private static final Pattern FY_ID_PATTERN = Pattern.compile(
            "/fiscal-years/(\\d+)(?:/|$)");

    private final FiscalYearRepository fiscalYearRepository;
    private final ObjectMapper objectMapper;

    public InactiveFiscalYearInterceptor(FiscalYearRepository fiscalYearRepository,
                                          ObjectMapper objectMapper) {
        this.fiscalYearRepository = fiscalYearRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String method = request.getMethod();

        // Only check mutating requests
        if (!MUTATING_METHODS.contains(method)) {
            return true;
        }

        String uri = request.getRequestURI();

        // Allow toggling active status (the only write operation permitted on inactive FYs)
        if (uri.endsWith("/toggle-active")) {
            return true;
        }

        // Extract the fiscal year ID from the URL
        Matcher matcher = FY_ID_PATTERN.matcher(uri);
        if (!matcher.find()) {
            return true; // URL is not FY-scoped
        }

        Long fyId = Long.parseLong(matcher.group(1));
        Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fyId);

        if (fyOpt.isEmpty()) {
            return true; // Let the controller handle 404
        }

        FiscalYear fy = fyOpt.get();
        if (!fy.getActive()) {
            logger.warning("Blocked write operation on inactive fiscal year " + fyId
                    + " (" + fy.getName() + "): " + method + " " + uri);

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse("This fiscal year is inactive and read-only. No changes are allowed.")));
            return false;
        }

        return true;
    }
}
