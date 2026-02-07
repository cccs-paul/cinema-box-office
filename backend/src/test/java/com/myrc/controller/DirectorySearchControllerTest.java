/*
 * myRC - Directory Search Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-07
 * Version: 1.0.0
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.service.DirectorySearchService;
import com.myrc.service.DirectorySearchService.SearchResult;

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

/**
 * Unit tests for {@link DirectorySearchController}.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-02-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DirectorySearchController Tests")
class DirectorySearchControllerTest {

    @Mock
    private DirectorySearchService directorySearchService;

    private DirectorySearchController controller;

    @BeforeEach
    void setUp() {
        controller = new DirectorySearchController(directorySearchService);
    }

    @Nested
    @DisplayName("GET /directory/users")
    class SearchUsersTests {

        @Test
        @DisplayName("should return 200 with matching users")
        void shouldReturnMatchingUsers() {
            List<SearchResult> expected = List.of(
                    new SearchResult("jsmith", "John Smith", "APP", "jsmith@example.com"),
                    new SearchResult("jdoe", "Jane Doe", "LDAP", "jdoe@example.com")
            );
            when(directorySearchService.searchUsers(eq("joh"), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchUsers("joh", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("jsmith", response.getBody().get(0).identifier());
            verify(directorySearchService).searchUsers("joh", 10);
        }

        @Test
        @DisplayName("should return 200 with empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            when(directorySearchService.searchUsers(eq("xyz"), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchUsers("xyz", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("should return 400 when query is null")
        void shouldReturnBadRequestWhenQueryIsNull() {
            ResponseEntity<List<SearchResult>> response = controller.searchUsers(null, 10);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(directorySearchService);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is empty")
        void shouldReturnAllEntriesWhenQueryIsEmpty() {
            List<SearchResult> expected = List.of(
                    new SearchResult("jsmith", "John Smith", "APP", "jsmith@example.com")
            );
            when(directorySearchService.searchUsers(eq(""), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchUsers("", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            verify(directorySearchService).searchUsers("", 10);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is blank")
        void shouldReturnAllEntriesWhenQueryIsBlank() {
            when(directorySearchService.searchUsers(eq("   "), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchUsers("   ", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(directorySearchService).searchUsers("   ", 10);
        }

        @Test
        @DisplayName("should clamp max results to 50")
        void shouldClampMaxResultsTo50() {
            when(directorySearchService.searchUsers(eq("test"), eq(50))).thenReturn(Collections.emptyList());

            controller.searchUsers("test", 100);

            verify(directorySearchService).searchUsers("test", 50);
        }

        @Test
        @DisplayName("should clamp max results to 1 minimum")
        void shouldClampMaxResultsToMinimum1() {
            when(directorySearchService.searchUsers(eq("test"), eq(1))).thenReturn(Collections.emptyList());

            controller.searchUsers("test", 0);

            verify(directorySearchService).searchUsers("test", 1);
        }

        @Test
        @DisplayName("should use default max results of 10")
        void shouldUseDefaultMaxResults() {
            when(directorySearchService.searchUsers(eq("test"), eq(10))).thenReturn(Collections.emptyList());

            controller.searchUsers("test", 10);

            verify(directorySearchService).searchUsers("test", 10);
        }

        @Test
        @DisplayName("should return results with correct fields")
        void shouldReturnResultsWithCorrectFields() {
            SearchResult result = new SearchResult("admin", "Admin User", "APP", "admin@example.com");
            when(directorySearchService.searchUsers(eq("admin"), eq(10))).thenReturn(List.of(result));

            ResponseEntity<List<SearchResult>> response = controller.searchUsers("admin", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            SearchResult returned = response.getBody().get(0);
            assertEquals("admin", returned.identifier());
            assertEquals("Admin User", returned.displayName());
            assertEquals("APP", returned.source());
            assertEquals("admin@example.com", returned.email());
        }
    }

    @Nested
    @DisplayName("GET /directory/groups")
    class SearchGroupsTests {

        @Test
        @DisplayName("should return 200 with matching groups")
        void shouldReturnMatchingGroups() {
            List<SearchResult> expected = List.of(
                    new SearchResult("cn=Finance,ou=Groups,dc=example,dc=com", "Finance", "LDAP", null),
                    new SearchResult("cn=IT-Team,ou=Groups,dc=example,dc=com", "IT Team", "LDAP", null)
            );
            when(directorySearchService.searchGroups(eq("fi"), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchGroups("fi", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("cn=Finance,ou=Groups,dc=example,dc=com", response.getBody().get(0).identifier());
            verify(directorySearchService).searchGroups("fi", 10);
        }

        @Test
        @DisplayName("should return 200 with empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            when(directorySearchService.searchGroups(eq("xyz"), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchGroups("xyz", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("should return 400 when query is null")
        void shouldReturnBadRequestWhenQueryIsNull() {
            ResponseEntity<List<SearchResult>> response = controller.searchGroups(null, 10);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(directorySearchService);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is empty")
        void shouldReturnAllEntriesWhenQueryIsEmpty() {
            List<SearchResult> expected = List.of(
                    new SearchResult("cn=Finance,ou=Groups,dc=example,dc=com", "Finance", "LDAP", null)
            );
            when(directorySearchService.searchGroups(eq(""), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchGroups("", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            verify(directorySearchService).searchGroups("", 10);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is blank")
        void shouldReturnAllEntriesWhenQueryIsBlank() {
            when(directorySearchService.searchGroups(eq("   "), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchGroups("   ", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(directorySearchService).searchGroups("   ", 10);
        }

        @Test
        @DisplayName("should clamp max results to 50")
        void shouldClampMaxResultsTo50() {
            when(directorySearchService.searchGroups(eq("test"), eq(50))).thenReturn(Collections.emptyList());

            controller.searchGroups("test", 100);

            verify(directorySearchService).searchGroups("test", 50);
        }

        @Test
        @DisplayName("should clamp max results to 1 minimum")
        void shouldClampMaxResultsToMinimum1() {
            when(directorySearchService.searchGroups(eq("test"), eq(1))).thenReturn(Collections.emptyList());

            controller.searchGroups("test", 0);

            verify(directorySearchService).searchGroups("test", 1);
        }

        @Test
        @DisplayName("should return results with null email for groups")
        void shouldReturnResultsWithNullEmail() {
            SearchResult result = new SearchResult("cn=DevOps,ou=Groups,dc=corp,dc=com", "DevOps", "LDAP", null);
            when(directorySearchService.searchGroups(eq("dev"), eq(10))).thenReturn(List.of(result));

            ResponseEntity<List<SearchResult>> response = controller.searchGroups("dev", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertNull(response.getBody().get(0).email());
            assertEquals("LDAP", response.getBody().get(0).source());
        }
    }

    @Nested
    @DisplayName("GET /directory/distribution-lists")
    class SearchDistributionListsTests {

        @Test
        @DisplayName("should return 200 with matching distribution lists")
        void shouldReturnMatchingDistributionLists() {
            List<SearchResult> expected = List.of(
                    new SearchResult("cn=all-staff,ou=distribution-lists,dc=myrc,dc=local",
                            "all-staff - All staff distribution list", "LDAP", "all-staff@myrc.local"),
                    new SearchResult("cn=finance-team,ou=distribution-lists,dc=myrc,dc=local",
                            "finance-team - Finance team distribution list", "LDAP", "finance-team@myrc.local")
            );
            when(directorySearchService.searchDistributionLists(eq("staff"), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists("staff", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("cn=all-staff,ou=distribution-lists,dc=myrc,dc=local", response.getBody().get(0).identifier());
            assertEquals("all-staff@myrc.local", response.getBody().get(0).email());
            verify(directorySearchService).searchDistributionLists("staff", 10);
        }

        @Test
        @DisplayName("should return 200 with empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            when(directorySearchService.searchDistributionLists(eq("xyz"), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists("xyz", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("should return 400 when query is null")
        void shouldReturnBadRequestWhenQueryIsNull() {
            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists(null, 10);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(directorySearchService);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is empty")
        void shouldReturnAllEntriesWhenQueryIsEmpty() {
            List<SearchResult> expected = List.of(
                    new SearchResult("cn=all-staff,ou=distribution-lists,dc=myrc,dc=local",
                            "all-staff - All staff distribution list", "LDAP", "all-staff@myrc.local")
            );
            when(directorySearchService.searchDistributionLists(eq(""), eq(10))).thenReturn(expected);

            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists("", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            verify(directorySearchService).searchDistributionLists("", 10);
        }

        @Test
        @DisplayName("should return 200 with all entries when query is blank")
        void shouldReturnAllEntriesWhenQueryIsBlank() {
            when(directorySearchService.searchDistributionLists(eq("   "), eq(10))).thenReturn(Collections.emptyList());

            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists("   ", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(directorySearchService).searchDistributionLists("   ", 10);
        }

        @Test
        @DisplayName("should clamp max results to 50")
        void shouldClampMaxResultsTo50() {
            when(directorySearchService.searchDistributionLists(eq("test"), eq(50))).thenReturn(Collections.emptyList());

            controller.searchDistributionLists("test", 100);

            verify(directorySearchService).searchDistributionLists("test", 50);
        }

        @Test
        @DisplayName("should clamp max results to 1 minimum")
        void shouldClampMaxResultsToMinimum1() {
            when(directorySearchService.searchDistributionLists(eq("test"), eq(1))).thenReturn(Collections.emptyList());

            controller.searchDistributionLists("test", 0);

            verify(directorySearchService).searchDistributionLists("test", 1);
        }

        @Test
        @DisplayName("should return results with email for distribution lists")
        void shouldReturnResultsWithEmail() {
            SearchResult result = new SearchResult("cn=ops,ou=distribution-lists,dc=myrc,dc=local",
                    "ops - Operations", "LDAP", "ops@myrc.local");
            when(directorySearchService.searchDistributionLists(eq("ops"), eq(10))).thenReturn(List.of(result));

            ResponseEntity<List<SearchResult>> response = controller.searchDistributionLists("ops", 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("ops@myrc.local", response.getBody().get(0).email());
            assertEquals("LDAP", response.getBody().get(0).source());
        }
    }
}
