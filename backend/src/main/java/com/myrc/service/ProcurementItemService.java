/*
 * myRC - Procurement Item Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Procurement Item management operations.
 */
package com.myrc.service;

import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.dto.ProcurementQuoteFileDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Procurement Item management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
public interface ProcurementItemService {

    /**
     * Get all procurement items for a fiscal year.
     *
     * @param fiscalYearId the fiscal year ID
     * @param username the requesting user's username
     * @return list of procurement items
     * @throws IllegalArgumentException if user doesn't have access
     */
    List<ProcurementItemDTO> getProcurementItemsByFiscalYearId(Long fiscalYearId, String username);

    /**
     * Get all procurement items for a fiscal year filtered by status.
     *
     * @param fiscalYearId the fiscal year ID
     * @param status the status to filter by
     * @param username the requesting user's username
     * @return list of procurement items
     * @throws IllegalArgumentException if user doesn't have access
     */
    List<ProcurementItemDTO> getProcurementItemsByFiscalYearIdAndStatus(Long fiscalYearId, String status, String username);

    /**
     * Get a specific procurement item by ID.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return optional procurement item
     */
    Optional<ProcurementItemDTO> getProcurementItemById(Long procurementItemId, String username);

    /**
     * Get a procurement item with all its quotes.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return optional procurement item with quotes
     */
    Optional<ProcurementItemDTO> getProcurementItemWithQuotes(Long procurementItemId, String username);

    /**
     * Create a new procurement item for a fiscal year.
     *
     * @param procurementItemDTO the procurement item data
     * @param username the requesting user's username
     * @return the created procurement item
     * @throws IllegalArgumentException if user doesn't have write access or validation fails
     */
    ProcurementItemDTO createProcurementItem(ProcurementItemDTO procurementItemDTO, String username);

    /**
     * Update an existing procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param procurementItemDTO the updated procurement item data
     * @param username the requesting user's username
     * @return the updated procurement item
     * @throws IllegalArgumentException if user doesn't have write access or validation fails
     */
    ProcurementItemDTO updateProcurementItem(Long procurementItemId, ProcurementItemDTO procurementItemDTO, String username);

    /**
     * Delete a procurement item (soft delete).
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @throws IllegalArgumentException if user doesn't have write access
     */
    void deleteProcurementItem(Long procurementItemId, String username);

    /**
     * Update the status of a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param status the new status
     * @param username the requesting user's username
     * @return the updated procurement item
     * @throws IllegalArgumentException if user doesn't have write access or status transition is invalid
     */
    ProcurementItemDTO updateProcurementItemStatus(Long procurementItemId, String status, String username);

    /**
     * Search procurement items by name, PR, or PO.
     *
     * @param fiscalYearId the fiscal year ID
     * @param searchTerm the search term
     * @param username the requesting user's username
     * @return list of matching procurement items
     */
    List<ProcurementItemDTO> searchProcurementItems(Long fiscalYearId, String searchTerm, String username);

    // Quote operations

    /**
     * Get all quotes for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return list of quotes
     */
    List<ProcurementQuoteDTO> getQuotesByProcurementItemId(Long procurementItemId, String username);

    /**
     * Get a specific quote by ID.
     *
     * @param quoteId the quote ID
     * @param username the requesting user's username
     * @return optional quote
     */
    Optional<ProcurementQuoteDTO> getQuoteById(Long quoteId, String username);

    /**
     * Get a quote with all its files.
     *
     * @param quoteId the quote ID
     * @param username the requesting user's username
     * @return optional quote with files
     */
    Optional<ProcurementQuoteDTO> getQuoteWithFiles(Long quoteId, String username);

    /**
     * Create a new quote for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param quoteDTO the quote data
     * @param username the requesting user's username
     * @return the created quote
     * @throws IllegalArgumentException if user doesn't have write access or validation fails
     */
    ProcurementQuoteDTO createQuote(Long procurementItemId, ProcurementQuoteDTO quoteDTO, String username);

