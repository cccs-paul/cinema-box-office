/*
 * myRC - Procurement Event Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Procurement Event/Update associated with a Procurement Item.
 * Each procurement item can have 0..n events tracking its progress and history.
 */
package com.myrc.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entity representing a Procurement Event/Update associated with a Procurement Item.
 * Events track the history and progress of procurement items through their lifecycle.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-29
 */
@Entity
@Table(name = "procurement_events")
public class ProcurementEvent {

    /**
     * Enumeration of procurement event types.
     */
    public enum EventType {
        /** Procurement process has not yet started */
        NOT_STARTED,
        /** Quote/estimate obtained from vendor */
        QUOTE,
        /** Requested acknowledgement from Software Asset Management team */
        SAM_ACKNOWLEDGEMENT_REQUESTED,
        /** Received acknowledgement from Software Asset Management team */
        SAM_ACKNOWLEDGEMENT_RECEIVED,
        /** Documentation package sent to Procurement */
        PACKAGE_SENT_TO_PROCUREMENT,
        /** Procurement has accepted the package and provided a Purchase Order */
        ACKNOWLEDGED_BY_PROCUREMENT,
        /** Procurement process put on pause */
        PAUSED,
        /** Procurement process cancelled */
        CANCELLED,
        /** Procurement process completed and contract awarded */
        CONTRACT_AWARDED,
        /** The goods of the procurement has been received */
        GOODS_RECEIVED,
        /** Invoice for all goods received */
        FULL_INVOICE_RECEIVED,
        /** Invoice for some, but not all goods received */
        PARTIAL_INVOICE_RECEIVED,
        /** Invoice for last delivery period of services received */
        MONTHLY_INVOICE_RECEIVED,
        /** Invoice for all goods/services signed for Section 34 */
        FULL_INVOICE_SIGNED,
        /** Invoice for some goods signed for Section 34 */
        PARTIAL_INVOICE_SIGNED,
        /** Invoice for last delivery period of services signed for Section 34 */
        MONTHLY_INVOICE_SIGNED,
        /** Procurement process completed and existing contract amended */
        CONTRACT_AMENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The procurement item this event belongs to.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "procurement_item_id", nullable = false)
    private ProcurementItem procurementItem;

    /**
     * The type of event.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private EventType eventType = EventType.NOT_STARTED;

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
     * Optional old status (for STATUS_CHANGE events).
     */
    @Column(length = 30)
    private String oldStatus;

    /**
     * Optional new status (for STATUS_CHANGE events).
     */
    @Column(length = 30)
    private String newStatus;

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

    /**
     * Files attached to this procurement event.
     * Each event can have 0..n files.
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcurementEventFile> files = new ArrayList<>();

    // Constructors
    public ProcurementEvent() {
        this.eventDate = LocalDate.now();
    }

    public ProcurementEvent(ProcurementItem procurementItem, EventType eventType, String comment) {
        this.procurementItem = procurementItem;
        this.eventType = eventType != null ? eventType : EventType.NOT_STARTED;
        this.comment = comment;
        this.eventDate = LocalDate.now();
    }

    public ProcurementEvent(ProcurementItem procurementItem, EventType eventType, LocalDate eventDate, String comment) {
        this.procurementItem = procurementItem;
        this.eventType = eventType != null ? eventType : EventType.NOT_STARTED;
        this.eventDate = eventDate != null ? eventDate : LocalDate.now();
        this.comment = comment;
    }

    /**
     * Factory method to create a status change event.
     *
     * @param procurementItem the procurement item
     * @param oldStatus the old status
     * @param newStatus the new status
     * @param comment optional comment
     * @param createdBy the user who made the change
     * @return the new event
     */
    public static ProcurementEvent createStatusChangeEvent(ProcurementItem procurementItem,
            String oldStatus, String newStatus, String comment, String createdBy) {
        ProcurementEvent event = new ProcurementEvent(procurementItem, EventType.NOT_STARTED, comment);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setCreatedBy(createdBy);
        return event;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcurementItem getProcurementItem() {
        return procurementItem;
    }

    public void setProcurementItem(ProcurementItem procurementItem) {
        this.procurementItem = procurementItem;
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
        this.eventDate = eventDate != null ? eventDate : LocalDate.now();
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

    public List<ProcurementEventFile> getFiles() {
        return files;
    }

    public void setFiles(List<ProcurementEventFile> files) {
        this.files = files;
    }

    /**
     * Add a file to this event.
     *
     * @param file the file to add
     */
    public void addFile(ProcurementEventFile file) {
        files.add(file);
        file.setEvent(this);
    }

    /**
     * Remove a file from this event.
     *
     * @param file the file to remove
     */
    public void removeFile(ProcurementEventFile file) {
        files.remove(file);
        file.setEvent(null);
    }

    /**
     * Get the version for optimistic locking.
     *
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Set the version for optimistic locking.
     *
     * @param version the version number
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcurementEvent that = (ProcurementEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProcurementEvent{" +
                "id=" + id +
                ", procurementItemId=" + (procurementItem != null ? procurementItem.getId() : null) +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(50, comment.length())) : null) + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", active=" + active +
                '}';
    }
}
