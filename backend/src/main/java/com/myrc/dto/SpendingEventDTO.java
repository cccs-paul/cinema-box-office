/*
 * myRC - Spending Event DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Spending Event.
 */
package com.myrc.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.myrc.model.SpendingEvent;

/**
 * Data Transfer Object for Spending Event.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
public class SpendingEventDTO {

    private Long id;
    private Long spendingItemId;
    private String spendingItemName;
    private String eventType;
    private LocalDate eventDate;
    private String comment;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    // Constructors
    public SpendingEventDTO() {
    }

    public SpendingEventDTO(Long id, Long spendingItemId, String eventType, 
            LocalDate eventDate, String comment) {
        this.id = id;
        this.spendingItemId = spendingItemId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.comment = comment;
    }

    /**
     * Create a DTO from an entity.
     *
     * @param entity the entity
     * @return the DTO
     */
    public static SpendingEventDTO fromEntity(SpendingEvent entity) {
        if (entity == null) {
            return null;
        }
        SpendingEventDTO dto = new SpendingEventDTO();
        dto.setId(entity.getId());
        dto.setSpendingItemId(entity.getSpendingItem() != null ? entity.getSpendingItem().getId() : null);
        dto.setSpendingItemName(entity.getSpendingItem() != null ? entity.getSpendingItem().getName() : null);
        dto.setEventType(entity.getEventType() != null ? entity.getEventType().name() : null);
        dto.setEventDate(entity.getEventDate());
        dto.setComment(entity.getComment());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setActive(entity.getActive());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpendingItemId() {
        return spendingItemId;
    }

    public void setSpendingItemId(Long spendingItemId) {
        this.spendingItemId = spendingItemId;
    }

    public String getSpendingItemName() {
        return spendingItemName;
    }

    public void setSpendingItemName(String spendingItemName) {
        this.spendingItemName = spendingItemName;
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

    @Override
    public String toString() {
        return "SpendingEventDTO{" +
                "id=" + id +
                ", spendingItemId=" + spendingItemId +
                ", spendingItemName='" + spendingItemName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", comment='" + comment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", active=" + active +
                '}';
    }
}
