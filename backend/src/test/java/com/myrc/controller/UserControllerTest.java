/*
 * myRC - User Controller Tests
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

import com.myrc.dto.ChangePasswordRequest;
import com.myrc.dto.CreateUserRequest;
import com.myrc.dto.UpdateUserRequest;
import com.myrc.dto.UserDTO;
import com.myrc.service.UserService;
import java.util.Arrays;
import java.util.Collection;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for UserController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService userService;

  private Authentication authentication;
  private UserController controller;
  private UserDTO testUser;

  @BeforeEach
  void setUp() {
    controller = new UserController(userService, null);
    
    // Create a simple Authentication implementation
    authentication = createAuthentication("testuser");

    testUser = new UserDTO();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFullName("Test User");
    testUser.setEnabled(true);
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
  @DisplayName("createUser Tests")
  class CreateUserTests {

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
      CreateUserRequest request = new CreateUserRequest();
      request.setUsername("newuser");
      request.setPassword("Password123!");
      request.setEmail("new@example.com");
      request.setFullName("New User");

      when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);

      ResponseEntity<?> response = controller.createUser(request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
      verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when user exists")
    void shouldReturnBadRequestWhenUserExists() {
      CreateUserRequest request = new CreateUserRequest();
      request.setUsername("existinguser");
      request.setPassword("Password123!");

      when(userService.createUser(any(CreateUserRequest.class)))
          .thenThrow(new IllegalArgumentException("User already exists"));

      ResponseEntity<?> response = controller.createUser(request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getAllUsers Tests")
  class GetAllUsersTests {

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
      when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));

      ResponseEntity<List<UserDTO>> response = controller.getAllUsers();

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should return empty list when no users")
    void shouldReturnEmptyListWhenNoUsers() {
      when(userService.getAllUsers()).thenReturn(Arrays.asList());

      ResponseEntity<List<UserDTO>> response = controller.getAllUsers();

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());
    }
  }

  @Nested
  @DisplayName("getCurrentUser Tests")
  class GetCurrentUserTests {

    @Test
    @DisplayName("Should return current user when authenticated")
    void shouldReturnCurrentUserWhenAuthenticated() {
      when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

      ResponseEntity<?> response = controller.getCurrentUser(authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("testuser", ((UserDTO) response.getBody()).getUsername());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication")
    void shouldReturnUnauthorizedWhenNoAuthentication() {
      ResponseEntity<?> response = controller.getCurrentUser(null);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return unauthorized when user not found")
    void shouldReturnUnauthorizedWhenUserNotFound() {
      Authentication unknownAuth = createAuthentication("unknownuser");
      when(userService.getUserByUsername("unknownuser")).thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getCurrentUser(unknownAuth);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getUserById Tests")
  class GetUserByIdTests {

    @Test
    @DisplayName("Should return user when found")
    void shouldReturnUserWhenFound() {
      when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

      ResponseEntity<?> response = controller.getUserById(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      when(userService.getUserById(999L)).thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getUserById(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getUserByUsername Tests")
  class GetUserByUsernameTests {

    @Test
    @DisplayName("Should return user when found")
    void shouldReturnUserWhenFound() {
      when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

      ResponseEntity<?> response = controller.getUserByUsername("testuser");

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      when(userService.getUserByUsername("unknown")).thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getUserByUsername("unknown");

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getUserByEmail Tests")
  class GetUserByEmailTests {

    @Test
    @DisplayName("Should return user when found")
    void shouldReturnUserWhenFound() {
      when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

      ResponseEntity<?> response = controller.getUserByEmail("test@example.com");

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      when(userService.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getUserByEmail("unknown@example.com");

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getEnabledUsers Tests")
  class GetEnabledUsersTests {

    @Test
    @DisplayName("Should return enabled users")
    void shouldReturnEnabledUsers() {
      when(userService.getEnabledUsers()).thenReturn(Arrays.asList(testUser));

      ResponseEntity<List<UserDTO>> response = controller.getEnabledUsers();

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
    }
  }

  @Nested
  @DisplayName("updateUser Tests")
  class UpdateUserTests {

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
      UpdateUserRequest request = new UpdateUserRequest();
      request.setFullName("Updated Name");
      request.setEmail("updated@example.com");

      when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(testUser);

      ResponseEntity<?> response = controller.updateUser(1L, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return bad request when update fails")
    void shouldReturnBadRequestWhenUpdateFails() {
      UpdateUserRequest request = new UpdateUserRequest();
      request.setEmail("invalid-email");

      when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
          .thenThrow(new IllegalArgumentException("Invalid email"));

      ResponseEntity<?> response = controller.updateUser(1L, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("changePassword Tests")
  class ChangePasswordTests {

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setCurrentPassword("OldPassword123!");
      request.setNewPassword("NewPassword123!");

      doNothing().when(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));

      ResponseEntity<String> response = controller.changePassword(1L, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Password changed successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return bad request when password change fails")
    void shouldReturnBadRequestWhenPasswordChangeFails() {
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setCurrentPassword("WrongPassword");
      request.setNewPassword("NewPassword123!");

      doThrow(new IllegalArgumentException("Current password is incorrect"))
          .when(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));

      ResponseEntity<String> response = controller.changePassword(1L, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("resetPassword Tests")
  class ResetPasswordTests {

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
      doNothing().when(userService).resetPassword(1L, "NewPassword123!");

      ResponseEntity<String> response = controller.resetPassword(1L, "NewPassword123!");

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Password reset successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return bad request when reset fails")
    void shouldReturnBadRequestWhenResetFails() {
      doThrow(new IllegalArgumentException("Invalid password"))
          .when(userService).resetPassword(1L, "weak");

      ResponseEntity<String> response = controller.resetPassword(1L, "weak");

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("enableUser Tests")
  class EnableUserTests {

    @Test
    @DisplayName("Should enable user successfully")
    void shouldEnableUserSuccessfully() {
      doNothing().when(userService).enableUser(1L);

      ResponseEntity<String> response = controller.enableUser(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("User enabled successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      doThrow(new IllegalArgumentException("User not found"))
          .when(userService).enableUser(999L);

      ResponseEntity<String> response = controller.enableUser(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("disableUser Tests")
  class DisableUserTests {

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
      doNothing().when(userService).disableUser(1L);

      ResponseEntity<String> response = controller.disableUser(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("User disabled successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      doThrow(new IllegalArgumentException("User not found"))
          .when(userService).disableUser(999L);

      ResponseEntity<String> response = controller.disableUser(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("unlockAccount Tests")
  class UnlockAccountTests {

    @Test
    @DisplayName("Should unlock account successfully")
    void shouldUnlockAccountSuccessfully() {
      doNothing().when(userService).unlockAccount(1L);

      ResponseEntity<String> response = controller.unlockAccount(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("User account unlocked successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      doThrow(new IllegalArgumentException("User not found"))
          .when(userService).unlockAccount(999L);

      ResponseEntity<String> response = controller.unlockAccount(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("verifyEmail Tests")
  class VerifyEmailTests {

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
      doNothing().when(userService).verifyEmail(1L);

      ResponseEntity<String> response = controller.verifyEmail(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Email verified successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      doThrow(new IllegalArgumentException("User not found"))
          .when(userService).verifyEmail(999L);

      ResponseEntity<String> response = controller.verifyEmail(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteUser Tests")
  class DeleteUserTests {

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
      doNothing().when(userService).deleteUser(1L);

      ResponseEntity<String> response = controller.deleteUser(1L);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      doThrow(new IllegalArgumentException("User not found"))
          .when(userService).deleteUser(999L);

      ResponseEntity<String> response = controller.deleteUser(999L);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }
}
