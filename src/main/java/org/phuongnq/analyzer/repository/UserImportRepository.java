package org.phuongnq.analyzer.repository;

import java.time.LocalDate;
import org.phuongnq.analyzer.repository.entity.UserImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserImportRepository extends JpaRepository<UserImport, Long> {

    @Query(value = """
        SELECT DISTINCT COUNT(name) = 2 FROM userImport WHERE sId = ?1 AND dataDate >= ?2 and (createdTime::date) >= ?3
        """, nativeQuery = true)
    boolean hasBothImportByDataDate(Long sId, LocalDate businessDate, LocalDate today);
}

