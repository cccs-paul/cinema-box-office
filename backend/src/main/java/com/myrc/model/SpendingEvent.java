/*
 * myRC - Spending Event Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Spending Tracking Event associated with a Spending Item.
 * Each spending item can have 0..n events tracking its progress and history.
 * This is used for spending items NOT linked to a procurement item.
 */
package com.myrc.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entity representing a Spending Tracking Event associated with a Spending Item.
 * Events track the history and progress of spending items through their lifecycle.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
@Entity
@Table(name = "spending_events")
public class SpendingEvent {

    /**
     * Enumeration of spending event types.
     * These track the progress of a spending item through its lifecycle.
     */
    public enum EventType {
        /** Spending is pending, waiting to be processed */
        PENDING,
        /** ECO (Expenditure Control Officer) approval has been requested */
        ECO_REQUESTED,
        /** ECO approval has been received */
        ECO_RECEIVED,
        /** External approval (outside the organization) has been requested */
        EXTERNAL_APPROVAL_REQUESTED,
        /** External approval has been received */
        EXTERNAL_APPROVAL_RECEIVED,
        /** Section 32 certification (commitment authority) has been provided */
        SECTION_32_PROVIDED,
        /** Goods or services have been received */
        RECEIVED_GOODS_SERVICES,
        /** Section 34 certification (performance authority) has been provided */
        SECTION_34_PROVIDED,
        /** Credit card payment has cleared */
        CREDIT_CARD_CLEARED,
        /** Spending has been cancelled */
        CANCELLED,
        /** Spending is on hold */
        ON_HOLD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The spending item this event belongs to.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "spending_item_id", nullable = false)
    private SpendingItem spendingItem;

    /**
     * The type of event.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private EventType eventType = EventType.PENDING;

    /**
     * The date of the event. Defaults to today's date.
     */
    @Column(nullable = false)
    private LocalDate eventDate;

    /**
     * Optional comment/description for the event.
     */
    @Column(length = 2000)
    private String comment;

    /**
     * Username of the user who created this event.
     */
    @Column(length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Version for optimistic locking.
     * Prevents lost updates when multiple users edit the same record.
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    // Constructors
    public SpendingEvent() {
        this.eventDate = LocalDate.now();
    }

    public SpendingEvent(SpendingItem spendingItem, EventType eventType, String comment) {
        this.spendingItem = spendingItem;
        this.eventType = eventType != null ? eventType : EventType.PENDING;
        this.comment = comment;
        this.eventDate = LocalDate.now();
    }

    public SpendingEvent(SpendingItem spendingItem, EventType eventType, LocalDate eventDate, String comment) {
        this.spendingItem = spendingItem;
        this.eventType = eventType != null ? eventType : EventType.PENDING;
        this.eventDate = eventDate != null ? eventDate : LocalDate.now();
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SpendingItem getSpendingItem() {
        return spendingItem;
    }

    public void setSpendingItem(SpendingItem spendingItem) {
        this.spendingItem = spendingItem;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "SpendingEvent{" +
                "id=" + id +
                ", spendingItemId=" + (spendingItem != null ? spendingItem.getId() : null) +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                ", comment='" + comment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpendingEvent)) return false;
        SpendingEvent that = (SpendingEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
