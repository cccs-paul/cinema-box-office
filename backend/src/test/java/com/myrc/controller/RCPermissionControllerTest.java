/*
 * myRC - RC Permission Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.RCAccessDTO;
import com.myrc.model.RCAccess.AccessLevel;
import com.myrc.model.RCAccess.PrincipalType;
import com.myrc.service.RCPermissionService;
import java.time.LocalDateTime;
import java.util.Arrays;
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
 * Unit tests for RCPermissionController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class RCPermissionControllerTest {

  @Mock
  private RCPermissionService permissionService;

  private Authentication authentication;
  private RCPermissionController controller;
  private RCAccessDTO testAccess;

  @BeforeEach
  void setUp() {
    controller = new RCPermissionController(permissionService);
    authentication = createAuthentication("testuser");

    // Set up test access DTO using proper setters
    testAccess = new RCAccessDTO();
    testAccess.setId(1L);
    testAccess.setRcId(1L);
    testAccess.setRcName("Test RC");
    testAccess.setPrincipalIdentifier("testuser");
    testAccess.setPrincipalDisplayName("Test User");
    testAccess.setPrincipalType("USER");
    testAccess.setAccessLevel("READ_WRITE");
    testAccess.setGrantedAt(LocalDateTime.now());
    testAccess.setGrantedBy("owner");
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
  @DisplayName("GET /rc/{rcId} - Get Permissions Tests")
  class GetPermissionsTests {

    @Test
    @DisplayName("Should return permissions successfully")
    void shouldReturnPermissionsSuccessfully() {
      when(permissionService.getPermissionsForRC(1L, "testuser"))
          .thenReturn(Arrays.asList(testAccess));

      ResponseEntity<?> response = controller.getPermissionsForRC(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      @SuppressWarnings("unchecked")
      List<RCAccessDTO> permissions = (List<RCAccessDTO>) response.getBody();
      assertEquals(1, permissions.size());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuth() {
      ResponseEntity<?> response = controller.getPermissionsForRC(1L, null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when not owner")
    void shouldReturnForbiddenWhenNotOwner() {
      when(permissionService.getPermissionsForRC(1L, "testuser"))
          .thenThrow(new SecurityException("Not an owner"));

      ResponseEntity<?> response = controller.getPermissionsForRC(1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when RC doesn't exist")
    void shouldReturnNotFoundWhenRCNotFound() {
      when(permissionService.getPermissionsForRC(999L, "testuser"))
          .thenThrow(new IllegalArgumentException("RC not found"));

      ResponseEntity<?> response = controller.getPermissionsForRC(999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /rc/{rcId}/user - Grant User Access Tests")
  class GrantUserAccessTests {

    @Test
    @DisplayName("Should grant user access successfully")
    void shouldGrantUserAccessSuccessfully() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("newuser");
      request.setAccessLevel("READ_WRITE");

      when(permissionService.grantUserAccess(1L, "newuser", AccessLevel.READ_WRITE, "testuser"))
          .thenReturn(testAccess);

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, authentication);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuth() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("newuser");
      request.setAccessLevel("READ_WRITE");

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when not owner")
    void shouldReturnForbiddenWhenNotOwner() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("newuser");
      request.setAccessLevel("READ_WRITE");

      when(permissionService.grantUserAccess(1L, "newuser", AccessLevel.READ_WRITE, "testuser"))
          .thenThrow(new SecurityException("Not an owner"));

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request for invalid access level")
    void shouldReturnBadRequestForInvalidAccessLevel() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("newuser");
      request.setAccessLevel("INVALID");

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request when user already has same access level")
    void shouldReturnBadRequestWhenUserAlreadyHasSameAccessLevel() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("existinguser");
      request.setAccessLevel("READ_WRITE");

      when(permissionService.grantUserAccess(1L, "existinguser", AccessLevel.READ_WRITE, "testuser"))
          .thenThrow(new IllegalArgumentException("User 'existinguser' already has READ_WRITE access to this RC."));

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("already has READ_WRITE access"));
    }

    @Test
    @DisplayName("Should return bad request when user already has different access level")
    void shouldReturnBadRequestWhenUserAlreadyHasDifferentAccessLevel() {
      RCPermissionController.GrantUserAccessRequest request = new RCPermissionController.GrantUserAccessRequest();
      request.setUsername("existinguser");
      request.setAccessLevel("READ_ONLY");

      when(permissionService.grantUserAccess(1L, "existinguser", AccessLevel.READ_ONLY, "testuser"))
          .thenThrow(new IllegalArgumentException("User 'existinguser' already has READ_WRITE access to this RC. Use update to change the access level."));

      ResponseEntity<?> response = controller.grantUserAccess(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("already has READ_WRITE access"));
      assertTrue(response.getBody().toString().contains("Use update to change the access level"));
    }
  }

  @Nested
  @DisplayName("POST /rc/{rcId}/group - Grant Group Access Tests")
  class GrantGroupAccessTests {

    @Test
    @DisplayName("Should grant group access successfully")
    void shouldGrantGroupAccessSuccessfully() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("test-group");
      request.setPrincipalDisplayName("Test Group");
      request.setPrincipalType("GROUP");
      request.setAccessLevel("READ_WRITE");

      RCAccessDTO groupAccess = new RCAccessDTO();
      groupAccess.setId(2L);
      groupAccess.setRcId(1L);
      groupAccess.setPrincipalIdentifier("test-group");
      groupAccess.setPrincipalDisplayName("Test Group");
      groupAccess.setPrincipalType("GROUP");
      groupAccess.setAccessLevel("READ_WRITE");

      when(permissionService.grantGroupAccess(1L, "test-group", "Test Group", PrincipalType.GROUP, AccessLevel.READ_WRITE, "testuser"))
          .thenReturn(groupAccess);

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, authentication);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should grant distribution list access")
    void shouldGrantDistributionListAccess() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("test-dl@example.com");
      request.setPrincipalDisplayName("Test DL");
      request.setPrincipalType("DISTRIBUTION_LIST");
      request.setAccessLevel("READ_ONLY");

      RCAccessDTO dlAccess = new RCAccessDTO();
      dlAccess.setId(3L);
      dlAccess.setRcId(1L);
      dlAccess.setPrincipalIdentifier("test-dl@example.com");
      dlAccess.setPrincipalDisplayName("Test DL");
      dlAccess.setPrincipalType("DISTRIBUTION_LIST");
      dlAccess.setAccessLevel("READ_ONLY");

      when(permissionService.grantGroupAccess(1L, "test-dl@example.com", "Test DL", PrincipalType.DISTRIBUTION_LIST, AccessLevel.READ_ONLY, "testuser"))
          .thenReturn(dlAccess);

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, authentication);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuth() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("test-group");
      request.setPrincipalDisplayName("Test Group");
      request.setPrincipalType("GROUP");
      request.setAccessLevel("READ_WRITE");

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when not owner")
    void shouldReturnForbiddenWhenNotOwner() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("test-group");
      request.setPrincipalDisplayName("Test Group");
      request.setPrincipalType("GROUP");
      request.setAccessLevel("READ_WRITE");

      when(permissionService.grantGroupAccess(1L, "test-group", "Test Group", PrincipalType.GROUP, AccessLevel.READ_WRITE, "testuser"))
          .thenThrow(new SecurityException("Not an owner"));

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request when group already has same access level")
    void shouldReturnBadRequestWhenGroupAlreadyHasSameAccessLevel() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("existing-group");
      request.setPrincipalDisplayName("Existing Group");
      request.setPrincipalType("GROUP");
      request.setAccessLevel("READ_WRITE");

      when(permissionService.grantGroupAccess(1L, "existing-group", "Existing Group", PrincipalType.GROUP, AccessLevel.READ_WRITE, "testuser"))
          .thenThrow(new IllegalArgumentException("Group 'existing-group' already has READ_WRITE access to this RC."));

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("Group"));
      assertTrue(response.getBody().toString().contains("already has READ_WRITE access"));
    }

    @Test
    @DisplayName("Should return bad request when group already has different access level")
    void shouldReturnBadRequestWhenGroupAlreadyHasDifferentAccessLevel() {
      RCPermissionController.GrantGroupAccessRequest request = new RCPermissionController.GrantGroupAccessRequest();
      request.setPrincipalIdentifier("existing-group");
      request.setPrincipalDisplayName("Existing Group");
      request.setPrincipalType("GROUP");
      request.setAccessLevel("READ_ONLY");

      when(permissionService.grantGroupAccess(1L, "existing-group", "Existing Group", PrincipalType.GROUP, AccessLevel.READ_ONLY, "testuser"))
          .thenThrow(new IllegalArgumentException("Group 'existing-group' already has READ_WRITE access to this RC. Use update to change the access level."));

      ResponseEntity<?> response = controller.grantGroupAccess(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("Group"));
      assertTrue(response.getBody().toString().contains("already has READ_WRITE access"));
      assertTrue(response.getBody().toString().contains("Use update to change the access level"));
    }
  }

  @Nested
  @DisplayName("PUT /{accessId} - Update Permission Tests")
  class UpdatePermissionTests {

    @Test
    @DisplayName("Should update permission successfully")
    void shouldUpdatePermissionSuccessfully() {
      RCPermissionController.UpdatePermissionRequest request = new RCPermissionController.UpdatePermissionRequest();
      request.setAccessLevel("READ_ONLY");

      when(permissionService.updatePermission(1L, AccessLevel.READ_ONLY, "testuser"))
          .thenReturn(testAccess);

      ResponseEntity<?> response = controller.updatePermission(1L, request, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuth() {
      RCPermissionController.UpdatePermissionRequest request = new RCPermissionController.UpdatePermissionRequest();
      request.setAccessLevel("READ_ONLY");

      ResponseEntity<?> response = controller.updatePermission(1L, request, null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when not owner")
    void shouldReturnForbiddenWhenNotOwner() {
      RCPermissionController.UpdatePermissionRequest request = new RCPermissionController.UpdatePermissionRequest();
      request.setAccessLevel("READ_ONLY");

      when(permissionService.updatePermission(1L, AccessLevel.READ_ONLY, "testuser"))
          .thenThrow(new SecurityException("Not an owner"));

      ResponseEntity<?> response = controller.updatePermission(1L, request, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request for invalid access level")
    void shouldReturnBadRequestForInvalidAccessLevel() {
      RCPermissionController.UpdatePermissionRequest request = new RCPermissionController.UpdatePermissionRequest();
      request.setAccessLevel("INVALID");

      ResponseEntity<?> response = controller.updatePermission(1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /{accessId} - Revoke Access Tests")
  class RevokeAccessTests {

    @Test
    @DisplayName("Should revoke access successfully")
    void shouldRevokeAccessSuccessfully() {
      doNothing().when(permissionService).revokeAccess(1L, "testuser");

      ResponseEntity<?> response = controller.revokeAccess(1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
      verify(permissionService).revokeAccess(1L, "testuser");
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuth() {
      ResponseEntity<?> response = controller.revokeAccess(1L, null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when not owner")
    void shouldReturnForbiddenWhenNotOwner() {
      doThrow(new SecurityException("Not an owner")).when(permissionService).revokeAccess(1L, "testuser");

      ResponseEntity<?> response = controller.revokeAccess(1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /rc/{rcId}/is-owner - Is Owner Tests")
  class IsOwnerTests {

    @Test
    @DisplayName("Should return true when user is owner")
    void shouldReturnTrueWhenOwner() {
      when(permissionService.isOwner(1L, "testuser")).thenReturn(true);

      ResponseEntity<Boolean> response = controller.isOwner(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody());
    }

    @Test
    @DisplayName("Should return false when user is not owner")
    void shouldReturnFalseWhenNotOwner() {
      when(permissionService.isOwner(1L, "testuser")).thenReturn(false);

      ResponseEntity<Boolean> response = controller.isOwner(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertFalse(response.getBody());
    }

    @Test
    @DisplayName("Should return false when no authentication")
    void shouldReturnFalseWhenNoAuth() {
      ResponseEntity<Boolean> response = controller.isOwner(1L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertFalse(response.getBody());
    }
  }

  @Nested
  @DisplayName("GET /rc/{rcId}/can-edit - Can Edit Tests")
  class CanEditTests {

    @Test
    @DisplayName("Should return true when user can edit")
    void shouldReturnTrueWhenCanEdit() {
      when(permissionService.canEditContent(eq(1L), eq("testuser"), any())).thenReturn(true);

      ResponseEntity<Boolean> response = controller.canEdit(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody());
    }

    @Test
    @DisplayName("Should return false when user cannot edit")
    void shouldReturnFalseWhenCannotEdit() {
      when(permissionService.canEditContent(eq(1L), eq("testuser"), any())).thenReturn(false);

      ResponseEntity<Boolean> response = controller.canEdit(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertFalse(response.getBody());
    }

    @Test
    @DisplayName("Should return false when no authentication")
    void shouldReturnFalseWhenNoAuth() {
      ResponseEntity<Boolean> response = controller.canEdit(1L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertFalse(response.getBody());
    }
  }
}
