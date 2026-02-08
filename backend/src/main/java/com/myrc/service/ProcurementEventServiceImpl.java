/*
 * myRC - Procurement Event Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 *
 * Description:
 * Implementation of ProcurementEventService for managing procurement events.
 */
package com.myrc.service;

import com.myrc.dto.ProcurementEventDTO;
import com.myrc.dto.ProcurementEventFileDTO;
import com.myrc.model.ProcurementEvent;
import com.myrc.model.ProcurementEventFile;
import com.myrc.model.ProcurementItem;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.ProcurementEventFileRepository;
import com.myrc.repository.ProcurementEventRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import com.myrc.service.RCPermissionService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of ProcurementEventService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Service
@Transactional
public class ProcurementEventServiceImpl implements ProcurementEventService {

    private static final Logger logger = Logger.getLogger(ProcurementEventServiceImpl.class.getName());

    /** Maximum file size: 50 MB */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    private final ProcurementEventRepository eventRepository;
    private final ProcurementEventFileRepository eventFileRepository;
    private final ProcurementItemRepository procurementItemRepository;
    private final ResponsibilityCentreRepository rcRepository;
    private final RCAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final RCPermissionService permissionService;

    public ProcurementEventServiceImpl(ProcurementEventRepository eventRepository,
                                        ProcurementEventFileRepository eventFileRepository,
                                        ProcurementItemRepository procurementItemRepository,
                                        ResponsibilityCentreRepository rcRepository,
                                        RCAccessRepository accessRepository,
                                        UserRepository userRepository,
                                        RCPermissionService permissionService) {
        this.eventRepository = eventRepository;
        this.eventFileRepository = eventFileRepository;
        this.procurementItemRepository = procurementItemRepository;
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
     * Get a procurement item and verify the user has access.
     *
     * @param procurementItemId the procurement item ID
     * @param username the username
     * @param requireWrite whether write access is required
     * @return the procurement item
     * @throws IllegalArgumentException if not found or no access
     */
    private ProcurementItem getProcurementItemWithAccess(Long procurementItemId, String username, boolean requireWrite) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
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
    public List<ProcurementEventDTO> getEventsForProcurementItem(Long procurementItemId, String username) {
        getProcurementItemWithAccess(procurementItemId, username, false);

        List<ProcurementEvent> events = eventRepository.findByProcurementItemIdAndActiveTrue(procurementItemId);
        return events.stream()
                .map(ProcurementEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementEventDTO getEventById(Long eventId, String username) {
        Optional<ProcurementEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        ProcurementEvent event = eventOpt.get();
        Long procurementItemId = event.getProcurementItem().getId();
        getProcurementItemWithAccess(procurementItemId, username, false);

        return ProcurementEventDTO.fromEntity(event);
    }

    @Override
    public ProcurementEventDTO createEvent(Long procurementItemId, ProcurementEventDTO dto, String username) {
        ProcurementItem procurementItem = getProcurementItemWithAccess(procurementItemId, username, true);

        // Parse event type
        ProcurementEvent.EventType eventType = ProcurementEvent.EventType.NOT_STARTED;
        if (dto.getEventType() != null && !dto.getEventType().trim().isEmpty()) {
            try {
                eventType = ProcurementEvent.EventType.valueOf(dto.getEventType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid event type: " + dto.getEventType());
            }
        }

        // Create event
        ProcurementEvent event = new ProcurementEvent();
        event.setProcurementItem(procurementItem);
        event.setEventType(eventType);
        event.setEventDate(dto.getEventDate() != null ? dto.getEventDate() : LocalDate.now());
        event.setComment(dto.getComment());
        event.setOldStatus(dto.getOldStatus());
        event.setNewStatus(dto.getNewStatus());
        event.setCreatedBy(username);

        ProcurementEvent saved = eventRepository.save(event);
        logger.info("Created procurement event " + saved.getId() + " for item " + procurementItemId + " by user " + username);

        return ProcurementEventDTO.fromEntity(saved);
    }

    @Override
    public ProcurementEventDTO updateEvent(Long eventId, ProcurementEventDTO dto, String username) {
        Optional<ProcurementEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        ProcurementEvent event = eventOpt.get();
        Long procurementItemId = event.getProcurementItem().getId();
        getProcurementItemWithAccess(procurementItemId, username, true);

        // Update event type if provided
        if (dto.getEventType() != null && !dto.getEventType().trim().isEmpty()) {
            try {
                event.setEventType(ProcurementEvent.EventType.valueOf(dto.getEventType().toUpperCase()));
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
        if (dto.getOldStatus() != null) {
            event.setOldStatus(dto.getOldStatus());
        }
        if (dto.getNewStatus() != null) {
            event.setNewStatus(dto.getNewStatus());
        }

        ProcurementEvent saved = eventRepository.save(event);
        logger.info("Updated procurement event " + eventId + " by user " + username);

        return ProcurementEventDTO.fromEntity(saved);
    }

    @Override
    public void deleteEvent(Long eventId, String username) {
        Optional<ProcurementEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        ProcurementEvent event = eventOpt.get();
        Long procurementItemId = event.getProcurementItem().getId();
        getProcurementItemWithAccess(procurementItemId, username, true);

        // Soft delete
        event.setActive(false);
        eventRepository.save(event);
        logger.info("Deleted procurement event " + eventId + " by user " + username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementEventDTO> getEventsByType(Long procurementItemId, String eventType, String username) {
        getProcurementItemWithAccess(procurementItemId, username, false);

        ProcurementEvent.EventType type;
        try {
            type = ProcurementEvent.EventType.valueOf(eventType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid event type: " + eventType);
        }

        List<ProcurementEvent> events = eventRepository.findByProcurementItemIdAndEventType(procurementItemId, type);
        return events.stream()
                .map(ProcurementEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementEventDTO> getEventsByDateRange(Long procurementItemId, LocalDate startDate,
            LocalDate endDate, String username) {
        getProcurementItemWithAccess(procurementItemId, username, false);

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        List<ProcurementEvent> events = eventRepository.findByProcurementItemIdAndDateRange(
                procurementItemId, startDate, endDate);
        return events.stream()
                .map(ProcurementEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventCount(Long procurementItemId, String username) {
        getProcurementItemWithAccess(procurementItemId, username, false);
        return eventRepository.countByProcurementItemId(procurementItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementEventDTO getMostRecentEvent(Long procurementItemId, String username) {
        getProcurementItemWithAccess(procurementItemId, username, false);

        Optional<ProcurementEvent> eventOpt = eventRepository.findMostRecentByProcurementItemId(procurementItemId);
        return eventOpt.map(ProcurementEventDTO::fromEntity).orElse(null);
    }

    // ==========================
    // Event File Operations
    // ==========================

    @Override
    public ProcurementEventFileDTO uploadEventFile(Long eventId, MultipartFile file, String description, String username) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 50 MB");
        }

        // Get event and verify access
        Optional<ProcurementEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        ProcurementEvent event = eventOpt.get();
        Long procurementItemId = event.getProcurementItem().getId();

        // Verify write access
        getProcurementItemWithAccess(procurementItemId, username, true);

        try {
            // Create file entity
            ProcurementEventFile eventFile = new ProcurementEventFile(
                    file.getOriginalFilename(),
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    file.getSize(),
                    file.getBytes(),
                    event
            );
            eventFile.setDescription(description);

            // Save and return
            ProcurementEventFile saved = eventFileRepository.save(eventFile);
            logger.info("Uploaded file '" + file.getOriginalFilename() + "' to event " + eventId + " by user " + username);

            return ProcurementEventFileDTO.fromEntity(saved);
        } catch (IOException e) {
            logger.severe("Failed to read file content: " + e.getMessage());
            throw new IllegalStateException("Failed to process uploaded file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementEventFileDTO> getEventFiles(Long eventId, String username) {
        // Get event and verify access
        Optional<ProcurementEvent> eventOpt = eventRepository.findByIdAndActiveTrue(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }

        ProcurementEvent event = eventOpt.get();
        Long procurementItemId = event.getProcurementItem().getId();

        // Verify read access
        getProcurementItemWithAccess(procurementItemId, username, false);

        List<ProcurementEventFile> files = eventFileRepository.findByEventIdAndActiveTrue(eventId);
        return files.stream()
                .map(ProcurementEventFileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementEventFile getEventFile(Long fileId, String username) {
        Optional<ProcurementEventFile> fileOpt = eventFileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !Boolean.TRUE.equals(fileOpt.get().getActive())) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementEventFile file = fileOpt.get();
        ProcurementEvent event = file.getEvent();
        Long procurementItemId = event.getProcurementItem().getId();

        // Verify read access
        getProcurementItemWithAccess(procurementItemId, username, false);

        return file;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementEventFileDTO getEventFileMetadata(Long fileId, String username) {
        ProcurementEventFile file = getEventFile(fileId, username);
        return ProcurementEventFileDTO.fromEntity(file);
    }

    @Override
    public ProcurementEventFileDTO updateEventFileDescription(Long fileId, String description, String username) {
        Optional<ProcurementEventFile> fileOpt = eventFileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !Boolean.TRUE.equals(fileOpt.get().getActive())) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementEventFile file = fileOpt.get();
        ProcurementEvent event = file.getEvent();
        Long procurementItemId = event.getProcurementItem().getId();

        // Verify write access
        getProcurementItemWithAccess(procurementItemId, username, true);

        file.setDescription(description);
        ProcurementEventFile saved = eventFileRepository.save(file);
        logger.info("Updated description of file " + fileId + " by user " + username);

        return ProcurementEventFileDTO.fromEntity(saved);
    }

    @Override
    public void deleteEventFile(Long fileId, String username) {
        Optional<ProcurementEventFile> fileOpt = eventFileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !Boolean.TRUE.equals(fileOpt.get().getActive())) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementEventFile file = fileOpt.get();
        ProcurementEvent event = file.getEvent();
        Long procurementItemId = event.getProcurementItem().getId();

        // Verify write access
        getProcurementItemWithAccess(procurementItemId, username, true);

        // Soft delete
        file.setActive(false);
        eventFileRepository.save(file);
        logger.info("Deleted file " + fileId + " by user " + username);
    }
}
