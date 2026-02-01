/*
 * myRC - Procurement Quote File DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Procurement Quote File.
 * Used for transferring file metadata between layers (excludes binary content).
 */
package com.myrc.dto;

import com.myrc.model.ProcurementQuoteFile;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Procurement Quote File.
 * Contains file metadata without the binary content for efficient transfer.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
public class ProcurementQuoteFileDTO {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String formattedFileSize;
    private String description;
    private Long quoteId;
    private String quoteVendorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private String downloadUrl;

    // Constructors
    public ProcurementQuoteFileDTO() {
    }

    public ProcurementQuoteFileDTO(Long id, String fileName, String contentType, Long fileSize,
                                   String formattedFileSize, String description, Long quoteId,
                                   String quoteVendorName, LocalDateTime createdAt, LocalDateTime updatedAt,
                                   Boolean active) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.formattedFileSize = formattedFileSize;
        this.description = description;
        this.quoteId = quoteId;
        this.quoteVendorName = quoteVendorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    /**
     * Creates a ProcurementQuoteFileDTO from a ProcurementQuoteFile entity.
     *
     * @param file the file entity
     * @return the file DTO (without binary content)
     */
    public static ProcurementQuoteFileDTO fromEntity(ProcurementQuoteFile file) {
        if (file == null) {
            return null;
        }
        return new ProcurementQuoteFileDTO(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getFileSize(),
                file.getFormattedFileSize(),
                file.getDescription(),
                file.getQuote() != null ? file.getQuote().getId() : null,
                file.getQuote() != null ? file.getQuote().getVendorName() : null,
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

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteVendorName() {
        return quoteVendorName;
    }

    public void setQuoteVendorName(String quoteVendorName) {
        this.quoteVendorName = quoteVendorName;
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
