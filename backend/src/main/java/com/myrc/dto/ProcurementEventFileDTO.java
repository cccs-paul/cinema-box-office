/*
 * myRC - Procurement Event File DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-04
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Procurement Event File.
 * Used for transferring file metadata between layers.
 */
package com.myrc.dto;

import com.myrc.model.ProcurementEventFile;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Procurement Event File.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-04
 */
public class ProcurementEventFileDTO {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String formattedFileSize;
    private String description;
    private Long eventId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    // Constructors
    public ProcurementEventFileDTO() {
    }

    public ProcurementEventFileDTO(Long id, String fileName, String contentType, Long fileSize,
                                   String formattedFileSize, String description, Long eventId,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.formattedFileSize = formattedFileSize;
        this.description = description;
        this.eventId = eventId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    /**
     * Creates a ProcurementEventFileDTO from a ProcurementEventFile entity.
     *
     * @param file the procurement event file entity
     * @return the procurement event file DTO
     */
    public static ProcurementEventFileDTO fromEntity(ProcurementEventFile file) {
        if (file == null) {
            return null;
        }
        return new ProcurementEventFileDTO(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getFileSize(),
                file.getFormattedFileSize(),
                file.getDescription(),
                file.getEvent() != null ? file.getEvent().getId() : null,
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

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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
}
