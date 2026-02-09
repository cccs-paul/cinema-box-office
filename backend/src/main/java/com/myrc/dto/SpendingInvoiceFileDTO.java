/*
 * myRC - Spending Invoice File DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Data Transfer Object for Spending Invoice File.
 * Contains file metadata without binary content for efficient transfer.
 */
package com.myrc.dto;

import com.myrc.model.SpendingInvoiceFile;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Spending Invoice File.
 * Contains file metadata without the binary content for efficient transfer.
 */
public class SpendingInvoiceFileDTO {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String formattedFileSize;
    private String description;
    private Long invoiceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private String downloadUrl;

    // Constructors
    public SpendingInvoiceFileDTO() {
    }

    public SpendingInvoiceFileDTO(Long id, String fileName, String contentType, Long fileSize,
                                  String formattedFileSize, String description, Long invoiceId,
                                  LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.formattedFileSize = formattedFileSize;
        this.description = description;
        this.invoiceId = invoiceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    /**
     * Creates a SpendingInvoiceFileDTO from a SpendingInvoiceFile entity.
     */
    public static SpendingInvoiceFileDTO fromEntity(SpendingInvoiceFile file) {
        if (file == null) {
            return null;
        }
        return new SpendingInvoiceFileDTO(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getFileSize(),
                file.getFormattedFileSize(),
                file.getDescription(),
                file.getInvoice() != null ? file.getInvoice().getId() : null,
                file.getCreatedAt(),
                file.getUpdatedAt(),
                file.getActive()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormattedFileSize() {
        return formattedFileSize;
    }

    public void setFormattedFileSize(String formattedFileSize) {
        this.formattedFileSize = formattedFileSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
