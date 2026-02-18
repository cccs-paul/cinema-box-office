/*
 * myRC - Procurement Event Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Procurement Event operations.
 */
package com.myrc.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.myrc.dto.ProcurementEventDTO;
import com.myrc.dto.ProcurementEventFileDTO;
import com.myrc.model.ProcurementEventFile;

/**
 * Service interface for Procurement Event management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
public interface ProcurementEventService {

    /**
     * Get all events for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return list of events ordered by date descending
     */
    List<ProcurementEventDTO> getEventsForProcurementItem(Long procurementItemId, String username);

    /**
     * Get a specific event by ID.
     *
     * @param eventId the event ID
     * @param username the requesting user's username
     * @return the event DTO
     */
    ProcurementEventDTO getEventById(Long eventId, String username);

    /**
     * Create a new event for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param dto the event data
     * @param username the creating user's username
     * @return the created event DTO
     */
    ProcurementEventDTO createEvent(Long procurementItemId, ProcurementEventDTO dto, String username);

    /**
     * Update an existing event.
     *
     * @param eventId the event ID
     * @param dto the updated event data
     * @param username the updating user's username
     * @return the updated event DTO
     */
    ProcurementEventDTO updateEvent(Long eventId, ProcurementEventDTO dto, String username);

    /**
     * Delete (soft delete) an event.
     *
     * @param eventId the event ID
     * @param username the deleting user's username
     */
    void deleteEvent(Long eventId, String username);

    /**
     * Get events by type for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param eventType the event type
     * @param username the requesting user's username
     * @return list of matching events
     */
    List<ProcurementEventDTO> getEventsByType(Long procurementItemId, String eventType, String username);

    /**
     * Get events within a date range for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param username the requesting user's username
     * @return list of matching events
     */
    List<ProcurementEventDTO> getEventsByDateRange(Long procurementItemId, LocalDate startDate, 
            LocalDate endDate, String username);

    /**
     * Get the count of events for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return the count of events
     */
    long getEventCount(Long procurementItemId, String username);

    /**
     * Get the most recent event for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param username the requesting user's username
     * @return the most recent event, or null if none
     */
    ProcurementEventDTO getMostRecentEvent(Long procurementItemId, String username);

    // ==========================
    // Event File Operations
    // ==========================

    /**
     * Upload a file to an event.
     *
     * @param eventId the event ID
     * @param file the file to upload
     * @param description optional description for the file
     * @param username the uploading user's username
     * @return the created file DTO
     */
    ProcurementEventFileDTO uploadEventFile(Long eventId, MultipartFile file, String description, String username);

    /**
     * Get all active files for an event.
     *
     * @param eventId the event ID
     * @param username the requesting user's username
     * @return list of file DTOs
     */
    List<ProcurementEventFileDTO> getEventFiles(Long eventId, String username);

    /**
     * Get a specific file by ID (includes content for download).
     *
     * @param fileId the file ID
     * @param username the requesting user's username
     * @return the file entity with content
     */
    ProcurementEventFile getEventFile(Long fileId, String username);

    /**
     * Get a specific file's metadata (without content).
     *
     * @param fileId the file ID
     * @param username the requesting user's username
     * @return the file DTO
     */
    ProcurementEventFileDTO getEventFileMetadata(Long fileId, String username);

    /**
     * Update a file's description.
     *
     * @param fileId the file ID
     * @param description the new description
     * @param username the updating user's username
     * @return the updated file DTO
     */
    ProcurementEventFileDTO updateEventFileDescription(Long fileId, String description, String username);

    /**
     * Delete (soft delete) a file.
     *
     * @param fileId the file ID
     * @param username the deleting user's username
     */
    void deleteEventFile(Long fileId, String username);

    /**
     * Replace an existing event file with a new one.
     *
     * @param fileId the file ID to replace
     * @param file the new file
     * @param description optional new description
     * @param username the replacing user's username
     * @return the updated file DTO
     */
    ProcurementEventFileDTO replaceEventFile(Long fileId, MultipartFile file, String description, String username);
}
