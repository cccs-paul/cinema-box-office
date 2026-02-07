/*
 * myRC - Directory Search Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import java.util.List;

/**
 * Service interface for searching users and groups from the application
 * database and optionally from LDAP directory.
 *
 * <p>Provides autocomplete/typeahead functionality for the permissions UI,
 * combining results from the local user database and the configured
 * LDAP directory (when enabled).</p>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-07
 */
public interface DirectorySearchService {

    /**
     * Search result representing a user or group entry.
     *
     * @param identifier the unique identifier (username or group DN)
     * @param displayName the human-readable display name
     * @param source the source of the entry (APP, LDAP)
     * @param email the email address (may be null)
     */
    record SearchResult(String identifier, String displayName, String source, String email) {}

    /**
     * Search for users matching the given query string.
     * Searches both the application database and LDAP directory (when enabled).
     * An empty query returns all available entries up to the max limit.
     *
     * @param query the search query (matched against username, full name, email; empty returns all)
     * @param maxResults the maximum number of results to return
     * @return list of matching user search results
     */
    List<SearchResult> searchUsers(String query, int maxResults);

    /**
     * Search for security groups matching the given query string.
     * Only returns results when LDAP is enabled and configured.
     * An empty query returns all available entries up to the max limit.
     *
     * @param query the search query (matched against group name/DN; empty returns all)
     * @param maxResults the maximum number of results to return
     * @return list of matching group search results
     */
    List<SearchResult> searchGroups(String query, int maxResults);

    /**
     * Search for distribution lists matching the given query string.
     * Only returns results when LDAP is enabled and configured.
     * Distribution lists are searched in a separate OU from security groups.
     * An empty query returns all available entries up to the max limit.
     *
     * @param query the search query (matched against list name, description, or email; empty returns all)
     * @param maxResults the maximum number of results to return
     * @return list of matching distribution list search results
     */
    List<SearchResult> searchDistributionLists(String query, int maxResults);
}
