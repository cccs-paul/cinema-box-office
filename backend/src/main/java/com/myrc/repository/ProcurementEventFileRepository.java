/*
 * myRC - Procurement Event File Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-04
 * Version: 1.0.0
 *
 * Description:
 * Repository interface for Procurement Event File entity.
 */
package com.myrc.repository;

import com.myrc.model.ProcurementEventFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for Procurement Event File entity.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-04
 */
@Repository
public interface ProcurementEventFileRepository extends JpaRepository<ProcurementEventFile, Long> {

    /**
     * Find all active files for a specific event.
     *
     * @param eventId the event ID
     * @return list of active files
     */
    List<ProcurementEventFile> findByEventIdAndActiveTrue(Long eventId);

    /**
     * Find all files for a specific event.
     *
     * @param eventId the event ID
     * @return list of all files
     */
    List<ProcurementEventFile> findByEventId(Long eventId);

    /**
     * Count active files for a specific event.
     *
     * @param eventId the event ID
     * @return count of active files
     */
    long countByEventIdAndActiveTrue(Long eventId);

    /**
     * Delete all files for a specific event.
     *
     * @param eventId the event ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProcurementEventFile f WHERE f.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    /**
     * Soft delete all files for a specific event by setting active to false.
     *
     * @param eventId the event ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProcurementEventFile f SET f.active = false WHERE f.event.id = :eventId")
    void softDeleteByEventId(@Param("eventId") Long eventId);

    /**
     * Delete all event files for all events belonging to a procurement item.
     * Used during cascade deletion of a responsibility centre.
     *
     * @param procurementItemId the procurement item ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProcurementEventFile f WHERE f.event.id IN "
         + "(SELECT e.id FROM ProcurementEvent e WHERE e.procurementItem.id = :procurementItemId)")
    void deleteByProcurementItemId(@Param("procurementItemId") Long procurementItemId);
}
