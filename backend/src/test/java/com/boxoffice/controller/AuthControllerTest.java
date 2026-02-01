/*
 * myRC - Authentication Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-31
 * Version: 1.0.0
 */
package com.boxoffice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boxoffice.config.LoginMethodsProperties;
import com.boxoffice.dto.CreateUserRequest;
import com.boxoffice.dto.ErrorResponse;
import com.boxoffice.dto.LoginMethodsDTO;
import com.boxoffice.dto.RegistrationRequest;
import com.boxoffice.dto.UserDTO;
import com.boxoffice.service.UserService;
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

/**
 * Unit tests for AuthController.
 * Tests login methods configuration and self-registration endpoints.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private LoginMethodsProperties loginMethodsProperties;

    @Mock
    private UserService userService;

    private AuthController controller;

    /**
     * Helper method to create LoginMethodsProperties with specified settings.
     */
    private LoginMethodsProperties createLoginMethodsProperties(
            boolean appAccountEnabled, boolean allowRegistration,
            boolean ldapEnabled, boolean oauth2Enabled) {
        LoginMethodsProperties props = new LoginMethodsProperties();
        
        LoginMethodsProperties.AppAccountConfig appAccount = new LoginMethodsProperties.AppAccountConfig();
        appAccount.setEnabled(appAccountEnabled);
        appAccount.setAllowRegistration(allowRegistration);
        props.setAppAccount(appAccount);
        
        LoginMethodsProperties.LdapConfig ldap = new LoginMethodsProperties.LdapConfig();
        ldap.setEnabled(ldapEnabled);
        props.setLdap(ldap);
        
        LoginMethodsProperties.OAuth2Config oauth2 = new LoginMethodsProperties.OAuth2Config();
        oauth2.setEnabled(oauth2Enabled);
        props.setOauth2(oauth2);
        
        return props;
    }

    @BeforeEach
    void setUp() {
        // Default: all enabled, registration allowed
        loginMethodsProperties = createLoginMethodsProperties(true, true, true, true);
        controller = new AuthController(loginMethodsProperties, userService);
    }

    @Test
    @DisplayName("Should create controller successfully")
    void testControllerCreation() {
        assertNotNull(controller);
    }

    @Nested
    @DisplayName("getLoginMethods Tests")
    class GetLoginMethodsTests {

        @Test
        @DisplayName("Should return login methods with all enabled")
        void shouldReturnLoginMethodsAllEnabled() {
            ResponseEntity<LoginMethodsDTO> response = controller.getLoginMethods();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getAppAccount().isEnabled());
            assertTrue(response.getBody().getAppAccount().isAllowRegistration());
            assertTrue(response.getBody().isLdapEnabled());
            assertTrue(response.getBody().isOauth2Enabled());
        }

        @Test
        @DisplayName("Should return login methods with all disabled")
        void shouldReturnLoginMethodsAllDisabled() {
            loginMethodsProperties = createLoginMethodsProperties(false, false, false, false);
            controller = new AuthController(loginMethodsProperties, userService);

            ResponseEntity<LoginMethodsDTO> response = controller.getLoginMethods();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().getAppAccount().isEnabled());
            assertFalse(response.getBody().getAppAccount().isAllowRegistration());
            assertFalse(response.getBody().isLdapEnabled());
            assertFalse(response.getBody().isOauth2Enabled());
        }

        @Test
        @DisplayName("Should return login methods with only LDAP enabled")
        void shouldReturnLoginMethodsOnlyLdapEnabled() {
            loginMethodsProperties = createLoginMethodsProperties(false, false, true, false);
            controller = new AuthController(loginMethodsProperties, userService);

            ResponseEntity<LoginMethodsDTO> response = controller.getLoginMethods();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().getAppAccount().isEnabled());
            assertTrue(response.getBody().isLdapEnabled());
            assertFalse(response.getBody().isOauth2Enabled());
        }

        @Test
        @DisplayName("Should return app account enabled but registration disabled")
        void shouldReturnAppAccountEnabledRegistrationDisabled() {
            loginMethodsProperties = createLoginMethodsProperties(true, false, false, false);
            controller = new AuthController(loginMethodsProperties, userService);

            ResponseEntity<LoginMethodsDTO> response = controller.getLoginMethods();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getAppAccount().isEnabled());
            assertFalse(response.getBody().getAppAccount().isAllowRegistration());
        }
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        private RegistrationRequest createValidRequest() {
            RegistrationRequest request = new RegistrationRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setPassword("Password123");
            request.setConfirmPassword("Password123");
            request.setFullName("New User");
            return request;
        }

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.empty());

            UserDTO createdUser = new UserDTO();
            createdUser.setId(1L);
            createdUser.setUsername("newuser");
            createdUser.setEmail("newuser@example.com");
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof UserDTO);
            verify(userService).createUser(any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("Should fail when App Account is disabled")
        void shouldFailWhenAppAccountDisabled() {
            loginMethodsProperties = createLoginMethodsProperties(false, true, true, true);
            controller = new AuthController(loginMethodsProperties, userService);

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("App Account login is currently disabled"));
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail when registration is disabled")
        void shouldFailWhenRegistrationDisabled() {
            loginMethodsProperties = createLoginMethodsProperties(true, false, true, true);
            controller = new AuthController(loginMethodsProperties, userService);

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("Self-registration is currently disabled"));
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail with invalid username - too short")
        void shouldFailWithInvalidUsernameTooShort() {
            RegistrationRequest request = createValidRequest();
            request.setUsername("ab");  // Too short

            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail with invalid username - invalid characters")
        void shouldFailWithInvalidUsernameInvalidChars() {
            RegistrationRequest request = createValidRequest();
            request.setUsername("user@name!");  // Invalid characters

            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail with invalid email")
        void shouldFailWithInvalidEmail() {
            RegistrationRequest request = createValidRequest();
            request.setEmail("invalid-email");

            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail with password too short")
        void shouldFailWithPasswordTooShort() {
            RegistrationRequest request = createValidRequest();
            request.setPassword("short");
            request.setConfirmPassword("short");

            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail with password mismatch")
        void shouldFailWithPasswordMismatch() {
            RegistrationRequest request = createValidRequest();
            request.setConfirmPassword("DifferentPassword");

            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("Passwords do not match"));
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail when username already exists")
        void shouldFailWhenUsernameExists() {
            UserDTO existingUser = new UserDTO();
            existingUser.setUsername("newuser");
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.of(existingUser));

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("Username is already taken"));
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should fail when service throws email exists exception")
        void shouldFailWhenEmailExists() {
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.empty());
            when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("Email is already registered"));
        }

        @Test
        @DisplayName("Should handle unexpected exception")
        void shouldHandleUnexpectedException() {
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.empty());
            when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

            RegistrationRequest request = createValidRequest();
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
        }

        @Test
        @DisplayName("Should use username as fullName when fullName is null")
        void shouldUseUsernameAsFullNameWhenNull() {
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.empty());

            UserDTO createdUser = new UserDTO();
            createdUser.setId(1L);
            createdUser.setUsername("newuser");
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

            RegistrationRequest request = createValidRequest();
            request.setFullName(null);
            
            ResponseEntity<?> response = controller.register(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(userService).createUser(argThat(req -> 
                req.getFullName().equals("newuser")
            ));
        }
    }

    @Nested
    @DisplayName("checkUsernameAvailability Tests")
    class CheckUsernameAvailabilityTests {

        @Test
        @DisplayName("Should return available for non-existent username")
        void shouldReturnAvailableForNonExistentUsername() {
            when(userService.getUserByUsername("newuser")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.checkUsernameAvailability("newuser");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof AuthController.UsernameAvailabilityResponse);
            AuthController.UsernameAvailabilityResponse body = 
                (AuthController.UsernameAvailabilityResponse) response.getBody();
            assertTrue(body.isAvailable());
            assertEquals("newuser", body.getUsername());
        }

        @Test
        @DisplayName("Should return unavailable for existing username")
        void shouldReturnUnavailableForExistingUsername() {
            UserDTO existingUser = new UserDTO();
            existingUser.setUsername("existinguser");
            when(userService.getUserByUsername("existinguser")).thenReturn(Optional.of(existingUser));

            ResponseEntity<?> response = controller.checkUsernameAvailability("existinguser");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof AuthController.UsernameAvailabilityResponse);
            AuthController.UsernameAvailabilityResponse body = 
                (AuthController.UsernameAvailabilityResponse) response.getBody();
            assertFalse(body.isAvailable());
        }

        @Test
        @DisplayName("Should return bad request for null username")
        void shouldReturnBadRequestForNullUsername() {
            ResponseEntity<?> response = controller.checkUsernameAvailability(null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
        }

        @Test
        @DisplayName("Should return bad request for empty username")
        void shouldReturnBadRequestForEmptyUsername() {
            ResponseEntity<?> response = controller.checkUsernameAvailability("   ");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
        }

        @Test
        @DisplayName("Should return bad request for username too short")
        void shouldReturnBadRequestForUsernameTooShort() {
            ResponseEntity<?> response = controller.checkUsernameAvailability("ab");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("at least 3 characters"));
        }

        @Test
        @DisplayName("Should return bad request for invalid characters")
        void shouldReturnBadRequestForInvalidCharacters() {
            ResponseEntity<?> response = controller.checkUsernameAvailability("user@name!");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody() instanceof ErrorResponse);
            ErrorResponse error = (ErrorResponse) response.getBody();
            assertTrue(error.getMessage().contains("only contain letters, numbers"));
        }

        @Test
        @DisplayName("Should accept username with underscores and hyphens")
        void shouldAcceptUsernameWithUnderscoresAndHyphens() {
            when(userService.getUserByUsername("user_name-123")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.checkUsernameAvailability("user_name-123");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof AuthController.UsernameAvailabilityResponse);
            AuthController.UsernameAvailabilityResponse body = 
                (AuthController.UsernameAvailabilityResponse) response.getBody();
            assertTrue(body.isAvailable());
        }
    }

    @Nested
    @DisplayName("UsernameAvailabilityResponse Tests")
    class UsernameAvailabilityResponseTests {

        @Test
        @DisplayName("Should create response with constructor")
        void shouldCreateResponseWithConstructor() {
            AuthController.UsernameAvailabilityResponse response = 
                new AuthController.UsernameAvailabilityResponse("testuser", true);

            assertEquals("testuser", response.getUsername());
            assertTrue(response.isAvailable());
        }

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            AuthController.UsernameAvailabilityResponse response = 
                new AuthController.UsernameAvailabilityResponse("original", true);
            
            response.setUsername("updated");
            
            assertEquals("updated", response.getUsername());
        }

        @Test
        @DisplayName("Should set and get available")
        void shouldSetAndGetAvailable() {
            AuthController.UsernameAvailabilityResponse response = 
                new AuthController.UsernameAvailabilityResponse("testuser", true);
            
            response.setAvailable(false);
            
            assertFalse(response.isAvailable());
        }
    }
}
