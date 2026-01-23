/*
 * myRC - Responsibility Centre Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.repository;

import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ResponsibilityCentre entity.
 * Provides CRUD operations and custom queries for responsibility centres.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@Repository
public interface ResponsibilityCentreRepository extends JpaRepository<ResponsibilityCentre, Long> {

  /**
   * Find all responsibility centres owned by a specific user.
   *
   * @param owner the owner user
   * @return list of responsibility centres owned by the user
   */
  List<ResponsibilityCentre> findByOwner(User owner);

  /**
   * Find a responsibility centre by name and owner.
   *
   * @param name the name of the responsibility centre
   * @param owner the owner user
   * @return optional containing the responsibility centre if found
   */
  Optional<ResponsibilityCentre> findByNameAndOwner(String name, User owner);

  /**
   * Check if a responsibility centre with the given name and owner exists.
   *
   * @param name the name of the responsibility centre
   * @param owner the owner user
   * @return true if exists, false otherwise
   */
  boolean existsByNameAndOwner(String name, User owner);

  /**
   * Find all active responsibility centres owned by a specific user.
   *
   * @param owner the owner user
   * @param active the active status
   * @return list of active responsibility centres owned by the user
   */
  List<ResponsibilityCentre> findByOwnerAndActive(User owner, Boolean active);
}