    /**
     * Update an existing quote.
     *
     * @param quoteId the quote ID
     * @param quoteDTO the updated quote data
     * @param username the requesting user's username
     * @return the updated quote
     * @throws IllegalArgumentException if user doesn't have write access or validation fails
     */
    ProcurementQuoteDTO updateQuote(Long quoteId, ProcurementQuoteDTO quoteDTO, String username);

    /**
     * Delete a quote (soft delete).
     *
     * @param quoteId the quote ID
     * @param username the requesting user's username
     * @throws IllegalArgumentException if user doesn't have write access
     */
    void deleteQuote(Long quoteId, String username);

    /**
     * Select a quote for the procurement item.
     *
     * @param quoteId the quote ID to select
     * @param username the requesting user's username
     * @return the updated quote
     * @throws IllegalArgumentException if user doesn't have write access
     */
    ProcurementQuoteDTO selectQuote(Long quoteId, String username);

    // File operations

    /**
     * Get all files for a quote.
     *
     * @param quoteId the quote ID
     * @param username the requesting user's username
     * @return list of file metadata
     */
    List<ProcurementQuoteFileDTO> getFilesByQuoteId(Long quoteId, String username);

    /**
     * Get file metadata by ID.
     *
     * @param fileId the file ID
     * @param username the requesting user's username
     * @return optional file metadata
     */
    Optional<ProcurementQuoteFileDTO> getFileMetadataById(Long fileId, String username);

    /**
     * Get file content by ID.
     *
     * @param fileId the file ID
     * @param username the requesting user's username
     * @return file content bytes
     * @throws IllegalArgumentException if file not found or user doesn't have access
     */
    byte[] getFileContent(Long fileId, String username);

    /**
     * Upload a file to a quote.
     *
     * @param quoteId the quote ID
     * @param file the file to upload
     * @param description optional file description
     * @param username the requesting user's username
     * @return the created file metadata
     * @throws IllegalArgumentException if user doesn't have write access or file validation fails
     */
    ProcurementQuoteFileDTO uploadFile(Long quoteId, MultipartFile file, String description, String username);

    /**
     * Replace an existing file with a new one.
     *
     * @param fileId the file ID to replace
     * @param file the new file
     * @param description optional file description (if null, keeps existing)
     * @param username the requesting user's username
     * @return the updated file metadata
     * @throws IllegalArgumentException if user doesn't have write access, file not found, or file validation fails
     */
    ProcurementQuoteFileDTO replaceFile(Long fileId, MultipartFile file, String description, String username);

    /**
     * Delete a file (soft delete).
     *
     * @param fileId the file ID
     * @param username the requesting user's username
     * @throws IllegalArgumentException if user doesn't have write access
     */
    void deleteFile(Long fileId, String username);

    /**
     * Toggle the link between a procurement item and a spending item.
     * If no spending item is linked, creates one from the procurement item data.
     * If a spending item is linked, unlinks it (optionally with force if modified).
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @param forceUnlink if true, unlinks even if spending item was modified
     * @return result containing the updated procurement item and link status
     * @throws IllegalArgumentException if user doesn't have write access
     */
    ToggleSpendingLinkResult toggleSpendingLink(Long procurementItemId, String username, boolean forceUnlink);

    /**
     * Result record for toggle spending link operation.
     */
    record ToggleSpendingLinkResult(
            ProcurementItemDTO procurementItem,
            boolean spendingLinked,
            boolean hasWarning,
            String warningMessage) {
        
        public static ToggleSpendingLinkResult success(ProcurementItemDTO item, boolean linked) {
            return new ToggleSpendingLinkResult(item, linked, false, null);
        }
        
        public static ToggleSpendingLinkResult warning(ProcurementItemDTO item, String message) {
            return new ToggleSpendingLinkResult(item, true, true, message);
        }
    }
}
