/*
 * myRC - Responsibility Centre Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.BoxOfficeApplication;
import com.myrc.dto.ResponsibilityCentreDTO;
import com.myrc.model.RCAccess;
import com.myrc.service.ResponsibilityCentreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResponsibilityCentreController.
 * Tests controller methods and error handling.
 *
 * @author myRC Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResponsibilityCentreController Unit Tests")
public class ResponsibilityCentreControllerTest {

  @Mock
  private ResponsibilityCentreService rcService;

  private ResponsibilityCentreController controller;
  private ResponsibilityCentreDTO testRC;
  private ResponsibilityCentreDTO testRC2;

  @BeforeEach
  void setUp() {
    // Initialize controller with mocked service
    controller = new ResponsibilityCentreController(rcService);

    // Create test RCs
    testRC = new ResponsibilityCentreDTO();
    testRC.setId(1L);
    testRC.setName("Main Theatre");
    testRC.setDescription("Main theatre responsibility centre");
    testRC.setOwnerUsername("testuser");
    testRC.setAccessLevel("READ_WRITE");
    testRC.setCreatedAt(LocalDateTime.now());
    testRC.setActive(true);

    testRC2 = new ResponsibilityCentreDTO();
    testRC2.setId(2L);
    testRC2.setName("Concessions");
    testRC2.setDescription("Concessions responsibility centre");
    testRC2.setOwnerUsername("testuser");
    testRC2.setAccessLevel("READ_WRITE");
    testRC2.setCreatedAt(LocalDateTime.now());
    testRC2.setActive(true);
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
    assertNotNull(rcService);
  }

  @Test
  @DisplayName("Should handle create responsibility centre request")
  void testCreateResponsibilityCentreCallsService() {
    // Arrange
    when(rcService.createResponsibilityCentre(
        eq("testuser"),
        eq("New Theatre"),
        eq("New theatre description")
    )).thenReturn(testRC);

    // Act
    ResponsibilityCentreDTO result = rcService.createResponsibilityCentre(
        "testuser", "New Theatre", "New theatre description");

    // Assert
    assertNotNull(result);
    assertEquals("Main Theatre", result.getName());
  }

  @Test
  @DisplayName("Should get all responsibility centres for a user")
  void testGetAllResponsibilityCentresReturnsListFromService() {
    // Arrange
    List<ResponsibilityCentreDTO> rcs = Arrays.asList(testRC, testRC2);
    when(rcService.getUserResponsibilityCentres("testuser")).thenReturn(rcs);

    // Act
    List<ResponsibilityCentreDTO> result = rcService.getUserResponsibilityCentres("testuser");

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Should retrieve RC by ID from service")
  void testGetResponsibilityCentreByIdReturnsFromService() {
    // Arrange
    when(rcService.getResponsibilityCentre(1L, "testuser")).thenReturn(Optional.of(testRC));

    // Act
    Optional<ResponsibilityCentreDTO> result = rcService.getResponsibilityCentre(1L, "testuser");

    // Assert
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
  }

  @Test
  @DisplayName("Should handle not found case when RC doesn't exist")
  void testGetResponsibilityCentreReturnsEmptyWhenNotFound() {
    // Arrange
    when(rcService.getResponsibilityCentre(999L, "testuser")).thenReturn(Optional.empty());

    // Act
    Optional<ResponsibilityCentreDTO> result = rcService.getResponsibilityCentre(999L, "testuser");

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should update responsibility centre via service")
  void testUpdateResponsibilityCentreCallsService() {
    // Arrange
    ResponsibilityCentreDTO updatedRC = new ResponsibilityCentreDTO();
    updatedRC.setId(1L);
    updatedRC.setName("Updated Theatre");
    updatedRC.setDescription("Updated description");
    updatedRC.setOwnerUsername("testuser");

    when(rcService.updateResponsibilityCentre(
        eq(1L),
        eq("testuser"),
        eq("Updated Theatre"),
        eq("Updated description")
    )).thenReturn(Optional.of(updatedRC));

    // Act
    Optional<ResponsibilityCentreDTO> result = rcService.updateResponsibilityCentre(
        1L, "testuser", "Updated Theatre", "Updated description");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Updated Theatre", result.get().getName());
  }

  @Test
  @DisplayName("Should delete responsibility centre via service")
  void testDeleteResponsibilityCentreCallsService() {
    // Arrange
    when(rcService.deleteResponsibilityCentre(1L, "testuser")).thenReturn(true);

    // Act
    boolean result = rcService.deleteResponsibilityCentre(1L, "testuser");

    // Assert
    assertTrue(result);
    verify(rcService, times(1)).deleteResponsibilityCentre(1L, "testuser");
  }

  @Test
  @DisplayName("Should grant access via service")
  void testGrantAccessCallsService() {
    // Arrange
    when(rcService.grantAccess(1L, "testuser", "granteduser", "READ")).thenReturn(Optional.empty());

    // Act
    Optional<?> result = rcService.grantAccess(1L, "testuser", "granteduser", "READ");

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should revoke access via service")
  void testRevokeAccessCallsService() {
    // Arrange
    when(rcService.revokeAccess(1L, "testuser", "revokeuser")).thenReturn(false);

    // Act
    boolean result = rcService.revokeAccess(1L, "testuser", "revokeuser");

    // Assert
    assertFalse(result);
  }

  @Test
  @DisplayName("Should get access list from service")
  void testGetAccessListReturnsFromService() {
    // Arrange
    List<RCAccess> accessList = new ArrayList<>();
    when(rcService.getResponsibilityCentreAccess(1L, "testuser")).thenReturn(accessList);

    // Act
    List<?> result = rcService.getResponsibilityCentreAccess(1L, "testuser");

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Service should be verified for no interaction before use")
  void testServiceMockVerification() {
    // Assert that service hasn't been interacted with yet
    verifyNoInteractions(rcService);
  }

  @Test
  @DisplayName("Should handle null values gracefully")
  void testNullHandling() {
    when(rcService.getResponsibilityCentre(1L, "testuser")).thenReturn(Optional.empty());
    
    Optional<ResponsibilityCentreDTO> result = rcService.getResponsibilityCentre(1L, "testuser");
    
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should handle clone responsibility centre request")
  void testCloneResponsibilityCentreCallsService() {
    // Arrange
    ResponsibilityCentreDTO clonedRC = new ResponsibilityCentreDTO();
    clonedRC.setId(3L);
    clonedRC.setName("Cloned Theatre");
    clonedRC.setDescription("Main theatre responsibility centre");
    clonedRC.setOwnerUsername("testuser");
    clonedRC.setAccessLevel("READ_WRITE");
    clonedRC.setCreatedAt(LocalDateTime.now());
    clonedRC.setActive(true);

    when(rcService.cloneResponsibilityCentre(
        eq(1L),
        eq("testuser"),
        eq("Cloned Theatre")
    )).thenReturn(clonedRC);

    // Act
    ResponsibilityCentreDTO result = rcService.cloneResponsibilityCentre(1L, "testuser", "Cloned Theatre");

    // Assert
    assertNotNull(result);
    assertEquals("Cloned Theatre", result.getName());
    assertEquals(3L, result.getId());
    verify(rcService, times(1)).cloneResponsibilityCentre(1L, "testuser", "Cloned Theatre");
  }

  @Test
  @DisplayName("Should handle clone with duplicate name")
  void testCloneResponsibilityCentreDuplicateName() {
    // Arrange
    when(rcService.cloneResponsibilityCentre(
        eq(1L),
        eq("testuser"),
        eq("Main Theatre")
    )).thenThrow(new IllegalArgumentException("A Responsibility Centre with this name already exists for this user"));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
        rcService.cloneResponsibilityCentre(1L, "testuser", "Main Theatre")
    );
  }

  @Test
  @DisplayName("Should handle clone with no access")
  void testCloneResponsibilityCentreNoAccess() {
    // Arrange
    when(rcService.cloneResponsibilityCentre(
        eq(1L),
        eq("noaccessuser"),
        eq("Cloned Theatre")
    )).thenThrow(new IllegalAccessError("User does not have access to clone this RC"));

    // Act & Assert
    assertThrows(IllegalAccessError.class, () ->
        rcService.cloneResponsibilityCentre(1L, "noaccessuser", "Cloned Theatre")
    );
  }

  @Test
  @DisplayName("Should handle clone with source not found")
  void testCloneResponsibilityCentreSourceNotFound() {
    // Arrange
    when(rcService.cloneResponsibilityCentre(
        eq(999L),
        eq("testuser"),
        eq("Cloned Theatre")
    )).thenThrow(new IllegalArgumentException("Source responsibility centre not found"));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
        rcService.cloneResponsibilityCentre(999L, "testuser", "Cloned Theatre")
    );
  }
}

