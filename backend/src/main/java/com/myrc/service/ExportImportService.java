/*
 * myRC - Export/Import Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 *
 * Description:
 * Service interface for data export and import operations.
 */
package com.myrc.service;

import com.myrc.dto.ExportDataDTO;

/**
 * Service interface for exporting and importing fiscal year data.
 * Supports full export with base64-encoded file attachments
 * and import to recreate all data.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
public interface ExportImportService {

    /**
     * Export all data for a fiscal year including file attachments.
     * Retrieves funding items, spending items (with invoices and files),
     * and procurement items (with events, quotes, and files).
     * All binary file content is base64-encoded.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param username the requesting user's username
     * @return the complete export data
     * @throws IllegalArgumentException if user does not have access
     */
    ExportDataDTO exportData(Long rcId, Long fyId, String username);

    /**
     * Import data into a fiscal year from an export package.
     * Creates funding items, spending items (with invoices and files),
     * and procurement items (with events, quotes, and files).
     * Decodes base64 file content and stores as binary.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param exportData the data to import
     * @param username the requesting user's username
     * @return the import result with counts of imported items
     * @throws IllegalArgumentException if user does not have write access
     */
    ExportDataDTO importData(Long rcId, Long fyId, ExportDataDTO exportData, String username);
}
