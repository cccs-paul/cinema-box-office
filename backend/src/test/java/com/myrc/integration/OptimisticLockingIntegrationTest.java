/*
 * myRC - Optimistic Locking Integration Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-01
 * Version: 1.0.0
 *
 * Description:
 * Integration tests verifying optimistic locking behavior
 * for concurrent data modifications.
 */
package com.myrc.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.myrc.model.FundingItem;
import com.myrc.model.FundingSource;
import com.myrc.model.FiscalYear;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for optimistic locking behavior.
 * These tests verify that concurrent modifications are properly
 * detected and prevented using JPA's @Version mechanism.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OptimisticLockingIntegrationTest {

    @Autowired
    private FundingItemRepository fundingItemRepository;

    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    @Autowired
    private ResponsibilityCentreRepository rcRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private ResponsibilityCentre testRC;
    private FiscalYear testFY;

    @BeforeEach
    void setUp() {
        // Create test user with unique name to avoid conflicts
        String uniqueSuffix = String.valueOf(System.nanoTime());
        testUser = new User();
        testUser.setUsername("optimistictest" + uniqueSuffix);
        testUser.setPasswordHash("$2a$10$dummyhash");
        testUser.setEmail("optimistictest" + uniqueSuffix + "@example.com");
        testUser.setFullName("Optimistic Test User");
        testUser.setAuthProvider(User.AuthProvider.LOCAL);
        testUser = userRepository.save(testUser);

        // Create test responsibility centre
        testRC = new ResponsibilityCentre();
        testRC.setName("Optimistic Test RC " + uniqueSuffix);
        testRC.setDescription("Test RC for optimistic locking");
        testRC.setOwner(testUser);
        testRC = rcRepository.save(testRC);

        // Create test fiscal year
        testFY = new FiscalYear();
        testFY.setName("FY Optimistic " + uniqueSuffix);
        testFY.setResponsibilityCentre(testRC);
        testFY = fiscalYearRepository.save(testFY);
    }

    @Test
    @DisplayName("Version should increment on update")
    @Transactional
    void versionShouldIncrementOnUpdate() {
        // Create a funding item
        FundingItem item = new FundingItem();
        item.setName("Test Item");
        item.setDescription("Initial description");
        item.setSource(FundingSource.BUSINESS_PLAN);
        item.setFiscalYear(testFY);
        
        FundingItem saved = fundingItemRepository.save(item);
        Long initialVersion = saved.getVersion();
        assertNotNull(initialVersion);
        assertEquals(0L, initialVersion);

        // Update the item
        saved.setDescription("Updated description");
        FundingItem updated = fundingItemRepository.saveAndFlush(saved);
        
        // Version should be incremented
        assertEquals(initialVersion + 1, updated.getVersion());
    }

    @Test
    @DisplayName("Concurrent modifications should throw optimistic locking exception")
    void concurrentModificationsShouldThrow() {
        // Create a funding item
        FundingItem item = new FundingItem();
        item.setName("Concurrent Test Item");
        item.setDescription("Initial description");
        item.setSource(FundingSource.BUSINESS_PLAN);
        item.setFiscalYear(testFY);
        
        FundingItem saved = fundingItemRepository.saveAndFlush(item);
        Long itemId = saved.getId();
        Long initialVersion = saved.getVersion();
        
        // Simulate another transaction by directly updating the version in the database
        // This bypasses the JPA persistence context, simulating a concurrent update
        jdbcTemplate.update(
            "UPDATE funding_items SET description = 'Concurrent update', version = version + 1 WHERE id = ?",
            itemId
        );

        // Clear the persistence context to ensure our entity is detached
        entityManager.clear();
        
        // Reload the saved reference (but it still has the old version)
        // Re-attach the original object with the stale version
        saved.setDescription("Second update (should fail)");
        
        // This should throw an optimistic locking exception because the version in DB is different
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            fundingItemRepository.saveAndFlush(saved);
        });
    }

    @Test
    @DisplayName("Responsibility Centre version should increment")
    @Transactional
    void rcVersionShouldIncrement() {
        Long initialVersion = testRC.getVersion();
        assertNotNull(initialVersion);
        
        testRC.setDescription("Updated description");
        ResponsibilityCentre updated = rcRepository.saveAndFlush(testRC);
        
        assertEquals(initialVersion + 1, updated.getVersion());
    }

    @Test
    @DisplayName("Fiscal Year version should increment")
    @Transactional
    void fyVersionShouldIncrement() {
        Long initialVersion = testFY.getVersion();
        assertNotNull(initialVersion);
        
        testFY.setName("Updated FY Name");
        FiscalYear updated = fiscalYearRepository.saveAndFlush(testFY);
        
        assertEquals(initialVersion + 1, updated.getVersion());
    }
}
