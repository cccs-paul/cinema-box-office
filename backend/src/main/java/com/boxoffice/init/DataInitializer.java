/*
 * Cinema Box Office User Management System
 * Data Initialization Component
 * 
 * Author: Box Office Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 */

package com.boxoffice.init;

import com.boxoffice.dto.CreateUserRequest;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import com.boxoffice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Initializes default application data on startup.
 * Creates a default admin user if no users exist in the database.
 * Skips initialization in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ResponsibilityCentreRepository rcRepository;

    @Autowired
    private RCAccessRepository rcAccessRepository;

    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        initializeDefaultUsers();
        initializeDemoRC();
    }

    private void initializeDefaultUsers() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Creating default admin user...");
            
            CreateUserRequest adminRequest = new CreateUserRequest();
            adminRequest.setUsername("admin");
            adminRequest.setEmail("admin@boxoffice.local");
            adminRequest.setFullName("Administrator");
            adminRequest.setPassword("Admin@123");
            adminRequest.setAuthProvider("LOCAL");
            
            Set<String> adminRoles = new HashSet<>();
            adminRoles.add("ADMIN");
            adminRoles.add("USER");
            adminRequest.setRoles(adminRoles);
            adminRequest.setProfileDescription("Default system administrator account");
            
            try {
                userService.createUser(adminRequest);
                logger.info("Default admin user created successfully with username: admin, password: Admin@123");
                logger.info("IMPORTANT: Please change the default admin password immediately for security!");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create default admin user: " + e.getMessage());
            }
        } else {
            logger.info("Admin user already exists, skipping initialization");
        }
        
        // Check if default-user exists (for unauthenticated development access)
        if (userRepository.findByUsername("default-user").isEmpty()) {
            logger.info("Creating default-user for unauthenticated access...");
            
            CreateUserRequest defaultRequest = new CreateUserRequest();
            defaultRequest.setUsername("default-user");
            defaultRequest.setEmail("default@boxoffice.local");
            defaultRequest.setFullName("Default User");
            defaultRequest.setPassword("DefaultPass@123");
            defaultRequest.setAuthProvider("LOCAL");
            
            Set<String> userRoles = new HashSet<>();
            userRoles.add("USER");
            defaultRequest.setRoles(userRoles);
            defaultRequest.setProfileDescription("Default user for unauthenticated access");
            
            try {
                userService.createUser(defaultRequest);
                logger.info("Default user created successfully for unauthenticated development access");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create default user: " + e.getMessage());
            }
        } else {
            logger.info("Default user already exists, skipping initialization");
        }
    }

    /**
     * Initialize the Demo RC with read-only access for all users.
     * The Demo RC is owned by admin and all other users get read-only access.
     * Also creates a Demo FY "FY 2025-2026" for the Demo RC.
     */
    private void initializeDemoRC() {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            logger.warning("Admin user not found, cannot create Demo RC");
            return;
        }

        ResponsibilityCentre demoRC;

        // Check if Demo RC already exists
        if (rcRepository.findByNameAndOwner("Demo", adminUser).isPresent()) {
            logger.info("Demo RC already exists, ensuring all users have read-only access");
            demoRC = rcRepository.findByNameAndOwner("Demo", adminUser).get();
            grantDemoAccessToAllUsers();
        } else {
            logger.info("Creating Demo RC...");
            try {
                demoRC = new ResponsibilityCentre(
                    "Demo",
                    "Demo responsibility centre for exploring the application. All users have read-only access.",
                    adminUser
                );
                demoRC = rcRepository.save(demoRC);
                logger.info("Demo RC created successfully");

                // Grant read-only access to all non-admin users
                grantDemoAccessToAllUsers();
            } catch (Exception e) {
                logger.warning(() -> "Failed to create Demo RC: " + e.getMessage());
                return;
            }
        }

        // Create Demo FY if it doesn't exist
        initializeDemoFY(demoRC);
    }

    /**
     * Initialize the Demo FY "FY 2025-2026" for the Demo RC.
     *
     * @param demoRC the Demo responsibility centre
     */
    private void initializeDemoFY(ResponsibilityCentre demoRC) {
        String demoFYName = "FY 2025-2026";

        if (fiscalYearRepository.existsByNameAndResponsibilityCentre(demoFYName, demoRC)) {
            logger.info("Demo FY already exists, skipping creation");
            return;
        }

        logger.info("Creating Demo FY...");
        try {
            FiscalYear demoFY = new FiscalYear(
                demoFYName,
                "Demo fiscal year for exploring the application.",
                demoRC
            );
            fiscalYearRepository.save(demoFY);
            logger.info("Demo FY created successfully: " + demoFYName);
        } catch (Exception e) {
            logger.warning(() -> "Failed to create Demo FY: " + e.getMessage());
        }
    }

    /**
     * Grant read-only access to the Demo RC for all users who don't already have access.
     */
    private void grantDemoAccessToAllUsers() {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            return;
        }

        ResponsibilityCentre demoRC = rcRepository.findByNameAndOwner("Demo", adminUser).orElse(null);
        if (demoRC == null) {
            return;
        }

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Skip if this is the owner (admin)
            if (user.getId().equals(adminUser.getId())) {
                continue;
            }

            // Check if user already has access
            if (rcAccessRepository.findByResponsibilityCentreAndUser(demoRC, user).isPresent()) {
                continue;
            }

            // Grant read-only access
            try {
                RCAccess access = new RCAccess(demoRC, user, RCAccess.AccessLevel.READ_ONLY);
                rcAccessRepository.save(access);
                logger.info(() -> "Granted read-only access to Demo RC for user: " + user.getUsername());
            } catch (Exception e) {
                logger.warning(() -> "Failed to grant Demo RC access to user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
}
