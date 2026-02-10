/*
 * myRC - Spending Invoice REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * REST Controller for Spending Invoice management.
 * Handles invoice CRUD and file upload/download operations.
 */
package com.myrc.controller;

import com.myrc.audit.Audited;
import com.myrc.dto.ErrorResponse;
import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingInvoiceFileDTO;
import com.myrc.service.SpendingInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * REST Controller for Spending Invoice management.
 * Invoices/receipts are attached to spending items.
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/spending-items/{spendingItemId}/invoices")
@Tag(name = "Spending Invoice Management", description = "APIs for managing invoices/receipts on spending items")
public class SpendingInvoiceController {

    private static final Logger logger = Logger.getLogger(SpendingInvoiceController.class.getName());
    private final SpendingInvoiceService invoiceService;

    public SpendingInvoiceController(SpendingInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // ==========================
    // Invoice CRUD Endpoints
    // ==========================

    @GetMapping
    @Operation(summary = "Get all invoices for a spending item")
    public ResponseEntity<?> getInvoices(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            List<SpendingInvoiceDTO> invoices = invoiceService.getInvoicesBySpendingItemId(spendingItemId, username);
            return ResponseEntity.ok(invoices);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get a specific invoice by ID")
    public ResponseEntity<?> getInvoice(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            Optional<SpendingInvoiceDTO> invoiceOpt = invoiceService.getInvoiceById(invoiceId, username);
            return invoiceOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to get invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Audited(action = "CREATE_SPENDING_INVOICE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Create a new invoice for a spending item")
    public ResponseEntity<?> createInvoice(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @RequestBody SpendingInvoiceDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            SpendingInvoiceDTO created = invoiceService.createInvoice(spendingItemId, request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to create invoice: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to create invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create invoice"));
        }
    }

    @PutMapping("/{invoiceId}")
    @Audited(action = "UPDATE_SPENDING_INVOICE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Update an existing invoice")
    public ResponseEntity<?> updateInvoice(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @RequestBody SpendingInvoiceDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            SpendingInvoiceDTO updated = invoiceService.updateInvoice(invoiceId, request, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to update invoice: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to update invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update invoice"));
        }
    }

    @DeleteMapping("/{invoiceId}")
    @Audited(action = "DELETE_SPENDING_INVOICE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Delete an invoice")
    public ResponseEntity<?> deleteInvoice(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            invoiceService.deleteInvoice(invoiceId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to delete invoice: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to delete invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================
    // File Endpoints
    // ==========================

    @GetMapping("/{invoiceId}/files")
    @Operation(summary = "Get all files for an invoice")
    public ResponseEntity<?> getFiles(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            List<SpendingInvoiceFileDTO> files = invoiceService.getFiles(invoiceId, username);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get invoice files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to get invoice files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{invoiceId}/files/{fileId}/download")
    @Operation(summary = "Download an invoice file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            Optional<SpendingInvoiceFileDTO> metaOpt = invoiceService.getFileMetadata(fileId, username);
            if (metaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SpendingInvoiceFileDTO meta = metaOpt.get();
            byte[] content = invoiceService.getFileContent(fileId, username);
            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(meta.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getFileName() + "\"")
                    .contentLength(meta.getFileSize())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to download invoice file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to download invoice file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{invoiceId}/files/{fileId}/view")
    @Operation(summary = "View an invoice file inline")
    public ResponseEntity<Resource> viewFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            Optional<SpendingInvoiceFileDTO> metaOpt = invoiceService.getFileMetadata(fileId, username);
            if (metaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SpendingInvoiceFileDTO meta = metaOpt.get();
            byte[] content = invoiceService.getFileContent(fileId, username);
            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(meta.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + meta.getFileName() + "\"")
                    .contentLength(meta.getFileSize())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to view invoice file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to view invoice file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{invoiceId}/files")
    @Audited(action = "UPLOAD_INVOICE_FILE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Upload a file to an invoice")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            SpendingInvoiceFileDTO uploaded = invoiceService.uploadFile(invoiceId, file, description, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to upload invoice file: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to upload invoice file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload file"));
        }
    }

    @DeleteMapping("/{invoiceId}/files/{fileId}")
    @Audited(action = "DELETE_INVOICE_FILE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Delete an invoice file")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            invoiceService.deleteFile(fileId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to delete invoice file: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to delete invoice file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{invoiceId}/files/{fileId}")
    @Audited(action = "REPLACE_INVOICE_FILE", entityType = "SPENDING_INVOICE")
    @Operation(summary = "Replace an invoice file")
    public ResponseEntity<?> replaceFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long invoiceId,
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        String username = getUsername(authentication);
        try {
            SpendingInvoiceFileDTO replaced = invoiceService.replaceFile(fileId, file, description, username);
            return ResponseEntity.ok(replaced);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to replace invoice file: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to replace invoice file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to replace file"));
        }
    }

    // ==========================
    // Helper Methods
    // ==========================

    private String getUsername(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
