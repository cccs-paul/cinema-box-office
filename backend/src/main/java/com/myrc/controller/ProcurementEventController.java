/*
 * myRC - Procurement Event REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Procurement Event management.
 */
package com.myrc.controller;

import com.myrc.dto.ErrorResponse;
import com.myrc.dto.ProcurementEventDTO;
import com.myrc.service.ProcurementEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Procurement Event management.
 * Procurement events track the history and progress of procurement items.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/procurement-items/{procurementItemId}/events")
@Tag(name = "Procurement Events", description = "APIs for managing procurement item events and tracking")
public class ProcurementEventController {

    private static final Logger logger = Logger.getLogger(ProcurementEventController.class.getName());
    private final ProcurementEventService eventService;

    public ProcurementEventController(ProcurementEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Get the username from the authentication principal.
     *
     * @param authentication the authentication object
     * @return the username
     */
    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "default-user";
        }
        return authentication.getName();
    }

    // ==========================
    // Event Endpoints
    // ==========================

    /**
     * Get all events for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param eventType optional event type filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param authentication the authentication principal
     * @return list of events
     */
    @GetMapping
    @Operation(summary = "Get all events for a procurement item",
            description = "Retrieves all events for a procurement item, optionally filtered by type or date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getEvents(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /procurement-items/" + procurementItemId + "/events - Fetching events for user: " + username);

        try {
            List<ProcurementEventDTO> events;
            if (eventType != null && !eventType.trim().isEmpty()) {
                events = eventService.getEventsByType(procurementItemId, eventType, username);
            } else if (startDate != null && endDate != null) {
                events = eventService.getEventsByDateRange(procurementItemId, startDate, endDate, username);
            } else {
                events = eventService.getEventsForProcurementItem(procurementItemId, username);
            }
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            logger.warning("Bad request fetching events: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error fetching events: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Get a specific event by ID.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param eventId the event ID
     * @param authentication the authentication principal
     * @return the event
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Get a specific event",
            description = "Retrieves a specific procurement event by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementEventDTO> getEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long eventId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /procurement-items/" + procurementItemId + "/events/" + eventId + " for user: " + username);

        try {
            ProcurementEventDTO event = eventService.getEventById(eventId, username);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            logger.warning("Event not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error fetching event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the count of events for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param authentication the authentication principal
     * @return the count of events
     */
    @GetMapping("/count")
    @Operation(summary = "Get event count",
            description = "Returns the number of events for a procurement item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> getEventCount(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            Authentication authentication) {
        String username = getUsername(authentication);

        try {
            long count = eventService.getEventCount(procurementItemId, username);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            logger.warning("Procurement item not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error fetching event count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the most recent event for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param authentication the authentication principal
     * @return the most recent event, or 204 if none
     */
    @GetMapping("/latest")
    @Operation(summary = "Get most recent event",
            description = "Returns the most recent event for a procurement item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No events found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementEventDTO> getMostRecentEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            Authentication authentication) {
        String username = getUsername(authentication);

        try {
            ProcurementEventDTO event = eventService.getMostRecentEvent(procurementItemId, username);
            if (event == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            logger.warning("Procurement item not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error fetching most recent event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new event for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param dto the event data
     * @param authentication the authentication principal
     * @return the created event
     */
    @PostMapping
    @Operation(summary = "Create a new event",
            description = "Creates a new event for a procurement item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestBody ProcurementEventDTO dto,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST /procurement-items/" + procurementItemId + "/events - Creating event for user: " + username);

        try {
            ProcurementEventDTO created = eventService.createEvent(procurementItemId, dto, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warning("Bad request creating event: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error creating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Update an existing event.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param eventId the event ID
     * @param dto the updated event data
     * @param authentication the authentication principal
     * @return the updated event
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "Update an event",
            description = "Updates an existing procurement event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long eventId,
            @RequestBody ProcurementEventDTO dto,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT /procurement-items/" + procurementItemId + "/events/" + eventId + " for user: " + username);

        try {
            ProcurementEventDTO updated = eventService.updateEvent(eventId, dto, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warning("Bad request updating event: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error updating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Delete an event (soft delete).
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param eventId the event ID
     * @param authentication the authentication principal
     * @return 204 No Content on success
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event",
            description = "Deletes a procurement event (soft delete).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long eventId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("DELETE /procurement-items/" + procurementItemId + "/events/" + eventId + " for user: " + username);

        try {
            eventService.deleteEvent(eventId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Event not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error deleting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
