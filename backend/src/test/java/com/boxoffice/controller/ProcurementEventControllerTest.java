/*
 * myRC - Procurement Event Controller Test
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 */
package com.boxoffice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boxoffice.dto.ProcurementEventDTO;
import com.boxoffice.service.ProcurementEventService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for ProcurementEventController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@ExtendWith(MockitoExtension.class)
class ProcurementEventControllerTest {

    @Mock
    private ProcurementEventService eventService;

    private Authentication authentication;
    private ProcurementEventController controller;
    private ProcurementEventDTO testEventDTO;

    @BeforeEach
    void setUp() {
        controller = new ProcurementEventController(eventService);
        authentication = createAuthentication("testuser");

        testEventDTO = new ProcurementEventDTO();
        testEventDTO.setId(1L);
        testEventDTO.setProcurementItemId(1L);
        testEventDTO.setProcurementItemName("Test Procurement Item");
        testEventDTO.setEventType("NOTE_ADDED");
        testEventDTO.setEventDate(LocalDate.now());
        testEventDTO.setComment("Test comment");
        testEventDTO.setCreatedBy("testuser");
        testEventDTO.setCreatedAt(LocalDateTime.now());
        testEventDTO.setActive(true);
    }

    private Authentication createAuthentication(String username) {
        return new Authentication() {
            @Override
            public String getName() { return username; }
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
            @Override
            public Object getCredentials() { return null; }
            @Override
            public Object getDetails() { return null; }
            @Override
            public Object getPrincipal() { return username; }
            @Override
            public boolean isAuthenticated() { return true; }
            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
        };
    }

    @Test
    @DisplayName("Should create controller successfully")
    void testControllerCreation() {
        assertNotNull(controller);
    }

    @Nested
    @DisplayName("getEvents Tests")
    class GetEventsTests {

        @Test
        @DisplayName("Should return list of events")
        void shouldReturnListOfEvents() {
            when(eventService.getEventsForProcurementItem(1L, "testuser"))
                .thenReturn(List.of(testEventDTO));

            ResponseEntity<?> response =
                controller.getEvents(1L, 1L, 1L, null, null, null, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, ((java.util.List<?>) response.getBody()).size());
            assertEquals("NOTE_ADDED", ((java.util.List<ProcurementEventDTO>) response.getBody()).get(0).getEventType());
        }

