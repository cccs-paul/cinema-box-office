/*
 * myRC - Responsibility Centre Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.CategoryRepository;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.FundingItemRepository;
import com.boxoffice.repository.MoneyAllocationRepository;
import com.boxoffice.repository.MoneyRepository;
import com.boxoffice.repository.ProcurementItemRepository;
import com.boxoffice.repository.ProcurementQuoteFileRepository;
import com.boxoffice.repository.ProcurementQuoteRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.SpendingCategoryRepository;
import com.boxoffice.repository.SpendingItemRepository;
import com.boxoffice.repository.SpendingMoneyAllocationRepository;
import com.boxoffice.repository.UserRepository;
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

  private ResponsibilityCentreServiceImpl service;

  private User testUser;
  private ResponsibilityCentre testRC;

  @BeforeEach
  void setUp() throws Exception {
    service = new ResponsibilityCentreServiceImpl(
        rcRepository,
        accessRepository,
        userRepository,
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
        procurementQuoteFileRepository
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
      when(rcRepository.existsByNameAndOwner("Test RC", testUser)).thenReturn(false);
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
    @DisplayName("Should throw exception when duplicate name")
    void testCreateResponsibilityCentreDuplicateName() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.existsByNameAndOwner("Test RC", testUser)).thenReturn(true);

      assertThrows(IllegalArgumentException.class,
          () -> service.createResponsibilityCentre("testuser", "Test RC", "desc"));
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

      List<ResponsibilityCentreDTO> result = service.getUserResponsibilityCentres("testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Test RC", result.get(0).getName());
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

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Test RC", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when RC not found")
    void testGetResponsibilityCentreNotFound() {
      when(rcRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<ResponsibilityCentreDTO> result = service.getResponsibilityCentre(999L, "testuser");

      assertFalse(result.isPresent());
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
      when(rcRepository.save(any(ResponsibilityCentre.class))).thenReturn(testRC);

      Optional<ResponsibilityCentreDTO> result = service.updateResponsibilityCentre(1L, "testuser",
          "Updated RC", "Updated description");

      assertTrue(result.isPresent());
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
}
