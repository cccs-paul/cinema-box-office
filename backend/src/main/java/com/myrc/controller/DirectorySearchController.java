/*
 * myRC - Directory Search Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.service.DirectorySearchService;
import com.myrc.service.DirectorySearchService.SearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

/**
 * REST Controller for directory search operations.
 * Provides autocomplete/typeahead endpoints for searching users and groups
 * from the application database and LDAP directory.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-02-07
 */
@RestController
@RequestMapping("/directory")
@Tag(name = "Directory Search", description = "APIs for searching users and groups for autocomplete")
public class DirectorySearchController {

    private static final Logger logger = Logger.getLogger(DirectorySearchController.class.getName());
    private static final int DEFAULT_MAX_RESULTS = 10;
    private static final int MAX_ALLOWED_RESULTS = 50;

    private final DirectorySearchService directorySearchService;

    /**
     * Constructor.
     *
     * @param directorySearchService the directory search service
     */
    public DirectorySearchController(DirectorySearchService directorySearchService) {
        this.directorySearchService = directorySearchService;
    }

    /**
     * Search for users matching a query string.
     * Returns results from both the application database and LDAP directory (when enabled).
     *
     * @param query the search query (minimum 1 character)
     * @param maxResults the maximum number of results to return (default 10, max 50)
     * @return list of matching user search results
     */
    @GetMapping("/users")
    @Operation(summary = "Search users",
            description = "Searches for users in the app database and LDAP directory (when enabled). "
                    + "Returns usernames, display names, email addresses, and source. "
                    + "An empty query returns all available entries up to the max limit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Query parameter is null")
    })
    public ResponseEntity<List<SearchResult>> searchUsers(
            @Parameter(description = "Search query (empty string returns all entries)")
            @RequestParam("q") String query,
            @Parameter(description = "Maximum results to return (default 10, max 50)")
            @RequestParam(value = "max", defaultValue = "10", required = false) int maxResults) {

        if (query == null) {
            return ResponseEntity.badRequest().build();
        }

        int clampedMax = Math.min(Math.max(maxResults, 1), MAX_ALLOWED_RESULTS);
        logger.fine("Searching users with query: '" + query + "', max: " + clampedMax);

        List<SearchResult> results = directorySearchService.searchUsers(query, clampedMax);
        return ResponseEntity.ok(results);
    }

    /**
     * Search for security groups matching a query string.
     * Only returns results when LDAP is enabled and configured.
     *
     * @param query the search query (minimum 1 character)
     * @param maxResults the maximum number of results to return (default 10, max 50)
     * @return list of matching group search results
     */
    @GetMapping("/groups")
    @Operation(summary = "Search groups",
            description = "Searches for security groups in the LDAP directory (when enabled). "
                    + "Returns group DNs, display names, and source. "
                    + "An empty query returns all available entries up to the max limit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Query parameter is null")
    })
    public ResponseEntity<List<SearchResult>> searchGroups(
            @Parameter(description = "Search query (empty string returns all entries)")
            @RequestParam("q") String query,
            @Parameter(description = "Maximum results to return (default 10, max 50)")
            @RequestParam(value = "max", defaultValue = "10", required = false) int maxResults) {

        if (query == null) {
            return ResponseEntity.badRequest().build();
        }

        int clampedMax = Math.min(Math.max(maxResults, 1), MAX_ALLOWED_RESULTS);
        logger.fine("Searching groups with query: '" + query + "', max: " + clampedMax);

        List<SearchResult> results = directorySearchService.searchGroups(query, clampedMax);
        return ResponseEntity.ok(results);
    }

    /**
     * Search for distribution lists matching a query string.
     * Only returns results when LDAP is enabled and configured.
     * Distribution lists are searched in a separate OU from security groups
     * and may include email addresses.
     *
     * @param query the search query (minimum 1 character)
     * @param maxResults the maximum number of results to return (default 10, max 50)
     * @return list of matching distribution list search results
     */
    @GetMapping("/distribution-lists")
    @Operation(summary = "Search distribution lists",
            description = "Searches for distribution lists in the LDAP directory (when enabled). "
                    + "Returns list DNs, display names, email addresses, and source. "
                    + "An empty query returns all available entries up to the max limit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Query parameter is null")
    })
    public ResponseEntity<List<SearchResult>> searchDistributionLists(
            @Parameter(description = "Search query (empty string returns all entries)")
            @RequestParam("q") String query,
            @Parameter(description = "Maximum results to return (default 10, max 50)")
            @RequestParam(value = "max", defaultValue = "10", required = false) int maxResults) {

        if (query == null) {
            return ResponseEntity.badRequest().build();
        }

        int clampedMax = Math.min(Math.max(maxResults, 1), MAX_ALLOWED_RESULTS);
        logger.fine("Searching distribution lists with query: '" + query + "', max: " + clampedMax);

        List<SearchResult> results = directorySearchService.searchDistributionLists(query, clampedMax);
        return ResponseEntity.ok(results);
    }
}
