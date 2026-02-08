/*
 * myRC - RC Permission Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.RCAccessDTO;
import com.myrc.model.RCAccess;
import com.myrc.model.RCAccess.AccessLevel;
import com.myrc.model.RCAccess.PrincipalType;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RCPermissionServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class RCPermissionServiceTest {

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DirectorySearchService directorySearchService;

  private RCPermissionServiceImpl permissionService;
  private User testUser;
  private User ownerUser;
  private ResponsibilityCentre testRC;
  private RCAccess testAccess;

  @BeforeEach
  void setUp() {
    permissionService = new RCPermissionServiceImpl(accessRepository, rcRepository, userRepository, directorySearchService);

    ownerUser = new User();
    ownerUser.setId(1L);
    ownerUser.setUsername("owner");
    ownerUser.setEmail("owner@example.com");
    ownerUser.setFullName("Owner User");

    testUser = new User();
    testUser.setId(2L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFullName("Test User");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(ownerUser);

    testAccess = new RCAccess();
    testAccess.setId(1L);
    testAccess.setResponsibilityCentre(testRC);
    testAccess.setUser(testUser);
    testAccess.setPrincipalIdentifier("testuser");
    testAccess.setPrincipalType(PrincipalType.USER);
    testAccess.setAccessLevel(AccessLevel.READ_WRITE);
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(permissionService);
  }

  @Nested
  @DisplayName("getPermissionsForRC Tests")
  class GetPermissionsTests {

    @Test
    @DisplayName("Should return permissions when user is owner")
    void shouldReturnPermissionsWhenOwner() {
      // isOwner() first calls rcRepository.findById
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      // isOwner() then calls userRepository.findByUsername
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      // Owner is RC owner, so isOwner returns true without checking access table
      // Then getPermissionsForRC calls findByResponsibilityCentre
      when(accessRepository.findByResponsibilityCentre(testRC)).thenReturn(Arrays.asList(testAccess));

      List<RCAccessDTO> result = permissionService.getPermissionsForRC(1L, "owner");

      assertNotNull(result);
      // Should return 2: the implicit owner + the testAccess
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should throw exception when RC not found")
    void shouldThrowExceptionWhenRCNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          permissionService.getPermissionsForRC(999L, "owner"));
    }

    @Test
    @DisplayName("Should throw SecurityException when user has no ownership")
    void shouldThrowSecurityExceptionWhenUserNotOwner() {
      User randomUser = new User();
      randomUser.setId(99L);
      randomUser.setUsername("random");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("random")).thenReturn(Optional.of(randomUser));

      assertThrows(SecurityException.class, () ->
          permissionService.getPermissionsForRC(1L, "random"));
    }
  }

  @Nested
  @DisplayName("grantUserAccess Tests")
  class GrantUserAccessTests {

    @Test
    @DisplayName("Should grant user access when requester is owner")
    void shouldGrantUserAccessWhenOwner() {
      User newUser = new User();
      newUser.setId(3L);
      newUser.setUsername("newuser");
      newUser.setFullName("New User");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(newUser));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, newUser))
          .thenReturn(Optional.empty()); // No existing access
      when(accessRepository.save(any(RCAccess.class))).thenAnswer(invocation -> {
        RCAccess saved = invocation.getArgument(0);
        saved.setId(2L);
        return saved;
      });

      RCAccessDTO result = permissionService.grantUserAccess(1L, "newuser", AccessLevel.READ_WRITE, "owner");

      assertNotNull(result);
      verify(accessRepository).save(any(RCAccess.class));
    }

    @Test
    @DisplayName("Should throw SecurityException when requester is not owner")
    void shouldThrowSecurityExceptionWhenRequesterNotOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      assertThrows(SecurityException.class, () ->
          permissionService.grantUserAccess(1L, "someuser", AccessLevel.READ_WRITE, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when RC not found")
    void shouldThrowExceptionWhenRCNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantUserAccess(999L, "testuser", AccessLevel.READ_WRITE, "owner"));
    }

    @Test
    @DisplayName("Should throw exception when target user not found in DB or directory")
    void shouldThrowExceptionWhenTargetUserNotFound() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
      // Directory search also returns no exact match
      when(directorySearchService.searchUsers("nonexistent", 50)).thenReturn(Collections.emptyList());

      assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantUserAccess(1L, "nonexistent", AccessLevel.READ_WRITE, "owner"));
    }

    @Test
    @DisplayName("Should grant access to LDAP user without creating local User entity")
    void shouldGrantAccessToLdapUserWithoutLocalUser() {
      DirectorySearchService.SearchResult ldapResult =
          new DirectorySearchService.SearchResult("amy", "Amy Wong", "LDAP", "amy@planetexpress.com");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("amy")).thenReturn(Optional.empty());
      when(directorySearchService.searchUsers("amy", 50)).thenReturn(List.of(ldapResult));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("amy"), eq(PrincipalType.USER)))
          .thenReturn(Optional.empty());
      when(accessRepository.save(any(RCAccess.class))).thenAnswer(invocation -> {
        RCAccess saved = invocation.getArgument(0);
        saved.setId(5L);
        return saved;
      });

      RCAccessDTO result = permissionService.grantUserAccess(1L, "amy", AccessLevel.READ_WRITE, "owner");

      assertNotNull(result);
      // Must NOT create a local User entity
      verify(userRepository, never()).save(any(User.class));
      // Must save the access record
      verify(accessRepository).save(argThat(access ->
          access.getUser() == null &&
          "amy".equals(access.getPrincipalIdentifier()) &&
          PrincipalType.USER == access.getPrincipalType() &&
          "Amy Wong".equals(access.getPrincipalDisplayName())
      ));
    }

    @Test
    @DisplayName("Should grant access to LDAP user using identifier as display name when directory has no display name")
    void shouldGrantAccessToLdapUserWithFallbackDisplayName() {
      DirectorySearchService.SearchResult noDisplayResult =
          new DirectorySearchService.SearchResult("bender", null, "LDAP", null);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("bender")).thenReturn(Optional.empty());
      when(directorySearchService.searchUsers("bender", 50)).thenReturn(List.of(noDisplayResult));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("bender"), eq(PrincipalType.USER)))
          .thenReturn(Optional.empty());
      when(accessRepository.save(any(RCAccess.class))).thenAnswer(invocation -> {
        RCAccess saved = invocation.getArgument(0);
        saved.setId(6L);
        return saved;
      });

      RCAccessDTO result = permissionService.grantUserAccess(1L, "bender", AccessLevel.READ_ONLY, "owner");

      assertNotNull(result);
      // Must NOT create a local User entity
      verify(userRepository, never()).save(any(User.class));
      // Must use identifier as fallback display name
      verify(accessRepository).save(argThat(access ->
          "bender".equals(access.getPrincipalIdentifier()) &&
          "bender".equals(access.getPrincipalDisplayName())
      ));
    }

    @Test
    @DisplayName("Should throw exception when LDAP user already has access by principalIdentifier")
    void shouldThrowExceptionWhenLdapUserAlreadyHasAccess() {
      DirectorySearchService.SearchResult ldapResult =
          new DirectorySearchService.SearchResult("amy", "Amy Wong", "LDAP", "amy@planetexpress.com");

      RCAccess existingAccess = new RCAccess();
      existingAccess.setId(10L);
      existingAccess.setResponsibilityCentre(testRC);
      existingAccess.setPrincipalIdentifier("amy");
      existingAccess.setPrincipalType(PrincipalType.USER);
      existingAccess.setAccessLevel(AccessLevel.READ_WRITE);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("amy")).thenReturn(Optional.empty());
      when(directorySearchService.searchUsers("amy", 50)).thenReturn(List.of(ldapResult));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("amy"), eq(PrincipalType.USER)))
          .thenReturn(Optional.of(existingAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantUserAccess(1L, "amy", AccessLevel.READ_ONLY, "owner"));
      assertTrue(exception.getMessage().contains("already has READ_WRITE access"));
      assertTrue(exception.getMessage().contains("Use update to change the access level"));
    }

    @Test
    @DisplayName("Should throw exception when user already has access with different level")
    void shouldThrowExceptionWhenUserAlreadyHasAccess() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(testAccess)); // Already has READ_WRITE access

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantUserAccess(1L, "testuser", AccessLevel.READ_ONLY, "owner"));
      
      assertTrue(exception.getMessage().contains("already has READ_WRITE access"));
      assertTrue(exception.getMessage().contains("Use update to change the access level"));
    }

    @Test
    @DisplayName("Should throw exception with specific message when user already has same access level")
    void shouldThrowExceptionWhenUserAlreadyHasSameAccessLevel() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(testAccess)); // Already has READ_WRITE access

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantUserAccess(1L, "testuser", AccessLevel.READ_WRITE, "owner"));
      
      assertTrue(exception.getMessage().contains("already has READ_WRITE access"));
      assertFalse(exception.getMessage().contains("Use update")); // Should NOT suggest update for same level
    }
  }

  @Nested
  @DisplayName("grantGroupAccess Tests")
  class GrantGroupAccessTests {

    @Test
    @DisplayName("Should grant group access when requester is owner")
    void shouldGrantGroupAccessWhenOwner() {
      // isOwner() checks - owner is RC owner so no access table check needed
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      // grantGroupAccess checks if group already has access
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("CN=TestGroup"), eq(PrincipalType.GROUP)))
          .thenReturn(Optional.empty());
      when(accessRepository.save(any(RCAccess.class))).thenAnswer(invocation -> {
        RCAccess saved = invocation.getArgument(0);
        saved.setId(3L);
        return saved;
      });

      RCAccessDTO result = permissionService.grantGroupAccess(1L, "CN=TestGroup", "Test Group",
          PrincipalType.GROUP, AccessLevel.READ_ONLY, "owner");

      assertNotNull(result);
      verify(accessRepository).save(any(RCAccess.class));
    }

    @Test
    @DisplayName("Should throw SecurityException when requester is not owner")
    void shouldThrowSecurityExceptionWhenRequesterNotOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      assertThrows(SecurityException.class, () ->
          permissionService.grantGroupAccess(1L, "CN=TestGroup", "Test Group",
              PrincipalType.GROUP, AccessLevel.READ_ONLY, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when using USER principal type")
    void shouldThrowExceptionWhenUsingUserPrincipalType() {
      assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantGroupAccess(1L, "testuser", "Test User",
              PrincipalType.USER, AccessLevel.READ_ONLY, "owner"));
    }

    @Test
    @DisplayName("Should throw exception when group already has access with different level")
    void shouldThrowExceptionWhenGroupAlreadyHasAccess() {
      RCAccess existingGroupAccess = new RCAccess();
      existingGroupAccess.setId(10L);
      existingGroupAccess.setResponsibilityCentre(testRC);
      existingGroupAccess.setPrincipalIdentifier("CN=TestGroup");
      existingGroupAccess.setPrincipalType(PrincipalType.GROUP);
      existingGroupAccess.setAccessLevel(AccessLevel.READ_WRITE);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("CN=TestGroup"), eq(PrincipalType.GROUP)))
          .thenReturn(Optional.of(existingGroupAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantGroupAccess(1L, "CN=TestGroup", "Test Group",
              PrincipalType.GROUP, AccessLevel.READ_ONLY, "owner"));
      
      assertTrue(exception.getMessage().contains("Group"));
      assertTrue(exception.getMessage().contains("already has READ_WRITE access"));
      assertTrue(exception.getMessage().contains("Use update to change the access level"));
    }

    @Test
    @DisplayName("Should throw exception with specific message when group already has same access level")
    void shouldThrowExceptionWhenGroupAlreadyHasSameAccessLevel() {
      RCAccess existingGroupAccess = new RCAccess();
      existingGroupAccess.setId(10L);
      existingGroupAccess.setResponsibilityCentre(testRC);
      existingGroupAccess.setPrincipalIdentifier("CN=TestGroup");
      existingGroupAccess.setPrincipalType(PrincipalType.GROUP);
      existingGroupAccess.setAccessLevel(AccessLevel.READ_ONLY);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("CN=TestGroup"), eq(PrincipalType.GROUP)))
          .thenReturn(Optional.of(existingGroupAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantGroupAccess(1L, "CN=TestGroup", "Test Group",
              PrincipalType.GROUP, AccessLevel.READ_ONLY, "owner"));
      
      assertTrue(exception.getMessage().contains("Group"));
      assertTrue(exception.getMessage().contains("already has READ_ONLY access"));
      assertFalse(exception.getMessage().contains("Use update")); // Should NOT suggest update for same level
    }

    @Test
    @DisplayName("Should throw exception with specific message when distribution list already has same access level")
    void shouldThrowExceptionWhenDistributionListAlreadyHasSameAccessLevel() {
      RCAccess existingDlAccess = new RCAccess();
      existingDlAccess.setId(10L);
      existingDlAccess.setResponsibilityCentre(testRC);
      existingDlAccess.setPrincipalIdentifier("DL-Finance@corp.local");
      existingDlAccess.setPrincipalType(PrincipalType.DISTRIBUTION_LIST);
      existingDlAccess.setAccessLevel(AccessLevel.READ_WRITE);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("DL-Finance@corp.local"), eq(PrincipalType.DISTRIBUTION_LIST)))
          .thenReturn(Optional.of(existingDlAccess));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
          permissionService.grantGroupAccess(1L, "DL-Finance@corp.local", "Finance DL",
              PrincipalType.DISTRIBUTION_LIST, AccessLevel.READ_WRITE, "owner"));
      
      assertTrue(exception.getMessage().contains("Distribution list"));
      assertTrue(exception.getMessage().contains("already has READ_WRITE access"));
      assertFalse(exception.getMessage().contains("Use update")); // Should NOT suggest update for same level
    }
  }

  @Nested
  @DisplayName("updatePermission Tests")
  class UpdatePermissionTests {

    @Test
    @DisplayName("Should update permission when requester is owner")
    void shouldUpdatePermissionWhenOwner() {
      when(accessRepository.findById(1L)).thenReturn(Optional.of(testAccess));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      when(accessRepository.save(any(RCAccess.class))).thenReturn(testAccess);

      RCAccessDTO result = permissionService.updatePermission(1L, AccessLevel.READ_ONLY, "owner");

      assertNotNull(result);
      verify(accessRepository).save(testAccess);
    }

    @Test
    @DisplayName("Should throw SecurityException when requester is not owner")
    void shouldThrowSecurityExceptionWhenRequesterNotOwner() {
      when(accessRepository.findById(1L)).thenReturn(Optional.of(testAccess));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      assertThrows(SecurityException.class, () ->
          permissionService.updatePermission(1L, AccessLevel.READ_ONLY, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when access record not found")
    void shouldThrowExceptionWhenAccessNotFound() {
      when(accessRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          permissionService.updatePermission(999L, AccessLevel.READ_ONLY, "owner"));
    }
  }

  @Nested
  @DisplayName("revokeAccess Tests")
  class RevokeAccessTests {

    @Test
    @DisplayName("Should revoke access when requester is owner")
    void shouldRevokeAccessWhenOwner() {
      when(accessRepository.findById(1L)).thenReturn(Optional.of(testAccess));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      doNothing().when(accessRepository).deleteAccessById(1L);

      assertDoesNotThrow(() -> permissionService.revokeAccess(1L, "owner"));
      verify(accessRepository).deleteAccessById(1L);
    }

    @Test
    @DisplayName("Should throw SecurityException when requester is not owner")
    void shouldThrowSecurityExceptionWhenRequesterNotOwner() {
      when(accessRepository.findById(1L)).thenReturn(Optional.of(testAccess));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      assertThrows(SecurityException.class, () ->
          permissionService.revokeAccess(1L, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when access record not found")
    void shouldThrowExceptionWhenAccessNotFound() {
      when(accessRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          permissionService.revokeAccess(999L, "owner"));
    }
  }

  @Nested
  @DisplayName("isOwner Tests")
  class IsOwnerTests {

    @Test
    @DisplayName("Should return true when user is RC owner")
    void shouldReturnTrueWhenRCOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));

      boolean result = permissionService.isOwner(1L, "owner");

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return true when user has explicit OWNER access")
    void shouldReturnTrueWhenExplicitOwnerAccess() {
      User anotherOwner = new User();
      anotherOwner.setId(3L);
      anotherOwner.setUsername("anotherowner");

      RCAccess ownerAccess = new RCAccess();
      ownerAccess.setResponsibilityCentre(testRC);
      ownerAccess.setUser(anotherOwner);
      ownerAccess.setAccessLevel(AccessLevel.OWNER);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("anotherowner")).thenReturn(Optional.of(anotherOwner));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, anotherOwner))
          .thenReturn(Optional.of(ownerAccess));

      boolean result = permissionService.isOwner(1L, "anotherowner");

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has non-owner access")
    void shouldReturnFalseWhenNonOwnerAccess() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(testAccess)); // READ_WRITE access

      boolean result = permissionService.isOwner(1L, "testuser");

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when RC not found")
    void shouldReturnFalseWhenRCNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      boolean result = permissionService.isOwner(999L, "owner");

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user not found in local DB or as principalIdentifier")
    void shouldReturnFalseWhenUserNotFound() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("nonexistent"), eq(PrincipalType.USER)))
          .thenReturn(Optional.empty());

      boolean result = permissionService.isOwner(1L, "nonexistent");

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when LDAP user has OWNER access via principalIdentifier")
    void shouldReturnTrueWhenLdapUserHasOwnerAccessByIdentifier() {
      RCAccess ldapOwnerAccess = new RCAccess();
      ldapOwnerAccess.setResponsibilityCentre(testRC);
      ldapOwnerAccess.setPrincipalIdentifier("ldapowner");
      ldapOwnerAccess.setPrincipalType(PrincipalType.USER);
      ldapOwnerAccess.setAccessLevel(AccessLevel.OWNER);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("ldapowner")).thenReturn(Optional.empty());
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
          eq(testRC), eq("ldapowner"), eq(PrincipalType.USER)))
          .thenReturn(Optional.of(ldapOwnerAccess));

      boolean result = permissionService.isOwner(1L, "ldapowner");

      assertTrue(result);
    }
  }

  @Nested
  @DisplayName("canEditContent Tests")
  class CanEditContentTests {

    @Test
    @DisplayName("Should return true when user has READ_WRITE access")
    void shouldReturnTrueWhenReadWriteAccess() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(testUser), any()))
          .thenReturn(Arrays.asList(testAccess)); // READ_WRITE access

      boolean result = permissionService.canEditContent(1L, "testuser");

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return true when user is owner")
    void shouldReturnTrueWhenOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
      // Owner via RC.owner, not via explicit access

      boolean result = permissionService.canEditContent(1L, "owner");

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has READ_ONLY access")
    void shouldReturnFalseWhenReadOnlyAccess() {
      RCAccess readOnlyAccess = new RCAccess();
      readOnlyAccess.setResponsibilityCentre(testRC);
      readOnlyAccess.setUser(testUser);
      readOnlyAccess.setAccessLevel(AccessLevel.READ_ONLY);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(testUser), any()))
          .thenReturn(Arrays.asList(readOnlyAccess));

      boolean result = permissionService.canEditContent(1L, "testuser");

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user has no access")
    void shouldReturnFalseWhenNoAccess() {
      User noAccessUser = new User();
      noAccessUser.setId(99L);
      noAccessUser.setUsername("noaccess");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("noaccess")).thenReturn(Optional.of(noAccessUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(noAccessUser), any()))
          .thenReturn(Collections.emptyList());

      boolean result = permissionService.canEditContent(1L, "noaccess");

      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("getEffectiveAccessLevel Tests")
  class GetEffectiveAccessLevelTests {

    @Test
    @DisplayName("Should return OWNER when user is RC owner")
    void shouldReturnOwnerWhenRCOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "owner", Collections.emptyList());

      assertTrue(result.isPresent());
      assertEquals(AccessLevel.OWNER, result.get());
    }

    @Test
    @DisplayName("Should return access level from direct user access")
    void shouldReturnDirectUserAccessLevel() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(testUser), any()))
          .thenReturn(Arrays.asList(testAccess)); // READ_WRITE

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "testuser", Collections.emptyList());

      assertTrue(result.isPresent());
      assertEquals(AccessLevel.READ_WRITE, result.get());
    }

    @Test
    @DisplayName("Should return highest access level from multiple accesses")
    void shouldReturnHighestAccessLevelFromGroups() {
      RCAccess readOnlyAccess = new RCAccess();
      readOnlyAccess.setAccessLevel(AccessLevel.READ_ONLY);

      RCAccess readWriteAccess = new RCAccess();
      readWriteAccess.setAccessLevel(AccessLevel.READ_WRITE);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(testUser), any()))
          .thenReturn(Arrays.asList(readOnlyAccess, readWriteAccess));

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "testuser", Collections.emptyList());

      assertTrue(result.isPresent());
      assertEquals(AccessLevel.READ_WRITE, result.get());
    }

    @Test
    @DisplayName("Should return empty when user has no access")
    void shouldReturnEmptyWhenNoAccess() {
      User noAccessUser = new User();
      noAccessUser.setId(99L);
      noAccessUser.setUsername("noaccess");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("noaccess")).thenReturn(Optional.of(noAccessUser));
      when(accessRepository.findAllAccessForUserInRC(eq(testRC), eq(noAccessUser), any()))
          .thenReturn(Collections.emptyList());

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "noaccess", Collections.emptyList());

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when RC not found")
    void shouldReturnEmptyWhenRCNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(999L, "owner", Collections.emptyList());

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return access level for LDAP user not in local DB")
    void shouldReturnAccessLevelForLdapUserNotInLocalDb() {
      RCAccess ldapUserAccess = new RCAccess();
      ldapUserAccess.setResponsibilityCentre(testRC);
      ldapUserAccess.setPrincipalIdentifier("ldapuser");
      ldapUserAccess.setPrincipalType(PrincipalType.USER);
      ldapUserAccess.setAccessLevel(AccessLevel.READ_WRITE);

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());
      when(accessRepository.findByPrincipalIdentifierIn(argThat(list ->
          list.contains("ldapuser"))))
          .thenReturn(List.of(ldapUserAccess));

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "ldapuser", Collections.emptyList());

      assertTrue(result.isPresent());
      assertEquals(AccessLevel.READ_WRITE, result.get());
    }

    @Test
    @DisplayName("Should return empty when LDAP user has no access")
    void shouldReturnEmptyWhenLdapUserHasNoAccess() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("ldapnoaccess")).thenReturn(Optional.empty());
      when(accessRepository.findByPrincipalIdentifierIn(argThat(list ->
          list.contains("ldapnoaccess"))))
          .thenReturn(Collections.emptyList());

      Optional<AccessLevel> result = permissionService.getEffectiveAccessLevel(1L, "ldapnoaccess", Collections.emptyList());

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("canManageRC Tests")
  class CanManageRCTests {

    @Test
    @DisplayName("Should return true when user is owner")
    void shouldReturnTrueWhenOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));

      boolean result = permissionService.canManageRC(1L, "owner");

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not owner")
    void shouldReturnFalseWhenNotOwner() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(accessRepository.findByResponsibilityCentreAndUser(testRC, testUser))
          .thenReturn(Optional.of(testAccess)); // READ_WRITE access

      boolean result = permissionService.canManageRC(1L, "testuser");

      assertFalse(result);
    }
  }
}
