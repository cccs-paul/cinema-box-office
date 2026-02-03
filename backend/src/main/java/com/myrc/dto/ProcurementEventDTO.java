/*
 * myRC - Procurement Event DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.1.0
 *
 * Description:
 * Data Transfer Object for Procurement Event.
 */
package com.myrc.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.myrc.model.ProcurementEvent;

/**
 * Data Transfer Object for Procurement Event.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-29
 */
public class ProcurementEventDTO {

    private Long id;
    private Long procurementItemId;
    private String procurementItemName;
    private String eventType;
    private LocalDate eventDate;
    private String comment;
    private String oldStatus;
    private String newStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private List<ProcurementEventFileDTO> files;
    private Integer fileCount;

    // Constructors
    public ProcurementEventDTO() {
        this.files = new ArrayList<>();
        this.fileCount = 0;
    }

    public ProcurementEventDTO(Long id, Long procurementItemId, String eventType, 
            LocalDate eventDate, String comment) {
        this.id = id;
        this.procurementItemId = procurementItemId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.comment = comment;
        this.files = new ArrayList<>();
        this.fileCount = 0;
    }

    /**
     * Create a DTO from an entity.
     *
     * @param entity the entity
     * @return the DTO
     */
    public static ProcurementEventDTO fromEntity(ProcurementEvent entity) {
        if (entity == null) {
            return null;
        }
        ProcurementEventDTO dto = new ProcurementEventDTO();
        dto.setId(entity.getId());
        dto.setProcurementItemId(entity.getProcurementItem() != null ? entity.getProcurementItem().getId() : null);
        dto.setProcurementItemName(entity.getProcurementItem() != null ? entity.getProcurementItem().getName() : null);
        dto.setEventType(entity.getEventType() != null ? entity.getEventType().name() : null);
        dto.setEventDate(entity.getEventDate());
        dto.setComment(entity.getComment());
        dto.setOldStatus(entity.getOldStatus());
        dto.setNewStatus(entity.getNewStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setActive(entity.getActive());
        if (entity.getFiles() != null) {
            dto.setFiles(entity.getFiles().stream()
                    .filter(f -> f.getActive())
                    .map(ProcurementEventFileDTO::fromEntity)
                    .collect(Collectors.toList()));
            dto.setFileCount((int) entity.getFiles().stream().filter(f -> f.getActive()).count());
        } else {
            dto.setFiles(new ArrayList<>());
            dto.setFileCount(0);
        }
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcurementItemId() {
        return procurementItemId;
    }

    public void setProcurementItemId(Long procurementItemId) {
        this.procurementItemId = procurementItemId;
    }

    public String getProcurementItemName() {
        return procurementItemName;
    }

    public void setProcurementItemName(String procurementItemName) {
        this.procurementItemName = procurementItemName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public List<ProcurementEventFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<ProcurementEventFileDTO> files) {
        this.files = files != null ? files : new ArrayList<>();
        this.fileCount = this.files.size();
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    @Override
    public String toString() {
        return "ProcurementEventDTO{" +
                "id=" + id +
                ", procurementItemId=" + procurementItemId +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", comment='" + comment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
