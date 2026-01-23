/*
 * myRC - Responsibility Centre Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponsibilityCentreServiceImplTest {

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ResponsibilityCentreServiceImpl service;

  private User testUser;
  private ResponsibilityCentre testRC;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setDescription("Test description");
    testRC.setOwner(testUser);
  }

  @Test
  void testCreateResponsibilityCentre() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.existsByNameAndOwner("Test RC", testUser)).thenReturn(false);
    when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

    ResponsibilityCentreDTO result = service.createResponsibilityCentre("testuser", "Test RC",
        "Test description");

    assertNotNull(result);
    assertEquals("Test RC", result.getName());
    assertEquals("testuser", result.getOwnerUsername());
    assertTrue(result.isOwner());
  }

  @Test
  void testCreateResponsibilityCentreUserNotFound() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.createResponsibilityCentre("nonexistent", "RC", "desc"));
  }

  @Test
  void testCreateResponsibilityCentreDuplicateName() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.existsByNameAndOwner("Test RC", testUser)).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> service.createResponsibilityCentre("testuser", "Test RC", "desc"));
  }

  @Test
  void testGetUserResponsibilityCentres() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findByOwner(testUser)).thenReturn(List.of(testRC));
    when(accessRepository.findByUser(testUser)).thenReturn(new ArrayList<>());

    List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("testuser");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test RC", result.get(0).getName());
  }

  @Test
  void testGetResponsibilityCentre() {
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(1L, "testuser");

    assertTrue(result.isPresent());
    assertEquals("Test RC", result.get().getName());
  }

  @Test
  void testUpdateResponsibilityCentre() {
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

    Optional<ResponsibilityCentreDTO> result = service.updateResponsibilityCentre(1L, "testuser",
        "Updated RC", "Updated description");

    assertTrue(result.isPresent());
  }

  @Test
  void testDeleteResponsibilityCentre() {
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    boolean result = service.deleteResponsibilityCentre(1L, "testuser");

    assertTrue(result);
    verify(rcRepository).deleteById(1L);
  }

  @Test
  void testGrantAccess() {
    User grantedToUser = new User();
    grantedToUser.setId(2L);
    grantedToUser.setUsername("grantedto");

    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("grantedto")).thenReturn(Optional.of(grantedToUser));
    when(accessRepository.save(any(RCAccess.class))).thenReturn(new RCAccess(testRC, grantedToUser,
        RCAccess.AccessLevel.READ_ONLY));

    Optional<RCAccess> result = service.grantAccess(1L, "testuser", "grantedto", "READ_ONLY");

    assertTrue(result.isPresent());
    assertEquals(RCAccess.AccessLevel.READ_ONLY, result.get().getAccessLevel());
  }

  @Test
  void testRevokeAccess() {
    User grantedToUser = new User();
    grantedToUser.setId(2L);
    grantedToUser.setUsername("grantedto");

    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("grantedto")).thenReturn(Optional.of(grantedToUser));

    boolean result = service.revokeAccess(1L, "testuser", "grantedto");

    assertTrue(result);
    verify(accessRepository).deleteByResponsibilityCentreAndUser(testRC, grantedToUser);
  }

  @Test
  void testCloneResponsibilityCentre() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(rcRepository.existsByNameAndOwner("Cloned RC", testUser)).thenReturn(false);
    
    ResponsibilityCentre clonedRC = new ResponsibilityCentre();
    clonedRC.setId(2L);
    clonedRC.setName("Cloned RC");
    clonedRC.setDescription("Test description");
    clonedRC.setOwner(testUser);
    
    when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(clonedRC);

    ResponsibilityCentreDTO result = service.cloneResponsibilityCentre(1L, "testuser", "Cloned RC");

    assertNotNull(result);
    assertEquals("Cloned RC", result.getName());
    assertEquals("testuser", result.getOwnerUsername());
    assertTrue(result.isOwner());
    verify(rcRepository).save(any(ResponsibilityCentre.class));
  }

  @Test
  void testCloneResponsibilityCentreUserNotFound() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.cloneResponsibilityCentre(1L, "nonexistent", "Cloned RC"));
  }

  @Test
  void testCloneResponsibilityCentreSourceNotFound() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.cloneResponsibilityCentre(999L, "testuser", "Cloned RC"));
  }

  @Test
  void testCloneResponsibilityCentreDuplicateName() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(rcRepository.existsByNameAndOwner("Cloned RC", testUser)).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> service.cloneResponsibilityCentre(1L, "testuser", "Cloned RC"));
  }

  @Test
  void testCloneResponsibilityCentreWithAccessUser() {
    User accessUser = new User();
    accessUser.setId(2L);
    accessUser.setUsername("accessuser");

    RCAccess access = new RCAccess(testRC, accessUser, RCAccess.AccessLevel.READ_ONLY);

    when(userRepository.findByUsername("accessuser")).thenReturn(Optional.of(accessUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, accessUser)).thenReturn(Optional.of(access));
    when(rcRepository.existsByNameAndOwner("Cloned RC", accessUser)).thenReturn(false);

    ResponsibilityCentre clonedRC = new ResponsibilityCentre();
    clonedRC.setId(3L);
    clonedRC.setName("Cloned RC");
    clonedRC.setDescription("Test description");
    clonedRC.setOwner(accessUser);

    when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(clonedRC);

    ResponsibilityCentreDTO result = service.cloneResponsibilityCentre(1L, "accessuser", "Cloned RC");

    assertNotNull(result);
    assertEquals("Cloned RC", result.getName());
  }

  @Test
  void testCloneResponsibilityCentreNoAccess() {
    User noAccessUser = new User();
    noAccessUser.setId(3L);
    noAccessUser.setUsername("noaccessuser");

    when(userRepository.findByUsername("noaccessuser")).thenReturn(Optional.of(noAccessUser));
    when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
    when(accessRepository.findByResponsibilityCentreAndUser(testRC, noAccessUser)).thenReturn(Optional.empty());

    assertThrows(IllegalAccessError.class,
        () -> service.cloneResponsibilityCentre(1L, "noaccessuser", "Cloned RC"));
  }
}
