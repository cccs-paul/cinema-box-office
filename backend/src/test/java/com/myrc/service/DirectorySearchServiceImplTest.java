/*
 * myRC - Directory Search Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.myrc.config.LdapProperties;
import com.myrc.model.User;
import com.myrc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Unit tests for {@link DirectorySearchServiceImpl}.
 *
 * <p>Tests the application database user search path. LDAP integration
 * is tested via integration tests since Java 25 restricts mocking of
 * javax.naming.directory interfaces from the java.base module.</p>
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-02-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DirectorySearchServiceImpl Tests")
class DirectorySearchServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private LdapProperties ldapProperties;
    private DirectorySearchServiceImpl service;

    @BeforeEach
    void setUp() {
        ldapProperties = new LdapProperties();
        ldapProperties.setEnabled(false);
        service = new DirectorySearchServiceImpl(userRepository, ldapProperties, null);
    }

    /**
     * Create test User instance.
     */
    private User createTestUser(String username, String fullName, String email) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        return user;
    }

    @Nested
    @DisplayName("searchUsers - input validation")
    class SearchUsersValidationTests {

        @Test
        @DisplayName("should return empty list for null query")
        void shouldReturnEmptyListForNullQuery() {
            List<DirectorySearchService.SearchResult> results = service.searchUsers(null, 10);
            assertTrue(results.isEmpty());
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("should return all users for empty query")
        void shouldReturnAllUsersForEmptyQuery() {
            User user = createTestUser("jsmith", "John Smith", "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).identifier());
        }

        @Test
        @DisplayName("should return all users for whitespace-only query")
        void shouldReturnAllUsersForWhitespaceQuery() {
            User user = createTestUser("admin", "Admin User", "admin@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("   ", 10);

            assertEquals(1, results.size());
            assertEquals("admin", results.get(0).identifier());
        }
    }

    @Nested
    @DisplayName("searchUsers - App DB matching")
    class SearchUsersAppMatchingTests {

        @Test
        @DisplayName("should match users by username")
        void shouldMatchUsersByUsername() {
            User user = createTestUser("jsmith", "John Smith", "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("jsmi", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).identifier());
            assertEquals("John Smith", results.get(0).displayName());
            assertEquals("APP", results.get(0).source());
            assertEquals("john@example.com", results.get(0).email());
        }

        @Test
        @DisplayName("should match users by full name")
        void shouldMatchUsersByFullName() {
            User user = createTestUser("jsmith", "John Smith", "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("smith", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).identifier());
        }

        @Test
        @DisplayName("should match users by email")
        void shouldMatchUsersByEmail() {
            User user = createTestUser("jsmith", "John Smith", "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("john@", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).identifier());
        }

        @Test
        @DisplayName("should be case-insensitive")
        void shouldBeCaseInsensitive() {
            User user = createTestUser("JSmith", "John Smith", "John@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("JOHN", 10);

            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("should not match non-matching users")
        void shouldNotMatchNonMatchingUsers() {
            User user = createTestUser("jsmith", "John Smith", "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("xyz", 10);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should handle user with null fields gracefully")
        void shouldHandleUserWithNullFields() {
            User user = createTestUser("jsmith", null, null);
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("jsmith", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).identifier());
            assertEquals("jsmith", results.get(0).displayName());
            assertNull(results.get(0).email());
        }
    }

    @Nested
    @DisplayName("searchUsers - result ordering and limiting")
    class SearchUsersOrderingTests {

        @Test
        @DisplayName("should respect maxResults limit")
        void shouldRespectMaxResultsLimit() {
            List<User> users = List.of(
                    createTestUser("alice", "Alice A", "alice@example.com"),
                    createTestUser("bob", "Bob B", "bob@example.com"),
                    createTestUser("carol", "Carol C", "carol@example.com")
            );
            when(userRepository.findAll()).thenReturn(users);

            List<DirectorySearchService.SearchResult> results = service.searchUsers("example.com", 2);

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should sort results alphabetically by identifier")
        void shouldSortResultsAlphabetically() {
            List<User> users = List.of(
                    createTestUser("charlie", "Charlie C", "c@ex.com"),
                    createTestUser("alice", "Alice A", "a@ex.com"),
                    createTestUser("bob", "Bob B", "b@ex.com")
            );
            when(userRepository.findAll()).thenReturn(users);

            List<DirectorySearchService.SearchResult> results = service.searchUsers("ex.com", 10);

            assertEquals(3, results.size());
            assertEquals("alice", results.get(0).identifier());
            assertEquals("bob", results.get(1).identifier());
            assertEquals("charlie", results.get(2).identifier());
        }

        @Test
        @DisplayName("should use identifier as display name when fullName is null")
        void shouldUseIdentifierWhenFullNameIsNull() {
            User user = createTestUser("jsmith", null, "john@example.com");
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("john@", 10);

            assertEquals(1, results.size());
            assertEquals("jsmith", results.get(0).displayName());
        }

        @Test
        @DisplayName("should deduplicate results by identifier")
        void shouldDeduplicateByIdentifier() {
            List<User> users = List.of(
                    createTestUser("jsmith", "John Smith", "john@example.com"),
                    createTestUser("jsmith", "John Smith (duplicate)", "john2@example.com")
            );
            when(userRepository.findAll()).thenReturn(users);

            List<DirectorySearchService.SearchResult> results = service.searchUsers("jsmith", 10);

            assertEquals(1, results.size());
            assertEquals("John Smith", results.get(0).displayName());
        }
    }

    @Nested
    @DisplayName("searchUsers - error handling")
    class SearchUsersErrorHandlingTests {

        @Test
        @DisplayName("should handle repository exception gracefully")
        void shouldHandleRepositoryException() {
            when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

            List<DirectorySearchService.SearchResult> results = service.searchUsers("test", 10);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("searchGroups - LDAP disabled")
    class SearchGroupsLdapDisabledTests {

        @Test
        @DisplayName("should return empty list when LDAP is disabled")
        void shouldReturnEmptyListWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchGroups("finance", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for null query")
        void shouldReturnEmptyListForNullQuery() {
            List<DirectorySearchService.SearchResult> results = service.searchGroups(null, 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for empty query when LDAP disabled")
        void shouldReturnEmptyListForEmptyQueryWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchGroups("", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for whitespace-only query when LDAP disabled")
        void shouldReturnEmptyListForWhitespaceQueryWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchGroups("   ", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when LDAP is enabled but context source is null")
        void shouldReturnEmptyListWhenContextSourceIsNull() {
            ldapProperties.setEnabled(true);
            service = new DirectorySearchServiceImpl(userRepository, ldapProperties, null);

            List<DirectorySearchService.SearchResult> results = service.searchGroups("test", 10);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("searchDistributionLists - LDAP disabled")
    class SearchDistributionListsLdapDisabledTests {

        @Test
        @DisplayName("should return empty list when LDAP is disabled")
        void shouldReturnEmptyListWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchDistributionLists("finance", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for null query")
        void shouldReturnEmptyListForNullQuery() {
            List<DirectorySearchService.SearchResult> results = service.searchDistributionLists(null, 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for empty query when LDAP disabled")
        void shouldReturnEmptyListForEmptyQueryWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchDistributionLists("", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for whitespace-only query when LDAP disabled")
        void shouldReturnEmptyListForWhitespaceQueryWhenLdapDisabled() {
            List<DirectorySearchService.SearchResult> results = service.searchDistributionLists("   ", 10);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when LDAP is enabled but context source is null")
        void shouldReturnEmptyListWhenContextSourceIsNull() {
            ldapProperties.setEnabled(true);
            service = new DirectorySearchServiceImpl(userRepository, ldapProperties, null);

            List<DirectorySearchService.SearchResult> results = service.searchDistributionLists("test", 10);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("SearchResult record")
    class SearchResultRecordTests {

        @Test
        @DisplayName("should create SearchResult with all fields")
        void shouldCreateSearchResultWithAllFields() {
            DirectorySearchService.SearchResult result =
                    new DirectorySearchService.SearchResult("jsmith", "John Smith", "APP", "john@example.com");

            assertEquals("jsmith", result.identifier());
            assertEquals("John Smith", result.displayName());
            assertEquals("APP", result.source());
            assertEquals("john@example.com", result.email());
        }

        @Test
        @DisplayName("should create SearchResult with null email")
        void shouldCreateSearchResultWithNullEmail() {
            DirectorySearchService.SearchResult result =
                    new DirectorySearchService.SearchResult("cn=Finance,dc=corp", "Finance", "LDAP", null);

            assertEquals("cn=Finance,dc=corp", result.identifier());
            assertEquals("Finance", result.displayName());
            assertEquals("LDAP", result.source());
            assertNull(result.email());
        }

        @Test
        @DisplayName("should support equality comparison")
        void shouldSupportEquality() {
            DirectorySearchService.SearchResult r1 =
                    new DirectorySearchService.SearchResult("id", "name", "APP", "e@e.com");
            DirectorySearchService.SearchResult r2 =
                    new DirectorySearchService.SearchResult("id", "name", "APP", "e@e.com");

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
        }
    }
}
