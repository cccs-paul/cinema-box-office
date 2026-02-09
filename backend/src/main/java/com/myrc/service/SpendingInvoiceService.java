/*
 * myRC - Spending Invoice Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingInvoiceFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing spending invoices.
 */
public interface SpendingInvoiceService {

    /**
     * Get all invoices for a spending item.
     */
    List<SpendingInvoiceDTO> getInvoicesBySpendingItemId(Long spendingItemId, String username);

    /**
     * Get an invoice by ID with its files.
     */
    Optional<SpendingInvoiceDTO> getInvoiceById(Long invoiceId, String username);

    /**
     * Create a new invoice for a spending item.
     */
    SpendingInvoiceDTO createInvoice(Long spendingItemId, SpendingInvoiceDTO invoiceDTO, String username);

    /**
     * Update an existing invoice.
     */
    SpendingInvoiceDTO updateInvoice(Long invoiceId, SpendingInvoiceDTO invoiceDTO, String username);

    /**
     * Delete an invoice (soft delete).
     */
    void deleteInvoice(Long invoiceId, String username);

    // File operations

    /**
     * Get files for an invoice.
     */
    List<SpendingInvoiceFileDTO> getFiles(Long invoiceId, String username);

    /**
     * Get a specific file's metadata.
     */
    Optional<SpendingInvoiceFileDTO> getFileMetadata(Long fileId, String username);

    /**
     * Get a file's binary content.
     */
    byte[] getFileContent(Long fileId, String username);

    /**
     * Upload a file to an invoice.
     */
    SpendingInvoiceFileDTO uploadFile(Long invoiceId, MultipartFile file, String description, String username);

    /**
     * Delete a file (soft delete).
     */
    void deleteFile(Long fileId, String username);

    /**
     * Replace a file's content.
     */
    SpendingInvoiceFileDTO replaceFile(Long fileId, MultipartFile file, String description, String username);
}
