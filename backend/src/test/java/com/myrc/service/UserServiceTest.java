/*
 * myRC User Management System
 * Basic User Service Tests
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * License: Apache License 2.0
 */

package com.myrc.service;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.myrc.dto.CreateUserRequest;
import com.myrc.dto.UserDTO;
import com.myrc.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void testCreateLocalUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("password123");
        request.setAuthProvider("LOCAL");
        Set<String> roles = new java.util.HashSet<>();
        roles.add("USER");
        request.setRoles(roles);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("LOCAL", result.getAuthProvider());
    }

    @Test
    @Transactional
    void testCreateLocalUserWithoutEmail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("noemailuser");
        request.setEmail(null);  // No email provided
        request.setFullName("No Email User");
        request.setPassword("password123");
        request.setAuthProvider("LOCAL");
        Set<String> roles = new java.util.HashSet<>();
        roles.add("USER");
        request.setRoles(roles);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("noemailuser", result.getUsername());
        assertEquals("noemailuser@noemail.local", result.getEmail());
        assertEquals("LOCAL", result.getAuthProvider());
    }

    @Test
    @Transactional
    void testCreateLocalUserWithEmptyEmail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("emptyemailuser");
        request.setEmail("");  // Empty email
        request.setFullName("Empty Email User");
        request.setPassword("password123");
        request.setAuthProvider("LOCAL");
        Set<String> roles = new java.util.HashSet<>();
        roles.add("USER");
        request.setRoles(roles);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("emptyemailuser", result.getUsername());
        assertEquals("emptyemailuser@noemail.local", result.getEmail());
        assertEquals("LOCAL", result.getAuthProvider());
    }

    @Test
    @Transactional
    void testDuplicateUsernameRejected() {
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setUsername("testuser");
        request1.setEmail("test1@example.com");
        request1.setPassword("password123");
        request1.setAuthProvider("LOCAL");

        userService.createUser(request1);

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("testuser");
        request2.setEmail("test2@example.com");
        request2.setPassword("password123");
        request2.setAuthProvider("LOCAL");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request2));
    }

    @Test
    @Transactional
    void testAuthenticate() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setAuthProvider("LOCAL");

        userService.createUser(request);

        Optional<UserDTO> result = userService.authenticate("testuser", "password123");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    @Transactional
    void testAuthenticateWrongPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setAuthProvider("LOCAL");

        userService.createUser(request);

        Optional<UserDTO> result = userService.authenticate("testuser", "wrongpassword");
        assertFalse(result.isPresent());
    }
}
