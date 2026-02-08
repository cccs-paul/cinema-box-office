/*
 * myRC - RC Access Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.RCAccess;
import com.myrc.model.RCAccess.AccessLevel;
import com.myrc.model.RCAccess.PrincipalType;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for RCAccess entity.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Repository
public interface RCAccessRepository extends JpaRepository<RCAccess, Long> {

  /**
   * Find access records for a user in a specific responsibility centre.
   *
   * @param rc the responsibility centre
   * @param user the user
   * @return optional containing the access record if found
   */
  Optional<RCAccess> findByResponsibilityCentreAndUser(ResponsibilityCentre rc, User user);

  /**
   * Find all access records for a user (excluding owned RCs).
   *
   * @param user the user
   * @return list of access records for the user
   */
  @Query("SELECT access FROM RCAccess access WHERE access.user = :user")
  List<RCAccess> findByUser(@Param("user") User user);

  /**
   * Find all access records for a responsibility centre.
   *
   * @param rc the responsibility centre
   * @return list of access records for the RC
   */
  List<RCAccess> findByResponsibilityCentre(ResponsibilityCentre rc);

  /**
   * Check if a user has access to a specific responsibility centre.
   *
   * @param rcId the RC ID
   * @param userId the user ID
   * @return true if the user has access
   */
  @Query("SELECT CASE WHEN COUNT(access) > 0 THEN true ELSE false END FROM RCAccess access " +
      "WHERE access.responsibilityCentre.id = :rcId AND access.user.id = :userId")
  boolean hasAccess(@Param("rcId") Long rcId, @Param("userId") Long userId);

  /**
   * Delete access record for a user in a specific RC.
   *
   * @param rc the responsibility centre
   * @param user the user
   */
  @Modifying
  @Query("DELETE FROM RCAccess a WHERE a.responsibilityCentre = :rc AND a.user = :user")
  void deleteByResponsibilityCentreAndUser(@Param("rc") ResponsibilityCentre rc, @Param("user") User user);

  /**
   * Delete all access records for a responsibility centre.
   *
   * @param rc the responsibility centre
   */
  @Modifying
  @Query("DELETE FROM RCAccess a WHERE a.responsibilityCentre = :rc")
  void deleteByResponsibilityCentre(@Param("rc") ResponsibilityCentre rc);

  /**
   * Find access by responsibility centre and principal identifier (for groups/distribution lists).
   *
   * @param rc the responsibility centre
   * @param principalIdentifier the principal identifier
   * @param principalType the type of principal
   * @return optional containing the access record if found
   */
  Optional<RCAccess> findByResponsibilityCentreAndPrincipalIdentifierAndPrincipalType(
      ResponsibilityCentre rc, String principalIdentifier, PrincipalType principalType);

  /**
   * Find all access records for a responsibility centre with a specific principal type.
   *
   * @param rc the responsibility centre
   * @param principalType the type of principal
   * @return list of access records
   */
  List<RCAccess> findByResponsibilityCentreAndPrincipalType(ResponsibilityCentre rc, PrincipalType principalType);

  /**
   * Find all access records matching a list of principal identifiers.
   * Covers group membership, distribution lists, and LDAP users stored without a User FK.
   *
   * @param identifiers list of principal identifiers (group DNs, distribution lists, or usernames)
   * @return list of access records matching the identifiers
   */
  @Query("SELECT a FROM RCAccess a WHERE a.principalIdentifier IN :identifiers AND a.principalType IN ('USER', 'GROUP', 'DISTRIBUTION_LIST')")
  List<RCAccess> findByPrincipalIdentifierIn(@Param("identifiers") List<String> identifiers);

  /**
   * Find all access records matching a list of principal identifiers for a specific RC.
   * Used to check group-based or principalIdentifier-based access when the user
   * has no local User entity (e.g. LDAP users without auto-provisioning).
   *
   * @param rc the responsibility centre
   * @param identifiers list of identifiers (username + group DNs)
   * @return list of access records matching the identifiers in the given RC
   */
  @Query("SELECT a FROM RCAccess a WHERE a.responsibilityCentre = :rc " +
      "AND a.principalIdentifier IN :identifiers " +
      "AND a.principalType IN ('USER', 'GROUP', 'DISTRIBUTION_LIST')")
  List<RCAccess> findByResponsibilityCentreAndPrincipalIdentifierIn(
      @Param("rc") ResponsibilityCentre rc, @Param("identifiers") List<String> identifiers);

  /**
   * Find all access records for a user in a specific RC.
   * Matches direct User FK access, and access by principalIdentifier for users, groups,
   * and distribution lists (covers LDAP users stored without a local User entity).
   *
   * @param rc the responsibility centre
   * @param user the user
   * @param identifiers list of identifiers (username + group DNs)
   * @return list of access records matching either direct user access or identifier match
   */
  @Query("SELECT a FROM RCAccess a WHERE a.responsibilityCentre = :rc AND " +
      "(a.user = :user OR (a.principalIdentifier IN :identifiers AND a.principalType IN ('USER', 'GROUP', 'DISTRIBUTION_LIST')))")
  List<RCAccess> findAllAccessForUserInRC(@Param("rc") ResponsibilityCentre rc, 
      @Param("user") User user, @Param("identifiers") List<String> identifiers);

  /**
   * Delete access record by ID.
   *
   * @param id the access record ID
   */
  @Modifying
  @Query("DELETE FROM RCAccess a WHERE a.id = :id")
  void deleteAccessById(@Param("id") Long id);

  /**
   * Count owners for a responsibility centre.
   *
   * @param rc the responsibility centre
   * @return count of owners
   */
  @Query("SELECT COUNT(a) FROM RCAccess a WHERE a.responsibilityCentre = :rc AND a.accessLevel = 'OWNER'")
  long countOwnersByRC(@Param("rc") ResponsibilityCentre rc);

  /**
   * Find all owners for a responsibility centre.
   *
   * @param rc the responsibility centre
   * @return list of owner access records
   */
  @Query("SELECT a FROM RCAccess a WHERE a.responsibilityCentre = :rc AND a.accessLevel = 'OWNER'")
  List<RCAccess> findOwnersByRC(@Param("rc") ResponsibilityCentre rc);
}
