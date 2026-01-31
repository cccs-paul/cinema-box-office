/*
 * myRC - Procurement Event Service Test
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 */
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boxoffice.dto.ProcurementEventDTO;
import com.boxoffice.model.*;
import com.boxoffice.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ProcurementEventServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@ExtendWith(MockitoExtension.class)
class ProcurementEventServiceTest {

    @Mock
    private ProcurementEventRepository eventRepository;

    @Mock
    private ProcurementItemRepository procurementItemRepository;

    @Mock
    private ResponsibilityCentreRepository rcRepository;

    @Mock
    private RCAccessRepository accessRepository;

    @Mock
    private UserRepository userRepository;

    private ProcurementEventServiceImpl eventService;

    private User testUser;
    private User adminUser;
    private ResponsibilityCentre testRC;
    private FiscalYear testFY;
    private ProcurementItem testProcurementItem;
    private ProcurementEvent testEvent;
    private RCAccess testAccess;

    @BeforeEach
    void setUp() {
        // Create the service with all required dependencies
        eventService = new ProcurementEventServiceImpl(
            eventRepository,
            procurementItemRepository,
            rcRepository,
            accessRepository,
            userRepository
        );

        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRoles(Set.of("USER"));

        // Set up admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRoles(Set.of("ADMIN", "USER"));

        // Set up test RC
        testRC = new ResponsibilityCentre();
        testRC.setId(1L);
        testRC.setName("Test RC");
        testRC.setOwner(testUser);

        // Set up test FY
        testFY = new FiscalYear();
        testFY.setId(1L);
        testFY.setName("FY 2025-2026");
        testFY.setResponsibilityCentre(testRC);

        // Set up test procurement item
        testProcurementItem = new ProcurementItem();
        testProcurementItem.setId(1L);
        testProcurementItem.setPurchaseRequisition("PR-2025-001");
        testProcurementItem.setName("Test Procurement Item");
        testProcurementItem.setStatus(ProcurementItem.Status.DRAFT);
        testProcurementItem.setFiscalYear(testFY);
        testProcurementItem.setActive(true);

        // Set up test event
        testEvent = new ProcurementEvent();
        testEvent.setId(1L);
        testEvent.setProcurementItem(testProcurementItem);
        testEvent.setEventType(ProcurementEvent.EventType.NOTE_ADDED);
        testEvent.setEventDate(LocalDate.now());
        testEvent.setComment("Test comment");
        testEvent.setCreatedBy("testuser");
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setActive(true);

        // Set up test access
        testAccess = new RCAccess();
        testAccess.setId(1L);
        testAccess.setUser(testUser);
        testAccess.setResponsibilityCentre(testRC);
        testAccess.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);
    }

    @Nested
    @DisplayName("getEventsForProcurementItem")
    class GetEventsForProcurementItemTests {

        @Test
        @DisplayName("Should return events for procurement item with access")
        void shouldReturnEventsWithAccess() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.findByProcurementItemIdAndActiveTrue(1L))
                .thenReturn(List.of(testEvent));

            // When
            List<ProcurementEventDTO> result = eventService.getEventsForProcurementItem(1L, "testuser");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("NOTE_ADDED", result.get(0).getEventType());
            verify(eventRepository).findByProcurementItemIdAndActiveTrue(1L);
        }

        @Test
        @DisplayName("Should throw exception when procurement item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            // Given - only stub what's needed before the exception
            when(procurementItemRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventsForProcurementItem(999L, "testuser"));
        }

        @Test
        @DisplayName("Admin should have access to all procurement items")
        void adminShouldHaveAccess() {
            // Given
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(eventRepository.findByProcurementItemIdAndActiveTrue(1L))
                .thenReturn(List.of(testEvent));

            // When
            List<ProcurementEventDTO> result = eventService.getEventsForProcurementItem(1L, "admin");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("createEvent")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully")
        void shouldCreateEventSuccessfully() {
            // Given
            ProcurementEventDTO dto = new ProcurementEventDTO();
            dto.setEventType("NOTE_ADDED");
            dto.setEventDate(LocalDate.now());
            dto.setComment("New test comment");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.save(any(ProcurementEvent.class))).thenAnswer(invocation -> {
                ProcurementEvent event = invocation.getArgument(0);
                event.setId(2L);
                event.setCreatedAt(LocalDateTime.now());
                return event;
            });

            // When
            ProcurementEventDTO result = eventService.createEvent(1L, dto, "testuser");

            // Then
            assertNotNull(result);
            assertEquals("NOTE_ADDED", result.getEventType());
            assertEquals("New test comment", result.getComment());
            
            ArgumentCaptor<ProcurementEvent> eventCaptor = ArgumentCaptor.forClass(ProcurementEvent.class);
            verify(eventRepository).save(eventCaptor.capture());
            assertEquals("testuser", eventCaptor.getValue().getCreatedBy());
        }

        @Test
        @DisplayName("Should throw exception for invalid event type")
        void shouldThrowExceptionForInvalidEventType() {
            // Given
            ProcurementEventDTO dto = new ProcurementEventDTO();
            dto.setEventType("INVALID_TYPE");
            dto.setComment("Test comment");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.createEvent(1L, dto, "testuser"));
        }

        @Test
        @DisplayName("Should throw exception without write access")
        void shouldThrowExceptionWithoutWriteAccess() {
            // Given
            ProcurementEventDTO dto = new ProcurementEventDTO();
            dto.setEventType("NOTE_ADDED");
            
            // Create a different user who is not the owner
            User otherUser = new User();
            otherUser.setId(99L);
            otherUser.setUsername("otheruser");
            otherUser.setRoles(Set.of("USER"));
            
            // Create a different RC with different owner
            ResponsibilityCentre otherRC = new ResponsibilityCentre();
            otherRC.setId(99L);
            otherRC.setName("Other RC");
            otherRC.setOwner(adminUser);  // admin is owner, not otherUser
            
            // Update the fiscal year to use the other RC
            testFY.setResponsibilityCentre(otherRC);

            RCAccess readOnlyAccess = new RCAccess();
            readOnlyAccess.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);

            when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(99L)).thenReturn(Optional.of(otherRC));
            when(accessRepository.findByResponsibilityCentreAndUser(otherRC, otherUser))
                .thenReturn(Optional.of(readOnlyAccess));

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.createEvent(1L, dto, "otheruser"));
        }
    }

    @Nested
    @DisplayName("updateEvent")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event successfully")
        void shouldUpdateEventSuccessfully() {
            // Given
            ProcurementEventDTO dto = new ProcurementEventDTO();
            dto.setComment("Updated comment");
            dto.setEventDate(LocalDate.now().plusDays(1));

            when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testEvent));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.save(any(ProcurementEvent.class))).thenReturn(testEvent);

            // When
            ProcurementEventDTO result = eventService.updateEvent(1L, dto, "testuser");

            // Then
            assertNotNull(result);
            verify(eventRepository).save(any(ProcurementEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFound() {
            // Given
            ProcurementEventDTO dto = new ProcurementEventDTO();
            dto.setComment("Updated comment");

            when(eventRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.updateEvent(999L, dto, "testuser"));
        }
    }

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEventTests {

        @Test
        @DisplayName("Should soft delete event successfully")
        void shouldSoftDeleteEventSuccessfully() {
            // Given
            when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testEvent));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.save(any(ProcurementEvent.class))).thenReturn(testEvent);

            // When
            eventService.deleteEvent(1L, "testuser");

            // Then
            ArgumentCaptor<ProcurementEvent> eventCaptor = ArgumentCaptor.forClass(ProcurementEvent.class);
            verify(eventRepository).save(eventCaptor.capture());
            assertFalse(eventCaptor.getValue().getActive());
        }
    }

    @Nested
    @DisplayName("getEventCount")
    class GetEventCountTests {

        @Test
        @DisplayName("Should return event count")
        void shouldReturnEventCount() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.countByProcurementItemId(1L)).thenReturn(5L);

            // When
            long count = eventService.getEventCount(1L, "testuser");

            // Then
            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("getMostRecentEvent")
    class GetMostRecentEventTests {

        @Test
        @DisplayName("Should return most recent event")
        void shouldReturnMostRecentEvent() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.findMostRecentByProcurementItemId(1L))
                .thenReturn(Optional.of(testEvent));

            // When
            ProcurementEventDTO result = eventService.getMostRecentEvent(1L, "testuser");

            // Then
            assertNotNull(result);
            assertEquals("NOTE_ADDED", result.getEventType());
        }

        @Test
        @DisplayName("Should return null when no events")
        void shouldReturnNullWhenNoEvents() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.findMostRecentByProcurementItemId(1L))
                .thenReturn(Optional.empty());

            // When
            ProcurementEventDTO result = eventService.getMostRecentEvent(1L, "testuser");

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getEventsByDateRange")
    class GetEventsByDateRangeTests {

        @Test
        @DisplayName("Should return events in date range")
        void shouldReturnEventsInDateRange() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventRepository.findByProcurementItemIdAndDateRange(1L, startDate, endDate))
                .thenReturn(List.of(testEvent));

            // When
            List<ProcurementEventDTO> result = eventService.getEventsByDateRange(
                1L, startDate, endDate, "testuser");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateAfterEndDate() {
            // Given
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(7);

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventsByDateRange(1L, startDate, endDate, "testuser"));
        }
    }
}
