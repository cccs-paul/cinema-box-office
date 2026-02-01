/*
 * myRC - Login Methods DTO Tests
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
 * Unit tests for LoginMethodsDTO.
 * Tests the login methods configuration data transfer object.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
class LoginMethodsDTOTest {

    private LoginMethodsDTO dto;

    @BeforeEach
    void setUp() {
        dto = new LoginMethodsDTO();
    }

    @Test
    @DisplayName("Should create DTO successfully with default constructor")
    void testDtoCreationDefault() {
        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should create DTO with parameterized constructor")
    void testDtoCreationParameterized() {
        LoginMethodsDTO.AppAccountConfig appAccount = 
            new LoginMethodsDTO.AppAccountConfig(true, true);
        
        LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, true, false);
        
        assertNotNull(loginMethods);
        assertTrue(loginMethods.getAppAccount().isEnabled());
        assertTrue(loginMethods.getAppAccount().isAllowRegistration());
        assertTrue(loginMethods.isLdapEnabled());
        assertFalse(loginMethods.isOauth2Enabled());
    }

    @Nested
    @DisplayName("AppAccountConfig Tests")
    class AppAccountConfigTests {

        @Test
        @DisplayName("Should create AppAccountConfig with default constructor")
        void shouldCreateWithDefaultConstructor() {
            LoginMethodsDTO.AppAccountConfig config = new LoginMethodsDTO.AppAccountConfig();
            assertNotNull(config);
        }

        @Test
        @DisplayName("Should create AppAccountConfig with parameterized constructor")
        void shouldCreateWithParameterizedConstructor() {
            LoginMethodsDTO.AppAccountConfig config = 
                new LoginMethodsDTO.AppAccountConfig(true, true);
            
            assertTrue(config.isEnabled());
            assertTrue(config.isAllowRegistration());
        }

        @Test
        @DisplayName("Should set and get enabled")
        void shouldSetAndGetEnabled() {
            LoginMethodsDTO.AppAccountConfig config = new LoginMethodsDTO.AppAccountConfig();
            
            config.setEnabled(true);
            assertTrue(config.isEnabled());
            
            config.setEnabled(false);
            assertFalse(config.isEnabled());
        }

        @Test
        @DisplayName("Should set and get allowRegistration")
        void shouldSetAndGetAllowRegistration() {
            LoginMethodsDTO.AppAccountConfig config = new LoginMethodsDTO.AppAccountConfig();
            
            config.setAllowRegistration(true);
            assertTrue(config.isAllowRegistration());
            
            config.setAllowRegistration(false);
            assertFalse(config.isAllowRegistration());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get appAccount")
        void shouldSetAndGetAppAccount() {
            LoginMethodsDTO.AppAccountConfig config = 
                new LoginMethodsDTO.AppAccountConfig(true, true);
            
            dto.setAppAccount(config);
            
            assertNotNull(dto.getAppAccount());
            assertTrue(dto.getAppAccount().isEnabled());
            assertTrue(dto.getAppAccount().isAllowRegistration());
        }

        @Test
        @DisplayName("Should set and get ldapEnabled")
        void shouldSetAndGetLdapEnabled() {
            dto.setLdapEnabled(true);
            assertTrue(dto.isLdapEnabled());
            
            dto.setLdapEnabled(false);
            assertFalse(dto.isLdapEnabled());
        }

        @Test
        @DisplayName("Should set and get oauth2Enabled")
        void shouldSetAndGetOauth2Enabled() {
            dto.setOauth2Enabled(true);
            assertTrue(dto.isOauth2Enabled());
            
            dto.setOauth2Enabled(false);
            assertFalse(dto.isOauth2Enabled());
        }
    }

    @Nested
    @DisplayName("Configuration Scenarios Tests")
    class ConfigurationScenariosTests {

        @Test
        @DisplayName("Should represent all methods enabled")
        void shouldRepresentAllMethodsEnabled() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(true, true);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, true, true);
            
            assertTrue(loginMethods.getAppAccount().isEnabled());
            assertTrue(loginMethods.getAppAccount().isAllowRegistration());
            assertTrue(loginMethods.isLdapEnabled());
            assertTrue(loginMethods.isOauth2Enabled());
        }

        @Test
        @DisplayName("Should represent all methods disabled")
        void shouldRepresentAllMethodsDisabled() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(false, false);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, false, false);
            
            assertFalse(loginMethods.getAppAccount().isEnabled());
            assertFalse(loginMethods.getAppAccount().isAllowRegistration());
            assertFalse(loginMethods.isLdapEnabled());
            assertFalse(loginMethods.isOauth2Enabled());
        }

        @Test
        @DisplayName("Should represent only App Account enabled with registration")
        void shouldRepresentOnlyAppAccountWithRegistration() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(true, true);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, false, false);
            
            assertTrue(loginMethods.getAppAccount().isEnabled());
            assertTrue(loginMethods.getAppAccount().isAllowRegistration());
            assertFalse(loginMethods.isLdapEnabled());
            assertFalse(loginMethods.isOauth2Enabled());
        }

        @Test
        @DisplayName("Should represent App Account enabled but registration disabled")
        void shouldRepresentAppAccountNoRegistration() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(true, false);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, false, false);
            
            assertTrue(loginMethods.getAppAccount().isEnabled());
            assertFalse(loginMethods.getAppAccount().isAllowRegistration());
        }

        @Test
        @DisplayName("Should represent only LDAP enabled")
        void shouldRepresentOnlyLdapEnabled() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(false, false);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, true, false);
            
            assertFalse(loginMethods.getAppAccount().isEnabled());
            assertTrue(loginMethods.isLdapEnabled());
            assertFalse(loginMethods.isOauth2Enabled());
        }

        @Test
        @DisplayName("Should represent only OAuth2 enabled")
        void shouldRepresentOnlyOauth2Enabled() {
            LoginMethodsDTO.AppAccountConfig appAccount = 
                new LoginMethodsDTO.AppAccountConfig(false, false);
            
            LoginMethodsDTO loginMethods = new LoginMethodsDTO(appAccount, false, true);
            
            assertFalse(loginMethods.getAppAccount().isEnabled());
            assertFalse(loginMethods.isLdapEnabled());
            assertTrue(loginMethods.isOauth2Enabled());
        }
    }
}
