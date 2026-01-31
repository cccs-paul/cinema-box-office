/*
 * myRC - Procurement Event Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Repository interface for Procurement Event entity operations.
 */
package com.boxoffice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boxoffice.model.ProcurementEvent;
import com.boxoffice.model.ProcurementItem;

/**
 * Repository interface for Procurement Event entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Repository
public interface ProcurementEventRepository extends JpaRepository<ProcurementEvent, Long> {

    /**
     * Find all active events for a procurement item.
     *
     * @param procurementItem the procurement item
     * @return list of active events ordered by event date descending
     */
    List<ProcurementEvent> findByProcurementItemAndActiveTrueOrderByEventDateDescCreatedAtDesc(
            ProcurementItem procurementItem);

    /**
     * Find all active events for a procurement item by ID.
     *
     * @param procurementItemId the procurement item ID
     * @return list of active events ordered by event date descending
     */
    @Query("SELECT e FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId " +
           "AND e.active = true ORDER BY e.eventDate DESC, e.createdAt DESC")
    List<ProcurementEvent> findByProcurementItemIdAndActiveTrue(@Param("procurementItemId") Long procurementItemId);

    /**
     * Find an active event by ID.
     *
     * @param id the event ID
     * @return the event if found and active
     */
    Optional<ProcurementEvent> findByIdAndActiveTrue(Long id);

    /**
     * Find events by type for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param eventType the event type
     * @return list of matching events
     */
    @Query("SELECT e FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId " +
           "AND e.eventType = :eventType AND e.active = true ORDER BY e.eventDate DESC")
    List<ProcurementEvent> findByProcurementItemIdAndEventType(
            @Param("procurementItemId") Long procurementItemId,
            @Param("eventType") ProcurementEvent.EventType eventType);

    /**
     * Find events within a date range for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of matching events
     */
    @Query("SELECT e FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId " +
           "AND e.eventDate >= :startDate AND e.eventDate <= :endDate AND e.active = true " +
           "ORDER BY e.eventDate DESC")
    List<ProcurementEvent> findByProcurementItemIdAndDateRange(
            @Param("procurementItemId") Long procurementItemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count active events for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @return the count of active events
     */
    @Query("SELECT COUNT(e) FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId " +
           "AND e.active = true")
    long countByProcurementItemId(@Param("procurementItemId") Long procurementItemId);

    /**
     * Check if a procurement item has any events.
     *
     * @param procurementItem the procurement item
     * @return true if events exist
     */
    boolean existsByProcurementItemAndActiveTrue(ProcurementItem procurementItem);

    /**
     * Find the most recent event for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @return the most recent event if any
     */
    @Query("SELECT e FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId " +
           "AND e.active = true ORDER BY e.eventDate DESC, e.createdAt DESC LIMIT 1")
    Optional<ProcurementEvent> findMostRecentByProcurementItemId(@Param("procurementItemId") Long procurementItemId);

    /**
     * Find all events created by a specific user.
     *
     * @param createdBy the username
     * @return list of events
     */
    @Query("SELECT e FROM ProcurementEvent e WHERE e.createdBy = :createdBy AND e.active = true " +
           "ORDER BY e.eventDate DESC, e.createdAt DESC")
    List<ProcurementEvent> findByCreatedBy(@Param("createdBy") String createdBy);
}