        @Test
        @DisplayName("Should filter events by type")
        void shouldFilterEventsByType() {
            when(eventService.getEventsByType(1L, "STATUS_CHANGE", "testuser"))
                .thenReturn(List.of());

            ResponseEntity<?> response =
                controller.getEvents(1L, 1L, 1L, "STATUS_CHANGE", null, null, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(eventService).getEventsByType(1L, "STATUS_CHANGE", "testuser");
        }

        @Test
        @DisplayName("Should filter events by date range")
        void shouldFilterEventsByDateRange() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(eventService.getEventsByDateRange(1L, startDate, endDate, "testuser"))
                .thenReturn(List.of(testEventDTO));

            ResponseEntity<?> response =
                controller.getEvents(1L, 1L, 1L, null, startDate, endDate, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(eventService).getEventsByDateRange(1L, startDate, endDate, "testuser");
        }

        @Test
        @DisplayName("Should return bad request when service throws IllegalArgumentException")
        void shouldReturnBadRequestWhenServiceFails() {
            when(eventService.getEventsForProcurementItem(1L, "testuser"))
                .thenThrow(new IllegalArgumentException("Access denied"));

            ResponseEntity<?> response =
                controller.getEvents(1L, 1L, 1L, null, null, null, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getEvent Tests")
    class GetEventTests {

        @Test
        @DisplayName("Should return event by ID")
        void shouldReturnEventById() {
            when(eventService.getEventById(1L, "testuser"))
                .thenReturn(testEventDTO);

            ResponseEntity<?> response =
                controller.getEvent(1L, 1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("NOTE_ADDED", ((ProcurementEventDTO) response.getBody()).getEventType());
        }

        @Test
        @DisplayName("Should return not found when event does not exist")
        void shouldReturnNotFoundWhenEventDoesNotExist() {
            when(eventService.getEventById(999L, "testuser"))
                .thenThrow(new IllegalArgumentException("Event not found"));

            ResponseEntity<?> response =
                controller.getEvent(1L, 1L, 1L, 999L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getEventCount Tests")
    class GetEventCountTests {

        @Test
        @DisplayName("Should return event count")
        void shouldReturnEventCount() {
            when(eventService.getEventCount(1L, "testuser"))
                .thenReturn(5L);

            ResponseEntity<Long> response =
                controller.getEventCount(1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(5L, response.getBody());
        }

        @Test
        @DisplayName("Should return not found when procurement item not found")
        void shouldReturnNotFoundWhenProcurementItemNotFound() {
            when(eventService.getEventCount(1L, "testuser"))
                .thenThrow(new IllegalArgumentException("Procurement item not found"));

            ResponseEntity<Long> response =
                controller.getEventCount(1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getMostRecentEvent Tests")
    class GetMostRecentEventTests {

        @Test
        @DisplayName("Should return most recent event")
        void shouldReturnMostRecentEvent() {
            when(eventService.getMostRecentEvent(1L, "testuser"))
                .thenReturn(testEventDTO);

            ResponseEntity<?> response =
                controller.getMostRecentEvent(1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("NOTE_ADDED", ((ProcurementEventDTO) response.getBody()).getEventType());
        }

        @Test
        @DisplayName("Should return no content when no events")
        void shouldReturnNoContentWhenNoEvents() {
            when(eventService.getMostRecentEvent(1L, "testuser"))
                .thenReturn(null);

            ResponseEntity<?> response =
                controller.getMostRecentEvent(1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("createEvent Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully")
        void shouldCreateEventSuccessfully() {
            ProcurementEventDTO request = new ProcurementEventDTO();
            request.setEventType("NOTE_ADDED");
            request.setComment("New comment");

            when(eventService.createEvent(eq(1L), any(ProcurementEventDTO.class), eq("testuser")))
                .thenReturn(testEventDTO);

            ResponseEntity<?> response =
                controller.createEvent(1L, 1L, 1L, request, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Should return bad request for invalid event type")
        void shouldReturnBadRequestForInvalidEventType() {
            ProcurementEventDTO request = new ProcurementEventDTO();
            request.setEventType("INVALID");

            when(eventService.createEvent(eq(1L), any(ProcurementEventDTO.class), eq("testuser")))
                .thenThrow(new IllegalArgumentException("Invalid event type"));

            ResponseEntity<?> response =
                controller.createEvent(1L, 1L, 1L, request, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("updateEvent Tests")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event successfully")
        void shouldUpdateEventSuccessfully() {
            ProcurementEventDTO request = new ProcurementEventDTO();
            request.setComment("Updated comment");

            when(eventService.updateEvent(eq(1L), any(ProcurementEventDTO.class), eq("testuser")))
                .thenReturn(testEventDTO);

            ResponseEntity<?> response =
                controller.updateEvent(1L, 1L, 1L, 1L, request, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Should return bad request when service throws exception")
        void shouldReturnBadRequestWhenServiceThrows() {
            ProcurementEventDTO request = new ProcurementEventDTO();
            request.setComment("Updated comment");

            when(eventService.updateEvent(eq(999L), any(ProcurementEventDTO.class), eq("testuser")))
                .thenThrow(new IllegalArgumentException("Event not found"));

            ResponseEntity<?> response =
                controller.updateEvent(1L, 1L, 1L, 999L, request, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("deleteEvent Tests")
    class DeleteEventTests {

        @Test
        @DisplayName("Should delete event successfully")
        void shouldDeleteEventSuccessfully() {
            doNothing().when(eventService).deleteEvent(1L, "testuser");

            ResponseEntity<Void> response =
                controller.deleteEvent(1L, 1L, 1L, 1L, authentication);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return not found when event does not exist")
        void shouldReturnNotFoundWhenEventDoesNotExist() {
            doThrow(new IllegalArgumentException("Event not found"))
                .when(eventService).deleteEvent(999L, "testuser");

            ResponseEntity<Void> response =
                controller.deleteEvent(1L, 1L, 1L, 999L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}
