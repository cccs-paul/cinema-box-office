/*
 * myRC - RC Access Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.repository;

import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
  void deleteByResponsibilityCentreAndUser(ResponsibilityCentre rc, User user);

  /**
   * Delete all access records for a responsibility centre.
   *
   * @param rc the responsibility centre
   */
  void deleteByResponsibilityCentre(ResponsibilityCentre rc);
}
