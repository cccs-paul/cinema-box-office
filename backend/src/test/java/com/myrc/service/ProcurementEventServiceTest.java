/*
 * myRC - Procurement Event Service Test
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-29
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.myrc.dto.ProcurementEventDTO;
import com.myrc.dto.ProcurementEventFileDTO;
import com.myrc.model.*;
import com.myrc.repository.*;
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
import org.springframework.mock.web.MockMultipartFile;

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
    private ProcurementEventFileRepository eventFileRepository;

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
    private ProcurementEventFile testEventFile;
    private RCAccess testAccess;

    @BeforeEach
    void setUp() {
        // Create the service with all required dependencies
        eventService = new ProcurementEventServiceImpl(
            eventRepository,
            eventFileRepository,
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
        testProcurementItem.setFiscalYear(testFY);
        testProcurementItem.setActive(true);

        // Set up test event
        testEvent = new ProcurementEvent();
        testEvent.setId(1L);
        testEvent.setProcurementItem(testProcurementItem);
        testEvent.setEventType(ProcurementEvent.EventType.NOT_STARTED);
        testEvent.setEventDate(LocalDate.now());
        testEvent.setComment("Test comment");
        testEvent.setCreatedBy("testuser");
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setActive(true);

        // Set up test event file
        testEventFile = new ProcurementEventFile();
        testEventFile.setId(1L);
        testEventFile.setFileName("test-document.pdf");
        testEventFile.setContentType("application/pdf");
        testEventFile.setFileSize(1024L);
        testEventFile.setContent("test content".getBytes());
        testEventFile.setDescription("Test file description");
        testEventFile.setEvent(testEvent);
        testEventFile.setActive(true);

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
            assertEquals("NOT_STARTED", result.get(0).getEventType());
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
            dto.setEventType("NOT_STARTED");
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
            assertEquals("NOT_STARTED", result.getEventType());
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
            dto.setEventType("NOT_STARTED");
            
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
            assertEquals("NOT_STARTED", result.getEventType());
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

    // ==========================
    // Event File Tests
    // ==========================

    @Nested
    @DisplayName("uploadEventFile")
    class UploadEventFileTests {

        @Test
        @DisplayName("Should upload file successfully")
        void shouldUploadFileSuccessfully() {
            // Given
            MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-document.pdf", "application/pdf", "test content".getBytes());

            when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testEvent));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventFileRepository.save(any(ProcurementEventFile.class))).thenAnswer(invocation -> {
                ProcurementEventFile file = invocation.getArgument(0);
                file.setId(1L);
                return file;
            });

            // When
            ProcurementEventFileDTO result = eventService.uploadEventFile(1L, mockFile, "Test description", "testuser");

            // Then
            assertNotNull(result);
            assertEquals("test-document.pdf", result.getFileName());
            assertEquals("application/pdf", result.getContentType());
            assertEquals("Test description", result.getDescription());
            verify(eventFileRepository).save(any(ProcurementEventFile.class));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.uploadEventFile(1L, emptyFile, null, "testuser"));
        }

        @Test
        @DisplayName("Should throw exception for null file")
        void shouldThrowExceptionForNullFile() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.uploadEventFile(1L, null, null, "testuser"));
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFound() {
            // Given
            MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());
            when(eventRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.uploadEventFile(999L, mockFile, null, "testuser"));
        }
    }

    @Nested
    @DisplayName("getEventFiles")
    class GetEventFilesTests {

        @Test
        @DisplayName("Should return files for event")
        void shouldReturnFilesForEvent() {
            // Given
            when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testEvent));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventFileRepository.findByEventIdAndActiveTrue(1L)).thenReturn(List.of(testEventFile));

            // When
            List<ProcurementEventFileDTO> result = eventService.getEventFiles(1L, "testuser");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("test-document.pdf", result.get(0).getFileName());
        }

        @Test
        @DisplayName("Should return empty list when no files")
        void shouldReturnEmptyListWhenNoFiles() {
            // Given
            when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testEvent));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventFileRepository.findByEventIdAndActiveTrue(1L)).thenReturn(List.of());

            // When
            List<ProcurementEventFileDTO> result = eventService.getEventFiles(1L, "testuser");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFound() {
            // Given
            when(eventRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventFiles(999L, "testuser"));
        }
    }

    @Nested
    @DisplayName("getEventFile")
    class GetEventFileTests {

        @Test
        @DisplayName("Should return file with content")
        void shouldReturnFileWithContent() {
            // Given
            when(eventFileRepository.findById(1L)).thenReturn(Optional.of(testEventFile));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));

            // When
            ProcurementEventFile result = eventService.getEventFile(1L, "testuser");

            // Then
            assertNotNull(result);
            assertEquals("test-document.pdf", result.getFileName());
            assertNotNull(result.getContent());
        }

        @Test
        @DisplayName("Should throw exception when file not found")
        void shouldThrowExceptionWhenFileNotFound() {
            // Given
            when(eventFileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventFile(999L, "testuser"));
        }

        @Test
        @DisplayName("Should throw exception when file is inactive")
        void shouldThrowExceptionWhenFileInactive() {
            // Given
            testEventFile.setActive(false);
            when(eventFileRepository.findById(1L)).thenReturn(Optional.of(testEventFile));

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventFile(1L, "testuser"));
        }
    }

    @Nested
    @DisplayName("updateEventFileDescription")
    class UpdateEventFileDescriptionTests {

        @Test
        @DisplayName("Should update file description")
        void shouldUpdateFileDescription() {
            // Given
            when(eventFileRepository.findById(1L)).thenReturn(Optional.of(testEventFile));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventFileRepository.save(any(ProcurementEventFile.class))).thenReturn(testEventFile);

            // When
            ProcurementEventFileDTO result = eventService.updateEventFileDescription(1L, "New description", "testuser");

            // Then
            assertNotNull(result);
            verify(eventFileRepository).save(any(ProcurementEventFile.class));
        }

        @Test
        @DisplayName("Should throw exception when file not found")
        void shouldThrowExceptionWhenFileNotFound() {
            // Given
            when(eventFileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.updateEventFileDescription(999L, "desc", "testuser"));
        }
    }

    @Nested
    @DisplayName("deleteEventFile")
    class DeleteEventFileTests {

        @Test
        @DisplayName("Should soft delete file")
        void shouldSoftDeleteFile() {
            // Given
            when(eventFileRepository.findById(1L)).thenReturn(Optional.of(testEventFile));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(procurementItemRepository.findById(1L)).thenReturn(Optional.of(testProcurementItem));
            when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
            when(eventFileRepository.save(any(ProcurementEventFile.class))).thenReturn(testEventFile);

            // When
            eventService.deleteEventFile(1L, "testuser");

            // Then
            ArgumentCaptor<ProcurementEventFile> captor = ArgumentCaptor.forClass(ProcurementEventFile.class);
            verify(eventFileRepository).save(captor.capture());
            assertFalse(captor.getValue().getActive());
        }

        @Test
        @DisplayName("Should throw exception when file not found")
        void shouldThrowExceptionWhenFileNotFound() {
            // Given
            when(eventFileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.deleteEventFile(999L, "testuser"));
        }

        @Test
        @DisplayName("Should throw exception when file already deleted")
        void shouldThrowExceptionWhenFileAlreadyDeleted() {
            // Given
            testEventFile.setActive(false);
            when(eventFileRepository.findById(1L)).thenReturn(Optional.of(testEventFile));

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> eventService.deleteEventFile(1L, "testuser"));
        }
    }
}
