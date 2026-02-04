/*
 * myRC - Spending Event REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Spending Event management.
 */
package com.myrc.controller;

import com.myrc.dto.ErrorResponse;
import com.myrc.dto.SpendingEventDTO;
import com.myrc.service.SpendingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Spending Event management.
 * Spending events track the history and progress of spending items that are not linked to procurement.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/spending-items/{spendingItemId}/events")
@Tag(name = "Spending Events", description = "APIs for managing spending item events and tracking")
public class SpendingEventController {

    private static final Logger logger = Logger.getLogger(SpendingEventController.class.getName());
    private final SpendingEventService eventService;

    public SpendingEventController(SpendingEventService eventService) {
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
     * Get all events for a spending item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param spendingItemId the spending item ID
     * @param authentication the authentication principal
     * @return list of events
     */
    @GetMapping
    @Operation(summary = "Get all events for a spending item",
            description = "Retrieves all events for a spending item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Spending item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getEvents(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /spending-items/" + spendingItemId + "/events - Fetching events for user: " + username);

        try {
            List<SpendingEventDTO> events = eventService.getEventsForSpendingItem(spendingItemId, username);
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
     * @param spendingItemId the spending item ID
     * @param eventId the event ID
     * @param authentication the authentication principal
     * @return the event
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Get a specific event",
            description = "Retrieves a specific spending event by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SpendingEventDTO> getEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @PathVariable Long eventId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /spending-items/" + spendingItemId + "/events/" + eventId + " for user: " + username);

        try {
            SpendingEventDTO event = eventService.getEventById(eventId, username);
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
     * Get the count of events for a spending item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param spendingItemId the spending item ID
     * @param authentication the authentication principal
     * @return the count of events
     */
    @GetMapping("/count")
    @Operation(summary = "Get event count",
            description = "Returns the number of events for a spending item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Spending item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> getEventCount(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            Authentication authentication) {
        String username = getUsername(authentication);

        try {
            long count = eventService.getEventCount(spendingItemId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            logger.warning("Spending item not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error fetching event count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the most recent event for a spending item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param spendingItemId the spending item ID
     * @param authentication the authentication principal
     * @return the most recent event, or 204 if none
     */
    @GetMapping("/latest")
    @Operation(summary = "Get most recent event",
            description = "Returns the most recent event for a spending item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No events found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Spending item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SpendingEventDTO> getMostRecentEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            Authentication authentication) {
        String username = getUsername(authentication);

        try {
            SpendingEventDTO event = eventService.getMostRecentEvent(spendingItemId, username);
            if (event == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            logger.warning("Spending item not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error fetching most recent event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new event for a spending item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param spendingItemId the spending item ID
     * @param dto the event data
     * @param authentication the authentication principal
     * @return the created event
     */
    @PostMapping
    @Operation(summary = "Create a new event",
            description = "Creates a new event for a spending item. Only allowed for spending items not linked to procurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or spending item is linked to procurement"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Spending item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createEvent(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long spendingItemId,
            @RequestBody SpendingEventDTO dto,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST /spending-items/" + spendingItemId + "/events - Creating event for user: " + username);

        try {
            SpendingEventDTO created = eventService.createEvent(spendingItemId, dto, username);
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
     * @param spendingItemId the spending item ID
     * @param eventId the event ID
     * @param dto the updated event data
     * @param authentication the authentication principal
     * @return the updated event
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "Update an event",
            description = "Updates an existing spending event.")
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
            @PathVariable Long spendingItemId,
            @PathVariable Long eventId,
            @RequestBody SpendingEventDTO dto,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT /spending-items/" + spendingItemId + "/events/" + eventId + " for user: " + username);

        try {
            SpendingEventDTO updated = eventService.updateEvent(eventId, dto, username);
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
     * @param spendingItemId the spending item ID
     * @param eventId the event ID
     * @param authentication the authentication principal
     * @return 204 No Content on success
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event",
            description = "Soft deletes a spending event.")
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
            @PathVariable Long spendingItemId,
            @PathVariable Long eventId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("DELETE /spending-items/" + spendingItemId + "/events/" + eventId + " for user: " + username);

        try {
            eventService.deleteEvent(eventId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Event not found for delete: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error deleting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
