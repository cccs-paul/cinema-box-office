/*
 * myRC - Directory Search Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.config.LdapProperties;
import com.myrc.model.User;
import com.myrc.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of DirectorySearchService.
 * Searches users from the application database and optionally from LDAP.
 * Searches groups exclusively from LDAP when enabled.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-07
 */
@Service
public class DirectorySearchServiceImpl implements DirectorySearchService {

    private static final Logger logger = LoggerFactory.getLogger(DirectorySearchServiceImpl.class);

    private final UserRepository userRepository;
    private final LdapProperties ldapProperties;
    private final ContextSource ldapContextSource;

    /**
     * Constructor with optional LDAP context source injection.
     *
     * @param userRepository the user repository
     * @param ldapProperties the LDAP properties
     * @param ldapContextSource the LDAP context source (null when LDAP is disabled)
     */
    public DirectorySearchServiceImpl(
            UserRepository userRepository,
            LdapProperties ldapProperties,
            @Autowired(required = false) ContextSource ldapContextSource) {
        this.userRepository = userRepository;
        this.ldapProperties = ldapProperties;
        this.ldapContextSource = ldapContextSource;
    }

    @Override
    public List<DirectorySearchService.SearchResult> searchUsers(String query, int maxResults) {
        if (query == null) {
            return List.of();
        }

        String normalizedQuery = query.trim().toLowerCase();
        Map<String, DirectorySearchService.SearchResult> results = new LinkedHashMap<>();

        // Search application database users (empty query returns all)
        searchAppUsers(normalizedQuery, results);

        // Search LDAP users if enabled
        if (isLdapEnabled()) {
            searchLdapUsers(normalizedQuery, results);
        }

        return results.values().stream()
                .sorted(Comparator.comparing(DirectorySearchService.SearchResult::identifier))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    @Override
    public List<DirectorySearchService.SearchResult> searchGroups(String query, int maxResults) {
        if (query == null) {
            return List.of();
        }

        if (!isLdapEnabled()) {
            return List.of();
        }

        String normalizedQuery = query.trim().toLowerCase();
        List<DirectorySearchService.SearchResult> results = new ArrayList<>();

        searchLdapGroups(normalizedQuery, results);

        return results.stream()
                .sorted(Comparator.comparing(DirectorySearchService.SearchResult::identifier))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    @Override
    public List<DirectorySearchService.SearchResult> searchDistributionLists(String query, int maxResults) {
        if (query == null) {
            return List.of();
        }

        if (!isLdapEnabled()) {
            return List.of();
        }

        String normalizedQuery = query.trim().toLowerCase();
        List<DirectorySearchService.SearchResult> results = new ArrayList<>();

        searchLdapDistributionLists(normalizedQuery, results);

        return results.stream()
                .sorted(Comparator.comparing(DirectorySearchService.SearchResult::identifier))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Search users in the application database.
     */
    private void searchAppUsers(String query, Map<String, DirectorySearchService.SearchResult> results) {
        try {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (matchesQuery(user, query)) {
                    String identifier = user.getUsername();
                    if (!results.containsKey(identifier)) {
                        results.put(identifier, new DirectorySearchService.SearchResult(
                                identifier,
                                user.getFullName() != null ? user.getFullName() : identifier,
                                "APP",
                                user.getEmail()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error searching app users: {}", e.getMessage());
        }
    }

    /**
     * Check if a user matches the search query.
     * An empty query matches all users (browse-all mode).
     */
    private boolean matchesQuery(User user, String query) {
        if (query.isEmpty()) {
            return true;
        }
        if (user.getUsername() != null && user.getUsername().toLowerCase().contains(query)) {
            return true;
        }
        if (user.getFullName() != null && user.getFullName().toLowerCase().contains(query)) {
            return true;
        }
        if (user.getEmail() != null && user.getEmail().toLowerCase().contains(query)) {
            return true;
        }
        return false;
    }

    /**
     * Search users in the LDAP directory.
     */
    private void searchLdapUsers(String query, Map<String, DirectorySearchService.SearchResult> results) {
        try {
            DirContext ctx = ldapContextSource.getReadOnlyContext();
            try {
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                controls.setCountLimit(20);

                String usernameAttr = ldapProperties.getAttributes() != null
                        ? ldapProperties.getAttributes().getUsername() : "uid";
                String emailAttr = ldapProperties.getAttributes() != null
                        ? ldapProperties.getAttributes().getEmail() : "mail";
                String nameAttr = ldapProperties.getAttributes() != null
                        ? ldapProperties.getAttributes().getName() : "cn";

                controls.setReturningAttributes(new String[]{usernameAttr, emailAttr, nameAttr});

                // Build filter to search by username, cn, or mail
                // Empty query matches all entries (browse-all mode)
                String filter;
                if (query.isEmpty()) {
                    filter = String.format("(|(%s=*)(%s=*)(%s=*))",
                            usernameAttr, nameAttr, emailAttr);
                } else {
                    String escapedQuery = escapeForLdapFilter(query);
                    filter = String.format(
                            "(|(%s=*%s*)(%s=*%s*)(%s=*%s*))",
                            usernameAttr, escapedQuery,
                            nameAttr, escapedQuery,
                            emailAttr, escapedQuery
                    );
                }

                String searchBase = ldapProperties.getUserSearchBase();
                NamingEnumeration<javax.naming.directory.SearchResult> searchResults = ctx.search(searchBase, filter, controls);

                while (searchResults.hasMore()) {
                    javax.naming.directory.SearchResult sr = searchResults.next();
                    Attributes attrs = sr.getAttributes();

                    String username = getAttributeValue(attrs, usernameAttr);
                    String displayName = getAttributeValue(attrs, nameAttr);
                    String email = getAttributeValue(attrs, emailAttr);

                    if (username != null && !results.containsKey(username)) {
                        results.put(username, new DirectorySearchService.SearchResult(
                                username,
                                displayName != null ? displayName : username,
                                "LDAP",
                                email
                        ));
                    }
                }
            } finally {
                ctx.close();
            }
        } catch (Exception e) {
            logger.warn("Error searching LDAP users: {}", e.getMessage());
        }
    }

    /**
     * Search groups in the LDAP directory.
     */
    private void searchLdapGroups(String query, List<DirectorySearchService.SearchResult> results) {
        try {
            DirContext ctx = ldapContextSource.getReadOnlyContext();
            try {
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                controls.setCountLimit(20);

                String groupNameAttr = ldapProperties.getGroupNameAttribute() != null
                        ? ldapProperties.getGroupNameAttribute() : "cn";

                controls.setReturningAttributes(new String[]{groupNameAttr, "description"});

                // Search for groups matching the query by cn or description
                // Empty query matches all groups (browse-all mode)
                String filter;
                if (query.isEmpty()) {
                    filter = "(objectClass=groupOfNames)";
                } else {
                    String escapedQuery = escapeForLdapFilter(query);
                    filter = String.format(
                            "(&(objectClass=groupOfNames)(|(%s=*%s*)(description=*%s*)))",
                            groupNameAttr, escapedQuery, escapedQuery
                    );
                }

                String searchBase = ldapProperties.getGroupSearchBase();
                NamingEnumeration<javax.naming.directory.SearchResult> searchResults = ctx.search(searchBase, filter, controls);

                while (searchResults.hasMore()) {
                    javax.naming.directory.SearchResult sr = searchResults.next();
                    Attributes attrs = sr.getAttributes();
                    String nameInDir = sr.getNameInNamespace();

                    String groupName = getAttributeValue(attrs, groupNameAttr);
                    String description = getAttributeValue(attrs, "description");

                    if (groupName != null) {
                        results.add(new DirectorySearchService.SearchResult(
                                nameInDir,
                                groupName + (description != null ? " - " + description : ""),
                                "LDAP",
                                null
                        ));
                    }
                }
            } finally {
                ctx.close();
            }
        } catch (Exception e) {
            logger.warn("Error searching LDAP groups: {}", e.getMessage());
        }
    }

    /**
     * Search distribution lists in the LDAP directory.
     * Distribution lists are stored in a separate OU from security groups
     * and typically have a mail attribute for email-based addressing.
     */
    private void searchLdapDistributionLists(String query, List<DirectorySearchService.SearchResult> results) {
        try {
            DirContext ctx = ldapContextSource.getReadOnlyContext();
            try {
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                controls.setCountLimit(20);

                String groupNameAttr = ldapProperties.getGroupNameAttribute() != null
                        ? ldapProperties.getGroupNameAttribute() : "cn";

                controls.setReturningAttributes(new String[]{groupNameAttr, "description", "mail"});

                // Search for distribution lists matching the query by cn, description, or mail
                // Empty query matches all distribution lists (browse-all mode)
                String filter;
                if (query.isEmpty()) {
                    filter = "(objectClass=groupOfNames)";
                } else {
                    String escapedQuery = escapeForLdapFilter(query);
                    filter = String.format(
                            "(&(objectClass=groupOfNames)(|(%s=*%s*)(description=*%s*)(mail=*%s*)))",
                            groupNameAttr, escapedQuery, escapedQuery, escapedQuery
                    );
                }

                String searchBase = ldapProperties.getDistributionListSearchBase();
                NamingEnumeration<javax.naming.directory.SearchResult> searchResults = ctx.search(searchBase, filter, controls);

                while (searchResults.hasMore()) {
                    javax.naming.directory.SearchResult sr = searchResults.next();
                    Attributes attrs = sr.getAttributes();
                    String nameInDir = sr.getNameInNamespace();

                    String listName = getAttributeValue(attrs, groupNameAttr);
                    String description = getAttributeValue(attrs, "description");
                    String email = getAttributeValue(attrs, "mail");

                    if (listName != null) {
                        results.add(new DirectorySearchService.SearchResult(
                                nameInDir,
                                listName + (description != null ? " - " + description : ""),
                                "LDAP",
                                email
                        ));
                    }
                }
            } finally {
                ctx.close();
            }
        } catch (Exception e) {
            logger.warn("Error searching LDAP distribution lists: {}", e.getMessage());
        }
    }

    /**
     * Check if LDAP is enabled and context source is available.
     */
    private boolean isLdapEnabled() {
        return ldapProperties.isEnabled() && ldapContextSource != null;
    }

    /**
     * Safely get a string attribute value from LDAP attributes.
     */
    private String getAttributeValue(Attributes attrs, String attributeName) {
        try {
            Attribute attr = attrs.get(attributeName);
            if (attr != null) {
                Object value = attr.get();
                return value != null ? value.toString() : null;
            }
        } catch (NamingException e) {
            logger.debug("Error reading attribute {}: {}", attributeName, e.getMessage());
        }
        return null;
    }

    /**
     * Escape special characters for LDAP filter values.
     * Prevents LDAP injection attacks.
     *
     * @param input the raw input string
     * @return escaped string safe for LDAP filter use
     */
    private String escapeForLdapFilter(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\5c"); break;
                case '*': sb.append("\\2a"); break;
                case '(': sb.append("\\28"); break;
                case ')': sb.append("\\29"); break;
                case '\0': sb.append("\\00"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
