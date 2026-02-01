/*
 * myRC - Registration Request DTO Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-31
 * Version: 1.0.0
 */
package com.myrc.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RegistrationRequest DTO.
 * Tests validation logic for self-registration requests.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
class RegistrationRequestTest {

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setPassword("Password123");
        request.setConfirmPassword("Password123");
        request.setFullName("Valid User");
    }

    @Test
    @DisplayName("Should create DTO successfully")
    void testDtoCreation() {
        assertNotNull(request);
    }

    @Test
    @DisplayName("Should create DTO with constructor")
    void testDtoCreationWithConstructor() {
        RegistrationRequest req = new RegistrationRequest(
            "testuser", "test@example.com", "password123", "password123", "Test User");
        
        assertEquals("testuser", req.getUsername());
        assertEquals("test@example.com", req.getEmail());
        assertEquals("password123", req.getPassword());
        assertEquals("password123", req.getConfirmPassword());
        assertEquals("Test User", req.getFullName());
    }

    @Nested
    @DisplayName("isValid Tests")
    class IsValidTests {

        @Test
        @DisplayName("Should return true for valid request")
        void shouldReturnTrueForValidRequest() {
            assertTrue(request.isValid());
        }

        @Test
        @DisplayName("Should return false for null username")
        void shouldReturnFalseForNullUsername() {
            request.setUsername(null);
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for empty username")
        void shouldReturnFalseForEmptyUsername() {
            request.setUsername("");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for whitespace username")
        void shouldReturnFalseForWhitespaceUsername() {
            request.setUsername("   ");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for username too short")
        void shouldReturnFalseForUsernameTooShort() {
            request.setUsername("ab");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for username too long")
        void shouldReturnFalseForUsernameTooLong() {
            request.setUsername("a".repeat(51));
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for null email")
        void shouldReturnFalseForNullEmail() {
            request.setEmail(null);
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for empty email")
        void shouldReturnFalseForEmptyEmail() {
            request.setEmail("");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for invalid email format")
        void shouldReturnFalseForInvalidEmailFormat() {
            request.setEmail("invalid-email");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for null password")
        void shouldReturnFalseForNullPassword() {
            request.setPassword(null);
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for empty password")
        void shouldReturnFalseForEmptyPassword() {
            request.setPassword("");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for password too short")
        void shouldReturnFalseForPasswordTooShort() {
            request.setPassword("short");
            request.setConfirmPassword("short");
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for null confirmPassword")
        void shouldReturnFalseForNullConfirmPassword() {
            request.setConfirmPassword(null);
            assertFalse(request.isValid());
        }

        @Test
        @DisplayName("Should return false for password mismatch")
        void shouldReturnFalseForPasswordMismatch() {
            request.setConfirmPassword("DifferentPassword");
            assertFalse(request.isValid());
        }
    }

    @Nested
    @DisplayName("getValidationError Tests")
    class GetValidationErrorTests {

        @Test
        @DisplayName("Should return null for valid request")
        void shouldReturnNullForValidRequest() {
            assertNull(request.getValidationError());
        }

        @Test
        @DisplayName("Should return error for null username")
        void shouldReturnErrorForNullUsername() {
            request.setUsername(null);
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Username is required"));
        }

        @Test
        @DisplayName("Should return error for empty username")
        void shouldReturnErrorForEmptyUsername() {
            request.setUsername("   ");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Username is required"));
        }

        @Test
        @DisplayName("Should return error for username too short")
        void shouldReturnErrorForUsernameTooShort() {
            request.setUsername("ab");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("at least 3 characters"));
        }

        @Test
        @DisplayName("Should return error for username too long")
        void shouldReturnErrorForUsernameTooLong() {
            request.setUsername("a".repeat(51));
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("not exceed 50 characters"));
        }

        @Test
        @DisplayName("Should return error for invalid username characters")
        void shouldReturnErrorForInvalidUsernameCharacters() {
            request.setUsername("user@name!");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("only contain letters, numbers"));
        }

        @Test
        @DisplayName("Should accept username with valid special chars")
        void shouldAcceptUsernameWithValidSpecialChars() {
            request.setUsername("user_name-123");
            assertNull(request.getValidationError());
        }

        @Test
        @DisplayName("Should return error for null email")
        void shouldReturnErrorForNullEmail() {
            request.setEmail(null);
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Email is required"));
        }

        @Test
        @DisplayName("Should return error for empty email")
        void shouldReturnErrorForEmptyEmail() {
            request.setEmail("   ");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Email is required"));
        }

        @Test
        @DisplayName("Should return error for invalid email format")
        void shouldReturnErrorForInvalidEmailFormat() {
            request.setEmail("not-an-email");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should return error for null password")
        void shouldReturnErrorForNullPassword() {
            request.setPassword(null);
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Password is required"));
        }

        @Test
        @DisplayName("Should return error for empty password")
        void shouldReturnErrorForEmptyPassword() {
            request.setPassword("");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Password is required"));
        }

        @Test
        @DisplayName("Should return error for password too short")
        void shouldReturnErrorForPasswordTooShort() {
            request.setPassword("short");
            request.setConfirmPassword("short");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("at least 8 characters"));
        }

        @Test
        @DisplayName("Should return error for password mismatch")
        void shouldReturnErrorForPasswordMismatch() {
            request.setConfirmPassword("DifferentPassword");
            String error = request.getValidationError();
            assertNotNull(error);
            assertTrue(error.contains("Passwords do not match"));
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set username")
        void shouldGetAndSetUsername() {
            request.setUsername("newuser");
            assertEquals("newuser", request.getUsername());
        }

        @Test
        @DisplayName("Should get and set email")
        void shouldGetAndSetEmail() {
            request.setEmail("new@example.com");
            assertEquals("new@example.com", request.getEmail());
        }

        @Test
        @DisplayName("Should get and set password")
        void shouldGetAndSetPassword() {
            request.setPassword("newpassword");
            assertEquals("newpassword", request.getPassword());
        }

        @Test
        @DisplayName("Should get and set confirmPassword")
        void shouldGetAndSetConfirmPassword() {
            request.setConfirmPassword("newconfirm");
            assertEquals("newconfirm", request.getConfirmPassword());
        }

        @Test
        @DisplayName("Should get and set fullName")
        void shouldGetAndSetFullName() {
            request.setFullName("New Full Name");
            assertEquals("New Full Name", request.getFullName());
        }
    }
}
