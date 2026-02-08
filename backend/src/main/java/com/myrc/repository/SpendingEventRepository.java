/*
 * myRC - Spending Event Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * Repository interface for Spending Event entity operations.
 */
package com.myrc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrc.model.SpendingEvent;
import com.myrc.model.SpendingItem;

/**
 * Repository interface for Spending Event entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
@Repository
public interface SpendingEventRepository extends JpaRepository<SpendingEvent, Long> {

    /**
     * Find all active events for a spending item.
     *
     * @param spendingItem the spending item
     * @return list of active events ordered by event date descending
     */
    List<SpendingEvent> findBySpendingItemAndActiveTrueOrderByEventDateDescCreatedAtDesc(
            SpendingItem spendingItem);

    /**
     * Find all active events for a spending item by ID.
     *
     * @param spendingItemId the spending item ID
     * @return list of active events ordered by event date descending
     */
    @Query("SELECT e FROM SpendingEvent e WHERE e.spendingItem.id = :spendingItemId " +
           "AND e.active = true ORDER BY e.eventDate DESC, e.createdAt DESC")
    List<SpendingEvent> findBySpendingItemIdAndActiveTrue(@Param("spendingItemId") Long spendingItemId);

    /**
     * Find an active event by ID.
     *
     * @param id the event ID
     * @return the event if found and active
     */
    Optional<SpendingEvent> findByIdAndActiveTrue(Long id);

    /**
     * Count active events for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @return the count of active events
     */
    @Query("SELECT COUNT(e) FROM SpendingEvent e WHERE e.spendingItem.id = :spendingItemId AND e.active = true")
    long countBySpendingItemIdAndActiveTrue(@Param("spendingItemId") Long spendingItemId);

    /**
     * Find the most recent active event for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @return the most recent event if found
     */
    @Query("SELECT e FROM SpendingEvent e WHERE e.spendingItem.id = :spendingItemId " +
           "AND e.active = true ORDER BY e.eventDate DESC, e.createdAt DESC LIMIT 1")
    Optional<SpendingEvent> findMostRecentBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

    /**
     * Delete all spending events for a spending item.
     * Used during cascade deletion of a responsibility centre.
     *
     * @param spendingItemId the spending item ID
     */
    @Modifying
    @Query("DELETE FROM SpendingEvent e WHERE e.spendingItem.id = :spendingItemId")
    void deleteBySpendingItemId(@Param("spendingItemId") Long spendingItemId);
}
