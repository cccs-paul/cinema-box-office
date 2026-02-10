/*
 * myRC - Export/Import REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for data export and import operations.
 * Provides endpoints to export all fiscal year data as JSON
 * (including base64-encoded file attachments) and to import
 * data from a previously exported JSON package.
 */
package com.myrc.controller;

import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myrc.dto.ErrorResponse;
import com.myrc.dto.ExportDataDTO;
import com.myrc.service.ExportImportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for data export and import operations.
 * Exports all line items (funding, spending, procurement) with their
 * associated invoices, events, quotes, and base64-encoded file attachments.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}")
@Tag(name = "Export/Import", description = "APIs for exporting and importing fiscal year data")
public class ExportImportController {

    private static final Logger logger = Logger.getLogger(ExportImportController.class.getName());
    private final ExportImportService exportImportService;

    /**
     * Constructor.
     *
     * @param exportImportService the export/import service
     */
    public ExportImportController(ExportImportService exportImportService) {
        this.exportImportService = exportImportService;
    }

    /**
     * Export all data for a fiscal year as JSON.
     * Includes funding items, spending items (with invoices and files),
     * procurement items (with events, quotes, and files).
     * File content is base64-encoded.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param authentication the authentication principal
     * @return the complete export data as JSON
     */
    @GetMapping("/export")
    @Operation(summary = "Export fiscal year data",
            description = "Exports all funding, spending, and procurement items with file attachments as JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> exportData(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId
                + "/export - Exporting data for user: " + username);

        try {
            ExportDataDTO exportData = exportImportService.exportData(rcId, fyId, username);
            return ResponseEntity.ok(exportData);
        } catch (IllegalArgumentException e) {
            logger.warning("Export failed: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Export failed with unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Export failed: " + e.getMessage()));
        }
    }

    /**
     * Import data into a fiscal year from a JSON export package.
     * Creates funding items, spending items (with invoices and files),
     * and procurement items (with events, quotes, and files).
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param authentication the authentication principal
     * @param exportData the data to import
     * @return the import result with counts
     */
    @PostMapping("/import")
    @Operation(summary = "Import fiscal year data",
            description = "Imports funding, spending, and procurement items with file attachments from JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid import data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> importData(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            Authentication authentication,
            @RequestBody ExportDataDTO exportData) {
        String username = getUsername(authentication);
        logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId
                + "/import - Importing data for user: " + username);

        try {
            if (exportData == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Import data is required"));
            }
            ExportDataDTO result = exportImportService.importData(rcId, fyId, exportData, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warning("Import failed: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Import failed with unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Import failed: " + e.getMessage()));
        }
    }

    /**
     * Extract username from authentication, defaulting for development mode.
     *
     * @param authentication the authentication principal
     * @return the username
     */
    private String getUsername(Authentication authentication) {
        String username = "default-user";
        if (authentication != null && authentication.getName() != null
                && !authentication.getName().isEmpty()) {
            username = authentication.getName();
        }
        return username;
    }
}
