/*
 * myRC - Spending Invoice File Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Entity representing a File attached to a Spending Invoice.
 * Stores file metadata and binary content for invoice documentation.
 */
package com.myrc.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a File attached to a Spending Invoice.
 * Stores both file metadata and binary content.
 */
@Entity
@Table(name = "spending_invoice_files")
public class SpendingInvoiceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original filename as uploaded.
     */
    @Column(nullable = false, length = 255)
    private String fileName;

    /**
     * MIME content type of the file.
     */
    @Column(nullable = false, length = 100)
    private String contentType;

    /**
     * File size in bytes.
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * Binary content of the file.
     * Stored as a BLOB in the database.
     */
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] content;

    /**
     * Optional description of the file.
     */
    @Column(length = 500)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SpendingInvoice invoice;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public SpendingInvoiceFile() {
    }

    public SpendingInvoiceFile(String fileName, String contentType, Long fileSize,
                               byte[] content, SpendingInvoice invoice) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.content = content;
        this.invoice = invoice;
    }

    /**
     * Get a human-readable formatted file size string.
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SpendingInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(SpendingInvoice invoice) {
        this.invoice = invoice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
}
