package com.boxoffice.repository;

import com.boxoffice.model.ResponsibilityCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResponsibilityCentreRepository extends JpaRepository<ResponsibilityCentre, Long> {
    List<ResponsibilityCentre> findByOwnerUsername(String ownerUsername);
    boolean existsByName(String name);
    Optional<ResponsibilityCentre> findByName(String name);
}
