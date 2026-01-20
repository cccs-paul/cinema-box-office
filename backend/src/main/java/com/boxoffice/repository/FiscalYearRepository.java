package com.boxoffice.repository;

import com.boxoffice.model.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {
    List<FiscalYear> findByRcId(Long rcId);
    boolean existsByNameAndRcId(String name, Long rcId);
}
