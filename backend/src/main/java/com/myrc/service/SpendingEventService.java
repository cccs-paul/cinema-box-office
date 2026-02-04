/*
 * myRC - Spending Event Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Spending Event operations.
 */
package com.myrc.service;

import java.time.LocalDate;
import java.util.List;

import com.myrc.dto.SpendingEventDTO;

/**
 * Service interface for Spending Event management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
public interface SpendingEventService {

    /**
     * Get all events for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @param username the requesting user's username
     * @return list of events ordered by date descending
     */
    List<SpendingEventDTO> getEventsForSpendingItem(Long spendingItemId, String username);

    /**
     * Get a specific event by ID.
     *
     * @param eventId the event ID
     * @param username the requesting user's username
     * @return the event DTO
     */
    SpendingEventDTO getEventById(Long eventId, String username);

    /**
     * Create a new event for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @param dto the event data
     * @param username the creating user's username
     * @return the created event DTO
     */
    SpendingEventDTO createEvent(Long spendingItemId, SpendingEventDTO dto, String username);

    /**
     * Update an existing event.
     *
     * @param eventId the event ID
     * @param dto the updated event data
     * @param username the updating user's username
     * @return the updated event DTO
     */
    SpendingEventDTO updateEvent(Long eventId, SpendingEventDTO dto, String username);

    /**
     * Delete (soft delete) an event.
     *
     * @param eventId the event ID
     * @param username the deleting user's username
     */
    void deleteEvent(Long eventId, String username);

    /**
     * Get the most recent event for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @param username the requesting user's username
     * @return the most recent event or null
     */
    SpendingEventDTO getMostRecentEvent(Long spendingItemId, String username);

    /**
     * Get the count of events for a spending item.
     *
     * @param spendingItemId the spending item ID
     * @return the event count
     */
    long getEventCount(Long spendingItemId);
}
