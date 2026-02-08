/*
 * myRC - Spending Event Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-30
 * Version: 1.0.0
 *
 * Description:
 * Implementation of SpendingEventService for managing spending events.
 */
package com.myrc.service;

import com.myrc.dto.SpendingEventDTO;
import com.myrc.model.SpendingEvent;
import com.myrc.model.SpendingItem;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.SpendingEventRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import com.myrc.service.RCPermissionService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SpendingEventService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-30
 */
@Service
@Transactional
public class SpendingEventServiceImpl implements SpendingEventService {

    private static final Logger logger = Logger.getLogger(SpendingEventServiceImpl.class.getName());

    private final SpendingEventRepository eventRepository;
    private final SpendingItemRepository spendingItemRepository;
    private final ResponsibilityCentreRepository rcRepository;
    private final RCAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final RCPermissionService permissionService;

    public SpendingEventServiceImpl(SpendingEventRepository eventRepository,
                                    SpendingItemRepository spendingItemRepository,
                                    ResponsibilityCentreRepository rcRepository,
                                    RCAccessRepository accessRepository,
                                    UserRepository userRepository,
                                    RCPermissionService permissionService) {
        this.eventRepository = eventRepository;
        this.spendingItemRepository = spendingItemRepository;
        this.rcRepository = rcRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    // ==========================
    // Access Control Helpers
    // ==========================

    /**
     * Check if a user has any access (read or write) to a responsibility centre.
     * Delegates to the centralized RCPermissionService which handles
     * local users, LDAP group-based access, and Demo RC visibility.
     *
     * @param rcId the responsibility centre ID
     * @param username the username
     * @return true if the user has access
     */
    private boolean hasAccessToRC(Long rcId, String username) {
        return permissionService.hasAccess(rcId, username);
    }

    /**
     * Check if a user has write access to a responsibility centre.
     * Delegates to the centralized RCPermissionService.
     *
     * @param rcId the responsibility centre ID
     * @param username the username
     * @return true if the user has write access
     */
    private boolean hasWriteAccessToRC(Long rcId, String username) {
        return permissionService.hasWriteAccess(rcId, username);
    }

    /**
     * Get a spending item and verify the user has access.
     *
     * @param spendingItemId the spending item ID
     * @param username the username
     * @param requireWrite whether write access is required
     * @return the spending item
     * @throws IllegalArgumentException if not found or no access
     */
    private SpendingItem getSpendingItemWithAccess(Long spendingItemId, String username, boolean requireWrite) {
        Optional<SpendingItem> itemOpt = spendingItemRepository.findById(spendingItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Spending item not found");
        }

        SpendingItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (requireWrite) {
            if (!hasWriteAccessToRC(rcId, username)) {
                throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
            }
        } else {
            if (!hasAccessToRC(rcId, username)) {
                throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
            }
        }

        return item;
    }

    // ==========================
    // Event Operations
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public List<SpendingEventDTO> getEventsForSpendingItem(Long spendingItemId, String username) {
        getSpendingItemWithAccess(spendingItemId, username, false);

        List<SpendingEvent> events = eventRepository.findBySpendingItemIdAndActiveTrue(spendingItemId);
        return events.stream()
                .map(SpendingEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SpendingEventDTO getEventById(Long eventId, String username) {
        Optional<SpendingEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        SpendingEvent event = eventOpt.get();
        Long spendingItemId = event.getSpendingItem().getId();
        getSpendingItemWithAccess(spendingItemId, username, false);

        return SpendingEventDTO.fromEntity(event);
    }

    @Override
    public SpendingEventDTO createEvent(Long spendingItemId, SpendingEventDTO dto, String username) {
        SpendingItem spendingItem = getSpendingItemWithAccess(spendingItemId, username, true);

        // Check if the spending item is linked to a procurement item
        if (spendingItem.getProcurementItem() != null) {
            throw new IllegalArgumentException("Cannot create tracking events for spending items linked to procurement. " +
                    "Use the linked procurement item's tracking events instead.");
        }

        // Parse event type
        SpendingEvent.EventType eventType = SpendingEvent.EventType.PENDING;
        if (dto.getEventType() != null && !dto.getEventType().trim().isEmpty()) {
            try {
                eventType = SpendingEvent.EventType.valueOf(dto.getEventType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid event type: " + dto.getEventType());
            }
        }

        // Create event
        SpendingEvent event = new SpendingEvent();
        event.setSpendingItem(spendingItem);
        event.setEventType(eventType);
        event.setEventDate(dto.getEventDate() != null ? dto.getEventDate() : LocalDate.now());
        event.setComment(dto.getComment());
        event.setCreatedBy(username);

        SpendingEvent saved = eventRepository.save(event);
        logger.info("Created spending event " + saved.getId() + " for item " + spendingItemId + " by user " + username);

        return SpendingEventDTO.fromEntity(saved);
    }

    @Override
    public SpendingEventDTO updateEvent(Long eventId, SpendingEventDTO dto, String username) {
        Optional<SpendingEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        SpendingEvent event = eventOpt.get();
        Long spendingItemId = event.getSpendingItem().getId();
        getSpendingItemWithAccess(spendingItemId, username, true);

        // Update event type if provided
        if (dto.getEventType() != null && !dto.getEventType().trim().isEmpty()) {
            try {
                event.setEventType(SpendingEvent.EventType.valueOf(dto.getEventType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid event type: " + dto.getEventType());
            }
        }

        // Update other fields
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getComment() != null) {
            event.setComment(dto.getComment());
        }

        SpendingEvent saved = eventRepository.save(event);
        logger.info("Updated spending event " + eventId + " by user " + username);

        return SpendingEventDTO.fromEntity(saved);
    }

    @Override
    public void deleteEvent(Long eventId, String username) {
        Optional<SpendingEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        SpendingEvent event = eventOpt.get();
        Long spendingItemId = event.getSpendingItem().getId();
        getSpendingItemWithAccess(spendingItemId, username, true);

        // Soft delete
        event.setActive(false);
        eventRepository.save(event);
        logger.info("Deleted spending event " + eventId + " by user " + username);
    }

    @Override
    @Transactional(readOnly = true)
    public SpendingEventDTO getMostRecentEvent(Long spendingItemId, String username) {
        getSpendingItemWithAccess(spendingItemId, username, false);

        Optional<SpendingEvent> eventOpt = eventRepository.findMostRecentBySpendingItemId(spendingItemId);
        return eventOpt.map(SpendingEventDTO::fromEntity).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventCount(Long spendingItemId) {
        return eventRepository.countBySpendingItemIdAndActiveTrue(spendingItemId);
    }
}
