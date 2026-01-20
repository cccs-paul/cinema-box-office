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
import com.boxoffice.model.User;
import com.boxoffice.repository.UserRepository;
import com.boxoffice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Initializes default application data on startup.
 * Creates a default admin user if no users exist in the database.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        initializeDefaultUsers();
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
                logger.warning("Failed to create default admin user: " + e.getMessage());
            }
        } else {
            logger.info("Admin user already exists, skipping initialization");
        }
    }
}
