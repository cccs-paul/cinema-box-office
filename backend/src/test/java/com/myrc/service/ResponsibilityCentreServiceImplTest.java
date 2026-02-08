/*
 * myRC - Responsibility Centre Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.ResponsibilityCentreDTO;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.MoneyAllocationRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import com.myrc.repository.UserRepository;
import com.myrc.service.UserService;
import com.myrc.model.FiscalYear;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ResponsibilityCentreServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class ResponsibilityCentreServiceImplTest {

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserService userService;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private FundingItemRepository fundingItemRepository;

  @Mock
  private SpendingItemRepository spendingItemRepository;

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private SpendingCategoryRepository spendingCategoryRepository;

  @Mock
  private CategoryRepository fundingCategoryRepository;

  @Mock
  private MoneyAllocationRepository moneyAllocationRepository;

  @Mock
  private SpendingMoneyAllocationRepository spendingMoneyAllocationRepository;

  @Mock
  private ProcurementItemRepository procurementItemRepository;

  @Mock
  private ProcurementQuoteRepository procurementQuoteRepository;

  @Mock
  private ProcurementQuoteFileRepository procurementQuoteFileRepository;

  @Mock
  private FiscalYearCloneService fiscalYearCloneService;

  private ResponsibilityCentreServiceImpl service;

  private User testUser;
  private ResponsibilityCentre testRC;

  @BeforeEach
  void setUp() throws Exception {
    service = new ResponsibilityCentreServiceImpl(
        rcRepository,
        accessRepository,
        userRepository,
        userService,
        fiscalYearRepository,
        fundingItemRepository,
        spendingItemRepository,
        moneyRepository,
        spendingCategoryRepository,
        fundingCategoryRepository,
        moneyAllocationRepository,
        spendingMoneyAllocationRepository,
        procurementItemRepository,
        procurementQuoteRepository,
        procurementQuoteFileRepository,
        fiscalYearCloneService
    );

    // Use reflection to set the entityManager field with a no-op implementation
    Field emField = ResponsibilityCentreServiceImpl.class.getDeclaredField("entityManager");
    emField.setAccessible(true);
    emField.set(service, createNoOpEntityManager());

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setDescription("Test description");
    testRC.setOwner(testUser);
  }

  /**
   * Create a minimal EntityManager that does nothing for flush() and clear().
   */
  private jakarta.persistence.EntityManager createNoOpEntityManager() {
    return new jakarta.persistence.EntityManager() {
      @Override public void persist(Object entity) { }
      @Override public <T> T merge(T entity) { return entity; }
      @Override public void remove(Object entity) { }
      @Override public <T> T find(Class<T> entityClass, Object primaryKey) { return null; }
      @Override public <T> T find(Class<T> entityClass, Object primaryKey, java.util.Map<String, Object> properties) { return null; }
      @Override public <T> T find(Class<T> entityClass, Object primaryKey, jakarta.persistence.LockModeType lockMode) { return null; }
      @Override public <T> T find(Class<T> entityClass, Object primaryKey, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { return null; }
      @Override public <T> T getReference(Class<T> entityClass, Object primaryKey) { return null; }
      @Override public void flush() { }
      @Override public void setFlushMode(jakarta.persistence.FlushModeType flushMode) { }
      @Override public jakarta.persistence.FlushModeType getFlushMode() { return jakarta.persistence.FlushModeType.AUTO; }
      @Override public void lock(Object entity, jakarta.persistence.LockModeType lockMode) { }
      @Override public void lock(Object entity, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { }
      @Override public void refresh(Object entity) { }
      @Override public void refresh(Object entity, java.util.Map<String, Object> properties) { }
      @Override public void refresh(Object entity, jakarta.persistence.LockModeType lockMode) { }
      @Override public void refresh(Object entity, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { }
      @Override public void clear() { }
      @Override public void detach(Object entity) { }
      @Override public boolean contains(Object entity) { return false; }
      @Override public jakarta.persistence.LockModeType getLockMode(Object entity) { return null; }
      @Override public void setProperty(String propertyName, Object value) { }
      @Override public java.util.Map<String, Object> getProperties() { return java.util.Collections.emptyMap(); }
      @Override public jakarta.persistence.Query createQuery(String qlString) { return null; }
      @Override public <T> jakarta.persistence.TypedQuery<T> createQuery(jakarta.persistence.criteria.CriteriaQuery<T> criteriaQuery) { return null; }
      @Override public jakarta.persistence.Query createQuery(jakarta.persistence.criteria.CriteriaUpdate updateQuery) { return null; }
      @Override public jakarta.persistence.Query createQuery(jakarta.persistence.criteria.CriteriaDelete deleteQuery) { return null; }
      @Override public <T> jakarta.persistence.TypedQuery<T> createQuery(String qlString, Class<T> resultClass) { return null; }
      @Override public jakarta.persistence.Query createNamedQuery(String name) { return null; }
      @Override public <T> jakarta.persistence.TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) { return null; }
      @Override public jakarta.persistence.Query createNativeQuery(String sqlString) { return null; }
      @Override public jakarta.persistence.Query createNativeQuery(String sqlString, Class resultClass) { return null; }
      @Override public jakarta.persistence.Query createNativeQuery(String sqlString, String resultSetMapping) { return null; }
      @Override public jakarta.persistence.StoredProcedureQuery createNamedStoredProcedureQuery(String name) { return null; }
      @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName) { return null; }
      @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) { return null; }
      @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) { return null; }
      @Override public void joinTransaction() { }
      @Override public boolean isJoinedToTransaction() { return false; }
      @Override public <T> T unwrap(Class<T> cls) { return null; }
      @Override public Object getDelegate() { return null; }
      @Override public void close() { }
      @Override public boolean isOpen() { return true; }
      @Override public jakarta.persistence.EntityTransaction getTransaction() { return null; }
      @Override public jakarta.persistence.EntityManagerFactory getEntityManagerFactory() { return null; }
      @Override public jakarta.persistence.criteria.CriteriaBuilder getCriteriaBuilder() { return null; }
      @Override public jakarta.persistence.metamodel.Metamodel getMetamodel() { return null; }
      @Override public <T> jakarta.persistence.EntityGraph<T> createEntityGraph(Class<T> rootType) { return null; }
      @Override public jakarta.persistence.EntityGraph<?> createEntityGraph(String graphName) { return null; }
      @Override public jakarta.persistence.EntityGraph<?> getEntityGraph(String graphName) { return null; }
      @Override public <T> java.util.List<jakarta.persistence.EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) { return null; }
    };
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(service);
  }

  @Nested
  @DisplayName("createResponsibilityCentre Tests")
  class CreateTests {

    @Test
    @DisplayName("Should create RC successfully")
    void testCreateResponsibilityCentre() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.existsByName("Test RC")).thenReturn(false);
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

      ResponsibilityCentreDTO result = service.createResponsibilityCentre("testuser", "Test RC",
          "Test description");

      assertNotNull(result);
      assertEquals("Test RC", result.getName());
      assertEquals("testuser", result.getOwnerUsername());
      assertTrue(result.isOwner());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateResponsibilityCentreUserNotFound() {
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> service.createResponsibilityCentre("nonexistent", "RC", "desc"));
    }

    @Test
    @DisplayName("Should throw exception when duplicate name globally")
    void testCreateResponsibilityCentreDuplicateName() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.existsByName("Test RC")).thenReturn(true);

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.createResponsibilityCentre("testuser", "Test RC", "desc"));
      assertTrue(exception.getMessage().contains("already exists"));
      assertTrue(exception.getMessage().contains("must be unique"));
    }

    @Test
    @DisplayName("Should auto-provision LDAP user and create RC when user not found locally")
    void testCreateResponsibilityCentreAutoProvisionLdapUser() {
      // First call: user not found; second call (after provisioning): user found
      when(userRepository.findByUsername("fry"))
          .thenReturn(Optional.empty())
          .thenReturn(Optional.of(testUser));

      // Set up security context with LDAP group authority
      org.springframework.security.core.context.SecurityContext secCtx =
          org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
      secCtx.setAuthentication(
          new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
              "fry", null,
              List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                  "LDAP_GROUP_DN_cn=ship_crew,ou=people,dc=planetexpress,dc=com"))));
      org.springframework.security.core.context.SecurityContextHolder.setContext(secCtx);

      try {
        when(rcRepository.existsByName("Fry RC")).thenReturn(false);
        when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

        ResponsibilityCentreDTO result = service.createResponsibilityCentre("fry", "Fry RC", "Fry's RC");

        assertNotNull(result);
        verify(userService).createOrUpdateLdapUser("fry", null, null, "fry");
      } finally {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
      }
    }
  }

  @Nested
  @DisplayName("getUserResponsibilityCentres Tests")
  class GetUserRCsTests {

    @Test
    @DisplayName("Should return user RCs")
    void testGetUserResponsibilityCentres() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findByOwner(testUser)).thenReturn(List.of(testRC));
      when(accessRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
      when(rcRepository.findByName("Demo")).thenReturn(Optional.empty());

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("testuser", List.of());

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Test RC", result.get(0).getName());
    }

    @Test
    @DisplayName("Should return RCs accessible via group membership")
    void testGetUserResponsibilityCentresViaGroup() {
      // LDAP user with no local User entity
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());

      // Group-based access
      ResponsibilityCentre groupRC = new ResponsibilityCentre("Group RC", "Accessible via group", testUser);
      groupRC.setId(2L);
      RCAccess groupAccess = new RCAccess();
      groupAccess.setResponsibilityCentre(groupRC);
      groupAccess.setPrincipalIdentifier("cn=ship_crew,ou=people,dc=planetexpress,dc=com");
      groupAccess.setPrincipalType(RCAccess.PrincipalType.GROUP);
      groupAccess.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);

      List<String> groupDns = List.of("cn=ship_crew,ou=people,dc=planetexpress,dc=com");
      when(accessRepository.findByPrincipalIdentifierIn(anyList()))
          .thenReturn(List.of(groupAccess));
      when(rcRepository.findByName("Demo")).thenReturn(Optional.empty());

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("ldapuser", groupDns);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Group RC", result.get(0).getName());
    }

    @Test
    @DisplayName("Should not duplicate RCs when user has both FK and group access")
    void testGetUserResponsibilityCentresNoDuplicates() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findByOwner(testUser)).thenReturn(List.of(testRC));
      when(accessRepository.findByUser(testUser)).thenReturn(new ArrayList<>());

      // Same RC also accessible via group
      RCAccess groupAccess = new RCAccess();
      groupAccess.setResponsibilityCentre(testRC);
      groupAccess.setPrincipalIdentifier("cn=group1,ou=groups,dc=example,dc=com");
      groupAccess.setPrincipalType(RCAccess.PrincipalType.GROUP);
      groupAccess.setAccessLevel(RCAccess.AccessLevel.READ_ONLY);

      when(accessRepository.findByPrincipalIdentifierIn(anyList()))
          .thenReturn(List.of(groupAccess));
      when(rcRepository.findByName("Demo")).thenReturn(Optional.empty());

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("testuser",
          List.of("cn=group1,ou=groups,dc=example,dc=com"));

      assertNotNull(result);
      assertEquals(1, result.size(), "Should not have duplicate entries for the same RC");
    }

    @Test
    @DisplayName("Should return empty list for unknown user with no group access and no Demo RC")
    void testGetUserResponsibilityCentresNoAccess() {
      when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
      when(accessRepository.findByPrincipalIdentifierIn(anyList())).thenReturn(List.of());
      when(rcRepository.findByName("Demo")).thenReturn(Optional.empty());

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("unknown", List.of());

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should always include Demo RC with READ_ONLY access for any authenticated user")
    void testGetUserResponsibilityCentresDemoRCAlwaysVisible() {
      // LDAP user with no local entity and no explicit access
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());
      when(accessRepository.findByPrincipalIdentifierIn(anyList())).thenReturn(List.of());

      // Demo RC exists
      ResponsibilityCentre demoRC = new ResponsibilityCentre("Demo",
          "Demo responsibility centre", testUser);
      demoRC.setId(99L);
      when(rcRepository.findByName("Demo")).thenReturn(Optional.of(demoRC));

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("ldapuser", List.of());

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Demo", result.get(0).getName());
      assertEquals("READ_ONLY", result.get(0).getAccessLevel());
    }

    @Test
    @DisplayName("Should not duplicate Demo RC when user already has explicit access")
    void testGetUserResponsibilityCentresDemoRCNoDuplicate() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      // User owns the Demo RC
      ResponsibilityCentre demoRC = new ResponsibilityCentre("Demo",
          "Demo responsibility centre", testUser);
      demoRC.setId(99L);
      when(rcRepository.findByOwner(testUser)).thenReturn(List.of(demoRC));
      when(accessRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
      when(rcRepository.findByName("Demo")).thenReturn(Optional.of(demoRC));

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("testuser", List.of());

      assertNotNull(result);
      assertEquals(1, result.size(), "Demo RC should not appear twice");
      assertEquals("Demo", result.get(0).getName());
      assertEquals("READ_ONLY", result.get(0).getAccessLevel());
    }
  }

  @Nested
  @DisplayName("getResponsibilityCentre Tests")
  class GetRCTests {

    @Test
    @DisplayName("Should return RC when user is owner")
    void testGetResponsibilityCentre() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(1L, "testuser", List.of());

      assertTrue(result.isPresent());
      assertEquals("Test RC", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when RC not found")
    void testGetResponsibilityCentreNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(999L, "testuser", List.of());

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return RC when LDAP user has group-based access")
    void testGetResponsibilityCentreViaGroup() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());

      RCAccess groupAccess = new RCAccess();
      groupAccess.setResponsibilityCentre(testRC);
      groupAccess.setPrincipalIdentifier("cn=ship_crew,ou=people,dc=planetexpress,dc=com");
      groupAccess.setPrincipalType(RCAccess.PrincipalType.GROUP);
      groupAccess.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);

      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierIn(eq(testRC), anyList()))
          .thenReturn(List.of(groupAccess));

      List<String> groupDns = List.of("cn=ship_crew,ou=people,dc=planetexpress,dc=com");
      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(1L, "ldapuser", groupDns);

      assertTrue(result.isPresent());
      assertEquals("Test RC", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when LDAP user has no group-based access")
    void testGetResponsibilityCentreNoGroupAccess() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierIn(eq(testRC), anyList()))
          .thenReturn(List.of());

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(1L, "ldapuser", List.of());

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return Demo RC with READ_ONLY access for any authenticated user")
    void testGetResponsibilityCentreDemoRCAlwaysAccessible() {
      ResponsibilityCentre demoRC = new ResponsibilityCentre("Demo",
          "Demo responsibility centre", testUser);
      demoRC.setId(99L);

      when(rcRepository.findById(99L)).thenReturn(Optional.of(demoRC));
      when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());
      when(accessRepository.findByResponsibilityCentreAndPrincipalIdentifierIn(eq(demoRC), anyList()))
          .thenReturn(List.of());

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(99L, "ldapuser", List.of());

      assertTrue(result.isPresent());
      assertEquals("Demo", result.get().getName());
      assertEquals("READ_ONLY", result.get().getAccessLevel());
    }
  }

  @Nested
  @DisplayName("updateResponsibilityCentre Tests")
  class UpdateTests {

    @Test
    @DisplayName("Should update RC successfully")
    void testUpdateResponsibilityCentre() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.existsByNameAndIdNot("Updated RC", 1L)).thenReturn(false);
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

      Optional<ResponsibilityCentreDTO> result = service.updateResponsibilityCentre(1L, "testuser",
          "Updated RC", "Updated description");

      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should allow updating with same name")
    void testUpdateResponsibilityCentreSameName() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      // No need to mock existsByNameAndIdNot because name hasn't changed
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

      Optional<ResponsibilityCentreDTO> result = service.updateResponsibilityCentre(1L, "testuser",
          "Test RC", "Updated description only");

      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should throw exception when renaming to existing name")
    void testUpdateResponsibilityCentreDuplicateName() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.existsByNameAndIdNot("Existing RC", 1L)).thenReturn(true);

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.updateResponsibilityCentre(1L, "testuser", "Existing RC", "desc"));
      assertTrue(exception.getMessage().contains("already exists"));
      assertTrue(exception.getMessage().contains("must be unique"));
    }
  }

  @Nested
  @DisplayName("deleteResponsibilityCentre Tests")
  class DeleteTests {

    @Test
    @DisplayName("Should delete RC successfully")
    void testDeleteResponsibilityCentre() {
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      boolean result = service.deleteResponsibilityCentre(1L, "testuser");

      assertTrue(result);
      verify(accessRepository).deleteByResponsibilityCentre(testRC);
      verify(rcRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should return false when RC not found")
    void testDeleteResponsibilityCentreNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      boolean result = service.deleteResponsibilityCentre(999L, "testuser");

      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("grantAccess Tests")
  class GrantAccessTests {

    @Test
    @DisplayName("Should grant access successfully")
    void testGrantAccess() {
      User grantedToUser = new User();
      grantedToUser.setId(2L);
      grantedToUser.setUsername("grantedto");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(userRepository.findByUsername("grantedto")).thenReturn(Optional.of(grantedToUser));
      when(accessRepository.save(any(RCAccess.class))).thenReturn(new RCAccess(testRC, grantedToUser,
          RCAccess.AccessLevel.READ_ONLY));

      Optional<RCAccess> result = service.grantAccess(1L, "testuser", "grantedto", "READ_ONLY");

      assertTrue(result.isPresent());
      assertEquals(RCAccess.AccessLevel.READ_ONLY, result.get().getAccessLevel());
    }
  }

  @Nested
  @DisplayName("revokeAccess Tests")
  class RevokeAccessTests {

    @Test
    @DisplayName("Should revoke access successfully")
    void testRevokeAccess() {
      User grantedToUser = new User();
      grantedToUser.setId(2L);
      grantedToUser.setUsername("grantedto");

      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(userRepository.findByUsername("grantedto")).thenReturn(Optional.of(grantedToUser));

      boolean result = service.revokeAccess(1L, "testuser", "grantedto");

      assertTrue(result);
      verify(accessRepository).deleteByResponsibilityCentreAndUser(testRC, grantedToUser);
    }
  }

  @Nested
  @DisplayName("cloneResponsibilityCentre Tests")
  class CloneTests {

    @Test
    @DisplayName("Should clone RC successfully with deep clone of fiscal years")
    void testCloneResponsibilityCentre() {
      ResponsibilityCentre clonedRc = new ResponsibilityCentre();
      clonedRc.setId(2L);
      clonedRc.setName("Cloned RC");
      clonedRc.setDescription("Test description");
      clonedRc.setOwner(testUser);

      FiscalYear sourceFY = new FiscalYear("FY 2025", "Test FY", testRC);
      sourceFY.setId(10L);

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(rcRepository.existsByName("Cloned RC")).thenReturn(false);
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(clonedRc);
      when(fiscalYearRepository.findByResponsibilityCentreId(1L))
          .thenReturn(List.of(sourceFY));

      FiscalYear clonedFY = new FiscalYear("FY 2025", "Test FY", clonedRc);
      clonedFY.setId(20L);
      when(fiscalYearCloneService.deepCloneFiscalYear(eq(sourceFY), eq("FY 2025"), any()))
          .thenReturn(clonedFY);

      ResponsibilityCentreDTO result = service.cloneResponsibilityCentre(1L, "testuser", "Cloned RC");

      assertNotNull(result);
      assertEquals("Cloned RC", result.getName());
      assertEquals("testuser", result.getOwnerUsername());
      verify(fiscalYearCloneService).deepCloneFiscalYear(eq(sourceFY), eq("FY 2025"), any());
    }

    @Test
    @DisplayName("Should throw exception when clone name already exists globally")
    void testCloneResponsibilityCentreDuplicateName() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(testRC));
      when(rcRepository.existsByName("Existing RC")).thenReturn(true);

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.cloneResponsibilityCentre(1L, "testuser", "Existing RC"));
      assertTrue(exception.getMessage().contains("already exists"));
      assertTrue(exception.getMessage().contains("must be unique"));
    }

    @Test
    @DisplayName("Should allow cloning Demo RC even without explicit RCAccess record")
    void testCloneDemoRcWithoutExplicitAccess() {
      // Create Demo RC owned by a different user
      User otherUser = new User();
      otherUser.setId(99L);
      otherUser.setUsername("otheruser");

      ResponsibilityCentre demoRc = new ResponsibilityCentre();
      demoRc.setId(42L);
      demoRc.setName("Demo");
      demoRc.setDescription("Demo RC");
      demoRc.setOwner(otherUser);

      ResponsibilityCentre clonedRc = new ResponsibilityCentre();
      clonedRc.setId(43L);
      clonedRc.setName("Demo (Copy)");
      clonedRc.setOwner(testUser);

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(42L)).thenReturn(Optional.of(demoRc));
      when(accessRepository.findByResponsibilityCentreAndUser(demoRc, testUser))
          .thenReturn(Optional.empty());
      when(rcRepository.existsByName("Demo (Copy)")).thenReturn(false);
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(clonedRc);
      when(fiscalYearRepository.findByResponsibilityCentreId(42L))
          .thenReturn(List.of());

      ResponsibilityCentreDTO result = service.cloneResponsibilityCentre(
          42L, "testuser", "Demo (Copy)");

      assertNotNull(result);
      assertEquals("Demo (Copy)", result.getName());
    }
  }
}
